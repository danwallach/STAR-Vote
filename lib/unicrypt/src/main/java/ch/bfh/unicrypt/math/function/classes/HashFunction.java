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

import ch.bfh.unicrypt.helper.hash.HashMethod;
import ch.bfh.unicrypt.helper.MathUtil;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.FiniteByteArrayElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.FixedByteArraySet;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import ch.bfh.unicrypt.math.function.abstracts.AbstractFunction;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;

/**
 * This class represents the concept of a hash function, which maps an arbitrarily long input element into an element of
 * a given co-domain. The mapping itself is defined by some cryptographic hash function such as SHA-256. For complex
 * input elements, there are two options: one in which the individual elements are first recursively paired with
 * {@link MathUtil#elegantPair(java.math.BigInteger[])}, and one in which the hashing itself is done recursively. The
 * co-domain is always an instance of {@link ZPlusMod}. Its order corresponds to the size of the cryptographic hash
 * function's output space (a power of 2).
 * <p>
 * @see Element#getHashValue()
 * @see Element#getRecursiveHashValue()
 * <p>
 * @author R. Haenni
 * @author R. E. Koenig
 * @version 2.0
 */
public class HashFunction
	   extends AbstractFunction<HashFunction, Set, Element, FixedByteArraySet, FiniteByteArrayElement> {

	private final HashMethod hashMethod;

	private HashFunction(Set domain, FixedByteArraySet coDomain, HashMethod hashMethod) {
		super(domain, coDomain);
		this.hashMethod = hashMethod;
	}

	public HashMethod getHashMethod() {
		return this.hashMethod;
	}

	@Override
	protected boolean defaultIsEquivalent(HashFunction other) {
		return this.getHashMethod().equals(other.getHashMethod());
	}

	@Override
	protected FiniteByteArrayElement abstractApply(final Element element, final RandomByteSequence randomByteSequence) {
		return this.getCoDomain().getElement(element.getHashValue(this.hashMethod));
	}

	/**
	 * This constructor generates a default SHA-256 hash function. The order of the co-domain is 2^256.
	 * <p>
	 * @param domain
	 * @return
	 */
	public static HashFunction getInstance(Set domain) {
		return HashFunction.getInstance(domain, HashMethod.getInstance());
	}

	/**
	 * This constructor generates a default hash function for a given hash algorithm name. The co-domain is chosen
	 * accordingly.
	 * <p>
	 * @param domain
	 * @param hashMethod The name of the hash algorithm
	 * @return
	 * @throws IllegalArgumentException if {@literal algorithmName} is null or an unknown hash algorithm name
	 */
	public static HashFunction getInstance(Set domain, final HashMethod hashMethod) {
		if (domain == null || hashMethod == null) {
			throw new IllegalArgumentException();
		}
		return new HashFunction(domain, FixedByteArraySet.getInstance(hashMethod.getHashAlgorithm().getHashLength()), hashMethod);
	}

}
