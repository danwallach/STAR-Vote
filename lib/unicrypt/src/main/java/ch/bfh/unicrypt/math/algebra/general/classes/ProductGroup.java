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
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Group;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Set;

/**
 *
 * @author rolfhaenni
 */
public class ProductGroup
	   extends ProductMonoid
	   implements Group<DenseArray<Element>> {

	protected ProductGroup(DenseArray<Set> sets) {
		super(sets);
	}

	@Override
	public Group getFirst() {
		return (Group) super.getFirst();
	}

	@Override
	public Group getLast() {
		return (Group) super.getLast();
	}

	@Override
	public Group getAt(int index) {
		return (Group) super.getAt(index);
	}

	@Override
	public Group getAt(int... indices) {
		return (Group) super.getAt(indices);
	}

	@Override
	public ProductGroup removeAt(final int index) {
		return (ProductGroup) super.removeAt(index);
	}

	public ProductGroup insertAt(final int index, Group group) {
		return (ProductGroup) super.insertAt(index, group);
	}

	public ProductGroup replaceAt(final int index, Group group) {
		return (ProductGroup) super.replaceAt(index, group);
	}

	public ProductGroup add(Group group) {
		return (ProductGroup) super.add(group);
	}

	public ProductGroup append(ProductGroup productGroup) {
		return (ProductGroup) super.append(productGroup);
	}

	@Override
	public ProductGroup extract(int offset, int length) {
		return (ProductGroup) super.extract(offset, length);
	}

	@Override
	public ProductGroup extractPrefix(int length) {
		return (ProductGroup) super.extractPrefix(length);
	}

	@Override
	public ProductGroup extractSuffix(int length) {
		return (ProductGroup) super.extractSuffix(length);
	}

	@Override
	public ProductGroup extractRange(int fromIndex, int toIndex) {
		return (ProductGroup) super.extractRange(fromIndex, toIndex);
	}

	@Override
	public ProductGroup remove(int offset, int length) {
		return (ProductGroup) super.remove(offset, length);
	}

	@Override
	public ProductGroup removePrefix(int length) {
		return (ProductGroup) super.removePrefix(length);
	}

	@Override
	public ProductGroup removeSuffix(int length) {
		return (ProductGroup) super.removeSuffix(length);
	}

	@Override
	public ProductGroup removeRange(int fromIndex, int toIndex) {
		return (ProductGroup) super.removeRange(fromIndex, toIndex);
	}

	@Override
	public ProductGroup reverse() {
		return (ProductGroup) super.reverse();
	}

	@Override
	public ProductGroup[] split(int... indices) {
		return (ProductGroup[]) super.split(indices);
	}

	@Override
	public final Tuple invert(Element element) {
		if (!this.contains(element)) {
			throw new IllegalArgumentException();
		}
		Tuple tuple = (Tuple) element;
		final Element[] invertedElements = new Element[this.getArity()];
		for (int i : this.getAllIndices()) {
			invertedElements[i] = tuple.getAt(i).invert();
		}
		return this.abstractGetElement(DenseArray.getInstance(invertedElements));
	}

	@Override
	public final Tuple applyInverse(Element element1, Element element2) {
		return this.apply(element1, this.invert(element2));
	}
//
	// STATIC FACTORY METHODS
	//

//	/**
//	 * This is a static factory method to construct a composed group without calling respective constructors. The input
//	 * groups are given as an array.
//	 * <p/>
//	 * @param groups The array of input groups
//	 * @return The corresponding composed group
//	 * @throws IllegalArgumentException if {@literal groups} is null or contains null
//	 */
//	public static ProductGroup getInstance(final Group... groups) {
//		if (groups == null) {
//			throw new IllegalArgumentException();
//		}
//		boolean isCyclic = false;
//		if (groups.length > 0) {
//			boolean uniform = true;
//			Group first = groups[0];
//			for (final Group group : groups) {
//				if (group == null) {
//					throw new IllegalArgumentException();
//				}
//				if (!group.isEquivalent(first)) {
//					uniform = false;
//				}
//				isCyclic = isCyclic && group.isCyclic();
//			}
//			if (uniform) {
//				return ProductGroup.getInstance(first, groups.length);
//			}
//		}
//		if (isCyclic) {
//			CyclicGroup[] cGroups = Arrays.copyOf(groups, groups.length, CyclicGroup[].class);
//			return ProductCyclicGroup.getInstance(cGroups);
//		}
//		return new ProductGroup(groups);
//	}
//
//	public static ProductGroup getInstance(final Group group, int arity) {
//		if ((group == null) || (arity < 0)) {
//			throw new IllegalArgumentException();
//		}
//		if (group.isCyclic() && arity < 2) {
//			return ProductCyclicGroup.getInstance((CyclicGroup) group, arity);
//		}
//		if (arity == 0) {
//			return new ProductGroup(new Group[]{});
//		}
//		return new ProductGroup(group, arity);
//	}
}
