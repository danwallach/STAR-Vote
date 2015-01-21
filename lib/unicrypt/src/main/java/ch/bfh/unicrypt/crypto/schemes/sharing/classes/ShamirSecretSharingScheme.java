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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.sharing.classes;

import ch.bfh.unicrypt.crypto.schemes.sharing.abstracts.AbstractThresholdSecretSharingScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.PolynomialElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.PolynomialRing;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModPrime;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.interfaces.DualisticElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Pair;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
import java.math.BigInteger;

/**
 *
 * @author Rolf Haenni <rolf.haenni@bfh.ch>
 */
public class ShamirSecretSharingScheme
	   extends AbstractThresholdSecretSharingScheme<ZModPrime, ZModElement, ProductGroup, Pair> {

	private final ZModPrime zModPrime;
	private final PolynomialRing polynomialRing;

	protected ShamirSecretSharingScheme(ZModPrime zModPrime, int size, int threshold) {
		super(zModPrime, ProductGroup.getInstance(zModPrime, 2), size, threshold);
		this.zModPrime = zModPrime;
		this.polynomialRing = PolynomialRing.getInstance(zModPrime);
	}

	public ZModPrime getZModPrime() {
		return this.zModPrime;
	}

	public PolynomialRing getPolynomialRing() {
		return this.polynomialRing;
	}

	@Override
	protected Tuple abstractShare(Element message, RandomByteSequence randomByteSequence) {

		// create an array of coefficients with size threshold
		// the coefficient of degree 0 is fixed (message)
		// all other coefficients are random
		DualisticElement[] coefficients = new DualisticElement[getThreshold()];
		coefficients[0] = (DualisticElement) message;
		for (int i = 1; i < getThreshold(); i++) {
			coefficients[i] = this.zModPrime.getRandomElement(randomByteSequence);
		}

		// create a polynomial out of the coefficients
		final PolynomialElement polynomial = this.polynomialRing.getElement(coefficients);

		// create a tuple which stores the shares
		Pair[] shares = new Pair[this.getSize()];
		DualisticElement xVal;

		// populate the tuple array with tuples of x and y values
		for (int i = 0; i < this.getSize(); i++) {
			xVal = this.zModPrime.getElement(BigInteger.valueOf(i + 1));
			shares[i] = polynomial.getPoint(xVal);
		}
		// the following line is more efficient than Tuple.getInstance(shares)
		return Tuple.getInstance(shares);
	}

	@Override
	protected ZModElement abstractRecover(Tuple shares) {
		int length = shares.getArity();
		// Calculating the lagrange coefficients for each point we got
		DualisticElement product;
		DualisticElement[] lagrangeCoefficients = new DualisticElement[length];
		for (int j = 0; j < length; j++) {
			product = null;
			DualisticElement elementJ = (DualisticElement) shares.getAt(j, 0);
			for (int l = 0; l < length; l++) {
				DualisticElement elementL = (DualisticElement) shares.getAt(l, 0);
				if (!elementJ.equals(elementL)) {
					if (product == null) {
						product = elementL.divide(elementL.subtract(elementJ));
					} else {
						product = product.multiply(elementL.divide(elementL.subtract(elementJ)));
					}
				}
			}
			lagrangeCoefficients[j] = product;
		}
		// multiply the y-value of the point with the lagrange coefficient and sum everything up
		ZModElement result = this.zModPrime.getIdentityElement();
		for (int j = 0; j < length; j++) {
			DualisticElement value = (DualisticElement) shares.getAt(j, 1);
			result = result.add(value.multiply(lagrangeCoefficients[j]));
		}
		return result;
	}

	public static ShamirSecretSharingScheme getInstance(ZModPrime zModPrime, int size, int threshold) {
		if (zModPrime == null || size < 1 || threshold < 1 || threshold > size
			   || BigInteger.valueOf(size).compareTo(zModPrime.getOrder()) >= 0) {
			throw new IllegalArgumentException();
		}
		return new ShamirSecretSharingScheme(zModPrime, size, threshold);
	}

}
