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

package com.frojasg1.image.browsers;

import com.frojasg1.image.Point2d;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class BoundingEdgePixelBrowser {
    protected int width;
    protected int height;

    public BoundingEdgePixelBrowser(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public boolean browse(BoundingEdge boundingEdge, Predicate<Point2d> visitor) {
        boolean result = false;
        if (visitor != null && boundingEdge != null) {
            result = true;
            if (boundingEdge == BoundingEdge.NORTH) {
                int yy = 1;
                int to = this.width - 1;
                for (int xx = 1; result && xx < to; xx++) {
                    result = visitor.test(createPoint(xx, yy));
                }
            } else if (boundingEdge == BoundingEdge.SOUTH) {
                int yy = this.height - 2;
                int to = this.width - 1;
                for (int xx = 1; result && xx < to; xx++) {
                    result = visitor.test(createPoint(xx, yy));
                }
            } else if (boundingEdge == BoundingEdge.EAST) {
                int xx = this.width - 2;
                int to = this.height - 1;
                for (int yy = 1; result && yy < to; yy++) {
                    result = visitor.test(createPoint(xx, yy));
                }
            } else if (boundingEdge == BoundingEdge.WEST) {
                int xx = 1;
                int to = this.height - 1;
                for (int yy = 1; result && yy < to; yy++) {
                    result = visitor.test(createPoint(xx, yy));
                }
            }
        }
        return result;
    }

    protected Point2d createPoint(int xx, int yy) {
        return Point2d.create(xx, yy);
    }
}
