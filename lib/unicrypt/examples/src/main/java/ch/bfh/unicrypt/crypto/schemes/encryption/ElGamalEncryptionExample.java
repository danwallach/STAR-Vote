/*
 * UniCrypt
 *
 *  UniCrypt(tm) Cryptographical framework allowing the implementation of cryptographic protocols e.g. e-voting
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
 *  1. This program is free softwareyou can redistribute it and/or modify
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
 *   For further information contact <e-mailunicrypt@bfh.ch>
 *
 *
 * Redistributions of files must retain the above copyright notice.
 */
package lib.unicrypt.examples.src.main.java.ch.bfh.unicrypt.crypto.schemes.encryption;

import ch.bfh.unicrypt.Example;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.encoder.classes.ZModToGStarModSafePrimeEncoder;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.encoder.interfaces.Encoder;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.encryption.classes.ElGamalEncryptionScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Pair;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.CyclicGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModSafePrime;

/**
 *
 * @author Rolf Haenni <rolf.haenni@bfh.ch>
 */
public class ElGamalEncryptionExample {

	public static void example1() {

		// Create cyclic group G_q (modulo 20 bits) and get default generator
		CyclicGroup cyclicGroup = GStarModSafePrime.getRandomInstance(20);
		Element generator = cyclicGroup.getDefaultGenerator();

		// Create ElGamal encryption scheme
		ElGamalEncryptionScheme elGamal = ElGamalEncryptionScheme.getInstance(generator);

		// Create keys
		Pair keyPair = elGamal.getKeyPairGenerator().generateKeyPair();
		Element privateKey = keyPair.getFirst();
		Element publicKey = keyPair.getSecond();

		// Create random message
		Element message = elGamal.getMessageSpace().getRandomElement();

		// Encryption
		Element encryption = elGamal.encrypt(publicKey, message);

		// Decryption
		Element decryption = elGamal.decrypt(privateKey, encryption);

		Example.setLabelLength("Encrypted Message");
		Example.printLine("Cylic Group", cyclicGroup);
		Example.printLine("Key Pair", keyPair);
		Example.printLine("Message", message);
		Example.printLine("Encrypted Message", encryption);
		Example.printLine("Decrypted Message", decryption);
	}

	public static void example2() {

		// Create cyclic group G_q (modulo 20 bits) and get default generator
		GStarModSafePrime cyclicGroup = GStarModSafePrime.getRandomInstance(20);
		Element generator = cyclicGroup.getDefaultGenerator();

		// Create ElGamal encryption scheme
		ElGamalEncryptionScheme elGamal = ElGamalEncryptionScheme.getInstance(generator);

		// Create encoder from Z_q to G_q
		Encoder encoder = ZModToGStarModSafePrimeEncoder.getInstance(cyclicGroup);

		// Create keys
		Pair keyPair = elGamal.getKeyPairGenerator().generateKeyPair();
		Element privateKey = keyPair.getFirst();
		Element publicKey = keyPair.getSecond();

		// Create, encode, and encrypt message m=66
		Element message = encoder.getDomain().getElementFrom(66);
		Element encodedMessage = encoder.encode(message);
		Element encryption = elGamal.encrypt(publicKey, encodedMessage);

		// Decrypt and decode encryption
		Element decryption = elGamal.decrypt(privateKey, encryption);
		Element decodedMessage = encoder.decode(decryption);

		Example.setLabelLength("Encrypted Message");
		Example.printLine("Cylic Group", cyclicGroup);
		Example.printLine("Key Pair", keyPair);
		Example.printLine("Encoder", encoder);
		Example.printLine("Message", message);
		Example.printLine("Encoded Message", encodedMessage);
		Example.printLine("Encrypted Message", encryption);
		Example.printLine("Decrypted Message", decryption);
		Example.printLine("Decoded Message", decodedMessage);
	}

	public static void example3() {

		// Create cyclic group G_23 and get default generator
		CyclicGroup cyclicGroup = GStarModSafePrime.getInstance(23);
		Element generator = cyclicGroup.getDefaultGenerator();

		// Create ElGamal encryption scheme
		ElGamalEncryptionScheme elGamal = ElGamalEncryptionScheme.getInstance(generator);

		// Create keys
		Pair keyPair = elGamal.getKeyPairGenerator().generateKeyPair();
		Element privateKey = keyPair.getFirst();
		Element publicKey = keyPair.getSecond();

		// Create messages m1=3, m2=6, and m12=m1*m2=18
		Element message1 = elGamal.getMessageSpace().getElementFrom(3);
		Element message2 = elGamal.getMessageSpace().getElementFrom(6);
		Element message12 = elGamal.getMessageSpace().apply(message1, message2);

		// Encryption
		Element encryption1 = elGamal.encrypt(publicKey, message1);
		Element encryption2 = elGamal.encrypt(publicKey, message2);
		Element encryption12 = elGamal.encrypt(publicKey, message12);

		// Decryption
		Element decryption1 = elGamal.decrypt(privateKey, encryption1);
		Element decryption2 = elGamal.decrypt(privateKey, encryption2);
		Element decryption12 = elGamal.decrypt(privateKey, encryption12);

		Example.setLabelLength("Decyption12");
		Example.printLine("Cylic Group", cyclicGroup);
		Example.printLine("Key Pair", keyPair);
		Example.printLine("Message1", message1);
		Example.printLine("Message2", message2);
		Example.printLine("Message12", message12);
		Example.printLine("Encyption1", encryption1);
		Example.printLine("Encyption2", encryption2);
		Example.printLine("Encyption12", encryption12);
		Example.printLine("Decyption1", decryption1);
		Example.printLine("Decyption2", decryption2);
		Example.printLine("Decyption12", decryption12);
	}

	public static void main(final String[] args) {
		Example.runExamples();
	}

}
