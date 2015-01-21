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
import ch.bfh.unicrypt.helper.Polynomial;
import ch.bfh.unicrypt.math.algebra.additive.abstracts.AbstractEC;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.PolynomialElement;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.PolynomialField;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModElement;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModTwo;
import ch.bfh.unicrypt.math.algebra.dualistic.interfaces.DualisticElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.params.interfaces.StandardECPolynomialFieldParams;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;

import java.math.BigInteger;

/**
 *
 * @author Christian Lutz
 */
public class ECPolynomialField
	   extends AbstractEC<PolynomialField<ZModTwo>, Polynomial<? extends DualisticElement<ZModTwo>>, PolynomialElement<ZModTwo>, ECPolynomialElement> {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public ECPolynomialField(PolynomialField<ZModTwo> finiteField, PolynomialElement<ZModTwo> a,
		   PolynomialElement<ZModTwo> b, PolynomialElement<ZModTwo> gx, PolynomialElement<ZModTwo> gy,
		   BigInteger givenOrder, BigInteger coFactor) {
		super(finiteField, a, b, gx, gy, givenOrder, coFactor);
	}

	public ECPolynomialField(PolynomialField<ZModTwo> finiteField, PolynomialElement<ZModTwo> a, PolynomialElement<ZModTwo> b,
		   BigInteger givenOrder, BigInteger coFactor) {
		super(finiteField, a, b, givenOrder, coFactor);
	}

	@Override
	protected boolean abstractContains(PolynomialElement<ZModTwo> x) {
		DualisticElement<ZModTwo> traceX = traceGF2m(x, this);
		DualisticElement<ZModTwo> traceA = traceGF2m(this.getA(), this);
		DualisticElement<ZModTwo> traceAX = traceGF2m(x.add(this.getA()).add(this.getB().divide(x.square())), this);

		
		boolean e1 = traceA.isEquivalent(traceX);
		boolean e2 = traceAX.isEquivalent(ZModTwo.ZERO);

		return e1 && e2;
	}

	@Override
	protected boolean abstractContains(PolynomialElement<ZModTwo> x, PolynomialElement<ZModTwo> y) {
		PolynomialElement<ZModTwo> left = y.power(2).add(x.multiply(y));
		PolynomialElement<ZModTwo> right = x.power(3).add(x.power(2).multiply(getA())).add(getB());
		return left.isEquivalent(right);
	}

	@Override
	protected ECPolynomialElement abstractGetElement(
		   Point<PolynomialElement<ZModTwo>> value) {
		return new ECPolynomialElement(this, value);
	}

	/**
	 * Return the two possible y-coordinates for a given x-coordinate
	 * <p>
	 * @param x x-coordinate of point
	 * @return
	 */
	public ECPolynomialElement[] getY(PolynomialElement<ZModTwo> x) {
		if (!this.contains(x)) {
			throw new IllegalArgumentException("No y-coordinate exists for the given x-coordinate: " + x);
		}

		PolynomialElement<ZModTwo> t = x.add(this.getA()).add(this.getB().divide(x.square()));
		PolynomialElement<ZModTwo> l = this.getFiniteField().solveQuadradicEquation(t);
		
		ECPolynomialElement y1=this.getElement(x, l.add(l.getSet().getOneElement()).multiply(x));
		ECPolynomialElement y2=this.getElement(x, l.multiply(x));
		ECPolynomialElement[] y={y1,y2};
		return y;
	}

	@Override
	protected ECPolynomialElement abstractGetIdentityElement() {
		return new ECPolynomialElement(this);
	}

	@Override
	protected ECPolynomialElement abstractApply(ECPolynomialElement element1, ECPolynomialElement element2) {
		if (element1.isZero()) {
			return element2;
		}
		if (element2.isZero()) {
			return element1;
		}
		if (element1.equals(element2.invert())) {
			return this.getIdentityElement();
		}
		PolynomialElement<ZModTwo> s, rx, ry;
		PolynomialElement<ZModTwo> px = element1.getX();
		PolynomialElement<ZModTwo> py = element1.getY();
		PolynomialElement<ZModTwo> qx = element2.getX();
		PolynomialElement<ZModTwo> qy = element2.getY();
		if (element1.equals(element2)) {
			final PolynomialElement<ZModTwo> one = this.getFiniteField().getOneElement();
			s = px.add(py.divide(px));
			rx = s.square().add(s).add(this.getA());
			ry = px.square().add((s.add(one)).multiply(rx));
		} else {
			s = py.add(qy).divide(px.add(qx));
			rx = s.square().add(s).add(px).add(qx).add(this.getA());
			ry = s.multiply(px.add(rx)).add(rx).add(py);
		}
		return this.abstractGetElement(Point.getInstance(rx, ry));
	}

	@Override
	protected ECPolynomialElement abstractInvert(ECPolynomialElement element) {
		if (element.isZero()) {
			return this.getZeroElement();
		}
		return this.abstractGetElement(Point.getInstance(element.getX(), element.getY().add(element.getX())));
	}

	@Override
	protected ECPolynomialElement getRandomElementWithoutGenerator(RandomByteSequence randomByteSequence) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Checks curve parameters for validity according SEC1: Elliptic Curve Cryptographie Ver. 1.0 page 21
	 * <p>
	 * @return True if curve parameters are valid
	 * @throws Exception
	 */
	public boolean isValid() throws Exception {
		boolean c2, c3, c4, c5, c6, c7, c8;
		int m = this.getFiniteField().getDegree();
		final BigInteger TWO = new BigInteger("2");

		c2 = this.getFiniteField().isIrreduciblePolynomial(this.getFiniteField().getIrreduciblePolynomial());

		c3 = this.getFiniteField().contains(getA());
		c3 = c3 && this.getFiniteField().contains(getB());
		c3 = c3 && this.getFiniteField().contains(this.getDefaultGenerator().getX());
		c3 = c3 && this.getFiniteField().contains(this.getDefaultGenerator().getY());

		c4 = !this.getB().equals(this.getFiniteField().getZeroElement());

		c5 = this.contains(this.getDefaultGenerator());

		c6 = MathUtil.isPrime(getOrder());

		c7 = this.selfApply(this.getDefaultGenerator(), getOrder()).isEquivalent(this.getZeroElement());

		for (BigInteger i = new BigInteger("1"); i.compareTo(new BigInteger("100")) < 0; i = i.add(BigInteger.ONE)) {
			if (TWO.modPow(i, getOrder()).equals(BigInteger.ONE)) {
				throw new Exception("Curve parameter not valid");
			}
		}

		c8 = !getOrder().multiply(getCoFactor()).equals(TWO.pow(m));

		return c2 && c3 && c4 && c5 && c6 && c7 && c8;
	}
	
	/**
	 * Private method implements selfApply to check if a ECPolynomialElement is a valid generator
	 * @param element
	 * @param posAmount
	 * @return
	 */
	private ECPolynomialElement selfApply(ECPolynomialElement element, BigInteger posAmount) {
		ECPolynomialElement result = element;
		for (int i = posAmount.bitLength() - 2; i >= 0; i--) {
			result = result.add(result);
			if (posAmount.testBit(i)) {
				result = result.add(element);
			}
		}
		return result;
	}

	//
	// STATIC FACTORY METHODS
	//
	/**
	 * Returns an elliptic curve over F2m y²+yx=x³+ax²+b if parameters are valid.
	 * <p>
	 * @param f          Finite field of type BinaryPolynomial
	 * @param a          Element of f representing a in the curve equation
	 * @param b          Element of f representing b in the curve equation
	 * @param givenOrder Order of the the used subgroup
	 * @param coFactor   Co-factor h*order= N -> total order of the group
	 * @return
	 * @throws Exception
	 */
	public static ECPolynomialField getInstance(PolynomialField<ZModTwo> f, PolynomialElement<ZModTwo> a, PolynomialElement<ZModTwo> b, BigInteger givenOrder, BigInteger coFactor) throws Exception {
		ECPolynomialField newInstance = new ECPolynomialField(f, a, b, givenOrder, coFactor);

		if (newInstance.isValid()) {
			return newInstance;
		} else {
			throw new Exception("Curve parameters not valid!");
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
	public static ECPolynomialField getInstance(PolynomialField<ZModTwo> f, PolynomialElement<ZModTwo> a, PolynomialElement<ZModTwo> b, PolynomialElement<ZModTwo> gx, PolynomialElement<ZModTwo> gy, BigInteger givenOrder, BigInteger coFactor) throws Exception {
		ECPolynomialField newInstance = new ECPolynomialField(f, a, b, gx, gy, givenOrder, coFactor);

		if (newInstance.isValid()) {
			return newInstance;
		} else {
			throw new Exception("Curve parameters not valid!");
		}
	}

	public static ECPolynomialField getInstance(final StandardECPolynomialFieldParams params) throws Exception {
		PolynomialField<ZModTwo> field;
		PolynomialElement<ZModTwo> a, b, gx, gy;
		BigInteger order, h;

		field = params.getFiniteField();
		a = params.getA();
		b = params.getB();
		gx = params.getGx();
		gy = params.getGy();
		order = params.getOrder();
		h = params.getH();
		return ECPolynomialField.getInstance(field, a, b, gx, gy, order, h);
	}

	/**
	 * Returns the trace of an polynomial of characteristic 2
	 * <p>
	 * @param x
	 * @param ec
	 * @return
	 */
	public static DualisticElement<ZModTwo> traceGF2m(PolynomialElement<ZModTwo> x, ECPolynomialField ec) {
		int deg = ec.getFiniteField().getDegree();
		DualisticElement<ZModTwo> sum = x.getValue().getCoefficient(0);

		for (int i = 1; i < deg; i++) {
			sum = sum.add(x.getValue().getCoefficient(i));
		}

		return sum;
	}

}
