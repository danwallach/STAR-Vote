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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.additive.classes;

import ch.bfh.unicrypt.helper.MathUtil;
import ch.bfh.unicrypt.helper.Point;
import ch.bfh.unicrypt.math.algebra.additive.abstracts.AbstractEC;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModElement;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModPrime;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.params.interfaces.StandardECZModParams;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
import java.math.BigInteger;

/**
 *
 * @author Christian Lutz
 */
public class ECZModPrime
	   extends AbstractEC<ZModPrime, BigInteger, ZModElement, ECZModElement> {

	protected ECZModPrime(ZModPrime finiteField, ZModElement a, ZModElement b, ZModElement gx, ZModElement gy, BigInteger givenOrder, BigInteger coFactor) {
		super(finiteField, a, b, gx, gy, givenOrder, coFactor);
	}

	protected ECZModPrime(ZModPrime finiteField, ZModElement a, ZModElement b, BigInteger givenOrder, BigInteger coFactor) {
		super(finiteField, a, b, givenOrder, coFactor);
	}

	@Override
	public boolean abstractContains(ZModElement x) {
		BigInteger p = this.getFiniteField().getModulus();
		ZModElement right = x.power(3).add(getA().multiply(x)).add(getB());
		if (MathUtil.hasSqrtModPrime(right.getValue(), p)) {
			BigInteger y1 = MathUtil.sqrtModPrime(right.getValue(), p);
			ZModElement y = this.getFiniteField().getElement(y1);
			return this.abstractContains(x, y);
		} else {
			return false;
		}
	}

	@Override
	protected boolean abstractContains(ZModElement x, ZModElement y) {
		ZModElement left = y.square();
		ZModElement right = x.power(3).add(x.multiply(this.getA())).add(this.getB());
		return left.isEquivalent(right);
	}

	@Override
	protected ECZModElement abstractGetElement(Point<ZModElement> value) {
		return new ECZModElement(this, value);
	}

	@Override
	protected ECZModElement abstractGetIdentityElement() {
		return new ECZModElement(this);
	}

	@Override
	protected ECZModElement abstractApply(ECZModElement element1, ECZModElement element2) {
		if (element1.isZero()) {
			return element2;
		}
		if (element2.isZero()) {
			return element1;
		}
		if (element1.isEquivalent(element2.invert())) {
			return this.getZeroElement();
		}
		ZModElement s, rx, ry;
		ZModElement px = element1.getX();
		ZModElement py = element1.getY();
		ZModElement qx = element2.getX();
		ZModElement qy = element2.getY();
		if (element1.isEquivalent(element2)) {
			ZModElement three = this.getFiniteField().getElement(3);
			ZModElement two = this.getFiniteField().getElement(2);
			s = ((px.square().multiply(three)).apply(this.getA())).divide(py.multiply(two));
			rx = s.square().apply(px.multiply(two).invert());
			ry = s.multiply(px.subtract(rx)).apply(py.invert());
		} else {
			s = py.apply(qy.invert()).divide(px.apply(qx.invert()));
			rx = (s.square().apply(px.invert()).apply(qx.invert()));
			ry = py.invert().add(s.multiply(px.apply(rx.invert())));
		}
		return this.abstractGetElement(Point.getInstance(rx, ry));
	}

	@Override
	protected ECZModElement abstractInvert(ECZModElement element) {
		if (element.isZero()) {
			return this.getZeroElement();
		}
		return this.abstractGetElement(Point.getInstance(element.getX(), element.getY().invert()));
	}

	@Override
	protected ECZModElement getRandomElementWithoutGenerator(RandomByteSequence randomByteSequence) {
		BigInteger p = this.getFiniteField().getModulus();
		ZModElement x = this.getFiniteField().getRandomElement(randomByteSequence);
		ZModElement y = x.power(3).add(this.getA().multiply(x)).add(this.getB());
		boolean neg = x.getValue().mod(new BigInteger("2")).equals(BigInteger.ONE);

		while (!MathUtil.hasSqrtModPrime(y.getValue(), p)) {
			x = this.getFiniteField().getRandomElement(randomByteSequence);
			y = x.power(3).add(this.getA().multiply(x)).add(this.getB());
		}
		//if neg is true return solution 2(p-sqrt) of sqrtModPrime else solution 1
		if (neg) {
			y = this.getFiniteField().getElement(p.subtract(MathUtil.sqrtModPrime(y.getValue(), p)));
		} else {
			y = this.getFiniteField().getElement(MathUtil.sqrtModPrime(y.getValue(), p));
		}
		return this.abstractGetElement(Point.getInstance(x, y));
	}

	/**
	 * Checks curve parameters for validity according SEC1: Elliptic Curve Cryptographie Ver. 1.0 page 18
	 * <p>
	 * @return True if curve parameters are valid
	 * @throws Exception
	 */
	public boolean isValid() throws Exception {
		boolean c11, c21, c22, c23, c24, c3, c4, c5, c61, c62;

		ZModElement i4 = getFiniteField().getElement(4);
		ZModElement i27 = getFiniteField().getElement(27);
		BigInteger p = this.getFiniteField().getModulus();

		c11 = MathUtil.arePrime(p);
		c21 = this.getFiniteField().contains(this.getA());
		c22 = this.getFiniteField().contains(this.getB());
		c23 = this.getFiniteField().contains(this.getDefaultGenerator().getValue().getX());
		c24 = this.getFiniteField().contains(this.getDefaultGenerator().getValue().getY());
		c3 = !getA().power(3).multiply(i4).add(i27.multiply(getB().square())).isZero();
		c4 = 0 >= getCoFactor().compareTo(new BigInteger("4"));
		c5 = this.selfApply(this.getDefaultGenerator(), getOrder()).isEquivalent(this.getZeroElement());
		c61 = true; //TODO
		for (BigInteger i = new BigInteger("1"); i.compareTo(new BigInteger("100")) < 0; i = i.add(BigInteger.ONE)) {
			if (p.modPow(i, getOrder()).equals(BigInteger.ONE)) {
				throw new Exception("Curve parameter not valid");
			}
		}
		c62 = !getOrder().equals(this.getFiniteField().getModulus());
		return c11 && c21 && c22 && c23 && c24 && c3 && c4 && c5 && c61 && c62;
	}
	
	/**
	 * Private method implements selfApply to check if a ECZmodElement is a valid generator
	 * @param element
	 * @param posAmount
	 * @return
	 */
	private ECZModElement selfApply(ECZModElement element, BigInteger posAmount) {
		ECZModElement result = element;
		for (int i = posAmount.bitLength() - 2; i >= 0; i--) {
			result = result.add(result);
			if (posAmount.testBit(i)) {
				result = result.add(element);
			}
		}
		return result;
	}

	/**
	 * Returns an elliptic curve over Fp y²=x³+ax+b
	 * <p>
	 * @param f          Finite field of type ZModPrime
	 * @param a          Element of F_p representing a in the curve equation
	 * @param b          Element of F_p representing b in the curve equation
	 * @param givenOrder Order of the the used subgroup
	 * @param coFactor   Co-factor h*order= N -> total order of the group
	 * @return
	 * @throws Exception
	 */
	public static ECZModPrime getInstance(ZModPrime f, ZModElement a, ZModElement b, BigInteger givenOrder, BigInteger coFactor) throws Exception {

		ECZModPrime newInstance = new ECZModPrime(f, a, b, givenOrder, coFactor);
		if (newInstance.isValid()) {
			return newInstance;
		} else {
			throw new IllegalArgumentException("Curve parameter are not valid!");
		}
	}

	/**
	 * Returns an elliptic curve over Fp y²=x³+ax+b if parameters are valid.
	 * <p>
	 * @param f          Finite field of type ZModPrime
	 * @param a          Element of F_p representing a in the curve equation
	 * @param b          Element of F_p representing b in the curve equation
	 * @param gx         x-coordinate of the generator
	 * @param gy         y-coordinate of the generator
	 * @param givenOrder Order of the the used subgroup
	 * @param coFactor   Co-factor h*order= N -> total order of the group
	 * @return
	 * @throws Exception
	 */
	public static ECZModPrime getInstance(ZModPrime f, ZModElement a, ZModElement b, ZModElement gx, ZModElement gy, BigInteger givenOrder, BigInteger coFactor) throws Exception {
		ECZModPrime newInstance = new ECZModPrime(f, a, b, gx, gy, givenOrder, coFactor);
		if (newInstance.isValid()) {
			return newInstance;
		} else {
			throw new IllegalArgumentException("Curve parameter are not valid!");
		}
	}

	public static ECZModPrime getInstance(final StandardECZModParams params) throws Exception {
		ZModPrime field;
		ZModElement a, b, gx, gy;
		BigInteger order, h;

		field = params.getFiniteField();
		a = params.getA();
		b = params.getB();
		gx = params.getGx();
		gy = params.getGy();
		order = params.getOrder();
		h = params.getH();

		return ECZModPrime.getInstance(field, a, b, gx, gy, order, h);
	}

}
