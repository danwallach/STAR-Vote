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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.multiplicative.classes;

import ch.bfh.unicrypt.helper.factorization.PrimePair;
import ch.bfh.unicrypt.random.classes.HybridRandomByteSequence;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author rolfhaenni
 */
public class ZStarModPrimePair
	   extends ZStarMod {

	private final static Map<BigInteger, ZStarModPrimePair> instances = new HashMap<BigInteger, ZStarModPrimePair>();

	protected ZStarModPrimePair(PrimePair primePair) {
		super(primePair);
	}

	public static ZStarModPrimePair getInstance(final PrimePair primePair) {
		if (primePair == null) {
			throw new IllegalArgumentException();
		}
		ZStarModPrimePair instance = ZStarModPrimePair.instances.get(primePair.getValue());
		if (instance == null) {
			instance = new ZStarModPrimePair(primePair);
			ZStarModPrimePair.instances.put(primePair.getValue(), instance);
		}
		return instance;
	}

	public static ZStarModPrimePair getInstance(final int p, final int q) {
		return ZStarModPrimePair.getInstance(BigInteger.valueOf(p), BigInteger.valueOf(q));
	}

	public static ZStarModPrimePair getInstance(final BigInteger p, final BigInteger q) {
		return ZStarModPrimePair.getInstance(PrimePair.getInstance(p, q));
	}

	public static ZStarModPrimePair getRandomInstance(int bitLength) {
		return ZStarModPrimePair.getRandomInstance(bitLength, HybridRandomByteSequence.getInstance());
	}

	public static ZStarModPrimePair getRandomInstance(int bitLength, RandomByteSequence randomByteSequence) {
		return ZStarModPrimePair.getInstance(PrimePair.getRandomInstance(bitLength, randomByteSequence));
	}

}
