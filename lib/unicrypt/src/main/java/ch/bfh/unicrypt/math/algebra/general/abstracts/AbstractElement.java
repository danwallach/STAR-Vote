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
import ch.bfh.unicrypt.helper.bytetree.ByteTree;
import ch.bfh.unicrypt.helper.converter.classes.ConvertMethod;
import ch.bfh.unicrypt.helper.converter.interfaces.Converter;
import ch.bfh.unicrypt.helper.hash.HashAlgorithm;
import ch.bfh.unicrypt.helper.hash.HashMethod;
import ch.bfh.unicrypt.math.algebra.additive.interfaces.AdditiveElement;
import ch.bfh.unicrypt.math.algebra.concatenative.interfaces.ConcatenativeElement;
import ch.bfh.unicrypt.math.algebra.dualistic.interfaces.DualisticElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import ch.bfh.unicrypt.math.algebra.general.interfaces.CyclicGroup;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Group;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Monoid;
import ch.bfh.unicrypt.math.algebra.general.interfaces.SemiGroup;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import ch.bfh.unicrypt.math.algebra.multiplicative.interfaces.MultiplicativeElement;
import java.math.BigInteger;
import java.util.HashMap;

/**
 * This abstract class represents the concept an element in a mathematical group. It allows applying the group operation
 * and other methods from a {@link Group} in a convenient way. Most methods provided by {@link AbstractElement} have an
 * equivalent method in {@link Group}.
 * <p>
 * @param <S> Generic type of {@link Set} of this element
 * @param <E> Generic type of this element
 * @param <V> Generic type of value stored in this element
 * @see Group
 * <p>
 * @author R. Haenni
 * @author R. E. Koenig
 * @version 2.0
 */
public abstract class AbstractElement<S extends Set<V>, E extends Element<V>, V extends Object>
	   extends UniCrypt
	   implements Element<V> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final S set;
	private final V value;

	// the following fields are needed for optimizations
	private final HashMap<Converter<V, BigInteger>, BigInteger> bigIntegers;
	private final HashMap<Converter<V, String>, String> strings;
	private final HashMap<Converter<V, ByteArray>, ByteArray> byteArrays;
	private final HashMap<HashMethod, ByteArray> hashValues;

	protected AbstractElement(final S set, V value) {
		this.set = set;
		this.value = value;
		this.bigIntegers = new HashMap<Converter<V, BigInteger>, BigInteger>();
		this.strings = new HashMap<Converter<V, String>, String>();
		this.byteArrays = new HashMap<Converter<V, ByteArray>, ByteArray>();
		this.hashValues = new HashMap<HashMethod, ByteArray>();
	}

	@Override
	public boolean isAdditive() {
		return this instanceof AdditiveElement;
	}

	@Override
	public boolean isMultiplicative() {
		return this instanceof MultiplicativeElement;
	}

	@Override
	public boolean isConcatenative() {
		return this instanceof ConcatenativeElement;
	}

	@Override
	public boolean isDualistic() {
		return this instanceof DualisticElement;
	}

	@Override
	public final boolean isTuple() {
		return this instanceof Tuple;
	}

	/**
	 * Returns the unique {@link Set} to which this element belongs
	 * <p>
	 * @return The element's set
	 */
	@Override
	public final S getSet() {
		return this.set;
	}

	/**
	 * Returns the positive BigInteger value that corresponds the element.
	 * <p>
	 * @return The corresponding BigInteger value
	 */
	@Override
	public final V getValue() {
		return this.value;
	}

	@Override
	public BigInteger getBigInteger() {
		return this.getBigInteger(this.set.getBigIntegerConverter());
	}

	@Override
	public BigInteger getBigInteger(Converter<V, BigInteger> converter) {
		if (converter == null) {
			throw new IllegalArgumentException();
		}
		BigInteger result = this.bigIntegers.get(converter);
		if (result == null) {
			result = converter.convert(this.value);
			this.bigIntegers.put(converter, result);
		}
		return result;
	}

	@Override
	public BigInteger getBigInteger(ConvertMethod<BigInteger> convertMethod) {
		if (convertMethod == null) {
			throw new IllegalArgumentException();
		}
		Converter<V, BigInteger> converter = (Converter<V, BigInteger>) convertMethod.getConverter(this.value.getClass());
		if (converter == null) {
			return this.getBigInteger();
		}
		return this.getBigInteger(converter);
	}

	@Override
	public String getString() {
		return this.getString(this.set.getStringConverter());
	}

	@Override
	public String getString(Converter<V, String> converter) {
		if (converter == null) {
			throw new IllegalArgumentException();
		}
		String result = this.strings.get(converter);
		if (result == null) {
			result = converter.convert(this.value);
			this.strings.put(converter, result);
		}
		return result;
	}

	@Override
	public String getString(ConvertMethod<String> convertMethod) {
		if (convertMethod == null) {
			throw new IllegalArgumentException();
		}
		Converter<V, String> converter = (Converter<V, String>) convertMethod.getConverter(this.value.getClass());
		if (converter == null) {
			return this.getString();
		}
		return this.getString(converter);
	}

	@Override
	public ByteArray getByteArray() {
		return this.getByteArray(this.set.getByteArrayConverter());
	}

	@Override
	public ByteArray getByteArray(Converter<V, ByteArray> converter) {
		if (converter == null) {
			throw new IllegalArgumentException();
		}
		ByteArray result = this.byteArrays.get(converter);
		if (result == null) {
			result = converter.convert(this.value);
			this.byteArrays.put(converter, result);
		}
		return result;
	}

	@Override
	public ByteArray getByteArray(ConvertMethod<ByteArray> convertMethod) {
		if (convertMethod == null) {
			throw new IllegalArgumentException();
		}
		Converter<V, ByteArray> converter = (Converter<V, ByteArray>) convertMethod.getConverter(this.value.getClass());
		if (converter == null) {
			return this.getByteArray();
		}
		return this.getByteArray(converter);
	}

	@Override
	public ByteTree getByteTree() {
		ConvertMethod<ByteArray> convertMethod = ConvertMethod.<ByteArray>getInstance();
		return this.getByteTree(convertMethod);
	}

	@Override
	public ByteTree getByteTree(Converter<V, ByteArray> converter) {
		ConvertMethod<ByteArray> convertMethod = ConvertMethod.<ByteArray>getInstance(converter);
		return this.getByteTree(convertMethod);
	}

	@Override
	public ByteTree getByteTree(ConvertMethod<ByteArray> convertMethod) {
		if (convertMethod == null) {
			throw new IllegalArgumentException();
		}
		if (this.isTuple()) {
			Tuple tuple = (Tuple) this;
			ByteTree[] byteTrees = new ByteTree[tuple.getArity()];
			int i = 0;
			for (Element element : tuple) {
				byteTrees[i++] = element.getByteTree(convertMethod);
			}
			return ByteTree.getInstance(byteTrees);

		}
		return ByteTree.getInstance(this.getByteArray(convertMethod));
	}

	@Override
	public final ByteArray getHashValue() {
		return this.getHashValue(HashMethod.getInstance());
	}

	@Override
	public final ByteArray getHashValue(HashMethod hashMethod) {
		if (hashMethod == null) {
			throw new IllegalArgumentException();
		}
		ByteArray hashValue = this.hashValues.get(hashMethod);
		if (hashValue == null) {
			ConvertMethod<ByteArray> convertMethod = hashMethod.getConvertMethod();
			HashAlgorithm algorithm = hashMethod.getHashAlgorithm();
			switch (hashMethod.getMode()) {
				case BYTEARRAY:
					hashValue = this.getByteArray(convertMethod).getHashValue(algorithm);
					break;
				case BYTETREE:
					hashValue = this.getByteTree(convertMethod).getHashValue(algorithm);
					break;
				case RECURSIVE:
					hashValue = this.getByteTree(convertMethod).getRecursiveHashValue(algorithm);
					break;
				default:
					throw new UnsupportedOperationException();
			}
			this.hashValues.put(hashMethod, hashValue);
		}
		return hashValue;
	}

	//
	// The following methods are equivalent to corresponding Set methods
	//
	/**
	 * @see Group#apply(Element, Element)
	 */
	@Override
	public final E apply(final Element element) {
		if (this.set.isSemiGroup()) {
			SemiGroup semiGroup = ((SemiGroup) this.set);
			return (E) semiGroup.apply(this, element);
		}
		throw new UnsupportedOperationException();
	}

	/**
	 * @see Group#applyInverse(Element, Element)
	 */
	@Override
	public final E applyInverse(final Element element) {
		if (this.set.isGroup()) {
			Group group = ((Group) this.set);
			return (E) group.applyInverse(this, element);
		}
		throw new UnsupportedOperationException();
	}

	/**
	 * @see Group#selfApply(Element, BigInteger)
	 */
	@Override
	public final E selfApply(final BigInteger amount) {
		if (this.set.isSemiGroup()) {
			SemiGroup semiGroup = ((SemiGroup) this.set);
			return (E) semiGroup.selfApply(this, amount);
		}
		throw new UnsupportedOperationException();
	}

	/**
	 * @see Group#selfApply(Element, Element)
	 */
	@Override
	public final E selfApply(final Element<BigInteger> amount) {
		if (this.set.isSemiGroup()) {
			SemiGroup semiGroup = ((SemiGroup) this.set);
			return (E) semiGroup.selfApply(this, amount);
		}
		throw new UnsupportedOperationException();
	}

	/**
	 * @see Group#selfApply(Element, int)
	 */
	@Override
	public final E selfApply(final int amount) {
		if (this.set.isSemiGroup()) {
			SemiGroup semiGroup = ((SemiGroup) this.set);
			return (E) semiGroup.selfApply(this, amount);
		}
		throw new UnsupportedOperationException();
	}

	/**
	 * @see Group#selfApply(Element)
	 */
	@Override
	public final E selfApply() {
		if (this.set.isSemiGroup()) {
			SemiGroup semiGroup = ((SemiGroup) this.set);
			return (E) semiGroup.selfApply(this);
		}
		throw new UnsupportedOperationException();
	}

	/**
	 * @see Group#invert(Element)
	 */
	@Override
	public final E invert() {
		if (this.set.isGroup()) {
			Group group = ((Group) this.set);
			return (E) group.invert(this);
		}
		throw new UnsupportedOperationException();
	}

	/**
	 * @see Group#isIdentityElement(Element)
	 */
	@Override
	public final boolean isIdentity() {
		if (this.set.isMonoid()) {
			Monoid monoid = ((Monoid) this.set);
			return monoid.isIdentityElement(this);
		}
		throw new UnsupportedOperationException();
	}

	/**
	 * @see CyclicGroup#isGenerator(Element)
	 */
	@Override
	public final boolean isGenerator() {
		if (this.set.isCyclic()) {
			CyclicGroup cyclicGroup = ((CyclicGroup) this.set);
			return cyclicGroup.isGenerator(this);
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public final boolean isEquivalent(final Element other) {
		if (other == null) {
			throw new IllegalArgumentException();
		}
		if (this == other) {
			return true;
		}
		if (!this.set.isEquivalent(other.getSet())) {
			return false;
		}
		return this.value.equals(other.getValue());
	}

	@Override
	public int hashCode() {
		int hashCode = 7;
		hashCode = 13 * hashCode + this.set.hashCode();
		hashCode = 13 * hashCode + this.value.hashCode();
		return hashCode;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null || this.getClass() != object.getClass()) {
			return false;
		}
		final Element other = (Element) object;
		if (!this.set.equals(other.getSet())) {
			return false;
		}
		return this.value.equals(other.getValue());
	}

	@Override
	protected String defaultToStringName() {
		return this.getClass().getSimpleName();
	}

	@Override
	protected String defaultToStringValue() {
		return this.value.toString();
	}

}
