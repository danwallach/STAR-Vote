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

import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.BooleanElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.BooleanSet;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductSet;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import ch.bfh.unicrypt.math.function.abstracts.AbstractFunction;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;

/**
 * This class represents the concept of a function, which tests the given input elements for equality. For this to work,
 * its domain is a power group and its co-domain the Boolean group. If all all input elements are equal, the function
 * outputs {@link BooleanGroup#TRUE}, and {@link BooleanGroup#FALSE} otherwise.
 * <p/>
 * @author R. Haenni
 * @author R. E. Koenig
 * @version 2.0
 */
public class EqualityFunction
	   extends AbstractFunction<EqualityFunction, ProductSet, Tuple, BooleanSet, BooleanElement> {

	private EqualityFunction(final ProductSet domain, final BooleanSet coDomain) {
		super(domain, coDomain);
	}

	@Override
	protected BooleanElement abstractApply(final Tuple element, final RandomByteSequence randomByteSequence) {
		int arity = element.getArity();
		if (arity > 1) {
			final Element firstElement = element.getFirst();
			for (Element currentElement : element) {
				if (!firstElement.isEquivalent(currentElement)) {
					return BooleanSet.FALSE;
				}
			}
		}
		return BooleanSet.TRUE;
	}

	/**
	 * This is a special factory method for this class for the particular case of two input elements.
	 * <p/>
	 * @param set The group on which this function operates
	 * @return The resulting equality function
	 * @throws IllegalArgumentException if {@literal group} is null
	 */
	public static EqualityFunction getInstance(final Set set) {
		return EqualityFunction.getInstance(set, 2);
	}

	/**
	 * This is the general factory method of this class. The first parameter is the group on which it operates, and the
	 * second parameter is the number of input elements to compare.
	 * <p/>
	 * @param set   The group on which this function operates
	 * @param arity The number of input elements to compare
	 * @return The resulting equality function
	 * @throws IllegalArgumentException if {@literal group} is null
	 * @throws IllegalArgumentException if {@literal arity} is negative
	 */
	public static EqualityFunction getInstance(final Set set, final int arity) {
		return new EqualityFunction(ProductSet.getInstance(set, arity), BooleanSet.getInstance());
	}

}
