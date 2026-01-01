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

import java.util.Comparator;
import java.util.function.Function;

/**
 *
 * @author Francisco Javier Rojas Garrido <frojasg1@hotmail.com>
 */
public class ComparatorFunctions
{
	protected static ComparatorFunctions INSTANCE = new ComparatorFunctions();

	public static ComparatorFunctions instance()
	{
		return( INSTANCE );
	}

	public <CC> Comparator<CC> notNullFirstComparator( Comparator<CC> comparator )
	{
		boolean notNullFirst = true;
		return( dealWithNullComparator( comparator, notNullFirst ) );
	}

	public <CC> Comparator<CC> dealWithNullComparator( Comparator<CC> comparator,
													   boolean notNullFirst )
	{
		return( (c1, c2) -> compare(c1, c2, comparator, notNullFirst));
	}

	public <CC> int compare(CC c1, CC c2, Comparator<CC> comparator, boolean notNullFirst)
	{
		int result = 0;
		int resultForFirstElemNull = (notNullFirst) ? 1 : -1;
		if( c1 != c2 ) {
			if( c1 == null ) {
				result = resultForFirstElemNull;
			} else if( c2 == null ) {
				result = -resultForFirstElemNull;
			}
			else
				result = comparator.compare(c1, c2);
		}
		return( result );
	}

	public <CC extends Comparable<CC>> int compareNotNull(CC cc1, CC cc2) {
		return cc1.compareTo(cc2);
	}

	public <CC, AA> int delegateComparison(CC c1, CC c2,
										   Function<CC, AA> getter, Comparator<AA> attribComparator,
										   boolean notNullFirst) {
		return compare(c1, c2,
				(cc1, cc2) -> notNullDelegateComparison(cc1, cc2, getter, attribComparator, notNullFirst),
				notNullFirst);
	}

	public <CC, AA extends Comparable<AA>> int delegateComparison(CC c1, CC c2,
																  Function<CC, AA> getter, boolean notNullFirst) {
		return compare(c1, c2,
				(cc1, cc2) -> notNullDelegateComparison(cc1, cc2, getter, this::compareNotNull, notNullFirst),
				notNullFirst);
	}

	public <CC, AA extends Comparable<AA>> int compareAttrib(CC c1, CC c2,
															 Function<CC, AA> getter1,
															 boolean notNullFirst) {
		return compare(c1, c2,
				(e1, e2) -> notNullDelegateComparison(c1, c2, getter1, this::compareNotNull, notNullFirst),
				notNullFirst);
	}

	public <CC> int compareAttribs(CC c1, CC c2,
								   boolean notNullFirst,
								   Function<CC, ? extends Comparable> ... getters) {
		int result = 0;
		for (Function<CC, ? extends Comparable> getter: getters) {
			result = compareAttrib(c1, c2, getter, notNullFirst);
			if (result != 0) {
				break;
			}
		}
		return result;
	}

	public <CC, AA, BB extends Comparable<BB>> int delegateComparison(CC c1, CC c2,
																	  Function<CC, AA> getter,
																	  Function<AA, BB> getter2,
																	  boolean notNullFirst) {
		return compare(c1, c2,
				(cc1, cc2) -> notNullDelegateComparison(cc1, cc2, getter,
						(a1, a2) -> delegateComparison(a1, a2, getter2, notNullFirst),
						notNullFirst),
				notNullFirst);
	}

	public <CC, AA> int notNullDelegateComparison(CC c1, CC c2,
												  Function<CC, AA> getter, Comparator<AA> attribComparator,
												  boolean notNullFirst) {
		AA att1 = getter.apply(c1);
		AA att2 = getter.apply(c2);
		return compare(att1, att2, attribComparator, notNullFirst);
	}
}
