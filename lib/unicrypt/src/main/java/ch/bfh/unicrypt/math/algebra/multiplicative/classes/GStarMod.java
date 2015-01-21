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

import ch.bfh.unicrypt.helper.MathUtil;
import ch.bfh.unicrypt.helper.converter.classes.biginteger.BigIntegerToBigInteger;
import ch.bfh.unicrypt.helper.converter.interfaces.BigIntegerConverter;
import ch.bfh.unicrypt.helper.factorization.Factorization;
import ch.bfh.unicrypt.helper.factorization.SpecialFactorization;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import ch.bfh.unicrypt.math.algebra.multiplicative.abstracts.AbstractMultiplicativeCyclicGroup;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
import java.math.BigInteger;

/**
 * This interface represents the concept of a sub-group G_m (of order m) of a cyclic group of integers Z*_n with the
 * operation of multiplication modulo n. For Z*_n to be cyclic, n must be 2, 4, p^e, or 2p^e, where p>2 is prime and
 * e>0. The actual sub-group depends on the given set of prime factors of the order phi(n) of Z*_n, where phi(n) is the
 * Euler totient function. The order m=|G_m| is the product of all given prime factors of phi(n). If all prime factors
 * of phi(n) are given, which implies m=phi(n), then G_m is the parent group Z*_n.
 * <p>
 * <p/>
 * @see "Handbook of Applied Cryptography, Fact 2.132"
 * @see "Handbook of Applied Cryptography, Definition 2.100"
 * @see "Handbook of Applied Cryptography, Definition 2.166"
 * <p>
 * @author R. Haenni
 * @author R. E. Koenig
 * @version 2.0
 */
public class GStarMod
	   extends AbstractMultiplicativeCyclicGroup<GStarModElement, BigInteger> {

	private final BigInteger modulus;
	private final SpecialFactorization moduloFactorization;
	private final Factorization orderFactorization;
	private ZStarMod superGroup;

	protected GStarMod(SpecialFactorization moduloFactorization, Factorization orderFactorization) {
		super(BigInteger.class);
		this.modulus = moduloFactorization.getValue();
		this.moduloFactorization = moduloFactorization;
		this.orderFactorization = orderFactorization;
	}

	/**
	 * Returns the modulus if this group.
	 * <p>
	 * <p/>
	 * @return The modulus
	 */
	public final BigInteger getModulus() {
		return this.modulus;
	}

	/**
	 * Returns a (possibly incomplete) prime factorization the modulus if this group. An incomplete factorization
	 * implies that the group order is unknown in such a case.
	 * <p>
	 * <p/>
	 * @return The prime factorization
	 */
	public final SpecialFactorization getModuloFactorization() {
		return this.moduloFactorization;
	}

	/**
	 * Returns prime factorization of the group order phi(n) of Z*_n.
	 * <p>
	 * <p/>
	 * @return The prime factorization of the group order
	 */
	public final Factorization getOrderFactorization() {
		return this.orderFactorization;
	}

	public final ZStarMod getZStarMod() {
		if (this.superGroup == null) {
			this.superGroup = ZStarMod.getInstance(this.getModuloFactorization());
		}
		return this.superGroup;
	}

	public final boolean contains(int integerValue) {
		return this.contains(BigInteger.valueOf(integerValue));
	}

	public final GStarModElement getElement(int integerValue) {
		return this.getElement(BigInteger.valueOf(integerValue));
	}

	/**
	 * Returns the quotient k=phi(n)/m of the orders of the two involved groups.
	 * <p>
	 * <p/>
	 * @return The quotient of the two orders.
	 */
	public BigInteger getCoFactor() {
		return this.getZStarMod().getOrder().divide(this.getOrder());
	}

	//
	// The following protected methods override the default implementation from
	// various super-classes
	//
	@Override
	protected GStarModElement defaultSelfApplyAlgorithm(final GStarModElement element, final BigInteger posAmount) {
		return this.abstractGetElement(element.getValue().modPow(posAmount, this.modulus));
	}

	@Override
	protected String defaultToStringValue() {
		return this.getModulus().toString() + "," + this.getOrder().toString();
	}

	//
	// The following protected methods implement the abstract methods from
	// various super-classes
	//
	@Override
	protected boolean abstractContains(final BigInteger value) {
		return value.signum() > 0
			   && value.compareTo(this.modulus) < 0
			   && MathUtil.areRelativelyPrime(value, this.modulus)
			   && value.modPow(this.getOrder(), this.modulus).equals(BigInteger.ONE);
	}

	@Override
	protected GStarModElement abstractGetElement(BigInteger value) {
		return new GStarModElement(this, value);
	}

	@Override
	protected BigIntegerConverter<BigInteger> abstractGetBigIntegerConverter() {
		return BigIntegerToBigInteger.getInstance();
	}

	@Override
	protected GStarModElement abstractGetRandomElement(final RandomByteSequence randomByteSequence) {
		ZStarModElement randomElement = this.getZStarMod().getRandomElement(randomByteSequence);
		return this.getElement(randomElement.power(this.getCoFactor()).getBigInteger());
// VERSION WITH OPTIMIZED EFFICIENCY BUT LACK OF INDEPENDENCE
//    if (this.getOrder().compareTo(this.getCoFactor()) > 0) { // choose between the faster method
//      // Method 1
//      ZStarModElement randomElement = this.getZStarMod().getRandomElement(random);
//      return this.getElement(randomElement.power(this.getCoFactor()));
//    }
//    // Method 2
//    return this.getDefaultGenerator().power(this.getZModOrder().getRandomElement(random));
	}

	@Override
	protected BigInteger abstractGetOrder() {
		return this.getOrderFactorization().getValue();
	}

	@Override
	protected GStarModElement abstractGetIdentityElement() {
		return this.abstractGetElement(BigInteger.ONE);
	}

	@Override
	protected GStarModElement abstractApply(final GStarModElement element1, final GStarModElement element2) {
		return this.abstractGetElement(element1.getValue().multiply(element2.getValue()).mod(this.modulus));
	}

	@Override
	protected GStarModElement abstractInvert(final GStarModElement element) {
		return this.abstractGetElement(element.getValue().modInverse(this.modulus));
	}

	/**
	 * See http://en.wikipedia.org/wiki/Schnorr_group
	 * <p>
	 * <p/>
	 * @return
	 */
	@Override
	protected GStarModElement abstractGetDefaultGenerator() {
		BigInteger alpha = BigInteger.ZERO;
		GStarModElement element;
		do {
			do {
				alpha = alpha.add(BigInteger.ONE);
			} while (!MathUtil.areRelativelyPrime(alpha, this.getModulus()));
			element = this.abstractGetElement(alpha.modPow(this.getCoFactor(), this.modulus));
		} while (!this.isGenerator(element)); // this test could be skipped for a prime order
		return element;
	}

	// see Handbook of Applied Cryptography, Algorithm 4.80 and Note 4.81 (the implemented)
	// method is a mix between 4.80 and 4.81
	// See also http://en.wikipedia.org/wiki/Schnorr_group
	@Override
	protected boolean abstractIsGenerator(GStarModElement element) {
		for (final BigInteger prime : this.getOrderFactorization().getPrimeFactors()) {
			if (element.selfApply(this.getOrder().divide(prime)).isEquivalent(this.getIdentityElement())) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected boolean abstractEquals(Set set) {
		final GStarMod other = (GStarMod) set;
		return this.getModulus().equals(other.getModulus()) && this.getOrder().equals(other.getOrder());
	}

	@Override
	protected int abstractHashCode() {
		int hash = 7;
		hash = 47 * hash + this.getModulus().hashCode();
		hash = 47 * hash + this.getOrder().hashCode();
		return hash;
	}

	//
	// STATIC FACTORY METHODS
	//
	/**
	 * This is the general static factory method for this class.
	 * <p>
	 * <p/>
	 * @param moduloFactorization
	 * @param orderFactorization
	 * @return
	 * @throws IllegalArgumentException if {@literal moduloFactorization} or {@literal orderFactorization} is null
	 * @throws IllegalArgumentException if the value of {@literal orderFactorization} does not divide phi(n)
	 */
	public static GStarMod getInstance(SpecialFactorization moduloFactorization, Factorization orderFactorization) {
		GStarMod group = new GStarMod(moduloFactorization, orderFactorization);
		if (!group.getOrder().mod(orderFactorization.getValue()).equals(BigInteger.ZERO)) {
			throw new IllegalArgumentException();
		}
		return group;
	}

}
