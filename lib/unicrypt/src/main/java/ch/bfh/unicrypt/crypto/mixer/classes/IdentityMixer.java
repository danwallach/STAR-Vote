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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.mixer.classes;

import ch.bfh.unicrypt.crypto.mixer.abstracts.AbstractMixer;
import ch.bfh.unicrypt.random.classes.HybridRandomByteSequence;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZMod;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.PermutationElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.CyclicGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.SelfApplyFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.interfaces.Function;

/**
 * Calls self-apply on every identity.
 * <p>
 * @author philipp
 */
public class IdentityMixer
	   extends AbstractMixer<CyclicGroup, ZMod> {

	private final CyclicGroup cyclicGroup;

	private IdentityMixer(CyclicGroup cyclicGroup, int size) {
		super(size);
		this.cyclicGroup = cyclicGroup;
	}

	public Tuple shuffle(Tuple elements, PermutationElement permutation, Element randomization) {
		if (!this.getRandomizationSpace().contains(randomization)) {
			throw new IllegalArgumentException();
		}
		return this.shuffle(elements, permutation, this.createRandomizationTuple(randomization));
	}

	public Element generateRandomization() {
		return this.generateRandomization(HybridRandomByteSequence.getInstance());
	}

	public Element generateRandomization(RandomByteSequence randomByteSequence) {
		return this.getRandomizationSpace().getRandomElement(randomByteSequence);
	}

	public CyclicGroup getCyclicGroup() {
		return this.cyclicGroup;
	}

	private Tuple createRandomizationTuple(Element r) {
		Element[] randomizations = new Element[this.getSize()];
		for (int i = 0; i < randomizations.length; i++) {
			randomizations[i] = r;
		}
		return Tuple.getInstance(randomizations);
	}

	@Override
	protected Tuple defaultGenerateRandomizations(RandomByteSequence randomByteSequence) {
		Element r = this.generateRandomization(randomByteSequence);
		return this.createRandomizationTuple(r);
	}

	@Override
	protected Function abstractGetShuffleFunction() {
		return SelfApplyFunction.getInstance(this.getCyclicGroup());
	}

	public static IdentityMixer getInstance(CyclicGroup cyclicGroup, int size) {
		if (cyclicGroup == null || size < 0) {
			throw new IllegalArgumentException();
		}
		return new IdentityMixer(cyclicGroup, size);
	}

}
