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
package lib.unicrypt.examples.src.main.java.ch.bfh.unicrypt.general;

import ch.bfh.unicrypt.Example;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.mixer.classes.ReEncryptionMixer;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.classes.RandomOracleChallengeGenerator;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.classes.RandomOracleSigmaChallengeGenerator;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.interfaces.ChallengeGenerator;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.interfaces.SigmaChallengeGenerator;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.proofsystem.classes.ElGamalEncryptionValidityProofSystem;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.proofsystem.classes.PermutationCommitmentProofSystem;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.proofsystem.classes.PreimageProofSystem;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.proofsystem.classes.ReEncryptionShuffleProofSystem;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.commitment.classes.PermutationCommitmentScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.encryption.classes.ElGamalEncryptionScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.Alphabet;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.additive.classes.ECZModPrime;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.concatenative.classes.StringMonoid;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Pair;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.PermutationElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductSet;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Subset;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Triple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.CyclicGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarMod;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModSafePrime;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.params.classes.SECECCParamsFp;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.GeneratorFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.interfaces.Function;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.random.classes.ReferenceRandomByteSequence;

/**
 *
 * @author philipp
 */
public class MixAndProofExample {

	// CHALLENGE GENERATOR
	public static void example1() {

		// Setup
		final GStarMod G_q = GStarModSafePrime.getInstance(167);
		final StringMonoid sm = StringMonoid.getInstance(Alphabet.BASE64);
		final Element proverId = sm.getElement("Prover1");

		// Challenge Generator
		//=====================
		// Create a non-interactive challenge generator that on input <StringElement> returns
		// 10 G_q elements
		ChallengeGenerator cg = RandomOracleChallengeGenerator.getInstance(sm, ProductSet.getInstance(G_q, 10));

		// Generate challenge
		Tuple challenges = (Tuple) cg.generate(sm.getElement("inputX"));
		Example.printLine("Challenges", challenges);

		// Sigma Challenge Generator
		//===========================
		// Proof function
		final Function f = GeneratorFunction.getInstance(G_q.getDefaultGenerator());
		// Public input
		final Element publicInput = G_q.getRandomElement();
		// Prover's commitment
		final Element commitment = f.apply(G_q.getZModOrder().getElement(3));

		// Create non-interactive sigma challenge generator for function <f> and prover <proverId>
		SigmaChallengeGenerator scg = RandomOracleSigmaChallengeGenerator.getInstance(f, proverId);

		// Generate challenge
		ZModElement challenge = scg.generate(publicInput, commitment);
		Example.printLine("Challenge", challenge);
	}

	// PREIMAGE PROOF GENERATOR
	public static void example2() {

		// Setup
		final GStarMod G_q = GStarModSafePrime.getInstance(167);
		final StringMonoid sm = StringMonoid.getInstance(Alphabet.BASE64);
		final Element proverId = sm.getElement("Prover1");

		// Create preimage proof generator for function f
		// f: Z_q -> G_q
		//    y = f(x) = 4^x
		//
		// - Create proof function
		GeneratorFunction f = GeneratorFunction.getInstance(G_q.getElement(4));
		// - Create sigma challenge generator
		SigmaChallengeGenerator scg = RandomOracleSigmaChallengeGenerator.getInstance(f, proverId);
		// - Create preimage proof generator
		PreimageProofSystem pg = PreimageProofSystem.getInstance(scg, f);

		// Private and public input
		Element privateInput = G_q.getZModOrder().getElement(3);
		Element publicInput = G_q.getElement(64);

		// Generate proof
		Triple proof = pg.generate(privateInput, publicInput);

		// Verify proof
		boolean v = pg.verify(proof, publicInput);
		Example.printLine("Proof is valid", v);
	}

	// ELGAMAL ENCRYPTION VALIDITY PROOF
	public static void example3() {

		// Setup
		final GStarMod G_q = GStarModSafePrime.getInstance(167);
		final StringMonoid sm = StringMonoid.getInstance(Alphabet.BASE64);
		final Element proverId = sm.getElement("Prover1");

		// Create ElGamal encryption validity proof
		//    Plaintexts: {4, 2, 8, 16}, g = 2, pk = 4
		//    Valid tuple: (2^3, 4^3*2) = (8, 128)
		//
		// - Create ElGamal encryption scheme
		ElGamalEncryptionScheme elGamalES = ElGamalEncryptionScheme.getInstance(G_q.getElement(2));
		Element publicKey = G_q.getElement(4);
		// - Create subset of valid plaintexts
		Subset plaintexts = Subset.getInstance(G_q, new Element[]{G_q.getElement(4), G_q.getElement(2), G_q.getElement(8), G_q.getElement(16)});
		// - Create ElGamal encryption validity proof generator (a non-inteactive sigma challenge generator
		//   is created implicitly
		ElGamalEncryptionValidityProofSystem pg = ElGamalEncryptionValidityProofSystem.getInstance(proverId, elGamalES, publicKey, plaintexts);

		// Public input
		Pair publicInput = Pair.getInstance(G_q.getElement(8), G_q.getElement(128));

		// Private input
		Element secret = G_q.getZModOrder().getElement(3);
		int index = 1;
		Pair privateInput = pg.createPrivateInput(secret, index);

		// Generate proof
		Triple proof = pg.generate(privateInput, publicInput);

		// Verify proof
		boolean v = pg.verify(proof, publicInput);
		Example.printLine("Proof is valid", v);
	}

	// MIXER
	public static void example4() {

		// Setup
		final GStarMod G_q = GStarModSafePrime.getInstance(167);

		// Create a few ciphertexts at random
		Tuple ciphertexts = ProductGroup.getInstance(ProductGroup.getInstance(G_q, 2), 5).getRandomElement();
		int size = ciphertexts.getArity();

		// Create ElGamal encryption scheme
		ElGamalEncryptionScheme elGamalES = ElGamalEncryptionScheme.getInstance(G_q.getElement(2));
		Element publicKey = G_q.getElement(4);

		// Create re-encryption mixer based on the ElGamal encryption scheme
		ReEncryptionMixer mixer = ReEncryptionMixer.getInstance(elGamalES, publicKey, size);

		// Create a random permutation
		PermutationElement permutation = mixer.getPermutationGroup().getRandomElement();

		// Create random randomizations (using a helper method of the mixer)
		Tuple randomizations = mixer.generateRandomizations();

		// Shuffle
		Tuple shuffledCiphertexts = mixer.shuffle(ciphertexts, permutation, randomizations);
		Example.printLine("Input ciphertexts ", ciphertexts);
		Example.printLine("Output ciphertexts", shuffledCiphertexts);
	}

	// COMPLETE SHUFFLE
	public static void example5() {

		// S E T U P
		//-----------
		// Create cyclic group
		final CyclicGroup G_q = GStarModSafePrime.getRandomInstance(160);
		// Create generator based on the default reference random byte sequence (-> independent generators)
		final Element g = G_q.getIndependentGenerator(0, ReferenceRandomByteSequence.getInstance());

		// Set size
		final int size = 10;

		// Create ElGamal encryption scheme
		ElGamalEncryptionScheme elGamalES = ElGamalEncryptionScheme.getInstance(g);
		Element publicKey = G_q.getRandomElement();

		// Create ciphertexts at random
		Tuple ciphertexts = ProductGroup.getInstance(elGamalES.getEncryptionSpace(), size).getRandomElement();

		// S H U F F L E
		//---------------
		// Create mixer
		ReEncryptionMixer mixer = ReEncryptionMixer.getInstance(elGamalES, publicKey, size);
		// Create a random permutation
		PermutationElement permutation = mixer.getPermutationGroup().getRandomElement();
		// Create random randomizations
		Tuple randomizations = mixer.generateRandomizations();
		// Shuffle
		Tuple shuffledCiphertexts = mixer.shuffle(ciphertexts, permutation, randomizations);

		// P R O O F
		//-----------
		//
		// 1. Permutation Proof
		//----------------------
		// Create permutation commitment
		PermutationCommitmentScheme pcs = PermutationCommitmentScheme.getInstance(G_q, size);
		Tuple permutationCommitmentRandomizations = pcs.getRandomizationSpace().getRandomElement();
		Tuple permutationCommitment = pcs.commit(permutation, permutationCommitmentRandomizations);

		// Create permutation commitment proof generator (a non-interactive challenge generator for the
		// e-values and a non-interactive sigma challenge generator are created implicitly, the independent
		// generators are created based on the default random reference byte sequence)
		PermutationCommitmentProofSystem pcpg = PermutationCommitmentProofSystem.getInstance(G_q, size);

		// Private and public input
		Pair privateInput1 = Pair.getInstance(permutation, permutationCommitmentRandomizations);
		Element publicInput1 = permutationCommitment;

		// Generate permutation commitment proof
		Pair proofPermutation = pcpg.generate(privateInput1, publicInput1);

		// 2. Shuffle Proof
		//------------------
		// Create shuffle proof generator (... -> see permutatin commitment proof generator)
		ReEncryptionShuffleProofSystem spg = ReEncryptionShuffleProofSystem.getInstance(G_q, size, elGamalES, publicKey);

		// Private and public input
		Triple privateInput2 = Triple.getInstance(permutation, permutationCommitmentRandomizations, randomizations);
		Triple publicInput2 = Triple.getInstance(permutationCommitment, ciphertexts, shuffledCiphertexts);

		// Generate shuffle proof
		Triple proofShuffle = spg.generate(privateInput2, publicInput2);

		// V E R I F Y
		//-------------
		// Verify permutation commitment proof
		boolean vPermutation = pcpg.verify(proofPermutation, publicInput1);

		// Verify shuffle proof
		boolean vShuffle = spg.verify(proofShuffle, publicInput2);

		// Verify equality of permutation commitments
		boolean vPermutationCommitments = publicInput1.isEquivalent(publicInput2.getFirst());

		if (vPermutation && vShuffle && vPermutationCommitments) {
			Example.printLine("Proof is valid!");
		} else {
			Example.printLine("Proof is NOT valid!");
		}
	}

	// COMPLETE SHUFFLE WITH DIFFERENT GROUPS AND EXPLICIT SECURITY PARAMETERS
	public static void example6() throws Exception {

		// S E T U P
		//-----------
		// Get default reference random byte sequence
		final ReferenceRandomByteSequence rrs = ReferenceRandomByteSequence.getInstance();

		// Set size
		final int size = 10;

		// Create cyclic group for commitments
		final ECZModPrime G_q_Com = ECZModPrime.getInstance(SECECCParamsFp.secp160r1);

		// Create independent generators
		final Tuple independentGenerators = G_q_Com.getIndependentGenerators(size, rrs);

		// Create cyclic group for encryption scheme
		final CyclicGroup G_q_Enc = GStarModSafePrime.getRandomInstance(160);

		// Create ElGamal encryption scheme
		ElGamalEncryptionScheme elGamalES = ElGamalEncryptionScheme.getInstance(G_q_Enc);
		Element publicKey = G_q_Enc.getRandomElement();

		// Create ciphertexts at random
		Tuple ciphertexts = ProductGroup.getInstance(elGamalES.getEncryptionSpace(), size).getRandomElement();

		// Create prover id
		final StringMonoid sm = StringMonoid.getInstance(Alphabet.BASE64);
		final Element proverId = sm.getElement("Mixer1");

		// S H U F F L E
		//---------------
		// Create mixer
		ReEncryptionMixer mixer = ReEncryptionMixer.getInstance(elGamalES, publicKey, size);
		// Create a random permutation
		PermutationElement permutation = mixer.getPermutationGroup().getRandomElement();
		// Create random randomizations
		Tuple randomizations = mixer.generateRandomizations();
		// Shuffle
		Tuple shuffledCiphertexts = mixer.shuffle(ciphertexts, permutation, randomizations);

		// P R O O F
		//-----------
		//
		// 1. Permutation Proof
		//----------------------
		// Create permutation commitment scheme based on the independent generators
		PermutationCommitmentScheme pcs = PermutationCommitmentScheme.getInstance(independentGenerators.getAt(0), independentGenerators.extractRange(1, size));
		// Create permutation commitment
		Tuple permutationCommitmentRandomizations = pcs.getRandomizationSpace().getRandomElement();
		Tuple permutationCommitment = pcs.commit(permutation, permutationCommitmentRandomizations);

		// Create permutation commitment proof generator based on the independent generators and with explicit
		// security parameters (a non-interactive challenge generator for the e-values and a non-interactive
		// sigma challenge generator are created implicitly)
		PermutationCommitmentProofSystem pcpg
			   = PermutationCommitmentProofSystem.getInstance(independentGenerators, proverId, 60, 60, 20);

		// Private and public input
		Pair privateInput1 = Pair.getInstance(permutation, permutationCommitmentRandomizations);
		Element publicInput1 = permutationCommitment;

		// Generate permutation commitment proof
		Pair proofPermutation = pcpg.generate(privateInput1, publicInput1);

		// 2. Shuffle Proof
		//------------------
		// Create shuffle proof generator (... -> see permutatin commitment proof generator)
		ReEncryptionShuffleProofSystem spg
			   = ReEncryptionShuffleProofSystem.getInstance(independentGenerators, elGamalES, publicKey, proverId, 60, 60, 20);

		// Private and public input
		Triple privateInput2 = Triple.getInstance(permutation, permutationCommitmentRandomizations, randomizations);
		Triple publicInput2 = Triple.getInstance(permutationCommitment, ciphertexts, shuffledCiphertexts);

		// Generate shuffle proof
		Triple proofShuffle = spg.generate(privateInput2, publicInput2);

		// V E R I F Y
		//-------------
		// Verify permutation commitment proof
		boolean vPermutation = pcpg.verify(proofPermutation, publicInput1);

		// Verify shuffle proof
		boolean vShuffle = spg.verify(proofShuffle, publicInput2);

		// Verify equality of permutation commitments
		boolean vPermutationCommitments = publicInput1.isEquivalent(publicInput2.getFirst());

		if (vPermutation && vShuffle && vPermutationCommitments) {
			Example.printLine("Proof is valid!");
		} else {
			Example.printLine("Proof is NOT valid!");
		}
	}

	public static void main(final String[] args) {
		Example.runExamples();
	}

}
