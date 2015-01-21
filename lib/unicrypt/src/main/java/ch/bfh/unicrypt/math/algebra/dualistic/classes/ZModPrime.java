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
import ch.bfh.unicrypt.math.algebra.dualistic.interfaces.PrimeField;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.ZStarModPrime;
import ch.bfh.unicrypt.random.classes.HybridRandomByteSequence;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author rolfhaenni
 */
public class ZModPrime
	   extends ZMod
	   implements PrimeField<BigInteger> {

	protected ZModPrime(Prime prime) {
		super(prime.getValue());
	}

	@Override
	public ZStarModPrime getMultiplicativeGroup() {
		return ZStarModPrime.getInstance(this.modulus);
	}

	@Override
	public ZModElement divide(Element element1, Element element2) {
		return this.multiply(element1, this.oneOver(element2));
	}

	@Override
	public ZModElement oneOver(Element element) {
		if (!this.contains(element)) {
			throw new IllegalArgumentException();
		}
		if (element.isEquivalent(this.getZeroElement())) {
			throw new UnsupportedOperationException();
		}
		return this.abstractGetElement(((ZModElement) element).getValue().modInverse(this.modulus));
	}

	@Override
	public ZModPrime getZModOrder() {
		return ZModPrime.getInstance(this.getOrder());
	}

	@Override
	public ZStarModPrime getZStarModOrder() {
		return ZStarModPrime.getInstance(this.getOrder());
	}

	//
	// STATIC FACTORY METHODS
	//
	private static final Map<BigInteger, ZModPrime> instances = new HashMap<BigInteger, ZModPrime>();

	public static ZModPrime getInstance(final Prime modulus) {
		if (modulus == null) {
			throw new IllegalArgumentException();
		}
		ZModPrime instance = ZModPrime.instances.get(modulus.getValue());
		if (instance == null) {
			instance = new ZModPrime(modulus);
			ZModPrime.instances.put(modulus.getValue(), instance);
		}
		return instance;
	}

	public static ZModPrime getInstance(final int modulus) {
		return ZModPrime.getInstance(BigInteger.valueOf(modulus));
	}

	public static ZModPrime getInstance(BigInteger modulus) {
		return ZModPrime.getInstance(Prime.getInstance(modulus));
	}

	public static ZModPrime getRandomInstance(int bitLength, RandomByteSequence randomByteSequence) {
		return ZModPrime.getInstance(Prime.getRandomInstance(bitLength, randomByteSequence));
	}

	public static ZModPrime getRandomInstance(int bitLength) {
		return ZModPrime.getRandomInstance(bitLength, HybridRandomByteSequence.getInstance());
	}

	@Override
	public BigInteger getCharacteristic() {
		return this.getOrder();
	}

}
