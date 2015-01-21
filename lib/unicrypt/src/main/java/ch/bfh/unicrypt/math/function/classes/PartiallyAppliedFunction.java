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
import ch.bfh.unicrypt.math.function.interfaces.Function;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;

/**
 * This class represents the concept of a function, which is derived from another function with a product (or power)
 * group domain by applying a single input element and thus by fixing the corresponding parameter to a constant value.
 * Therefore, the input arity of such a function of is the input arity of the parent function minus 1. Functions of that
 * type are usually constructed by calling the method {@link Function#partiallyApply(Element, int)} for a given function
 * with a product (or power) group domain.
 * <p>
 * @author R. Haenni
 * @author R. E. Koenig
 * @version 2.0
 */
public class PartiallyAppliedFunction
	   extends AbstractFunction<PartiallyAppliedFunction, ProductSet, Tuple, Set, Element> {

	private final Function parentFunction;
	private final Element parameter;
	private final int index;

	/**
	 * This is the default constructor of this class. It derives from a given function a new function, in which one
	 * input element is fixed to a given element and thus expects one input element less.
	 * <p>
	 * @param parentFunction The given function
	 * @param parameter      The given parameter to fix
	 * @param index          The index of the parameter to fix
	 * @throws IllegalArgumentException  if the {@literal function} is null or not a ProductGroup
	 * @throws IndexOutOfBoundsException if the {@literal index} is negative or > the arity of the function's domain
	 * @throws IllegalArgumentException  if the {@literal element} is not an element of the corresponding group
	 */
	private PartiallyAppliedFunction(final ProductSet domain, final Set coDomain, final Function parentFunction, final Element parameter, final int index) {
		super(domain, coDomain);
		this.parentFunction = parentFunction;
		this.parameter = parameter;
		this.index = index;
	}

	/**
	 * Returns the parent function from which {@literal this} function has been derived.
	 * <p>
	 * @return The parent function
	 */
	public Function getParentFunction() {
		return this.parentFunction;
	}

	/**
	 * Returns the input element that has been used to derive {@literal this} function from the parent function.
	 * <p>
	 * @return The input element
	 */
	public Element getParameter() {
		return this.parameter;
	}

	/**
	 * Returns the index of the parameter that has been fixed to derive {@literal this} function from the parent
	 * function.
	 * <p>
	 * @return The index of the input element
	 */
	public int getIndex() {
		return this.index;
	}

	@Override
	protected boolean defaultIsEquivalent(PartiallyAppliedFunction other) {
		return this.getParentFunction().isEquivalent(other.getParentFunction())
			   && this.getParameter().isEquivalent(other.getParameter())
			   && this.getIndex() == other.getIndex();
	}

	//
	// The following protected method implements the abstract method from {@code AbstractFunction}
	//
	@Override
	protected Element abstractApply(final Tuple element, final RandomByteSequence randomByteSequence) {
		int arity = element.getArity();
		final Element[] allElements = new Element[arity + 1];
		for (int i = 0; i < arity; i++) {
			if (i < this.getIndex()) {
				allElements[i] = element.getAt(i);
			} else {
				allElements[i + 1] = element.getAt(i);
			}
			allElements[this.getIndex()] = this.getParameter();
		}
		return this.getParentFunction().apply(allElements, randomByteSequence);
	}

	//
	// STATIC FACTORY METHODS
	//
	/**
	 * This is the default constructor of this class. It derives from a given function a new function, in which one
	 * input element is fixed to a given element and thus expects one input element less.
	 * <p>
	 * @param parentFunction
	 * @param element        The given parameter to fix
	 * @param index          The index of the parameter to fix
	 * @return
	 * @throws IllegalArgumentException  if the {@literal function} is null or not a ProductGroup
	 * @throws IndexOutOfBoundsException if the {@literal index} is negative or > the arity of the function's domain
	 * @throws IllegalArgumentException  if the {@literal element} is not an element of the corresponding group
	 */
	public static PartiallyAppliedFunction getInstance(final Function parentFunction, final Element element, final int index) {
		if (parentFunction == null || !parentFunction.getDomain().isProduct()) {
			throw new IllegalArgumentException();
		}
		ProductSet domain = (ProductSet) parentFunction.getDomain();
		if (!domain.getAt(index).contains(element)) {
			throw new IllegalArgumentException();
		}
		return new PartiallyAppliedFunction(domain.removeAt(index), parentFunction.getCoDomain(), parentFunction, element, index);
	}

}
