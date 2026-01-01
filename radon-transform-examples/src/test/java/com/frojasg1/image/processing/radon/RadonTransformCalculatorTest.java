/*
 *
 * MIT License
 *
 * Copyright (c) 2026. Francisco Javier Rojas Garrido <frojasg1@hotmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *
 */

package com.frojasg1.image.processing.radon;


import com.frojasg1.gen.DoubleFunctions;
import com.frojasg1.gen.tuples.Pair;
import com.frojasg1.image.Point2d;
import com.frojasg1.image.gen.geometry.GeometryFunctions;
import com.frojasg1.image.normalized.My2dContainer;
import com.frojasg1.image.processing.radon.result.RadonTransformResult;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class RadonTransformCalculatorTest {


    @Before
    public void setUp() {
        Thread.setDefaultUncaughtExceptionHandler(this::uncaughtException);
    }

    @Test
    public void radonTransform() throws IOException, ExecutionException, InterruptedException, TimeoutException {
        BufferedImage originalImage = ImageIO.read(new File("src/test/resources/scannedBoardInputImage.png"));
        BufferedImage binarizedCanny = ImageIO.read(new File("src/test/resources/BinarizedCanny.tiff"));

        ExecutorService executorService = Executors.newFixedThreadPool(6);

        RadonTransformExecutor radonTransformExecutor = new RadonTransformExecutor(executorService::submit);

        long start = System.currentTimeMillis();

        int timeoutMs = 600_000;

        Future<RadonTransformCalculator> future = radonTransformExecutor.calculateRadonTransform(binarizedCanny, timeoutMs);
        RadonTransformCalculator radonTransform = future.get(timeoutMs, TimeUnit.MILLISECONDS);

        BufferedImage radonTransformImage = radonTransform.createRadonTransformationImage();

        long elapsedMs = System.currentTimeMillis() - start;

        System.out.println("Elapsed ms: " + elapsedMs);

        RadonTransformResult result = radonTransform.getResult();
        My2dContainer radonTransformResult = result.getRadonTransform();

        double thetaTolerance = Math.toRadians(10);
        Point2d maxRhoThetaDeltaForSameLine = createPoint2d(2.0d, Math.toRadians(1));
        List<Pair<Point2d, Double>> rhoThetaLinesValues = result
                .getTopLines(4, rhoTheta -> filterRhoTheta(rhoTheta, thetaTolerance), maxRhoThetaDeltaForSameLine);

        List<Point2d> rhoThetaLines = rhoThetaLinesValues.stream()
                .map(Pair::getKey)
                .collect(Collectors.toList());

        Point2d center = radonTransform.getImageCenter();
        List<Point2d> vertex = calculateVertex(center, rhoThetaLines,
                radonTransform.getWidth(), radonTransform.getHeight());
        List<Point2d> sortedVertex = sortVertexForConvexPolygon(vertex);

        BufferedImage originalImageWithEdges = paintEdges(originalImage, sortedVertex, Color.GREEN, 7);

        BufferedImage originalImageWithFullEdges = paintRhoThetaLines(originalImage, center, rhoThetaLines, Color.GREEN, 7);

        ImageIO.write(radonTransformImage, "png", new File("src/test/resources/radonImage.png"));
        ImageIO.write(originalImageWithEdges, "png", new File("src/test/resources/scannedBoardInputImageWithEdges.png"));
        ImageIO.write(originalImageWithFullEdges, "png", new File("src/test/resources/scannedBoardInputImageWithFullEdges.png"));

        assertTrue(radonTransform.wasSuccessful());
        assertNotNull(result.getMax());
        assertEquals(4, sortedVertex.size());
    }

    // filtering for nearly horizontal or vertical lines
    protected boolean filterRhoTheta(Point2d rhoTheta, double thetaTolerance) {
        double theta = rhoTheta.y;
        return areClose(theta, 0, thetaTolerance) || areClose(theta, Math.PI, thetaTolerance) || // for vertical lines (theta is orthogonal to the line)
                areClose(theta, Math.PI * 0.5d, thetaTolerance); // for horizontal lines
    }

    protected DoubleFunctions getDoubleFunctions() {
        return DoubleFunctions.instance();
    }

    protected boolean areClose(double value1, double value2, double tolerance) {
        return getDoubleFunctions().areClose(value1, value2, tolerance);
    }

    protected List<Point2d> calculateVertex(Point2d center, List<Point2d> rhoThetaLines, int width, int height) {
        return getGeometryFunctions().calculateIntersections(center, rhoThetaLines, width, height);
    }

    protected GeometryFunctions getGeometryFunctions() {
        return GeometryFunctions.instance();
    }

    protected List<Point2d> sortVertexForConvexPolygon(List<Point2d> vertex) {
        return getGeometryFunctions().sortVertexForConvexPolygon(vertex);
    }

    protected BufferedImage copy(BufferedImage image) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D grp = result.createGraphics();
        grp.drawImage(image, 0, 0, null);

        grp.dispose();

        return result;
    }

    protected void paintEdgesInternal(BufferedImage image, List<Point2d> vertex, Color edgesColor, int brushThick) {
        Graphics2D grp = image.createGraphics();

        if (vertex.size() > 1) {
            Stroke stroke = new BasicStroke(brushThick);
            grp.setColor(edgesColor);
            grp.setStroke(stroke);

            Point2d v1 = null;
            Point2d latestVert = null;
            for (Point2d vert : vertex) {
                if (latestVert != null) {
                    drawLine(grp, latestVert, vert);
                }
                latestVert = vert;
                if (v1 == null) {
                    v1 = vert;
                }
            }
            drawLine(grp, latestVert, v1);
        }

        grp.dispose();
    }

    protected BufferedImage paintRhoThetaLines(BufferedImage image,
                                               Point2d center, List<Point2d> rhoThetaLines,
                                               Color edgesColor, int brushThick) {
        BufferedImage result = copy(image);
        for (Point2d rhoTheta: rhoThetaLines) {
            paintEdgesInternal(result, createVertexForRhoTheta(center, rhoTheta), edgesColor, brushThick);
        }

        return result;
    }

    protected List<Point2d> createVertexForRhoTheta(Point2d center, Point2d rhoTheta) {
        List<Point2d> result = new ArrayList<>();
        Point2d nearestPoint = getNearestPoint(center, rhoTheta);
        Point2d lineDirection = getLineNormalizedDirection(rhoTheta);

        result.add(nearestPoint.add(lineDirection.multiplyByScalar(-1000)));
        result.add(nearestPoint.add(lineDirection.multiplyByScalar(1000)));

        return result;
    }

    protected BufferedImage paintEdges(BufferedImage image, List<Point2d> vertex, Color edgesColor, int brushThick) {
        BufferedImage result = copy(image);
        paintEdgesInternal(result, vertex, edgesColor, brushThick);

        return result;
    }

    protected void drawLine(Graphics2D grp, Point2d v1, Point2d v2) {
        grp.drawLine(v1.getIntX(), v1.getIntY(), v2.getIntX(), v2.getIntY());
    }

    protected void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
    }

    protected Point2d getNearestPoint(Point2d center, Point2d rhoTheta) {
        return getGeometryFunctions().getNearestPoint(center, rhoTheta);
    }

    protected Point2d getLineNormalizedDirection(Point2d rhoTheta) {
        return getGeometryFunctions().getLineNormalizedDirection(rhoTheta);
    }

    protected Point2d createPoint2d(double xx, double yy) {
        return Point2d.create(xx, yy);
    }
}