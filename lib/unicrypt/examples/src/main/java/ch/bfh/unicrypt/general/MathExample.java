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
package lib.unicrypt.examples.src.main.java.ch.bfh.unicrypt.general;

import ch.bfh.unicrypt.Example;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.Alphabet;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.array.classes.ByteArray;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.bytetree.ByteTree;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.concatenative.classes.StringMonoid;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.N;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.Z;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModPrime;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.interfaces.DualisticElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.FiniteStringElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.FiniteStringSet;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.PermutationGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductSet;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Group;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModSafePrime;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.multiplicative.interfaces.MultiplicativeElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.CompositeFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.HashFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.PermutationFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.ProductFunction;
import java.math.BigInteger;

/**
 *
 * @author Rolf Haenni <rolf.haenni@bfh.ch>
 */
public class MathExample {

	public static void example1() {

		// Define finite string set of length 2-5
		FiniteStringSet fss = FiniteStringSet.getInstance(Alphabet.LOWER_CASE, 2, 5);
		BigInteger order = fss.getOrder();

		// Define some string element
		FiniteStringElement e1 = fss.getElement("hello");
		Element<String> e2 = fss.getElement("world");
		FiniteStringElement e3 = fss.getRandomElement();

		// Compute BigInteger, byte array, hash value, byte tree
		String v = e1.getValue();
		BigInteger i = e1.getBigInteger();
		ByteArray a = e1.getByteArray();
		ByteArray h = e1.getHashValue();
		ByteTree t = e1.getByteTree();

		// Print results
		Example.printLine("Set", fss);
		Example.printLine("Order", order);
		Example.printLines("Elements", e1, e2, e3);
		Example.printLine("Value", v);
		Example.printLine("BigInteger", i);
		Example.printLine("ByteArray", a);
		Example.printLine("Hash", h);
		Example.printLine("ByteTree", t);
		Example.printLine("ByteTreeArray", t.getByteArray());
	}

	public static void example2() {

		// Define group and compute order q = (23-1)/2 = 11
		GStarModSafePrime g11 = GStarModSafePrime.getInstance(23);
		BigInteger order = g11.getOrder();

		// Iterate over all elements
		for (Element e : g11.getElements()) {
			Example.printLine(e);
		}

		// Define two group elements
		GStarModElement e1 = g11.getElement(3);
		MultiplicativeElement e2 = g11.getElement(9);

		// Compute product: 3*9 mod 23 = 4
		Element e3_1 = g11.multiply(e1, e2);
		Element e3_2 = e1.multiply(e2);

		// Compute multiplicative inverse
		Element e4_1 = g11.oneOver(e1);
		Element e4_2 = e1.oneOver();

		// Select default generator and comute power
		MultiplicativeElement generator = g11.getDefaultGenerator();
		Element e5_1 = g11.power(generator, 5);
		Element e5_2 = generator.power(5);

		// Print results
		Example.printLine("Group", g11);
		Example.printLine("Order", order);
		Example.printLines("Product", e3_1, e3_2);
		Example.printLines("Inverse", e4_1, e4_2);
		Example.printLines("Generator", generator);
		Example.printLines("Power", e5_1, e5_2);
	}

	public static void example3() {

		// Define group and compute order q = (23-1)/2 = 11
		ZModPrime z11 = ZModPrime.getInstance(11);
		BigInteger order = z11.getOrder();

		// Iterate over all elements
		for (Element e : z11.getElements()) {
			Example.printLine(e);
		}

		// Define two group elements
		ZModElement e1 = z11.getElement(2);
		DualisticElement e2 = z11.getElement(3);
		DualisticElement e3 = z11.getElement(7);

		// Compute new elements
		Element e4 = e1.add(e2).multiply(e3);
		Element e5 = z11.multiply(e1, e2, e3).power(3);
		Element e6 = e1.subtract(e3).oneOver().square();

		// Print results
		Example.printLine("Group", z11);
		Example.printLine("Order", order);
		Example.printLines("Elements", e1, e2, e3);
		Example.printLines("Results", e4, e5, e6);
	}

	public static void example4() {
		// Generate 3 atomic sets
		Z s1 = Z.getInstance();
		N s2 = N.getInstance();
		StringMonoid s3 = StringMonoid.getInstance(Alphabet.LOWER_CASE);

		// Generate s1 x s2, (s1 x s2)^3 and (s1 x s2)^3 x s3
		ProductSet s4 = ProductSet.getInstance(s1, s2);
		ProductSet s5 = ProductSet.getInstance(s4, 3);
		ProductSet s6 = s5.add(s3);

		// Define tuples for s4
		Tuple t4_1 = Tuple.getInstance(s1.getElement(10), s2.getElement(5));
		Tuple t4_2 = s4.getElement(s1.getElement(10), s2.getElement(5));

		// Define tuples for s5
		Tuple t5_1 = Tuple.getInstance(t4_1, 3);
		Tuple t5_2 = s5.getElement(t4_1, t4_1, t4_1);

		// Generate tuple for s6 and convert it
		Tuple t6 = t5_1.add(s3.getElement("hello"));
		BigInteger i = t6.getBigInteger();
		ByteArray a = t6.getByteArray();
		ByteArray h = t6.getHashValue();
		ByteTree t = t6.getByteTree();

		// Print results
		Example.printLines("Atomic sets", s1, s2, s3);
		Example.printLines("Product sets", s4, s5, s6);
		Example.printLines("Tuples", t4_1, t4_2, t5_1, t5_2, t6);
		Example.printLine("BigInteger", i);
		Example.printLine("ByteArray", a);
		Example.printLine("Hash", h);
		Example.printLine("ByteTree", t);
		Example.printLine("ByteTreeArray", t.getByteArray());
	}

	public static void example5() {

		// Generate random permutation of size 5
		Group group = PermutationGroup.getInstance(5);
		Element permutation = group.getRandomElement();

		// Define permutation function Z_23^5 -> Z_23^5
		Group z23 = ZModPrime.getInstance(23);
		PermutationFunction p = PermutationFunction.getInstance(z23, 5);

		// Pick random domain element from Z_23^10 and permute it
		Tuple tuple = ProductSet.getInstance(z23, 5).getRandomElement();
		Tuple permutedTuple = p.apply(tuple, permutation);

		// Create and append hash function
		HashFunction h = HashFunction.getInstance(z23);
		CompositeFunction ph = CompositeFunction.getInstance(p, ProductFunction.getInstance(h, 5));
		Element hashedTuple = ph.apply(tuple, permutation);

		// Print results
		Example.printLine("Permutation", permutation);
		Example.printLines("Input Tuple", tuple);
		Example.printLines("Permuted Tuple", permutedTuple);
		Example.printLines("Hashed Tuple", hashedTuple);
	}

	public static void main(final String[] args) {
		Example.runExamples();
	}

}
