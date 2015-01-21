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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.concatenative.classes;

import ch.bfh.unicrypt.helper.array.classes.ByteArray;
import ch.bfh.unicrypt.helper.converter.classes.biginteger.ByteArrayToBigInteger;
import ch.bfh.unicrypt.helper.converter.classes.bytearray.ByteArrayToByteArray;
import ch.bfh.unicrypt.helper.converter.interfaces.BigIntegerConverter;
import ch.bfh.unicrypt.helper.converter.interfaces.ByteArrayConverter;
import ch.bfh.unicrypt.math.algebra.concatenative.abstracts.AbstractConcatenativeMonoid;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author rolfhaenni
 */
public class ByteArrayMonoid
	   extends AbstractConcatenativeMonoid<ByteArrayElement, ByteArray> {

	private ByteArrayMonoid(int blockLength) {
		super(ByteArray.class, blockLength);
	}

	public final ByteArrayElement getElement(byte[] bytes) {
		return this.getElement(ByteArray.getInstance(bytes));
	}

	// for strings of the form "00|95|2B|9B|E2|FD|30|89"
	public final ByteArrayElement getElement(String string) {
		return this.getElement(ByteArray.getInstance(string));
	}

	@Override
	protected ByteArrayConverter<ByteArray> defaultGetByteArrayConverter() {
		return ByteArrayToByteArray.getInstance();
	}

	//
	// The following protected methods implement the abstract methods from
	// various super-classes
	//
	@Override
	protected BigInteger abstractGetOrder() {
		return Set.INFINITE_ORDER;
	}

	@Override
	protected boolean abstractContains(ByteArray value) {
		return value.getLength() % this.blockLength == 0;
	}

	@Override
	protected ByteArrayElement abstractGetElement(ByteArray value) {
		return new ByteArrayElement(this, value);
	}

	@Override
	protected BigIntegerConverter<ByteArray> abstractGetBigIntegerConverter() {
		return ByteArrayToBigInteger.getInstance(this.blockLength);
	}

	@Override
	protected ByteArrayElement abstractGetIdentityElement() {
		return this.abstractGetElement(ByteArray.getInstance());
	}

	@Override
	protected ByteArrayElement abstractGetRandomElement(RandomByteSequence randomByteSequence) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected ByteArrayElement abstractGetRandomElement(int length, RandomByteSequence randomByteSequence) {
		return this.abstractGetElement(ByteArray.getRandomInstance(length, randomByteSequence));
	}

	@Override
	protected ByteArrayElement abstractApply(ByteArrayElement element1, ByteArrayElement element2) {
		return this.abstractGetElement(element1.getValue().append(element2.getValue()));
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
	private static final Map<Integer, ByteArrayMonoid> instances = new HashMap<Integer, ByteArrayMonoid>();

	public static ByteArrayMonoid getInstance() {
		return ByteArrayMonoid.getInstance(1);
	}

	/**
	 * Returns the singleton object of this class.
	 * <p>
	 * @param blockLength
	 * @return The singleton object of this class
	 */
	public static ByteArrayMonoid getInstance(int blockLength) {
		if (blockLength < 1) {
			throw new IllegalArgumentException();
		}
		ByteArrayMonoid instance = ByteArrayMonoid.instances.get(Integer.valueOf(blockLength));
		if (instance == null) {
			instance = new ByteArrayMonoid(blockLength);
			ByteArrayMonoid.instances.put(blockLength, instance);
		}
		return instance;
	}

}
