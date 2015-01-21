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

import ch.bfh.unicrypt.helper.factorization.Prime;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.unicrypt.math.algebra.multiplicative.interfaces.MultiplicativeCyclicGroup;
import ch.bfh.unicrypt.random.classes.HybridRandomByteSequence;
import ch.bfh.unicrypt.random.classes.ReferenceRandomByteSequence;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author rolfhaenni
 */
public class ZStarModPrime
	   extends ZStarMod
	   implements MultiplicativeCyclicGroup<BigInteger> {

	private final static Map<BigInteger, ZStarModPrime> instances = new HashMap<BigInteger, ZStarModPrime>();

	protected ZStarModPrime(Prime modulus) {
		super(modulus);
	}

	@Override
	public ZStarModElement getDefaultGenerator() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public ZStarModElement getRandomGenerator() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public ZStarModElement getRandomGenerator(RandomByteSequence randomByteSequence) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public ZStarModElement getIndependentGenerator(int index) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public ZStarModElement getIndependentGenerator(int index, ReferenceRandomByteSequence referenceRandomByteSequence) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Tuple getIndependentGenerators(int maxIndex) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Tuple getIndependentGenerators(int maxIndex, ReferenceRandomByteSequence referenceRandomByteSequence) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Tuple getIndependentGenerators(int minIndex, int maxIndex) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Tuple getIndependentGenerators(int minIndex, int maxIndex, ReferenceRandomByteSequence referenceRandomByteSequence) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean isGenerator(Element element) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public static ZStarModPrime getInstance(final Prime modulus) {
		if (modulus == null) {
			throw new IllegalArgumentException();
		}
		ZStarModPrime instance = ZStarModPrime.instances.get(modulus.getValue());
		if (instance == null) {
			instance = new ZStarModPrime(modulus);
			ZStarModPrime.instances.put(modulus.getValue(), instance);
		}
		return instance;
	}

	public static ZStarModPrime getInstance(final int modulus) {
		return ZStarModPrime.getInstance(BigInteger.valueOf(modulus));
	}

	public static ZStarModPrime getInstance(final BigInteger modulus) {
		return ZStarModPrime.getInstance(Prime.getInstance(modulus));
	}

	public static ZStarModPrime getRandomInstance(int bitLength) {
		return ZStarModPrime.getRandomInstance(bitLength, HybridRandomByteSequence.getInstance());
	}

	public static ZStarModPrime getRandomInstance(int bitLength, RandomByteSequence randomByteSequence) {
		return ZStarModPrime.getInstance(Prime.getRandomInstance(bitLength, randomByteSequence));
	}

}
