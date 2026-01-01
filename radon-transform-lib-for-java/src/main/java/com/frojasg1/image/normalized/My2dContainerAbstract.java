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

package com.frojasg1.image.normalized;


import com.frojasg1.image.Point2d;

public abstract class My2dContainerAbstract implements My2dContainer {

    protected int width;
    protected int height;

    protected Double initialValue;


    public My2dContainerAbstract(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public My2dContainerAbstract(int width, int height, double initialValue) {
        this(width, height);

        this.initialValue = initialValue;
    }

    @Override
    public void reset() {
        initWithValue(getInitialValue());
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    protected Double getInitialValue() {
        return initialValue;
    }

    @Override
    public void set(Point2d pt, Double value) {
        set(pt.getIntX(), pt.getIntY(), value);
    }

    @Override
    public Double getValue(Point2d pt) {
        return getValue(pt.getIntX(), pt.getIntY());
    }

    @Override
    public void checkCompatibility(My2dContainer normalizedImage) {
        if(!isCompatible(normalizedImage)) {
            throw new RuntimeException(
                    String.format("NormalizedImages are not compatible: %s and %s",
                            this, normalizedImage));
        }
    }

    @Override
    public boolean isCompatible(My2dContainer normalizedImage) {
        return (normalizedImage != null) &&
                ( this.getWidth() == normalizedImage.getWidth() ) &&
                ( this.getHeight() == normalizedImage.getHeight() );
    }
}
