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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes;

import ch.bfh.unicrypt.helper.array.classes.DenseArray;
import ch.bfh.unicrypt.helper.array.interfaces.ImmutableArray;
import ch.bfh.unicrypt.helper.array.interfaces.RecursiveArray;
import ch.bfh.unicrypt.helper.bytetree.ByteTree;
import ch.bfh.unicrypt.helper.bytetree.ByteTreeNode;
import ch.bfh.unicrypt.helper.converter.abstracts.AbstractBigIntegerConverter;
import ch.bfh.unicrypt.helper.converter.classes.ConvertMethod;
import ch.bfh.unicrypt.helper.converter.interfaces.BigIntegerConverter;
import ch.bfh.unicrypt.helper.MathUtil;
import ch.bfh.unicrypt.math.algebra.general.abstracts.AbstractSet;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Group;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Monoid;
import ch.bfh.unicrypt.math.algebra.general.interfaces.SemiGroup;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
import java.math.BigInteger;
import java.util.Iterator;

/**
 *
 * @author rolfhaenni
 */
public class ProductSet
	   extends AbstractSet<Tuple, DenseArray<Element>>
	   implements RecursiveArray<Set> {

	private final DenseArray<Set> sets;

	protected ProductSet(DenseArray<Set> sets) {
		super(DenseArray.class);
		this.sets = sets;
	}

	public final boolean contains(Element... elements) {
		return this.contains(DenseArray.getInstance(elements));
	}

	public final Tuple getElement(final Element... elements) {
		return this.getElement(DenseArray.getInstance(elements));
	}

	public final Tuple getElementFrom(final int... values) {
		if (values == null) {
			throw new IllegalArgumentException();
		}
		BigInteger[] bigIntegers = new BigInteger[values.length];
		for (int i = 0; i < values.length; i++) {
			bigIntegers[i] = BigInteger.valueOf(values[i]);
		}
		return this.getElementFrom(bigIntegers);
	}

	public final Tuple getElementFrom(BigInteger... values) {
		if (values == null || values.length != this.getLength()) {
			throw new IllegalArgumentException();
		}
		Element[] elements = new Element[this.getLength()];
		for (int i : this.getAllIndices()) {
			elements[i] = this.getAt(i).getElementFrom(values[i]);
			if (elements[i] == null) {
				return null; // no such element
			}
		}
		return this.abstractGetElement(DenseArray.getInstance(elements));
	}

	//
	// The following protected methods implement the abstract methods from
	// various super-classes
	//
	@Override
	protected BigInteger abstractGetOrder() {
		if (this.isEmpty()) {
			return BigInteger.ONE;
		}
		if (this.isUniform()) {
			Set first = this.getFirst();
			if (first.isFinite() && first.hasKnownOrder()) {
				return first.getOrder().pow(this.getLength());
			}
			return first.getOrder();
		}
		BigInteger result = BigInteger.ONE;
		for (Set set : this.sets) {
			if (!set.isFinite()) {
				return Set.INFINITE_ORDER;
			}
			if (!set.hasKnownOrder() || result.equals(Set.UNKNOWN_ORDER)) {
				result = Set.UNKNOWN_ORDER;
			} else {
				result = result.multiply(set.getOrder());
			}
		}
		return result;
	}

	@Override
	protected boolean abstractContains(DenseArray<Element> value) {
		if (value == null || value.getLength() != this.getLength()) {
			return false;
		}
		for (int i : this.getAllIndices()) {
			if (!this.getAt(i).contains(value.getAt(i))) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected Tuple abstractGetElement(DenseArray<Element> value) {
		if (this.getLength() == 1) {
			return new Singleton(this, value);
		}
		if (this.getLength() == 2) {
			return new Pair(this, value);
		}
		if (this.getLength() == 3) {
			return new Triple(this, value);
		}
		return new Tuple(this, value);
	}

	@Override
	protected BigIntegerConverter<DenseArray<Element>> abstractGetBigIntegerConverter() {
		return new AbstractBigIntegerConverter<DenseArray<Element>>(null) { // class parameter not needed

			@Override
			protected BigInteger abstractConvert(DenseArray<Element> elements) {
				BigInteger[] bigIntegers = new BigInteger[getLength()];
				int i = 0;
				for (Element element : elements) {
					bigIntegers[i] = element.getBigInteger();
					i++;
				}
				return MathUtil.pair(bigIntegers);
			}

			@Override
			protected DenseArray<Element> abstractReconvert(BigInteger bigInteger) {
				BigInteger[] values = MathUtil.unpair(bigInteger, getLength());
				Element[] elements = new Element[getLength()];
				int i = 0;
				for (BigInteger value : values) {
					elements[i] = getAt(i).getElementFrom(value);
					i++;
				}
				return DenseArray.getInstance(elements);
			}
		};
	}

	@Override
	protected Tuple defaultGetElementFrom(ByteTree byteTree, ConvertMethod convertMethod) {
		if (!byteTree.isLeaf()) {
			int length = this.getLength();
			DenseArray<ByteTree> byteTrees = ((ByteTreeNode) byteTree).getByteTrees();
			if (byteTrees.getLength() == length) {
				Element[] elements = new Element[length];
				for (int i : this.getAllIndices()) {
					elements[i] = this.getAt(i).getElementFrom(byteTrees.getAt(i), convertMethod);
					if (elements[i] == null) {
						// no such element
						return null;
					}
				}
				return this.abstractGetElement(DenseArray.getInstance(elements));
			}
		}
		// no such element
		return null;
	}

	@Override
	protected Tuple abstractGetRandomElement(RandomByteSequence randomByteSequence) {
		final Element[] randomElements = new Element[this.getLength()];
		for (int i : this.getAllIndices()) {
			randomElements[i] = this.getAt(i).getRandomElement(randomByteSequence);
		}
		return this.abstractGetElement(DenseArray.getInstance(randomElements));
	}

	@Override
	protected boolean abstractEquals(Set set) {
		ProductSet other = (ProductSet) set;
		if (this.getLength() != other.getLength()) {
			return false;
		}
		for (int i : this.getAllIndices()) {
			if (!this.getAt(i).equals(other.getAt(i))) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected int abstractHashCode() {
		int hash = 7;
		hash = 47 * hash + this.getLength();
		for (int i : this.getAllIndices()) {
			hash = 47 * hash + this.getAt(i).hashCode();
		}
		return hash;
	}

	@Override
	protected boolean defaultIsEquivalent(Set set) {
		ProductSet other = (ProductSet) set;
		if (this.getLength() != other.getLength()) {
			return false;
		}
		for (int i : this.getAllIndices()) {
			if (!this.getAt(i).isEquivalent(other.getAt(i))) {
				return false;
			}
		}
		return true;
	}

	public int getArity() {
		return this.getLength();
	}

	@Override
	public int getLength() {
		return this.sets.getLength();
	}

	@Override
	public final boolean isEmpty() {
		return this.sets.isEmpty();
	}

	@Override
	public final boolean isUniform() {
		return this.sets.isUniform();
	}

	@Override
	public Iterable<Integer> getAllIndices() {
		return this.sets.getAllIndices();
	}

	@Override
	public Iterable<Integer> getIndices(Set set) {
		return this.sets.getIndices(set);
	}

	@Override
	public Iterable<Integer> getIndicesExcept(Set set) {
		return this.sets.getIndicesExcept(set);
	}

	@Override
	public int count(Set set) {
		return this.sets.count(set);
	}

	@Override
	public int countPrefix(Set set) {
		return this.sets.countPrefix(set);
	}

	@Override
	public int countSuffix(Set set) {
		return this.sets.countSuffix(set);
	}

	@Override
	public Set getFirst() {
		return this.sets.getFirst();
	}

	@Override
	public Set getLast() {
		return this.sets.getLast();
	}

	@Override
	public Set getAt(int index) {
		return this.sets.getAt(index);
	}

	@Override
	public Set getAt(int... indices) {
		if (indices == null) {
			throw new IllegalArgumentException();
		}
		Set set = this;
		for (final int index : indices) {
			if (set.isProduct()) {
				set = ((ProductSet) set).getAt(index);
			} else {
				throw new IllegalArgumentException();
			}
		}
		return set;
	}

	@Override
	public ProductSet extract(int offset, int length) {
		return ProductSet.getInstance(this.sets.extract(offset, length));
	}

	@Override
	public ProductSet extractPrefix(int length) {
		return ProductSet.getInstance(this.sets.extractPrefix(length));
	}

	@Override
	public ProductSet extractSuffix(int length) {
		return ProductSet.getInstance(this.sets.extractSuffix(length));
	}

	@Override
	public ProductSet extractRange(int fromIndex, int toIndex) {
		return ProductSet.getInstance(this.sets.extractRange(fromIndex, toIndex));
	}

	@Override
	public ProductSet removePrefix(int length) {
		return ProductSet.getInstance(this.sets.removePrefix(length));
	}

	@Override
	public ProductSet remove(int offset, int length) {
		return ProductSet.getInstance(this.sets.remove(offset, length));
	}

	@Override
	public ProductSet removeSuffix(int length) {
		return ProductSet.getInstance(this.sets.removeSuffix(length));
	}

	@Override
	public ProductSet removeRange(int fromIndex, int toIndex) {
		return ProductSet.getInstance(this.sets.removeRange(fromIndex, toIndex));
	}

	@Override
	public ProductSet removeAt(final int index) {
		return ProductSet.getInstance(this.sets.removeAt(index));
	}

	@Override
	public ProductSet insertAt(int index, Set set) {
		return ProductSet.getInstance(this.sets.insertAt(index, set));
	}

	@Override
	public ProductSet replaceAt(int index, Set set) {
		return ProductSet.getInstance(this.sets.replaceAt(index, set));
	}

	@Override
	public ProductSet add(Set set) {
		return ProductSet.getInstance(this.sets.add(set));
	}

	@Override
	public ProductSet append(ImmutableArray<Set> other) {
		return ProductSet.getInstance(this.sets.append(other));
	}

	@Override
	public ProductSet reverse() {
		return ProductSet.getInstance(this.sets.reverse());
	}

	@Override
	public ProductSet[] split(int... indices) {
		DenseArray<Set>[] setArray = this.sets.split(indices);
		ProductSet[] result = new ProductSet[setArray.length];
		for (int i = 0; i < setArray.length; i++) {
			result[i] = ProductSet.getInstance(setArray[i]);
		}
		return result;
	}

	@Override
	public Iterator<Set> iterator() {
		return this.sets.iterator();
	}

	@Override
	protected BigInteger defaultGetOrderLowerBound() {
		if (this.isUniform()) {
			return this.getFirst().getOrderLowerBound().pow(this.getLength());
		}
		BigInteger result = BigInteger.ONE;
		for (Set set : this.sets) {
			result = result.multiply(set.getOrderLowerBound());
		}
		return result;
	}

	@Override
	protected BigInteger defaultGetOrderUpperBound() {
		if (this.isUniform()) {
			return this.getFirst().getOrderUpperBound().pow(this.getLength());
		}
		BigInteger result = BigInteger.ONE;
		for (Set set : this.sets) {
			if (set.getOrderUpperBound().equals(Set.INFINITE_ORDER)) {
				return Set.INFINITE_ORDER;
			}
			result = result.multiply(set.getOrderUpperBound());
		}
		return result;
	}

	@Override
	protected BigInteger defaultGetMinimalOrder() {
		if (this.isUniform()) {
			return this.getFirst().getMinimalOrder();
		}
		BigInteger result = null;
		for (Set set : this.sets) {
			if (result == null) {
				result = set.getMinimalOrder();
			} else {
				result = result.min(set.getMinimalOrder());
			}
		}
		return result;
	}

	@Override
	protected String defaultToStringValue() {
		if (this.isEmpty()) {
			return "";
		}
		if (this.isUniform()) {
			return this.getFirst().toString() + "^" + this.getLength();
		}
		String result = "";
		String separator = "";
		for (Set set : this.sets) {
			result = result + separator + set.toString();
			separator = " x ";
		}
		return result;
	}

	//
	// STATIC FACTORY METHODS
	//
	public static ProductSet getInstance(DenseArray<Set> sets) {
		if (sets == null) {
			throw new IllegalArgumentException();
		}
		boolean isSemiGroup = true;
		boolean isMonoid = true;
		boolean isGroup = true;
		boolean isCyclic = true;
		for (final Set set : sets) {
			isSemiGroup = isSemiGroup && set.isSemiGroup();
			isMonoid = isMonoid && set.isMonoid();
			isGroup = isGroup && set.isGroup();
			isCyclic = isCyclic && set.isCyclic();
		}
		if (isCyclic) {
			BigInteger[] orders = new BigInteger[sets.getLength()];
			int i = 0;
			for (Set set : sets) {
				orders[i++] = set.getOrder();
			}
			if (MathUtil.areRelativelyPrime(orders)) {
				return new ProductCyclicGroup(sets);
			}
		}
		if (isGroup) {
			return new ProductGroup(sets);
		}
		if (isMonoid) {
			return new ProductMonoid(sets);
		}
		if (isSemiGroup) {
			return new ProductSemiGroup(sets);
		}
		return new ProductSet(sets);
	}

	public static ProductSet getInstance(final Set... sets) {
		return ProductSet.getInstance(DenseArray.<Set>getInstance(sets));
	}

	public static ProductSemiGroup getInstance(final SemiGroup... semiGroups) {
		return (ProductSemiGroup) ProductSet.getInstance(DenseArray.<Set>getInstance(semiGroups));
	}

	public static ProductMonoid getInstance(final Monoid... monoids) {
		return (ProductMonoid) ProductSet.getInstance(DenseArray.<Set>getInstance(monoids));
	}

	public static ProductGroup getInstance(final Group... groups) {
		return (ProductGroup) ProductSet.getInstance(DenseArray.<Set>getInstance(groups));
	}

	public static ProductSet getInstance(final Set set, int arity) {
		return ProductSet.getInstance(DenseArray.<Set>getInstance(set, arity));
	}

	public static ProductSemiGroup getInstance(final SemiGroup semiGroup, int arity) {
		return (ProductSemiGroup) ProductSet.getInstance(DenseArray.<Set>getInstance(semiGroup, arity));
	}

	public static ProductMonoid getInstance(final Monoid monoid, int arity) {
		return (ProductMonoid) ProductSet.getInstance(DenseArray.<Set>getInstance(monoid, arity));
	}

	public static ProductGroup getInstance(final Group group, int arity) {
		return (ProductGroup) ProductSet.getInstance(DenseArray.<Set>getInstance(group, arity));
	}

}
