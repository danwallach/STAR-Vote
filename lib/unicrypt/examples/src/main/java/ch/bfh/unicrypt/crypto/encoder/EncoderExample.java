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
package lib.unicrypt.examples.src.main.java.ch.bfh.unicrypt.crypto.encoder;

import ch.bfh.unicrypt.Example;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.encoder.classes.ZModToECPolynomialFieldEncoder;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.encoder.classes.ZModToECZModPrimeEncoder;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.additive.classes.ECPolynomialField;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.additive.classes.ECZModPrime;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModPrime;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.params.classes.SECECCParamsF2m;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.params.classes.SECECCParamsFp;

/**
 *
 * @author Rolf Haenni <rolf.haenni@bfh.ch>
 */
public class EncoderExample {



	/**
	 * Example shows how to encode an element from ZModPrime into an elliptic curve over F2m
	 * <p>
	 * @throws Exception
	 */
	public static void example1() throws Exception {

		// Define underlying groups
		ECPolynomialField ec = ECPolynomialField.getInstance(SECECCParamsF2m.sect113r1);
		ZModPrime z = ZModPrime.getInstance(ec.getOrder());

		// Create encoders
		ZModToECPolynomialFieldEncoder encoder1 = ZModToECPolynomialFieldEncoder.getInstance(z, ec,15);



		Element message = encoder1.getDomain().getElement(12350);
		Element encodedMessage = encoder1.encode(message);
		Element decodedMessage = encoder1.decode(encodedMessage);

		Example.printLines("Groups", ec, z);
		Example.printLines("Encoders", encoder1);
		Example.printLines("Messages", message, encodedMessage, decodedMessage);

	}
	
	/**
	 * Example shows how to encode an element from ZModPrime into an elliptic curve over F2m
	 * <p>
	 * @throws Exception
	 */
	public static void example2() throws Exception {

		// Define underlying groups
		ECZModPrime ec = ECZModPrime.getInstance(SECECCParamsFp.secp160r1);

		// Create encoders
		ZModToECZModPrimeEncoder encoder1 = ZModToECZModPrimeEncoder.getInstance(ec,15);

		// Encode and decode message
		Element message = encoder1.getDomain().getElement(123456);
		Element encodedMessage = encoder1.encode(message);
		Element decodedMessage = encoder1.decode(encodedMessage);

		Example.printLines("Groups", ec);
		Example.printLines("Encoders", encoder1);
		Example.printLines("Messages", message, encodedMessage, decodedMessage);

	}

	public static void main(final String[] args) {
		Example.runExamples();
	}

}
