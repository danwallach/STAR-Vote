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

import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.interfaces.ChallengeGenerator;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.interfaces.SigmaChallengeGenerator;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.proofsystem.classes.PermutationCommitmentProofSystem;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.proofsystem.classes.ReEncryptionShuffleProofSystem;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.commitment.classes.PermutationCommitmentScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.encryption.classes.ElGamalEncryptionScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.encryption.interfaces.ReEncryptionScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.Alphabet;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.Permutation;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.additive.classes.ECZModPrime;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.concatenative.classes.StringMonoid;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZMod;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModPrime;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Pair;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.PermutationElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.PermutationGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Triple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.CyclicGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.params.classes.SECECCParamsFp;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.PermutationFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.random.classes.PseudoRandomOracle;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.random.classes.ReferenceRandomByteSequence;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.random.interfaces.RandomOracle;
import java.math.BigInteger;

/**
 *
 * @author philipp
 */
public class ShuffleProofSystemECExample {

	public Triple createCiphertexts(int size, CyclicGroup G_q, ReEncryptionScheme encryptionScheme, Element encryptionPK, PermutationElement pi) {

		final ZMod Z_q = G_q.getZModOrder();

		// Ciphertexts
		Tuple rV = ProductGroup.getInstance(Z_q, size).getRandomElement();
		ProductGroup uVSpace = ProductGroup.getInstance(ProductGroup.getInstance(G_q, 2), size);
		Tuple uV = uVSpace.getRandomElement();
		Element[] uPrimes = new Element[size];
		for (int i = 0; i < size; i++) {
			uPrimes[i] = encryptionScheme.reEncrypt(encryptionPK, uV.getAt(i), rV.getAt(i));
		}
		Tuple uPrimeV = PermutationFunction.getInstance(ProductGroup.getInstance(G_q, 2), size).apply(Tuple.getInstance(uPrimes), pi);

		return Triple.getInstance(uV, uPrimeV, rV);
	}

	public void proofOfShuffle(int size, CyclicGroup G_q, ReEncryptionScheme encryptionScheme, Element encryptionPK, PermutationElement pi, Tuple uV, Tuple uPrimeV, Tuple rV) {

		final RandomOracle ro = PseudoRandomOracle.DEFAULT;
		final ReferenceRandomByteSequence rrs = ReferenceRandomByteSequence.getInstance();
		final Element proverId = StringMonoid.getInstance(Alphabet.BASE64).getElement("Shuffler");
		final int ke = 60;
		final int kc = 60;
		final int kr = 20;

		// Permutation commitment
		PermutationCommitmentScheme pcs = PermutationCommitmentScheme.getInstance(G_q, size, rrs);
		Tuple sV = pcs.getRandomizationSpace().getRandomElement();
		Tuple cPiV = pcs.commit(pi, sV);

		// Permutation commitment proof generator
		SigmaChallengeGenerator scg = PermutationCommitmentProofSystem.createNonInteractiveSigmaChallengeGenerator(G_q, size, kc, proverId, ro);
		ChallengeGenerator ecg = PermutationCommitmentProofSystem.createNonInteractiveEValuesGenerator(G_q, size, ke, ro);
		PermutationCommitmentProofSystem pcpg = PermutationCommitmentProofSystem.getInstance(scg, ecg, G_q, size, kr, rrs);

		// Shuffle Proof Generator
		SigmaChallengeGenerator scgS = ReEncryptionShuffleProofSystem.createNonInteractiveSigmaChallengeGenerator(G_q, encryptionScheme, size, kc, proverId, ro);
		ChallengeGenerator ecgS = ReEncryptionShuffleProofSystem.createNonInteractiveEValuesGenerator(G_q, encryptionScheme, size, ke, ro);
		ReEncryptionShuffleProofSystem spg = ReEncryptionShuffleProofSystem.getInstance(scgS, ecgS, G_q, size, encryptionScheme, encryptionPK, kr, rrs);

		// Proof
		Pair proofPermutation = pcpg.generate(Pair.getInstance(pi, sV), cPiV);
		Tuple privateInput = Tuple.getInstance(pi, sV, rV);
		Tuple publicInput = Tuple.getInstance(cPiV, uV, uPrimeV);
		Triple proofShuffle = spg.generate(privateInput, publicInput);

		// Verify
		// (Important: If it is not given from the context, check equality of
		//             the permutation commitments!)
		boolean vPermutation = pcpg.verify(proofPermutation, cPiV);
		boolean vShuffle = spg.verify(proofShuffle, publicInput);
		System.out.println("Shuffle was sucessful: " + (vPermutation && vShuffle));
	}

	public static void main(String[] args) throws Exception {

		// Setup
		final int size = 100;
		ZModPrime f = ZModPrime.getInstance(29);
		ZModElement a = f.getElement(4);
		ZModElement b = f.getElement(20);
		ZModElement gx = f.getElement(1);
		ZModElement gy = f.getElement(5);
		BigInteger order = BigInteger.valueOf(37);
		BigInteger h = BigInteger.ONE;
		//final ECZModPrime G_q = ECZModPrime.getInstance(f, a, b, gx, gy, order, h);

		final ECZModPrime G_q = ECZModPrime.getInstance(SECECCParamsFp.secp160r1); //Possible curves secp{112,160,192,224,256,384,521}r1

		// Create encryption scheme and key
		final ReferenceRandomByteSequence rrs = ReferenceRandomByteSequence.getInstance();
		final Element g = G_q.getIndependentGenerator(0, rrs);
		ReEncryptionScheme encryptionScheme = ElGamalEncryptionScheme.getInstance(g);
		final Element encryptionPK = G_q.getRandomElement();

		// Create random permutation
		final Permutation permutation = Permutation.getRandomInstance(size);
		final PermutationElement pi = PermutationGroup.getInstance(size).getElement(permutation);

		// Create example instance
		ShuffleProofSystemECExample ex = new ShuffleProofSystemECExample();

		// Create ciphertexts (uV: input, uPrimeV: shuffled output, rV: randomness of re-encryption)
		final Triple c = ex.createCiphertexts(size, G_q, encryptionScheme, encryptionPK, pi);
		final Tuple uV = (Tuple) c.getFirst();
		final Tuple uPrimeV = (Tuple) c.getSecond();
		final Tuple rV = (Tuple) c.getThird();

		// Create and verify proof
		long time = System.currentTimeMillis();
		ex.proofOfShuffle(size, G_q, encryptionScheme, encryptionPK, pi, uV, uPrimeV, rV);
		System.out.println("Finished after: " + (System.currentTimeMillis() - time) + " MilliSeconds.");

	}

}
