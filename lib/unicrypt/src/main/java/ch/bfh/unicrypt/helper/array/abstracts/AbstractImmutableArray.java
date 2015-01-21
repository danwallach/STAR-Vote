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

import ch.bfh.unicrypt.helper.UniCrypt;
import ch.bfh.unicrypt.helper.array.interfaces.ImmutableArray;
import ch.bfh.unicrypt.helper.iterable.IterableRange;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Rolf Haenni <rolf.haenni@bfh.ch>
 * @param <A>
 * @param <V>
 */
abstract public class AbstractImmutableArray<A extends AbstractImmutableArray<A, V>, V extends Object>
	   extends UniCrypt
	   implements ImmutableArray<V> {

	protected Class valueClass;
	protected int length;
	protected int offset;
	protected boolean reverse;
	protected Boolean uniform = null;

	protected AbstractImmutableArray(Class valueClass, int length, int offset, boolean reverse) {
		this.valueClass = valueClass;
		this.length = length;
		this.offset = offset;
		this.reverse = reverse;
		if (length <= 1) {
			this.uniform = true;
		}
	}

	@Override
	public final int getLength() {
		return this.length;
	}

	@Override
	public final boolean isEmpty() {
		return this.length == 0;
	}

	@Override
	public final boolean isUniform() {
		if (this.uniform == null) {
			this.uniform = true;
			if (this.length > 1) {
				V first = this.abstractGetAt(0);
				for (int i = 1; i < this.length; i++) {
					if (!first.equals(this.abstractGetAt(i))) {
						this.uniform = false;
						break;
					}
				}
			}
		}
		return this.uniform;
	}

	@Override
	public final Iterable<Integer> getAllIndices() {
		return IterableRange.getInstance(0, this.length - 1);
	}

	@Override
	public final Iterable<Integer> getIndices(V value) {
		if (value == null) {
			throw new IllegalArgumentException();
		}
		return defaultGetIndices(value);
	}

	protected Iterable<Integer> defaultGetIndices(V value) {
		List<Integer> result = new LinkedList<Integer>();
		for (int i : this.getAllIndices()) {
			if (this.abstractGetAt(i).equals(value)) {
				result.add(i);
			}
		}
		return result;
	}

	@Override
	public final Iterable<Integer> getIndicesExcept(V value) {
		if (value == null) {
			throw new IllegalArgumentException();
		}
		return defaultGetIndicesExcept(value);
	}

	protected Iterable<Integer> defaultGetIndicesExcept(V value) {
		List<Integer> result = new LinkedList<Integer>();
		for (int i : this.getAllIndices()) {
			if (!this.abstractGetAt(i).equals(value)) {
				result.add(i);
			}
		}
		return result;
	}

	@Override
	public final int count(V value) {
		if (value == null) {
			throw new IllegalArgumentException();
		}
		int result = 0;
		for (int i : this.getAllIndices()) {
			if (this.abstractGetAt(i).equals(value)) {
				result++;
			}
		}
		return result;
	}

	@Override
	public final int countPrefix(V value) {
		if (value == null) {
			throw new IllegalArgumentException();
		}
		int result = 0;
		for (int i : this.getAllIndices()) {
			if (this.abstractGetAt(i).equals(value)) {
				result++;
			} else {
				break;
			}
		}
		return result;
	}

	@Override
	public final int countSuffix(V value) {
		if (value == null) {
			throw new IllegalArgumentException();
		}
		int result = 0;
		for (int i = this.length - 1; i >= 0; i--) {
			if (this.abstractGetAt(i).equals(value)) {
				result++;
			} else {
				break;
			}
		}
		return result;
	}

	@Override
	public final V getAt(int index) {
		if (index < 0 || index >= this.length) {
			throw new IndexOutOfBoundsException();
		}
		return this.abstractGetAt(index);
	}

	@Override
	public final V getFirst() {
		return this.getAt(0);
	}

	@Override
	public final V getLast() {
		return this.getAt(this.length - 1);
	}

	@Override
	public final A extract(int fromIndex, int length) {
		if (fromIndex < 0 || length < 0 || fromIndex + length > this.length) {
			throw new IllegalArgumentException();
		}
		if (fromIndex == 0 && length == this.length) {
			return (A) this;
		}
		return abstractExtract(fromIndex, length);
	}

	@Override
	public final A extractPrefix(int length) {
		return this.extract(0, length);
	}

	@Override
	public final A extractSuffix(int length) {
		return this.extract(this.length - length, length);
	}

	@Override
	public final A extractRange(int fromIndex, int toIndex) {
		return this.extract(fromIndex, toIndex - fromIndex + 1);
	}

	@Override
	public final A remove(int fromIndex, int length) {
		if (fromIndex < 0 || length < 0 || fromIndex + length > this.length) {
			throw new IllegalArgumentException();
		}
		if (length == 0) {
			return (A) this;
		}
		A prefix = this.extractPrefix(fromIndex);
		A suffix = this.extractSuffix(this.length - fromIndex - length);
		if (prefix.getLength() == 0) {
			return suffix;
		}
		if (suffix.getLength() == 0) {
			return prefix;
		}
		return prefix.append(suffix);
	}

	// prefix here means the lowest indices
	@Override
	public final A removePrefix(int length) {
		return this.remove(0, length);
	}

	// trailing here means the highest indices
	@Override
	public final A removeSuffix(int length) {
		return this.remove(this.length - length, length);
	}

	@Override
	public final A removeRange(int fromIndex, int toIndex) {
		return this.remove(fromIndex, toIndex - fromIndex + 1);
	}

	@Override
	public final A removeAt(int index) {
		return this.removeRange(index, index);
	}

	@Override
	public final A append(ImmutableArray<V> other) {
		if (other == null) {
			throw new IllegalArgumentException();
		}
		if (other.getLength() == 0) {
			return (A) this;
		}
		return this.abstractAppend(other);
	}

	@Override
	public final A add(V value) {
		return this.insertAt(this.length, value);
	}

	@Override
	public final A insertAt(int index, V value) {
		if (index < 0 || index > this.length) {
			throw new IndexOutOfBoundsException();
		}
		if (value == null) {
			throw new IllegalArgumentException();
		}
		return this.abstractInsertAt(index, value);
	}

	@Override
	public final A replaceAt(int index, V value) {
		if (index < 0 || index >= this.length) {
			throw new IndexOutOfBoundsException();
		}
		if (value == null) {
			throw new IllegalArgumentException();
		}
		if (this.getAt(index).equals(value)) {
			return (A) this;
		}
		return this.abstractReplaceAt(index, value);
	}

	@Override
	public final A[] split(int... indices) {
		if (indices == null) {
			throw new IllegalArgumentException();
		}
		A[] result = (A[]) Array.newInstance(this.valueClass, indices.length + 1);
		int lastIndex = 0;
		for (int i = 0; i < indices.length; i++) {
			int currentIndex = indices[i];
			if (currentIndex < lastIndex || currentIndex > this.length) {
				throw new IllegalArgumentException();
			}
			result[i] = this.extract(lastIndex, currentIndex - lastIndex);
			lastIndex = currentIndex;
		}
		result[indices.length] = this.extract(lastIndex, this.length - lastIndex);
		return result;
	}

	@Override
	public final A reverse() {
		return this.abstractReverse();
	}

	@Override
	protected final String defaultToStringName() {
		return "";
	}

	@Override
	public final Iterator<V> iterator() {
		return new Iterator<V>() {

			int currentIndex = 0;

			@Override
			public boolean hasNext() {
				return currentIndex < length;
			}

			@Override
			public V next() {
				return abstractGetAt(currentIndex++);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 43 * hash + this.length;
		for (V value : this) {
			hash = 43 * hash + value.hashCode();
		}
		return hash;
	}

	@Override
	public boolean equals(Object value) {
		if (this == value) {
			return true;
		}
		if (value == null || !(value instanceof AbstractImmutableArray)) {
			return false;
		}
		final AbstractImmutableArray other = (AbstractImmutableArray) value;
		if (this.length != other.length) {
			return false;
		}
		for (int i : this.getAllIndices()) {
			if (!this.abstractGetAt(i).equals(other.abstractGetAt(i))) {
				return false;
			}
		}
		return true;
	}

	abstract protected V abstractGetAt(int index);

	abstract protected A abstractExtract(int fromIndex, int length);

	abstract protected A abstractInsertAt(int index, V value);

	abstract protected A abstractReplaceAt(int index, V value);

	abstract protected A abstractReverse();

	abstract protected A abstractAppend(ImmutableArray<V> other);

}
