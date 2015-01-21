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
package lib.unicrypt.examples.src.main.java.ch.bfh.unicrypt.crypto.mixer;

import ch.bfh.unicrypt.Example;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.mixer.classes.ReEncryptionMixer;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.mixer.interfaces.Mixer;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.encryption.classes.ElGamalEncryptionScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Pair;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.PermutationElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.CyclicGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModSafePrime;

/**
 *
 * @author philipp
 */
public class ReEncryptionMixerExample {

	public static void example1() {

		// P R E P A R E
		//---------------
		// Create cyclic group (modulo 20 bits) and get default generator
		CyclicGroup cyclicGroup = GStarModSafePrime.getRandomInstance(20);
		Element generator = cyclicGroup.getDefaultGenerator();

		// Set size
		int size = 10;

		// Create ElGamal encryption system and keys
		ElGamalEncryptionScheme elGamal = ElGamalEncryptionScheme.getInstance(generator);
		Pair keyPair = elGamal.getKeyPairGenerator().generateKeyPair();
		Element privateKey = keyPair.getFirst();
		Element publicKey = keyPair.getSecond();

		// Create random encryptions
		Tuple messages = ProductGroup.getInstance(cyclicGroup, size).getRandomElement();
		Tuple encryptions = Tuple.getInstance();
		for (int i = 0; i < size; i++) {
			encryptions = encryptions.add(elGamal.encrypt(publicKey, messages.getAt(i)));
		}

		// S H U F F L E
		//---------------
		// Create mixer and shuffle
		Mixer mixer = ReEncryptionMixer.getInstance(elGamal, publicKey, size);
		Tuple shuffledEncyptions = mixer.shuffle(encryptions);

		// DECRYPTION
		//-----------
		Tuple decryptions = Tuple.getInstance();
		for (int i = 0; i < size; i++) {
			decryptions = decryptions.add(elGamal.decrypt(privateKey, shuffledEncyptions.getAt(i)));
		}

		Example.printLine("Cylic Group", cyclicGroup);
		Example.printLines("Messages", messages);
		Example.printLines("Encyptions", encryptions);
		Example.printLines("Shuffled Encyptions", shuffledEncyptions);
		Example.printLines("Decyptions", decryptions);
	}

	public static void example2() {

		// P R E P A R E
		//---------------
		// Create cyclic group (modulo 20 bits) and get default generator
		CyclicGroup cyclicGroup = GStarModSafePrime.getRandomInstance(20);
		Element generator = cyclicGroup.getDefaultGenerator();

		// Set size
		int size = 10;

		// Create ElGamal encryption system and keys
		ElGamalEncryptionScheme elGamal = ElGamalEncryptionScheme.getInstance(generator);
		Pair keyPair = elGamal.getKeyPairGenerator().generateKeyPair();
		Element privateKey = keyPair.getFirst();
		Element publicKey = keyPair.getSecond();

		// Create random encryptions
		Tuple messages = ProductGroup.getInstance(cyclicGroup, size).getRandomElement();
		Tuple encryptions = Tuple.getInstance();
		for (int i = 0; i < size; i++) {
			encryptions = encryptions.add(elGamal.encrypt(publicKey, messages.getAt(i)));
		}

		// S H U F F L E
		//---------------
		// Create mixer and shuffle
		ReEncryptionMixer mixer = ReEncryptionMixer.getInstance(elGamal, publicKey, size);

		// Create permutation
		PermutationElement permutation = mixer.getPermutationGroup().getRandomElement();

		// Create randomizations
		Tuple randomizations = mixer.generateRandomizations();

		Tuple shuffledEncyptions = mixer.shuffle(encryptions, permutation, randomizations);

		// DECRYPTION
		//-----------
		Tuple decryptions = Tuple.getInstance();
		for (int i = 0; i < size; i++) {
			decryptions = decryptions.add(elGamal.decrypt(privateKey, shuffledEncyptions.getAt(i)));
		}

		Example.printLine("Cylic Group", cyclicGroup);
		Example.printLines("Messages", messages);
		Example.printLines("Encyptions", encryptions);
		Example.printLines("Shuffled Encyptions", shuffledEncyptions);
		Example.printLines("Decyptions", decryptions);
	}

	public static void main(final String[] args) {
		Example.runExamples();
	}

}
