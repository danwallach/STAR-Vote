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
import static ch.bfh.unicrypt.crypto.proofsystem.abstracts.AbstractShuffleProofSystem.DEFAULT_KR;
import ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.classes.RandomOracleChallengeGenerator;
import ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.classes.RandomOracleSigmaChallengeGenerator;
import ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.interfaces.ChallengeGenerator;
import ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.interfaces.SigmaChallengeGenerator;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.commitment.classes.GeneralizedPedersenCommitmentScheme;
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
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.abstracts.AbstractFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.ConvertFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.PermutationFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.ProductFunction;
import ch.bfh.unicrypt.random.classes.PseudoRandomOracle;
import ch.bfh.unicrypt.random.classes.ReferenceRandomByteSequence;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
import ch.bfh.unicrypt.random.interfaces.RandomOracle;
import java.math.BigInteger;

/**
 *
 * @author philipp
 */
public class IdentityShuffleProofSystem
	   extends AbstractShuffleProofSystem {

	final private CyclicGroup identityGroup;

	private IdentityShuffleProofSystem(SigmaChallengeGenerator sigmaChallengeGenerator, ChallengeGenerator eValuesGenerator,
		   CyclicGroup cyclicGroup, int size, int kr, Tuple independentGenerators, CyclicGroup identityGroup) {

		super(sigmaChallengeGenerator, eValuesGenerator, cyclicGroup, size, kr, independentGenerators);
		this.identityGroup = identityGroup;
	}

	//===================================================================================
	// Interface implementation
	//
	// Private: (PermutationElement pi, PermutationCommitment-Randomizations sV, alpha)
	@Override
	protected ProductGroup abstractGetPrivateInputSpace() {
		return ProductGroup.getInstance(PermutationGroup.getInstance(this.getSize()),
										ProductGroup.getInstance(this.getCyclicGroup().getZModOrder(), this.getSize()),
										this.identityGroup.getZModOrder());
	}

	// Public:  (PermutationCommitment cPiV, Input-Identites uV, Output-Identites uPrimeV, g_k-1, g_k)
	@Override
	protected ProductGroup abstractGetPublicInputSpace() {
		return ProductGroup.getInstance(ProductGroup.getInstance(this.getCyclicGroup(), this.getSize()),
										ProductGroup.getInstance(this.identityGroup, this.getSize()),
										ProductGroup.getInstance(this.identityGroup, this.getSize()),
										this.identityGroup,
										this.identityGroup);
	}

	// t: (Generalized Pedersen Commitemnt, Identity, alpha Commitment)
	@Override
	public ProductGroup getCommitmentSpace() {
		return createCommitmentSpace(this.getCyclicGroup(), this.identityGroup);
	}

	// s: (r, w, ePrimeV)
	@Override
	public ProductGroup getResponseSpace() {
		return ProductGroup.getInstance(this.identityGroup.getZModOrder(),
										this.getCyclicGroup().getZModOrder(),
										ProductSet.getInstance(ZMod.getInstance(BigInteger.valueOf(2).pow(this.getKe() + this.getKc() + this.getKr())), this.getSize()));
	}

	public CyclicGroup getIdentityGroup() {
		return this.identityGroup;
	}

	//===================================================================================
	// Helpers to create spaces
	//
	// (Generalized Pedersen Commitemnt, Identity, alpha Commitment)
	private static ProductGroup createCommitmentSpace(CyclicGroup cyclicGroup, CyclicGroup identityGroup) {
		return ProductGroup.getInstance(cyclicGroup, identityGroup, identityGroup);
	}

	// (Permutation Commitment, Input Identities, Output Identites, gk-1, gk)
	private static ProductGroup createChallengeGeneratorPublicInputSpace(CyclicGroup cyclicGroup, CyclicGroup identityGroup, int size) {
		return ProductGroup.getInstance(ProductGroup.getInstance(cyclicGroup, size),
										ProductGroup.getInstance(identityGroup, size),
										ProductGroup.getInstance(identityGroup, size),
										identityGroup,
										identityGroup);
	}

	//===================================================================================
	// Generate and Validate
	//
	@Override
	protected Triple abstractGenerate(Triple privateInput, Tuple publicInput, RandomByteSequence randomByteSequence) {

		// Unfold private and public input
		final PermutationElement pi = (PermutationElement) privateInput.getFirst();
		final Tuple sV = (Tuple) privateInput.getSecond();
		final Element alpha = privateInput.getThird();
		final Tuple uV = (Tuple) publicInput.getAt(1);
		final Tuple uPrimeV = (Tuple) publicInput.getAt(2);
		final Element gK_1 = publicInput.getAt(3);
		final Tuple eV = (Tuple) this.getEValuesGenerator().generate(publicInput);

		// Compute private values for sigma proof
		final Element w = computeInnerProduct(sV, eV);
		Tuple ePrimeV = PermutationFunction.getInstance(eV.getSet()).apply(eV, pi);

		// Map ePrimeV to [0,...,2^(ke+kc+kr) - 1]^N
		ePrimeV = ProductFunction.getInstance(
			   ConvertFunction.getInstance(
					  ZMod.getInstance(BigInteger.valueOf(2).pow(this.getKe())),
					  ZMod.getInstance(BigInteger.valueOf(2).pow(this.getKe() + this.getKc() + this.getKr()))),
			   ePrimeV.getLength())
			   .apply(ePrimeV);

		// Compute u                                                                    [N]
		final Element u = computeInnerProduct(uV, eV);

		// Create sigma proof
		PreimageProofFunction f = new PreimageProofFunction(this.getCyclicGroup(), this.getSize(), this.getResponseSpace(), this.getCommitmentSpace(), this.getIndependentGenerators(), u, uPrimeV, gK_1, this.identityGroup);
		final Element randomElement = this.getResponseSpace().getRandomElement(randomByteSequence);
		final Element commitment = f.apply(randomElement);                        // [2N+3]
		final ZModElement challenge = this.getSigmaChallengeGenerator().generate(publicInput, commitment);
		final Element response = randomElement.apply(Tuple.getInstance(alpha, w, ePrimeV).selfApply(challenge));
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
		final Element gK_1 = publicInput.getAt(3);
		final Element gK = publicInput.getAt(4);
		final Tuple eV = (Tuple) this.getEValuesGenerator().generate(publicInput);

		// Compute u                                                                    [N]
		final Element u = computeInnerProduct(uV, eV);

		// Compute image of preimage proof
		final Element[] ps = new Element[3];
		// - p_1 == c_pi^e                                                              [N]
		ps[0] = computeInnerProduct(cPiV, eV);
		// - p_2 = 1
		ps[1] = this.identityGroup.getIdentityElement();
		// - p_3 = g_k
		ps[2] = gK;

		final Tuple pV = Tuple.getInstance(ps);

		// 1. Verify preimage proof
		PreimageProofFunction f = new PreimageProofFunction(this.getCyclicGroup(), this.getSize(), this.getResponseSpace(), this.getCommitmentSpace(), this.getIndependentGenerators(), u, uPrimeV, gK_1, this.identityGroup);
		final Element challenge = this.getSigmaChallengeGenerator().generate(publicInput, commitment);
		final Element left = f.apply(response);                                   // [2N+3]
		final Element right = commitment.apply(pV.selfApply(challenge));          //    [3]
		//                                                                          --------
		return left.isEquivalent(right);                                          // [4N+6]
	}

	//===================================================================================
	// Nested class PreimageProofFunction
	//
	private class PreimageProofFunction
		   extends AbstractFunction<PreimageProofFunction, ProductGroup, Tuple, ProductGroup, Tuple> {

		private final CyclicGroup cyclicGroup;
		private final Element u;
		private final Tuple uPrimeV;
		private final Element gK_1;
		private final CyclicGroup identityGroup;
		final GeneralizedPedersenCommitmentScheme gpcs;

		protected PreimageProofFunction(CyclicGroup cyclicGroup, int size, ProductGroup domain, ProductGroup coDomain, Tuple independentGenerators, Element u, Tuple uPrimeV, Element gK_1, CyclicGroup identityGroup) {
			super(domain, coDomain);
			this.cyclicGroup = cyclicGroup;
			this.u = u;
			this.uPrimeV = uPrimeV;
			this.gK_1 = gK_1;
			this.identityGroup = identityGroup;
			this.gpcs = GeneralizedPedersenCommitmentScheme.getInstance(independentGenerators.getAt(0), independentGenerators.extract(1, size));
		}

		@Override
		protected Tuple abstractApply(Tuple element, RandomByteSequence randomByteSequence) {

			// Unfold element
			final Element alpha = element.getAt(0);
			final Element w = element.getAt(1);
			final Tuple ePrimeV = (Tuple) element.getAt(2);

			// Result array
			final Element[] cV = new Element[3];

			// COMPUTE...
			// - Com(e', w)                              [n+1]
			ZMod zMod = this.cyclicGroup.getZModOrder();
			Element ePrimeVs[] = new Element[ePrimeV.getArity()];
			for (int i = 0; i < ePrimeV.getArity(); i++) {
				ePrimeVs[i] = zMod.getElement(((ZModElement) ePrimeV.getAt(i)).getValue().mod(zMod.getOrder()));
			}
			cV[0] = this.gpcs.commit(Tuple.getInstance(ePrimeVs), w);

			// - Prod(u'_i^(e'_i)) * u^(-alpha)          [n+1]
			final Element a = computeInnerProduct(this.uPrimeV, ePrimeV);
			final Element b = this.u.selfApply(alpha.invert());
			cV[1] = a.apply(b);

			// - g_(k-1)^alpha                             [1]
			cV[2] = gK_1.selfApply(alpha);

			//                                        ---------
			//                                          [2n+3]
			return Tuple.getInstance(cV);
		}

	}

	//===================================================================================
	// getInstance...
	//
	public static IdentityShuffleProofSystem getInstance(CyclicGroup cyclicGroup, int size, CyclicGroup identityGroup) {
		return getInstance(
			   createRandomOracleSigmaChallengeGenerator(cyclicGroup, identityGroup, size),
			   createRandomOracleChallengeGenerator(cyclicGroup, identityGroup, size),
			   cyclicGroup, size, identityGroup, DEFAULT_KR, ReferenceRandomByteSequence.getInstance());
	}

	public static IdentityShuffleProofSystem getInstance(CyclicGroup cyclicGroup, int size, CyclicGroup identityGroup,
		   Element proverId, int ke, int kc, int kr, ReferenceRandomByteSequence rrbs) {
		return getInstance(
			   createRandomOracleSigmaChallengeGenerator(cyclicGroup, identityGroup, size, kc, proverId),
			   createRandomOracleChallengeGenerator(cyclicGroup, identityGroup, size, ke),
			   cyclicGroup, size, identityGroup, kr, rrbs);
	}

	public static IdentityShuffleProofSystem getInstance(Tuple independentGenerators, CyclicGroup identityGroup, Element proverId, int ke, int kc, int kr) {
		if (independentGenerators == null || independentGenerators.getArity() < 2 || !independentGenerators.getFirst().getSet().isCyclic()) {
			throw new IllegalArgumentException();
		}
		CyclicGroup cyclicGroup = (CyclicGroup) independentGenerators.getFirst().getSet();
		int size = independentGenerators.getArity() - 1;
		return getInstance(
			   createRandomOracleSigmaChallengeGenerator(cyclicGroup, identityGroup, size, kc, proverId),
			   createRandomOracleChallengeGenerator(cyclicGroup, identityGroup, size, ke),
			   independentGenerators, identityGroup, kr);
	}

	public static IdentityShuffleProofSystem getInstance(SigmaChallengeGenerator sigmaChallengeGenerator,
		   ChallengeGenerator eValuesGenerator, CyclicGroup cyclicGroup, int size, CyclicGroup identityGroup) {
		return getInstance(sigmaChallengeGenerator, eValuesGenerator, cyclicGroup, size, identityGroup, DEFAULT_KR, ReferenceRandomByteSequence.getInstance());
	}

	public static IdentityShuffleProofSystem getInstance(SigmaChallengeGenerator sigmaChallengeGenerator,
		   ChallengeGenerator eValuesGenerator, CyclicGroup cyclicGroup, int size, CyclicGroup identityGroup, int kr, ReferenceRandomByteSequence referenceRandomByteSequence) {

		if (cyclicGroup == null || size < 1 || referenceRandomByteSequence == null) {
			throw new IllegalArgumentException();
		}
		Tuple independentGenerators = cyclicGroup.getIndependentGenerators(size, referenceRandomByteSequence);
		return getInstance(sigmaChallengeGenerator, eValuesGenerator, independentGenerators, identityGroup, kr);
	}

	public static IdentityShuffleProofSystem getInstance(SigmaChallengeGenerator sigmaChallengeGenerator,
		   ChallengeGenerator eValuesGenerator, Tuple independentGenerators, CyclicGroup identityGroup, int kr) {

		if (sigmaChallengeGenerator == null || eValuesGenerator == null || independentGenerators == null
			   || independentGenerators.getArity() < 2 || !independentGenerators.getSet().isUniform() || !independentGenerators.getFirst().getSet().isCyclic()
			   || identityGroup == null || kr < 1) {
			throw new IllegalArgumentException();
		}
		CyclicGroup cyclicGroup = (CyclicGroup) independentGenerators.getFirst().getSet();
		int size = independentGenerators.getArity() - 1;
		if (!sigmaChallengeGenerator.getPublicInputSpace().isEquivalent(createChallengeGeneratorPublicInputSpace(cyclicGroup, identityGroup, size))
			   || !sigmaChallengeGenerator.getCommitmentSpace().isEquivalent(createCommitmentSpace(cyclicGroup, identityGroup))
			   || !eValuesGenerator.getInputSpace().isEquivalent(createChallengeGeneratorPublicInputSpace(cyclicGroup, identityGroup, size))
			   // TODO			   || !eValuesGenerator.getChallengeSpace().isEquivalent(ProductSet.getInstance(Z.getInstance(), size))
			   || !((ProductSet) eValuesGenerator.getChallengeSpace()).isUniform()) {
			throw new IllegalArgumentException();
		}

		return new IdentityShuffleProofSystem(sigmaChallengeGenerator, eValuesGenerator, cyclicGroup, size, kr, independentGenerators, identityGroup);
	}

	//===================================================================================
	// Service functions to create non-interactive SigmaChallengeGenerator and MultiChallengeGenerator
	//
	public static RandomOracleSigmaChallengeGenerator createRandomOracleSigmaChallengeGenerator(final CyclicGroup cyclicGroup, final CyclicGroup identityGroup, final int size) {
		return createRandomOracleSigmaChallengeGenerator(cyclicGroup, identityGroup, size, cyclicGroup.getOrder().bitLength(), (Element) null, PseudoRandomOracle.getInstance());
	}

	public static RandomOracleSigmaChallengeGenerator createRandomOracleSigmaChallengeGenerator(final CyclicGroup cyclicGroup, final CyclicGroup identityGroup, final int size, final int kc, final Element proverId) {
		return createRandomOracleSigmaChallengeGenerator(cyclicGroup, identityGroup, size, kc, proverId, PseudoRandomOracle.getInstance());
	}

	public static RandomOracleSigmaChallengeGenerator createRandomOracleSigmaChallengeGenerator(final CyclicGroup cyclicGroup, final CyclicGroup identityGroup, final int size, final int kc, final Element proverId, final RandomOracle randomOracle) {
		if (cyclicGroup == null || identityGroup == null || size < 1 || kc < 1) {
			throw new IllegalArgumentException();
		}
		return RandomOracleSigmaChallengeGenerator.getInstance(
			   createChallengeGeneratorPublicInputSpace(cyclicGroup, identityGroup, size),
			   createCommitmentSpace(cyclicGroup, identityGroup),
			   createChallengeSpace(kc),
			   proverId,
			   randomOracle);
	}

	public static RandomOracleChallengeGenerator createRandomOracleChallengeGenerator(final CyclicGroup cyclicGroup, final CyclicGroup identityGroup, final int size) {
		return createRandomOracleChallengeGenerator(cyclicGroup, identityGroup, size, cyclicGroup.getOrder().bitLength(), PseudoRandomOracle.getInstance());
	}

	public static RandomOracleChallengeGenerator createRandomOracleChallengeGenerator(final CyclicGroup cyclicGroup, final CyclicGroup identityGroup, final int size, final int ke) {
		return createRandomOracleChallengeGenerator(cyclicGroup, identityGroup, size, ke, PseudoRandomOracle.getInstance());
	}

	public static RandomOracleChallengeGenerator createRandomOracleChallengeGenerator(final CyclicGroup cyclicGroup, final CyclicGroup identityGroup, final int size, final int ke, final RandomOracle randomOracle) {
		if (cyclicGroup == null || identityGroup == null || size < 1 || ke < 1) {
			throw new IllegalArgumentException();
		}
		return RandomOracleChallengeGenerator.getInstance(createChallengeGeneratorPublicInputSpace(cyclicGroup, identityGroup, size),
														  createEValuesGeneratorChallengeSpace(ke, size),
														  randomOracle);
	}

}
