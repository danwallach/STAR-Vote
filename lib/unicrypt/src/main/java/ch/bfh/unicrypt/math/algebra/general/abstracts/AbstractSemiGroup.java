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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.abstracts;

import ch.bfh.unicrypt.helper.iterable.IterableArray;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.unicrypt.math.algebra.general.interfaces.SemiGroup;
import java.math.BigInteger;

/**
 * This abstract class provides a basis implementation for objects of type {@link SemiGroup}.
 * <p>
 * @param <E> Generic type of elements of this semigroup
 * @param <V> Generic type of values stored in the elements of this semigroup
 * @see AbstractElement
 * <p>
 * @author R. Haenni
 * @author R. E. Koenig
 * @version 2.0
 */
public abstract class AbstractSemiGroup<E extends Element<V>, V extends Object>
	   extends AbstractSet<E, V>
	   implements SemiGroup<V> {

	protected AbstractSemiGroup(Class<? extends Object> valueClass) {
		super(valueClass);
	}

	@Override
	public final E apply(final Element element1, final Element element2) {
		if (!this.contains(element1) || !this.contains(element2)) {
			throw new IllegalArgumentException();
		}
		return this.abstractApply((E) element1, (E) element2);
	}

	@Override
	public final E apply(final Element... elements) {
		if (elements == null) {
			throw new IllegalArgumentException();
		}
		return this.defaultApply(IterableArray.getInstance(elements));
	}

	@Override
	public final E apply(final Iterable<Element> elements) {
		if (elements == null) {
			throw new IllegalArgumentException();
		}
		return this.defaultApply(elements);
	}

	@Override
	public final E selfApply(final Element element, final BigInteger amount) {
		if (!this.contains(element) || amount == null) {
			throw new IllegalArgumentException();
		}
		return this.defaultSelfApply((E) element, amount);
	}

	@Override
	public final E selfApply(final Element element, final Element<BigInteger> amount) {
		if (amount == null) {
			throw new IllegalArgumentException();
		}
		return this.selfApply(element, amount.getValue());
	}

	@Override
	public final E selfApply(final Element element, final int amount) {
		return this.selfApply(element, BigInteger.valueOf(amount));
	}

	@Override
	public final E selfApply(final Element element) {
		return this.apply(element, element);
	}

	@Override
	public final E multiSelfApply(final Element[] elements, final BigInteger[] amounts) {
		if ((elements == null) || (amounts == null) || (elements.length != amounts.length)) {
			throw new IllegalArgumentException();
		}
		return this.defaultMultiSelfApply(elements, amounts);
	}

	//
	// The following protected methods are default implementations for sets.
	// They may need to be changed in certain sub-classes.
	//
	protected E defaultApply(final Iterable<Element> elements) {
		if (!elements.iterator().hasNext()) {
			throw new IllegalArgumentException();
		}
		E result = null;
		for (Element element : elements) {
			if (result == null) {
				result = (E) element;
			} else {
				result = this.apply(result, element);
			}
		}
		return result;
	}

	protected E defaultSelfApply(E element, BigInteger amount) {
		if (amount.signum() <= 0) {
			throw new IllegalArgumentException();
		}
		return this.defaultSelfApplyAlgorithm(element, amount);
	}

	protected E defaultSelfApplyAlgorithm(E element, BigInteger posAmount) {
		E result = element;
		for (int i = posAmount.bitLength() - 2; i >= 0; i--) {
			result = this.abstractApply(result, result);
			if (posAmount.testBit(i)) {
				result = this.abstractApply(result, element);
			}
		}
		return result;
	}

	protected E defaultMultiSelfApply(final Element[] elements, final BigInteger[] amounts) {
		if (elements.length == 0) {
			throw new IllegalArgumentException();
		}
		Element[] results = new Element[elements.length];
		for (int i = 0; i < elements.length; i++) {
			results[i] = this.selfApply(elements[i], amounts[i]);
		}
		return this.apply(results);
	}

	//
	// The following protected abstract method must be implemented in every
	// direct sub-class.
	//
	protected abstract E abstractApply(E element1, E element2);

}
