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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.keygenerator.abstracts;

import ch.bfh.unicrypt.crypto.keygenerator.interfaces.SecretKeyGenerator;
import ch.bfh.unicrypt.helper.array.classes.ByteArray;
import ch.bfh.unicrypt.helper.converter.classes.bytearray.StringToByteArray;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.SingletonGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.RandomFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.interfaces.Function;
import ch.bfh.unicrypt.random.classes.HybridRandomByteSequence;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;

public abstract class AbstractSecretKeyGenerator<KS extends Set, KE extends Element>
	   extends AbstractKeyGenerator
	   implements SecretKeyGenerator {

	private final KS secretKeySpace;
	private Function secretKeyGenerationFunction; // with singleton domain

	protected AbstractSecretKeyGenerator(final KS secretKeySpace, StringToByteArray converter) {
		super(converter);
		this.secretKeySpace = secretKeySpace;
	}

	@Override
	public final KS getSecretKeySpace() {
		return this.secretKeySpace;
	}

	@Override
	public KE generateSecretKey() {
		return this.generateSecretKey(HybridRandomByteSequence.getInstance());
	}

	@Override
	public KE generateSecretKey(RandomByteSequence randomByteSequence) {
		return (KE) this.getSecretKeyGenerationFunction().apply(SingletonGroup.getInstance().getElement(), randomByteSequence);
	}

	@Override
	public KE generateSecretKey(String password) {
		return this.generateSecretKey(password, ByteArray.getInstance());
	}

	@Override
	public KE generateSecretKey(String password, ByteArray salt) {
		if (password == null || salt == null) {
			throw new IllegalArgumentException();
		}
		ByteArray seed = converter.convert(password).append(salt);
		return this.generateSecretKey(HybridRandomByteSequence.getInstance(seed));
	}

	@Override
	public Function getSecretKeyGenerationFunction() {
		if (this.secretKeyGenerationFunction == null) {
			this.secretKeyGenerationFunction = this.defaultGetSecretKeyGenerationFunction();
		}
		return this.secretKeyGenerationFunction;
	}

	protected Function defaultGetSecretKeyGenerationFunction() {
		return RandomFunction.getInstance(this.getSecretKeySpace());
	}

	@Override
	protected String defaultToStringValue() {
		return this.getSecretKeySpace().toString();
	}

}
