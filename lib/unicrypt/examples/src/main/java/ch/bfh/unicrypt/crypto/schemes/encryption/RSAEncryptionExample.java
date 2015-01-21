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
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.encryption.classes.RSAEncryptionScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.encryption.interfaces.AsymmetricEncryptionScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModPrimePair;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Pair;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;

/**
 *
 * @author Rolf Haenni <rolf.haenni@bfh.ch>
 */
public class RSAEncryptionExample {

	public static void example1() {
		// Create random underlying group for n = pq = 53*61
		ZModPrimePair zMod = ZModPrimePair.getInstance(53, 61);

		// Crete RSA encryption scheme
		AsymmetricEncryptionScheme rsa = RSAEncryptionScheme.getInstance(zMod);

		// Generate key pair
		Pair keyPair = rsa.getKeyPairGenerator().generateKeyPair();
		Element privateKey = keyPair.getFirst();
		Element publicKey = keyPair.getSecond();

		// Select random message
		Element message = rsa.getMessageSpace().getRandomElement();

		// Encrypt and decrypt message
		Element encryptedMessage = rsa.encrypt(publicKey, message);
		Element decryptedMessage = rsa.decrypt(privateKey, encryptedMessage);

		Example.printLine(rsa);
		Example.printLines("Keys", privateKey, publicKey);
		Example.printLines("Message", message, encryptedMessage, decryptedMessage);

	}

	public static void main(final String[] args) {
		Example.runExamples();
	}

}
