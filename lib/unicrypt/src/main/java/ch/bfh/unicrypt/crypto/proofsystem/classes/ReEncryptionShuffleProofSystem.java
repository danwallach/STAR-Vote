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

import ch.bfh.unicrypt.crypto.proofsystem.abstracts.AbstractShuffleProofSystem;
import ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.classes.RandomOracleChallengeGenerator;
import ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.classes.RandomOracleSigmaChallengeGenerator;
import ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.interfaces.ChallengeGenerator;
import ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.interfaces.SigmaChallengeGenerator;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.commitment.classes.GeneralizedPedersenCommitmentScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.encryption.interfaces.ReEncryptionScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZMod;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.PermutationElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.PermutationGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductSet;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Triple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.CyclicGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Group;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.abstracts.AbstractFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.ConvertFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.PermutationFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.ProductFunction;
import ch.bfh.unicrypt.random.classes.PseudoRandomOracle;
import ch.bfh.unicrypt.random.classes.ReferenceRandomByteSequence;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
import ch.bfh.unicrypt.random.interfaces.RandomOracle;
import java.math.BigInteger;

//
//
// @see [Wik09] Protocol 2: Commitment-Consistent Proof of a Shuffle
//
public class ReEncryptionShuffleProofSystem
	   extends AbstractShuffleProofSystem {

	final private ReEncryptionScheme encryptionScheme;
	final private Element encryptionPK;

	private ReEncryptionShuffleProofSystem(SigmaChallengeGenerator sigmaChallengeGenerator, ChallengeGenerator eValuesGenerator,
		   CyclicGroup cyclicGroup, int size, int kr, Tuple independentGenerators, ReEncryptionScheme encryptionScheme, Element encryptionPK) {

		super(sigmaChallengeGenerator, eValuesGenerator, cyclicGroup, size, kr, independentGenerators);

		this.encryptionScheme = encryptionScheme;
		this.encryptionPK = encryptionPK;
	}

	//===================================================================================
	// Interface implementation
	//
	// Private: (PermutationElement pi, PermutationCommitment-Randomizations sV, ReEncryption-Randomizations rV)
	@Override
	protected ProductGroup abstractGetPrivateInputSpace() {
		return ProductGroup.getInstance(PermutationGroup.getInstance(this.getSize()),
										ProductGroup.getInstance(this.getCyclicGroup().getZModOrder(), this.getSize()),
										ProductGroup.getInstance((Group) this.encryptionScheme.getRandomizationSpace(), this.getSize()));
	}

	// Public:  (PermutationCommitment cPiV, Input-Ciphertexts uV, Output-Ciphertexts uPrimeV)
	@Override
	protected ProductGroup abstractGetPublicInputSpace() {
		return ProductGroup.getInstance(ProductGroup.getInstance(this.getCyclicGroup(), this.getSize()),
										ProductGroup.getInstance((Group) this.encryptionScheme.getEncryptionSpace(), this.getSize()),
										ProductGroup.getInstance((Group) this.encryptionScheme.getEncryptionSpace(), this.getSize()));
	}

	// t: (Generalized Pedersen Commitemnt, Encryption)
	@Override
	public ProductGroup getCommitmentSpace() {
		return createCommitmentSpace(this.getCyclicGroup(), this.encryptionScheme);
	}

	// s: (r, w, ePrimeV)
	@Override
	public ProductGroup getResponseSpace() {
		return ProductGroup.getInstance((Group) this.encryptionScheme.getRandomizationSpace(),
										this.getCyclicGroup().getZModOrder(),
										ProductGroup.getInstance(ZMod.getInstance(BigInteger.valueOf(2).pow(this.getKe() + this.getKc() + this.getKr())), this.getSize()));
	}

	public ReEncryptionScheme getEncryptionScheme() {
		return this.encryptionScheme;
	}

	public Element getEncryptionPK() {
		return this.encryptionPK;
	}

	//===================================================================================
	// Helpers to create spaces
	//
	// (Generalized Pedersen Commitemnt, Encryption)
	private static ProductGroup createCommitmentSpace(CyclicGroup cyclicGroup, ReEncryptionScheme encryptionScheme) {
		return ProductGroup.getInstance(cyclicGroup, (Group) encryptionScheme.getEncryptionSpace());
	}

	// (Permutation Commitment, Input Ciphertexts, Output Ciphertexts)
	private static ProductGroup createChallengeGeneratorPublicInputSpace(CyclicGroup cyclicGroup, ReEncryptionScheme encryptionScheme, int size) {
		return ProductGroup.getInstance(ProductGroup.getInstance(cyclicGroup, size),
										ProductGroup.getInstance((Group) encryptionScheme.getEncryptionSpace(), size),
										ProductGroup.getInstance((Group) encryptionScheme.getEncryptionSpace(), size));
	}

	//===================================================================================
	// Generate and Validate
	//
	@Override
	protected Triple abstractGenerate(Triple privateInput, Tuple publicInput, RandomByteSequence randomByteSequence) {

		// Unfold private and public input
		final PermutationElement pi = (PermutationElement) privateInput.getFirst();
		final Tuple sV = (Tuple) privateInput.getSecond();
		final Tuple rV = (Tuple) privateInput.getThird();
		final Tuple uPrimeV = (Tuple) publicInput.getAt(2);
		final Tuple eV = (Tuple) this.getEValuesGenerator().generate(publicInput);

		// Compute private values for sigma proof
		final Element r = computeInnerProduct(rV, eV);
		final Element w = computeInnerProduct(sV, eV);
		Tuple ePrimeV = PermutationFunction.getInstance(eV.getSet()).apply(eV, pi);

		// Map ePrimeV to [0,...,2^(ke+kc+kr) - 1]^N
		ePrimeV = ProductFunction.getInstance(
			   ConvertFunction.getInstance(
					  ZMod.getInstance(BigInteger.valueOf(2).pow(this.getKe())),
					  ZMod.getInstance(BigInteger.valueOf(2).pow(this.getKe() + this.getKc() + this.getKr()))),
			   ePrimeV.getLength())
			   .apply(ePrimeV);

		// Create sigma proof
		PreimageProofFunction f = new PreimageProofFunction(this.getCyclicGroup(), this.getSize(), this.getResponseSpace(), this.getCommitmentSpace(), this.getIndependentGenerators(), uPrimeV, this.encryptionScheme, this.encryptionPK);
		final Element randomElement = this.getResponseSpace().getRandomElement(randomByteSequence);
		final Element commitment = f.apply(randomElement);                        // [3N+3]
		final Element challenge = this.getSigmaChallengeGenerator().generate(publicInput, commitment);
		final Element response = randomElement.apply(Tuple.getInstance(r, w, ePrimeV).selfApply(challenge));
		Triple preimageProof = (Triple) Triple.getInstance(commitment, challenge, response);
		//                                                                          --------
		return preimageProof;                                                     // [3N+3]
	}

	@Override
	protected boolean abstractVerify(Triple proof, Tuple publicInput) {

		// Unfold proof and public input
		final Tuple commitment = (Tuple) proof.getAt(0);
		final Tuple response = (Tuple) proof.getAt(2);
		final Tuple cPiV = (Tuple) publicInput.getFirst();
		final Tuple uV = (Tuple) publicInput.getAt(1);
		final Tuple uPrimeV = (Tuple) publicInput.getAt(2);
		final Tuple eV = (Tuple) this.getEValuesGenerator().generate(publicInput);

		// Compute image of preimage proof
		final Element[] ps = new Element[2];
		// - p_1 == c_pi^e                                                              [N]
		ps[0] = computeInnerProduct(cPiV, eV);
		// - p_2 = u                                                                   [2N]
		ps[1] = computeInnerProduct(uV, eV);

		final Tuple pV = Tuple.getInstance(ps);

		// 1. Verify preimage proof
		PreimageProofFunction f = new PreimageProofFunction(this.getCyclicGroup(), this.getSize(), this.getResponseSpace(), this.getCommitmentSpace(), this.getIndependentGenerators(), uPrimeV, this.encryptionScheme, this.encryptionPK);
		final Element challenge = this.getSigmaChallengeGenerator().generate(publicInput, commitment);
		final Element left = f.apply(response);                                   // [3N+3]
		final Element right = commitment.apply(pV.selfApply(challenge));          //    [3]
		//                                                                          --------
		return left.isEquivalent(right);                                          // [6N+6]
	}

	//===================================================================================
	// Nested class PreimageProofFunction
	//
	private class PreimageProofFunction
		   extends AbstractFunction<PreimageProofFunction, ProductGroup, Tuple, ProductGroup, Tuple> {

		private final CyclicGroup cyclicGroup;
		private final Tuple uPrimeV;
		private final ReEncryptionScheme encryptionScheme;
		private final Element encryptionPK;
		final GeneralizedPedersenCommitmentScheme gpcs;

		protected PreimageProofFunction(CyclicGroup cyclicGroup, int size, ProductGroup domain, ProductGroup coDomain, Tuple independentGenerators, Tuple uPrimeV, ReEncryptionScheme encryptionScheme, Element encryptionPK) {
			super(domain, coDomain);
			this.cyclicGroup = cyclicGroup;
			this.uPrimeV = uPrimeV;
			this.encryptionScheme = encryptionScheme;
			this.encryptionPK = encryptionPK;
			this.gpcs = GeneralizedPedersenCommitmentScheme.getInstance(independentGenerators.getAt(0), independentGenerators.extract(1, size));
		}

		@Override
		protected Tuple abstractApply(Tuple element, RandomByteSequence randomByteSequence) {

			// Unfold element
			final Element r = element.getAt(0);
			final Element w = element.getAt(1);
			final Tuple ePrimeV = (Tuple) element.getAt(2);

			// Result array
			final Element[] cV = new Element[2];

			// COMPUTE...
			// - Com(e', w)                              [n+1]
			ZMod zMod = this.cyclicGroup.getZModOrder();
			Element ePrimeVs[] = new Element[ePrimeV.getArity()];
			for (int i = 0; i < ePrimeV.getArity(); i++) {
				ePrimeVs[i] = zMod.getElement(((ZModElement) ePrimeV.getAt(i)).getValue().mod(zMod.getOrder()));
			}
			cV[0] = gpcs.commit(Tuple.getInstance(ePrimeVs), w);

			// - Prod(u'_i^(e'_i)) * Enc(1, -r)         [2n+2]
			final Element a = computeInnerProduct(this.uPrimeV, ePrimeV);
			final Element b = encryptionScheme.encrypt(encryptionPK, encryptionScheme.getMessageSpace().getIdentityElement(), r.invert());
			cV[1] = a.apply(b);

			//                                        ---------
			//                                          [3n+3]
			return Tuple.getInstance(cV);
		}

	}

	//===================================================================================
	// getInstance...
	//
	public static ReEncryptionShuffleProofSystem getInstance(CyclicGroup cyclicGroup, int size, ReEncryptionScheme encryptionScheme, Element encryptionPK) {
		return getInstance(
			   createNonInteractiveSigmaChallengeGenerator(cyclicGroup, encryptionScheme, size),
			   createNonInteractiveEValuesGenerator(cyclicGroup, encryptionScheme, size),
			   cyclicGroup, size, encryptionScheme, encryptionPK, DEFAULT_KR, ReferenceRandomByteSequence.getInstance());
	}

	public static ReEncryptionShuffleProofSystem getInstance(CyclicGroup cyclicGroup, int size, ReEncryptionScheme encryptionScheme,
		   Element encryptionPK, Element proverId, int ke, int kc, int kr, ReferenceRandomByteSequence rrbs) {
		return getInstance(
			   createNonInteractiveSigmaChallengeGenerator(cyclicGroup, encryptionScheme, size, kc, proverId),
			   createNonInteractiveEValuesGenerator(cyclicGroup, encryptionScheme, size, ke),
			   cyclicGroup, size, encryptionScheme, encryptionPK, kr, rrbs);
	}

	public static ReEncryptionShuffleProofSystem getInstance(Tuple independentGenerators, ReEncryptionScheme encryptionScheme, Element encryptionPK, Element proverId, int ke, int kc, int kr) {
		if (independentGenerators == null || independentGenerators.getArity() < 2 || !independentGenerators.getFirst().getSet().isCyclic()) {
			throw new IllegalArgumentException();
		}
		CyclicGroup cyclicGroup = (CyclicGroup) independentGenerators.getFirst().getSet();
		int size = independentGenerators.getArity() - 1;
		return getInstance(
			   createNonInteractiveSigmaChallengeGenerator(cyclicGroup, encryptionScheme, size, kc, proverId),
			   createNonInteractiveEValuesGenerator(cyclicGroup, encryptionScheme, size, ke),
			   independentGenerators,
			   encryptionScheme, encryptionPK, kr);
	}

	public static ReEncryptionShuffleProofSystem getInstance(SigmaChallengeGenerator sigmaChallengeGenerator,
		   ChallengeGenerator eValuesGenerator, CyclicGroup cyclicGroup, int size, ReEncryptionScheme encryptionScheme, Element encryptionPK) {
		return getInstance(sigmaChallengeGenerator, eValuesGenerator, cyclicGroup, size, encryptionScheme, encryptionPK, DEFAULT_KR, ReferenceRandomByteSequence.getInstance());
	}

	public static ReEncryptionShuffleProofSystem getInstance(SigmaChallengeGenerator sigmaChallengeGenerator,
		   ChallengeGenerator eValuesGenerator, CyclicGroup cyclicGroup, int size, ReEncryptionScheme encryptionScheme, Element encryptionPK, int kr, ReferenceRandomByteSequence referenceRandomByteSequence) {

		if (cyclicGroup == null || size < 1 || referenceRandomByteSequence == null) {
			throw new IllegalArgumentException();
		}
		Tuple independentGenerators = cyclicGroup.getIndependentGenerators(size, referenceRandomByteSequence);
		return getInstance(sigmaChallengeGenerator, eValuesGenerator, independentGenerators, encryptionScheme, encryptionPK, kr);
	}

	public static ReEncryptionShuffleProofSystem getInstance(SigmaChallengeGenerator sigmaChallengeGenerator,
		   ChallengeGenerator eValuesGenerator, Tuple independentGenerators, ReEncryptionScheme encryptionScheme, Element encryptionPK, int kr) {

		if (sigmaChallengeGenerator == null || eValuesGenerator == null || independentGenerators == null
			   || independentGenerators.getArity() < 2 || !independentGenerators.getSet().isUniform() || !independentGenerators.getFirst().getSet().isCyclic()
			   || encryptionScheme == null || !encryptionScheme.getKeyPairGenerator().getPublicKeySpace().contains(encryptionPK)
			   || !encryptionScheme.getEncryptionSpace().isGroup() || !encryptionScheme.getRandomizationSpace().isGroup()
			   || kr < 1) {
			throw new IllegalArgumentException();
		}
		CyclicGroup cyclicGroup = (CyclicGroup) independentGenerators.getFirst().getSet();
		int size = independentGenerators.getArity() - 1;
		if (!sigmaChallengeGenerator.getPublicInputSpace().isEquivalent(createChallengeGeneratorPublicInputSpace(cyclicGroup, encryptionScheme, size))
			   || !sigmaChallengeGenerator.getCommitmentSpace().isEquivalent(createCommitmentSpace(cyclicGroup, encryptionScheme))
			   || !eValuesGenerator.getInputSpace().isEquivalent(createChallengeGeneratorPublicInputSpace(cyclicGroup, encryptionScheme, size))
			   // TODO			   || !eValuesGenerator.getChallengeSpace().isEquivalent(ProductSet.getInstance(Z.getInstance(), size))
			   || !((ProductSet) eValuesGenerator.getChallengeSpace()).isUniform()) {
			throw new IllegalArgumentException();
		}

		return new ReEncryptionShuffleProofSystem(sigmaChallengeGenerator, eValuesGenerator, cyclicGroup, size, kr, independentGenerators, encryptionScheme, encryptionPK);
	}

	//===================================================================================
	// Service functions to create non-interactive SigmaChallengeGenerator and MultiChallengeGenerator
	//
	public static RandomOracleSigmaChallengeGenerator createNonInteractiveSigmaChallengeGenerator(final CyclicGroup cyclicGroup, final ReEncryptionScheme encryptionScheme, final int size) {
		return createNonInteractiveSigmaChallengeGenerator(cyclicGroup, encryptionScheme, size, cyclicGroup.getOrder().bitLength(), (Element) null, PseudoRandomOracle.getInstance());
	}

	public static RandomOracleSigmaChallengeGenerator createNonInteractiveSigmaChallengeGenerator(final CyclicGroup cyclicGroup, final ReEncryptionScheme encryptionScheme, final int size, final int kc, final Element proverId) {
		return createNonInteractiveSigmaChallengeGenerator(cyclicGroup, encryptionScheme, size, kc, proverId, PseudoRandomOracle.getInstance());
	}

	public static RandomOracleSigmaChallengeGenerator createNonInteractiveSigmaChallengeGenerator(final CyclicGroup cyclicGroup, final ReEncryptionScheme encryptionScheme, final int size, final int kc, final Element proverId, final RandomOracle randomOracle) {
		if (cyclicGroup == null || encryptionScheme == null || !encryptionScheme.getEncryptionSpace().isGroup() || size < 1 || kc < 1) {
			throw new IllegalArgumentException();
		}
		return RandomOracleSigmaChallengeGenerator.getInstance(createChallengeGeneratorPublicInputSpace(cyclicGroup, encryptionScheme, size),
															   createCommitmentSpace(cyclicGroup, encryptionScheme),
															   createChallengeSpace(kc),
															   proverId,
															   randomOracle);
	}

	public static RandomOracleChallengeGenerator createNonInteractiveEValuesGenerator(final CyclicGroup cyclicGroup, final ReEncryptionScheme encryptionScheme, final int size) {
		return createNonInteractiveEValuesGenerator(cyclicGroup, encryptionScheme, size, cyclicGroup.getOrder().bitLength(), PseudoRandomOracle.getInstance());
	}

	public static RandomOracleChallengeGenerator createNonInteractiveEValuesGenerator(final CyclicGroup cyclicGroup, final ReEncryptionScheme encryptionScheme, final int size, final int ke) {
		return createNonInteractiveEValuesGenerator(cyclicGroup, encryptionScheme, size, ke, PseudoRandomOracle.getInstance());
	}

	public static RandomOracleChallengeGenerator createNonInteractiveEValuesGenerator(final CyclicGroup cyclicGroup, final ReEncryptionScheme encryptionScheme, final int size, final int ke, final RandomOracle randomOracle) {
		if (cyclicGroup == null || encryptionScheme == null || !encryptionScheme.getEncryptionSpace().isGroup() || size < 1 || ke < 1) {
			throw new IllegalArgumentException();
		}
		return RandomOracleChallengeGenerator.getInstance(createChallengeGeneratorPublicInputSpace(cyclicGroup, encryptionScheme, size),
														  createEValuesGeneratorChallengeSpace(ke, size),
														  randomOracle);
	}

}
