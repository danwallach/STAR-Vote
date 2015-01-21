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

import ch.bfh.unicrypt.helper.UniCrypt;
import ch.bfh.unicrypt.helper.MathUtil;
import java.math.BigInteger;
import java.util.Arrays;

public class Factorization
	   extends UniCrypt {

	private final BigInteger value;
	private final BigInteger[] primeFactors;
	private final int[] exponents;

	protected Factorization(BigInteger value, BigInteger[] primeFactors, int[] exponents) {
		this.value = value;
		this.primeFactors = primeFactors;
		this.exponents = exponents;
	}

	public BigInteger getValue() {
		return this.value;
	}

	public BigInteger[] getPrimeFactors() {
		return this.primeFactors;
	}

	public int[] getExponents() {
		return this.exponents;
	}

	@Override
	protected String defaultToStringValue() {
		return "" + this.getValue();
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 89 * hash + (this.value != null ? this.value.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Factorization other = (Factorization) obj;
		return this.value == other.value || (this.value != null && this.value.equals(other.value));
	}

	public static Factorization getInstance() {
		return Factorization.getInstance(new BigInteger[]{});
	}

	public static Factorization getInstance(BigInteger primeFactor) {
		return Factorization.getInstance(new BigInteger[]{primeFactor});
	}

	public static Factorization getInstance(BigInteger primeFactor, int exponent) {
		return Factorization.getInstance(new BigInteger[]{primeFactor}, new int[]{exponent});
	}

	public static Factorization getInstance(BigInteger... primeFactors) {
		if (primeFactors == null) {
			throw new IllegalArgumentException();
		}
		int[] exponents = new int[primeFactors.length];
		Arrays.fill(exponents, 1);
		return Factorization.getInstance(primeFactors, exponents);
	}

	public static Factorization getInstance(BigInteger[] primeFactors, int[] exponents) {
		if (primeFactors == null || exponents == null || primeFactors.length != exponents.length) {
			throw new IllegalArgumentException();
		}
		BigInteger value = BigInteger.ONE;
		for (int i = 0; i < primeFactors.length; i++) {
			if (primeFactors[i] == null || !MathUtil.isPrime(primeFactors[i]) || exponents[i] < 1) {
				throw new IllegalArgumentException();
			}
			value = value.multiply(primeFactors[i].pow(exponents[i]));
		}
		BigInteger[] newPrimeFactors = MathUtil.removeDuplicates(primeFactors);
		int newLength = newPrimeFactors.length;
		int[] newExponents = new int[newLength];
		for (int i = 0; i < newLength; i++) {
			for (int j = 0; j < primeFactors.length; j++) {
				if (newPrimeFactors[i].equals(primeFactors[j])) {
					newExponents[i] = newExponents[i] + exponents[j];
				}
			}
		}
		return new Factorization(value, newPrimeFactors, newExponents);
	}

}
