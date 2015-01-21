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
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.hash.HashAlgorithm;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.random.classes.HybridRandomByteSequence;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.random.interfaces.TrueRandomByteSequence;

/**
 *
 * @author Reto E. Koenig <reto.koenig@bfh.ch>
 */
public class TrueRandomByteSequenceExample {

	public static void example1() {
		System.out.println("Use a true default true random 'generator' for ephemeral keys.");
		System.out.println("The initial seeding is a blocking method");
		TrueRandomByteSequence random = HybridRandomByteSequence.getInstance();

		System.out.println("The refresh of the internal state is non-blocking");
		System.out.println("Security (Backward) in bits: " + random.getBackwardSecurityInBytes() * 8);
		System.out.println("Security (Forward) in bits:" + random.getForwardSecurityInBytes() * 8);
		System.out.println("Get a single Byte: " + random.getNextByte());
		System.out.println("Get a ByteArray: " + random.getNextByteArray(32));
		System.out.println("Get a boolean via RandomNumberGenerator " + random.getRandomNumberGenerator().nextBoolean());
		System.out.println("Get a BigInteger via RandomNumberGenerator " + random.getRandomNumberGenerator().nextBigInteger(42));
		System.out.println("Get a savePrime via RandomNumberGenerator " + random.getRandomNumberGenerator().nextSavePrime(10));
		random = null;
		System.gc();

	}

	public static void example2() {
		System.out.println("Use a weakest true random 'generator' for ephemeral keys.");
		System.out.println("The initial seeding is a blocking method");
		TrueRandomByteSequence random = HybridRandomByteSequence.getInstance(HashAlgorithm.getInstance(), 1, 1);

		System.out.println("The refresh of the internal state is non-blocking");
		System.out.println("Security (Backward) in bits: " + random.getBackwardSecurityInBytes() * 8);
		System.out.println("Security (Forward) in bits:" + random.getForwardSecurityInBytes() * 8);
		System.out.println("Get a single Byte: " + random.getNextByte());
		System.out.println("Get a ByteArray: " + random.getNextByteArray(32));
		System.out.println("Get a boolean via RandomNumberGenerator " + random.getRandomNumberGenerator().nextBoolean());
		System.out.println("Get a BigInteger via RandomNumberGenerator " + random.getRandomNumberGenerator().nextBigInteger(42));
		System.out.println("Get a savePrime via RandomNumberGenerator " + random.getRandomNumberGenerator().nextSavePrime(10));
		random = null;
		System.gc();
	}

	public static void example3() {
		System.out.println("Use a strongest (in relation to the used cryptographic hash function) true random 'generator' for ephemeral keys.");
		System.out.println("The initial seeding is a blocking method");
		TrueRandomByteSequence random = HybridRandomByteSequence.getInstance(HashAlgorithm.getInstance(), HashAlgorithm.getInstance().getHashLength() - 1, HashAlgorithm.getInstance().getHashLength());

		System.out.println("The refresh of the internal state is non-blocking");
		System.out.println("Security (Backward) in bits: " + random.getBackwardSecurityInBytes() * 8);
		System.out.println("Security (Forward) in bits:" + random.getForwardSecurityInBytes() * 8);
		System.out.println("Get a single Byte: " + random.getNextByte());
		System.out.println("Get a ByteArray: " + random.getNextByteArray(32));
		System.out.println("Get a boolean via RandomNumberGenerator " + random.getRandomNumberGenerator().nextBoolean());
		System.out.println("Get a BigInteger via RandomNumberGenerator " + random.getRandomNumberGenerator().nextBigInteger(42));
		System.out.println("Get a savePrime via RandomNumberGenerator " + random.getRandomNumberGenerator().nextSavePrime(10));
		random = null;
		System.gc();
	}

	public static void main(final String[] args) {
		Example.runExamples();
//		System.out.println("The actual true random 'generator' is seeded and refreshed via a distribution sampler which gets its sample from /dev/random.");
//		System.out.println("Other sensors will be accessed for distribution sampling in the near future.");
//		System.out.println("If Example 3 is blocking now, an internal refresh of the random instance is in progress... Please be patient or do some WebSurfing!");
	}

}
