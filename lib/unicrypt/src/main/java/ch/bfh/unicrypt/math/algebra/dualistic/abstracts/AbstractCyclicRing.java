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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.abstracts;

import ch.bfh.unicrypt.math.algebra.dualistic.interfaces.CyclicRing;
import ch.bfh.unicrypt.math.algebra.dualistic.interfaces.DualisticElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductSet;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.unicrypt.random.classes.HybridRandomByteSequence;
import ch.bfh.unicrypt.random.classes.ReferenceRandomByteSequence;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This abstract class provides a basis implementation for objects of type {@link CyclicRing}.
 * <p>
 * @param <E> Generic type of the elements of this cyclic ring
 * @param <V> Generic type of values stored in the elements of this cyclic ring
 * @author rolfhaenni
 */
public abstract class AbstractCyclicRing<E extends DualisticElement<V>, V extends Object>
	   extends AbstractRing<E, V>
	   implements CyclicRing<V> {

	private E defaultGenerator;

	public AbstractCyclicRing(Class<? extends Object> valueClass) {
		super(valueClass);
	}

	@Override
	public final E getDefaultGenerator() {
		if (this.defaultGenerator == null) {
			this.defaultGenerator = this.abstractGetDefaultGenerator();
		}
		return this.defaultGenerator;
	}

	@Override
	public final E getRandomGenerator() {
		return this.defaultGetRandomGenerator(HybridRandomByteSequence.getInstance());
	}

	@Override
	public final E getRandomGenerator(RandomByteSequence randomByteSequence) {
		if (randomByteSequence == null) {
			throw new IllegalArgumentException();
		}
		return this.defaultGetRandomGenerator(randomByteSequence);
	}

	@Override
	public final E getIndependentGenerator(int index) {
		return this.getIndependentGenerator(index, (ReferenceRandomByteSequence) null);
	}

	@Override
	public final E getIndependentGenerator(int index, ReferenceRandomByteSequence referenceRandomByteSequence) {
		return (E) this.getIndependentGenerators(index, referenceRandomByteSequence).getAt(index);
	}

	@Override
	public final Tuple getIndependentGenerators(int maxIndex) {
		return this.getIndependentGenerators(maxIndex, (ReferenceRandomByteSequence) null);
	}

	@Override
	public final Tuple getIndependentGenerators(int maxIndex, ReferenceRandomByteSequence referenceRandomByteSequence) {
		return this.getIndependentGenerators(0, maxIndex, referenceRandomByteSequence);
	}

	@Override
	public final Tuple getIndependentGenerators(int minIndex, int maxIndex) {
		return this.getIndependentGenerators(minIndex, maxIndex, (ReferenceRandomByteSequence) null);
	}

	@Override
	public final Tuple getIndependentGenerators(int minIndex, int maxIndex, ReferenceRandomByteSequence referenceRandomByteSequence) {
		if (minIndex < 0 || maxIndex < minIndex) {
			throw new IndexOutOfBoundsException();
		}
		if (referenceRandomByteSequence == null) {
			referenceRandomByteSequence = ReferenceRandomByteSequence.getInstance();
		}
		// The following line is necessary for creating a generic array
		Element[] generators = new Element[maxIndex - minIndex + 1];
		for (int i = 0; i <= maxIndex; i++) {
			E generator = this.defaultGetIndependentGenerator(referenceRandomByteSequence);
			if (i >= minIndex) {
				generators[i - minIndex] = generator;
			}
		}
		return ProductSet.getInstance(this, generators.length).getElement(generators);
	}

	@Override
	public final boolean isGenerator(Element element) {
		if (!this.contains(element)) {
			throw new IllegalArgumentException();
		}
		return this.abstractIsGenerator((E) element);
	}

	// see Handbook of Applied Cryptography, Algorithm 4.80 and Note 4.81
	protected E defaultGetRandomGenerator(RandomByteSequence randomByteSequence) {
		E element;
		do {
			element = this.getRandomElement(randomByteSequence);
		} while (!this.isGenerator(element));
		return element;
	}

	protected E defaultGetIndependentGenerator(ReferenceRandomByteSequence referenceRandomByteSequence) {
		return this.defaultGetRandomGenerator(referenceRandomByteSequence);
	}

	@Override
	protected Iterator<E> defaultGetIterator(final BigInteger maxCounter) {
		final AbstractCyclicRing<E, V> cyclicRing = this;
		return new Iterator<E>() {
			BigInteger counter = BigInteger.ZERO;
			E currentElement = cyclicRing.getIdentityElement();

			@Override
			public boolean hasNext() {
				return counter.compareTo(maxCounter) < 0;
			}

			@Override
			public E next() {
				if (this.hasNext()) {
					this.counter = this.counter.add(BigInteger.ONE);
					E nextElement = this.currentElement;
					this.currentElement = cyclicRing.apply(this.currentElement, cyclicRing.getDefaultGenerator());
					return nextElement;
				}
				throw new NoSuchElementException();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	//
	// The following protected abstract method must be implemented in every direct sub-class
	//
	protected abstract E abstractGetDefaultGenerator();

	protected abstract boolean abstractIsGenerator(E element);

}
