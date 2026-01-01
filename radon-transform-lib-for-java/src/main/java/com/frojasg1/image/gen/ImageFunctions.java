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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

public class ImageFunctions {
    public static final double RED_COEFICIENT_FOR_GRAY_SCALE = 0.2126d;
    public static final double GREEN_COEFICIENT_FOR_GRAY_SCALE = 0.7152d;
    public static final double BLUE_COEFICIENT_FOR_GRAY_SCALE = 0.0722d;

    protected static ImageFunctions INSTANCE = new ImageFunctions();

    public static ImageFunctions instance() {
        return INSTANCE;
    }


    public int getGrayScale(int rgb) {
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = (rgb) & 0xff;

        //from https://en.wikipedia.org/wiki/Grayscale, calculating luminance
//        short gray = (short)(0.2126 * r + 0.7152 * g + 0.0722 * b);

        short gray = (short) (RED_COEFICIENT_FOR_GRAY_SCALE * r +
                GREEN_COEFICIENT_FOR_GRAY_SCALE * g +
                BLUE_COEFICIENT_FOR_GRAY_SCALE * b);

        return gray;
    }

    public void paintStringCentered(Graphics gc, Font font, String str, Color textColor, Rectangle bounds, Color backgroundColor )
    {
        if( backgroundColor != null )
        {
            gc.setColor( backgroundColor );
            gc.fillRect( (int) bounds.getX(), (int) bounds.getY(), (int) bounds.getWidth(), (int) bounds.getHeight() );
        }

        Point centralPoint = new Point( (int) bounds.getCenterX(), (int) bounds.getCenterY() );

        paintStringCentered( gc, font, str, textColor, centralPoint );
    }

    public void paintStringCentered( Graphics gc, Font font, String str, Color textColor,
                                     Point centralPoint )
    {
        if( textColor != null ) gc.setColor( textColor );

        if( str != null )
        {
            FontRenderContext frc = ((Graphics2D)gc).getFontRenderContext();
            Rectangle2D wrappedBounds = font.getStringBounds(str, frc);
//			int x1 = (int) Math.floor( centralPoint.getX() - wrappedBounds.getWidth() / 2 );
//			int y1 = (int) Math.floor( centralPoint.getY() + wrappedBounds.getHeight() / 2 ) - 3;
            int x1 = (int) Math.floor( centralPoint.getX() - ( wrappedBounds.getWidth() /*+ font.getSize() / 2*/ ) / 2 );
            int y1 = (int) Math.floor( centralPoint.getY() + ( wrappedBounds.getHeight() - font.getSize() ) / 2 );

            paintStringLeftTopJustified( gc, font, str, textColor, x1, y1 );
        }
    }

    public void paintStringLeftTopJustified( Graphics gc, Font font, String str, Color textColor,
                                             int xx, int yy )
    {
        if( textColor != null ) gc.setColor( textColor );

        if( str != null )
        {
            gc.setFont( font );
            gc.drawString( str, xx, yy);
        }
    }
}
