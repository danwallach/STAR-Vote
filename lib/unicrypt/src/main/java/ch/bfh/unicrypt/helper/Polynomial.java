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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper;

import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.array.classes.ByteArray;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.array.classes.DenseArray;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 *
 * @author philipp
 * @param <C>
 */
public class Polynomial<C>
	   extends UniCrypt {

	public static final int ZERO_POLYNOMIAL_DEGREE = -1;

	/** Polynomial's degree. */
	private final int degree;
	/** Holds the coefficients. Might be null if the polynomial is binary. */
	private final Map<Integer, C> coefficients;
	/** Holds the coefficients of binary polynomials. Is null if the polynomial is not binary. */
	private final ByteArray binaryCoefficients;

	/** Polynomial's zero coefficient. */
	private final C zeroCoefficient;
	/** Polynomial's one coefficient. */
	private final C oneCoefficient;

	/** Holds the indices of the non zero coefficients. */
	private DenseArray<Integer> indices;

	private Polynomial(Map<Integer, C> coefficients, C zeroCoefficient, C oneCoefficient) {
		this.coefficients = coefficients;
		this.zeroCoefficient = zeroCoefficient;
		this.oneCoefficient = oneCoefficient;

		int maxIndex = 0;
		boolean isBinary = true;
		for (Integer index : coefficients.keySet()) {
			maxIndex = Math.max(maxIndex, index);
			isBinary = this.oneCoefficient.equals(coefficients.get(index)) && isBinary;
		}
		if (maxIndex == 0) {
			C c = coefficients.get(0);
			this.degree = c == null || zeroCoefficient.equals(c) ? ZERO_POLYNOMIAL_DEGREE : maxIndex;
		} else {
			this.degree = maxIndex;
		}

		if (isBinary) {
			if (this.degree == ZERO_POLYNOMIAL_DEGREE) {
				this.binaryCoefficients = ByteArray.getInstance();
			} else {
				byte[] bytes = new byte[(int) Math.ceil((this.degree + 1) / 8.0)];
				Arrays.fill(bytes, (byte) 0x00);
				for (Integer index : coefficients.keySet()) {
					int byteIndex = index / Byte.SIZE;
					int bitIndex = index % Byte.SIZE;
					bytes[byteIndex] = (byte) (bytes[byteIndex] | (0x01 << bitIndex));
				}
				this.binaryCoefficients = ByteArray.getInstance(bytes);
			}
		} else {
			this.binaryCoefficients = null;
		}
	}

	private Polynomial(ByteArray coefficients, C zeroCoefficient, C oneCoefficient) {
		this.coefficients = null;
		this.binaryCoefficients = coefficients;
		this.zeroCoefficient = zeroCoefficient;
		this.oneCoefficient = oneCoefficient;

		if (coefficients.getLength() == 0) {
			this.degree = ZERO_POLYNOMIAL_DEGREE;
		} else {
			int byteIndex = 0;
			for (int i = 0; i < this.binaryCoefficients.getLength(); i++) {
				if (this.binaryCoefficients.getByteAt(i) != 0) {
					byteIndex = i;
				}
			}
			byte b = coefficients.getByteAt(byteIndex);
			int bitIndex = Integer.SIZE - Integer.numberOfLeadingZeros(b & 0xff);
			int d = byteIndex * Byte.SIZE + bitIndex - 1;
			this.degree = d < 0 ? ZERO_POLYNOMIAL_DEGREE : d;
		}
	}

	public int getDegree() {
		return this.degree;
	}

	public boolean isBinary() {
		return this.binaryCoefficients != null;
	}

	public boolean isZeroPolynomial() {
		return this.degree == ZERO_POLYNOMIAL_DEGREE;
	}

	public boolean isMonic() {
		return !this.isZeroPolynomial() && this.oneCoefficient.equals(this.getCoefficient(this.degree));
	}

	public C getCoefficient(int index) {
		if (index < 0) {
			throw new IllegalArgumentException();
		}
		if (this.isBinary()) {
			if (index < this.binaryCoefficients.getBitLength() && this.binaryCoefficients.getBitAt(index)) {
				return this.oneCoefficient;
			}
		} else {
			C coefficient = this.coefficients.get(index);
			if (coefficient != null) {
				return coefficient;
			}
		}
		return this.zeroCoefficient;
	}

	public ByteArray getCoefficients() {
		if (!this.isBinary()) {
			throw new UnsupportedOperationException();
		}
		return this.binaryCoefficients;
	}

	public final DenseArray<Integer> getIndices() {
		if (this.indices == null) {
			if (this.isBinary()) {
				ArrayList<Integer> ind = new ArrayList();
				for (int i = 0; i < this.binaryCoefficients.getBitLength(); i++) {
					if (this.binaryCoefficients.getBitAt(i)) {
						ind.add(i);
					}
				}
				this.indices = DenseArray.getInstance(ind);
			} else {
				TreeSet ind = new TreeSet(this.coefficients.keySet());
				this.indices = DenseArray.getInstance(ind);
			}
		}
		return this.indices;
	}

	public final Polynomial<C> getTerm(int index) {
		if (index < 0) {
			throw new IllegalArgumentException();
		}
		HashMap map = new HashMap(1);
		map.put(index, this.getCoefficient(index));
		return new Polynomial(map, this.zeroCoefficient, this.oneCoefficient);
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 79 * hash + (this.coefficients != null ? this.coefficients.hashCode() : this.binaryCoefficients.hashCode());
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		final Polynomial<C> other = (Polynomial<C>) obj;

		if (this.getDegree() == ZERO_POLYNOMIAL_DEGREE && other.getDegree() == ZERO_POLYNOMIAL_DEGREE) {
			return true;
		}

		if (this.isBinary()) {
			return this.binaryCoefficients == other.binaryCoefficients || this.binaryCoefficients.equals(other.binaryCoefficients);
		} else {
			return this.coefficients == other.coefficients || this.coefficients.equals(other.coefficients);
		}
	}

	@Override
	protected String defaultToStringValue() {
		String result = "f(x)=";

		String separator = "";
		if (this.getIndices().getLength() == 0) {
			result += this.coefficientToString(this.zeroCoefficient);
		}
		for (Integer index : this.getIndices()) {
			C coefficient = this.getCoefficient(index);
			if (coefficient != this.zeroCoefficient || this.getDegree() == 0) {
				result += separator;
				if (coefficient != this.oneCoefficient || index == 0) {
					result += this.coefficientToString(coefficient);
				}
				if (index > 0) {
					result += index == 1 ? "X" : "X^" + index;
				}
				separator = "+";
			}
		}

		return result;
	}

	private String coefficientToString(C coefficient) {
		if (coefficient instanceof Boolean) {
			return coefficient == this.zeroCoefficient ? "0" : "1";
		}
		return coefficient.toString();
	}

	public static <C> Polynomial<C> getInstance(Map<Integer, C> coefficients, C zeroCoefficient, C oneCoefficient) {
		if (coefficients == null || zeroCoefficient == null || oneCoefficient == null) {
			throw new IllegalArgumentException();
		}
		Map<Integer, C> result = new HashMap<Integer, C>();
		for (Integer i : coefficients.keySet()) {
			C coeff = coefficients.get(i);
			if (coeff == null) {
				throw new IllegalArgumentException();
			}
			if (!coeff.equals(zeroCoefficient)) {
				result.put(i, coeff);
			}
		}
		return new Polynomial<C>(result, zeroCoefficient, oneCoefficient);
	}

	public static <C> Polynomial<C> getInstance(C[] coefficients, C zeroCoefficient, C oneCoefficient) {
		if (coefficients == null || zeroCoefficient == null || oneCoefficient == null) {
			throw new IllegalArgumentException();
		}

		Map<Integer, C> result = new HashMap<Integer, C>();
		for (int i = 0; i < coefficients.length; i++) {
			C coeff = coefficients[i];
			if (coeff == null) {
				throw new IllegalArgumentException();
			}
			if (!coeff.equals(zeroCoefficient)) {
				result.put(i, coeff);
			}
		}
		return new Polynomial<C>(result, zeroCoefficient, oneCoefficient);
	}

	public static <C> Polynomial<C> getInstance(ByteArray coefficients, C zeroCoefficient, C oneCoefficient) {
		if (coefficients == null || zeroCoefficient == null || oneCoefficient == null) {
			throw new IllegalArgumentException();
		}
		return new Polynomial<C>(coefficients.removeSuffix(), zeroCoefficient, oneCoefficient);
	}

}
