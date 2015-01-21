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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes;

import ch.bfh.unicrypt.helper.Polynomial;
import ch.bfh.unicrypt.math.algebra.dualistic.interfaces.DualisticElement;
import ch.bfh.unicrypt.math.algebra.dualistic.interfaces.FiniteField;
import ch.bfh.unicrypt.math.algebra.dualistic.interfaces.PrimeField;
import ch.bfh.unicrypt.math.algebra.dualistic.interfaces.Ring;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Pair;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Triple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.unicrypt.math.algebra.multiplicative.interfaces.MultiplicativeGroup;
import ch.bfh.unicrypt.random.classes.HybridRandomByteSequence;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
import java.math.BigInteger;

/**
 *
 * @author rolfhaenni
 * @param <V>
 */
public class PolynomialField<V>
	   extends PolynomialRing<V>
	   implements FiniteField<Polynomial<? extends DualisticElement<V>>> {

	private final PolynomialElement<V> irreduciblePolynomial;

	protected PolynomialField(PrimeField primeField, PolynomialElement<V> irreduciblePolynomial) {
		super(primeField);
		this.irreduciblePolynomial = irreduciblePolynomial;
	}

	public PrimeField<V> getPrimeField() {
		return (PrimeField<V>) super.getRing();
	}

	public PolynomialElement<V> getIrreduciblePolynomial() {
		return this.irreduciblePolynomial;
	}

	public int getDegree() {
		return this.irreduciblePolynomial.getValue().getDegree();
	}

	//
	// The following protected methods override the default implementation from
	// various super-classes
	//
	@Override
	protected BigInteger abstractGetOrder() {
		// p^m
		return this.getCharacteristic().pow(this.getDegree());
	}

	@Override
	protected boolean abstractContains(Polynomial value) {
		return super.abstractContains(value) && value.getDegree() < this.getDegree();
	}

	@Override
	protected PolynomialElement<V> abstractGetElement(Polynomial value) {
		return new PolynomialElement<V>(this, value);
	}

	@Override
	public PolynomialElement<V> getRandomElement(int degree, RandomByteSequence randomByteSequence) {
		if (degree >= this.getDegree()) {
			throw new IllegalArgumentException();
		}
		return super.getRandomElement(degree, randomByteSequence);
	}

	@Override
	public PolynomialElement<V> getRandomMonicElement(int degree, boolean a0NotZero, RandomByteSequence randomByteSequence) {
		if (degree >= this.getDegree()) {
			throw new IllegalArgumentException();
		}
		return super.getRandomMonicElement(degree, a0NotZero, randomByteSequence);
	}

	@Override
	public BigInteger getCharacteristic() {
		return this.getPrimeField().getOrder();
	}

	@Override
	public MultiplicativeGroup<Polynomial<? extends DualisticElement<V>>> getMultiplicativeGroup() {
		// TODO Create muliplicative.classes.FStar (Definition 2.228, Fact
		// 2.229/2.230)
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	protected PolynomialElement<V> abstractMultiply(PolynomialElement<V> element1, PolynomialElement<V> element2) {
		Polynomial<? extends DualisticElement<V>> polynomial1 = element1.getValue();
		Polynomial<? extends DualisticElement<V>> polynomial2 = element2.getValue();

		if (element1.isEquivalent(this.getZeroElement()) || element2.isEquivalent(this.getZeroElement())) {
			return this.getZeroElement();
		}
		final PolynomialRing<V> ring = PolynomialRing.getInstance((Ring<V>) this.getSemiRing());
		PolynomialElement<V> result;
		if (this.isBinary()) {
			result = ring.getElementUnchecked(multiplyBinary(polynomial1, polynomial2));
		} else {
			result = ring.getElementUnchecked(multiplyNonBinary(polynomial1, polynomial2));
		}
		return this.getElement(this.mod(result).getValue());
	}

	@Override
	public PolynomialElement<V> divide(Element element1, Element element2) {
		return this.multiply(element1, this.oneOver(element2));
	}

	/**
	 * oneOver.
	 * <p>
	 * Compute using extended Euclidean algorithm for polynomial (Algorithm 2.226)
	 * <p>
	 * <p>
	 * @param element
	 * @return
	 */
	@Override
	public PolynomialElement<V> oneOver(Element element) {

		if (!this.contains(element)) {
			throw new IllegalArgumentException();
		}

		if (element.isEquivalent(this.getZeroElement())) {
			throw new UnsupportedOperationException();
		}

		Triple euclid = this.extendedEuclidean((PolynomialElement<V>) element, this.irreduciblePolynomial);
		return this.getElement(((PolynomialElement<V>) euclid.getSecond()).getValue());

	}

	/**
	 * Mod. g(x) mod irreduciblePolynomial = h(x)
	 * <p>
	 * Z_p must be a field.
	 * <p>
	 * <p>
	 * @param g g(x) in Z_p[x]
	 * @return h(x) in Z_p[x]
	 */
	private PolynomialElement<V> mod(PolynomialElement<V> g) {
		if (g.getValue().getDegree() < this.getDegree()) {
			return g;
		}
		Pair longDiv = this.longDivision(g, this.irreduciblePolynomial);
		return (PolynomialElement<V>) longDiv.getSecond();
	}

	/**
	 * Computes a solution for the quadratic equation z²+z=b for any polynomial basis. Source: AMERICAN NATIONAL
	 * STANDARD X9.62 D.1.6
	 * <p>
	 * @return PolynomialElement z which is a solution for the quadratic equation z²+z=b
	 */
	public PolynomialElement<V> solveQuadradicEquation(PolynomialElement<V> b) {
		PolynomialElement<V> y = this.getZeroElement();
		PolynomialElement<V> z = this.getZeroElement();

		while (y.isEquivalent(this.getZeroElement())) {

			PolynomialElement<V> r = this.getRandomElement(this.getDegree()-1);
			z = this.getZeroElement();
			PolynomialElement<V> w = b;
			int m = this.getDegree();

			for (int i = 1; i < m; i++) {

				PolynomialElement<V> w2=w.square();
				z = z.square().add(w2.multiply(r));
				w = w2.add(b);
			}

			y = z.square().add(z);
			if (!w.isEquivalent(this.getZeroElement())) {
				throw new IllegalArgumentException("No solution for quadratic equation was found");
			}

		}

		return z;
	}

	/**
	 * Test if there is a solution for the quadratic equation z²+z=b for any polynomial basis. Source: AMERICAN NATIONAL
	 * STANDARD X9.62 D.1.6
	 * <p>
	 * @param b
	 * @return true/false
	 */
	public boolean hasQuadradicEquationSolution(PolynomialElement<V> b) {
		PolynomialElement<V> y = this.getZeroElement();
		PolynomialElement<V> z;

		while (y.equals(this.getZeroElement())) {

			PolynomialElement<V> r = this.getRandomElement(this.getDegree() - 1);
			z = this.getZeroElement();
			PolynomialElement<V> w = b;
			int m = this.getDegree();

			for (int i = 1; i <= m - 1; i++) {
				z = z.square().add(w.square().multiply((r)));
				w = w.square().add(b);
			}

			y = z.square().add(z);
			if (!w.equals(this.getZeroElement())) {
				return false;
			}

		}

		return true;
	}

	//
	// STATIC FACTORY METHODS
	//
	public static <V> PolynomialField getInstance(PrimeField primeField,
		   int degree) {
		return getInstance(primeField, degree,
						   HybridRandomByteSequence.getInstance());
	}

	public static <V> PolynomialField getInstance(PrimeField primeField,
		   int degree, RandomByteSequence randomByteSequence) {
		if (primeField == null || degree < 1) {
			throw new IllegalArgumentException();
		}
		PolynomialRing<V> ring = PolynomialRing.getInstance(primeField);
		PolynomialElement<V> irreduciblePolynomial = ring
			   .findIrreduciblePolynomial(degree, randomByteSequence);
		return new PolynomialField(primeField, irreduciblePolynomial);
	}

	public static <V> PolynomialField getInstance(PrimeField primeField,
		   PolynomialElement<V> irreduciblePolynomial) {
		if (primeField == null
			   || irreduciblePolynomial == null
			   || !irreduciblePolynomial.getSet().getSemiRing()
			   .isEquivalent(primeField)
			   || !irreduciblePolynomial.isIrreducible()) {
			throw new IllegalArgumentException();
		}
		return new PolynomialField(primeField, irreduciblePolynomial);
	}

}
