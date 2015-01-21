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

import ch.bfh.unicrypt.helper.Alphabet;
import ch.bfh.unicrypt.helper.converter.classes.biginteger.StringToBigInteger;
import ch.bfh.unicrypt.helper.converter.classes.string.StringToString;
import ch.bfh.unicrypt.helper.converter.interfaces.BigIntegerConverter;
import ch.bfh.unicrypt.helper.converter.interfaces.StringConverter;
import ch.bfh.unicrypt.math.algebra.concatenative.abstracts.AbstractConcatenativeMonoid;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
import java.math.BigInteger;

/**
 *
 * @author rolfhaenni
 */
public class StringMonoid
	   extends AbstractConcatenativeMonoid<StringElement, String> {

	private final Alphabet alphabet;

	private StringMonoid(Alphabet alphabet, int blockLength) {
		super(String.class, blockLength);
		this.alphabet = alphabet;
	}

	public Alphabet getAlphabet() {
		return this.alphabet;
	}

	@Override
	protected StringConverter<String> defaultGetStringConverter() {
		return StringToString.getInstance();
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
	protected boolean abstractContains(String value) {
		return value.length() % this.getBlockLength() == 0 && this.getAlphabet().isValid(value);
	}

	@Override
	protected StringElement abstractGetElement(String value) {
		return new StringElement(this, value);
	}

	@Override
	protected BigIntegerConverter<String> abstractGetBigIntegerConverter() {
		return StringToBigInteger.getInstance(this.alphabet, this.blockLength);
	}

	@Override
	protected StringElement abstractGetIdentityElement() {
		return this.abstractGetElement("");
	}

	@Override
	protected StringElement abstractGetRandomElement(RandomByteSequence randomByteSequence) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected final StringElement abstractGetRandomElement(int length, RandomByteSequence randomByteSequence) {
		char[] chars = new char[length];
		for (int i = 0; i < length; i++) {
			chars[i] = this.getAlphabet().getCharacter(randomByteSequence.getRandomNumberGenerator().nextInteger(this.getAlphabet().getSize() - 1));
		}
		return this.abstractGetElement(new String(chars));
	}

	@Override
	protected StringElement abstractApply(StringElement element1, StringElement element2) {
		return this.abstractGetElement(element1.getValue() + element2.getValue());
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
	public static StringMonoid getInstance(Alphabet alphabet) {
		return StringMonoid.getInstance(alphabet, 1);
	}

	public static StringMonoid getInstance(Alphabet alphabet, int blockLength) {
		if (alphabet == null || blockLength < 1) {
			throw new IllegalArgumentException();
		}
		return new StringMonoid(alphabet, blockLength);
	}

}
