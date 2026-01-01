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

package com.frojasg1.image.helpers;

import com.frojasg1.image.normalized.My2dContainer;
import com.frojasg1.image.gen.ImageFunctions;
import com.frojasg1.image.normalized.impl.MyNormalizedImageImpl;
import java.awt.image.BufferedImage;
import java.util.function.Function;

public class BufferedImageToMyNormalizedImageConverter {

    protected static BufferedImageToMyNormalizedImageConverter INSTANCE = new BufferedImageToMyNormalizedImageConverter();

    public static BufferedImageToMyNormalizedImageConverter instance() {
        return INSTANCE;
    }

    public My2dContainer convert(BufferedImage image) {
        return convert(image, this::normalizedGreyScale);
    }

    public My2dContainer convert(BufferedImage image, Function<Integer, Double> normalizer) {
        My2dContainer result = createMyNormalizedImage(image);
        for (int yy = 0; yy < image.getHeight(); yy++) {
            for (int xx = 0; xx < image.getWidth(); xx++) {
                result.set(xx, yy, normalizer.apply(image.getRGB(xx, yy)));
            }
        }

        return result;
    }

    public BufferedImage convert(My2dContainer myImage) {
        return convert(myImage, this::normalizedLuminanceToGreyScaleRgb);
    }

    public BufferedImage convert(My2dContainer myImage, Function<Double, Integer> denormalizer) {
        BufferedImage result = createBufferedImage(myImage);

        double value = 0.0d;
        for (int yy = 0; yy < result.getHeight(); yy++) {
            for (int xx = 0; xx < result.getWidth(); xx++) {
                value = myImage.getValue(xx, yy);
                if (value >= 0.0d) {
                    result.setRGB(xx, yy, denormalizer.apply(value));
                }
            }
        }

        return result;
    }

    protected My2dContainer createMyNormalizedImage(BufferedImage image) {
        return new MyNormalizedImageImpl(image.getWidth(), image.getHeight())
                .init();
    }

    protected double normalizedGreyScale(int rgb) {
        return getGreyScale(rgb) / 255d;
    }

    protected int getGreyScale(int rgb) {
        return getImageFunctions().getGrayScale(rgb);
    }

    protected ImageFunctions getImageFunctions() {
        return ImageFunctions.instance();
    }

    protected int normalizedLuminanceToGreyScaleRgb(double value) {
        return greyScaleToARgb(normalizedLuminanceToGreyScale(value), 0xFF);
    }

    protected int normalizedLuminanceToGreyScale(double value) {
        return (int) (value * 255d);
    }

    protected int greyScaleToARgb(int greyScale, int alpha) {
        return (greyScale & 0xFF) | ((greyScale & 0xFF) << 8) | ((greyScale & 0xFF) << 16) | (alpha << 24);
    }

    protected BufferedImage createBufferedImage(My2dContainer myImage) {
        return new BufferedImage(myImage.getWidth(), myImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
    }
}
