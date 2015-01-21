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

import ch.bfh.unicrypt.math.algebra.dualistic.interfaces.DualisticElement;
import ch.bfh.unicrypt.math.algebra.dualistic.interfaces.Field;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.unicrypt.math.algebra.multiplicative.interfaces.MultiplicativeGroup;
import java.math.BigInteger;

/**
 * This abstract class provides a basis implementation for objects of type {@link Field}.
 * <p>
 * @param <E> Generic type of elements of this field
 * @param <M> Generic type of the {@link MultiplicativeGroup} of this field
 * @param <V> Generic type of values stored in the elements of this field
 * @author rolfhaenni
 */
public abstract class AbstractField<E extends DualisticElement<V>, M extends MultiplicativeGroup, V extends Object>
	   extends AbstractRing<E, V>
	   implements Field<V> {

	private M multiplicativeGroup;

	public AbstractField(Class<? extends Object> valueClass) {
		super(valueClass);
	}

	@Override
	public M getMultiplicativeGroup() {
		if (this.multiplicativeGroup == null) {
			this.multiplicativeGroup = this.abstractGetMultiplicativeGroup();
		}
		return this.multiplicativeGroup;
	}

	@Override
	public final E divide(Element element1, Element element2) {
		return this.multiply(element1, this.oneOver(element2));
	}

	@Override
	public final E oneOver(Element element) {
		if (!this.contains(element)) {
			throw new IllegalArgumentException();
		}
		if (((E) element).isZero()) {
			throw new UnsupportedOperationException();
		}
		return this.abstractOneOver((E) element);
	}

	@Override
	protected E defaultPower(E element, BigInteger amount) {
		if (element.isZero()) {
			return element;
		}
		boolean negAmount = (amount.signum() < 0);
		amount = amount.abs();
		if (this.isFinite() && this.hasKnownOrder()) {
			amount = amount.mod(this.getOrder().subtract(BigInteger.ONE));
		}
		if (amount.signum() == 0) {
			return this.getOneElement();
		}
		E result = this.defaultPowerAlgorithm(element, amount);
		if (negAmount) {
			return this.oneOver(result);
		}
		return result;
	}

	/**
	 *
	 * @param element
	 * @return
	 */
	protected abstract E abstractOneOver(E element);

	/**
	 *
	 * @return
	 */
	protected abstract M abstractGetMultiplicativeGroup();

}
