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
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author Rolf Haenni <rolf.haenni@bfh.ch>
 */
public class LongArray
	   extends AbstractDefaultValueArray<LongArray, Long> {

	private static final long ALL_ZERO = 0x0000000000000000L;
	private static final long ALL_ONE = 0xFFFFFFFFFFFFFFFFL;

	protected final long[] longs;

	protected LongArray(long fillLong, int length) {
		this(new long[]{fillLong}, 0, length, 0, 0, false);
	}

	protected LongArray(long[] longs) {
		this(longs, 0, longs.length, 0, 0, false);
	}

	protected LongArray(long[] longs, int offset, int length, int trailer, int header, boolean reverse) {
		super(LongArray.class, LongArray.ALL_ZERO, trailer, header, length, offset, reverse);
		this.longs = longs;
		if (longs.length <= 1 && trailer == 0 && header == 0) {
			this.uniform = true;
		}
	}

	public long[] getLongs() {
		long[] result = new long[this.length];
		for (int i : this.getAllIndices()) {
			result[i] = this.abstractGetLongAt(i);
		}
		return result;
	}

	public long getLongAt(int index) {
		if (index < 0 || index >= this.length) {
			throw new IndexOutOfBoundsException();
		}
		return this.abstractGetLongAt(index);
	}

	public LongArray xor(LongArray... others) {
		return this.applyOperand(0, others, false, ALL_ZERO);
	}

	public LongArray and(LongArray... others) {
		return this.applyOperand(1, others, false, ALL_ZERO);
	}

	public LongArray or(LongArray... others) {
		return this.applyOperand(2, others, false, ALL_ZERO);
	}

	public LongArray xorFillZero(LongArray... others) {
		return this.applyOperand(0, others, true, ALL_ZERO);
	}

	public LongArray andFillZero(LongArray... others) {
		return this.applyOperand(1, others, true, ALL_ZERO);
	}

	public LongArray orFillZero(LongArray... others) {
		return this.applyOperand(2, others, true, ALL_ZERO);
	}

	public LongArray xorFillOne(LongArray... others) {
		return this.applyOperand(0, others, true, ALL_ONE);
	}

	public LongArray andFillOne(LongArray... others) {
		return this.applyOperand(1, others, true, ALL_ONE);
	}

	public LongArray orFillOne(LongArray... others) {
		return this.applyOperand(2, others, true, ALL_ONE);
	}

	private LongArray applyOperand(int operator, LongArray[] others, boolean maxLength, long fillLong) {
		if (others == null) {
			throw new IllegalArgumentException();
		}
		int newLength = this.length;
		for (LongArray other : others) {
			if (other == null) {
				throw new IllegalArgumentException();
			}
			newLength = maxLength ? Math.max(newLength, other.length) : Math.min(newLength, other.length);
		}
		long[] result = new long[newLength];
		for (int i = 0; i < newLength; i++) {
			result[i] = (i < this.length) ? this.abstractGetLongAt(i) : fillLong;
		}
		for (LongArray other : others) {
			for (int i = 0; i < newLength; i++) {
				long operand = (i < other.length) ? other.abstractGetLongAt(i) : fillLong;
				switch (operator) {
					case 0:
						result[i] = result[i] ^ operand; // XOR
						break;
					case 1:
						result[i] = result[i] & operand;
						break;
					case 2:
						result[i] = result[i] | operand;
						break;
					default:
						throw new UnsupportedOperationException();
				}
			}
		}
		return new LongArray(result);
	}

	public LongArray not() {
		long[] result = new long[this.length];
		for (int i : this.getAllIndices()) {
			result[i] = ~this.abstractGetLongAt(i);
		}
		return new LongArray(result);
	}

	public static LongArray getInstance() {
		return new LongArray(new long[0]);
	}

	public static LongArray getInstance(boolean fillbit, int length) {
		if (fillbit) {
			return LongArray.getInstance(ALL_ONE, length);
		} else {
			return LongArray.getInstance(ALL_ZERO, length);
		}
	}

	public static LongArray getInstance(long fillLong, int length) {
		if (length < 0) {
			throw new IllegalArgumentException();
		}
		return new LongArray(fillLong, length);
	}

	public static LongArray getInstance(long... longs) {
		if (longs == null) {
			throw new IllegalArgumentException();
		}
		return new LongArray(Arrays.copyOf(longs, longs.length));
	}

	public static LongArray getInstance(Collection<Long> collection) {
		if (collection == null) {
			throw new IllegalArgumentException();
		}
		long[] longs = new long[collection.size()];
		int i = 0;
		for (Long l : collection) {
			if (l == null) {
				throw new IllegalArgumentException();
			}
			longs[i] = l;
			i++;
		}
		return new LongArray(longs);
	}

	@Override
	protected Long abstractGetAt(int index) {
		return this.abstractGetLongAt(index);
	}

	private long abstractGetLongAt(int index) {
		if (this.reverse) {
			index = this.length - index - 1;
		}
		if (index < this.trailer || index >= this.length - this.header) {
			return this.defaultValue;
		}
		return this.longs[(this.offset + index - this.trailer) % this.longs.length];
	}

	@Override
	protected LongArray abstractAppend(ImmutableArray<Long> other) {
		long[] result = new long[this.length + other.getLength()];
		for (int i : this.getAllIndices()) {
			result[i] = this.abstractGetLongAt(i);
		}
		for (int i : other.getAllIndices()) {
			result[this.length + i] = other.getAt(i);
		}
		return new LongArray(result);
	}

	@Override
	protected LongArray abstractInsertAt(int index, Long newLong) {
		long[] result = new long[this.length + 1];
		for (int i : this.getAllIndices()) {
			if (i < index) {
				result[i] = this.abstractGetLongAt(i);
			} else {
				result[i + 1] = this.abstractGetLongAt(i);
			}
		}
		result[index] = newLong;
		return new LongArray(result);
	}

	@Override
	protected LongArray abstractReplaceAt(int index, Long newLong) {
		long[] result = new long[this.length];
		for (int i : this.getAllIndices()) {
			result[i] = this.abstractGetLongAt(i);
		}
		result[index] = newLong;
		return new LongArray(result);
	}

	@Override
	protected LongArray abstractGetInstance(int offset, int length, int trailer, int header, boolean reverse) {
		return new LongArray(this.longs, offset, length, trailer, header, reverse);
	}

	protected LongArray abstractGetInstance(int deltaOffset, int deltaTrailer, int deltaHeader) {
		return new LongArray(this.longs,
							 offset + deltaOffset,
							 this.length - deltaOffset + deltaTrailer + deltaHeader,
							 trailer + deltaTrailer, header + deltaHeader,
							 this.reverse);
	}

	// internal method used in subclasses (the remaining bits are filled up with 0)
	protected final LongArray extractBits(int offset, int n) {
		if (n == 0) {
			return LongArray.getInstance();
		}
		if (offset % Long.SIZE == 0 && n % Long.SIZE == 0) {
			return this.extract(offset / Long.SIZE, n / Long.SIZE);
		}
		int reverseOffset = Long.SIZE - offset;
		long[] result = new long[(n - 1) / Long.SIZE + 1];
		for (int i = 0; i < result.length; i++) {
			result[i] = (this.abstractGetLongAt(i) >>> offset);
			if (i < this.length - 1) {
				result[i] = result[i] | (this.abstractGetLongAt(i + 1) << reverseOffset);
			}
		}
		return LongArray.getInstance(result);
	}

	public static void main(String[] args) {
		LongArray l = LongArray.getInstance(0xFFFFFFFFFFFFFFFFL, 0x0L, 0xFFFFFFFFFFFFFFFFL);
		System.out.println(l);
	}

}
