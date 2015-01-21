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
package lib.unicrypt.examples.src.main.java.ch.bfh.unicrypt.math.algebra.general;

import ch.bfh.unicrypt.Example;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.PermutationGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import java.math.BigInteger;

public class PermutationExample {

	public static void example1() {

		PermutationGroup pg = PermutationGroup.getInstance(5);

		// Compute order |pq| = 5! = 120
		BigInteger order = pg.getOrder();

		// Pick random permutation and invert it
		Element p1 = pg.getRandomElement();
		Element p2 = pg.invert(p1);

		// Combine p1 and p2 into p12 = (0,1,2,3,4)
		Element p12 = pg.apply(p1, p2);

		Example.printLines(pg, order, p1, p2, p12);
	}

	public static void example2() {
		PermutationGroup pg = PermutationGroup.getInstance(5);

		// Pick random permutation and invert it
		Element p1 = pg.getRandomElement();

		// Convert it to BigInteger
		BigInteger x = p1.getBigInteger();

		// Convert it back to PermutationElement
		Element p2 = pg.getElementFrom(x);

		Example.printLines(p1, x, p2);

	}

	public static void main(final String[] args) {
		Example.runExamples();
	}

}
