/*
 * UniCrypt
 *
 *  UniCrypt(tm) : Cryptographical framework allowing the implementation of cryptographic protocols e.g. e-voting
 *  Copyright (C) 2014 Bern University of Applied Sciences (BFH), Research Institute for
 *  Security in the Information Society (RISIS), E-Voting Group (EVG)
 *  Quellgasse 21, CH-2501 Biel, Switzerland
 *
 *  Licensed under Dual License consisting of:
 *  1. GNU Affero General Public License (AGPL) v3
 *  and
 *  2. Commercial license
 *
 *
 *  1. This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *  2. Licensees holding valid commercial licenses for UniCrypt may use this file in
 *   accordance with the commercial license agreement provided with the
 *   Software or, alternatively, in accordance with the terms contained in
 *   a written agreement between you and Bern University of Applied Sciences (BFH), Research Institute for
 *   Security in the Information Society (RISIS), E-Voting Group (EVG)
 *   Quellgasse 21, CH-2501 Biel, Switzerland.
 *
 *
 *   For further information contact <e-mail: unicrypt@bfh.ch>
 *
 *
 * Redistributions of files must retain the above copyright notice.
 */
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes;

import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductSet;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import ch.bfh.unicrypt.math.function.abstracts.AbstractFunction;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
import java.util.Arrays;

/**
 * This class represents the concept of a generalized identity function f:X->X^n with f(x)=(x,...,x) for all elements x
 * in X. This class represents the concept of an identity function f:X->X with f(x)=x for all elements x in X.
 * <p/>
 * @author R. Haenni
 * @author R. E. Koenig
 * @version 1.0
 */
public class MultiIdentityFunction
	   extends AbstractFunction<MultiIdentityFunction, Set, Element, ProductSet, Tuple> {

	private MultiIdentityFunction(final Set domain, final ProductSet coDomain) {
		super(domain, coDomain);
	}

	//
	// The following protected method implements the abstract method from {@code AbstractFunction}
	//
	@Override
	protected Tuple abstractApply(final Element element, final RandomByteSequence randomByteSequence) {
		final Element[] elements = new Element[this.getCoDomain().getArity()];
		Arrays.fill(elements, element);
		return this.getCoDomain().getElement(elements);
	}

	//
	// STATIC FACTORY METHODS
	//
	public static MultiIdentityFunction getInstance(final Set set) {
		return MultiIdentityFunction.getInstance(set, 1);
	}

	/**
	 * This is the default constructor for this class. It creates a generalized identity function for a given group,
	 * which reproduces the input value multiple time.
	 * <p/>
	 * @param set   The given set
	 * @param arity The arity of the output element
	 * @return
	 * @throws IllegalArgumentException if {@literal group} is null
	 * @throws IllegalArgumentException if {@literal arity} is negative
	 */
	public static MultiIdentityFunction getInstance(final Set set, final int arity) {
		if (set == null || arity < 0) {
			throw new IllegalArgumentException();
		}
		return new MultiIdentityFunction(set, ProductSet.getInstance(set, arity));
	}

}
