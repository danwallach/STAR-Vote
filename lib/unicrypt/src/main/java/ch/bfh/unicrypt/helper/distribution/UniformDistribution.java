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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.distribution;

import ch.bfh.unicrypt.helper.UniCrypt;
import ch.bfh.unicrypt.helper.MathUtil;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
import java.math.BigInteger;

/**
 *
 * @see http://en.wikipedia.org/wiki/Uniform_distribution_%28discrete%29
 * <p>
 * @author philipp
 */
public class UniformDistribution
	   extends UniCrypt
	   implements Distribution {

	private final BigInteger a;
	private final BigInteger b;

	private UniformDistribution(final BigInteger a, final BigInteger b) {
		this.a = a;
		this.b = b;
	}

	@Override
	public BigInteger getLowerBound() {
		return this.a;
	}

	@Override
	public BigInteger getUpperBound() {
		return this.b;
	}

	@Override
	public BigInteger getBigInteger(RandomByteSequence randomByteSequence) {
		return randomByteSequence.getRandomNumberGenerator().nextBigInteger(this.a, this.b);
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 79 * hash + this.getClass().hashCode();
		hash = 79 * hash + this.a.hashCode();
		hash = 79 * hash + this.b.hashCode();
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final UniformDistribution other = (UniformDistribution) obj;
		return this.a.equals(other.a) && this.b.equals(other.b);
	}

	public static UniformDistribution getInstance(final BigInteger b) {
		return UniformDistribution.getInstance(BigInteger.ZERO, b);
	}

	public static UniformDistribution getInstance(final BigInteger a, final BigInteger b) {
		if (a == null || b == null || a.compareTo(b) > 0) {
			throw new IllegalArgumentException();
		}
		return new UniformDistribution(a, b);
	}

	public static UniformDistribution getInstance(final int maxBits) {
		return UniformDistribution.getInstance(0, maxBits);
	}

	public static UniformDistribution getInstance(final int minBits, final int maxBits) {
		if (minBits < 0 || maxBits < minBits) {
			throw new IllegalArgumentException();
		}
		BigInteger a = (minBits == 0) ? BigInteger.ZERO : MathUtil.powerOfTwo(minBits - 1);
		BigInteger b = MathUtil.powerOfTwo(maxBits).subtract(BigInteger.ONE);
		return UniformDistribution.getInstance(a, b);
	}

}
