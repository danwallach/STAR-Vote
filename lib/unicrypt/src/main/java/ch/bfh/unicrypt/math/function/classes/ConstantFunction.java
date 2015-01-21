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

import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.SingletonElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.SingletonGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Monoid;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import ch.bfh.unicrypt.math.function.abstracts.AbstractFunction;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;

/**
 * This class represents the concept of a constant function with no input. When the function is called, it returns
 * always the same element as output value.
 * <p>
 * @author R. Haenni
 * @author R. E. Koenig
 * @version 2.0
 */
public class ConstantFunction
	   extends AbstractFunction<ConstantFunction, SingletonGroup, SingletonElement, Set, Element> {

	private final Element element;

	/**
	 * This is the general constructor of this class. It creates a function that returns always the same element when
	 * called.
	 * <p>
	 * @param element The constant output value of the function
	 * @throws IllegalArgumentException if {@literal element} is null
	 */
	private ConstantFunction(Set coDomain, Element element) {
		super(SingletonGroup.getInstance(), coDomain);
		this.element = element;
	}

	/**
	 * Returns the constant element returned by this function.
	 * <p>
	 * @return The constant element
	 */
	public Element getElement() {
		return this.element;
	}

	@Override
	protected boolean defaultIsEquivalent(ConstantFunction other) {
		return this.getElement().isEquivalent(other.getElement());
	}

	@Override
	protected Element abstractApply(SingletonElement element, RandomByteSequence randomByteSequence) {
		return this.element;
	}

	/**
	 * This is the general factory method of this class. It creates a function that always returns the same element when
	 * called.
	 * <p>
	 * @param element The given element
	 * @return The constant function
	 * @throws IllegalArgumentException if {@literal group} is null
	 */
	public static ConstantFunction getInstance(final Element element) {
		if (element == null) {
			throw new IllegalArgumentException();
		}
		return new ConstantFunction(element.getSet(), element);
	}

	/**
	 * This is a special factory method of this class, which creates a function that always returns the identity element
	 * of the given group.
	 * <p>
	 * @param monoid The given group
	 * @return The resulting function
	 */
	public static ConstantFunction getInstance(final Monoid monoid) {
		if (monoid == null) {
			throw new IllegalArgumentException();
		}
		return new ConstantFunction(monoid, monoid.getIdentityElement());
	}

}
