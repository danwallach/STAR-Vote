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

import ch.bfh.unicrypt.helper.converter.classes.biginteger.BigIntegerToBigInteger;
import ch.bfh.unicrypt.helper.converter.classes.string.BigIntegerToString;
import ch.bfh.unicrypt.helper.converter.interfaces.BigIntegerConverter;
import ch.bfh.unicrypt.helper.converter.interfaces.StringConverter;
import ch.bfh.unicrypt.helper.distribution.Distribution;
import ch.bfh.unicrypt.helper.distribution.InfiniteDistribution;
import ch.bfh.unicrypt.helper.distribution.UniformDistribution;
import ch.bfh.unicrypt.math.algebra.dualistic.abstracts.AbstractCyclicRing;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Group;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * /**
 * This class implements the additive cyclic group of (positive and negative) integers with infinite order. Its identity
 * element is 0, and there are exactly two generators, namely 1 and -1. To invert an element, it is multiplied with -1.
 * <p>
 * @see "Handbook of Applied Cryptography, Example 2.164"
 * @see <a href="http://en.wikipedia.org/wiki/Integer">http://en.wikipedia.org/wiki/Integer</a>
 * <p>
 * @author R. Haenni
 * @author R. E. Koenig
 * @version 2.0
 */
public class Z
	   extends AbstractCyclicRing<ZElement, BigInteger> {

	private final Distribution distribution;

	private Z(final Distribution distribution) {
		super(BigInteger.class);
		this.distribution = distribution;
	}

	public Distribution getDistribution() {
		return this.distribution;
	}

	public final boolean contains(int integerValue) {
		return this.contains(BigInteger.valueOf(integerValue));
	}

	public final ZElement getElement(int integerValue) {
		return this.getElement(BigInteger.valueOf(integerValue));
	}

	//
	// The following protected methods override the default implementation from
	// various super-classes
	//
	@Override
	protected ZElement defaultSelfApply(ZElement element, BigInteger amount) {
		return this.abstractGetElement(element.getValue().multiply(amount));
	}

	@Override
	protected StringConverter<BigInteger> defaultGetStringConverter() {
		return BigIntegerToString.getInstance();
	}

	@Override
	protected ZElement defaultGetRandomGenerator(final RandomByteSequence randomByteSequence) {
		if (randomByteSequence.getRandomNumberGenerator().nextBoolean()) {
			return this.getDefaultGenerator();
		}
		return this.getDefaultGenerator().invert();
	}

	@Override
	protected boolean defaultIsEquivalent(Set set) {
		return true;
	}

	//
	// The following protected methods implement the abstract methods from
	// various super-classes
	//
	@Override
	protected ZElement abstractApply(ZElement element1, ZElement element2) {
		return this.abstractGetElement(element1.getValue().add(element2.getValue()));
	}

	@Override
	protected ZElement abstractGetIdentityElement() {
		return this.abstractGetElement(BigInteger.ZERO);
	}

	@Override
	protected ZElement abstractInvert(ZElement element) {
		return this.abstractGetElement(element.getValue().negate());
	}

	@Override
	protected ZElement abstractMultiply(ZElement element1, ZElement element2) {
		return this.abstractGetElement(element1.getValue().multiply(element2.getValue()));
	}

	@Override
	protected ZElement abstractGetOne() {
		return this.abstractGetElement(BigInteger.ONE);
	}

	@Override
	protected BigInteger abstractGetOrder() {
		return Group.INFINITE_ORDER;
	}

	@Override
	protected boolean abstractContains(BigInteger value) {
		return true;
	}

	@Override
	protected ZElement abstractGetElement(BigInteger value) {
		return new ZElement(this, value);
	}

	@Override
	protected BigIntegerConverter<BigInteger> abstractGetBigIntegerConverter() {
		return BigIntegerToBigInteger.getInstance(true);
	}

	@Override
	protected ZElement abstractGetRandomElement(RandomByteSequence randomByteSequence) {
		return this.getElement(this.distribution.getBigInteger(randomByteSequence));
	}

	@Override
	protected ZElement abstractGetDefaultGenerator() {
		return this.abstractGetElement(BigInteger.ONE);
	}

	@Override
	protected boolean abstractIsGenerator(final ZElement element) {
		return element.getValue().abs().equals(BigInteger.ONE);
	}

	@Override
	protected boolean abstractEquals(Set set) {
		Z other = (Z) set;
		return this.distribution.equals(other.distribution);
	}

	@Override
	protected int abstractHashCode() {
		return this.distribution.hashCode();
	}

	//
	// STATIC FACTORY METHODS
	//
	private static final Map<Distribution, Z> instances = new HashMap<Distribution, Z>();

	/**
	 * Returns the unique instance of this class for an infinite distribution.
	 * <p>
	 * @return An instance of this class.
	 */
	public static Z getInstance() {
		return Z.getInstance(InfiniteDistribution.getInstance());
	}

	/**
	 * Returns the unique instance of this class for a uniform distribution over the interval [0,2^{@literal b}-1].
	 * <p>
	 * @param b The bit length of the interval of the uniform distribution.
	 * @return An instance of this class.
	 * @throws IllegalArgumentException if {@literal b} is negative.
	 */
	public static Z getInstance(int b) {
		if (b < 0) {
			throw new IllegalArgumentException();
		}
		return Z.getInstance(UniformDistribution.getInstance(b));
	}

	/**
	 * Returns the unique instance of this class for a uniform distribution over the interval [0,{@literal b}-1].
	 * <p>
	 * @param b The upper bound (exclusive) of the interval of the uniform distribution.
	 * @return An instance of this class.
	 * @throws IllegalArgumentException if {@literal b} is smaller than one.
	 */
	public static Z getInstance(BigInteger b) {
		if (b == null || b.compareTo(BigInteger.ZERO) < 1) {
			throw new IllegalArgumentException();
		}
		return Z.getInstance(UniformDistribution.getInstance(b.subtract(BigInteger.ONE)));
	}

	/**
	 * Returns the unique instance of this class for a given distribution.
	 * <p>
	 * @param distribution The distribution for random elements.
	 * @return An instance of this class.
	 * @throws IllegalArgumentException if {@literal distribution} is null.
	 */
	public static Z getInstance(Distribution distribution) {
		if (distribution == null) {
			throw new IllegalArgumentException();
		}
		Z instance = Z.instances.get(distribution);
		if (instance == null) {
			instance = new Z(distribution);
			Z.instances.put(distribution, instance);
		}
		return instance;
	}

}
