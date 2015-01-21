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

import ch.bfh.unicrypt.helper.converter.interfaces.BigIntegerConverter;
import ch.bfh.unicrypt.math.algebra.general.abstracts.AbstractSet;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 *
 * @author Rolf Haenni <rolf.haenni@bfh.ch>
 */
public class Subset
	   extends AbstractSet<Element<Object>, Object> {

	private final Set superSet;
	private final LinkedHashSet<Element<Object>> elementSet;

	protected Subset(Set superSet, LinkedHashSet<Element<Object>> elements) {
		super(Element.class);
		this.superSet = superSet;
		this.elementSet = elements;
	}

	public Set getSuperset() {
		return this.superSet;
	}

	@Override
	protected Iterator<Element<Object>> defaultGetIterator(final BigInteger maxCounter) {
		// maxCounter is ignored here, because the subset are usually very small
		return this.elementSet.iterator();
	}

	@Override
	protected boolean defaultContains(final Element element) {
		return this.elementSet.contains(element);
	}

	@Override
	protected boolean defaultIsEquivalent(Set set) {
		Subset other = (Subset) set;
		return this.superSet.isEquivalent(other.superSet) && this.elementSet.equals(other.elementSet);
	}

	@Override
	protected boolean abstractContains(Object value) {
		for (Element element : this.elementSet) {
			if (element.getValue().equals(value)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected Element abstractGetElement(Object value) {
		for (Element element : this.elementSet) {
			if (element.getValue().equals(value)) {
				return element;
			}
		}
		throw new IllegalArgumentException();
	}

	@Override
	protected BigIntegerConverter<Object> abstractGetBigIntegerConverter() {
		return this.superSet.getBigIntegerConverter();
	}

	@Override
	protected BigInteger abstractGetOrder() {
		return BigInteger.valueOf(this.elementSet.size());
	}

	@Override
	protected Element abstractGetRandomElement(RandomByteSequence randomByteSequence) {
		int randomIndex = randomByteSequence.getRandomNumberGenerator().nextInteger(this.elementSet.size() - 1);
		int i = 0;
		for (Element element : this.getElements()) {
			if (i == randomIndex) {
				return element;
			}
			i++;
		}
		throw new UnknownError();
	}

	@Override
	protected boolean abstractEquals(Set set) {
		Subset other = (Subset) set;
		return this.superSet.equals(other.superSet) && this.elementSet.equals(other.elementSet);
	}

	@Override
	protected int abstractHashCode() {
		int hash = 7;
		hash = 47 * hash + this.superSet.hashCode();
		hash = 47 * hash + this.elementSet.hashCode();
		return hash;
	}

	public static Subset getInstance(Set superSet, Element... elements) {
		if (superSet == null || elements == null) {
			throw new IllegalArgumentException();
		}
		// A LinkedHashSet retains the order
		LinkedHashSet<Element<Object>> hashSet = new LinkedHashSet<Element<Object>>();
		for (Element element : elements) {
			if (element == null || !superSet.contains(element)) {
				throw new IllegalArgumentException();
			}
			hashSet.add(element);
		}
		return new Subset(superSet, hashSet);
	}

}
