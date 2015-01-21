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
import ch.bfh.unicrypt.math.algebra.general.abstracts.AbstractElement;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import java.util.Iterator;

/**
 *
 * @author rolfhaenni
 */
public class Tuple
	   extends AbstractElement<ProductSet, Tuple, DenseArray<Element>>
	   implements RecursiveArray<Element> {

	protected Tuple(final ProductSet set, final DenseArray<Element> elements) {
		super(set, elements);
	}

	public int getArity() {
		return this.getLength();
	}

	@Override
	public int getLength() {
		return this.getValue().getLength();
	}

	@Override
	public final boolean isEmpty() {
		return this.getValue().isEmpty();
	}

	@Override
	public final boolean isUniform() {
		return this.getValue().isUniform();
	}

	@Override
	public Iterable<Integer> getAllIndices() {
		return this.getValue().getAllIndices();
	}

	@Override
	public Iterable<Integer> getIndices(Element element) {
		return this.getValue().getIndices(element);
	}

	@Override
	public Iterable<Integer> getIndicesExcept(Element element) {
		return this.getValue().getIndicesExcept(element);
	}

	@Override
	public int count(Element element) {
		return this.getValue().count(element);
	}

	@Override
	public int countPrefix(Element element) {
		return this.getValue().countPrefix(element);
	}

	@Override
	public int countSuffix(Element element) {
		return this.getValue().countSuffix(element);
	}

	@Override
	public Element getFirst() {
		return this.getValue().getFirst();
	}

	@Override
	public Element getLast() {
		return this.getValue().getLast();
	}

	@Override
	public Element getAt(int index) {
		return this.getValue().getAt(index);
	}

	@Override
	public Element getAt(int... indices) {
		if (indices == null) {
			throw new IllegalArgumentException();
		}
		Element element = this;
		for (final int index : indices) {
			if (element.isTuple()) {
				element = ((Tuple) element).getAt(index);
			} else {
				throw new IllegalArgumentException();
			}
		}
		return element;
	}

	@Override
	public Tuple extract(int offset, int length) {
		return Tuple.getInstance(this.getSet().extract(offset, length), this.getValue().extract(offset, length));
	}

	@Override
	public Tuple extractPrefix(int length) {
		return Tuple.getInstance(this.getSet().extractPrefix(length), this.getValue().extractPrefix(length));
	}

	@Override
	public Tuple extractSuffix(int length) {
		return Tuple.getInstance(this.getSet().extractSuffix(length), this.getValue().extractSuffix(length));
	}

	@Override
	public Tuple extractRange(int fromIndex, int toIndex) {
		return Tuple.getInstance(this.getSet().extractRange(fromIndex, toIndex), this.getValue().extractRange(fromIndex, toIndex));
	}

	@Override
	public Tuple remove(int offset, int length) {
		return Tuple.getInstance(this.getSet().remove(offset, length), this.getValue().remove(offset, length));
	}

	@Override
	public Tuple removePrefix(int length) {
		return Tuple.getInstance(this.getSet().removePrefix(length), this.getValue().removePrefix(length));
	}

	@Override
	public Tuple removeSuffix(int length) {
		return Tuple.getInstance(this.getSet().removeSuffix(length), this.getValue().removeSuffix(length));
	}

	@Override
	public Tuple removeRange(int fromIndex, int toIndex) {
		return Tuple.getInstance(this.getSet().removeRange(fromIndex, toIndex), this.getValue().removeRange(fromIndex, toIndex));
	}

	@Override
	public Tuple removeAt(final int index) {
		return Tuple.getInstance(this.getSet().removeAt(index), this.getValue().removeAt(index));
	}

	@Override
	public Tuple insertAt(int index, Element element) {
		return Tuple.getInstance(this.getSet().insertAt(index, element.getSet()), this.getValue().insertAt(index, element));
	}

	@Override
	public Tuple add(Element element) {
		return Tuple.getInstance(this.getSet().add(element.getSet()), this.getValue().add(element));
	}

	@Override
	public Tuple append(ImmutableArray<Element> other) {
		return Tuple.getInstance(this.getValue().append(other));
	}

	@Override
	public Tuple replaceAt(int index, Element element) {
		return Tuple.getInstance(this.getSet().replaceAt(index, element.getSet()), this.getValue().replaceAt(index, element));
	}

	@Override
	public Tuple reverse() {
		return Tuple.getInstance(this.getSet().reverse(), this.getValue().reverse());
	}

	@Override
	public Tuple[] split(int... indices) {
		DenseArray<Element>[] elementArray = this.getValue().split(indices);
		Tuple[] result = new Tuple[elementArray.length];
		for (int i = 0; i < elementArray.length; i++) {
			result[i] = Tuple.getInstance(elementArray[i]);
		}
		return result;
	}

	@Override
	public Iterator<Element> iterator() {
		return this.getValue().iterator();
	}

	@Override
	protected String defaultToStringValue() {
		String result = "";
		String separator = "";
		for (Element element : this) {
			result = result + separator + element.toString();
			separator = ", ";
		}
		return result;
	}

	/**
	 * This is a static factory method to construct a composed element without the need of constructing the
	 * corresponding product or power group beforehand. The input elements are given as an array.
	 * <p>
	 * <p/>
	 * @param elements The array of input elements
	 * @return The corresponding tuple element
	 * @throws IllegalArgumentException if {@literal elements} is null or contains null
	 */
	public static Tuple getInstance(DenseArray<Element> elements) {
		if (elements == null || elements.getLength() < 0) {
			throw new IllegalArgumentException();
		}
		ProductSet productSet;
		if (elements.isUniform() && !elements.isEmpty()) {
			productSet = ProductSet.getInstance(elements.getFirst().getSet(), elements.getLength());
		} else {
			Set[] sets = new Set[elements.getLength()];
			for (int i : elements.getAllIndices()) {
				sets[i] = elements.getAt(i).getSet();
			}
			productSet = ProductSet.getInstance(sets);
		}
		return Tuple.getInstance(productSet, elements);
	}

	public static Tuple getInstance(Element... elements) {
		return Tuple.getInstance(DenseArray.getInstance(elements));
	}

	public static Tuple getInstance(Element element, int arity) {
		return Tuple.getInstance(DenseArray.getInstance(element, arity));
	}

	// helper method to distinguish between pairs, triples and tuples
	private static Tuple getInstance(ProductSet productSet, DenseArray<Element> elements) {
		if (elements.getLength() == 1) {
			return new Singleton(productSet, elements);
		}
		if (elements.getLength() == 2) {
			return new Pair(productSet, elements);
		}
		if (elements.getLength() == 3) {
			return new Triple(productSet, elements);
		}
		return new Tuple(productSet, elements);
	}

}
