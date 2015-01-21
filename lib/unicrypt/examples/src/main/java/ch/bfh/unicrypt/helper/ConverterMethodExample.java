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
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.converter.classes.ConvertMethod;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.converter.classes.bytearray.BigIntegerToByteArray;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.converter.classes.bytearray.StringToByteArray;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.hash.HashMethod;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.hash.HashMethod.Mode;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.concatenative.classes.StringElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.concatenative.classes.StringMonoid;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZMod;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.PermutationElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.PermutationGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductSet;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 *
 * @author Rolf Haenni <rolf.haenni@bfh.ch>
 */
public class ConverterMethodExample {

	public static void example1() {

		// String converter
		StringToByteArray stringConverter = StringToByteArray.getInstance(Charset.forName("UTF-8"));

		// BigIntegerToByteArray
		BigIntegerToByteArray bigIntegerConverter = BigIntegerToByteArray.getInstance(ByteOrder.LITTLE_ENDIAN);

		// Two ConvertMethods
		ConvertMethod convertMethod1 = ConvertMethod.getInstance();
		ConvertMethod convertMethod2 = ConvertMethod.getInstance(bigIntegerConverter);
		ConvertMethod convertMethod3 = ConvertMethod.getInstance(stringConverter);
		ConvertMethod convertMethod4 = ConvertMethod.getInstance(stringConverter, bigIntegerConverter);

		// String monoid
		StringMonoid set = StringMonoid.getInstance(Alphabet.DECIMAL);

		// String element
		StringElement element = set.getElement("1234");

		Example.printLine(set);
		Example.printLine(element);

		Example.printLine("BigInteger", element.getBigInteger());

		// Converting the element to byte array (forth and back)
		Example.printLabelLine("CONVERSION TO BYTE ARRAY");

		ByteArray byteArray;
		byteArray = element.getByteArray();
		Example.printLine(set.getElementFrom(byteArray), byteArray);

		byteArray = element.getByteArray(stringConverter);
		Example.printLine(set.getElementFrom(byteArray, stringConverter), byteArray);

		byteArray = element.getByteArray(convertMethod1);
		Example.printLine(set.getElementFrom(byteArray, convertMethod1), byteArray);

		byteArray = element.getByteArray(convertMethod2);
		Example.printLine(set.getElementFrom(byteArray, convertMethod2), byteArray);

		byteArray = element.getByteArray(convertMethod3);
		Example.printLine(set.getElementFrom(byteArray, convertMethod3), byteArray);

		byteArray = element.getByteArray(convertMethod4);
		Example.printLine(set.getElementFrom(byteArray, convertMethod4), byteArray);

		// Converting the element to byte tree (forth and back)
		Example.printLabelLine("CONVERSION TO BYTE TREE");

		ByteTree byteTree;
		byteTree = element.getByteTree();
		Example.printLine(set.getElementFrom(byteTree), byteTree);

		byteTree = element.getByteTree(stringConverter);
		Example.printLine(set.getElementFrom(byteTree, stringConverter), byteTree);

		byteTree = element.getByteTree(convertMethod1);
		Example.printLine(set.getElementFrom(byteTree, convertMethod1), byteTree);

		byteTree = element.getByteTree(convertMethod2);
		Example.printLine(set.getElementFrom(byteTree, convertMethod2), byteTree);

		byteTree = element.getByteTree(convertMethod3);
		Example.printLine(set.getElementFrom(byteTree, convertMethod3), byteTree);

		byteTree = element.getByteTree(convertMethod4);
		Example.printLine(set.getElementFrom(byteTree, convertMethod4), byteTree);
	}

	public static void example2() {

		// Two converters
		StringToByteArray stringConverter = StringToByteArray.getInstance(Charset.forName("UTF-8"));
		BigIntegerToByteArray bigIntegerConverter = BigIntegerToByteArray.getInstance(ByteOrder.LITTLE_ENDIAN);

		// Two ConvertMethods
		ConvertMethod convertMethod1 = ConvertMethod.getInstance(stringConverter);
		ConvertMethod convertMethod2 = ConvertMethod.getInstance(stringConverter, bigIntegerConverter);

		// Three sets
		StringMonoid s1 = StringMonoid.getInstance(Alphabet.DECIMAL);
		ZMod s2 = ZMod.getInstance(33);
		PermutationGroup s3 = PermutationGroup.getInstance(5);

		// Three elements
		StringElement e1 = s1.getElement("1234");
		ZModElement e2 = s2.getElement(5);
		PermutationElement e3 = s3.getElement(Permutation.getInstance(5));

		// Tuple and ProductSet
		ProductSet productSet = ProductSet.getInstance(s1, s2, s3);
		Tuple tuple = productSet.getElement(e1, e2, e3);

		Example.printLine(e1, e2, e3);
		Example.printLine(tuple);

		Example.printLine("BigInteger", tuple.getBigInteger());

		// Converting the tuple to byte array forth and back
		Example.printLabelLine("CONVERSION TO BYTE ARRAY");

		ByteArray byteArray;
		byteArray = tuple.getByteArray();
		Example.printLine(productSet.getElementFrom(byteArray), byteArray);

		byteArray = tuple.getByteArray(convertMethod1);
		Example.printLine(productSet.getElementFrom(byteArray, convertMethod1), byteArray);

		byteArray = tuple.getByteArray(convertMethod2);
		Example.printLine(productSet.getElementFrom(byteArray, convertMethod2), byteArray);

		// Converting the tuple to byte tree forth and back
		Example.printLabelLine("CONVERSION TO BYTE TREE");

		ByteTree byteTree;
		byteTree = tuple.getByteTree();
		Example.printLine(productSet.getElementFrom(byteTree), byteTree);

		byteTree = tuple.getByteTree(convertMethod1);
		Example.printLine(productSet.getElementFrom(byteTree, convertMethod1), byteTree);

		byteTree = tuple.getByteTree(convertMethod2);
		Example.printLine(productSet.getElementFrom(byteTree, convertMethod2), byteTree);

		// Computing hash values
		Example.printLabelLine("HASH VALUES");

		Example.printLine(tuple.getHashValue(HashMethod.getInstance()));
		Example.printLine(tuple.getHashValue(HashMethod.getInstance(Mode.BYTEARRAY)));
		Example.printLine(tuple.getHashValue(HashMethod.getInstance(Mode.BYTETREE)));
		Example.printLine(tuple.getHashValue(HashMethod.getInstance(Mode.RECURSIVE)));

		Example.printLine(tuple.getHashValue(HashMethod.getInstance(convertMethod1)));
		Example.printLine(tuple.getHashValue(HashMethod.getInstance(convertMethod1, Mode.BYTEARRAY)));
		Example.printLine(tuple.getHashValue(HashMethod.getInstance(convertMethod1, Mode.BYTETREE)));
		Example.printLine(tuple.getHashValue(HashMethod.getInstance(convertMethod1, Mode.RECURSIVE)));

		Example.printLine(tuple.getHashValue(HashMethod.getInstance(convertMethod2)));
		Example.printLine(tuple.getHashValue(HashMethod.getInstance(convertMethod2, Mode.BYTEARRAY)));
		Example.printLine(tuple.getHashValue(HashMethod.getInstance(convertMethod2, Mode.BYTETREE)));
		Example.printLine(tuple.getHashValue(HashMethod.getInstance(convertMethod2, Mode.RECURSIVE)));
	}

	public static void main(final String[] args) {
		Example.runExamples();
	}

}
