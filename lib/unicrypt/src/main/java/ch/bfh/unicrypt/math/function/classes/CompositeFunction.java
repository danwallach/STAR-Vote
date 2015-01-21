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

import ch.bfh.unicrypt.helper.array.classes.DenseArray;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import ch.bfh.unicrypt.math.function.abstracts.AbstractCompoundFunction;
import ch.bfh.unicrypt.math.function.interfaces.Function;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;

/**
 * This class represents the concept of a composite function f:X_1->Y_n. It consists of multiple internal functions
 * f_i:X_i->Y_i, which are applied sequentially to the input element in the given order. For this to work, X_i=Y_{i-1}
 * must hold for i=2,...,n, i.e., the co-domain of the first function must correspond to the domain of the second
 * function, the co-domain of the second function must correspond to the domain of the third function, and so on.
 * <p>
 * @author R. Haenni
 * @author R. E. Koenig
 * @version 2.0
 */
public final class CompositeFunction
	   extends AbstractCompoundFunction<CompositeFunction, Set, Element, Set, Element> {

	private CompositeFunction(Set domain, Set coDomain, final DenseArray<Function> functions) {
		super(domain, coDomain, functions);
	}

	@Override
	protected final Element abstractApply(final Element element, final RandomByteSequence randomByteSequence) {
		Element result = element;
		for (Function function : this) {
			result = function.apply(result, randomByteSequence);
		}
		return result;
	}

	@Override
	protected CompositeFunction abstractGetInstance(DenseArray<Function> functions) {
		return CompositeFunction.getInstance(functions);
	}

	@Override
	protected Class getArrayClass() {
		return CompositeFunction.class;
	}

	/**
	 * This is the general factory method of this class. It takes an array of functions as input and produces the
	 * corresponding composite function.
	 * <p>
	 * @param functions The given array of functions
	 * @return The resulting composite function
	 * @throws IllegalArgumentException if {@literal functions} is null, contains null, or is empty
	 * @throws IllegalArgumentException if the domain of a function is different from the co-domain of the previous
	 *                                  function
	 */
	public static CompositeFunction getInstance(final DenseArray<Function> functions) {
		if (functions == null || functions.getLength() == 0) {
			throw new IllegalArgumentException();
		}
		int arity = functions.getLength();
		if (functions.isUniform()) {
			if (arity > 1 && !functions.getFirst().getDomain().isEquivalent(functions.getFirst().getCoDomain())) {
				throw new IllegalArgumentException();
			}
		} else {
			for (int i = 1; i < arity; i++) {
				if (!(functions.getAt(i - 1).getCoDomain().isEquivalent(functions.getAt(i).getDomain()))) {
					throw new IllegalArgumentException();
				}
			}
		}
		return new CompositeFunction(functions.getFirst().getDomain(), functions.getLast().getCoDomain(), functions);
	}

	public static CompositeFunction getInstance(final Function... functions) {
		return CompositeFunction.getInstance(DenseArray.getInstance(functions));
	}

	public static CompositeFunction getInstance(final Function function, final int arity) {
		return CompositeFunction.getInstance(DenseArray.getInstance(function, arity));
	}

}
