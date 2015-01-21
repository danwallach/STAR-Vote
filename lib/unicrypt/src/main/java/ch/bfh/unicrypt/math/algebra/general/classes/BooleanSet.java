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

import ch.bfh.unicrypt.helper.converter.classes.biginteger.BooleanToBigInteger;
import ch.bfh.unicrypt.helper.converter.interfaces.BigIntegerConverter;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModPrime;
import ch.bfh.unicrypt.math.algebra.general.abstracts.AbstractSet;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.ZStarModPrime;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
import java.math.BigInteger;

/**
 * This interface represents the group that consists of two elements only, for example TRUE and FALSE. This group is
 * isomorphic to the additive group of integers modulo 2. It is therefore possible to consider and implement it as a
 * specialization of {@link ZPlusMod}.
 * <p>
 * @author R. Haenni
 * @author R. E. Koenig
 * @version 2.0
 */
public class BooleanSet
	   extends AbstractSet<BooleanElement, Boolean> {

	public static final BooleanElement TRUE = BooleanSet.getInstance().getElement(true);
	public static final BooleanElement FALSE = BooleanSet.getInstance().getElement(false);

	private final BooleanElement trueElement;
	private final BooleanElement falseElement;

	private BooleanSet() {
		super(Boolean.class);
		trueElement = new BooleanElement(this, true);
		falseElement = new BooleanElement(this, false);
	}

	@Override
	public ZModPrime getZModOrder() {
		return ZModPrime.getInstance(this.getOrder());
	}

	@Override
	public ZStarModPrime getZStarModOrder() {
		return ZStarModPrime.getInstance(this.getOrder());
	}

	@Override
	protected BigInteger abstractGetOrder() {
		return BigInteger.valueOf(2);
	}

	@Override
	protected boolean abstractContains(Boolean value) {
		return true;
	}

	@Override
	protected BooleanElement abstractGetElement(Boolean value) {
		return (value ? trueElement : falseElement);
	}

	@Override
	protected BigIntegerConverter<Boolean> abstractGetBigIntegerConverter() {
		return BooleanToBigInteger.getInstance();
	}

	@Override
	protected BooleanElement abstractGetRandomElement(RandomByteSequence randomByteSequence) {
		return this.getElement(randomByteSequence.getRandomNumberGenerator().nextBoolean());
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
	private static BooleanSet instance;

	/**
	 * Returns the singleton object of this class.
	 * <p>
	 * @return The singleton object of this class
	 */
	public static BooleanSet getInstance() {
		if (BooleanSet.instance == null) {
			BooleanSet.instance = new BooleanSet();
		}
		return BooleanSet.instance;
	}

}
