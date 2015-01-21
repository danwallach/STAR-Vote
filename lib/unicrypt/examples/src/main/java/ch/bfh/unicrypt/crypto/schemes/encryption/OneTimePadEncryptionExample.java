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
 *it under the terms of the GNU Affero General Public License as published by
 *the Free Software Foundation, either version 3 of the License, or
 *(at your option) any later version.
 *
 *This program is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU Affero General Public License for more details.
 *
 *You should have received a copy of the GNU Affero General Public License
 *along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *  2. Licensees holding valid commercial licenses for UniCrypt may use this file in
 *accordance with the commercial license agreement provided with the
 *Software or, alternatively, in accordance with the terms contained in
 *a written agreement between you and Bern University of Applied Sciences (BFH), Research Institute for
 *Security in the Information Society (RISIS), E-Voting Group (EVG)
 *Quellgasse 21, CH-2501 Biel, Switzerland.
 *
 *
 *For further information contact <e-mail: unicrypt@bfh.ch>
 *
 *
 * Redistributions of files must retain the above copyright notice.
 */
package lib.unicrypt.examples.src.main.java.ch.bfh.unicrypt.crypto.schemes.encryption;

import ch.bfh.unicrypt.Example;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.encoder.classes.FiniteStringToFiniteByteArrayEncoder;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.encryption.classes.OneTimePadEncryptionScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.FiniteStringSet;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.Alphabet;

/**
 *
 * @author Rolf Haenni <rolf.haenni@bfh.ch>
 */
public class OneTimePadEncryptionExample {

	public static void example1() {

		// Create one time pad (length = 20 bytes)
		OneTimePadEncryptionScheme oneTimePad = OneTimePadEncryptionScheme.getInstance(20);

		// Create random key (length = 20 bytes)
		Element key = oneTimePad.getSecretKeyGenerator().generateSecretKey();

		// Create random message (length = 20 bytes)
		Element message = oneTimePad.getMessageSpace().getRandomElement();

		// Perform the encryption
		Element encryptedMessage = oneTimePad.encrypt(key, message);

		// Perform the decryption
		Element decryptedMessage = oneTimePad.decrypt(key, encryptedMessage);

		Example.printLine("Key", key);
		Example.printLine("Message", message);
		Example.printLine("Encrypted Message", encryptedMessage);
		Example.printLine("Decrypted Message", decryptedMessage);
	}

	public static void example2() {

		// Define alphabet and encoder
		Alphabet alphabet = Alphabet.ALPHANUMERIC;
		FiniteStringSet stringSet = FiniteStringSet.getInstance(alphabet, 20);
		FiniteStringToFiniteByteArrayEncoder encoder = FiniteStringToFiniteByteArrayEncoder.getInstance(stringSet);

		// Create one time pad (length = 20 bytes)
		OneTimePadEncryptionScheme oneTimePad = OneTimePadEncryptionScheme.getInstance(encoder.getFiniteByteArraySet());

		// Create random key (length = 20 bytes)
		Element key = oneTimePad.getSecretKeyGenerator().generateSecretKey();

		// Create, encode, and encrypt message
		Element message = stringSet.getElement("HalloWorld");
		Element encodedMessage = encoder.encode(message);
		Element encryptedMessage = oneTimePad.encrypt(key, encodedMessage);

		// Decrypt and decode message
		Element decryptedMessage = oneTimePad.decrypt(key, encryptedMessage);
		Element decodedMessage = encoder.decode(decryptedMessage);

		Example.setLabelLength("Encrypted Message");
		Example.printLine("Key", key);
		Example.printLine("Message", message);
		Example.printLine("Encoded Message", encodedMessage);
		Example.printLine("Encrypted Message", encryptedMessage);
		Example.printLine("Decrypted Message", decryptedMessage);
		Example.printLine("Decoded Message", decodedMessage);
	}

	public static void main(final String[] args) {
		Example.runExamples();
	}

}
