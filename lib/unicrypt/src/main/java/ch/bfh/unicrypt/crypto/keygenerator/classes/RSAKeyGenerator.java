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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.keygenerator.classes;

import ch.bfh.unicrypt.crypto.keygenerator.abstracts.AbstractKeyPairGenerator;
import ch.bfh.unicrypt.helper.converter.classes.biginteger.BigIntegerToBigInteger;
import ch.bfh.unicrypt.helper.converter.classes.bytearray.StringToByteArray;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZMod;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModPrimePair;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.multiplicative.classes.ZStarMod;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.CompositeFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.ConvertFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.InvertFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.RandomFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.interfaces.Function;

/**
 *
 * @author rolfhaenni
 */
public class RSAKeyGenerator
	   extends AbstractKeyPairGenerator<ZMod, ZModElement, ZMod, ZModElement> {

	private final ZMod zMod;
	private ZStarMod zStarMod;

	protected RSAKeyGenerator(ZMod zMod, StringToByteArray converter) {
		super(zMod, zMod, converter);
		this.zMod = zMod;
	}

	public ZStarMod getZStarMod() {
		if (this.zStarMod == null) {
			if (this.zMod instanceof ZModPrimePair) {
				// calling getZStarModOrder() twice is necessary here
				this.zStarMod = ((ZModPrimePair) this.zMod).getZStarModOrder().getZStarModOrder();
			} else {
				// keys can only be generated if p and q are known
				throw new UnsupportedOperationException();
			}
		}
		return this.zStarMod;
	}

	@Override
	protected Function defaultGetPrivateKeyGenerationFunction() {
		return CompositeFunction.getInstance(
			   RandomFunction.getInstance(this.getZStarMod()),
			   ConvertFunction.getInstance(this.getZStarMod(), this.zMod, BigIntegerToBigInteger.getInstance()));
	}

	@Override
	protected Function abstractGetPublicKeyGenerationFunction() {
		return CompositeFunction.getInstance(
			   ConvertFunction.getInstance(this.zMod, this.getZStarMod(), BigIntegerToBigInteger.getInstance()),
			   InvertFunction.getInstance(this.getZStarMod()),
			   ConvertFunction.getInstance(this.getZStarMod(), this.zMod, BigIntegerToBigInteger.getInstance()));
	}

	public static RSAKeyGenerator getInstance(ZMod zMod) {
		return RSAKeyGenerator.getInstance(zMod, StringToByteArray.getInstance());
	}

	public static RSAKeyGenerator getInstance(ZMod zMod, StringToByteArray converter) {
		if (zMod == null || converter == null) {
			throw new IllegalArgumentException();
		}
		return new RSAKeyGenerator(zMod, converter);
	}

}
