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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.abstracts;

import ch.bfh.unicrypt.helper.iterable.IterableArray;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.additive.abstracts.AbstractAdditiveMonoid;
import ch.bfh.unicrypt.math.algebra.dualistic.interfaces.DualisticElement;
import ch.bfh.unicrypt.math.algebra.dualistic.interfaces.SemiRing;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import java.math.BigInteger;

/**
 * This abstract class provides a basis implementation for objects of type {@link SemiRing}.
 * <p>
 * @param <E> Generic type of the elements of this semiring
 * @param <V> Generic type of values stored in the elements of this semiring
 * @author rolfhaenni
 */
public abstract class AbstractSemiRing<E extends DualisticElement<V>, V extends Object>
	   extends AbstractAdditiveMonoid<E, V>
	   implements SemiRing<V> {

	private E one;

	public AbstractSemiRing(Class<? extends Object> valueClass) {
		super(valueClass);
	}

	@Override
	public E multiply(Element element1, Element element2) {
		if (!this.contains(element1) || !this.contains(element2)) {
			throw new IllegalArgumentException();
		}
		return this.abstractMultiply((E) element1, (E) element2);
	}

	@Override
	public E multiply(Element... elements) {
		if (elements == null) {
			throw new IllegalArgumentException();
		}
		return this.defaultMultiply(IterableArray.getInstance(elements));
	}

	@Override
	public E multiply(Iterable<Element> elements) {
		if (elements == null) {
			throw new IllegalArgumentException();
		}
		return this.defaultMultiply(elements);
	}

	@Override
	public E power(Element element, BigInteger amount) {
		if (!this.contains(element) || (amount == null)) {
			throw new IllegalArgumentException();
		}
		return this.defaultPower((E) element, amount);
	}

	@Override
	public E power(Element element, Element<BigInteger> amount) {
		if (amount == null) {
			throw new IllegalArgumentException();
		}
		return this.power(element, amount.getValue());
	}

	@Override
	public E power(Element element, int amount) {
		return this.power(element, BigInteger.valueOf(amount));
	}

	@Override
	public E square(Element element) {
		return this.multiply(element, element);
	}

	@Override
	public E productOfPowers(Element[] elements, BigInteger[] amounts) {
		if ((elements == null) || (amounts == null) || (elements.length != amounts.length)) {
			throw new IllegalArgumentException();
		}
		return this.defaultProductOfPowers(elements, amounts);
	}

	@Override
	public E getOneElement() {
		if (this.one == null) {
			this.one = this.abstractGetOne();
		}
		return this.one;
	}

	@Override
	public boolean isOneElement(final Element element) {
		return this.areEquivalent(element, this.getOneElement());
	}

//
// The following protected methods are default implementations for sets.
// They may need to be changed in certain sub-classes.
//
	protected E defaultMultiply(final Iterable<Element> elements) {
		if (!elements.iterator().hasNext()) {
			return this.getOneElement();
		}
		E result = null;
		for (Element element : elements) {
			if (result == null) {
				result = (E) element;
			} else {
				result = this.multiply(result, element);
			}
		}
		return result;
	}

	protected E defaultPower(E element, BigInteger amount) {
		if (amount.signum() < 0) {
			throw new IllegalArgumentException();
		}
		if (amount.signum() == 0) {
			return this.getOneElement();
		}
		return this.defaultPowerAlgorithm(element, amount);
	}

	protected E defaultPowerAlgorithm(E element, BigInteger posAmount) {
		E result = element;
		for (int i = posAmount.bitLength() - 2; i >= 0; i--) {
			result = this.abstractMultiply(result, result);
			if (posAmount.testBit(i)) {
				result = this.abstractMultiply(result, element);
			}
		}
		return result;
	}

	protected E defaultProductOfPowers(final Element[] elements, final BigInteger[] amounts) {
		if (elements.length == 0) {
			return this.getOneElement();
		}
		Element[] results = new Element[elements.length];
		for (int i = 0; i < elements.length; i++) {
			results[i] = this.power(elements[i], amounts[i]);
		}
		return this.multiply(results);
	}

	@Override
	protected BigInteger defaultGetOrderLowerBound() {
		return BigInteger.valueOf(2);
	}

	//
	// The following protected abstract method must be implemented in every
	// direct sub-class.
	//
	protected abstract E abstractMultiply(E element1, E element2);

	protected abstract E abstractGetOne();

}
