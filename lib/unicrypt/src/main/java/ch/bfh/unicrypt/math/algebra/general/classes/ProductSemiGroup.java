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
import ch.bfh.unicrypt.helper.iterable.IterableArray;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.unicrypt.math.algebra.general.interfaces.SemiGroup;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import java.math.BigInteger;

/**
 *
 * @author rolfhaenni
 */
public class ProductSemiGroup
	   extends ProductSet
	   implements SemiGroup<DenseArray<Element>> {

	protected ProductSemiGroup(DenseArray<Set> sets) {
		super(sets);
	}

	@Override
	public SemiGroup getAt(int index) {
		return (SemiGroup) super.getAt(index);
	}

	@Override
	public SemiGroup getAt(int... indices) {
		return (SemiGroup) super.getAt(indices);
	}

	@Override
	public SemiGroup getFirst() {
		return (SemiGroup) super.getFirst();
	}

	@Override
	public SemiGroup getLast() {
		return (SemiGroup) super.getLast();
	}

	@Override
	public ProductSemiGroup removeAt(final int index) {
		return (ProductSemiGroup) super.removeAt(index);
	}

	public ProductSemiGroup insertAt(final int index, SemiGroup semiGroup) {
		return (ProductSemiGroup) super.insertAt(index, semiGroup);
	}

	public ProductSemiGroup replaceAt(final int index, SemiGroup semiGroup) {
		return (ProductSemiGroup) super.replaceAt(index, semiGroup);
	}

	public ProductSemiGroup add(SemiGroup semiGroup) {
		return (ProductSemiGroup) super.add(semiGroup);
	}

	public ProductSemiGroup append(ProductSemiGroup productSemiGroup) {
		return (ProductSemiGroup) super.append(productSemiGroup);
	}

	@Override
	public ProductSemiGroup extract(int offset, int length) {
		return (ProductSemiGroup) super.extract(offset, length);
	}

	@Override
	public ProductSemiGroup extractPrefix(int length) {
		return (ProductSemiGroup) super.extractPrefix(length);
	}

	@Override
	public ProductSemiGroup extractSuffix(int length) {
		return (ProductSemiGroup) super.extractSuffix(length);
	}

	@Override
	public ProductSemiGroup extractRange(int fromIndex, int toIndex) {
		return (ProductSemiGroup) super.extractRange(fromIndex, toIndex);
	}

	@Override
	public ProductSemiGroup remove(int offset, int length) {
		return (ProductSemiGroup) super.remove(offset, length);
	}

	@Override
	public ProductSemiGroup removePrefix(int length) {
		return (ProductSemiGroup) super.removePrefix(length);
	}

	@Override
	public ProductSemiGroup removeSuffix(int length) {
		return (ProductSemiGroup) super.removeSuffix(length);
	}

	@Override
	public ProductSemiGroup removeRange(int fromIndex, int toIndex) {
		return (ProductSemiGroup) super.removeRange(fromIndex, toIndex);
	}

	@Override
	public ProductSemiGroup reverse() {
		return (ProductSemiGroup) super.reverse();
	}

	@Override
	public ProductSemiGroup[] split(int... indices) {
		return (ProductSemiGroup[]) super.split(indices);
	}

	@Override
	public final Tuple apply(Element element1, Element element2) {
		if (!this.contains(element1) || !this.contains(element2)) {
			System.out.println("ERROR");
			System.out.println(this);
			System.out.println(element1);
			System.out.println(element2);
			throw new IllegalArgumentException();
		}
		return this.abstractApply((Tuple) element1, (Tuple) element2);
	}

	@Override
	public final Tuple apply(final Element... elements) {
		if (elements == null) {
			throw new IllegalArgumentException();
		}
		return this.defaultApply(IterableArray.getInstance(elements));
	}

	@Override
	public final Tuple apply(Iterable<Element> elements) {
		if (elements == null) {
			throw new IllegalArgumentException();
		}
		return this.defaultApply(elements);
	}

	@Override
	public final Tuple selfApply(Element element, BigInteger amount) {
		if (!this.contains(element) || amount == null) {
			throw new IllegalArgumentException();
		}
		Tuple tuple = (Tuple) element;
		final Element[] results = new Element[this.getArity()];
		for (int i : this.getAllIndices()) {
			results[i] = tuple.getAt(i).selfApply(amount);
		}
		return this.abstractGetElement(DenseArray.getInstance(results));
	}

	@Override
	public final Tuple selfApply(Element element, Element<BigInteger> amount) {
		if (amount == null) {
			throw new IllegalArgumentException();
		}
		return this.selfApply(element, amount.getValue());
	}

	@Override
	public final Tuple selfApply(Element element, int amount) {
		return this.selfApply(element, BigInteger.valueOf(amount));
	}

	@Override
	public final Tuple selfApply(Element element) {
		return this.apply(element, element);
	}

	@Override
	public final Tuple multiSelfApply(final Element[] elements, final BigInteger[] amounts) {
		if ((elements == null) || (amounts == null) || (elements.length != amounts.length)) {
			throw new IllegalArgumentException();
		}
		return this.defaultMultiSelfApply(elements, amounts);
	}

	protected Tuple abstractApply(Tuple tuple1, Tuple tuple2) {
		final Element[] results = new Element[this.getArity()];
		for (int i : this.getAllIndices()) {
			results[i] = tuple1.getAt(i).apply(tuple2.getAt(i));
		}
		return this.abstractGetElement(DenseArray.getInstance(results));
	}

	protected Tuple defaultApply(final Iterable<Element> elements) {
		if (!elements.iterator().hasNext()) {
			throw new IllegalArgumentException();
		}
		Element result = null;
		for (Element element : elements) {
			if (result == null) {
				result = element;
			} else {
				result = this.apply(result, element);
			}
		}
		return (Tuple) result;
	}

	protected Tuple defaultMultiSelfApply(final Element[] elements, final BigInteger[] amounts) {
		if (elements.length == 0) {
			throw new IllegalArgumentException();
		}
		Element[] results = new Element[elements.length];
		for (int i = 0; i < elements.length; i++) {
			results[i] = this.selfApply(elements[i], amounts[i]);
		}
		return this.apply(results);
	}

}
