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
package lib.unicrypt.examples.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic;

import ch.bfh.unicrypt.Example;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.concatenative.classes.StringMonoid;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZMod;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductSet;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.Alphabet;

/**
 *
 * @author Rolf Haenni <rolf.haenni@bfh.ch>
 */
public class ZModExample {

	public static void example1() {

		// Generate 3 atomic sets
		ZMod s1 = ZMod.getInstance(20);
		ZMod s2 = ZMod.getInstance(30);
		StringMonoid s3 = StringMonoid.getInstance(Alphabet.LOWER_CASE);

		// Generate s1 x s2, (s1 x s2)^3 and (s1 x s2)^3 x s3
		ProductSet s4 = ProductSet.getInstance(s1, s2);
		ProductSet s5 = ProductSet.getInstance(s4, 3);
		ProductSet s6 = s5.add(s3);

		// Select random tuple t1 = (e1,e2) from s4
		Tuple t1 = s4.getRandomElement();

		// Generate tuple t2 = (-5,"hello") from s1 x s3
		Tuple t2 = Tuple.getInstance(s1.getElement(-5), s3.getElement("hello"));

		Example.printLines(s1, s2, s3, s4, s5, s6, t1, t2);

	}

	public static void main(final String[] args) {
		Example.runExamples();
	}

}
