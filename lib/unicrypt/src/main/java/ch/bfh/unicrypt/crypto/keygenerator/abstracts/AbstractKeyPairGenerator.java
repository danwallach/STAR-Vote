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

import ch.bfh.unicrypt.crypto.keygenerator.interfaces.KeyPairGenerator;
import ch.bfh.unicrypt.helper.array.classes.ByteArray;
import ch.bfh.unicrypt.helper.converter.classes.bytearray.StringToByteArray;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Pair;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductSet;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.SingletonGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.CompositeFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.IdentityFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.RandomFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.SharedDomainFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.interfaces.Function;
import ch.bfh.unicrypt.random.classes.HybridRandomByteSequence;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;

/**
 *
 * @author rolfhaenni
 * @param <PRS> Private key space
 * @param <PRE> Private key element
 * @param <PUS> Public key space
 * @param <PUE> Public key element
 */
public abstract class AbstractKeyPairGenerator<PRS extends Set, PRE extends Element, PUS extends Set, PUE extends Element>
	   extends AbstractKeyGenerator
	   implements KeyPairGenerator {

	private final PRS privateKeySpace;
	private final PUS publicKeySpace;

	private Function privateKeyGenerationFunction;
	private Function publicKeyGenerationFunction;
	private Function keyPairGenerationFunction;

	/**
	 *
	 * @param privateKeySpace
	 * @param publicKeySpace
	 * @param converter
	 */
	protected AbstractKeyPairGenerator(PRS privateKeySpace, PUS publicKeySpace, StringToByteArray converter) {
		super(converter);
		this.privateKeySpace = privateKeySpace;
		this.publicKeySpace = publicKeySpace;
	}

	@Override
	public final PRS getPrivateKeySpace() {
		return this.privateKeySpace;
	}

	@Override
	public final PUS getPublicKeySpace() {
		return this.publicKeySpace;
	}

	@Override
	public final Function getPrivateKeyGenerationFunction() {
		if (this.privateKeyGenerationFunction == null) {
			this.privateKeyGenerationFunction = this.defaultGetPrivateKeyGenerationFunction();
		}
		return this.privateKeyGenerationFunction;
	}

	@Override
	public final PRE generatePrivateKey() {
		return this.generatePrivateKey(HybridRandomByteSequence.getInstance());
	}

	/**
	 *
	 * @param randomByteSequence
	 * @return
	 */
	@Override
	public final PRE generatePrivateKey(RandomByteSequence randomByteSequence) {
		return (PRE) this.getPrivateKeyGenerationFunction().apply(SingletonGroup.getInstance().getElement(), randomByteSequence);
	}

	/**
	 *
	 * @param password
	 * @return
	 */
	@Override
	public PRE generatePrivateKey(String password) {
		return this.generatePrivateKey(password, ByteArray.getInstance());
	}

	/**
	 *
	 * @param password
	 * @param salt
	 * @return
	 */
	@Override
	public PRE generatePrivateKey(String password, ByteArray salt) {
		if (password == null || salt == null) {
			throw new IllegalArgumentException();
		}
		ByteArray seed = converter.convert(password).append(salt);
		return this.generatePrivateKey(HybridRandomByteSequence.getInstance(seed));
	}

	/**
	 *
	 * @return
	 */
	@Override
	public final Function getPublicKeyGenerationFunction() {
		if (this.publicKeyGenerationFunction == null) {
			this.publicKeyGenerationFunction = this.abstractGetPublicKeyGenerationFunction();
		}
		return this.publicKeyGenerationFunction;
	}

	/**
	 *
	 * @param privateKey
	 * @return
	 */
	@Override
	public final PUE generatePublicKey(Element privateKey) {
		return this.generatePublicKey(privateKey, HybridRandomByteSequence.getInstance());
	}

	/**
	 *
	 * @param privateKey
	 * @param randomByteSequence
	 * @return
	 */
	@Override
	public final PUE generatePublicKey(Element privateKey, RandomByteSequence randomByteSequence) {
		if (privateKey == null || !this.getPrivateKeySpace().contains(privateKey)) {
			throw new IllegalArgumentException();
		}
		return (PUE) this.getPublicKeyGenerationFunction().apply(privateKey, randomByteSequence);
	}

	/**
	 *
	 * @return
	 */
	@Override
	public ProductSet getKeyPairSpace() {
		return (ProductSet) this.getKeyPairGenerationFunction().getCoDomain();
	}

	//		this.keyPairSpace = ProductSet.getInstance(this.getPrivateKeySpace(), this.getPublicKeySpace());
	/**
	 *
	 * @return
	 */
	@Override
	public final Function getKeyPairGenerationFunction() {
		if (this.keyPairGenerationFunction == null) {
			this.keyPairGenerationFunction = CompositeFunction.getInstance(
				   this.getPrivateKeyGenerationFunction(),
				   SharedDomainFunction.getInstance(IdentityFunction.getInstance(this.privateKeySpace),
													this.getPublicKeyGenerationFunction()));
		}
		return this.keyPairGenerationFunction;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public Pair generateKeyPair() {
		return this.generateKeyPair(HybridRandomByteSequence.getInstance());
	}

	/**
	 *
	 * @param randomByteSequence
	 * @return
	 */
	@Override
	public Pair generateKeyPair(RandomByteSequence randomByteSequence) {
		return (Pair) this.getKeyPairGenerationFunction().apply(SingletonGroup.getInstance().getElement(), randomByteSequence);
	}

	/**
	 *
	 * @param password
	 * @return
	 */
	@Override
	public Pair generateKeyPair(String password) {
		return this.generateKeyPair(password, ByteArray.getInstance());
	}

	/**
	 *
	 * @param password
	 * @param salt
	 * @return
	 */
	@Override
	public Pair generateKeyPair(String password, ByteArray salt) {
		if (password == null || salt == null) {
			throw new IllegalArgumentException();
		}
		ByteArray seed = converter.convert(password).append(salt);
		return this.generateKeyPair(HybridRandomByteSequence.getInstance(seed));
	}

	/**
	 *
	 * @return
	 */
	@Override
	protected String defaultToStringValue() {
		return this.getPrivateKeySpace().toString() + ", " + this.getPublicKeySpace().toString();
	}

	/**
	 *
	 * @return
	 */
	protected Function defaultGetPrivateKeyGenerationFunction() {
		return RandomFunction.getInstance(this.getPrivateKeySpace());
	}

	/**
	 *
	 * @return
	 */
	protected abstract Function abstractGetPublicKeyGenerationFunction();

}
