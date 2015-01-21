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
package lib.unicrypt.examples.src.main.java.ch.bfh.unicrypt.crypto.schemes.encryption;

import ch.bfh.unicrypt.Example;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.encoder.classes.ZModToECPolynomialFieldEncoder;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.encoder.classes.ZModToECZModPrimeEncoder;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.encoder.interfaces.Encoder;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.encryption.classes.ElGamalEncryptionScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.additive.classes.ECPolynomialField;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.additive.classes.ECZModPrime;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Pair;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.CyclicGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.params.classes.SECECCParamsF2m;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.params.classes.SECECCParamsFp;

public class ElGamalECCExample {

	public static void example1() throws Exception {

		// Create cyclic group EC Fp (modulo 521 bits) and get default generator
		CyclicGroup cyclicGroup = ECZModPrime.getInstance(SECECCParamsFp.secp521r1);
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

	public static void example2() throws Exception {

		// Create cyclic group EC Fp (modulo 521 bits) and get default generator
		ECZModPrime cyclicGroup = ECZModPrime.getInstance(SECECCParamsFp.secp521r1);
		Element generator = cyclicGroup.getDefaultGenerator();

		// Create ElGamal encryption scheme
		ElGamalEncryptionScheme elGamal = ElGamalEncryptionScheme.getInstance(generator);

		// Create encoder from Z_q to EC Fp
		Encoder encoder = ZModToECZModPrimeEncoder.getInstance(cyclicGroup,15);

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

	public static void example3() throws Exception {

		// Create cyclic group EC Fp (modulo 521 bits) and get default generator
		CyclicGroup cyclicGroup = ECPolynomialField.getInstance(SECECCParamsF2m.sect113r1);
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

	public static void example4() throws Exception {

		// Create cyclic group EC Fp (modulo 521 bits) and get default generator
		ECPolynomialField cyclicGroup = ECPolynomialField.getInstance(SECECCParamsF2m.sect113r1);

		Element generator = cyclicGroup.getDefaultGenerator();

		// Create ElGamal encryption scheme
		ElGamalEncryptionScheme elGamal = ElGamalEncryptionScheme.getInstance(generator);

		// Create encoder from Z_q to EC Fp
		Encoder encoder = ZModToECPolynomialFieldEncoder.getInstance(cyclicGroup.getZModOrder(), cyclicGroup,15);

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

	public static void main(final String[] args) {
		Example.runExamples();
	}

}
