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
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.concatenative.classes.StringMonoid;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZMod;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Pair;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.PermutationElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.PermutationGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Triple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.CyclicGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModSafePrime;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.PermutationFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.random.classes.PseudoRandomOracle;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.random.classes.ReferenceRandomByteSequence;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.random.interfaces.RandomOracle;
import java.math.BigInteger;

public class ShuffleProofSystemExample {

	// A few safe primes
	final static String P_256 = "88059184022561109274134540595138392753102891002065208740257707896840303297223"; //256
	final static String P_512 = "7345427226905070499764889740717053678145447263298168648090208932893269201500094910685938670933846388714280765884795335365679757155747184872890100586114127"; //512
	final static String P_1024 = "124839508901459225295131478904766553151715203799479873450319702669888301683936126519033292399204126892064039399466769614858812059914518351605494976695246338946504781671208279483554047133686061305170930849857475703281378907333309894394327830075584429809888154770188970744592711756609335320238222672153149255987"; //1024
	final static String P_2048 = "32317006071311007300714876688669951960444102669715484032130345427524655138867890893197201411522913463688717960921898019494119559150490921095088152386448283120630877367300996091750197750389652106796057638384067568276792218642619756161838094338476170470581645852036305042887575891541065808607552399123930385521914333389668342420684974786564569494856176035326322058077805659331026192708460314150258592864177116725943603718461857357598351152301645904403697613233287231227125684710820209725157101726931323469678542580656697935045997268352998638215525166389437335543602135433229604645318478604952148193555853611059594288367"; //2048

	// 160/1024
	final static String Q = "1081119563825030427708677600856959359670713108783";
	final static String P = "132981118064499312972124229719551507064282251442693318094413647002876359530119444044769383265695686373097209253015503887096288112369989708235068428214124661556800389180762828009952422599372290980806417384771730325122099441368051976156139223257233269955912341167062173607119895128870594055324929155200165347329";

	final static Element proverId = StringMonoid.getInstance(Alphabet.BASE64).getElement("Shuffler");
	final static int ke = 60;
	final static int kc = 60;
	final static int kr = 20;

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

		// Permutation commitment
		PermutationCommitmentScheme pcs = PermutationCommitmentScheme.getInstance(G_q, size, rrs);
		Tuple sV = pcs.getRandomizationSpace().getRandomElement();
		Tuple cPiV = pcs.commit(pi, sV);
		System.out.println("Permutation Commitment");

		// Permutation commitment proof generator
		SigmaChallengeGenerator scg = PermutationCommitmentProofSystem.createNonInteractiveSigmaChallengeGenerator(G_q, size, kc, proverId, ro);
		ChallengeGenerator ecg = PermutationCommitmentProofSystem.createNonInteractiveEValuesGenerator(G_q, size, ke, ro);
		PermutationCommitmentProofSystem pcps = PermutationCommitmentProofSystem.getInstance(scg, ecg, G_q, size, kr, rrs);

		// Shuffle Proof Generator
		SigmaChallengeGenerator scgS = ReEncryptionShuffleProofSystem.createNonInteractiveSigmaChallengeGenerator(G_q, encryptionScheme, size, kc, proverId, ro);
		ChallengeGenerator ecgS = ReEncryptionShuffleProofSystem.createNonInteractiveEValuesGenerator(G_q, encryptionScheme, size, ke, ro);
		ReEncryptionShuffleProofSystem sps = ReEncryptionShuffleProofSystem.getInstance(scgS, ecgS, G_q, size, encryptionScheme, encryptionPK, kr, rrs);

		// Proof
		Pair proofPermutation = pcps.generate(Pair.getInstance(pi, sV), cPiV);
		Tuple privateInput = Tuple.getInstance(pi, sV, rV);
		Tuple publicInput = Tuple.getInstance(cPiV, uV, uPrimeV);
		Triple proofShuffle = sps.generate(privateInput, publicInput);
		System.out.println("Shuffle-Proof");

		// Verify
		// (Important: If it is not given from the context, check equality of
		//             the permutation commitments!)
		boolean vPermutation = pcps.verify(proofPermutation, cPiV);
		boolean vShuffle = sps.verify(proofShuffle, publicInput);
		System.out.println("Verify");
		System.out.println("Shuffle was sucessful: " + (vPermutation && vShuffle));
	}

	public static void main(String[] args) {

		// Setup
		final int size = 100;
		final CyclicGroup G_q = GStarModSafePrime.getInstance(new BigInteger(P_1024, 10));
		//final CyclicGroup G_q = GStarModPrime.getInstance(new BigInteger(P, 10), new BigInteger(Q, 10));

		// Create encryption scheme and key
		final ReferenceRandomByteSequence rrs = ReferenceRandomByteSequence.getInstance();
		final Element g = G_q.getIndependentGenerator(0, rrs);
		ReEncryptionScheme encryptionScheme = ElGamalEncryptionScheme.getInstance(g);
		final Element encryptionPK = G_q.getRandomElement();

		// Create random permutation
		final Permutation permutation = Permutation.getRandomInstance(size);
		final PermutationElement pi = PermutationGroup.getInstance(size).getElement(permutation);

		// Create example instance
		ShuffleProofSystemExample ex = new ShuffleProofSystemExample();

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
