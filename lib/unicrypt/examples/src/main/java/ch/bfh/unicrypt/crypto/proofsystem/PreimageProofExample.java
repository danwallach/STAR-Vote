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
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.keygenerator.interfaces.KeyPairGenerator;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.proofsystem.classes.PreimageProofSystem;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.encryption.classes.ElGamalEncryptionScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Triple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.CyclicGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModSafePrime;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.interfaces.Function;

/**
 *
 * @author Rolf Haenni <rolf.haenni@bfh.ch>
 */
public class PreimageProofExample {

	public static void example1() {

		// Create cyclic group G_q (modulo 20 bits) and get default generator
		CyclicGroup cyclicGroup = GStarModSafePrime.getRandomInstance(20);
		Element generator = cyclicGroup.getDefaultGenerator();

		// Create ElGamal encryption scheme
		ElGamalEncryptionScheme elGamal = ElGamalEncryptionScheme.getInstance(generator);

		// Generate keys
		KeyPairGenerator kpg = elGamal.getKeyPairGenerator();
		Element privateKey = kpg.generatePrivateKey();
		Element publicKey = kpg.generatePublicKey(privateKey);

		// Generate proof generator
		Function function = kpg.getPublicKeyGenerationFunction();
		PreimageProofSystem pg = PreimageProofSystem.getInstance(function);

		// Generate and verify proof
		Triple proof = pg.generate(privateKey, publicKey);
		boolean result = pg.verify(proof, publicKey);

		Example.printLine("Cyclic Group", cyclicGroup);
		Example.printLine("Generator", generator);
		Example.printLines("Keys", privateKey, publicKey);
		Example.printLine("Proof", proof);
		Example.printLine("Check", result);
	}

	public static void main(final String[] args) {
		Example.runExamples();
	}

}
