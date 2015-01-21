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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.factorization;

import java.math.BigInteger;

/**
 * This is the general static factory method for this class. Returns a new instance for the general case n=p^k or
 * n=2p^k, where p is prime and k>=1 (for p>2) or k=1 (for p=2).
 * <p>
 * @throws IllegalArgumentException if {@code prime} is null or not prime
 * @throws IllegalArgumentException if {@code exponent<1}
 * @
 * throws IllegalArgumentException if {@code prime=2} and {@code exponent>1}
 */
public class SpecialFactorization
	   extends Factorization {

	protected SpecialFactorization(BigInteger value, BigInteger[] primeFactors, int[] exponents) {
		super(value, primeFactors, exponents);
	}

	public static SpecialFactorization getInstance(BigInteger primeFactor) {
		return SpecialFactorization.getInstance(primeFactor, 1, false);
	}

	public static SpecialFactorization getInstance(BigInteger primeFactor, int exponent) {
		return SpecialFactorization.getInstance(primeFactor, exponent, false);
	}

	public static SpecialFactorization getInstance(BigInteger primeFactor, boolean doubling) {
		return SpecialFactorization.getInstance(primeFactor, 1, doubling);
	}

	public static SpecialFactorization getInstance(BigInteger primeFactor, int exponent, boolean doubling) {
		BigInteger[] primeFactors;
		int[] exponents;
		BigInteger value;
		if (doubling) {
			value = primeFactor.pow(exponent).multiply(BigInteger.valueOf(2));
			primeFactors = new BigInteger[]{primeFactor, BigInteger.valueOf(2)};
			exponents = new int[]{exponent, 1};
		} else {
			value = primeFactor.pow(exponent);
			primeFactors = new BigInteger[]{primeFactor};
			exponents = new int[]{exponent};
		}
		return new SpecialFactorization(value, primeFactors, exponents);
	}

}
