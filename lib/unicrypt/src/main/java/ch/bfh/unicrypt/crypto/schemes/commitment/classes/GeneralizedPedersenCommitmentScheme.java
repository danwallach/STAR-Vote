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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.commitment.classes;

import ch.bfh.unicrypt.crypto.schemes.commitment.abstracts.AbstractRandomizedCommitmentScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZMod;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductSet;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.CyclicGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.ApplyFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.CompositeFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.GeneratorFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.ProductFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.interfaces.Function;
import ch.bfh.unicrypt.random.classes.ReferenceRandomByteSequence;

public class GeneralizedPedersenCommitmentScheme
	   extends AbstractRandomizedCommitmentScheme<ProductGroup, Tuple, CyclicGroup, Element, ZMod> {

	private final CyclicGroup cyclicGroup;
	private final Element randomizationGenerator;
	private final Tuple messageGenerators;
	private final int size;

	protected GeneralizedPedersenCommitmentScheme(CyclicGroup cyclicGroup, int size, Element randomizationGenerator, Tuple messageGenerators) {
		super(ProductSet.getInstance(cyclicGroup.getZModOrder(), size), cyclicGroup, cyclicGroup.getZModOrder());
		this.cyclicGroup = cyclicGroup;
		this.size = size;
		this.randomizationGenerator = randomizationGenerator;
		this.messageGenerators = messageGenerators;
	}

	public final CyclicGroup getCyclicGroup() {
		return this.cyclicGroup;
	}

	public final Element getRandomizationGenerator() {
		return this.randomizationGenerator;
	}

	public final Tuple getMessageGenerators() {
		return this.messageGenerators;
	}

	public int getSize() {
		return this.size;
	}

	@Override
	protected Function abstractGetCommitmentFunction() {
		final Function[] generatorFunctions = new Function[this.size];
		for (int i = 0; i < this.size; i++) {
			generatorFunctions[i] = GeneratorFunction.getInstance(this.messageGenerators.getAt(i));
		}
		return CompositeFunction.getInstance(
			   ProductFunction.getInstance(
					  CompositeFunction.getInstance(ProductFunction.getInstance(generatorFunctions),
													ApplyFunction.getInstance(this.cyclicGroup, this.size)),
					  GeneratorFunction.getInstance(this.randomizationGenerator)),
			   ApplyFunction.getInstance(this.cyclicGroup));
	}

	public static GeneralizedPedersenCommitmentScheme getInstance(final CyclicGroup cyclicGroup, final int size) {
		return GeneralizedPedersenCommitmentScheme.getInstance(cyclicGroup, size, ReferenceRandomByteSequence.getInstance());
	}

	public static GeneralizedPedersenCommitmentScheme getInstance(final CyclicGroup cyclicGroup, final int size, ReferenceRandomByteSequence referenceRandomByteSequence) {
		if (cyclicGroup == null || size < 1 || referenceRandomByteSequence == null) {
			throw new IllegalArgumentException();
		}
		// TODO: is this thread safe???
		referenceRandomByteSequence.reset();
		Element randomizationGenerator = cyclicGroup.getIndependentGenerator(0, referenceRandomByteSequence);
		Tuple messageGenerators = cyclicGroup.getIndependentGenerators(1, size, referenceRandomByteSequence);
		return new GeneralizedPedersenCommitmentScheme(cyclicGroup, size, randomizationGenerator, messageGenerators);
	}

	/**
	 * It is not explicitely checked, whether the passed generators are indeed generators (and independent)! It is
	 * assumed that this is given from the context.
	 * <p>
	 * @param randomizationGenerator
	 * @param messageGenerators
	 * @return
	 */
	public static GeneralizedPedersenCommitmentScheme getInstance(final Element randomizationGenerator, final Tuple messageGenerators) {
		if (randomizationGenerator == null || messageGenerators == null || !randomizationGenerator.getSet().isCyclic()
			   || messageGenerators.getArity() < 1 || !messageGenerators.getSet().isUniform() || !randomizationGenerator.getSet().isEquivalent(messageGenerators.getFirst().getSet())) {
			throw new IllegalArgumentException();
		}
		CyclicGroup cycicGroup = (CyclicGroup) randomizationGenerator.getSet();
		int size = messageGenerators.getArity();
		return new GeneralizedPedersenCommitmentScheme(cycicGroup, size, randomizationGenerator, messageGenerators);
	}

}
