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

package com.frojasg1.gen.concurrent;
// copied from:
//package org.apache.http.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BasicFuture<T> implements Future<T> {
    private final FutureCallback<T> callback;
    private volatile boolean completed;
    private volatile boolean cancelled;
    private volatile T result;
    private volatile Exception ex;

    public BasicFuture(FutureCallback<T> callback) {
        this.callback = callback;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public boolean isDone() {
        return this.completed;
    }

    private T getResult() throws ExecutionException {
        if (this.ex != null) {
            throw new ExecutionException(this.ex);
        } else {
            return this.result;
        }
    }

    @Override
    public synchronized T get() throws InterruptedException, ExecutionException {
        while(!this.completed) {
            this.wait();
        }

        return this.getResult();
    }

    @Override
    public synchronized T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (unit == null) {
            throw new IllegalArgumentException("Time unit cannot be null");
        }
        long msecs = unit.toMillis(timeout);
        long startTime = msecs <= 0L ? 0L : System.currentTimeMillis();
        long waitTime = msecs;
        if (this.completed) {
            return this.getResult();
        } else if (msecs <= 0L) {
            throw new TimeoutException();
        } else {
            do {
                this.wait(waitTime);
                if (this.completed) {
                    return this.getResult();
                }

                waitTime = msecs - (System.currentTimeMillis() - startTime);
            } while(waitTime > 0L);

            throw new TimeoutException();
        }
    }

    public boolean completed(T result) {
        synchronized(this) {
            if (this.completed) {
                return false;
            }

            this.completed = true;
            this.result = result;
            this.notifyAll();
        }

        if (this.callback != null) {
            this.callback.completed(result);
        }

        return true;
    }

    public boolean failed(Exception exception) {
        synchronized(this) {
            if (this.completed) {
                return false;
            }

            this.completed = true;
            this.ex = exception;
            this.notifyAll();
        }

        if (this.callback != null) {
            this.callback.failed(exception);
        }

        return true;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        synchronized(this) {
            if (this.completed) {
                return false;
            }

            this.completed = true;
            this.cancelled = true;
            this.notifyAll();
        }

        if (this.callback != null) {
            this.callback.cancelled();
        }

        return true;
    }

    public boolean cancel() {
        return this.cancel(true);
    }
}
