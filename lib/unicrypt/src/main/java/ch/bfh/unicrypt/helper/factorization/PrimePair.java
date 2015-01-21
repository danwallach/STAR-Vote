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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.factorization;

import ch.bfh.unicrypt.random.classes.HybridRandomByteSequence;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
import java.math.BigInteger;

/**
 *
 * @author rolfhaenni
 */
public class PrimePair
	   extends Factorization {

	protected PrimePair(Prime prime1, Prime prime2) {
		this(prime1.getValue(), prime2.getValue());
	}

	protected PrimePair(BigInteger prime1, BigInteger prime2) {
		super(prime1.multiply(prime2), new BigInteger[]{prime1, prime2}, new int[]{1, 1});
	}

	public BigInteger getFirst() {
		return this.getPrimeFactors()[0];
	}

	public BigInteger getSecond() {
		return this.getPrimeFactors()[1];
	}

	public static PrimePair getInstance(BigInteger prime1, BigInteger prime2) {
		Prime pr1 = Prime.getInstance(prime1);
		Prime pr2 = Prime.getInstance(prime2);
		return PrimePair.getInstance(pr1, pr2);
	}

	public static PrimePair getInstance(Prime prime1, Prime prime2) {
		if (prime1 == null || prime2 == null || prime1.equals(prime2)) {
			throw new IllegalArgumentException();
		}
		return new PrimePair(prime1, prime2);
	}

	public static PrimePair getRandomInstance(int bitLength) {
		return PrimePair.getRandomInstance(bitLength, HybridRandomByteSequence.getInstance());
	}

	public static PrimePair getRandomInstance(int bitLength, RandomByteSequence randomByteSequence) {
		return PrimePair.getInstance(Prime.getRandomInstance(bitLength, randomByteSequence), Prime.getRandomInstance(bitLength, randomByteSequence));
	}

}
