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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.converter.classes.biginteger;

import ch.bfh.unicrypt.helper.Alphabet;
import ch.bfh.unicrypt.helper.converter.abstracts.AbstractBigIntegerConverter;
import java.math.BigInteger;

/**
 *
 * @author Rolf Haenni <rolf.haenni@bfh.ch>
 */
public class FiniteStringToBigInteger
	   extends AbstractBigIntegerConverter<String> {

	private final Alphabet alphabet;
	private final int minLength;
	private final int maxLength;

	protected FiniteStringToBigInteger(Alphabet alphabet, int minLength, int maxLength) {
		super(String.class);
		this.alphabet = alphabet;
		this.minLength = minLength;
		this.maxLength = maxLength;
	}

	@Override
	protected BigInteger abstractConvert(String value) {
		int length = value.length();
		BigInteger result = BigInteger.ZERO;
		BigInteger size = BigInteger.valueOf(this.alphabet.getSize());
		for (int i = 0; i < length; i++) {
			int charIndex = this.alphabet.getIndex(value.charAt(i));
			if (i < length - this.minLength) {
				charIndex++;
			}
			result = result.multiply(size).add(BigInteger.valueOf(charIndex));
		}
		return result;
	}

	@Override
	protected String abstractReconvert(BigInteger value) {
		BigInteger size = BigInteger.valueOf(this.alphabet.getSize());
		StringBuilder strBuilder = new StringBuilder(this.maxLength);
		while (!value.equals(BigInteger.ZERO) || strBuilder.length() < this.minLength) {
			if (strBuilder.length() >= this.minLength) {
				value = value.subtract(BigInteger.ONE);
			}
			strBuilder.append(this.alphabet.getCharacter(value.mod(size).intValue()));
			value = value.divide(size);
		}
		return strBuilder.reverse().toString();
	}

	public static FiniteStringToBigInteger getInstance(Alphabet alphabet, int minLength, int maxLength) {
		return new FiniteStringToBigInteger(alphabet, minLength, maxLength);
	}

}
