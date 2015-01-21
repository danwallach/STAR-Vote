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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.abstracts;

import ch.bfh.unicrypt.helper.UniCrypt;
import ch.bfh.unicrypt.helper.array.classes.ByteArray;
import ch.bfh.unicrypt.helper.array.interfaces.ImmutableArray;
import ch.bfh.unicrypt.helper.bytetree.ByteTree;
import ch.bfh.unicrypt.helper.bytetree.ByteTreeLeaf;
import ch.bfh.unicrypt.helper.converter.abstracts.AbstractByteArrayConverter;
import ch.bfh.unicrypt.helper.converter.abstracts.AbstractStringConverter;
import ch.bfh.unicrypt.helper.converter.classes.ConvertMethod;
import ch.bfh.unicrypt.helper.converter.classes.bytearray.BigIntegerToByteArray;
import ch.bfh.unicrypt.helper.converter.classes.string.BigIntegerToString;
import ch.bfh.unicrypt.helper.converter.interfaces.BigIntegerConverter;
import ch.bfh.unicrypt.helper.converter.interfaces.ByteArrayConverter;
import ch.bfh.unicrypt.helper.converter.interfaces.Converter;
import ch.bfh.unicrypt.helper.converter.interfaces.StringConverter;
import ch.bfh.unicrypt.math.algebra.additive.interfaces.AdditiveSemiGroup;
import ch.bfh.unicrypt.math.algebra.concatenative.interfaces.ConcatenativeSemiGroup;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.ZMod;
import ch.bfh.unicrypt.math.algebra.dualistic.interfaces.Field;
import ch.bfh.unicrypt.math.algebra.dualistic.interfaces.Ring;
import ch.bfh.unicrypt.math.algebra.dualistic.interfaces.SemiRing;
import ch.bfh.unicrypt.math.algebra.general.interfaces.CyclicGroup;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Group;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Monoid;
import ch.bfh.unicrypt.math.algebra.general.interfaces.SemiGroup;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.ZStarMod;
import ch.bfh.unicrypt.math.algebra.multiplicative.interfaces.MultiplicativeSemiGroup;
import ch.bfh.unicrypt.random.classes.HybridRandomByteSequence;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
import java.math.BigInteger;
import java.util.Iterator;

/**
 * This abstract class provides a basis implementation for the interface Set.
 * <p>
 * @param <E> Generic type of elements of this set
 * @param <V> Generic type of values stored in the elements of this set
 * @see AbstractElement
 * <p>
 * @author R. Haenni
 * @author R. E. Koenig
 * @version 2.0
 */
public abstract class AbstractSet<E extends Element<V>, V extends Object>
	   extends UniCrypt
	   implements Set<V> {

	private final Class<? extends Object> valueClass;
	private BigInteger order, lowerBound, upperBound, minimum;

	private BigIntegerConverter<V> bigIntegerConverter;
	private StringConverter<V> stringConverter;
	private ByteArrayConverter<V> byteArrayConverter;

	protected AbstractSet(Class<? extends Object> valueClass) {
		this.valueClass = valueClass;
	}

	@Override
	public final boolean isSemiGroup() {
		return this instanceof SemiGroup;
	}

	@Override
	public final boolean isMonoid() {
		return this instanceof Monoid;
	}

	@Override
	public final boolean isGroup() {
		return this instanceof Group;
	}

	@Override
	public final boolean isSemiRing() {
		return this instanceof SemiRing;
	}

	@Override
	public final boolean isRing() {
		return this instanceof Ring;
	}

	@Override
	public final boolean isField() {
		return this instanceof Field;
	}

	@Override
	public final boolean isCyclic() {
		return this instanceof CyclicGroup;
	}

	@Override
	public final boolean isAdditive() {
		return this instanceof AdditiveSemiGroup;
	}

	@Override
	public final boolean isMultiplicative() {
		return this instanceof MultiplicativeSemiGroup;
	}

	@Override
	public final boolean isConcatenative() {
		return this instanceof ConcatenativeSemiGroup;
	}

	@Override
	public final boolean isProduct() {
		return this instanceof ImmutableArray;
	}

	@Override
	public final boolean isFinite() {
		return !this.getOrder().equals(Set.INFINITE_ORDER);
	}

	@Override
	public final boolean hasKnownOrder() {
		return !this.getOrder().equals(Set.UNKNOWN_ORDER);
	}

	@Override
	public final BigInteger getOrder() {
		if (this.order == null) {
			this.order = this.abstractGetOrder();
		}
		return this.order;
	}

	@Override
	public final BigInteger getOrderLowerBound() {
		if (this.lowerBound == null) {
			if (this.hasKnownOrder()) {
				this.lowerBound = this.getOrder();
			} else {
				this.lowerBound = this.defaultGetOrderLowerBound();
			}
		}
		return this.lowerBound;
	}

	@Override
	public final BigInteger getOrderUpperBound() {
		if (this.upperBound == null) {
			if (this.hasKnownOrder()) {
				this.upperBound = this.getOrder();
			} else {
				this.upperBound = this.defaultGetOrderUpperBound();
			}
		}
		return this.upperBound;
	}

	@Override
	public final BigInteger getMinimalOrder() {
		if (this.minimum == null) {
			this.minimum = this.defaultGetMinimalOrder();
		}
		return this.minimum;
	}

	@Override
	public final boolean isSingleton() {
		return this.getOrder().equals(BigInteger.ONE);
	}

	@Override
	public ZMod getZModOrder() {
		if (!(this.isFinite() && this.hasKnownOrder())) {
			throw new UnsupportedOperationException();
		}
		return ZMod.getInstance(this.getOrder());
	}

	@Override
	public ZStarMod getZStarModOrder() {
		if (!(this.isFinite() && this.hasKnownOrder())) {
			throw new UnsupportedOperationException();
		}
		return ZStarMod.getInstance(this.getOrder());
	}

	@Override
	public final E getElement(V value) {
		if (!this.contains(value)) {
			throw new IllegalArgumentException();
		}
		return this.abstractGetElement(value);
	}

	@Override
	public final boolean contains(V value) {
		if (value == null) {
			throw new IllegalArgumentException();
		}
		return this.abstractContains(value);
	}

	@Override
	public final boolean contains(Element element) {
		if (element == null) {
			throw new IllegalArgumentException();
		}
		return this.defaultContains(element);
	}

	@Override
	public final E getElementFrom(int integer) {
		return this.getElementFrom(BigInteger.valueOf(integer));
	}

	@Override
	public final E getElementFrom(BigInteger bigInteger) {
		return this.getElementFrom(bigInteger, this.getBigIntegerConverter());
	}

	@Override
	public final E getElementFrom(BigInteger bigInteger, Converter<V, BigInteger> converter) {
		if (bigInteger == null || converter == null) {
			throw new IllegalArgumentException();
		}
		V value = converter.reconvert(bigInteger);
		if (value != null && this.abstractContains(value)) {
			return this.abstractGetElement(value);
		}
		// no such element
		return null;
	}

	@Override
	public final E getElementFrom(BigInteger bigInteger, ConvertMethod<BigInteger> convertMethod) {
		if (convertMethod == null) {
			throw new IllegalArgumentException();
		}
		Converter<V, BigInteger> converter = (Converter<V, BigInteger>) convertMethod.getConverter(this.getClass());
		if (converter == null) {
			return this.getElementFrom(bigInteger);
		}
		return this.getElementFrom(bigInteger, converter);
	}

	@Override
	public final E getElementFrom(String string) {
		return this.getElementFrom(string, this.getStringConverter());
	}

	@Override
	public final E getElementFrom(String string, Converter<V, String> converter) {
		if (string == null || converter == null) {
			throw new IllegalArgumentException();
		}
		V value = converter.reconvert(string);
		if (value != null && this.abstractContains(value)) {
			return this.abstractGetElement(value);
		}
		// no such element
		return null;
	}

	@Override
	public final E getElementFrom(String string, ConvertMethod<String> convertMethod) {
		if (convertMethod == null) {
			throw new IllegalArgumentException();
		}
		Converter<V, String> converter = (Converter<V, String>) convertMethod.getConverter(this.getClass());
		if (converter == null) {
			return this.getElementFrom(string);
		}
		return this.getElementFrom(string, converter);
	}

	@Override
	public final E getElementFrom(ByteArray byteArray) {
		return this.getElementFrom(byteArray, this.getByteArrayConverter());
	}

	@Override
	public final E getElementFrom(ByteArray byteArray, Converter<V, ByteArray> converter) {
		if (byteArray == null || converter == null) {
			throw new IllegalArgumentException();
		}
		V value = converter.reconvert(byteArray);
		if (value != null && this.abstractContains(value)) {
			return this.abstractGetElement(value);
		}
		// no such element
		return null;
	}

	@Override
	public final E getElementFrom(ByteArray byteArray, ConvertMethod<ByteArray> convertMethod) {
		if (convertMethod == null) {
			throw new IllegalArgumentException();
		}
		Converter<V, ByteArray> converter = (Converter<V, ByteArray>) convertMethod.getConverter(this.getClass());
		if (converter == null) {
			return this.getElementFrom(byteArray);
		}
		return this.getElementFrom(byteArray, converter);
	}

	@Override
	public final E getElementFrom(ByteTree byteTree) {
		ConvertMethod<ByteArray> convertMethod = ConvertMethod.<ByteArray>getInstance();
		return this.getElementFrom(byteTree, convertMethod);
	}

	@Override
	public final E getElementFrom(ByteTree byteTree, Converter<V, ByteArray> converter) {
		ConvertMethod<ByteArray> convertMethod = ConvertMethod.<ByteArray>getInstance(converter);
		return this.getElementFrom(byteTree, convertMethod);
	}

	@Override
	public final E getElementFrom(ByteTree byteTree, ConvertMethod<ByteArray> convertMethod) {
		if (byteTree == null || convertMethod == null) {
			throw new IllegalArgumentException();
		}
		return this.defaultGetElementFrom(byteTree, convertMethod);
	}

	@Override
	public final E getRandomElement() {
		return this.abstractGetRandomElement(HybridRandomByteSequence.getInstance());
	}

	@Override
	public final E getRandomElement(RandomByteSequence randomByteSequence) {
		if (randomByteSequence == null) {
			throw new IllegalArgumentException();
		}
		return this.abstractGetRandomElement(randomByteSequence);
	}

	@Override
	public final BigIntegerConverter<V> getBigIntegerConverter() {
		if (this.bigIntegerConverter == null) {
			this.bigIntegerConverter = this.abstractGetBigIntegerConverter();
		}
		return this.bigIntegerConverter;
	}

	@Override
	public StringConverter<V> getStringConverter() {
		if (this.stringConverter == null) {
			this.stringConverter = this.defaultGetStringConverter();
		}
		return this.stringConverter;
	}

	@Override
	public ByteArrayConverter<V> getByteArrayConverter() {
		if (this.byteArrayConverter == null) {
			this.byteArrayConverter = this.defaultGetByteArrayConverter();
		}
		return this.byteArrayConverter;
	}

	@Override
	public final boolean areEquivalent(final Element element1, final Element element2) {
		if (!this.contains(element1) || !this.contains(element2)) {
			throw new IllegalArgumentException();
		}
		return element1.isEquivalent(element2);
	}

	@Override
	public final boolean isEquivalent(final Set other) {
		if (other == null) {
			throw new IllegalArgumentException();
		}
		if (this == other) {
			return true;
		}
		// Check if this.getClass() is a superclass of other.getClass()
		if (this.getClass().isAssignableFrom(other.getClass())) {
			return this.defaultIsEquivalent(other);
		}
		// Vice versa
		if (other.getClass().isAssignableFrom(this.getClass())) {
			return other.isEquivalent(this);
		}
		return false;
	}

	@Override
	public final Iterable<E> getElements() {
		return new Iterable<E>() {

			@Override
			public Iterator<E> iterator() {
				return getIterator();
			}

		};
	}

	@Override
	public final Iterable<E> getElements(final int n) {
		if (n < 0) {
			throw new IllegalArgumentException();
		}
		return new Iterable<E>() {

			@Override
			public Iterator<E> iterator() {
				return getIterator(n);
			}

		};
	}

	@Override
	public final Iterator<E> getIterator() {
		return this.defaultGetIterator(this.getOrder());
	}

	@Override
	public final Iterator<E> getIterator(int n) {
		return this.defaultGetIterator(BigInteger.valueOf(n).min(this.getOrder()));
	}

	@Override
	public final int hashCode() {
		int hash = 7;
		hash = 47 * hash + this.valueClass.hashCode();
		hash = 47 * hash + this.getClass().hashCode();
		hash = 47 * hash + this.abstractHashCode();
		return hash;
	}

	@Override
	public final boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		return this.abstractEquals((Set) other);
	}

	//
	// The following protected methods are default implementations for sets.
	// They may need to be changed in certain sub-classes.
	//
	protected BigInteger defaultGetOrderLowerBound() {
		return BigInteger.ZERO;
	}

	protected BigInteger defaultGetOrderUpperBound() {
		return Set.INFINITE_ORDER;
	}

	protected BigInteger defaultGetMinimalOrder() {
		return this.getOrderLowerBound();
	}

	// this method is different only for Subset
	protected boolean defaultContains(final Element element) {
		return this.isEquivalent(element.getSet());
	}

	protected E defaultGetElementFrom(ByteTree byteTree, ConvertMethod<ByteArray> convertMethod) {
		if (byteTree.isLeaf()) {
			ByteArray byteArray = ((ByteTreeLeaf) byteTree).getValue();
			return this.getElementFrom(byteArray, convertMethod);
		}
		// no such element
		return null;
	}

	protected StringConverter<V> defaultGetStringConverter() {
		return new AbstractStringConverter<V>(null) {

			private final StringConverter<BigInteger> converter = BigIntegerToString.getInstance();

			@Override
			protected String abstractConvert(V value) {
				return this.converter.convert(getBigIntegerConverter().convert(value));
			}

			@Override
			protected V abstractReconvert(String value) {
				return getBigIntegerConverter().reconvert(this.converter.reconvert(value));
			}
		};
	}

	protected ByteArrayConverter<V> defaultGetByteArrayConverter() {
		return new AbstractByteArrayConverter<V>(null) {

			private final ByteArrayConverter<BigInteger> converter = BigIntegerToByteArray.getInstance();

			@Override
			protected ByteArray abstractConvert(V value) {
				return this.converter.convert(getBigIntegerConverter().convert(value));
			}

			@Override
			protected V abstractReconvert(ByteArray value) {
				return getBigIntegerConverter().reconvert(this.converter.reconvert(value));
			}
		};
	}

	protected boolean defaultIsEquivalent(Set set) {
		return this.abstractEquals(set);
	}

	protected Iterator<E> defaultGetIterator(final BigInteger maxCounter) {
		final AbstractSet<E, V> set = this;
		return new Iterator<E>() {
			BigInteger counter = BigInteger.ZERO;
			BigInteger currentValue = BigInteger.ZERO;

			@Override
			public boolean hasNext() {
				if (set.hasKnownOrder()) {
					if (set.isFinite()) {
						return counter.compareTo(maxCounter) < 0;
					}
					return true;
				}
				return false; // the default iterator does not work for sets of unknown order
			}

			@Override
			public E next() {
				E element = set.getElementFrom(this.currentValue);
				while (element == null) {
					this.currentValue = this.currentValue.add(BigInteger.ONE);
					element = set.getElementFrom(this.currentValue);
				}
				this.counter = this.counter.add(BigInteger.ONE);
				this.currentValue = this.currentValue.add(BigInteger.ONE);
				return element;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

		};
	}

	//
	// The following protected abstract method must be implemented in every direct
	// sub-class.
	//
	protected abstract BigInteger abstractGetOrder();

	protected abstract boolean abstractContains(V value);

	protected abstract E abstractGetElement(V value);

	protected abstract E abstractGetRandomElement(RandomByteSequence randomByteSequence);

	protected abstract BigIntegerConverter<V> abstractGetBigIntegerConverter();

	protected abstract boolean abstractEquals(Set set);

	protected abstract int abstractHashCode();

}
