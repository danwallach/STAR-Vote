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
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.Alphabet;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.concatenative.classes.StringMonoid;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.N;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.Z;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZMod;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductSet;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Set;

/**
 *
 * @author Rolf Haenni <rolf.haenni@bfh.ch>
 */
public class ProductSetExample {

	public static void example1() {

		Set s1 = Z.getInstance();
		Set s2 = N.getInstance();
		Set s3 = StringMonoid.getInstance(Alphabet.LOWER_CASE);

		ProductSet s12 = ProductSet.getInstance(s1, s2);
		Example.printLine(s12);

		ProductSet s123 = s12.add(s3);
		Example.printLine(s123);

		ProductSet s123123 = s123.append(s123);
		Example.printLine(s123123);

		ProductSet s1212 = ProductSet.getInstance(s12, 2);
		Example.printLine(s1212);

		ProductSet s22 = ProductSet.getInstance(s2, 2);
		Example.printLine(s22);

		ProductSet s12_22 = ProductSet.getInstance(s12, s22);
		Example.printLine(s12_22);
	}

	public static void example2() {
		Set z2 = ZMod.getInstance(2);
		Set z3 = ZMod.getInstance(3);
		Set z4 = ZMod.getInstance(4);
		Set z5 = ZMod.getInstance(5);
		Set z6 = ZMod.getInstance(6);
		Set z7 = ZMod.getInstance(7);
		Set z8 = ZMod.getInstance(8);
		Set z9 = ZMod.getInstance(9);
		ProductSet ps = ProductSet.getInstance(z2, z3, z4, z5, z6, z7, z8, z9);

		Example.printLine(ps);
		Example.printLine(ps.extract(2, 4));
		Example.printLines(ps.split(2, 4, 7));

		Tuple t = ps.getRandomElement();
		Example.printLine(t);
		Example.printLine(t.extract(2, 4));
		Example.printLines(t.split(2, 4, 7));

	}

	public static void main(final String[] args) {
		Example.runExamples();
	}

}
