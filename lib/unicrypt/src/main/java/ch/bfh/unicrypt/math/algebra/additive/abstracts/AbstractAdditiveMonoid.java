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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.additive.abstracts;

import ch.bfh.unicrypt.math.algebra.additive.interfaces.AdditiveElement;
import ch.bfh.unicrypt.math.algebra.additive.interfaces.AdditiveMonoid;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.abstracts.AbstractMonoid;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import java.math.BigInteger;

/**
 * This abstract class provides a basis implementation for objects of type {@link AdditiveMonoid}.
 * <p>
 * @param <E> Generic type of elements of this monoid
 * @param <V> Generic type of values stored in the elements of this monoid
 * @author
 */
public abstract class AbstractAdditiveMonoid<E extends AdditiveElement<V>, V extends Object>
	   extends AbstractMonoid<E, V>
	   implements AdditiveMonoid<V> {

	public AbstractAdditiveMonoid(Class<? extends Object> valueClass) {
		super(valueClass);
	}

	@Override
	public final E add(final Element element1, final Element element2) {
		return this.apply(element1, element2);
	}

	@Override
	public final E add(final Element... elements) {
		return this.apply(elements);
	}

	@Override
	public final E add(final Iterable<Element> elements) {
		return this.apply(elements);
	}

	@Override
	public final E times(final Element element, final BigInteger amount) {
		return this.selfApply(element, amount);
	}

	@Override
	public final E times(final Element element, final Element<BigInteger> amount) {
		return this.selfApply(element, amount);
	}

	@Override
	public final E times(final Element element, final int amount) {
		return this.selfApply(element, amount);
	}

	@Override
	public final E timesTwo(Element element) {
		return this.selfApply(element);
	}

	@Override
	public final E sumOfProducts(Element[] elements, BigInteger[] amounts) {
		return this.multiSelfApply(elements, amounts);
	}

	@Override
	public final E getZeroElement() {
		return this.getIdentityElement();
	}

	@Override
	public final boolean isZeroElement(Element element) {
		return this.isIdentityElement(element);
	}

}
