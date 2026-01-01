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

package com.frojasg1.image.gen.geometry;

import com.frojasg1.gen.DoubleFunctions;
import com.frojasg1.image.Point2d;
import com.frojasg1.image.gen.PermutationBrowser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class GeometryFunctions {

    protected static GeometryFunctions INSTANCE = new GeometryFunctions();


    public static GeometryFunctions instance() {
        return INSTANCE;
    }

    public Point2d calculateLineClosestPoint(Point2d lineStartPoint, Point2d lineEndPoint, Point2d outerPoint) {
        Point2d lineDirection = lineEndPoint.subtract(lineStartPoint).normalizeVector();
        Point2d startToOuterPoint = outerPoint.subtract(lineStartPoint);

        double dot = lineDirection.dot(startToOuterPoint);

        return lineStartPoint.add(lineDirection.normalizeVector().multiplyByScalar(dot));
    }

    public Point2d calculateIntersection(Point2d center, Point2d rhoTheta1, Point2d rhoTheta2, int width, int height) {
        Point2d result = calculateIntersection(rhoTheta1, rhoTheta2);

        if (result != null) {
            result = center.add(result);

            if (!isInside(result, width, height)) {
                result = null;
            }
        }

        return result;
    }


    // https://es.wikipedia.org/wiki/Transformada_de_Radon
    public Point2d calculateIntersection(Point2d rhoTheta1, Point2d rhoTheta2) {
        // line equation for rhoTheta:
        //  x * cos(theta) + y * sin(theta) - rho = 0

        // linear system to be solved:
        //
        //  /                            \   /      \
        //  |  cos(theta1)   sin(theta1) |   | rho1 |
        //  |  cos(theta2)   sin(theta2) | = | rho2 |
        //  \                           /    \     /

        double theta1Cos = Math.cos(rhoTheta1.y);
        double theta1Sin = Math.sin(rhoTheta1.y);
        double theta2Cos = Math.cos(rhoTheta2.y);
        double theta2Sin = Math.sin(rhoTheta2.y);

        double rho1 = rhoTheta1.x;
        double rho2 = rhoTheta2.x;

        Point2d result = null;

        // Kramer
        double det = theta1Cos * theta2Sin - theta1Sin * theta2Cos;

        if (Math.abs(det) > 1e-4) {
            double xx = (rho1 * theta2Sin - rho2 * theta1Sin) / det;
            double yy = (theta1Cos * rho2 - theta2Cos * rho1) / det;

            result = createPoint2d(xx, yy);
        }
        return result;
    }

    protected Point2d createPoint2d(double xx, double yy) {
        return Point2d.create(xx, yy);
    }

    protected boolean isInside(Point2d point, int width, int height) {
        return (point.getIntX() >= 0 && point.getIntX() < width) &&
                (point.getIntY() >= 0 && point.getIntY() < height);
    }

    public List<Point2d> calculateIntersections(Point2d center, List<Point2d> rhoThetaLines, int width, int height) {
        List<Point2d> result = new ArrayList<>();

        Point2d intersection = null;
        Iterator<Point2d> it = null;
        Point2d rhoTheta2 = null;
        int index = 1;
        for (Point2d rhoTheta1: rhoThetaLines) {
            if (index < rhoThetaLines.size()) {
                it = rhoThetaLines.listIterator(index++);
                while (it.hasNext()) {
                    rhoTheta2 = it.next();
                    intersection = calculateIntersection(center, rhoTheta1, rhoTheta2, width, height);

                    if (intersection != null) {
                        result.add(intersection);
                    }
                }
            }
        }
        return result;
    }

    public double vectorProduct(Point2d pt1, Point2d pt2, Point2d pt3) {
        Point2d delta1 = pt2.subtract(pt1);
        Point2d delta2 = pt3.subtract(pt1);

        return delta1.x * delta2.y - delta2.x * delta1.y;
    }

    // O(n!) !!  for 4 vertex it is ok
    public List<Point2d> sortVertexForConvexPolygon(List<Point2d> vertex) {
        List<Point2d> result = Collections.emptyList();
        if (vertex != null && !vertex.isEmpty()) {

            AtomicReference<List<Point2d>> resultRef = new AtomicReference<>();
            PermutationBrowser<Point2d> permutationBrowser = new PermutationBrowser<>(vertex);

            permutationBrowser.browsePermutations(vertexPermutationList -> considerPermutation(vertexPermutationList, resultRef));

            if (resultRef.get() != null) {
                result = resultRef.get();
            }
        }

        return result;
    }

    protected boolean considerPermutation(List<Point2d> vertexPermutationList, AtomicReference<List<Point2d>> resultRef) {
        boolean result = isConvexPolygon(vertexPermutationList);
        if (result) {
            resultRef.set(vertexPermutationList);
        }
        return result;
    }

    protected boolean isConvexPolygon(List<Point2d> vertex) {
        boolean result = false;
        if (vertex != null && vertex.size() > 2) {
            Iterator<Point2d> it = vertex.iterator();
            Point2d v1 = it.next();
            Point2d v2 = it.next();
            Point2d prevPrevVert = v1;
            Point2d prevVert = v2;
            Point2d currVert = it.next();
            int sgn = sgn(vectorProduct(prevPrevVert, prevVert, currVert));
            result = sgn != 0;
            while (result && it.hasNext()) {
                prevPrevVert = prevVert;
                prevVert = currVert;
                currVert = it.next();

                if (sgn != sgn(vectorProduct(prevPrevVert, prevVert, currVert))) {
                    result = false;
                }
            }
            result = result &&
                    sgn == sgn(vectorProduct(prevVert, currVert, v1)) &&
                    sgn == sgn(vectorProduct(currVert, v1, v2));
        }

        return result;
    }

    protected DoubleFunctions getDoubleFunctions() {
        return DoubleFunctions.instance();
    }

    protected int sgn(double dd) {
        return getDoubleFunctions().sgn(dd);
    }

    public Point2d getNearestPoint(Point2d center, Point2d rhoTheta) {
        double rho = rhoTheta.x;
        double theta = rhoTheta.y;

        double xx = center.x + rho * Math.cos(theta);
        double yy = center.y + rho * Math.sin(theta);

        return createPoint2d(xx, yy);
    }

    public Point2d getLineNormalizedDirection(Point2d rhoTheta) {
        double theta = rhoTheta.y;

        return createPoint2d(Math.sin(theta), -Math.cos(theta));
    }
}
