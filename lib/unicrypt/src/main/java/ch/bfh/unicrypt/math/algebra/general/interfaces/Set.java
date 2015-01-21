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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces;

import ch.bfh.unicrypt.helper.array.classes.ByteArray;
import ch.bfh.unicrypt.helper.bytetree.ByteTree;
import ch.bfh.unicrypt.helper.converter.classes.ConvertMethod;
import ch.bfh.unicrypt.helper.converter.classes.bytearray.BigIntegerToByteArray;
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
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.ZStarMod;
import ch.bfh.unicrypt.math.algebra.multiplicative.interfaces.MultiplicativeSemiGroup;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
import java.math.BigInteger;
import java.util.Iterator;

/**
 * This interface represents the concept of a non-empty mathematical set of elements. The number of elements in the set
 * is called order of this set. The order may be infinite or unknown. TODO It is assumed that each element of a set
 * corresponds to a unique BigInteger or integer value. Therefore, the interface provides methods for converting
 * elements into corresponding BigInteger or integer values and back.
 * <p>
 * @author R. Haenni
 * @author R. E. Koenig
 * @version 2.0
 * @param <V> Generic type of values stored in the elements of this set
 */
public interface Set<V extends Object> {

	/**
	 * A constant value that represents an infinite order.
	 */
	public static final BigInteger INFINITE_ORDER = BigInteger.valueOf(-1);
	/**
	 * A constant value that represents an unknown order.
	 */
	public static final BigInteger UNKNOWN_ORDER = BigInteger.valueOf(-2);

	/**
	 * Returns {@code true} if this set is an instance of {@link SemiGroup}.
	 * <p>
	 * @return {@code true} if this set is a semigroup
	 */
	public boolean isSemiGroup();

	/**
	 * Returns {@code true} if this set is an instance of {@link Monoid}.
	 * <p>
	 * @return {@code true} if this set is a monoid
	 */
	public boolean isMonoid();

	/**
	 * Returns {@code true} if this set is an instance of {@link Group}.
	 * <p>
	 * @return {@code true} if this set is a group
	 */
	public boolean isGroup();

	/**
	 * Returns {@code true} if this set is an instance of {@link SemiRing}.
	 * <p>
	 * @return {@code true} if this set is a semiring
	 */
	public boolean isSemiRing();

	/**
	 * Returns {@code true} if this set is an instance of {@link Ring}.
	 * <p>
	 * @return {@code true} if this set is a ring
	 */
	public boolean isRing();

	/**
	 * Returns {@code true} if this set is an instance of {@link Field}.
	 * <p>
	 * @return {@code true} if this set is a field
	 */
	public boolean isField();

	/**
	 * Returns {@code true} if this set is an instance of {@link CyclicGroup}.
	 * <p>
	 * @return {@code true} if this set is cyclic
	 */
	public boolean isCyclic();

	/**
	 * Returns {@code true} if this set is an instance of {@link AdditiveSemiGroup}.
	 * <p>
	 * @return {@code true} if this set is additive
	 */
	public boolean isAdditive();

	/**
	 * Returns {@code true} if this set is an instance of {@link MultiplicativeSemiGroup}.
	 * <p>
	 * @return {@code true} if this set is multiplicative
	 */
	public boolean isMultiplicative();

	/**
	 * Returns {@code true} if this set is an instance of {@link ConcatenativeSemiGroup}.
	 * <p>
	 * @return {@code true} if this set is concatenative
	 */
	public boolean isConcatenative();

	/**
	 * Returns {@code true} if this set is a cartesian product
	 * <p>
	 * @return {@code true} if this set is a product set
	 */
	public boolean isProduct();

	/**
	 * Returns {@code true} if this set is of finite order.
	 * <p>
	 * @return {@code true} if this set is finite
	 */
	public boolean isFinite();

	/**
	 * Returns {@code true} if this set has a known order.
	 * <p>
	 * @return {@code true} if this set has a known order
	 */
	public boolean hasKnownOrder();

	/**
	 * Returns the set order. If the set order is unknown, {@link #UNKNOWN_ORDER} is returned. If the set order is
	 * infinite, {@link #INFINITE_ORDER} is returned.
	 * <p>
	 * @see "Handbook of Applied Cryptography, Definition 2.163"
	 * @return The set order
	 */
	public BigInteger getOrder();

	/**
	 * TODO Returns a lower bound for the set order in case the exact set order is unknown. The least return value is 0
	 * (?). Otherwise, if the exact set order is known (or infinite), the exact set order is returned.
	 * <p>
	 * @return A lower bound for the set order
	 */
	public BigInteger getOrderLowerBound();

	/**
	 * TODO Returns an upper bound for the set order in case the exact set order is unknown. The heighest return value
	 * is -1. Otherwise, if the exact set order is known (or infinite), the exact set order is returned.
	 * <p>
	 * @return A upper bound for the set order
	 */
	public BigInteger getOrderUpperBound();

	/**
	 * TODO Returns the minimal order of this set. entweder order oder min. coumpound (recursive)
	 * <p>
	 * @return The minimal order of this set
	 */
	public BigInteger getMinimalOrder();

	/**
	 * Checks if the set is of order 1.
	 * <p>
	 * @return {@code true} if the order is 1
	 */
	public boolean isSingleton();

	/**
	 * TODO Returns an additive integer group of type {@link ZMod} with the same set order. For this to work, the set
	 * order must be finite and known.
	 * <p>
	 * @return The resulting additive group.
	 * @throws UnsupportedOperationException if the set order is infinite or unknown
	 */
	public ZMod getZModOrder();

	/**
	 * Returns an multiplicative integer set of type {@link ZMod} with the same set order. For this to work, the set
	 * order must be finite and known. TODO teilerfremd
	 * <p>
	 * @return The resulting multiplicative group.
	 * @throws UnsupportedOperationException if the set order is infinite or unknown
	 */
	public ZStarMod getZStarModOrder();

	/**
	 * Checks if a given element belongs to this set.
	 * <p>
	 * @param element The given element
	 * @return {@code true} if {@literal element} belongs to this set
	 */
	public boolean contains(Element element);

	/**
	 * TODO Checks if a given value belongs to this set.
	 * <p>
	 * @param value The given value
	 * @return {@code true} if {@literal value} belongs to this set
	 */
	public boolean contains(V value);

	/**
	 * TODO Returns the corresponding {@link Element} for the given {@literal value}.
	 * <p>
	 * @param value The given value
	 * @return the corresponding element for the given value
	 * @throws IllegalArgumentException if this set does not contain {@literal value}
	 */
	public Element<V> getElement(V value);

	/**
	 * Creates and returns the element that corresponds to a given integer (if one exists). Returns {@literal null}
	 * otherwise.
	 * <p>
	 * @param integer The given integer value
	 * @return The corresponding element, or {@literal null} if no such element exists
	 */
	public Element<V> getElementFrom(int integer);

	/**
	 * Creates and returns the element that corresponds to a given BigInteger value (if one exists). Returns
	 * {@literal null} otherwise.
	 * <p>
	 * @param bigInteger The given BigInteger value
	 * @return The corresponding element, or {@literal null} if no such element exists
	 */
	public Element<V> getElementFrom(BigInteger bigInteger);

	public Element<V> getElementFrom(BigInteger bigInteger, Converter<V, BigInteger> converter);

	public Element<V> getElementFrom(BigInteger bigInteger, ConvertMethod<BigInteger> convertMethod);

	public Element<V> getElementFrom(String string);

	public Element<V> getElementFrom(String string, Converter<V, String> converter);

	public Element<V> getElementFrom(String string, ConvertMethod<String> convertMethod);

	/**
	 * TODO Returns the corresponding {@link Element} for the given {@link ByteArray} using the default converter.
	 * <p>
	 * @param byteArray The given ByteArray
	 * @return the corresponding element
	 */
	public Element<V> getElementFrom(ByteArray byteArray);

	public Element<V> getElementFrom(ByteArray byteArray, Converter<V, ByteArray> converter);

	/**
	 * TODO Returns the corresponding {@link Element} for the given {@link ByteArray} using the
	 * {@link BigIntegerToByteArray}.
	 * <p>
	 * @param byteArray
	 * @param convertMethod
	 * @return
	 */
	public Element<V> getElementFrom(ByteArray byteArray, ConvertMethod<ByteArray> convertMethod);

	/**
	 * TODO Returns the corresponding {@link Element} for the given {@link ByteTree}.
	 * <p>
	 * @param byteTree The given ByteTree
	 * @return the corresponding element
	 */
	public Element<V> getElementFrom(ByteTree byteTree);

	public Element<V> getElementFrom(ByteTree byteTree, Converter<V, ByteArray> converter);

	/**
	 * <p>
	 * <p>
	 * @param byteTree
	 * @param convertMethod
	 * @return
	 */
	public Element<V> getElementFrom(ByteTree byteTree, ConvertMethod<ByteArray> convertMethod);

	/**
	 * Selects and returns a random group element using the default random generator. For finite order set, it is
	 * selected uniformly at random. For sets of infinite or unknown order, the underlying probability distribution is
	 * not further specified.
	 * <p>
	 * @return A random element from the set
	 */
	public Element<V> getRandomElement();

	/**
	 * Selects and returns a random set element using a given random generator. If no random generator is specified,
	 * i.e., if {@literal random} is null, then the system-wide random generator is taken. For finite order set, it is
	 * selected uniformly at random. For sets of infinite or unknown order, the underlying probability distribution is
	 * not generally specified.
	 * <p>
	 * @param randomByteSequence Either {@literal null} or a given random generator
	 * @return A random element from the set
	 */
	public Element<V> getRandomElement(RandomByteSequence randomByteSequence);

	/**
	 * Checks if two given elements of this set are equal.
	 * <p>
	 * @param element1 The first element
	 * @param element2 The second element
	 * @return {@code true} if the elements are equal and belong to the group
	 */
	public boolean areEquivalent(Element element1, Element element2);

	/**
	 * TODO Returns {
	 * <p>
	 * @true} if this set is equal to a given Set.
	 * <p>
	 * @param set The given Set.
	 * @return {@code true} if this set is equal
	 */
	public boolean isEquivalent(Set set);

	public BigIntegerConverter<V> getBigIntegerConverter();

	public StringConverter<V> getStringConverter();

	public ByteArrayConverter<V> getByteArrayConverter();

	/**
	 * TODO
	 * <p>
	 * @return
	 */
	public Iterable<? extends Element<V>> getElements();

	public Iterable<? extends Element<V>> getElements(int n);

	/**
	 * TODO
	 * <p>
	 * @return
	 */
	public Iterator<? extends Element<V>> getIterator();

	public Iterator<? extends Element<V>> getIterator(int n);

}
