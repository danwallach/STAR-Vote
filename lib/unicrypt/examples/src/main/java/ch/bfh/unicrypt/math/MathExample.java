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
package lib.unicrypt.examples.src.main.java.ch.bfh.unicrypt.math;

import ch.bfh.unicrypt.Example;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.Alphabet;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.concatenative.classes.StringElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.concatenative.classes.StringMonoid;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.PolynomialElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.PolynomialRing;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZMod;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModTwo;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import java.math.BigInteger;
import java.util.ArrayList;

/**
 *
 * @author Rolf Haenni <rolf.haenni@bfh.ch>
 */
public class MathExample {

	public static void example1() {
		// Generate Z_23 (specific type)
		ZMod zMod = ZMod.getInstance(23);

		// Option 1: Non-Generic Type (casting required)
		Element e1 = zMod.getElement(5);
		BigInteger v1 = (BigInteger) e1.getValue();
		Example.printLine(e1, v1);

		// Option 2: Generic Type
		Element<BigInteger> e2 = zMod.getElement(7);
		BigInteger v2 = e2.getValue();
		Example.printLine(e2, v2);

		// Option 3: Specific Type
		ZModElement e3 = zMod.getElement(9);
		BigInteger v3 = e3.getValue();
		Example.printLine(e3, v3);
	}

	public static void example2() {
		// Generate String Monoid (specific type)
		StringMonoid strMonoid = StringMonoid.getInstance(Alphabet.DECIMAL);

		// Option 1: Non-Generic Type (casting required)
		Element e1 = strMonoid.getElement("123");
		String str1 = (String) e1.getValue();
		Example.printLine(e1, str1);

		// Option 2: Generic Type
		Element<String> e2 = strMonoid.getElement("1234");
		String str2 = e2.getValue();
		Example.printLine(e2, str2);

		// Option 3: Specific Type
		StringElement e3 = strMonoid.getElement("12345");
		String str3 = e3.getValue();
		Example.printLine(e3, str3);
	}

	public static void example3() {
		// Generate Z_23 (non-generic type)
		Set zMod = ZMod.getInstance(23);

		// Option 1: Non-Generic Type
		Element e1 = zMod.getElementFrom(5);
		Example.printLine(e1);

		// Option 2: Generic Type
		Element<BigInteger> e2 = zMod.getElementFrom(7);
		Example.printLine(e2);

		// Option 3: Specific Type (casting required)
		ZModElement e3 = (ZModElement) zMod.getElementFrom(9);
		Example.printLine(e3);
	}

	public static void example4() {
		// Generate Z_23 (generic type)
		Set<BigInteger> zMod = ZMod.getInstance(23);

		// Option 1: Non-Generic Type
		Element e1 = zMod.getElementFrom(5);
		Example.printLine(e1);

		// Option 2: Generic Type
		Element<BigInteger> e2 = zMod.getElementFrom(7);
		Example.printLine(e2);

		// Option 3: Specific Type (casting required)
		ZModElement e3 = (ZModElement) zMod.getElementFrom(9);
		Example.printLine(e3);
	}

	/**
	 * PolynomialField example Shows how to generate a irreducible Polynom from a BitString
	 */
	public static void example5() {
		ZModTwo primeField = ZModTwo.getInstance();
		PolynomialRing<ZModTwo> ring = PolynomialRing.getInstance(primeField);

		String b = "10100100001000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001";

		BigInteger h = new BigInteger("10100100001000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001", 2);
		Example.printLine(h.toString(16));

		//Read bits and create a BigInteger ArrayList
		ArrayList<BigInteger> arrayBigInteger = new ArrayList<BigInteger>();
		for (Character s : h.toString(2).toCharArray()) {
			arrayBigInteger.add(new BigInteger(s.toString()));

		}

		//Convert ArrayList BigInteger array and get element
		BigInteger[] bigs = {};
		bigs = arrayBigInteger.toArray(bigs);

		PolynomialElement<ZModTwo> irreduciblePolynom = ring.getElement(bigs);
		PolynomialElement<ZModTwo> p1 = irreduciblePolynom;
		Example.printLine(irreduciblePolynom);
		Example.printLine(irreduciblePolynom.isIrreducible());

	}

	public static void main(final String[] args) {
		Example.runExamples();
	}

}
