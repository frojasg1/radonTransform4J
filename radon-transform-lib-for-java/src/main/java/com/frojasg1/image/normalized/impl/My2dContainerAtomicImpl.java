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


import com.frojasg1.gen.DoubleFunctions;
import com.frojasg1.gen.fun.TriConsumer;
import com.frojasg1.image.Point2d;
import com.frojasg1.image.normalized.My2dContainerAbstract;
import java.util.concurrent.atomic.AtomicReference;

public class My2dContainerAtomicImpl extends My2dContainerAbstract {
    protected AtomicReference[][] container;

    public My2dContainerAtomicImpl(int width, int height) {
        super(width, height);
    }

    public My2dContainerAtomicImpl(int width, int height, double initialValue) {
        super(width, height, initialValue);
    }

    public My2dContainerAtomicImpl init() {
        container = createContainer();

        if (initialValue != null) {
            initWithValue(this.initialValue);
        }

        return this;
    }

    public AtomicReference[][] getContainer() {
        return container;
    }

    protected AtomicReference reset(int xx, int yy) {
        return reset(container, xx, yy);
    }

    protected AtomicReference[][] createContainer() {
        AtomicReference[][] result = new AtomicReference[height][width];
        browseElem(result, (xx, yy, val) -> reset(result, xx, yy));

        return result;
    }

    // Not thread safe
    protected AtomicReference reset(AtomicReference[][] container, int xx, int yy) {
        AtomicReference elem = container[yy][xx];
        if (elem == null) {
            elem = createElem(xx, yy);
            container[yy][xx] = elem;
        }
        return elem;
    }

    protected AtomicReference createElem(int xx, int yy) {
        return new AtomicReference<>(initialValue);
    }

    protected void set(AtomicReference[][] container, int xx, int yy, Double value) {
        set(getElem(container, xx, yy), value);
    }

    protected Double getValue(AtomicReference<Double> elem) {
        return elem.get();
    }

    protected Double set(AtomicReference<Double> elem, Double value) {
        return setInternal(elem, value);
    }

    protected Double reset(AtomicReference<Double> elem, Double value) {
        reset(elem);
        return setInternal(elem, value);
    }

    protected DoubleFunctions getDoubleFunctions() {
        return DoubleFunctions.instance();
    }

    protected Double max(Double d1, Double d2) {
        return getDoubleFunctions().max(d1, d2);
    }

    protected Double setInternal(AtomicReference<Double> elem, Double value) {
        return elem.accumulateAndGet(value, this::max);
    }

    public void set(Point2d pt, Double value) {
        set(pt.getIntX(), pt.getIntY(), value);
    }

    @Override
    public Double getValue(Point2d pt) {
        return super.getValue(pt);
    }

    @Override
    public void set(int xx, int yy, Double value) {
        set(container, xx, yy, value);
    }

    @Override
    public void reset() {
        super.reset();
    }

    protected void reset(AtomicReference<Double> elem) {
        elem.set(getInitialValue());
    }

    @Override
    public void initWithValue(double initValue) {
        browseElem((xx, yy, elem) -> reset(elem, getInitialValue()));
    }

    public void browseElem(TriConsumer<Integer, Integer, AtomicReference<Double>> visitor) {
        browseElem(container, visitor);
    }

    @Override
    public void browseValue(TriConsumer<Integer, Integer, Double> visitor) {
        browseValue(container, visitor);
    }

    protected void browseElem(AtomicReference[][] container,
                              TriConsumer<Integer, Integer, AtomicReference<Double>> visitor) {
        if (visitor != null) {
            for (int yy = 0; yy < this.height; yy++) {
                AtomicReference[] line = container[yy];
                for (int xx = 0; xx < this.width; xx++) {
                    visitor.accept(xx, yy, line[xx]);
                }
            }
        }
    }

    protected void browseValue(AtomicReference[][] container, TriConsumer<Integer, Integer, Double> visitor) {
        browseElem(container, (xx, yy, elem) -> browseValue(xx, yy , elem, visitor));
    }

    protected void browseValue(int xx, int yy, AtomicReference<Double> elem, TriConsumer<Integer, Integer, Double> visitor) {
        visitor.accept(xx, yy, getValue(elem));
    }

    public AtomicReference<Double> getElem(int xx, int yy) {
        return getElem(container, xx, yy);
    }

    public AtomicReference<Double> getElem(AtomicReference[][] container, int xx, int yy) {
        return container[yy][xx];
    }

    @Override
    public Double getValue(int xx, int yy) {
        return getValue(getElem(xx, yy));
    }

    @Override
    public String toString() {
        String elemClassName = AtomicReference.class.getSimpleName();
        return getClass().getSimpleName() + "{" +
                String.format("container=%s[%d][%d] --> %s[%d]",
                        elemClassName, getWidth(), getHeight(), elemClassName, getWidth()*getHeight()) +
                '}';
    }
}
