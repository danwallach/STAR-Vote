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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.multiplicative.abstracts;

import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.abstracts.AbstractElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.unicrypt.math.algebra.multiplicative.interfaces.MultiplicativeElement;
import ch.bfh.unicrypt.math.algebra.multiplicative.interfaces.MultiplicativeGroup;
import ch.bfh.unicrypt.math.algebra.multiplicative.interfaces.MultiplicativeMonoid;
import ch.bfh.unicrypt.math.algebra.multiplicative.interfaces.MultiplicativeSemiGroup;
import java.math.BigInteger;

/**
 *
 * This abstract class provides a basis implementation for objects of type {@link MultiplicativeElement}.
 * <p>
 * @param <S> Generic type of the {@link MultiplicativeSemiGroup} of this element
 * @param <E> Generic type of this element
 * @param <V> Generic type of value stored in this element
 * @author rolfhaenni
 */
public abstract class AbstractMultiplicativeElement<S extends MultiplicativeSemiGroup<V>, E extends MultiplicativeElement<V>, V extends Object>
	   extends AbstractElement<S, E, V>
	   implements MultiplicativeElement<V> {

	protected AbstractMultiplicativeElement(final S semiGroup, final V value) {
		super(semiGroup, value);
	}

	/**
	 * @see Group#apply(Element, Element)
	 */
	@Override
	public final E multiply(final Element element) {
		return (E) this.getSet().multiply(this, element);
	}

	/**
	 * @see Group#applyInverse(Element, Element)
	 */
	@Override
	public final E divide(final Element element) {
		if (this.getSet().isGroup()) {
			MultiplicativeGroup group = ((MultiplicativeGroup) this.getSet());
			return (E) group.divide(this, element);
		}
		throw new UnsupportedOperationException();
	}

	/**
	 * @see Group#T(Element, BigInteger)
	 */
	@Override
	public final E power(final BigInteger amount) {
		return (E) this.getSet().power(this, amount);
	}

	/**
	 * @see Group#selfApply(Element, Element)
	 */
	@Override
	public final E power(final Element<BigInteger> amount) {
		return (E) this.getSet().power(this, amount);
	}

	/**
	 * @see Group#selfApply(Element, int)
	 */
	@Override
	public final E power(final int amount) {
		return (E) this.getSet().power(this, amount);
	}

	/**
	 * @see Group#selfApply(Element)
	 */
	@Override
	public final E square() {
		return (E) this.getSet().square(this);
	}

	@Override
	public final E oneOver() {
		if (this.getSet().isGroup()) {
			MultiplicativeGroup group = ((MultiplicativeGroup) this.getSet());
			return (E) group.invert(this);
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isOne() {
		if (this.getSet().isMonoid()) {
			MultiplicativeMonoid monoid = ((MultiplicativeMonoid) this.getSet());
			return monoid.isOneElement(this);
		}
		throw new UnsupportedOperationException();
	}

}
