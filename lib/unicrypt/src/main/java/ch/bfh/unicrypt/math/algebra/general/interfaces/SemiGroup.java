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

import java.math.BigInteger;

/**
 * TODO This interface represents the mathematical concept of a semigroup. It defines a set of elements and an
 * associative (but not necessarily commutative) binary operation. It is implemented as a specialization of {@link Set}.
 * <p>
 * @param <V> Generic type of values stored in the elements of this semigroup
 * @see "Handbook of Applied Cryptography, Definition 2.162"
 * <p>
 * @author R. Haenni
 * @author R. E. Koenig
 * @version 2.0
 */
public interface SemiGroup<V extends Object>
	   extends Set<V> {

	/**
	 * Applies the binary group operation to two semigroup elements (in the given order).
	 * <p>
	 * @param element1 The first semigroup element
	 * @param element2 The second semigroup element
	 * @return The result of applying the semigroup operation to the two input elements
	 * <p>
	 * @throws IllegalArgumentException if {@code element1} or {@code element2} does not belong to the semigroup
   * */
	public Element<V> apply(Element element1, Element element2);

	/**
	 * Applies the binary group operation pair-wise sequentially to multiple elements (in the given order). Returns the
	 * identity element, if the given list of elements is empty.
	 * <p>
	 * @param elements A given array of elements
	 * @return The result of applying the operation to the input elements
	 * @throws IllegalArgumentException if one of the elements in {@code elements} does not belong to the semigroup
	 */
	public Element<V> apply(Element... elements);

	public Element<V> apply(Iterable<Element> elements);

	/**
	 * Applies the binary group operation repeatedly to {@code amount} many instances of a given semigroup element. If
	 * {@code amount} equals 0, then the identity element is returned. If {@code amount} is negative, then the
	 * corresponding positive value is applied to the inverse of the given element. If the semigroup is finite and if
	 * its order is known to be {@code q}, then {
	 * <p>
	 * @amount} can be replaced by {@code amount mod q}.
	 * @param element The given group element
	 * @param amount  The number of instances of the input element
	 * @return The result of applying the group operation multiple times to the input element
	 * @throws IllegalArgumentException if {@code element} does not belong to the semigroup
	 */
	public Element<V> selfApply(Element element, BigInteger amount);

	/**
	 * Same as {@link #Group.selfApply(Element, BigInteger)}, except that the amount is given as an {@link Element}
	 * object, which can always be converted into a BigInteger value.
	 * <p>
	 * @param element A given group element
	 * @param amount  The number of instances of the input element given as an {@link Element} object
	 * @return The result of applying the group operation multiple times to the input element
	 * @throws IllegalArgumentException if {@code element} does not belong to the semigroup
	 */
	public Element<V> selfApply(Element element, Element<BigInteger> amount);

	/**
	 * Same as {@link #Group.selfApply(Element, BigInteger)}, except that the amount is given as an {@code int} value.
	 * <p>
	 * @param element A given group element
	 * @param amount  The number of instances of the input element
	 * @return The result of applying the operation multiple times to the input element TODO
	 * @throws IllegalArgumentException if {@code element} does not belong to the semigroup
	 */
	public Element<V> selfApply(Element element, int amount);

	/**
	 * Applies the group operation to two instances of a given semigroup element. This is equivalent to
	 * {@code selfApply(element, 2)}.
	 * <p>
	 * @param element A given group element
	 * @return The result of applying the group operation to the input element TODO
	 * @throws IllegalArgumentException if {@code element} does not belong to the semigroup
	 */
	public Element<V> selfApply(Element element);

	/**
	 * Applies the binary operation pair-wise sequentially to the results of computing
	 * {@link #selfApply(Element, BigInteger)} multiple times. In an additive group, this operation is sometimes called
	 * 'weighed sum', and 'product-of-powers' in a multiplicative group.
	 * <p>
	 * @param elements A given array of elements
	 * @param amounts  Corresponding amounts
	 * @return The result of this operation
	 * @throws IllegalArgumentException if one of the elements of {@code elements} does not belong to the semigroup
	 * @throws IllegalArgumentException if {@code elements} and {@code amounts} have different lengths
	 */
	public Element<V> multiSelfApply(Element[] elements, BigInteger[] amounts);

}
