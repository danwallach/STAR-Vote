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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.mixer.abstracts;

import ch.bfh.unicrypt.crypto.mixer.interfaces.Mixer;
import ch.bfh.unicrypt.random.classes.HybridRandomByteSequence;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.PermutationElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.PermutationGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductSet;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.PermutationFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.interfaces.Function;

/**
 *
 * @author philipp
 * @param <C>
 * @param <R>
 */
public abstract class AbstractMixer<C extends Set, R extends Set>
	   implements Mixer {

	final private int size;

	private Function shuffleFunction;
	private PermutationFunction permutationFunction;

	protected AbstractMixer(int size) {
		this.size = size;
	}

	@Override
	public final C getShuffleSpace() {
		return (C) this.getShuffleFunction().getCoDomain();
	}

	@Override
	public final R getRandomizationSpace() {
		return (R) ((ProductSet) this.getShuffleFunction().getDomain()).getAt(1);
	}

	@Override
	public final Function getShuffleFunction() {
		if (this.shuffleFunction == null) {
			this.shuffleFunction = this.abstractGetShuffleFunction();
		}
		return this.shuffleFunction;
	}

	@Override
	public final int getSize() {
		return this.size;
	}

	@Override
	public final PermutationFunction getPermutationFunction() {
		if (this.permutationFunction == null) {
			this.permutationFunction = PermutationFunction.getInstance(this.getShuffleSpace(), this.getSize());
		}
		return this.permutationFunction;
	}

	@Override
	public final Tuple shuffle(final Tuple elements) {
		return this.shuffle(elements, HybridRandomByteSequence.getInstance());
	}

	@Override
	public final Tuple shuffle(final Tuple elements, RandomByteSequence randomByteSequence) {
		PermutationElement permutation = PermutationGroup.getInstance(this.getSize()).getRandomElement(randomByteSequence);
		Tuple randomizations = this.generateRandomizations(randomByteSequence);
		return this.shuffle(elements, permutation, randomizations);
	}

	@Override
	public final Tuple shuffle(final Tuple elements, final PermutationElement permutation, final Tuple randomizations) {
		if (!this.getShufflesSpace().contains(elements) || !this.getRandomizationsSpace().contains(randomizations)
			   || permutation == null || permutation.getValue().getSize() != this.getSize()) {
			throw new IllegalArgumentException();
		}
		Element[] elementsPrime = new Element[this.getSize()];
		for (int i = 0; i < this.getSize(); i++) {
			elementsPrime[i] = this.getShuffleFunction().apply(elements.getAt(i), randomizations.getAt(i));
		}
		return this.getPermutationFunction().apply(Tuple.getInstance(elementsPrime), permutation);
	}

	@Override
	public final Tuple generateRandomizations() {
		return this.generateRandomizations(HybridRandomByteSequence.getInstance());
	}

	@Override
	public final Tuple generateRandomizations(final RandomByteSequence randomByteSequence) {
		if (randomByteSequence == null) {
			throw new IllegalArgumentException();
		}
		return this.defaultGenerateRandomizations(randomByteSequence);
	}

	@Override
	public PermutationGroup getPermutationGroup() {
		return PermutationGroup.getInstance(this.size);
	}

	protected Tuple defaultGenerateRandomizations(RandomByteSequence randomByteSequence) {
		return this.getRandomizationsSpace().getRandomElement(randomByteSequence);
	}

	protected abstract Function abstractGetShuffleFunction();

	private ProductSet getRandomizationsSpace() {
		return ProductSet.getInstance(this.getRandomizationSpace(), this.getSize());
	}

	private ProductSet getShufflesSpace() {
		return ProductSet.getInstance(this.getShuffleSpace(), this.getSize());
	}

}
