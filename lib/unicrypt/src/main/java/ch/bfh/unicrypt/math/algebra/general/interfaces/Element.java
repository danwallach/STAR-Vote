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
import ch.bfh.unicrypt.helper.converter.interfaces.Converter;
import ch.bfh.unicrypt.helper.hash.HashMethod;
import ch.bfh.unicrypt.math.algebra.additive.interfaces.AdditiveElement;
import ch.bfh.unicrypt.math.algebra.concatenative.interfaces.ConcatenativeElement;
import ch.bfh.unicrypt.math.algebra.dualistic.interfaces.DualisticElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import ch.bfh.unicrypt.math.algebra.multiplicative.interfaces.MultiplicativeElement;
import java.math.BigInteger;

/**
 * This abstract class represents the concept of an element in a mathematical group. It allows applying the group
 * operation and other methods from a {@link Group} in a convenient way. Most methods provided by {@link Element} have
 * an equivalent method in {@link Group}.
 * <p>
 * @param <V> Generic type of values stored in this element
 * @see Group
 * <p>
 * @author R. Haenni
 * @author R. E. Koenig
 * @version 2.0
 */
public interface Element<V extends Object> {

	/**
	 * Returns {@code true} if this element is an {@link AdditiveElement}.
	 * <p>
	 * @return {
	 * @true} if this element is an AdditiveElement
	 */
	public boolean isAdditive();

	/**
	 * Returns {@code true} if this element is a {@link MultiplicativeElement}.
	 * <p>
	 * @return {
	 * @true} if this element is a MultiplicativeElement
	 */
	public boolean isMultiplicative();

	/**
	 * Returns {@code true} if this element is a {@link ConcatenativeElement}.
	 * <p>
	 * @return {
	 * @true} if this element is a ConcatenativeElement
	 */
	public boolean isConcatenative();

	/**
	 * Returns {@code true} if this element is a {@link DualisticElement}.
	 * <p>
	 * @return {@code true} if this element is a DualisticElement
	 */
	public boolean isDualistic();

	/**
	 * Returns {@code true} if this element is a {@link Tuple}.
	 * <p>
	 * @return {@code true} if this element is a Tuple
	 */
	public boolean isTuple();

	/**
	 * Returns the unique {@link Set} to which this element belongs.
	 * <p>
	 * @return The element's set
	 */
	public Set<V> getSet();

	/**
	 * Returns the positive BigInteger value that corresponds this element.
	 * <p>
	 * @return The corresponding BigInteger value
	 */
	public V getValue();

	/**
	 * TODO Returns the positive BigInteger value that corresponds this element.
	 * <p>
	 * @return The corresponding BigInteger value
	 */
	public BigInteger getBigInteger();

	public BigInteger getBigInteger(Converter<V, BigInteger> convert);

	public BigInteger getBigInteger(ConvertMethod<BigInteger> convertMethod);

	public String getString();

	public String getString(Converter<V, String> convert);

	public String getString(ConvertMethod<String> convertMethod);

	/**
	 * TODO Returns the corresponding {@link ByteArray} of this element.
	 * <p>
	 * @return The corresponding ByteArray
	 */
	public ByteArray getByteArray();

	/**
	 * TODO Returns the corresponding {@link ByteArray} of this Element with the help of a a given
	 * {@link BigIntegerToByteArray}.
	 * <p>
	 * @param converter
	 * @return The corresponding ByteArray
	 */
	public ByteArray getByteArray(Converter<V, ByteArray> converter);

	public ByteArray getByteArray(ConvertMethod<ByteArray> convertMethod);

	/**
	 * TODO Returns the corresponding {@link ByteTree} of this Element.
	 * <p>
	 * @return The corresponding ByteTree
	 */
	public ByteTree getByteTree();

	public ByteTree getByteTree(Converter<V, ByteArray> converter);

	/**
	 * TODO Returns the corresponding {@link ByteTree} of this Element with the help of a given
	 * {@link BigIntegerToByteArray}.
	 * <p>
	 * @param convertMethod
	 * @return The corresponding ByteTree
	 */
	public ByteTree getByteTree(ConvertMethod<ByteArray> convertMethod);

	/**
	 * TODO
	 * <p>
	 * @return
	 */
	public ByteArray getHashValue();

	/**
	 * TODO
	 * <p>
	 * @param hashMethod
	 * @return
	 */
	public ByteArray getHashValue(HashMethod hashMethod);

	/**
	 * Checks if this element is mathematically equivalent to the given element. For this, they need to belong to the
	 * same set.
	 * <p>
	 * @param element The given Element
	 * @return {@code true} if the element is equivalent to the given element
	 */
	public boolean isEquivalent(Element element);

	// The following methods are equivalent to corresponding Set methods
	/**
	 * @return @see Group#apply(Element, Element)
	 */
	public Element<V> apply(Element element);

	/**
	 * @return @see Group#applyInverse(Element, Element)
	 */
	public Element<V> applyInverse(Element element);

	/**
	 * @return @see Group#selfApply(Element, BigInteger)
	 */
	public Element<V> selfApply(BigInteger amount);

	/**
	 * @return @see Group#selfApply(Element, Element)
	 */
	public Element<V> selfApply(Element<BigInteger> amount);

	/**
	 * @return @see Group#selfApply(Element, int)
	 */
	public Element<V> selfApply(int amount);

	/**
	 * @return @see Group#selfApply(Element)
	 */
	public Element<V> selfApply();

	/**
	 * @return @see Group#invert(Element)
	 */
	public Element<V> invert();

	/**
	 * @return @see Group#isIdentityElement(Element)
	 */
	public boolean isIdentity();

	/**
	 * @return @see CyclicGroup#isGenerator(Element)
	 */
	public boolean isGenerator();

}
