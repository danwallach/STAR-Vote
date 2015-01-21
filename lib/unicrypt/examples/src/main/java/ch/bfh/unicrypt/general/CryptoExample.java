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
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.encoder.classes.CompositeEncoder;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.encoder.classes.FiniteStringToZModEncoder;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.encoder.classes.ZModToGStarModSafePrimeEncoder;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.encoder.interfaces.Encoder;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.keygenerator.interfaces.KeyPairGenerator;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.encryption.classes.ElGamalEncryptionScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.sharing.classes.ShamirSecretSharingScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.Alphabet;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZMod;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModPrime;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.CyclicGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModSafePrime;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.interfaces.Function;

/**
 *
 * @author Rolf Haenni <rolf.haenni@bfh.ch>
 */
public class CryptoExample {

	public static void example1() {

		// Define underlying prime field and create (5,3)-threshold sharing scheme
		ZModPrime z29 = ZModPrime.getInstance(29);
		ShamirSecretSharingScheme sss = ShamirSecretSharingScheme.getInstance(z29, 5, 3);

		// Create message m=25
		Element message = sss.getMessageSpace().getElement(25);

		// Compute shares
		Tuple shares = sss.share(message);

		// Select subset of shares and recover message
		Tuple someShares = shares.removeAt(1).removeAt(3);
		Element recoveredMessage1 = sss.recover(someShares);

		// Recover message differently
		Element recoveredMessage2 = sss.recover(shares.getAt(1), shares.getAt(2), shares.getAt(3));

		// Print results
		Example.printLine("Message", message);
		Example.printLines("Shares", shares);
		Example.printLines("Some Shares", someShares);
		Example.printLine("Recovered Message", recoveredMessage1);
		Example.printLine("Recovered Message", recoveredMessage2);
	}

	public static void example2() {

		// Create cyclic group and get default generator
		CyclicGroup g_q = GStarModSafePrime.getInstance(23);
		Element generator = g_q.getDefaultGenerator();

		// Create ElGamal encryption scheme
		ElGamalEncryptionScheme elGamal = ElGamalEncryptionScheme.getInstance(generator);

		// Create keys
		KeyPairGenerator kpg = elGamal.getKeyPairGenerator();
		Element privateKey = kpg.generatePrivateKey();
		Element publicKey = kpg.generatePublicKey(privateKey);

		// Create random message
		Element message = elGamal.getMessageSpace().getRandomElement();

		// Perform encryption
		Element encryption = elGamal.encrypt(publicKey, message);

		// Perform decryption
		Element decryption = elGamal.decrypt(privateKey, encryption);

		// Get encryption and decryption function
		Function encFunction = elGamal.getEncryptionFunction();
		Function decFunction = elGamal.getDecryptionFunction();

		// Print results
		Example.printLine("Scheme", elGamal);
		Example.printLines("Keys", privateKey, publicKey);
		Example.printLine("Message", message);
		Example.printLine("Enryption", encryption);
		Example.printLine("Decryption", decryption);
		Example.printLines("Functions", encFunction, decFunction);
	}

	public static void example3() {

		// Define underlying groups (64 bits)
		GStarModSafePrime group = GStarModSafePrime.getRandomInstance(64);
		ZMod zMod = group.getZModOrder();

		// Create encoders
		Encoder encoder1 = FiniteStringToZModEncoder.getInstance(zMod, Alphabet.LOWER_CASE);
		Encoder encoder2 = ZModToGStarModSafePrimeEncoder.getInstance(group);
		Encoder encoder12 = CompositeEncoder.getInstance(encoder1, encoder2);

		// Define message
		Set messageSpace = encoder12.getDomain();
		Element message = messageSpace.getElement("hello");

		// Encode and decode message
		Element encodedMessage = encoder12.encode(message);
		Element decodedMessage = encoder12.decode(encodedMessage);

		Example.printLines("Groups", group, zMod);
		Example.printLine("MessageSpace", messageSpace);
		Example.printLines("Encoders", encoder1, encoder2, encoder12);
		Example.printLines("Messages", message, encodedMessage, decodedMessage);
	}

	public static void main(final String[] args) {
		Example.runExamples();
	}

}
