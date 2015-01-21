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
package lib.unicrypt.examples.src.main.java.ch.bfh.unicrypt.crypto.proofsystem;

import ch.bfh.unicrypt.Example;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.mixer.classes.ReEncryptionMixer;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.interfaces.ChallengeGenerator;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.interfaces.SigmaChallengeGenerator;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.proofsystem.classes.PermutationCommitmentProofSystem;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.proofsystem.classes.ReEncryptionShuffleProofSystem;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.commitment.classes.PermutationCommitmentScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.encryption.classes.ElGamalEncryptionScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.Alphabet;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.concatenative.classes.StringMonoid;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Pair;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.PermutationElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.PermutationGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Triple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.CyclicGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModSafePrime;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.random.classes.PseudoRandomOracle;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.random.classes.ReferenceRandomByteSequence;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.random.interfaces.RandomOracle;

/**
 *
 * @author philipp
 */
public class CompleteShuffleExample {

	//
	// As tight as possible!
	//
	public static void example1() {

		// P R E P A R E
		//---------------
		// Create cyclic group and get generator
		final CyclicGroup G_q = GStarModSafePrime.getRandomInstance(160);
		final Element g = G_q.getIndependentGenerator(0, ReferenceRandomByteSequence.getInstance());

		// Set size
		final int size = 10;

		// Create ElGamal keys and encryption system
		ElGamalEncryptionScheme es = ElGamalEncryptionScheme.getInstance(g);
		Pair keys = es.getKeyPairGenerator().generateKeyPair();
		Element publicKey = keys.getSecond();

		// Create ciphertexts
		Tuple messages = ProductGroup.getInstance(G_q, size).getRandomElement();
		Element[] ciphertextArray = new Element[size];
		for (int i = 0; i < size; i++) {
			ciphertextArray[i] = es.encrypt(publicKey, messages.getAt(i));
		}
		Tuple ciphertexts = Tuple.getInstance(ciphertextArray);

		// S H U F F L E
		//---------------
		System.out.println("Shuffle...");
		// Create mixer
		ReEncryptionMixer mixer = ReEncryptionMixer.getInstance(es, publicKey, size);
		// Create permutation
		PermutationElement permutation = PermutationGroup.getInstance(size).getRandomElement();
		// Create randomizations
		Tuple randomizations = mixer.generateRandomizations();
		// Shuffle
		Tuple shuffledCiphertexts = mixer.shuffle(ciphertexts, permutation, randomizations);

		// P R O O F
		//-----------
		//
		// 1. Permutation Proof
		//----------------------
		System.out.println("Permutation Proof...");
		// Create permutation commitment
		PermutationCommitmentScheme pcs = PermutationCommitmentScheme.getInstance(G_q, size);
		Tuple permutationCommitmentRandomizations = pcs.getRandomizationSpace().getRandomElement();
		Tuple permutationCommitment = pcs.commit(permutation, permutationCommitmentRandomizations);

		// Create permutation commitment proof generator
		PermutationCommitmentProofSystem pcps = PermutationCommitmentProofSystem.getInstance(G_q, size);

		// Create permutation commitment proof
		Pair proofPermutation = pcps.generate(Pair.getInstance(permutation, permutationCommitmentRandomizations), permutationCommitment);

		// 2. Shuffle Proof
		//------------------
		System.out.println("Shuffle Proof...");
		// Create shuffle proof generator
		ReEncryptionShuffleProofSystem sps = ReEncryptionShuffleProofSystem.getInstance(G_q, size, es, publicKey);

		// Compose private and public input
		Triple privateInput = Triple.getInstance(permutation, permutationCommitmentRandomizations, randomizations);
		Triple publicInput = Triple.getInstance(permutationCommitment, ciphertexts, shuffledCiphertexts);

		// Create shuffle proof
		Triple proofShuffle = sps.generate(privateInput, publicInput);

		// V E R I F Y
		//-------------
		System.out.print("Verify... ");
		// Verify permutation commitment proof
		boolean vPermutation = pcps.verify(proofPermutation, permutationCommitment);

		// Verify shuffle proof
		boolean vShuffle = sps.verify(proofShuffle, publicInput);

		// Verify equality of permutation commitments
		boolean vPermutationCommitments = permutationCommitment.isEquivalent(publicInput.getFirst());

		if (vPermutation && vShuffle && vPermutationCommitments) {
			System.out.println("Proof is valid!");
		} else {
			System.out.println("Proof is NOT valid!");
		}
	}

	public static void example2() {

		// P R E P A R E
		//---------------
		// Create random oracle and random reference string
		final RandomOracle ro = PseudoRandomOracle.DEFAULT;
		final ReferenceRandomByteSequence rrs = ReferenceRandomByteSequence.getInstance();

		// Create cyclic group and get generator
		final CyclicGroup G_q = GStarModSafePrime.getRandomInstance(160);
		final Element g = G_q.getIndependentGenerator(0, rrs);

		// Set size, prover-id and security parameters
		final int size = 10;
		final Element proverId = StringMonoid.getInstance(Alphabet.BASE64).getElement("Shuffler");
		final int ke = 60;
		final int kc = 60;
		final int kr = 20;

		// Create ElGamal keys and encryption system
		ElGamalEncryptionScheme es = ElGamalEncryptionScheme.getInstance(g);
		Pair keys = es.getKeyPairGenerator().generateKeyPair();
		Element publicKey = keys.getSecond();

		// Create ciphertexts
		Tuple messages = ProductGroup.getInstance(G_q, size).getRandomElement();
		Element[] ciphertextArray = new Element[size];
		for (int i = 0; i < size; i++) {
			ciphertextArray[i] = es.encrypt(publicKey, messages.getAt(i));
		}
		Tuple ciphertexts = Tuple.getInstance(ciphertextArray);

		// S H U F F L E
		//---------------
		System.out.println("Shuffle...");
		// Create mixer
		ReEncryptionMixer mixer = ReEncryptionMixer.getInstance(es, publicKey, size);
		// Create permutation
		PermutationElement permutation = PermutationGroup.getInstance(size).getRandomElement();
		// Create randomizations
		Tuple randomizations = mixer.generateRandomizations();
		// Shuffle
		Tuple shuffledCiphertexts = mixer.shuffle(ciphertexts, permutation, randomizations);

		// P R O O F
		//-----------
		//
		// 1. Permutation Proof
		//----------------------
		System.out.println("Permutation Proof...");
		// Create permutation commitment
		PermutationCommitmentScheme pcs = PermutationCommitmentScheme.getInstance(G_q, size, rrs);
		Tuple permutationCommitmentRandomizations = pcs.getRandomizationSpace().getRandomElement();
		Tuple permutationCommitment = pcs.commit(permutation, permutationCommitmentRandomizations);

		// Create permutation commitment proof generator
		SigmaChallengeGenerator scg = PermutationCommitmentProofSystem.createNonInteractiveSigmaChallengeGenerator(G_q, size, kc, proverId, ro);
		ChallengeGenerator ecg = PermutationCommitmentProofSystem.createNonInteractiveEValuesGenerator(G_q, size, ke, ro);
		PermutationCommitmentProofSystem pcps = PermutationCommitmentProofSystem.getInstance(scg, ecg, G_q, size, kr, rrs);

		// Create permutation commitment proof
		Pair proofPermutation = pcps.generate(Pair.getInstance(permutation, permutationCommitmentRandomizations), permutationCommitment);

		// 2. Shuffle Proof
		//------------------
		System.out.println("Shuffle Proof...");
		// Create shuffle proof generator
		SigmaChallengeGenerator scgS = ReEncryptionShuffleProofSystem.createNonInteractiveSigmaChallengeGenerator(G_q, es, size, kc, proverId, ro);
		ChallengeGenerator ecgS = ReEncryptionShuffleProofSystem.createNonInteractiveEValuesGenerator(G_q, es, size, ke, ro);
		ReEncryptionShuffleProofSystem sps = ReEncryptionShuffleProofSystem.getInstance(scgS, ecgS, G_q, size, es, publicKey, kr, rrs);

		// Compose private and public input
		Triple privateInput = Triple.getInstance(permutation, permutationCommitmentRandomizations, randomizations);
		Triple publicInput = Triple.getInstance(permutationCommitment, ciphertexts, shuffledCiphertexts);

		// Create shuffle proof
		Triple proofShuffle = sps.generate(privateInput, publicInput);

		// V E R I F Y
		//-------------
		System.out.print("Verify... ");
		// Verify permutation commitment proof
		boolean vPermutation = pcps.verify(proofPermutation, permutationCommitment);

		// Verify shuffle proof
		boolean vShuffle = sps.verify(proofShuffle, publicInput);

		// Verify equality of permutation commitments
		boolean vPermutationCommitments = permutationCommitment.isEquivalent(publicInput.getFirst());

		if (vPermutation && vShuffle && vPermutationCommitments) {
			System.out.println("Proof is valid!");
		} else {
			System.out.println("Proof is NOT valid!");
		}
	}

	public static void main(final String[] args) {
		Example.runExamples();
	}

}
