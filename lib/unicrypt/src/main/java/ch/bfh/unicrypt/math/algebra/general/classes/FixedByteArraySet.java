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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes;

import ch.bfh.unicrypt.helper.array.classes.ByteArray;
import ch.bfh.unicrypt.helper.MathUtil;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Rolf Haenni <rolf.haenni@bfh.ch>
 */
public class FixedByteArraySet
	   extends FiniteByteArraySet {

	private FixedByteArraySet(int length) {
		super(length, length);
	}

	public int getLength() {
		return this.getMinLength();
	}

	@Override
	protected FiniteByteArrayElement abstractGetRandomElement(RandomByteSequence randomByteSequence) {
		// this imlementation is more efficient than the one from the parent class
		return this.abstractGetElement(ByteArray.getRandomInstance(this.getLength(), randomByteSequence));
	}

	private static final Map<Integer, FixedByteArraySet> instances = new HashMap<Integer, FixedByteArraySet>();

	public static FixedByteArraySet getInstance(final int length) {
		if (length < 0) {
			throw new IllegalArgumentException();
		}
		FixedByteArraySet instance = FixedByteArraySet.instances.get(Integer.valueOf(length));
		if (instance == null) {
			instance = new FixedByteArraySet(length);
			FixedByteArraySet.instances.put(Integer.valueOf(length), instance);
		}
		return instance;
	}

	public static FixedByteArraySet getInstance(final BigInteger minOrder) {
		if (minOrder == null || minOrder.signum() < 0) {
			throw new IllegalArgumentException();
		}
		int length = 0;
		BigInteger size = MathUtil.powerOfTwo(Byte.SIZE);
		BigInteger order = BigInteger.ONE;
		while (order.compareTo(minOrder) < 0) {
			order = order.multiply(size);
			length++;
		}
		return FixedByteArraySet.getInstance(length);
	}

}
