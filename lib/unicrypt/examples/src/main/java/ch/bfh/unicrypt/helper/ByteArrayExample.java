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

/**
 *
 * @author Rolf Haenni <rolf.haenni@bfh.ch>
 */
public class ByteArrayExample {

	public static void example1() {
		ByteArray byteArray = ByteArray.getInstance(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});

		Example.printLine(byteArray);
		Example.printLine("Length    ", byteArray.getLength());
		Example.printLine("Bytes     ", byteArray);

		Example.printLine("Extract   ", byteArray.extract(2, 4));
		Example.printLine("RemoveAt  ", byteArray.removeAt(6));
		Example.printLine("ShiftLeft ", byteArray.shiftLeft(7));
		Example.printLine("ShiftRight", byteArray.shiftRight(7));

		Object[] byteArrays = byteArray.split(2, 4, 7);
		Example.printLine("Split     ", byteArrays);
	}

	public static void example2() {
		ByteArray byteArray1 = ByteArray.getInstance(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
		ByteArray byteArray2 = ByteArray.getInstance("E3|85|72|37|08|19");
		ByteArray byteArray3 = ByteArray.getRandomInstance(5);

		Example.printLine(byteArray1);
		Example.printLine(byteArray2);
		Example.printLine(byteArray3);

		Example.printLine("Conc   ", byteArray1.append(byteArray2).append(byteArray3));
	}

	public static void example3() {
		ByteArray byteArray1 = ByteArray.getInstance(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
		ByteArray byteArray2 = byteArray1.reverse();

		Example.printLine(byteArray1);
		Example.printLine(byteArray2);

		Example.printLine("Extract   ", byteArray2.extract(2, 4));
		Example.printLine("RemoveAt  ", byteArray2.removeAt(6));
		Example.printLine("ShiftLeft ", byteArray2.shiftLeft(7));
		Example.printLine("ShiftRight", byteArray2.shiftRight(7));

		Object[] byteArrays = byteArray2.split(2, 4, 7);
		Example.printLine("Split     ", byteArrays);
	}

	public static void example4() {
		ByteArray byteArray1 = ByteArray.getInstance(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
		ByteArray byteArray2 = byteArray1.extract(1, 8).reverse();

		Example.printLine(byteArray1);
		Example.printLine(byteArray2);

		Example.printLine("Extract   ", byteArray2.extract(2, 4));
		Object[] byteArrays = byteArray2.split(2, 4);
		Example.printLine("Split     ", byteArrays);
		Example.printLine("Append    ", byteArray1.append(byteArray2));
		Example.printLine("Append    ", byteArray2.append(byteArray1));
		Example.printLine("ShiftLeft ", byteArray2.shiftLeft(7));
		Example.printLine("ShiftRight ", byteArray2.shiftRight(7));
	}

	public static void example5() {
		ByteArray byteArray1 = ByteArray.getInstance(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}).extract(1, 7);

		Example.printLine(byteArray1);
		for (int i = 0; i < 16; i++) {
			Example.printLine("GetBitAt", byteArray1.getBitAt(i));
		}
		Example.printLine("0-Bits", byteArray1.countZeroBits());
		Example.printLine("1-Bits", byteArray1.countOneBits());
		Example.printLine("Leading Zeros", byteArray1.countLeadingZeroBits());
		Example.printLine("Trailing Zeros", byteArray1.countTrailingZeroBits());

		ByteArray byteArray2 = byteArray1.reverse();

		Example.printLine(byteArray2);
		for (int i = 0; i < 16; i++) {
			Example.printLine("GetBitAt", byteArray2.getBitAt(i));
		}
		Example.printLine("Count 0-Bits ", byteArray2.countZeroBits());
		Example.printLine("Count 1-Bit s", byteArray2.countOneBits());
		Example.printLine("Leading Zeros", byteArray2.countLeadingZeroBits());
		Example.printLine("Trailing Zeros", byteArray2.countTrailingZeroBits());
	}

	public static void example6() {
		ByteArray byteArray = ByteArray.getInstance(new byte[]{64});

		Example.printLine(byteArray);
		for (int i = 0; i < 10; i++) {
			Example.printLine("Left ", byteArray.shiftBitsRight(i));
		}
		for (int i = 0; i < 10; i++) {
			Example.printLine("Right", byteArray.shiftBitsLeft(i));
		}
	}

	public static void main(final String[] args) {
		Example.runExamples();
	}

}
