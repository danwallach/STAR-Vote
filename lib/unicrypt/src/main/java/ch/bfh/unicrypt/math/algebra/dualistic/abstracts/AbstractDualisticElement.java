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

import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.additive.abstracts.AbstractAdditiveElement;
import ch.bfh.unicrypt.math.algebra.dualistic.interfaces.DualisticElement;
import ch.bfh.unicrypt.math.algebra.dualistic.interfaces.Field;
import ch.bfh.unicrypt.math.algebra.dualistic.interfaces.SemiRing;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import java.math.BigInteger;

/**
 * This abstract class provides a basis implementation for objects of type {@link DualisticElement}.
 * <p>
 * @param <S> Generic type of the {@link Semiring} of this element
 * @param <E> Generic type of this element
 * @param <V> Generic type of value stored in this element
 * <p>
 * @author
 */
public abstract class AbstractDualisticElement<S extends SemiRing<V>, E extends DualisticElement<V>, V extends Object>
	   extends AbstractAdditiveElement<S, E, V>
	   implements DualisticElement<V> {

	protected AbstractDualisticElement(final S ring, final V value) {
		super(ring, value);
	}

	@Override
	public final E multiply(final Element element) {
		return (E) this.getSet().multiply(this, element);
	}

	@Override
	public final E power(final BigInteger amount) {
		return (E) this.getSet().power(this, amount);
	}

	@Override
	public final E power(final Element<BigInteger> amount) {
		return (E) this.getSet().power(this, amount);
	}

	@Override
	public final E power(final int amount) {
		return (E) this.getSet().power(this, amount);
	}

	@Override
	public final E square() {
		return (E) this.getSet().square(this);
	}

	@Override
	public final E divide(final Element element) {
		if (this.getSet().isField()) {
			Field field = ((Field) this.getSet());
			return (E) field.divide(this, element);
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public final E oneOver() {
		if (this.getSet().isField()) {
			Field field = ((Field) this.getSet());
			return (E) field.oneOver(this);
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isOne() {
		return this.getSet().isOneElement(this);
	}

}
