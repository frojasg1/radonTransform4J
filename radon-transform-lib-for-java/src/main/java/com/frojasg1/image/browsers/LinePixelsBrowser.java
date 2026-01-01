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

import com.frojasg1.gen.DoubleFunctions;
import com.frojasg1.gen.IntegerFunctions;
import java.util.function.BiConsumer;

public class LinePixelsBrowser {
    protected static LinePixelsBrowser INSTANCE = new LinePixelsBrowser();

    public static LinePixelsBrowser instance() {
        return INSTANCE;
    }

    public void browseLine(int x1, int y1, int x2, int y2, BiConsumer<Integer, Integer> pixelVisitor) {
        if (abs(x1 - x2) >= abs(y1 - y2)) {
            browseLineInternal(x1, y1, x2, y2, pixelVisitor);
        } else {
            browseLineInternal(y1, x1, y2, x2, (longCoord, shortCoord) -> pixelVisitor.accept(shortCoord, longCoord));
        }
    }

    protected void browseLineInternal(int long1, int short1, int long2, int short2, BiConsumer<Integer, Integer> pixelVisitor) {
        int shortStart = short1;
        double longStart = long1;
        int shortEnd = short2;
        double longEnd = long2;

        if (short1 == short2) {
            browseLongCoord(long1, long2, short1, pixelVisitor);
        } else {
            if (short1 > short2) {
                shortStart = short2;
                longStart = long2;
                shortEnd = short1;
                longEnd = long1;
            }

            int unitDelta = sgn(longEnd - longStart);
            double delta = (longEnd - longStart) / (shortEnd - shortStart);
            double deltaHalves = 0.5d * delta;

            longStart += 0.5d;
            longEnd += 0.5d;

            // first step of short coordinate, only one half step
            int longFrom = (int) longStart;
            double longTo = longFrom + deltaHalves;
            browseLongCoord(longFrom, (int) longTo, shortStart, pixelVisitor);

            if (shortEnd - shortStart > 1) {
                longFrom = (int) longTo + unitDelta;
                for (int shortCoord = shortStart + 1;
                     shortCoord <= shortEnd - 1;
                     shortCoord++,longFrom = (int) longTo + unitDelta, longTo += delta
                ) {
                    browseLongCoord(longFrom, (int) longTo, shortCoord, pixelVisitor);
                }
            }

            // last step of short coordinate, only one half step
            if (longEnd != longStart) {
                longFrom = (int) longTo + unitDelta;
            }
            browseLongCoord(longFrom, (int) longEnd, shortEnd, pixelVisitor);
        }
    }

    protected void browseLongCoord(int from, int to, int shortCoord, BiConsumer<Integer, Integer> pixelVisitor) {
        int unitDelta = sgn(to - from);
        if (unitDelta >= 0) {
            for (int longCoord = from; longCoord <= to; longCoord++) {
                pixelVisitor.accept(longCoord, shortCoord);
            }
        } else {
            for (int longCoord = from; longCoord >= to; longCoord--) {
                pixelVisitor.accept(longCoord, shortCoord);
            }
        }
    }

    protected int sgn(double value) {
        return DoubleFunctions.instance().sgn(value);
    }

    protected int abs(int value) {
        return IntegerFunctions.abs(value);
    }

    protected int min(int i1, int i2) {
        return IntegerFunctions.min(i1, i2);
    }
    protected int max(int i1, int i2) {
        return IntegerFunctions.max(i1, i2);
    }
}
