///*
// * UniCrypt
// *
// *  UniCrypt(tm) : Cryptographical framework allowing the implementation of cryptographic protocols e.g. e-voting
// *  Copyright (C) 2014 Bern University of Applied Sciences (BFH), Research Institute for
// *  Security in the Information Society (RISIS), E-Voting Group (EVG)
// *  Quellgasse 21, CH-2501 Biel, Switzerland
// *
// *  Licensed under Dual License consisting of:
// *  1. GNU Affero General Public License (AGPL) v3
// *  and
// *  2. Commercial license
// *
// *
// *  1. This program is free software: you can redistribute it and/or modify
// *   it under the terms of the GNU Affero General Public License as published by
// *   the Free Software Foundation, either version 3 of the License, or
// *   (at your option) any later version.
// *
// *   This program is distributed in the hope that it will be useful,
// *   but WITHOUT ANY WARRANTY; without even the implied warranty of
// *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// *   GNU Affero General Public License for more details.
// *
// *   You should have received a copy of the GNU Affero General Public License
// *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
// *
// *
// *  2. Licensees holding valid commercial licenses for UniCrypt may use this file in
// *   accordance with the commercial license agreement provided with the
// *   Software or, alternatively, in accordance with the terms contained in
// *   a written agreement between you and Bern University of Applied Sciences (BFH), Research Institute for
// *   Security in the Information Society (RISIS), E-Voting Group (EVG)
// *   Quellgasse 21, CH-2501 Biel, Switzerland.
// *
// *
// *   For further information contact <e-mail: unicrypt@bfh.ch>
// *
// *
// * Redistributions of files must retain the above copyright notice.
// */
//package ch.bfh.unicrypt.helper.array.classes;
//
//import ch.bfh.unicrypt.helper.MathUtil;
//import ch.bfh.unicrypt.helper.array.abstracts.AbstractDefaultValueArray;
//import ch.bfh.unicrypt.helper.array.interfaces.ImmutableArray;
//import ch.bfh.unicrypt.helper.converter.classes.string.BitArrayToString;
//import ch.bfh.unicrypt.helper.hash.HashAlgorithm;
//import ch.bfh.unicrypt.random.classes.HybridRandomByteSequence;
//import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
//
///**
// *
// * @author Rolf Haenni <rolf.haenni@bfh.ch>
// */
//public class NewBitArray
//	   extends AbstractDefaultValueArray<NewBitArray, Boolean> {
//
//	LongArray longArray;
//
//	private NewBitArray(LongArray longArray, int length) {
//		this(longArray, 0, length, 0, 0, false);
//	}
//
//	private NewBitArray(LongArray longArray, int offset, int length, int trailer, int header, boolean reverse) {
//		super(NewBitArray.class, false, trailer, header, length, offset, reverse);
//		this.longArray = longArray;
//	}
//
//	public int getByteLength() {
//		return (this.length + Byte.SIZE - 1) / Byte.SIZE;
//	}
//
//	public boolean[] getBits() {
//		boolean[] result = new boolean[this.length];
//		for (int i : this.getAllIndices()) {
//			result[i] = this.abstractGetAt(i);
//		}
//		return result;
//	}
//
//	// filled up with trailing zeros
//	public LongArray getLongArray() {
//		if ((this.offset % Long.SIZE) == 0 && (this.trailer % Long.SIZE) == 0 && (this.header % Long.SIZE) == 0 && (this.length % Long.SIZE) == 0) {
//			LongArray result = this.longArray.removePrefix(offset / Long.SIZE);
//			return result.appendPrefixAndSuffix(this.trailer / Long.SIZE, this.header / Long.SIZE);
//		}
//
//		int length = (this.length + 1) / Long.SIZE;
//
//	}
//
//	protected long getLongAt(int bitIndex)
//
//	// filled up with trailing zeros
//	public byte[] getLongs() {
//		int byteLength = (this.length + Byte.SIZE - 1) / Byte.SIZE;
//		byte[] result = new byte[byteLength];
//		for (int i = 0; i < byteLength; i++) {
//			result[i] = this.getByteAt(i);
//		}
//		return result;
//	}
//
//	public boolean getBitAt(int index) {
//		if (index < 0 || index >= this.length) {
//			throw new IndexOutOfBoundsException();
//		}
//		return this.abstractGetAt(index);
//	}
//
//	public byte getByteAt(int byteIndex) {
//		byte result = 0;
//		int bitIndex = byteIndex * Byte.SIZE;
//		int maxBitIndex = Math.min(this.length, bitIndex + Byte.SIZE);
//		for (int i = 0; bitIndex < maxBitIndex; i++, bitIndex++) {
//			if (this.abstractGetAt(bitIndex)) {
//				result = MathUtil.setBit(result, i);
//			}
//		}
//		return result;
//	}
//
//	public LongArray getHashValue() {
//		return this.getHashValue(HashAlgorithm.getInstance());
//	}
//
//	public LongArray getHashValue(HashAlgorithm hashAlgorithm) {
//		if (hashAlgorithm == null) {
//			throw new IllegalArgumentException();
//		}
//		byte[] hash = hashAlgorithm.getHashValue(this.getBytes());
//		return new LongArray(hash);
//	}
//
//	public static NewBitArray getInstance() {
//		return NewBitArray.getInstance(new boolean[0]);
//	}
//
//	public static NewBitArray getInstance(int length) {
//		return NewBitArray.getInstance(false, length);
//	}
//
//	public static NewBitArray getInstance(boolean fillBit, int length) {
//		if (length < 0) {
//			throw new IllegalArgumentException();
//		}
//		int byteLength = (length + Byte.SIZE - 1) / Byte.SIZE;
//		return new NewBitArray(LongArray.getInstance(fillBit, byteLength), length);
//	}
//
//	public static NewBitArray getInstance(boolean... bits) {
//		if (bits == null) {
//			throw new IllegalArgumentException();
//		}
//		return NewBitArray.getInstance(bitsToBytes(bits), bits.length);
//	}
//
//	public static NewBitArray getInstance(byte... bytes) {
//		return NewBitArray.getInstance(LongArray.getInstance(bytes));
//	}
//
//	public static NewBitArray getInstance(byte[] bytes, int length) {
//		return NewBitArray.getInstance(LongArray.getInstance(bytes), length);
//	}
//
//	public static NewBitArray getInstance(LongArray longArray) {
//		if (longArray == null) {
//			throw new IllegalArgumentException();
//		}
//		return new NewBitArray(longArray, longArray.getLength() * Byte.SIZE);
//	}
//
//	public static NewBitArray getInstance(LongArray longArray, int length) {
//		if (longArray == null || length > longArray.getLength() * Byte.SIZE) {
//			throw new IllegalArgumentException();
//		}
//		return new NewBitArray(longArray, length);
//	}
//
//	// a string of '0's and '1's
//	public static NewBitArray getInstance(String binaryString) {
//		if (binaryString == null) {
//			throw new IllegalArgumentException();
//		}
//		return BitArrayToString.getInstance().reconvert(binaryString);
//	}
//
//	public static NewBitArray getRandomInstance(int length) {
//		return NewBitArray.getRandomInstance(length, HybridRandomByteSequence.getInstance());
//	}
//
//	public static NewBitArray getRandomInstance(int length, RandomByteSequence randomByteSequence) {
//		if (length < 0 || randomByteSequence == null) {
//			throw new IllegalArgumentException();
//		}
//		int byteLength = (length + Byte.SIZE - 1) / Byte.SIZE;
//		return new NewBitArray(randomByteSequence.getNextLongArray(byteLength), length);
//	}
//
////	@Override
////	protected String defaultToStringValue() {
////		String str = BitArrayToString.getInstance().convert(this);
////		return "\"" + str + "\"";
////	}
//	@Override
//	protected Boolean abstractGetAt(int index) {
//		return this.abstractGetBitAt(index);
//	}
//
//	private boolean abstractGetBitAt(int index) {
//		if (this.reverse) {
//			index = this.length - index - 1;
//		}
//		if (index < this.trailer || index >= this.length - this.header) {
//			return false;
//		}
//		index = this.offset + index - this.trailer;
//		int longIndex = index / Long.SIZE;
//		long mask = 1 << (index % Long.SIZE);
//		return (this.longArray.getLongAt(longIndex) & mask) != 0;
//	}
//
//	@Override
//	protected NewBitArray abstractAppend(ImmutableArray<Boolean> other) {
//		boolean[] result = new boolean[this.length + other.getLength()];
//		for (int i : this.getAllIndices()) {
//			result[i] = this.getBitAt(i);
//		}
//		for (int i : other.getAllIndices()) {
//			result[this.length + i] = other.getAt(i);
//		}
//		return NewBitArray.getInstance(result);
//	}
//
//	@Override
//	protected NewBitArray abstractInsertAt(int index, Boolean newBit) {
//		boolean[] result = new boolean[this.length + 1];
//		for (int i : this.getAllIndices()) {
//			if (i < index) {
//				result[i] = this.abstractGetBitAt(i);
//			} else {
//				result[i + 1] = this.abstractGetBitAt(i);
//			}
//		}
//		result[index] = newBit;
//		return NewBitArray.getInstance(result);
//	}
//
//	@Override
//	protected NewBitArray abstractReplaceAt(int index, Boolean newBit) {
//		boolean[] result = new boolean[this.length];
//		for (int i : this.getAllIndices()) {
//			result[i] = this.abstractGetBitAt(i);
//		}
//		result[index] = newBit;
//		return NewBitArray.getInstance(result);
//	}
//
//	@Override
//	protected NewBitArray abstractGetInstance(int offset, int length, int trailer, int header, boolean reverse) {
//		// make sure the offset is smaller than Long.SIZE
////		return new NewBitArray(this.longArray.removePrefix(offset / Long.SIZE), offset % Long.SIZE, length, trailer, header, reverse);
//		return new NewBitArray(this.longArray, offset, length, trailer, header, reverse);
//	}
//
//}
