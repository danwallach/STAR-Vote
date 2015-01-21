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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.random.classes;

import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.UniCrypt;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.MathUtil;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
import java.math.BigInteger;

/**
 * @author R. Haenni
 * @author R. E. Koenig
 * @version 1.0
 */
public class RandomNumberGenerator
	   extends UniCrypt {

	private final RandomByteSequence randomByteSequence;

	private RandomNumberGenerator(RandomByteSequence randomByteSequence) {
		this.randomByteSequence = randomByteSequence;
	}

	/**
	 * Generates a random boolean value.
	 * <p>
	 * @return The random boolean value
	 */
	public final boolean nextBoolean() {
		return this.randomByteSequence.getNextByte() % 2 == 1;
	}

	/**
	 * Generates a random byte between -128 and 127 (inclusive).
	 * <p>
	 * @return The random byte value
	 */
	public final byte nextByte() {
		return this.randomByteSequence.getNextByte();
	}

	/**
	 * Generates a random byte[] of size length where each byte carries a value of -128 and 127 (inclusive).
	 * <p>
	 * @param length
	 * @return The random byte[] value
	 */
	public final byte[] nextBytes(int length) {
		if (length < 0) {
			throw new IllegalArgumentException();
		}
		return this.randomByteSequence.getNextByteArray(length).getBytes();
	}

	/**
	 * Generates a random integer beween 0 and Integer.MAX_VALUE.
	 * <p>
	 * @return The random int
	 */
	public final int nextInteger() {
		return this.nextInteger(Integer.MAX_VALUE);
	}

	/**
	 * Generates a random integer between 0 and {@literal maxValue} (inclusive).
	 * <p>
	 * @param maxValue The maximal value
	 * @return The random integer
	 * @throws IllegalArgumentException if {@literal maxValue < 0}
	 */
	public final int nextInteger(int maxValue) {
		if (maxValue < 0) {
			throw new IllegalArgumentException();
		}//This is a slow implementation.
		return this.nextBigInteger(BigInteger.valueOf(maxValue)).intValue();
	}

	/**
	 * Generates a random integer between {@literal minValue} (inclusive) and {@literal maxValue} (inclusive).
	 * <p>
	 * @param minValue The minimal value
	 * @param maxValue The maximal value
	 * @return The random integer
	 * @throws IllegalArgumentException if {@literal maxValue < minValue}
	 */
	public final int nextInteger(int minValue, int maxValue) {
		return this.nextInteger(maxValue - minValue) + minValue;
	}

	/**
	 * Generates a random BigInteger value of a certain bit length.
	 * <p>
	 * @param bitLength The given bit length
	 * @return The random BigInteger value
	 * @throws IllegalArgumentException if {@literal bitLength < 0}
	 */
	public final BigInteger nextBigInteger(int bitLength) {
		if (bitLength < 0) {
			throw new IllegalArgumentException();
		}
		if (bitLength == 0) {
			return BigInteger.ZERO;
		}
		return this.internalNextBigInteger(bitLength, true);
	}

	/**
	 * Generates a random BigInteger between 0 and {@literal maxValue} (inclusive).
	 * <p>
	 * @param maxValue The maximal value
	 * @return The random BigInteger value
	 * @throws IllegalArgumentException if {@literal maxValue} is null or if {@literal maxValue < 0}
	 */
	public final BigInteger nextBigInteger(BigInteger maxValue) {
		if (maxValue == null || maxValue.signum() < 0) {
			throw new IllegalArgumentException();
		}
		BigInteger randomValue;
		int bitLength = maxValue.bitLength();
		do {
			randomValue = this.internalNextBigInteger(bitLength, false);
		} while (randomValue.compareTo(maxValue) > 0);
		return randomValue;
	}

	/**
	 * Generates a random BigInteger value between {@literal minValue} (inclusive) and {@literal maxValue} (inclusive).
	 * <p>
	 * @param minValue The minimal value
	 * @param maxValue The maximal value
	 * @return The random BigInteger value
	 * @throws IllegalArgumentException if {@literal minValue} or {@literal maxValue} is null, or if
	 *                                  {@literal maxValue < minValue}
	 */
	public final BigInteger nextBigInteger(BigInteger minValue, BigInteger maxValue) {
		if (minValue == null || maxValue == null) {
			throw new IllegalArgumentException();
		}
		return this.nextBigInteger(maxValue.subtract(minValue)).add(minValue);
	}

	/**
	 * Generates a random BigInteger value of a certain bit length that is probably prime with high certainty.
	 * <p>
	 * @param bitLength The given bit length
	 * @return The random BigInteger prime number
	 * @throws IllegalArgumentException if {@literal bitLength < 2}
	 */
	public final BigInteger nextPrime(int bitLength) {
		if (bitLength < 2) {
			throw new IllegalArgumentException();
		}
		BigInteger prime;
		do {
			prime = this.internalNextBigInteger(bitLength, true);
		} while (!MathUtil.isPrime(prime));
		return prime;
	}

	/**
	 * Generates a random BigInteger value of a certain bit length that is a save prime with high certainty.
	 * <p>
	 * @param bitLength The given bit length
	 * @return The random BigInteger save prime
	 * @throws IllegalArgumentException if {@literal bitLength < 3}
	 * @see "Handbook of Applied Cryptography, Algorithm 4.86"
	 */
	public final BigInteger nextSavePrime(int bitLength) {
		BigInteger prime;
		BigInteger savePrime;
		do {
			prime = this.nextPrime(bitLength - 1);
			savePrime = prime.shiftLeft(1).add(BigInteger.ONE);
		} while (!MathUtil.isPrime(savePrime));
		return savePrime;
	}

	/**
	 * Generates a pair of distinct random BigInteger values of respective bit lengths such that both values are
	 * probably prime with high certainty and such that the second divides the first minus one.
	 * <p>
	 * @param bitLength1 The bit length of the first random prime
	 * @param bitLength2 The bit length of the second random prime
	 * @return A BigInteger array containing the two primes
	 * @throws IllegalArgumentException if {@literal bitLength1 <= bitLength2} or {@literal bitLengh2<2}
	 */
	public final BigInteger[] nextPrimePair(int bitLength1, int bitLength2) {
		if (bitLength1 <= bitLength2 || bitLength2 < 2) {
			throw new IllegalArgumentException();
		}
		BigInteger k;
		BigInteger prime1, prime2;
		BigInteger minValue, maxValue;
		do {
			prime2 = this.nextPrime(bitLength2);
			minValue = MathUtil.powerOfTwo(bitLength1 - 1);
			maxValue = MathUtil.powerOfTwo(bitLength1).subtract(BigInteger.ONE);
			k = this.nextBigInteger(minValue.divide(prime2).add(BigInteger.ONE), maxValue.divide(prime2));
			prime1 = prime2.multiply(k).add(BigInteger.ONE);
		} while (!MathUtil.isPrime(prime1));
		return new BigInteger[]{prime1, prime2};
	}

	private BigInteger internalNextBigInteger(int bitLength, boolean isMsbSet) {
		if (bitLength < 1) {
			return BigInteger.ZERO;
		}
		int amountOfBytes = (int) Math.ceil(bitLength / 8.0);
		byte[] bytes = this.nextBytes(amountOfBytes);

		int shift = 8 - (bitLength % 8);
		if (shift == 8) {
			shift = 0;
		}
		if (isMsbSet) {
			bytes[0] = (byte) (((bytes[0] & 0xFF) | 0x80) >> shift);
		} else {
			bytes[0] = (byte) ((bytes[0] & 0xFF) >> shift);

		}
		return new BigInteger(1, bytes);
	}

	public static RandomNumberGenerator getInstance(RandomByteSequence randomByteSequence) {
		if (randomByteSequence == null) {
			throw new IllegalArgumentException();
		}
		return new RandomNumberGenerator(randomByteSequence);
	}

}
