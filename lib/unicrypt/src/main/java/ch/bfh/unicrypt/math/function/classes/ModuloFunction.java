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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes;

import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZMod;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import ch.bfh.unicrypt.math.function.abstracts.AbstractFunction;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
import java.math.BigInteger;

/**
 * This class represents the concept of an identity function f:X->Z_2 with f(x)=x mod N for all elements x in X.
 * <p>
 * @author R. Haenni
 * @author R. E. Koenig
 * @version 1.0
 */
public class ModuloFunction
	   extends AbstractFunction<ModuloFunction, Set<BigInteger>, Element<BigInteger>, ZMod, ZModElement> {

	private final BigInteger modulus;

	private ModuloFunction(final Set domain, final ZMod coDomain) {
		super(domain, coDomain);
		this.modulus = coDomain.getModulus();
	}

	public BigInteger getModulus() {
		return this.modulus;
	}

	@Override
	protected boolean defaultIsEquivalent(ModuloFunction other) {
		return this.getModulus().equals(other.getModulus());
	}

	//
	// The following protected method implements the abstract method from {@code AbstractFunction}
	//
	@Override
	protected ZModElement abstractApply(final Element<BigInteger> element, final RandomByteSequence randomByteSequence) {
		return this.getCoDomain().getElement(element.getValue().mod(this.getModulus()));
	}

	//
	// STATIC FACTORY METHODS
	//
	/**
	 * This is the default constructor for this class. It creates an identity function for a given group.
	 * <p>
	 * @param domain  The given Group
	 * @param modulus
	 * @return
	 * @throws IllegalArgumentException if the group is null
	 */
	public static ModuloFunction getInstance(final Set<BigInteger> domain, BigInteger modulus) {
		return ModuloFunction.getInstance(domain, ZMod.getInstance(modulus));
	}

	public static ModuloFunction getInstance(final Set<BigInteger> domain, ZMod coDomain) {
		if (domain == null || coDomain == null) {
			throw new IllegalArgumentException();
		}
		return new ModuloFunction(domain, coDomain);
	}

}
