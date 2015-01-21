/*
 * UniCrypt
 *
 *  UniCrypt(tm) : Cryptographical framework allowing the implementation of cryptographic protocols e.g. e-voting
 *  Copyright (C) Error: on line 7, column 34 in file:///Users/rolfhaenni/GIT/unicrypt/examples/license-dualLicense.txt
 The string doesn't match the expected date/time format. The string to parse was: "05-Feb-2014". The expected format was: "MMM d, yyyy". Bern University of Applied Sciences (BFH), Research Institute for
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
package lib.unicrypt.examples.src.main.java.ch.bfh.unicrypt.math.function;

import ch.bfh.unicrypt.Example;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModPrime;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Group;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.HashFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.interfaces.Function;

/**
 *
 * @author Rolf Haenni <rolf.haenni@bfh.ch>
 */
public class HashFunctionExample {

	public static void example1() {
		// Generate product group Z23^10 and random element
		Group z23 = ZModPrime.getInstance(23);
		ProductGroup pg = ProductGroup.getInstance(z23, 10);
		Tuple tuple = pg.getRandomElement();

		// Define hash function Z23^10 -> {0,1}^256 (default SHA256)
		Function function = HashFunction.getInstance(pg);

		// Apply hash function to tuple (return finite byte array)
		Element hashValue = function.apply(tuple);

		Example.printLine("Function", function);
		Example.printLines("Value and Hash Value", tuple, hashValue);
	}

	public static void main(final String[] args) {
		Example.runExamples();
	}

}
