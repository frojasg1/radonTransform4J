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

package com.frojasg1.gen;

public class IntegerFunctions {

	public static int max( int i1, int i2 )
	{
		return( i1>i2 ? i1 : i2 );
	}

	public static int min( int i1, int i2 )
	{
		return( i1<i2 ? i1 : i2 );
	}

	public static int limit( int value, int lowerBound, int upperBound )
	{
		return( max( lowerBound, min( upperBound, value ) ) );
	}

	public static int abs( int ii )
	{
		return( ii>=0 ? ii : -ii );
	}

	public static int sgn( int ii )
	{
		return( ii>0 ? 1 : ( ii<0 ? -1 : 0 ) );
	}

	public static long max( long i1, long i2 )
	{
		return( i1>i2 ? i1 : i2 );
	}

	public static long min( long i1, long i2 )
	{
		return( i1<i2 ? i1 : i2 );
	}

	public static long limit( long value, long lowerBound, long upperBound )
	{
		return( max( lowerBound, min( upperBound, value ) ) );
	}

	public static long abs( long ii )
	{
		return( ii>=0 ? ii : -ii );
	}

	public static long sgn( long ii )
	{
		return( ii>0 ? 1 : ( ii<0 ? -1 : 0 ) );
	}
}
