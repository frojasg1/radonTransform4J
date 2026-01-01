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

import com.frojasg1.gen.concurrent.BasicFuture;
import com.frojasg1.gen.concurrent.FutureCallback;
import java.awt.image.BufferedImage;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class RadonTransformExecutor {
    protected Consumer<Runnable> executor;

    public RadonTransformExecutor(Consumer<Runnable> executor) {
        this.executor = executor;
    }

    public Future<RadonTransformCalculator> calculateRadonTransform(BufferedImage image) {
        return calculateRadonTransform(image, 100_000_000);
    }

    public Future<RadonTransformCalculator> calculateRadonTransform(BufferedImage image,
                                                                    int internalTimeoutInMs) {
        return calculateRadonTransform(image, internalTimeoutInMs, null);
    }

    public Future<RadonTransformCalculator> calculateRadonTransform(BufferedImage image,
                                                                    int internalTimeoutInMs,
                                                                    FutureCallback<RadonTransformCalculator> callback) {
        BasicFuture<RadonTransformCalculator> result = createFuture(callback);

        execute(image, internalTimeoutInMs, result);

        return result;
    }

    public Consumer<Runnable> getExecutor() {
        return executor;
    }

    protected void execute(BufferedImage image,
                           int internalTimeoutInMs,
                           BasicFuture<RadonTransformCalculator> future) {
        new Thread(() -> executeInternal(image, internalTimeoutInMs, future)).start();
    }

    protected void executeInternal(BufferedImage image,
                                   int internalTimeoutInMs,
                                   BasicFuture<RadonTransformCalculator> future) {
        RadonTransformCalculator radonTransformCalculator = new RadonTransformCalculator(future, getExecutor());

        radonTransformCalculator.process(image, internalTimeoutInMs);
    }

    protected <T> BasicFuture<T> createFuture(FutureCallback<T> callback) {
        return new BasicFuture<>(callback);
    }
}
