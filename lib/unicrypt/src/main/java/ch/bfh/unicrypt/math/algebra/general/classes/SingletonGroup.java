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

import ch.bfh.unicrypt.helper.converter.classes.biginteger.BigIntegerToBigInteger;
import ch.bfh.unicrypt.helper.converter.interfaces.BigIntegerConverter;
import ch.bfh.unicrypt.math.algebra.general.abstracts.AbstractCyclicGroup;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * @author rolfhaenni
 * <p>
 */
public class SingletonGroup
	   extends AbstractCyclicGroup<SingletonElement, BigInteger> {

	private final SingletonElement element;

	private SingletonGroup(BigInteger value) {
		super(BigInteger.class);
		this.element = new SingletonElement(this, value);
	}

	public final SingletonElement getElement() {
		return this.element;
	}

	public final BigInteger getValue() {
		return this.element.getValue();
	}

	//
	// The following protected methods override the default implementation from {@code AbstractGroup}
	//
	@Override
	protected String defaultToStringValue() {
		return this.getValue().toString();
	}

	@Override
	protected boolean abstractContains(BigInteger value) {
		return this.getValue().equals(value);
	}

	@Override
	protected SingletonElement abstractGetElement(final BigInteger value) {
		return this.element;
	}

	@Override
	protected BigIntegerConverter<BigInteger> abstractGetBigIntegerConverter() {
		return BigIntegerToBigInteger.getInstance();
	}

	@Override
	protected SingletonElement defaultSelfApply(SingletonElement element, BigInteger amount) {
		return this.element;
	}

	@Override
	protected SingletonElement abstractGetRandomElement(RandomByteSequence randomByteSequence) {
		return this.element;
	}

	@Override
	protected BigInteger abstractGetOrder() {
		return BigInteger.ONE;
	}

	@Override
	protected SingletonElement abstractGetIdentityElement() {
		return this.element;
	}

	@Override
	protected SingletonElement abstractApply(SingletonElement element1, SingletonElement element2) {
		return this.element;
	}

	@Override
	protected SingletonElement abstractInvert(SingletonElement element) {
		return this.element;
	}

	@Override
	protected SingletonElement abstractGetDefaultGenerator() {
		return this.element;
	}

	@Override
	protected boolean abstractIsGenerator(SingletonElement element) {
		return true;
	}

	@Override
	protected boolean abstractEquals(Set set) {
		return this.getValue().equals(((SingletonGroup) set).getValue());
	}

	@Override
	protected int abstractHashCode() {
		int hash = 7;
		hash = 47 * hash + this.getValue().hashCode();
		return hash;
	}

	//
	// STATIC FACTORY METHODS
	//
	private static final Map<BigInteger, SingletonGroup> instances = new HashMap<BigInteger, SingletonGroup>();

	public static SingletonGroup getInstance(final BigInteger value) {
		if (value == null) {
			throw new IllegalArgumentException();
		}
		SingletonGroup instance = SingletonGroup.instances.get(value);
		if (instance == null) {
			instance = new SingletonGroup(value);
			SingletonGroup.instances.put(value, instance);
		}
		return instance;
	}

	public static SingletonGroup getInstance() {
		return SingletonGroup.getInstance(BigInteger.ZERO);
	}

}
