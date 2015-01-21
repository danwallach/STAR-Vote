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
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.CyclicGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.ApplyFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.CompositeFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.GeneratorFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.ProductFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.interfaces.Function;
import ch.bfh.unicrypt.random.classes.ReferenceRandomByteSequence;

public class PedersenCommitmentScheme
	   extends AbstractRandomizedCommitmentScheme<ZMod, ZModElement, CyclicGroup, Element, ZMod> {

	private final CyclicGroup cyclicGroup;
	private final Element randomizationGenerator;
	private final Element messageGenerator;

	protected PedersenCommitmentScheme(CyclicGroup cyclicGroup, Element randomizationGenerator, Element messageGenerator) {
		super(cyclicGroup.getZModOrder(), cyclicGroup, cyclicGroup.getZModOrder());
		this.cyclicGroup = cyclicGroup;
		this.randomizationGenerator = randomizationGenerator;
		this.messageGenerator = messageGenerator;
	}

	public final CyclicGroup getCyclicGroup() {
		return this.cyclicGroup;
	}

	public final Element getRandomizationGenerator() {
		return this.randomizationGenerator;
	}

	public final Element getMessageGenerator() {
		return this.messageGenerator;
	}

	@Override
	protected Function abstractGetCommitmentFunction() {
		return CompositeFunction.getInstance(
			   ProductFunction.getInstance(GeneratorFunction.getInstance(this.messageGenerator),
										   GeneratorFunction.getInstance(this.randomizationGenerator)),
			   ApplyFunction.getInstance(this.getCyclicGroup()));
	}

	public static PedersenCommitmentScheme getInstance(CyclicGroup cyclicGroup) {
		return PedersenCommitmentScheme.getInstance(cyclicGroup, ReferenceRandomByteSequence.getInstance());
	}

	public static PedersenCommitmentScheme getInstance(CyclicGroup cyclicGroup, ReferenceRandomByteSequence referenceRandomByteSequence) {
		if (cyclicGroup == null || referenceRandomByteSequence == null) {
			throw new IllegalArgumentException();
		}
		// TODO: is this thread safe???
		referenceRandomByteSequence.reset();
		Tuple generators = cyclicGroup.getIndependentGenerators(2, referenceRandomByteSequence);
		return new PedersenCommitmentScheme(cyclicGroup, generators.getAt(0), generators.getAt(1));
	}

	public static PedersenCommitmentScheme getInstance(Element generator1, Element generator2) {
		if (generator1 == null || generator2 == null || !generator1.getSet().isEquivalent(generator2.getSet())
			   || !generator1.getSet().isCyclic() || !generator1.isGenerator() || !generator2.isGenerator()) {
			throw new IllegalArgumentException();
		}
		return new PedersenCommitmentScheme((CyclicGroup) generator1.getSet(), generator1, generator2);
	}

}
