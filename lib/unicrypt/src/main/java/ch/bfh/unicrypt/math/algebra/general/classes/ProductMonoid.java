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
import ch.bfh.unicrypt.math.algebra.general.interfaces.Monoid;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import java.math.BigInteger;

/**
 *
 * @author rolfhaenni
 */
public class ProductMonoid
	   extends ProductSemiGroup
	   implements Monoid<DenseArray<Element>> {

	private Tuple identityElement;

	protected ProductMonoid(DenseArray<Set> sets) {
		super(sets);
	}

	@Override
	public Monoid getFirst() {
		return (Monoid) super.getFirst();
	}

	@Override
	public Monoid getLast() {
		return (Monoid) super.getLast();
	}

	@Override
	public Monoid getAt(int index) {
		return (Monoid) super.getAt(index);
	}

	@Override
	public Monoid getAt(int... indices) {
		return (Monoid) super.getAt(indices);
	}

	@Override
	public ProductMonoid removeAt(final int index) {
		return (ProductMonoid) super.removeAt(index);
	}

	public ProductMonoid insertAt(final int index, Monoid monoid) {
		return (ProductMonoid) super.insertAt(index, monoid);
	}

	public ProductMonoid replaceAt(final int index, Monoid monoid) {
		return (ProductMonoid) super.replaceAt(index, monoid);
	}

	public ProductMonoid add(Monoid monoid) {
		return (ProductMonoid) super.add(monoid);
	}

	public ProductMonoid append(ProductMonoid monoid) {
		return (ProductMonoid) super.append(monoid);
	}

	@Override
	public ProductMonoid extract(int offset, int length) {
		return (ProductMonoid) super.extract(offset, length);
	}

	@Override
	public ProductMonoid extractPrefix(int length) {
		return (ProductMonoid) super.extractPrefix(length);
	}

	@Override
	public ProductMonoid extractSuffix(int length) {
		return (ProductMonoid) super.extractSuffix(length);
	}

	@Override
	public ProductMonoid extractRange(int fromIndex, int toIndex) {
		return (ProductMonoid) super.extractRange(fromIndex, toIndex);
	}

	@Override
	public ProductMonoid remove(int offset, int length) {
		return (ProductMonoid) super.remove(offset, length);
	}

	@Override
	public ProductMonoid removePrefix(int length) {
		return (ProductMonoid) super.removePrefix(length);
	}

	@Override
	public ProductMonoid removeSuffix(int length) {
		return (ProductMonoid) super.removeSuffix(length);
	}

	@Override
	public ProductMonoid removeRange(int fromIndex, int toIndex) {
		return (ProductMonoid) super.removeRange(fromIndex, toIndex);
	}

	@Override
	public ProductMonoid reverse() {
		return (ProductMonoid) super.reverse();
	}

	@Override
	public ProductMonoid[] split(int... indices) {
		return (ProductMonoid[]) super.split(indices);
	}

	@Override
	public Tuple getIdentityElement() {
		if (this.identityElement == null) {
			final Element[] identityElements = new Element[this.getArity()];
			for (int i : this.getAllIndices()) {
				identityElements[i] = this.getAt(i).getIdentityElement();
			}
			this.identityElement = this.abstractGetElement(DenseArray.getInstance(identityElements));
		}
		return this.identityElement;
	}

	@Override
	public boolean isIdentityElement(Element element) {
		return this.areEquivalent(element, this.getIdentityElement());
	}

	@Override
	public Tuple defaultApply(final Iterable<Element> elements) {
		if (!elements.iterator().hasNext()) {
			return this.getIdentityElement();
		}
		return super.defaultApply(elements);
	}

	@Override
	protected Tuple defaultMultiSelfApply(final Element[] elements, BigInteger[] amounts) {
		if (elements.length == 0) {
			return this.getIdentityElement();
		}
		return super.defaultMultiSelfApply(elements, amounts);
	}

}
