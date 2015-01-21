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

import ch.bfh.unicrypt.helper.MathUtil;
import ch.bfh.unicrypt.helper.Point;
import ch.bfh.unicrypt.helper.converter.abstracts.AbstractBigIntegerConverter;
import ch.bfh.unicrypt.helper.converter.interfaces.BigIntegerConverter;
import ch.bfh.unicrypt.math.algebra.additive.interfaces.EC;
import ch.bfh.unicrypt.math.algebra.additive.interfaces.ECElement;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.ZMod;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModPrime;
import ch.bfh.unicrypt.math.algebra.dualistic.interfaces.DualisticElement;
import ch.bfh.unicrypt.math.algebra.dualistic.interfaces.FiniteField;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Pair;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.ZStarModPrime;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
import java.math.BigInteger;

/**
 * This abstract class provides a basis implementation for objects of type {@link EC}.
 * <p>
 * @param <F>  Generic type of the {@link FiniteField} of this elliptic curve
 * @param <V>  Generic type of values stored in the elements of this elliptic curve
 * @param <D>
 * @param <EE>
 */
public abstract class AbstractEC<F extends FiniteField<V>, V extends Object, D extends DualisticElement<V>, EE extends ECElement<V, D>>
	   extends AbstractAdditiveCyclicGroup<EE, Point<D>>
	   implements EC<V, D> {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private final F finiteField;
	private final D a, b;
	private final EE givenGenerator;
	private final BigInteger givenOrder, coFactor;
	private final Point<DualisticElement<V>> infinityPoint = Point.<DualisticElement<V>>getInstance();

	protected AbstractEC(F finiteField, D a, D b, D gx, D gy, BigInteger givenOrder, BigInteger coFactor) {
		super(Point.class);
		this.finiteField = finiteField;
		this.a = a;
		this.b = b;
		this.givenOrder = givenOrder;
		this.coFactor = coFactor;
		this.givenGenerator = this.getElement(gx, gy);
	}

	protected AbstractEC(F finitefield, D a, D b, BigInteger givenOrder, BigInteger coFactor) {
		super(Pair.class);
		this.finiteField = finitefield;
		this.a = a;
		this.b = b;
		this.givenOrder = givenOrder;
		this.coFactor = coFactor;
		this.givenGenerator = this.computeGenerator();
	}

	@Override
	public final F getFiniteField() {
		return this.finiteField;
	}

	@Override
	public final D getB() {
		return this.b;
	}

	@Override
	public final D getA() {
		return this.a;
	}

	@Override
	public final BigInteger getCoFactor() {
		return this.coFactor;
	}

	@Override
	public ZModPrime getZModOrder() {
		return ZModPrime.getInstance(this.getOrder());
	}

	@Override
	public ZStarModPrime getZStarModOrder() {
		return ZStarModPrime.getInstance(this.getOrder());
	}

	@Override
	public final boolean contains(D xValue) {
		if (xValue == null || !this.getFiniteField().contains(xValue)) {
			throw new IllegalArgumentException();
		}
		return this.abstractContains((D) xValue);
	}

	protected abstract boolean abstractContains(D xValue);

	@Override
	public final boolean contains(D xValue, D yValue) {
		if (xValue == null || yValue == null || !this.getFiniteField().contains(xValue) || !this.getFiniteField().contains(yValue)) {
			throw new IllegalArgumentException();
		}
		return this.abstractContains((D) xValue, (D) yValue);
	}

	protected abstract boolean abstractContains(D xValue, D yValue);

	@Override
	public final EE getElement(D xValue, D yValue) {
		if (!this.contains(xValue, yValue)) {
			throw new IllegalArgumentException();
		}
		return this.abstractGetElement(Point.getInstance((D) xValue, (D) yValue));
	}

	@Override
	protected BigInteger abstractGetOrder() {
		return this.givenOrder;
	}

	@Override
	protected boolean abstractContains(Point<D> value) {
		return this.abstractContains(value.getX(), value.getY());
	}

	@Override
	protected BigIntegerConverter<Point<D>> abstractGetBigIntegerConverter() {
		return new AbstractBigIntegerConverter<Point<D>>(null) { // class parameter not needed

			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			protected BigInteger abstractConvert(Point<D> point) {
				if (point.equals(infinityPoint)) {
					return BigInteger.ZERO;
				}
				return MathUtil.pair(point.getX().getBigInteger(), point.getY().getBigInteger()).add(BigInteger.ONE);
			}

			@Override
			protected Point<D> abstractReconvert(BigInteger value) {
				if (value.equals(BigInteger.ZERO)) {
					return getZeroElement().getValue();
				}
				BigInteger[] result = MathUtil.unpair(value.subtract(BigInteger.ONE));
				DualisticElement<V> xValue = getFiniteField().getElementFrom(result[0]);
				DualisticElement<V> yValue = getFiniteField().getElementFrom(result[1]);
				if (xValue == null || yValue == null) {
					return null; // no such element
				}
				return Point.getInstance((D) xValue, (D) yValue);
			}
		};
	}

	@Override
	protected EE abstractGetDefaultGenerator() {
		return this.givenGenerator;
	}

	private EE computeGenerator() {
		EE element = this.selfApply(this.getRandomElement(), this.getCoFactor());
		while (!this.isGenerator(element)) {
			element = this.getRandomElement();
		}
		return element;
	}

	@Override
	protected boolean abstractIsGenerator(EE element) {
		return MathUtil.isPrime(this.getOrder()) && this.selfApply(element, this.getOrder()).isZero();
	}

	@Override
	protected EE abstractGetRandomElement(RandomByteSequence randomByteSequence) {
		if (this.getDefaultGenerator() != null) {
			ZMod r = ZMod.getInstance(this.getFiniteField().getOrder());
			return this.selfApply(this.getDefaultGenerator(), r.getRandomElement().getBigInteger());
		} else {
			return this.getRandomElementWithoutGenerator(randomByteSequence);
		}
	}

	@Override
	protected boolean abstractEquals(Set set) {
		AbstractEC<F, V, D, EE> other = (AbstractEC<F, V, D, EE>) set;
		if (!this.finiteField.isEquivalent(other.finiteField)) {
			return false;
		}
		if (!this.a.equals(other.a)) {
			return false;
		}
		if (!this.b.equals(other.b)) {
			return false;
		}
		if (!this.givenOrder.equals(other.givenOrder)) {
			return false;
		}
		if (!this.coFactor.equals(other.coFactor)) {
			return false;
		}
		if (!this.givenGenerator.equals(other.givenGenerator)) {
			return false;
		}
		return true;
	}

	@Override
	protected int abstractHashCode() {
		int hash = 7;
		hash = 47 * hash + this.finiteField.hashCode();
		hash = 47 * hash + this.a.hashCode();
		hash = 47 * hash + this.b.hashCode();
		hash = 47 * hash + this.givenOrder.hashCode();
		hash = 47 * hash + this.coFactor.hashCode();
		hash = 47 * hash + this.givenGenerator.hashCode();
		return hash;
	}

	@Override
	protected boolean defaultIsEquivalent(Set set) {
		AbstractEC<F, V, D, EE> other = (AbstractEC<F, V, D, EE>) set;
		if (!this.finiteField.isEquivalent(other.finiteField)) {
			return false;
		}
		if (!this.a.isEquivalent(other.a)) {
			return false;
		}
		if (!this.b.isEquivalent(other.b)) {
			return false;
		}
		if (!this.givenOrder.equals(other.givenOrder)) {
			return false;
		}
		if (!this.coFactor.equals(other.coFactor)) {
			return false;
		}
		return true;
	}

	/**
	 * Returns random element with coFactorout knowing a generator of tcoFactore group.
	 * <p>
	 * @param randomByteSequence The given random byte sequence
	 * @return The random element
	 */
	protected abstract EE getRandomElementWithoutGenerator(RandomByteSequence randomByteSequence);

	@Override
	protected String defaultToStringValue() {
		return this.getA().getValue() + "," + this.getB().getValue();
	}

}
