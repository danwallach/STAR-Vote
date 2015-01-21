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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Rolf Haenni <rolf.haenni@bfh.ch>
 * @param <V>
 */
public class SparseArray<V extends Object>
	   extends AbstractDefaultValueArray<SparseArray<V>, V> {

	private final Map<Integer, V> map;

	private SparseArray(V defaultValue, Map<Integer, V> map, int length) {
		this(defaultValue, map, 0, length, 0, 0, false);
	}

	private SparseArray(V defaultValue, Map<Integer, V> map, int offset, int length, int trailer, int header, boolean reverse) {
		super(SparseArray.class, defaultValue, trailer, header, length, offset, reverse);
		this.map = map;
		if (map.isEmpty()) {
			this.uniform = true;
		}
	}

	// this method is more efficient than its predecessor
	@Override
	protected Iterable<Integer> defaultGetIndices(V value) {
		if (this.defaultValue.equals(value)) {
			return super.defaultGetIndices(value);
		}
		List<Integer> result = new LinkedList<Integer>();
		for (Integer i : this.map.keySet()) {
			if (i >= this.offset && i < this.offset + this.length - this.header - this.trailer) {
				if (this.map.get(i).equals(value)) {
					result.add(this.getIndex(i));
				}
			}
		}
		return result;
	}

	// this method is more efficient than its predecessor
	@Override
	protected Iterable<Integer> defaultGetIndicesExcept(V value) {
		if (!this.defaultValue.equals(value)) {
			return super.defaultGetIndicesExcept(value);
		}
		List<Integer> result = new LinkedList<Integer>();
		for (Integer i : this.map.keySet()) {
			if (i >= this.offset && i < this.offset + this.length - this.header - this.trailer) {
				if (!this.map.get(i).equals(this.defaultValue)) {
					result.add(this.getIndex(i));
				}
			}
		}
		return result;
	}

	@Override
	protected String defaultToStringValue() {
		if (this.isEmpty()) {
			return "[]";
		}
		String str = "";
		String delimiter = "";
		for (int i : this.getIndicesExcept()) {
			str = str + delimiter + i + "->" + this.getAt(i);
			delimiter = ", ";
		}
		return "[" + this.getLength() + ": " + str + "]";
	}

	public static <T> SparseArray<T> getInstance(T defaultValue, int length) {
		return SparseArray.getInstance(defaultValue, new HashMap<Integer, T>(), length);
	}

	public static <T> SparseArray<T> getInstance(T defaultValue, Map<Integer, T> map) {
		if (map == null) {
			throw new IllegalArgumentException();
		}
		int maxIndex = 0;
		for (Integer i : map.keySet()) {
			maxIndex = Math.max(maxIndex, i);
		}
		return SparseArray.getInstance(defaultValue, map, maxIndex + 1);
	}

	public static <T> SparseArray<T> getInstance(T defaultValue, Map<Integer, T> map, int length) {
		if (defaultValue == null || map == null || length < 0) {
			throw new IllegalArgumentException();
		}
		Map<Integer, T> newMap = new HashMap();
		for (Integer i : map.keySet()) {
			T value = map.get(i);
			if (value == null || i >= length) {
				throw new IllegalArgumentException();
			}
			if (!value.equals(defaultValue)) {
				newMap.put(i, value);
			}
		}
		return new SparseArray<T>(defaultValue, newMap, length);
	}

	public static <T> SparseArray<T> getInstance(T defaultValue, T... values) {
		if (defaultValue == null || values == null) {
			throw new IllegalArgumentException();
		}
		Map<Integer, T> map = new HashMap<Integer, T>();
		int i = 0;
		for (T value : values) {
			if (value == null) {
				throw new IllegalArgumentException();
			}
			if (!value.equals(defaultValue)) {
				map.put(i, value);
			}
			i++;
		}
		return new SparseArray<T>(defaultValue, map, values.length);
	}

	@Override
	protected V abstractGetAt(int index) {
		if (this.reverse) {
			index = this.length - index - 1;
		}
		if (index < this.trailer || index >= this.length - this.header) {
			return this.defaultValue;
		}
// JAVA 8
// return this.map.getOrDefault(index - this.trailer + this.offset, this.defaultValue);
		V result = this.map.get(index - this.trailer + this.offset);
		if (result == null) {
			return this.defaultValue;
		}
		return result;
	}

	@Override
	protected SparseArray<V> abstractAppend(ImmutableArray<V> other) {
		Map<Integer, V> newMap = new HashMap<Integer, V>();
		for (int i : this.getIndicesExcept()) {
			newMap.put(i, this.abstractGetAt(i));
		}
		for (int i : other.getIndicesExcept(this.defaultValue)) {
			newMap.put(this.length + i, other.getAt(i));
		}
		return new SparseArray<V>(this.defaultValue, newMap, this.length + other.getLength());
	}

	@Override
	protected SparseArray<V> abstractInsertAt(int index, V newObject) {
		Map<Integer, V> newMap = new HashMap<Integer, V>();
		for (int i : this.getIndicesExcept()) {
			if (i < index) {
				newMap.put(i, this.abstractGetAt(i));
			} else {
				newMap.put(i + 1, this.abstractGetAt(i));
			}
		}
		if (!newObject.equals(this.defaultValue)) {
			newMap.put(index, newObject);
		}
		return new SparseArray<V>(this.defaultValue, newMap, this.length + 1);
	}

	@Override
	protected SparseArray<V> abstractReplaceAt(int index, V newObject) {
		Map<Integer, V> newMap = new HashMap<Integer, V>();
		for (int i : this.getIndicesExcept()) {
			if (i != index) {
				newMap.put(i, this.abstractGetAt(i));
			}
		}
		if (!newObject.equals(this.defaultValue)) {
			newMap.put(index, newObject);
		}
		return new SparseArray<V>(this.defaultValue, newMap, this.length);
	}

	@Override
	protected SparseArray<V> abstractGetInstance(int offset, int length, int trailer, int header, boolean reverse) {
		return new SparseArray<V>(this.defaultValue, this.map, offset, length, trailer, header, reverse);
	}

	private int getIndex(int i) {
		if (this.reverse) {
			return this.length - i - 1 + this.offset - this.trailer;
		}
		return i - this.offset + this.trailer;
	}

}
