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
import ch.bfh.unicrypt.math.algebra.additive.interfaces.AdditiveGroup;
import ch.bfh.unicrypt.math.algebra.additive.interfaces.AdditiveMonoid;
import ch.bfh.unicrypt.math.algebra.additive.interfaces.AdditiveSemiGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.abstracts.AbstractElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import java.math.BigInteger;

/**
 * This abstract class provides a basis implementation for objects of type {@link AdditiveElement}.
 * <p>
 * TODO
 * <p>
 * @param <S> Generic type of {@link AdditiveSemiGroup} of this element
 * @param <E> Generic type of the element
 * @param <V> Generic type of value stored in the element and the elements of the additive semigroup
 * @see Element
 * <p>
 * @author rolfhaenni
 */
public abstract class AbstractAdditiveElement<S extends AdditiveSemiGroup<V>, E extends AdditiveElement<V>, V extends Object>
	   extends AbstractElement<S, E, V>
	   implements AdditiveElement<V> {

	protected AbstractAdditiveElement(final S semiGroup, final V value) {
		super(semiGroup, value);
	}

	@Override
	public final E add(final Element element) {
		return (E) this.getSet().add(this, element);
	}

	@Override
	public final E subtract(final Element element) {
		if (this.getSet().isGroup()) {
			AdditiveGroup group = ((AdditiveGroup) this.getSet());
			return (E) group.subtract(this, element);
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public final E times(final BigInteger amount) {
		return (E) this.getSet().times(this, amount);
	}

	@Override
	public final E times(final Element<BigInteger> amount) {
		return (E) this.getSet().times(this, amount);
	}

	@Override
	public final E times(final int amount) {
		return (E) this.getSet().times(this, amount);
	}

	@Override
	public final E timesTwo() {
		return (E) this.getSet().timesTwo(this);
	}

	@Override
	public final E negate() {
		if (this.getSet().isGroup()) {
			AdditiveGroup group = ((AdditiveGroup) this.getSet());
			return (E) group.invert(this);
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isZero() {
		if (this.getSet().isMonoid()) {
			AdditiveMonoid monoid = ((AdditiveMonoid) this.getSet());
			return monoid.isZeroElement(this);
		}
		throw new UnsupportedOperationException();
	}

}
