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

package com.frojasg1.image.normalized.impl;


import com.frojasg1.gen.fun.TriConsumer;
import com.frojasg1.image.normalized.My2dContainerAbstract;

public class MyNormalizedImageImpl extends My2dContainerAbstract {
    protected Double[][] container;
    public MyNormalizedImageImpl(int width, int height) {
        super(width, height);
    }

    public MyNormalizedImageImpl(int width, int height, double initialValue) {
        super(width, height, initialValue);
    }


    public MyNormalizedImageImpl init() {
        container = createContainer();

        if (initialValue != null) {
            initWithValue(this.initialValue);
        }

        return this;
    }

    @Override
    public void initWithValue(double initValue) {
        for (int yy = 0; yy < this.height; yy++) {
            Double[] line = container[yy];
            for (int xx = 0; xx < this.width; xx++) {
                line[xx] = initValue;
            }
        }
    }

    protected Double[][] createContainer() {
        return new Double[height][width];
    }

    public Double[][] getContainer() {
        return container;
    }

    public void browseValue(TriConsumer<Integer, Integer, Double> visitor) {
        browseValue(container, visitor);
    }

    protected void browseValue(Double[][] container, TriConsumer<Integer, Integer, Double> visitor) {
        if (visitor != null) {
            for (int yy = 0; yy < this.height; yy++) {
                Double[] line = container[yy];
                for (int xx = 0; xx < this.width; xx++) {
                    visitor.accept(xx, yy, line[xx]);
                }
            }
        }
    }

    protected void set(Double[][] container, int xx, int yy, Double value) {
        container[yy][xx] = value;
    }

    protected void reset(int xx, int yy, Double initialValue) {
        set(xx, yy, initialValue);
    }

    @Override
    public Double getValue(int xx, int yy) {
        return container[yy][xx];
    }

    @Override
    public void set(int xx, int yy, Double value) {
        set(container, xx, yy, value);
    }

    @Override
    public String toString() {
        String elemClassName = Double.class.getSimpleName();
        return getClass().getSimpleName() + "{" +
                String.format("container=%s[%d][%d] --> %s[%d]",
                        elemClassName, getWidth(), getHeight(), elemClassName, getWidth()*getHeight()) +
                '}';
    }
}
