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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.encoder.classes;

import ch.bfh.unicrypt.crypto.encoder.abstracts.AbstractEncoder;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModPrime;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarMod;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModSafePrime;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.abstracts.AbstractFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.interfaces.Function;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
import java.math.BigInteger;

public class ZModToGStarModSafePrimeEncoder
	   extends AbstractEncoder<ZModPrime, ZModElement, GStarModSafePrime, GStarModElement> {

	private final GStarModSafePrime gStarModSafePrime;

	protected ZModToGStarModSafePrimeEncoder(GStarModSafePrime gStarModSafePrime) {
		this.gStarModSafePrime = gStarModSafePrime;
	}

	public GStarModSafePrime getGStarModSafePrime() {
		return this.gStarModSafePrime;
	}

	@Override
	protected Function abstractGetEncodingFunction() {
		return new EncodingFunction(this.getGStarModSafePrime().getZModOrder(), this.getGStarModSafePrime());
	}

	@Override
	protected Function abstractGetDecodingFunction() {
		return new DecodingFunction(this.getGStarModSafePrime(), this.getGStarModSafePrime().getZModOrder());
	}

	public static ZModToGStarModSafePrimeEncoder getInstance(GStarModSafePrime gStarModSafePrime) {
		if (gStarModSafePrime == null) {
			throw new IllegalArgumentException();
		}
		return new ZModToGStarModSafePrimeEncoder(gStarModSafePrime);
	}

	static class EncodingFunction
		   extends AbstractFunction<EncodingFunction, ZModPrime, ZModElement, GStarModSafePrime, GStarModElement> {

		protected EncodingFunction(final ZModPrime domain, final GStarMod coDomain) {
			super(domain, coDomain);
		}

		@Override
		protected GStarModElement abstractApply(final ZModElement element, final RandomByteSequence randomByteSequence) {
			final BigInteger value = element.getValue().add(BigInteger.ONE);
			final GStarModSafePrime coDomain = this.getCoDomain();
			if (coDomain.contains(value)) {
				return coDomain.getElement(value);
			}
			return coDomain.getElement(coDomain.getModulus().subtract(value));
		}

	}

	static class DecodingFunction
		   extends AbstractFunction<DecodingFunction, GStarModSafePrime, GStarModElement, ZModPrime, ZModElement> {

		protected DecodingFunction(final GStarMod domain, final ZModPrime coDomain) {
			super(domain, coDomain);
		}

		@Override
		protected ZModElement abstractApply(final GStarModElement element, final RandomByteSequence randomByteSequence) {
			final BigInteger value = element.getValue();
			final GStarModSafePrime domain = this.getDomain();
			if (value.compareTo(domain.getOrder()) <= 0) {
				return this.getCoDomain().getElement(value.subtract(BigInteger.ONE));
			}
			return this.getCoDomain().getElement((domain.getModulus().subtract(value)).subtract(BigInteger.ONE));
		}

	}

}
