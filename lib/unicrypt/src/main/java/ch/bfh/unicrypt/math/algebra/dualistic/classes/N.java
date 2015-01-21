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
import ch.bfh.unicrypt.helper.converter.interfaces.BigIntegerConverter;
import ch.bfh.unicrypt.math.algebra.dualistic.abstracts.AbstractSemiRing;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Group;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
import java.math.BigInteger;

/**
 * /**
 * This class implements the SemiRing of non-negative integers with infinite order. Its identity element is 0.
 * <p>
 * @see "Handbook of Applied Cryptography, Example 2.164"
 * @see <a href="http://en.wikipedia.org/wiki/Integer">http://en.wikipedia.org/wiki/Integer</a>
 * <p>
 * @author R. Haenni
 * @author R. E. Koenig
 * @version 2.0
 */
public class N
	   extends AbstractSemiRing<NElement, BigInteger> {

	public N() {
		super(BigInteger.class);
	}

	public final boolean contains(int integerValue) {
		return this.contains(BigInteger.valueOf(integerValue));
	}

	public final NElement getElement(int integerValue) {
		return this.getElement(BigInteger.valueOf(integerValue));
	}

	//
	// The following protected methods override the default implementation from
	// various super-classes
	//
	@Override
	protected NElement defaultSelfApply(NElement element, BigInteger amount) {
		return this.abstractGetElement(element.getValue().multiply(amount));
	}

	//
	// The following protected methods implement the abstract methods from
	// various super-classes
	//
	@Override
	protected NElement abstractApply(NElement element1, NElement element2) {
		return this.abstractGetElement(element1.getValue().add(element2.getValue()));
	}

	@Override
	protected NElement abstractGetIdentityElement() {
		return this.abstractGetElement(BigInteger.ZERO);
	}

	@Override
	protected NElement abstractMultiply(NElement element1, NElement element2) {
		return this.abstractGetElement(element1.getValue().multiply(element2.getValue()));
	}

	@Override
	protected NElement abstractGetOne() {
		return this.abstractGetElement(BigInteger.ONE);
	}

	@Override
	protected BigInteger abstractGetOrder() {
		return Group.INFINITE_ORDER;
	}

	@Override
	protected boolean abstractContains(BigInteger value) {
		return value.signum() >= 0;
	}

	@Override
	protected NElement abstractGetElement(BigInteger value) {
		return new NElement(this, value);
	}

	@Override
	protected BigIntegerConverter<BigInteger> abstractGetBigIntegerConverter() {
		return BigIntegerToBigInteger.getInstance();
	}

	@Override
	protected NElement abstractGetRandomElement(RandomByteSequence randomByteSequence) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected boolean abstractEquals(Set set) {
		return true;
	}

	@Override
	protected int abstractHashCode() {
		return 1;
	}

	//
	// STATIC FACTORY METHODS
	//
	private static N instance;

	/**
	 * Returns the singleton object of this class.
	 * <p>
	 * @return The singleton object of this class
	 */
	public static N getInstance() {
		if (N.instance == null) {
			N.instance = new N();
		}
		return N.instance;
	}

}
