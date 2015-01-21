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
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.encryption.classes.ElGamalEncryptionScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZMod;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Pair;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductSet;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Subset;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.CyclicGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.ApplyFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.CompositeFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.InvertFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.SelectionFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.SharedDomainFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.interfaces.Function;
import ch.bfh.unicrypt.random.classes.PseudoRandomOracle;
import ch.bfh.unicrypt.random.interfaces.RandomOracle;

public class ElGamalEncryptionValidityProofSystem
	   extends AbstractSigmaSetMembershipProofSystem<ProductGroup, Pair> {

	private final ElGamalEncryptionScheme elGamalES;
	private final Element publicKey;

	protected ElGamalEncryptionValidityProofSystem(final SigmaChallengeGenerator challengeGenerator, final ElGamalEncryptionScheme elGamalES, Element publicKey, final Subset plaintexts) {
		super(challengeGenerator, plaintexts);
		this.elGamalES = elGamalES;
		this.publicKey = publicKey;
	}

	public static ElGamalEncryptionValidityProofSystem getInstance(final ElGamalEncryptionScheme elGamalES, final Element publicKey, final Subset plaintexts) {
		return ElGamalEncryptionValidityProofSystem.getInstance((Element) null, elGamalES, publicKey, plaintexts);
	}

	public static ElGamalEncryptionValidityProofSystem getInstance(final Element proverId, final ElGamalEncryptionScheme elGamalES, final Element publicKey, final Subset plaintexts) {
		SigmaChallengeGenerator challengeGenerator = ElGamalEncryptionValidityProofSystem.createNonInteractiveChallengeGenerator(elGamalES, plaintexts.getOrder().intValue(), proverId);
		return ElGamalEncryptionValidityProofSystem.getInstance(challengeGenerator, elGamalES, publicKey, plaintexts);
	}

	public static ElGamalEncryptionValidityProofSystem getInstance(final SigmaChallengeGenerator challengeGenerator, final ElGamalEncryptionScheme elGamalES, final Element publicKey, final Subset plaintexts) {
		if (challengeGenerator == null || elGamalES == null || publicKey == null || !elGamalES.getCyclicGroup().contains(publicKey)
			   || plaintexts == null || plaintexts.getOrder().intValue() < 1) {
			throw new IllegalArgumentException();
		}
		final Set codomain = ProductGroup.getInstance(elGamalES.getEncryptionFunction().getCoDomain(), plaintexts.getOrder().intValue());
		if (!codomain.isEquivalent(challengeGenerator.getPublicInputSpace()) || !codomain.isEquivalent(challengeGenerator.getCommitmentSpace())
			   || !ZMod.getInstance(elGamalES.getCyclicGroup().getOrder()).isEquivalent(challengeGenerator.getChallengeSpace())) {
			throw new IllegalArgumentException("Spaces of challenge generator don't match!");
		}
		return new ElGamalEncryptionValidityProofSystem(challengeGenerator, elGamalES, publicKey, plaintexts);
	}

	@Override
	protected Function abstractGetSetMembershipFunction() {
		return this.elGamalES.getEncryptionFunction().partiallyApply(this.publicKey, 0);
	}

	@Override
	protected Function abstractGetDeltaFunction() {
		final CyclicGroup elGamalCyclicGroup = this.elGamalES.getCyclicGroup();
		final ProductSet deltaFunctionDomain = ProductSet.getInstance(elGamalCyclicGroup, this.getSetMembershipProofFunction().getCoDomain());
		return SharedDomainFunction.getInstance(
			   SelectionFunction.getInstance(deltaFunctionDomain, 1, 0),
			   CompositeFunction.getInstance(
					  SharedDomainFunction.getInstance(
							 SelectionFunction.getInstance(deltaFunctionDomain, 1, 1),
							 CompositeFunction.getInstance(
									SelectionFunction.getInstance(deltaFunctionDomain, 0),
									InvertFunction.getInstance(elGamalCyclicGroup))),
					  ApplyFunction.getInstance(elGamalCyclicGroup)));
	}

	public static RandomOracleSigmaChallengeGenerator createNonInteractiveChallengeGenerator(final ElGamalEncryptionScheme elGamalES, final int numberOfPlaintexts) {
		return ElGamalEncryptionValidityProofSystem.createNonInteractiveChallengeGenerator(elGamalES, numberOfPlaintexts, PseudoRandomOracle.DEFAULT);
	}

	public static RandomOracleSigmaChallengeGenerator createNonInteractiveChallengeGenerator(final ElGamalEncryptionScheme elGamalES, final int numberOfPlaintexts, final Element proverId) {
		return ElGamalEncryptionValidityProofSystem.createNonInteractiveChallengeGenerator(elGamalES, numberOfPlaintexts, proverId, PseudoRandomOracle.DEFAULT);
	}

	public static RandomOracleSigmaChallengeGenerator createNonInteractiveChallengeGenerator(final ElGamalEncryptionScheme elGamalES, final int numberOfPlaintexts, final RandomOracle randomOracle) {
		return ElGamalEncryptionValidityProofSystem.createNonInteractiveChallengeGenerator(elGamalES, numberOfPlaintexts, (Element) null, randomOracle);
	}

	public static RandomOracleSigmaChallengeGenerator createNonInteractiveChallengeGenerator(final ElGamalEncryptionScheme elGamalES, final int numberOfPlaintexts, final Element proverId, final RandomOracle randomOracle) {
		if (elGamalES == null || numberOfPlaintexts < 1 || randomOracle == null) {
			throw new IllegalArgumentException();
		}
		final ProductGroup codomain = ProductGroup.getInstance((ProductGroup) elGamalES.getEncryptionFunction().getCoDomain(), numberOfPlaintexts);
		return RandomOracleSigmaChallengeGenerator.getInstance(codomain, codomain, ZMod.getInstance(elGamalES.getCyclicGroup().getOrder()), proverId, randomOracle);
	}

}
