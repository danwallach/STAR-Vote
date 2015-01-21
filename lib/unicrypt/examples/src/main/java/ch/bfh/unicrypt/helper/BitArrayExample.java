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
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.array.classes.BitArray;
import java.util.Arrays;

/**
 *
 * @author Rolf Haenni <rolf.haenni@bfh.ch>
 */
public class BitArrayExample {

	public static void example1() {

		BitArray bitArray = BitArray.getInstance("000101010000110");

		Example.printLine(bitArray);
		Example.printLine("Length        ", bitArray.getLength());
		Example.printLine("Trailing Zeros", bitArray.countPrefix());
		Example.printLine("Leading Zeros ", bitArray.countSuffix());
		Example.printLine("Count Zeros   ", bitArray.count(false));
		Example.printLine("Count Ones    ", bitArray.count(true));
		Example.printLine("Extract       ", bitArray.extract(2, 9));
		Example.printLine("Extract Prefix", bitArray.extractPrefix(3));
		Example.printLine("Extract Suffix", bitArray.extractSuffix(5));
		Example.printLine("Extract Range ", bitArray.extractRange(7, 9));
		Example.printLine("Get All       ", Arrays.toString(bitArray.getBits()));
		Example.printLine("RemoveAt      ", bitArray.removeAt(7));
		Example.printLine("Shif Left     ", bitArray.shiftLeft(3));
		Example.printLine("Shift Right   ", bitArray.shiftRight(3));
		Example.printLines("Split         ", bitArray.split(2, 4, 10));
		Example.printLine("Strip L.Zeros ", bitArray.removeSuffix());
		Example.printLine("Strip T.Zeros ", bitArray.removePrefix());
		Example.printLine("Strip Prefix  ", bitArray.removePrefix(3));
		Example.printLine("Strip Suffix  ", bitArray.removeSuffix(3));
		Example.printLine("Concatenate   ", bitArray.append(bitArray));
	}

	public static void example2() {

		BitArray bitArray1 = BitArray.getInstance();
		BitArray bitArray2 = BitArray.getInstance("110101110001111");
		BitArray bitArray3 = BitArray.getInstance(new boolean[10]);
		BitArray bitArray4 = BitArray.getInstance(new byte[3]);
		BitArray bitArray5 = BitArray.getInstance(10);
		BitArray bitArray6 = BitArray.getInstance(false, 10);
		BitArray bitArray7 = BitArray.getInstance(true, 10);
		BitArray bitArray8 = BitArray.getRandomInstance(20);

		Example.printLine(bitArray1);
		Example.printLine(bitArray2);
		Example.printLine(bitArray3);
		Example.printLine(bitArray4);
		Example.printLine(bitArray5);
		Example.printLine(bitArray6);
		Example.printLine(bitArray7);
		Example.printLine(bitArray8);
	}

	public static void main(final String[] args) {
		Example.runExamples();
	}

}
