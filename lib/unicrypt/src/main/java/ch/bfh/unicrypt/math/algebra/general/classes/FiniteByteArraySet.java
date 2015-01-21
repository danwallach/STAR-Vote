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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes;

import ch.bfh.unicrypt.helper.array.classes.ByteArray;
import ch.bfh.unicrypt.helper.converter.classes.biginteger.FiniteByteArrayToBigInteger;
import ch.bfh.unicrypt.helper.converter.classes.bytearray.ByteArrayToByteArray;
import ch.bfh.unicrypt.helper.converter.interfaces.BigIntegerConverter;
import ch.bfh.unicrypt.helper.converter.interfaces.ByteArrayConverter;
import ch.bfh.unicrypt.helper.MathUtil;
import ch.bfh.unicrypt.math.algebra.general.abstracts.AbstractSet;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
import java.math.BigInteger;

/**
 *
 * @author rolfhaenni
 */
public class FiniteByteArraySet
	   extends AbstractSet<FiniteByteArrayElement, ByteArray> {

	private final int minLength;
	private final int maxLength;

	protected FiniteByteArraySet(int minLength, int maxLength) {
		super(ByteArray.class);
		this.minLength = minLength;
		this.maxLength = maxLength;
	}

	public int getMinLength() {
		return this.minLength;
	}

	public int getMaxLength() {
		return this.maxLength;
	}

	public boolean fixedLength() {
		return this.minLength == maxLength;
	}

	public final FiniteByteArrayElement getElement(byte[] bytes) {
		return this.getElement(ByteArray.getInstance(bytes));
	}

	// for strings of the form "00|95|2B|9B|E2|FD|30|89"
	public final FiniteByteArrayElement getElement(String string) {
		return this.getElement(ByteArray.getInstance(string));
	}

	@Override
	protected ByteArrayConverter<ByteArray> defaultGetByteArrayConverter() {
		return ByteArrayToByteArray.getInstance();
	}

	@Override
	protected boolean abstractContains(ByteArray value) {
		return value.getLength() >= this.minLength && value.getLength() <= this.getMaxLength();
	}

	@Override
	protected FiniteByteArrayElement abstractGetElement(ByteArray value) {
		return new FiniteByteArrayElement(this, value);
	}

	@Override
	protected BigIntegerConverter<ByteArray> abstractGetBigIntegerConverter() {
		return FiniteByteArrayToBigInteger.getInstance(this.minLength, this.maxLength);
	}

	@Override
	protected BigInteger abstractGetOrder() {
		BigInteger size = MathUtil.powerOfTwo(Byte.SIZE);
		BigInteger order = BigInteger.ONE;
		for (int i = 0; i < this.maxLength - this.minLength; i++) {
			order = order.multiply(size).add(BigInteger.ONE);
		}
		return order.multiply(size.pow(this.minLength));
	}

	@Override
	protected FiniteByteArrayElement abstractGetRandomElement(RandomByteSequence randomByteSequence) {
		// this seems to be unnecessarly complicated, but is needed to generate shorter byte arrays with equal probability
		return this.getElementFrom(randomByteSequence.getRandomNumberGenerator().nextBigInteger(this.getOrder().subtract(BigInteger.ONE)));
	}

	@Override
	public boolean abstractEquals(final Set set) {
		final FiniteByteArraySet other = (FiniteByteArraySet) set;
		return this.minLength == other.minLength && this.maxLength == other.maxLength;
	}

	@Override
	protected int abstractHashCode() {
		int hash = 7;
		hash = 47 * hash + this.minLength;
		hash = 47 * hash + this.maxLength;
		return hash;
	}

	//
	// STATIC FACTORY METHODS
	//
	public static FiniteByteArraySet getInstance(final int maxLength) {
		return FiniteByteArraySet.getInstance(0, maxLength);
	}

	public static FiniteByteArraySet getInstance(final int minLength, final int maxLength) {
		if (minLength < 0 || maxLength < minLength) {
			throw new IllegalArgumentException();
		}
		if (minLength == maxLength) {
			return FixedByteArraySet.getInstance(minLength);
		}
		return new FiniteByteArraySet(minLength, maxLength);
	}

	public static FiniteByteArraySet getInstance(final BigInteger minOrder) {
		return FiniteByteArraySet.getInstance(minOrder, 0);
	}

	public static FiniteByteArraySet getInstance(final BigInteger minOrder, int minLength) {
		if (minOrder == null || minOrder.signum() < 0 || minLength < 0) {
			throw new IllegalArgumentException();
		}
		int maxLength = minLength;
		BigInteger size = MathUtil.powerOfTwo(Byte.SIZE);
		BigInteger order1 = size.pow(minLength);
		BigInteger order2 = BigInteger.ONE;
		while (order1.multiply(order2).compareTo(minOrder) < 0) {
			order2 = order2.multiply(size).add(BigInteger.ONE);
			maxLength++;
		}
		if (minLength == maxLength) {
			return FixedByteArraySet.getInstance(minLength);
		}
		return new FiniteByteArraySet(minLength, maxLength);
	}

}
