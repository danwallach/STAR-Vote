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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.array.abstracts;

import ch.bfh.unicrypt.helper.array.interfaces.DefaultValueArray;

/**
 *
 * @author Rolf Haenni <rolf.haenni@bfh.ch>
 * @param <A>
 * @param <V>
 */
abstract public class AbstractDefaultValueArray<A extends AbstractDefaultValueArray<A, V>, V extends Object>
	   extends AbstractImmutableArray<A, V>
	   implements DefaultValueArray<V> {

	protected final V defaultValue;
	protected int trailer; // number of trailing zeros not included in byteArray
	protected int header; // number of leading zeros not included in byteArray

	protected AbstractDefaultValueArray(Class valueClass, V defaultValue, int trailer, int header, int length, int offset, boolean reverse) {
		super(valueClass, length, offset, reverse);
		this.defaultValue = defaultValue;
		this.trailer = trailer;
		this.header = header;
		if (header + trailer == length) {
			this.uniform = true;
		}
	}

	@Override
	public final V getDefault() {
		return this.defaultValue;
	}

	@Override
	public final Iterable<Integer> getIndices() {
		return this.getIndices(this.defaultValue);
	}

	@Override
	public final Iterable<Integer> getIndicesExcept() {
		return this.getIndicesExcept(this.defaultValue);
	}

	@Override
	public final int count() {
		return this.count(this.defaultValue);
	}

	@Override
	public final int countPrefix() {
		return this.countPrefix(this.defaultValue);
	}

	@Override
	public final int countSuffix() {
		return this.countSuffix(this.defaultValue);
	}

	@Override
	public final A insertAt(int index) {
		return this.insertAt(index, this.defaultValue);
	}

	@Override
	public final A replaceAt(int index) {
		return this.replaceAt(index, this.defaultValue);
	}

	@Override
	public final A add() {
		return this.add(this.defaultValue);
	}

	@Override
	public final A appendPrefix(int n) {
		if (n < 0) {
			throw new IllegalArgumentException();
		}
		if (this.reverse) {
			return this.abstractGetInstance(this.offset, this.length + n, this.trailer, this.header + n, this.reverse);
		} else {
			return this.abstractGetInstance(this.offset, this.length + n, this.trailer + n, this.header, this.reverse);
		}
	}

	@Override
	public final A appendSuffix(int n) {
		if (n < 0) {
			throw new IllegalArgumentException();
		}
		if (this.reverse) {
			return this.abstractGetInstance(this.offset, this.length + n, this.trailer + n, this.header, this.reverse);
		} else {
			return this.abstractGetInstance(this.offset, this.length + n, this.trailer, this.header + n, this.reverse);
		}
	}

	@Override
	public final A appendPrefixAndSuffix(int n, int m) {
		if (n < 0 || m < 0) {
			throw new IllegalArgumentException();
		}
		if (this.reverse) {
			return this.abstractGetInstance(this.offset, this.length + n + m, this.trailer + m, this.header + n, this.reverse);
		} else {
			return this.abstractGetInstance(this.offset, this.length + n + m, this.trailer + n, this.header + m, this.reverse);
		}
	}

	@Override
	public final A removePrefix() {
		return this.removePrefix(this.countPrefix());
	}

	@Override
	public final A removeSuffix() {
		return this.removeSuffix(this.countSuffix());
	}

	@Override
	public final A shiftLeft(int n) {
		if (n < 0) {
			return this.shiftRight(-n);
		}
		return this.removePrefix(Math.min(this.getLength(), n));
	}

	@Override
	public final A shiftRight(int n) {
		if (n <= 0) {
			return this.shiftLeft(-n);
		}
		return this.appendPrefix(n);
	}

	@Override
	protected A abstractExtract(int fromIndex, int length) {
		if (this.reverse) {
			fromIndex = this.length - fromIndex - length;
		}
		int newTrailer = Math.min(Math.max(0, this.trailer - fromIndex), length);
		int newHeader = Math.min(Math.max(0, this.header - (this.length - fromIndex - length)), length);
		int newOffset = this.offset + Math.max(0, fromIndex - this.trailer);
		return this.abstractGetInstance(newOffset, length, newTrailer, newHeader, this.reverse);
	}

	@Override
	protected A abstractReverse() {
		return this.abstractGetInstance(this.offset, this.length, this.trailer, this.header, !this.reverse);
	}

	protected abstract A abstractGetInstance(int offset, int length, int trailer, int header, boolean reverse);

}
