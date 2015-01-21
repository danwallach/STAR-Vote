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

import ch.bfh.unicrypt.helper.array.abstracts.AbstractImmutableArray;
import ch.bfh.unicrypt.helper.array.interfaces.ImmutableArray;
import java.util.Collection;

/**
 *
 * @author Rolf Haenni <rolf.haenni@bfh.ch>
 * @param <V>
 */
public class DenseArray<V extends Object>
	   extends AbstractImmutableArray<DenseArray<V>, V> {

	// The obects are stored either as an ordinary, possibly empty array (case 1)
	// or as an array of length 1 together with the full length of the immutable
	// array (case 2, all elements are equal)
	private final Object[] values;

	protected DenseArray(V value, int length) {
		this(new Object[]{value}, 0, length, false);
	}

	protected DenseArray(Object[] values) {
		this(values, 0, values.length, false);
	}

	protected DenseArray(Object[] values, int offset, int length, boolean reverse) {
		super(DenseArray.class, length, offset, reverse);
		this.values = values;
		if (values.length <= 1) {
			this.uniform = true;
		}
	}

	@Override
	protected String defaultToStringValue() {
		String str = "";
		String delimiter = "";
		for (int i : this.getAllIndices()) {
			str = str + delimiter + this.abstractGetAt(i);
			delimiter = ", ";
		}
		return "[" + str + "]";
	}

	public static <T> DenseArray<T> getInstance(T... array) {
		if (array == null) {
			throw new IllegalArgumentException();
		}
		Object[] newArray = new Object[array.length];
		int i = 0;
		for (T value : array) {
			if (value == null) {
				throw new IllegalArgumentException();
			}
			newArray[i++] = value;
		}
		return new DenseArray<T>(newArray);
	}

	public static <T> DenseArray<T> getInstance(Collection<T> collection) {
		if (collection == null) {
			throw new IllegalArgumentException();
		}
		Object[] array = new Object[collection.size()];
		int i = 0;
		for (T value : collection) {
			if (value == null) {
				throw new IllegalArgumentException();
			}
			array[i++] = value;
		}
		return new DenseArray<T>(array);
	}

	public static <T> DenseArray<T> getInstance(T value, int length) {
		if (value == null || length < 0) {
			throw new IllegalArgumentException();
		}
		return new DenseArray(new Object[]{value}, 0, length, false);
	}

	@Override
	protected V abstractGetAt(int index) {
		if (this.reverse) {
			index = this.length - index - 1;
		}
		return (V) this.values[(this.offset + index) % this.values.length];
	}

	@Override
	protected DenseArray<V> abstractExtract(int fromIndex, int length) {
		if (this.reverse) {
			fromIndex = this.length - fromIndex - length;
		}
		return new DenseArray<V>(this.values, this.offset + fromIndex, length, this.reverse);
	}

	@Override
	protected DenseArray<V> abstractAppend(ImmutableArray<V> other) {
		Object[] result = new Object[this.length + other.getLength()];
		for (int i : this.getAllIndices()) {
			result[i] = this.abstractGetAt(i);
		}
		for (int i : other.getAllIndices()) {
			result[this.length + i] = other.getAt(i);
		}
		return new DenseArray<V>(result);
	}

	@Override
	protected DenseArray<V> abstractInsertAt(int index, V newObject) {
		Object[] result = new Object[this.length + 1];
		for (int i : this.getAllIndices()) {
			if (i < index) {
				result[i] = this.abstractGetAt(i);
			} else {
				result[i + 1] = this.abstractGetAt(i);
			}
		}
		result[index] = newObject;
		return new DenseArray<V>(result);
	}

	@Override
	protected DenseArray<V> abstractReplaceAt(int index, V newObject) {
		Object[] result = new Object[this.length];
		for (int i : this.getAllIndices()) {
			result[i] = this.abstractGetAt(i);
		}
		result[index] = newObject;
		return new DenseArray<V>(result);
	}

	@Override
	protected DenseArray<V> abstractReverse() {
		return new DenseArray<V>(this.values, this.offset, this.length, !this.reverse);
	}

}
