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

package com.frojasg1.image.gen;

import com.frojasg1.gen.fun.TriFunction;
import com.frojasg1.image.normalized.My2dContainerAbstract;
import com.frojasg1.image.normalized.impl.MyNormalizedImageImpl;
import java.util.Arrays;
import java.util.function.BinaryOperator;

public class MyNormalizedImageFunctions {

    protected static MyNormalizedImageFunctions INSTANCE = new MyNormalizedImageFunctions();

    public static MyNormalizedImageFunctions instance() {
        return INSTANCE;
    }


    public void filter(My2dContainerAbstract normalizedResultImage, TriFunction<Integer, Integer, Double, Double> transformer) {
        filter(normalizedResultImage,
                0, normalizedResultImage.getWidth(), 0, normalizedResultImage.getHeight(),
                transformer);
    }

    public void filter(My2dContainerAbstract normalizedResultImage,
                       int xFrom, int xTo, int yFrom, int yTo,
                       TriFunction<Integer, Integer, Double, Double> transformer) {
        for (int yy = yFrom; yy < yTo; yy++) {
            for (int xx = xFrom; xx < xTo; xx++) {
                normalizedResultImage.set(xx, yy, transformer.apply(xx, yy, normalizedResultImage.getValue(xx, yy)));
            }
        }
    }

    public double reduce(MyNormalizedImageImpl normalizedResultImage, double initValue, BinaryOperator<Double> reducer) {
        return Arrays.stream(normalizedResultImage.getContainer())
                .map(lineArr -> Arrays.stream(lineArr).reduce(initValue, reducer))
                .findFirst()
                .orElse(initValue);
    }

    public double reduce(My2dContainerAbstract normalizedResultImage,
                         int xFrom, int xTo, int yFrom, int yTo,
                         double initValue, BinaryOperator<Double> reducer) {
        double result = initValue;
        for (int yy = yFrom; yy < yTo; yy++) {
            for (int xx = xFrom; xx < xTo; xx++) {
                result = reducer.apply(normalizedResultImage.getValue(xx, yy), result);
            }
        }
        return result;
    }
}
