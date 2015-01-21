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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.encryption.abstracts;

import ch.bfh.unicrypt.crypto.schemes.encryption.interfaces.EncryptionScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.scheme.abstracts.AbstractScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.interfaces.Function;

/**
 *
 * @author rolfhaenni
 * @param <MS> Message space
 * @param <ME> Message element
 * @param <ES> Encryption space
 * @param <EE> Encryption element
 */
public abstract class AbstractEncryptionScheme<MS extends Set, ME extends Element, ES extends Set, EE extends Element>
	   extends AbstractScheme<MS>
	   implements EncryptionScheme {

	protected final ES encryptionSpace;
	private Function encryptionFunction;
	private Function decryptionFunction;

	public AbstractEncryptionScheme(MS messageSpace, ES encryptionSpace) {
		super(messageSpace);
		this.encryptionSpace = encryptionSpace;
	}

	@Override
	public final ES getEncryptionSpace() {
		return this.encryptionSpace;
	}

	@Override
	public final Function getEncryptionFunction() {
		if (this.encryptionFunction == null) {
			this.encryptionFunction = this.abstractGetEncryptionFunction();
		}
		return this.encryptionFunction;
	}

	@Override
	public final Function getDecryptionFunction() {
		if (this.decryptionFunction == null) {
			this.decryptionFunction = this.abstractGetDecryptionFunction();
		}
		return this.decryptionFunction;
	}

	@Override
	public EE encrypt(Element encryptionKey, Element message) {
		if (!this.getEncryptionKeySpace().contains(encryptionKey) || !this.getMessageSpace().contains(message)) {
			throw new IllegalArgumentException();
		}
		return (EE) this.getEncryptionFunction().apply(encryptionKey, message);
	}

	@Override
	public final ME decrypt(Element decryptionKey, Element encryption) {
		if (!this.getDecryptionKeySpace().contains(decryptionKey) || !this.getEncryptionSpace().contains(encryption)) {
			throw new IllegalArgumentException();
		}
		return (ME) this.getDecryptionFunction().apply(decryptionKey, encryption);
	}

	protected abstract Function abstractGetEncryptionFunction();

	protected abstract Function abstractGetDecryptionFunction();

}
