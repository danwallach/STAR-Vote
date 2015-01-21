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
package lib.unicrypt.examples.src.main.java.ch.bfh.unicrypt.helper;

import ch.bfh.unicrypt.Example;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.array.classes.ByteArray;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.bytetree.ByteTree;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.N;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Pair;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.PermutationGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductSet;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;

/**
 *
 * @author Reto E. Koenig <reto.koenig@bfh.ch>
 */
public class ByteTreeExample {

	public static void example1() {

		// Define multiple byte tree leaves
		ByteTree l1 = ByteTree.getInstance(ByteArray.getInstance(1));
		ByteTree l2 = ByteTree.getInstance(ByteArray.getInstance(2));
		ByteTree l3 = ByteTree.getInstance(ByteArray.getInstance(3));
		ByteTree l4 = ByteTree.getInstance(ByteArray.getInstance(4));
		ByteTree l5 = ByteTree.getInstance(ByteArray.getInstance(1, 0));

		// Combine l1 to l3
		ByteTree n1 = ByteTree.getInstance(l1, l2, l3);

		// Combine l4 and l5
		ByteTree n2 = ByteTree.getInstance(l4, l5);

		// Combine n1 and n2
		ByteTree byteTree = ByteTree.getInstance(n1, n2);
		ByteArray byteArray = byteTree.getByteArray();

		Example.printLines("Leaves", l1, l2, l3, l4, l5);
		Example.printLines("Nodes", n1, n2);
		Example.printLines("ByteTree/ByteArray", byteTree, byteArray);
	}

	public static void example2() {

		// The same as Example 1, but using UniCrypt elements
		N nSet = N.getInstance();

		// Define multiple natural numbers
		Element e1 = nSet.getElement(1);
		Element e2 = nSet.getElement(2);
		Element e3 = nSet.getElement(3);
		Element e4 = nSet.getElement(4);
		Element e5 = nSet.getElement(256);

		// Combine e1 to e3
		Tuple t1 = Tuple.getInstance(e1, e2, e3);
		ByteTree n1 = t1.getByteTree();

		// Combine e4 and e5
		Tuple t2 = Tuple.getInstance(e4, e5);
		ByteTree n2 = t2.getByteTree();

		// Combine t1 and t2
		Tuple tuple = Tuple.getInstance(t1, t2);
		ByteTree byteTree = tuple.getByteTree();
		ByteArray byteArray = byteTree.getByteArray();

		Example.printLines("Elements", e1, e2, e3, e4, e5);
		Example.printLines("Tuples/Nodes", t1, n1, t2, n2);
		Example.printLines("Tuple/ByteTree/ByteArray", tuple, byteTree, byteArray);
	}

	public static void example3() {

		// Same as Example 1
		ByteTree l1 = ByteTree.getInstance(ByteArray.getInstance(1));
		ByteTree l2 = ByteTree.getInstance(ByteArray.getInstance(2));
		ByteTree l3 = ByteTree.getInstance(ByteArray.getInstance(3));
		ByteTree l4 = ByteTree.getInstance(ByteArray.getInstance(4));
		ByteTree l5 = ByteTree.getInstance(ByteArray.getInstance(1, 0));
		ByteTree n1 = ByteTree.getInstance(l1, l2, l3);
		ByteTree n2 = ByteTree.getInstance(l4, l5);
		ByteTree byteTree = ByteTree.getInstance(n1, n2);
		ByteArray byteArray = byteTree.getByteArray();

		// Reconstruct byte tree from byte array
		ByteTree result = ByteTree.getInstanceFrom(byteArray);

		Example.printLine("ByteTree", byteTree);
		Example.printLine("ByteArray", byteArray);
		Example.printLine("Result", result);
	}

	public static void example4() {

		// Same as Example 2
		N nSet = N.getInstance();
		Element e1 = nSet.getElement(1);
		Element e2 = nSet.getElement(2);
		Element e3 = nSet.getElement(3);
		Element e4 = nSet.getElement(4);
		Element e5 = nSet.getElement(256);
		Tuple t1 = Tuple.getInstance(e1, e2, e3);
		Tuple t2 = Tuple.getInstance(e4, e5);
		Tuple tuple = Tuple.getInstance(t1, t2);
		ByteTree byteTree = tuple.getByteTree();
		ByteArray byteArray = byteTree.getByteArray();

		// Reconstruct tuple from byte array
		ProductSet set = tuple.getSet();
		ByteTree recByteTree = ByteTree.getInstanceFrom(byteArray);
		Tuple recTuple = set.getElementFrom(recByteTree);

		Example.printLines("Tuples", tuple, recTuple);
		Example.printLines("ByteTrees", byteTree, recByteTree);
	}

	public static void example5() {

		// Construct two permutation elements
		PermutationGroup group = PermutationGroup.getInstance(5);
		Element p1 = group.getRandomElement();
		Element p2 = group.getRandomElement();

		// Construct pair (p1,p2) and convert to byte tree
		Pair pair = Pair.getInstance(p1, p2);
		ByteTree byteTree = pair.getByteTree();
		ByteArray byteArray = byteTree.getByteArray();

		// Reconstruct tuple from byte array
		ProductSet set = pair.getSet();
		ByteTree recByteTree = ByteTree.getInstanceFrom(byteArray);
		Tuple recPair = set.getElementFrom(recByteTree);

		Example.printLine("Pair", pair);
		Example.printLines("ByteTree/ByteArray", byteTree, byteArray);
		Example.printLines("Recovered ByteTree/Pair", recByteTree, recPair);
	}

	public static void main(String[] args) {
		Example.runExamples();
	}

}
