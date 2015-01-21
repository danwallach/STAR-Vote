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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.proofsystem.abstracts;

import ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.interfaces.SigmaChallengeGenerator;
import ch.bfh.unicrypt.crypto.proofsystem.interfaces.SigmaProofSystem;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZMod;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductSet;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Triple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.interfaces.Function;

public abstract class AbstractSigmaProofSystem<PRS extends Set, PRE extends Element, PUS extends Set, PUE extends Element, F extends Function>
	   extends AbstractProofSystem<PRS, PRE, PUS, PUE, ProductSet, Triple>
	   implements SigmaProofSystem {

	private final SigmaChallengeGenerator challengeGenerator;

	protected AbstractSigmaProofSystem(final SigmaChallengeGenerator challengeGenerator) {
		this.challengeGenerator = challengeGenerator;
	}

	@Override
	public final F getPreimageProofFunction() {
		return this.abstractGetPreimageProofFunction();
	}

	@Override
	public final SigmaChallengeGenerator getChallengeGenerator() {
		return this.challengeGenerator;
	}

	@Override
	public final Set getCommitmentSpace() {
		return this.getPreimageProofFunction().getCoDomain();
	}

	@Override
	public final ZMod getChallengeSpace() {
		return ZMod.getInstance(this.getPreimageProofFunction().getDomain().getMinimalOrder());
	}

	@Override
	public final Set getResponseSpace() {
		return this.getPreimageProofFunction().getDomain();
	}

	@Override
	public final PUE getCommitment(final Triple proof) {
		if (!this.getProofSpace().contains(proof)) {
			throw new IllegalArgumentException();
		}
		return (PUE) proof.getFirst();
	}

	@Override
	public final Element getChallenge(final Triple proof) {
		if (!this.getProofSpace().contains(proof)) {
			throw new IllegalArgumentException();
		}
		return proof.getSecond();
	}

	@Override
	public final PRE getResponse(final Triple proof) {
		if (!this.getProofSpace().contains(proof)) {
			throw new IllegalArgumentException();
		}
		return (PRE) proof.getThird();
	}

	protected abstract F abstractGetPreimageProofFunction();

	// Checks space equality of challenge generator and proof function
	protected static boolean checkSpaceEquality(final SigmaChallengeGenerator challengeGenerator, final Function proofFunction) {
		return (proofFunction == null || challengeGenerator == null
			   || !proofFunction.getCoDomain().isEquivalent(challengeGenerator.getPublicInputSpace())
			   || !proofFunction.getCoDomain().isEquivalent(challengeGenerator.getCommitmentSpace())
			   || !ZMod.getInstance(proofFunction.getDomain().getMinimalOrder()).isEquivalent(challengeGenerator.getChallengeSpace()));

	}

}
