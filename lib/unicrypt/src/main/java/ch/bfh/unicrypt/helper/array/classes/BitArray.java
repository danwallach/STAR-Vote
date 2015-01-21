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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.array.classes;

import ch.bfh.unicrypt.helper.array.abstracts.AbstractDefaultValueArray;
import ch.bfh.unicrypt.helper.array.interfaces.ImmutableArray;
import ch.bfh.unicrypt.helper.converter.classes.string.BitArrayToString;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.hash.HashAlgorithm;
import ch.bfh.unicrypt.helper.MathUtil;
import ch.bfh.unicrypt.random.classes.HybridRandomByteSequence;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;

/**
 *
 * @author Rolf Haenni <rolf.haenni@bfh.ch>
 */
public class BitArray
	   extends AbstractDefaultValueArray<BitArray, Boolean> {

	ByteArray byteArray;

	private BitArray(ByteArray byteArray, int length) {
		this(byteArray, 0, length, 0, 0, false);
	}

	private BitArray(ByteArray byteArray, int offset, int length, int trailer, int header, boolean reverse) {
		super(BitArray.class, false, trailer, header, length, offset, reverse);
		this.byteArray = byteArray;
	}

	public int getByteLength() {
		return (this.length + Byte.SIZE - 1) / Byte.SIZE;
	}

	public boolean[] getBits() {
		boolean[] result = new boolean[this.length];
		for (int i : this.getAllIndices()) {
			result[i] = this.abstractGetAt(i);
		}
		return result;
	}

	// filled up with trailing zeros
	public byte[] getBytes() {
		int byteLength = (this.length + Byte.SIZE - 1) / Byte.SIZE;
		byte[] result = new byte[byteLength];
		for (int i = 0; i < byteLength; i++) {
			result[i] = this.getByteAt(i);
		}
		return result;
	}

	public boolean getBitAt(int index) {
		if (index < 0 || index >= this.length) {
			throw new IndexOutOfBoundsException();
		}
		return this.abstractGetAt(index);
	}

	public byte getByteAt(int byteIndex) {
		byte result = 0;
		int bitIndex = byteIndex * Byte.SIZE;
		int maxBitIndex = Math.min(this.length, bitIndex + Byte.SIZE);
		for (int i = 0; bitIndex < maxBitIndex; i++, bitIndex++) {
			if (this.abstractGetAt(bitIndex)) {
				result = MathUtil.setBit(result, i);
			}
		}
		return result;
	}

	public ByteArray getHashValue() {
		return this.getHashValue(HashAlgorithm.getInstance());
	}

	public ByteArray getHashValue(HashAlgorithm hashAlgorithm) {
		if (hashAlgorithm == null) {
			throw new IllegalArgumentException();
		}
		byte[] hash = hashAlgorithm.getHashValue(this.getBytes());
		return new ByteArray(hash);
	}

	public static BitArray getInstance() {
		return BitArray.getInstance(new boolean[0]);
	}

	public static BitArray getInstance(int length) {
		return BitArray.getInstance(false, length);
	}

	public static BitArray getInstance(boolean fillBit, int length) {
		if (length < 0) {
			throw new IllegalArgumentException();
		}
		int byteLength = (length + Byte.SIZE - 1) / Byte.SIZE;
		return new BitArray(ByteArray.getInstance(fillBit, byteLength), length);
	}

	public static BitArray getInstance(boolean... bits) {
		if (bits == null) {
			throw new IllegalArgumentException();
		}
		return BitArray.getInstance(bitsToBytes(bits), bits.length);
	}

	public static BitArray getInstance(byte... bytes) {
		return BitArray.getInstance(ByteArray.getInstance(bytes));
	}

	public static BitArray getInstance(byte[] bytes, int length) {
		return BitArray.getInstance(ByteArray.getInstance(bytes), length);
	}

	public static BitArray getInstance(ByteArray byteArray) {
		if (byteArray == null) {
			throw new IllegalArgumentException();
		}
		return new BitArray(byteArray, byteArray.getLength() * Byte.SIZE);
	}

	public static BitArray getInstance(ByteArray byteArray, int length) {
		if (byteArray == null || length > byteArray.getLength() * Byte.SIZE) {
			throw new IllegalArgumentException();
		}
		return new BitArray(byteArray, length);
	}

	// a string of '0's and '1's
	public static BitArray getInstance(String binaryString) {
		if (binaryString == null) {
			throw new IllegalArgumentException();
		}
		return BitArrayToString.getInstance().reconvert(binaryString);
	}

	public static BitArray getRandomInstance(int length) {
		return BitArray.getRandomInstance(length, HybridRandomByteSequence.getInstance());
	}

	public static BitArray getRandomInstance(int length, RandomByteSequence randomByteSequence) {
		if (length < 0 || randomByteSequence == null) {
			throw new IllegalArgumentException();
		}
		int byteLength = (length + Byte.SIZE - 1) / Byte.SIZE;
		return new BitArray(randomByteSequence.getNextByteArray(byteLength), length);
	}

	@Override
	protected String defaultToStringValue() {
		String str = BitArrayToString.getInstance().convert(this);
		return "\"" + str + "\"";
	}

	@Override
	protected Boolean abstractGetAt(int index) {
		return this.abstractGetBitAt(index);
	}

	private boolean abstractGetBitAt(int index) {
		if (this.reverse) {
			index = this.length - index - 1;
		}
		if (index < this.trailer || index >= this.length - this.header) {
			return false;
		}
		index = this.offset + index - this.trailer;
		int byteIndex = index / Byte.SIZE;
		byte mask = MathUtil.bitMask(index % Byte.SIZE);
		return MathUtil.logicalAND(this.byteArray.getByteAt(byteIndex), mask) != 0;
	}

	@Override
	protected BitArray abstractAppend(ImmutableArray<Boolean> other) {
		boolean[] result = new boolean[this.length + other.getLength()];
		for (int i : this.getAllIndices()) {
			result[i] = this.getBitAt(i);
		}
		for (int i : other.getAllIndices()) {
			result[this.length + i] = other.getAt(i);
		}
		return BitArray.getInstance(result);
	}

	@Override
	protected BitArray abstractInsertAt(int index, Boolean newBit) {
		boolean[] result = new boolean[this.length + 1];
		for (int i : this.getAllIndices()) {
			if (i < index) {
				result[i] = this.abstractGetBitAt(i);
			} else {
				result[i + 1] = this.abstractGetBitAt(i);
			}
		}
		result[index] = newBit;
		return BitArray.getInstance(result);
	}

	@Override
	protected BitArray abstractReplaceAt(int index, Boolean newBit) {
		boolean[] result = new boolean[this.length];
		for (int i : this.getAllIndices()) {
			result[i] = this.abstractGetBitAt(i);
		}
		result[index] = newBit;
		return BitArray.getInstance(result);
	}

	@Override
	protected BitArray abstractGetInstance(int offset, int length, int trailer, int header, boolean reverse) {
		return new BitArray(this.byteArray, offset, length, trailer, header, reverse);
	}

	private static byte[] bitsToBytes(boolean[] bits) {
		int byteLength = (bits.length + Byte.SIZE - 1) / Byte.SIZE;
		byte[] bytes = new byte[byteLength];
		for (int i = 0; i < bits.length; i++) {
			int byteIndex = i / Byte.SIZE;
			int bitIndex = i % Byte.SIZE;
			if (bits[i]) {
				bytes[byteIndex] = MathUtil.setBit(bytes[byteIndex], bitIndex);
			}
		}
		return bytes;
	}

}
