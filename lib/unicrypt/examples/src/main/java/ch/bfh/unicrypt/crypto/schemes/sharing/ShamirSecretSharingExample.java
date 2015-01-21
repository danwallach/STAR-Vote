/*
 * UniCrypt
 *
 *  UniCrypt(tm) : Cryptographical framework allowing the implementation of cryptographic protocols e.g. e-voting
 *  Copyright (C) Error: on line 7, column 34 in file:///Users/rolfhaenni/GIT/unicrypt/examples/license-dualLicense.txt
 The string doesn't match the expected date/time format. The string to parse was: "30-Jan-2014". The expected format was: "MMM d, yyyy". Bern University of Applied Sciences (BFH), Research Institute for
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
package lib.unicrypt.examples.src.main.java.ch.bfh.unicrypt.crypto.schemes.sharing;

import ch.bfh.unicrypt.Example;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.sharing.classes.ShamirSecretSharingScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.sharing.interfaces.SecretSharingScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModPrime;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;

/**
 *
 * @author Rolf Haenni <rolf.haenni@bfh.ch>
 */
public class ShamirSecretSharingExample {

	public static void example1() {

		// Define underlying prime field and create (5,3)-threshold sharing scheme
		ZModPrime z29 = ZModPrime.getInstance(29);
		SecretSharingScheme sss = ShamirSecretSharingScheme.getInstance(z29, 5, 3);

		// Create message m=25
		Element message = sss.getMessageSpace().getElementFrom(5);

		// Compute shares
		Tuple shares = sss.share(message);

		// Select subset of shares
		Tuple someShares = shares.removeAt(1).removeAt(3);

		// Recover message
		Element recoveredMessage1 = sss.recover(someShares);

		// Recover message differently
		Element recoveredMessage2 = sss.recover(shares.getAt(1), shares.getAt(2), shares.getAt(3));

		Example.printLine("Message", message);
		Example.printLines("Shares", shares);
		Example.printLines("Some Shares", someShares);
		Example.printLine("Recovered Message", recoveredMessage1);
		Example.printLine("Recovered Message", recoveredMessage2);

	}

	public static void main(final String[] args) {
		Example.runExamples();
	}

}
