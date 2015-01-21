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
package lib.unicrypt.examples.src.main.java.ch.bfh.unicrypt.random;

import ch.bfh.unicrypt.Example;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.random.classes.PseudoRandomOracle;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.random.classes.ReferenceRandomByteSequence;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.random.interfaces.RandomOracle;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.array.classes.ByteArray;
import java.math.BigInteger;

/**
 *
 * @author Reto E. Koenig <reto.koenig@bfh.ch>
 */
public class RandomOracleExample {

	public static void example1() {
		{
			System.out.println("Use the default random Oracle with the default cryptographic hash method");
			RandomOracle oracle = PseudoRandomOracle.getInstance();

			System.out.println("Request the default common random reference 'string' ");
			ReferenceRandomByteSequence referenceString = oracle.getReferenceRandomByteSequence();
			System.out.println("The query in use as ByteArray: " + referenceString.getSeed());

			System.out.println("Get a single Byte: " + referenceString.getNextByte());
			System.out.println("Get a ByteArray: " + referenceString.getNextByteArray(32));
			System.out.println("Get a boolean via RandomNumberGenerator " + referenceString.getRandomNumberGenerator().nextBoolean());
			System.out.println("Get a BigInteger via RandomNumberGenerator " + referenceString.getRandomNumberGenerator().nextBigInteger(42));
			System.out.println("Get a savePrime via RandomNumberGenerator " + referenceString.getRandomNumberGenerator().nextSavePrime(10));
		}
		System.out.println("");
		System.out.println("Doing the same thing again...");
		System.out.println("");
		{
			System.out.println("Use the default random Oracle with the default cryptographic hash method");
			RandomOracle oracle = PseudoRandomOracle.getInstance();

			System.out.println("Request the default common random reference 'string' ");
			ReferenceRandomByteSequence referenceString = oracle.getReferenceRandomByteSequence();
			System.out.println("The query in use as ByteArray: " + referenceString.getSeed());

			System.out.println("Get a single Byte: " + referenceString.getNextByte());
			System.out.println("Get a ByteArray: " + referenceString.getNextByteArray(32));
			System.out.println("Get a boolean via RandomNumberGenerator " + referenceString.getRandomNumberGenerator().nextBoolean());
			System.out.println("Get a BigInteger via RandomNumberGenerator " + referenceString.getRandomNumberGenerator().nextBigInteger(42));
			System.out.println("Get a savePrime via RandomNumberGenerator " + referenceString.getRandomNumberGenerator().nextSavePrime(10));

		}
	}

	public static void example2() {
		{
			System.out.println("Use the default random Oracle with the default cryptographic hash method");
			RandomOracle oracle = PseudoRandomOracle.getInstance();

			System.out.println("Request the common random reference 'string' using a common ByteArray query");
			ByteArray commonQuery = ByteArray.getInstance("This is the common query".getBytes());
			System.out.println("The query to be used as ByteArray: " + commonQuery);
			ReferenceRandomByteSequence referenceString = oracle.getReferenceRandomByteSequence(commonQuery);
			System.out.println("The query in use as ByteArray: " + referenceString.getSeed());

			System.out.println("Get a single Byte: " + referenceString.getNextByte());
			System.out.println("Get a ByteArray: " + referenceString.getNextByteArray(32));
			System.out.println("Get a boolean via RandomNumberGenerator " + referenceString.getRandomNumberGenerator().nextBoolean());
			System.out.println("Get a BigInteger via RandomNumberGenerator " + referenceString.getRandomNumberGenerator().nextBigInteger(42));
			System.out.println("Get a savePrime via RandomNumberGenerator " + referenceString.getRandomNumberGenerator().nextSavePrime(10));
		}
		System.out.println("");
		System.out.println("Doing the same thing again...");
		System.out.println("");
		{
			System.out.println("Use the default random Oracle with the default cryptographic hash method");
			RandomOracle oracle = PseudoRandomOracle.getInstance();

			System.out.println("Request the common random reference 'string' using a common ByteArray query");
			ByteArray commonQuery = ByteArray.getInstance("This is the common query".getBytes());
			System.out.println("The query to be used as ByteArray: " + commonQuery);
			ReferenceRandomByteSequence referenceString = oracle.getReferenceRandomByteSequence(commonQuery);
			System.out.println("The query in use as ByteArray: " + referenceString.getSeed());

			System.out.println("Get a single Byte: " + referenceString.getNextByte());
			System.out.println("Get a ByteArray: " + referenceString.getNextByteArray(32));
			System.out.println("Get a boolean via RandomNumberGenerator " + referenceString.getRandomNumberGenerator().nextBoolean());
			System.out.println("Get a BigInteger via RandomNumberGenerator " + referenceString.getRandomNumberGenerator().nextBigInteger(42));
			System.out.println("Get a savePrime via RandomNumberGenerator " + referenceString.getRandomNumberGenerator().nextSavePrime(10));

		}
	}

	public static void example3() {
		{
			System.out.println("Use the default random Oracle with the default cryptographic hash method");
			RandomOracle oracle = PseudoRandomOracle.getInstance();

			System.out.println("Request the common random reference 'string' using a common BigInteger query");
			BigInteger commonQuery = new BigInteger("123456789012345678901234567890");
			ReferenceRandomByteSequence referenceString = oracle.getReferenceRandomByteSequence(commonQuery);
			System.out.println("The query in use as ByteArray: " + referenceString.getSeed());

			System.out.println("Get a single Byte: " + referenceString.getNextByte());
			System.out.println("Get a ByteArray: " + referenceString.getNextByteArray(32));
			System.out.println("Get a boolean via RandomNumberGenerator " + referenceString.getRandomNumberGenerator().nextBoolean());
			System.out.println("Get a BigInteger via RandomNumberGenerator " + referenceString.getRandomNumberGenerator().nextBigInteger(42));
			System.out.println("Get a savePrime via RandomNumberGenerator " + referenceString.getRandomNumberGenerator().nextSavePrime(10));
		}
		System.out.println("");
		System.out.println("Doing the same thing again...");
		System.out.println("");
		{
			System.out.println("Use the default random Oracle with the default cryptographic hash method");
			RandomOracle oracle = PseudoRandomOracle.getInstance();

			System.out.println("Request the common random reference 'string' using a common BigInteger query");
			BigInteger commonQuery = new BigInteger("123456789012345678901234567890");
			ReferenceRandomByteSequence referenceString = oracle.getReferenceRandomByteSequence(commonQuery);
			System.out.println("The query in use as ByteArray: " + referenceString.getSeed());

			System.out.println("Get a single Byte: " + referenceString.getNextByte());
			System.out.println("Get a ByteArray: " + referenceString.getNextByteArray(32));
			System.out.println("Get a boolean via RandomNumberGenerator " + referenceString.getRandomNumberGenerator().nextBoolean());
			System.out.println("Get a BigInteger via RandomNumberGenerator " + referenceString.getRandomNumberGenerator().nextBigInteger(42));
			System.out.println("Get a savePrime via RandomNumberGenerator " + referenceString.getRandomNumberGenerator().nextSavePrime(10));

		}
	}

	public static void main(final String[] args) {
		Example.runExamples();
	}

}
