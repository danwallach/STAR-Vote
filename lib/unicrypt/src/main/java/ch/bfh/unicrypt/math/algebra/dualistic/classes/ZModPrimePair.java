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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes;

import ch.bfh.unicrypt.helper.factorization.Prime;
import ch.bfh.unicrypt.helper.factorization.PrimePair;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.ZStarModPrimePair;
import ch.bfh.unicrypt.random.classes.HybridRandomByteSequence;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author rolfhaenni
 */
public class ZModPrimePair
	   extends ZMod {

	PrimePair primePair;

	protected ZModPrimePair(PrimePair primePair) {
		super(primePair.getValue());
		this.primePair = primePair;
	}

	public PrimePair getPrimePair() {
		return this.primePair;
	}

	public BigInteger getFirstPrime() {
		return this.getPrimePair().getFirst();
	}

	public BigInteger getSecondPrime() {
		return this.getPrimePair().getSecond();
	}

	@Override
	public ZModPrimePair getZModOrder() {
		return ZModPrimePair.getInstance(this.getPrimePair());
	}

	@Override
	public ZStarModPrimePair getZStarModOrder() {
		return ZStarModPrimePair.getInstance(this.getPrimePair());
	}

	private static final Map<BigInteger, ZModPrimePair> instances = new HashMap<BigInteger, ZModPrimePair>();

	public static ZModPrimePair getInstance(final PrimePair primePair) {
		if (primePair == null) {
			throw new IllegalArgumentException();
		}
		ZModPrimePair instance = ZModPrimePair.instances.get(primePair.getValue());
		if (instance == null) {
			instance = new ZModPrimePair(primePair);
			ZModPrimePair.instances.put(primePair.getValue(), instance);
		}
		return instance;
	}

	public static ZModPrimePair getInstance(final int prime1, final int prime2) {
		return ZModPrimePair.getInstance(BigInteger.valueOf(prime1), BigInteger.valueOf(prime2));
	}

	public static ZModPrimePair getInstance(BigInteger prime1, BigInteger prime2) {
		return ZModPrimePair.getInstance(PrimePair.getInstance(prime1, prime2));
	}

	public static ZModPrimePair getInstance(Prime prime1, Prime prime2) {
		return new ZModPrimePair(PrimePair.getInstance(prime1, prime2));
	}

	public static ZModPrimePair getRandomInstance(int bitLength, RandomByteSequence randomByteSequence) {
		return new ZModPrimePair(PrimePair.getRandomInstance(bitLength, randomByteSequence));
	}

	public static ZModPrimePair getRandomInstance(int bitLength) {
		return ZModPrimePair.getRandomInstance(bitLength, HybridRandomByteSequence.getInstance());
	}

}
