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

public class DoubleFunctions {
	
	protected static DoubleFunctions INSTANCE = new DoubleFunctions();

	public static DoubleFunctions instance()
	{
		return INSTANCE;
	}

	public Double min( Double d1, Double d2 )
	{
		Double result = d1;
		if( ( d1 == null ) || ( d2 != null ) && ( d2 < d1 ) )
			result = d2;

		return( result );
	}

	public double aritmeticMean( double ... values )
	{
		double result = 0d;
		if( ( values != null ) && ( values.length > 0 ) )
		{
			for( double value: values )
				result += value;
			
			result /= values.length;
		}

		return( result );
	}

	public double relativeDifference( double d1, double d2 )
	{
		double greater = d1;
		double smaller = d2;
		if( Math.abs(d2) > Math.abs(d1) )
		{
			greater = d2;
			smaller = d1;
		}
			
		double result = 0;
		if( isInside( d2, -1e-6, 1e-6 ) )
			result = Math.abs(greater - smaller) / greater;

		return( result );
	}

	public Double max( Double d1, Double d2 )
	{
		Double result = d1;
		if( ( d1 == null ) || ( d2 != null ) && ( d2 > d1 ) )
			result = d2;

		return( result );
	}

	public int round( double value )
	{
		return( (int) Math.round(value) );
	}

	public int minInt( double d1, double d2 )
	{
		return( round( min(d1, d2) ) );
	}

	public int maxInt( double d1, double d2 )
	{
		return( round( max(d1, d2) ) );
	}

	public int sgn( double dd )
	{
		return( (dd==0) ? 0 : ( (dd>0) ? 1 : -1 ) );
	}

	public double abs( double dd )
	{
		return( (dd==0) ? 0 : ( (dd>0) ? dd : -dd ) );
	}

	public double limit( double value, double lowerBound, double upperBound )
	{
		return( max( lowerBound, min( upperBound, value ) ) );
	}

	public boolean isInside( double value, double lowerBound, double upperBound )
	{
		return( ( value >= lowerBound ) && ( value <= upperBound ) );
	}

	public boolean isStrictlyInside( double value, double lowerBound, double upperBound )
	{
		return( ( value > lowerBound ) && ( value < upperBound ) );
	}

	public boolean isInt( double value )
	{
		return( ( value - Math.round(value) ) == 0 );
	}

	protected double calculateRelativeErrorInternal(double big, double small)
	{
		double result = abs(big - small);
		double absBig = abs(big);
		if( absBig > 1 )
			result = result / absBig;
		
		return( result );
	}

	public double calculateRelativeError(double value1, double value2)
	{
		double result = 0;
		if( abs(value1) > abs(value2) )
			result = calculateRelativeErrorInternal( value1, value2 );
		else
			result = calculateRelativeErrorInternal( value2, value1 );

		return( result );	
	}

	public boolean areClose(double value1, double value2, double tolerance) {
		if(tolerance < 0d) {
			throw new IllegalArgumentException("tolerance must be positive");
		}

		double diff = abs(value1 - value2);
		int sgn = sgn(diff);
		boolean result = (sgn == 0);
		if(!result) {
			if(sgn == 1) {
				result = (diff < tolerance);
			} else if(sgn == -1) {
				result = (-diff < tolerance);
			} else {
				throw new RuntimeException(String.format("sgn should have been one of the following: (-1, 0, 1) but is %d", sgn));
			}
		}
		return result;
	}

	public double getSquareDistance(double[] arr1, double[] arr2) {
		double result = 0;
		double diff;
		for(int ii=0; ii<arr1.length; ii++) {
			diff = arr1[ii] - arr2[ii];
			result += diff * diff;
		}
		return result;
	}

	public int toInt(double value) {
		return (int) value;
	}


}
