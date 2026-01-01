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

package com.frojasg1.image;


import com.frojasg1.gen.DoubleFunctions;

/**
 *
 * @author Francisco Javier Rojas Garrido <frojasg1@hotmail.com>
 */
// http://karlchenofhell.org/cppswp/lischinski.pdf
public class Point2d implements Comparable<Point2d> {
	public double x;
	public double y;

	public Point2d(double x, double y )
	{
		this.x = x;
		this.y = y;
	}

	public Point2d(Point2d that )
	{
		x = that.x;
		y = that.y;
	}

	public Point2d createCopy()
	{
		return( new Point2d( x, y ) );
	}

	protected boolean isInt( double value )
	{
		return( DoubleFunctions.instance().isInt(value) );
	}

	public boolean isInt() {
		return isInt(x) && isInt(y);
	}

	public Point2d createIntCopyIfNotInt()
	{
		return isInt() ? this : createCopy().toInt();
	}

	public Point2d toInt()
	{
		this.x = getIntX();
		this.y = getIntY();

		return this;
	}

	public double distance( Point2d pt )
	{
		return Math.sqrt(squaredDistance(pt));
	}

	public double squaredDistance( Point2d pt )
	{
		double dx = (x - pt.x);
		double dy = (y - pt.y);

		return( dx * dx + dy * dy );
	}

	public double manhattanDistance(Point2d pt) {
		return abs(pt.x - this.x) + abs(pt.y - this.y);
	}

	public Point2d subtract( Point2d pt )
	{
		return( create( x-pt.x, y-pt.y ) );
	}
	public Point2d add( Point2d pt )
	{
		return( create( x+pt.x, y+pt.y ) );
	}

	public double norm()
	{
		return( Math.sqrt( squaredNorm() ) );
	}

	public double squaredNorm()
	{
		return( x * x + y * y );
	}

	public int getIntX()
	{
		return( (int) x );
	}

	public int getIntY()
	{
		return( (int) y );
	}

	public Point2d applyDeltas(double deltax, double deltay, Point2d result) {
		if(result == null) {
			result = createCopy();
		}
		result.x = this.x + deltax;
		result.y = this.y + deltay;
		return result;
	}

	protected DoubleFunctions getDoubleFunctions() {
		return DoubleFunctions.instance();
	}

	protected double abs(double val) {
		return getDoubleFunctions().abs(val);
	}

	public Point2d normalizeVector() {
		double norm = norm();

		return multiplyByScalar(1.0d / norm);
	}

	public Point2d multiplyByScalar(double scalar) {
		return create(this.x * scalar, this.y * scalar);
	}

	@Override
	public int hashCode() {
		int hash = 7;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Point2d other = (Point2d) obj;
		if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x)) {
			return false;
		}
		if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.y)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Point2d{" + "x=" + x + ", y=" + y + '}';
	}

	protected int sgn( double value )
	{
		int result = 0;
		if( value > 0 )
			result = 1;
		else if( value < 0 )
			result = -1;

		return( result );
	}

	public double dot(Point2d pt) {
		return this.x * pt.x + this.y * pt.y;
	}

	@Override
	public int compareTo(Point2d o) {
		int result = sgn( x - o.x );
		if( result == 0 )
			result = sgn( y - o.y );

		return( result );
	}

	public static Point2d create(double xx, double yy) {
		return new Point2d(xx, yy);
	}
}
