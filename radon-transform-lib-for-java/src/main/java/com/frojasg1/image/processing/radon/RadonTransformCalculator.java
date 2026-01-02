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
import com.frojasg1.gen.concurrent.BasicFuture;
import com.frojasg1.gen.tuples.Pair;
import com.frojasg1.image.Point2d;
import com.frojasg1.image.browsers.BoundingEdge;
import com.frojasg1.image.browsers.BoundingEdgePixelBrowser;
import com.frojasg1.image.browsers.LinePixelsBrowser;
import com.frojasg1.image.gen.ImageFunctions;
import com.frojasg1.image.gen.geometry.GeometryFunctions;
import com.frojasg1.image.helpers.BufferedImageToMyNormalizedImageConverter;
import com.frojasg1.image.normalized.My2dContainer;
import com.frojasg1.image.processing.radon.pixel.RadonIntegrationContext;
import com.frojasg1.image.processing.radon.result.RadonTransformResult;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RadonTransformCalculator {
    private static final Logger LOGGER = LoggerFactory.getLogger(RadonTransformCalculator.class);

    protected static double PI_HALVES = Math.PI * 0.5d;

    protected Point2d imageCenter;
    protected My2dContainer myNormalizedImage;
    protected RadonTransformResult result;

    protected AtomicInteger successesCnt = new AtomicInteger();
    protected AtomicInteger pendingTasksToFinishRef = new AtomicInteger();
    protected int semaphoreNumPermits = -1;
    protected Semaphore semaphore;
    protected Consumer<Runnable> executor;


    protected BasicFuture<RadonTransformCalculator> future;
    protected boolean wasSuccessful;
    protected boolean wasTimeout;
    protected long startTime;
    protected long endTime;

    public RadonTransformCalculator(BasicFuture<RadonTransformCalculator> future) {
        this(future, Runnable::run);
    }

    public RadonTransformCalculator(BasicFuture<RadonTransformCalculator> future, Consumer<Runnable> executor) {
        this.future = future;
        this.executor = executor;
    }

    public void process(BufferedImage image) {
        process(image, 600_000);
    }

    protected boolean imageFulfills(BufferedImage image) {
        return image != null && image.getWidth() > 2 && image.getHeight() > 2;
    }

    protected void resetForStarting() {
        this.startTime = System.currentTimeMillis();
        this.endTime = this.startTime;

        this.wasSuccessful = false;
        this.wasTimeout = false;

        this.myNormalizedImage = null;
        this.imageCenter = null;
        this.result = null;
        this.semaphore = new Semaphore(0);
        this.successesCnt.set(0);
    }

    public synchronized void process(BufferedImage image, int timeoutMs) {

        try {
            if (startTime > 0) {
                throw new IllegalStateException("Already run");
            }

            resetForStarting();

            if (!imageFulfills(image)) {
                throw new IllegalArgumentException("Not suitable image: " + image);
            }

            this.myNormalizedImage = convert(image);
            this.imageCenter = calculateImageCenter();

            this.result = createEmptyRadonTransformResult(image);

            this.semaphoreNumPermits = process(
                    BoundingEdge.NORTH, BoundingEdge.EAST,
                    BoundingEdge.NORTH, BoundingEdge.SOUTH,
                    BoundingEdge.NORTH, BoundingEdge.WEST,
                    BoundingEdge.EAST, BoundingEdge.SOUTH,
                    BoundingEdge.EAST, BoundingEdge.WEST,
                    BoundingEdge.SOUTH, BoundingEdge.WEST
            );

            try {
                wasTimeout = !semaphore.tryAcquire(semaphoreNumPermits, timeoutMs, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ie) {
                LOGGER.info("An Interrupted exception was caught", ie);
                Thread.currentThread().interrupt();
            }

            result.finished();
            if (successesCnt.get() != semaphoreNumPermits) {
                String errorText = String.format("ERROR ( %d / %d )", successesCnt.get(), semaphoreNumPermits);
                LOGGER.error("{}", errorText);
            } else if (wasTimeout()) {
                String errorText = String.format("Timeout ( after %d ms )", getProcessingTimeMs());
                LOGGER.error("{}", errorText);
            } else if (isCancelled()) {
                String errorText = "Cancelled by user";
                LOGGER.error("{}", errorText);
            } else {
                wasSuccessful = true;
                LOGGER.info("Successful radon transform computation");
            }

            this.endTime = System.currentTimeMillis();

            future.completed(this);
        } catch (Exception ex) {
            LOGGER.error("Error calculating radon transform", ex);
            future.failed(ex);
        }
    }

    public BufferedImage createRadonTransformationImage() {
        BufferedImage resultImage = convert(result.getStandardizedRadonTransform());

        if (successesCnt.get() != semaphoreNumPermits) {
            String errorText = String.format("ERROR ( %d / %d )", successesCnt.get(), semaphoreNumPermits);
            paintError(resultImage, errorText);
        } else if (wasTimeout()) {
            String errorText = String.format("Timeout ( after %d ms )", getProcessingTimeMs());
            paintError(resultImage, errorText);
        } else if (isCancelled()) {
            String errorText = "Cancelled by user";
            paintError(resultImage, errorText);
        }

        return resultImage;
    }

    public Pair<Point2d, Double> getMax() {
        Pair<Point2d, Double> result = null;
        RadonTransformResult myResult = getResult();
        if (myResult != null) {
            result = myResult.getMax();
        }
        return result;
    }

    protected Font createFont() {
        return new Font( "Lucida", Font.BOLD, 48 );
    }

    protected Point toPoint(Point2d pt) {
        Point result = null;
        if (pt != null) {
            result = new Point(pt.getIntX(), pt.getIntY());
        }
        return result;
    }

    protected void paintError(BufferedImage result, String errorText) {
        Graphics2D grp = result.createGraphics();
        paintStringCentered(grp, createFont(), errorText, Color.RED, toPoint(imageCenter));

        grp.dispose();
    }

    protected Point2d calculateImageCenter() {
        return createPoint2d((getWidth() - 1) * 0.5d, (getHeight() - 1) * 0.5d);
    }

    public Point2d getImageCenter() {
        return imageCenter;
    }

    protected int process(BoundingEdge... pairs) {
        AtomicInteger numTasksRef = new AtomicInteger();
        if ((pairs.length & 0x1) == 1) {
            throw new IllegalArgumentException("Pairs are not paired: " + Arrays.toString(pairs));
        }

        BoundingEdgePixelBrowser edgePixelBrowser = createBoundingEdgePixelBrowser(myNormalizedImage);

        Iterator<BoundingEdge> it = Arrays.stream(pairs).iterator();
        BoundingEdge first = null;
        while (it.hasNext()) {
            first = it.next();
            BoundingEdge second = it.next();

            edgePixelBrowser
                    .browse(first,
                            pt -> process(pt, second, edgePixelBrowser, numTasksRef));
        }
        return numTasksRef.get();
    }

    protected boolean process(Point2d startPoint,
                              BoundingEdge boundingEdge,
                              BoundingEdgePixelBrowser edgePixelBrowser,
                              AtomicInteger numTasksRef) {
        numTasksRef.incrementAndGet();
        increasePendingTasksToFinish();

        runTask(() ->
                processInternal(startPoint, boundingEdge, edgePixelBrowser));

        return !hasToStopImmediately();
    }

    protected void runTask(Runnable runnable) {
        executor.accept(runnable);
    }

    protected void processInternal(Point2d startPoint,
                                   BoundingEdge boundingEdge,
                                   BoundingEdgePixelBrowser edgePixelBrowser) {
        try {
            RadonIntegrationContext integrationContext = createMyPixelRadonContext(myNormalizedImage);

            edgePixelBrowser
                    .browse(boundingEdge,
                            endPoint -> processInternal(
                                    integrationContext, startPoint, endPoint));

            successesCnt.incrementAndGet();
        } catch (Exception ex) {
            LOGGER.error("Error processing Radon transform", ex);
        } finally {
            decreasePendingTasksToFinish();
            semaphore.release(1);
        }
    }

    protected boolean processInternal(RadonIntegrationContext integrationContext,
                                      Point2d startPoint,
                                      Point2d endPoint) {
        try {
            if (!Objects.equals(startPoint, endPoint)) {
//            if (!isRedundant(startPoint, endPoint) && !Objects.equals(startPoint, endPoint)) {
                integrationContext.reset();

                getLinePixelsBrowser()
                        .browseLine(startPoint.getIntX(), startPoint.getIntY(),
                                endPoint.getIntX(), endPoint.getIntY(),
                                integrationContext::processPixel);

                Point2d rhoTheta = calculateRhoTheta(startPoint, endPoint);

                result.set(rhoTheta.x, rhoTheta.y, integrationContext.getAccumulation(), integrationContext.getCount());
            }
            return !hasToStopImmediately();
        } catch (Exception ex) {
            throw new RuntimeException(String.format("Error processing line %s - %s", startPoint, endPoint), ex);
        }
    }

    protected int getWidth() {
        return getMyNormalizedImage().getWidth();
    }

    protected int getHeight() {
        return getMyNormalizedImage().getHeight();
    }

    protected Point2d calculateRhoTheta(Point2d startPoint, Point2d endPoint) {
        Point2d center = getImageCenter();
        Point2d lineClosestPoint = calculateLineClosestPoint(startPoint, endPoint, center);

        Point2d delta = lineClosestPoint.subtract(center);
        double rho = delta.norm();
        double theta = 0.0d;

        if (isCloseToZero(delta.x) && isCloseToZero(delta.y)) {
            theta = calculateTheta(endPoint.subtract(startPoint));
        } else {
            theta = calculateTheta(delta);
        }
        if (delta.y < 0) {
            rho = -rho;
        }


        return createPoint2d(rho, theta);
    }

    protected boolean isCloseToZero(double value) {
        return abs(value) <= 1e-4;
    }

    protected DoubleFunctions getDoubleFunctions() {
        return DoubleFunctions.instance();
    }

    protected double abs(double dd) {
        return getDoubleFunctions().abs(dd);
    }

    protected double calculateTheta(Point2d delta) {
        double result = PI_HALVES;
        if (!isCloseToZero(delta.x)) {
            result = Math.atan2(delta.y, delta.x);
        }

        return result;
    }

    protected GeometryFunctions getGeometryFunctions() {
        return GeometryFunctions.instance();
    }

    protected Point2d calculateLineClosestPoint(Point2d lineStartPoint, Point2d lineEndPoint, Point2d outerPoint) {
        return getGeometryFunctions().calculateLineClosestPoint(lineStartPoint, lineEndPoint, outerPoint);
    }

    protected LinePixelsBrowser getLinePixelsBrowser() {
        return LinePixelsBrowser.instance();
    }

    protected BoundingEdgePixelBrowser createBoundingEdgePixelBrowser(My2dContainer myNormalizedImage) {
        return new BoundingEdgePixelBrowser(myNormalizedImage.getWidth(), myNormalizedImage.getHeight());
    }


    public My2dContainer getMyNormalizedImage() {
        return myNormalizedImage;
    }

    public RadonTransformResult getResult() {
        return result;
    }

    protected RadonIntegrationContext createMyPixelRadonContext(My2dContainer myNormalizedImage) {
        return new RadonIntegrationContext(myNormalizedImage);
    }

    protected BufferedImageToMyNormalizedImageConverter getBufferedImageToMyNormalizedImage() {
        return BufferedImageToMyNormalizedImageConverter.instance();
    }

    protected My2dContainer convert(BufferedImage image) {
        return getBufferedImageToMyNormalizedImage().convert(image);
    }

    protected BufferedImage convert(My2dContainer myImage) {
        return getBufferedImageToMyNormalizedImage().convert(myImage);
    }

    protected RadonTransformResult createEmptyRadonTransformResult(BufferedImage image) {
        return new RadonTransformResult(image.getWidth(), image.getHeight())
                .init();
    }

    protected Point2d createPoint2d(double xx, double yy) {
        return Point2d.create(xx, yy);
    }

    protected ImageFunctions getImageFunctions() {
        return ImageFunctions.instance();
    }

    public void paintStringCentered(Graphics gc, Font font, String str, Color textColor, Point centralPoint) {
        getImageFunctions().paintStringCentered(gc, font, str, textColor, centralPoint);
    }

    protected boolean isCancelled() {
        return future.isCancelled();
    }

    protected boolean futureIsDone() {
        return future.isDone();
    }

    protected boolean hasToStopImmediately() {
        return wasTimeout() || futureIsDone();
    }

    public void cancel() {
        future.cancel();
    }

    public boolean isRunning() {
        return getPendingTasksToFinish() > 0;
    }

    public int getPendingTasksToFinish() {
        return pendingTasksToFinishRef.get();
    }

    protected void increasePendingTasksToFinish() {
        pendingTasksToFinishRef.incrementAndGet();
    }

    protected void decreasePendingTasksToFinish() {
        pendingTasksToFinishRef.decrementAndGet();
    }

    public boolean wasSuccessful() {
        return wasSuccessful;
    }

    public boolean wasTimeout() {
        return wasTimeout;
    }

    public long getProcessingTimeMs() {
        return this.endTime - this.startTime;
    }
}
