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
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.encoder.classes.StringToByteArrayEncoder;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.encoder.interfaces.Encoder;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.encryption.classes.AESEncryptionScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.padding.classes.ANSIPaddingScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.padding.classes.PKCSPaddingScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.padding.interfaces.ReversiblePaddingScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.Alphabet;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.concatenative.classes.ByteArrayMonoid;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.concatenative.classes.StringMonoid;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;

/**
 *
 * @author Rolf Haenni <rolf.haenni@bfh.ch>
 */
public class AESEncryptionExample {

	public static void example1() {

		AESEncryptionScheme aes = AESEncryptionScheme.getInstance();

		// Random key (default length = 16 bytes)
		Element key = aes.generateSecretKey();

		// Random message (length = 32 bytes = multiple of block length)
		Element message = aes.getMessageSpace().getRandomElement(32);

		// Perform the encryption and decryption
		Element encryptedMessage = aes.encrypt(key, message);
		Element decryptedMessage = aes.decrypt(key, encryptedMessage);

		Example.setLabelLength("Encrypted Message");
		Example.printLine("Key", key);
		Example.printLine("Message", message);
		Example.printLine("Encrypted Message", encryptedMessage);
		Example.printLine("Decrypted Message", decryptedMessage);
	}

	public static void example2() {

		// Given message (length = 8 bytes)
		Element message = ByteArrayMonoid.getInstance().getElement("00|95|2B|9B|E2|FD|30|89");

		// Apply padding scheme (increase length to block size = 16 bytes)
		ReversiblePaddingScheme pkcs = PKCSPaddingScheme.getInstance(16);
		Element paddedMessage = pkcs.pad(message);

		// Perform the encryption and decryption
		AESEncryptionScheme aes = AESEncryptionScheme.getInstance();
		Element key = aes.generateSecretKey();
		Element encryptedMessage = aes.encrypt(key, paddedMessage);
		Element decryptedMessage = aes.decrypt(key, encryptedMessage);

		// Apply padding scheme (decrease length to 8 bytes)
		Element unpaddedMessage = pkcs.unpad(decryptedMessage);

		Example.setLabelLength("Encrypted Message");
		Example.printLine("Key", key);
		Example.printLine("Message", message);
		Example.printLine("Padded Message", paddedMessage);
		Example.printLine("Encrypted Message", encryptedMessage);
		Example.printLine("Decrypted Message", decryptedMessage);
		Example.printLine("Unpadded Message", unpaddedMessage);
	}

	public static void example3() {

		// Define alphabet, string monoid, encoder, padding
		Alphabet alphabet = Alphabet.ALPHANUMERIC;
		StringMonoid stringMonoid = StringMonoid.getInstance(alphabet);
		Encoder encoder = StringToByteArrayEncoder.getInstance(stringMonoid);
		ReversiblePaddingScheme ansi = ANSIPaddingScheme.getInstance(16);

		// Define encryption scheme and key
		AESEncryptionScheme aes = AESEncryptionScheme.getInstance();
		Element key = aes.generateSecretKey();

		// Define string message to encrypt
		Element message = stringMonoid.getElement("HalloWorld");

		// Perform the encoding, padding, and encryption
		Element encodedMessage = encoder.encode(message);
		Element paddedMessage = ansi.pad(encodedMessage);
		Element encryptedMessage = aes.encrypt(key, paddedMessage);

		// Perform the decryption, decoding an unpadding
		Element decryptedMessage = aes.decrypt(key, encryptedMessage);
		Element unpaddedMessage = ansi.unpad(decryptedMessage);
		Element decodedMessage = encoder.decode(unpaddedMessage);

		Example.setLabelLength("Encrypted Message");
		Example.printLine("Key", key);
		Example.printLine("Message", message);
		Example.printLine("Encoded Message", encodedMessage);
		Example.printLine("Padded Message", paddedMessage);
		Example.printLine("Encrypted Message", encryptedMessage);
		Example.printLine("Decrypted Message", decryptedMessage);
		Example.printLine("UnpaddedMessage", unpaddedMessage);
		Example.printLine("Decoded Message", decodedMessage);
	}

	public static void example4() {

		AESEncryptionScheme aes = AESEncryptionScheme.getInstance(
			   // AESEncryptionScheme.KeyLength.KEY192,
			   AESEncryptionScheme.KeyLength.KEY128,
			   AESEncryptionScheme.Mode.CBC,
			   AESEncryptionScheme.DEFAULT_IV);

		// Random key (16 bytes) and message (32 bytes)
		Element key = aes.generateSecretKey();
		Element message = aes.getMessageSpace().getRandomElement(32);

		// perform the encryption and decryption
		Element encryptedMessage = aes.encrypt(key, message);
		Element decryptedMessage = aes.decrypt(key, encryptedMessage);

		Example.setLabelLength("Encrypted Message");
		Example.printLine("Key", key);
		Example.printLine("Message", message);
		Example.printLine("Encrypted Message", encryptedMessage);
		Example.printLine("Decrypted Message", decryptedMessage);
	}

	public static void main(final String[] args) {
		Example.runExamples();
	}

}
