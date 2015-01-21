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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.proofsystem.classes;

import ch.bfh.unicrypt.crypto.proofsystem.abstracts.AbstractSigmaSetMembershipProofSystem;
import ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.classes.RandomOracleSigmaChallengeGenerator;
import ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.interfaces.SigmaChallengeGenerator;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.commitment.classes.PedersenCommitmentScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZMod;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductSet;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Subset;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.CyclicGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Group;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.ApplyFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.CompositeFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.GeneratorFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.InvertFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.SelectionFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.SharedDomainFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.interfaces.Function;
import ch.bfh.unicrypt.random.classes.PseudoRandomOracle;
import ch.bfh.unicrypt.random.interfaces.RandomOracle;

public class PedersenCommitmentValidityProofSystem
	   extends AbstractSigmaSetMembershipProofSystem<CyclicGroup, Element> {

	private final PedersenCommitmentScheme pedersenCS;

	protected PedersenCommitmentValidityProofSystem(final SigmaChallengeGenerator challengeGenerator, final PedersenCommitmentScheme pedersenCS, final Subset messages) {
		super(challengeGenerator, messages);
		this.pedersenCS = pedersenCS;
	}

	public static PedersenCommitmentValidityProofSystem getInstance(final PedersenCommitmentScheme pedersenCS, final Subset messages) {
		return PedersenCommitmentValidityProofSystem.getInstance(pedersenCS, messages, (Element) null);
	}

	public static PedersenCommitmentValidityProofSystem getInstance(final PedersenCommitmentScheme pedersenCS, final Subset messages, final Element proverId) {
		SigmaChallengeGenerator challengeGenerator = PedersenCommitmentValidityProofSystem.createNonInteractiveChallengeGenerator(pedersenCS, messages.getOrder().intValue(), proverId);
		return PedersenCommitmentValidityProofSystem.getInstance(challengeGenerator, pedersenCS, messages);
	}

	public static PedersenCommitmentValidityProofSystem getInstance(final SigmaChallengeGenerator challengeGenerator, final PedersenCommitmentScheme pedersenCS, final Subset messages) {
		if (challengeGenerator == null || pedersenCS == null || messages == null || messages.getOrder().intValue() < 1) {
			throw new IllegalArgumentException();
		}

		final Set codomain = ProductGroup.getInstance(pedersenCS.getCommitmentFunction().getCoDomain(), messages.getOrder().intValue());
		if (!codomain.isEquivalent(challengeGenerator.getPublicInputSpace()) || !codomain.isEquivalent(challengeGenerator.getCommitmentSpace())
			   || !ZMod.getInstance(pedersenCS.getCyclicGroup().getOrder()).isEquivalent(challengeGenerator.getChallengeSpace())) {
			throw new IllegalArgumentException("Spaces of challenge generator don't match!");
		}
		return new PedersenCommitmentValidityProofSystem(challengeGenerator, pedersenCS, messages);
	}

	@Override
	protected Function abstractGetSetMembershipFunction() {
		return this.pedersenCS.getCommitmentFunction();
	}

	@Override
	protected Function abstractGetDeltaFunction() {
		final ProductSet deltaFunctionDomain = ProductSet.getInstance(this.pedersenCS.getMessageSpace(), this.getSetMembershipProofFunction().getCoDomain());
		final Function deltaFunction = CompositeFunction.getInstance(
			   SharedDomainFunction.getInstance(
					  SelectionFunction.getInstance(deltaFunctionDomain, 1),
					  CompositeFunction.getInstance(
							 SelectionFunction.getInstance(deltaFunctionDomain, 0),
							 GeneratorFunction.getInstance(this.pedersenCS.getMessageGenerator()),
							 InvertFunction.getInstance(this.pedersenCS.getCyclicGroup()))),
			   ApplyFunction.getInstance(this.pedersenCS.getCyclicGroup()));
		return deltaFunction;
	}

	public static RandomOracleSigmaChallengeGenerator createNonInteractiveChallengeGenerator(final PedersenCommitmentScheme pedersenCS, final int numberOfMessages) {
		return PedersenCommitmentValidityProofSystem.createNonInteractiveChallengeGenerator(pedersenCS, numberOfMessages, PseudoRandomOracle.DEFAULT);
	}

	public static RandomOracleSigmaChallengeGenerator createNonInteractiveChallengeGenerator(final PedersenCommitmentScheme pedersenCS, final int numberOfMessages, final Element proverId) {
		return PedersenCommitmentValidityProofSystem.createNonInteractiveChallengeGenerator(pedersenCS, numberOfMessages, proverId, PseudoRandomOracle.DEFAULT);
	}

	public static RandomOracleSigmaChallengeGenerator createNonInteractiveChallengeGenerator(final PedersenCommitmentScheme pedersenCS, final int numberOfMessages, final RandomOracle randomOracle) {
		return PedersenCommitmentValidityProofSystem.createNonInteractiveChallengeGenerator(pedersenCS, numberOfMessages, (Element) null, randomOracle);
	}

	public static RandomOracleSigmaChallengeGenerator createNonInteractiveChallengeGenerator(final PedersenCommitmentScheme pedersenCS, final int numberOfMessages, final Element proverId, final RandomOracle randomOracle) {
		if (pedersenCS == null || numberOfMessages < 1 || randomOracle == null) {
			throw new IllegalArgumentException();
		}
		final Group codomain = ProductGroup.getInstance((Group) pedersenCS.getCommitmentFunction().getCoDomain(), numberOfMessages);
		return RandomOracleSigmaChallengeGenerator.getInstance(codomain, codomain, ZMod.getInstance(pedersenCS.getCyclicGroup().getOrder()), proverId, randomOracle);

	}

}
