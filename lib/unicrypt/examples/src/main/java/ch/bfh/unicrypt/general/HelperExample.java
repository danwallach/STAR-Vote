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
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.array.classes.ByteArray;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.bytetree.ByteTree;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.hash.HashAlgorithm;

/**
 *
 * @author Rolf Haenni <rolf.haenni@bfh.ch>
 */
public class HelperExample {

	public static void example1() {

		// Define byte arrays
		ByteArray b1 = ByteArray.getInstance("F1|62|23|C4|25|86|A7");
		ByteArray b2 = ByteArray.getInstance(10, 254, 120);
		ByteArray b3 = ByteArray.getRandomInstance(7);

		// Perform operations
		ByteArray b4 = b1.extract(2, 4);
		ByteArray b5 = b1.append(b2);
		ByteArray b6 = b1.xor(b3);

		// Compute hash values
		ByteArray b7 = b1.getHashValue();
		ByteArray b8 = b1.getHashValue(HashAlgorithm.MD5);

		// Print results
		Example.printLines("ByteArrays", b1, b2, b3);
		Example.printLine("Extract", b4);
		Example.printLine("Concatenate", b5);
		Example.printLine("XOR", b6);
		Example.printLine("SHA-256", b7);
		Example.printLine("MD5", b8);
	}

	public static void example2() {

		// Define multiple byte tree leaves
		ByteTree bt1 = ByteTree.getInstance(ByteArray.getInstance("1A|43"));
		ByteTree bt2 = ByteTree.getInstance(ByteArray.getInstance("71|B2|29"));
		ByteTree bt3 = ByteTree.getInstance(ByteArray.getInstance("2F"));
		ByteTree bt4 = ByteTree.getInstance(ByteArray.getInstance("C4|B2"));

		// Combine bt3, bt4
		ByteTree bt5 = ByteTree.getInstance(bt3, bt4);

		// Combine bt1, bt2, bt5
		ByteTree byteTree = ByteTree.getInstance(bt1, bt2, bt5);

		// Convert ByteTree to ByteArray
		ByteArray byteArray = byteTree.getByteArray();

		// Convert ByteArray to ByteTree
		ByteTree recoveredByteTree = ByteTree.getInstanceFrom(byteArray);

		// Print results
		Example.printLines("Nodes", bt1, bt2, bt3, bt4, bt5);
		Example.printLine("ByteTree ", byteTree);
		Example.printLine("ByteArray", byteArray);
		Example.printLine("Recovered", recoveredByteTree);
	}

	public static void main(final String[] args) {
		Example.runExamples();
	}

}
