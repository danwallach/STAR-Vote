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

import ch.bfh.unicrypt.crypto.keygenerator.interfaces.KeyPairGenerator;
import ch.bfh.unicrypt.crypto.schemes.encryption.interfaces.ReEncryptionScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductSet;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Monoid;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.ApplyFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.CompositeFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.RemovalFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.SelectionFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.SharedDomainFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.interfaces.Function;
import ch.bfh.unicrypt.random.classes.HybridRandomByteSequence;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;

/**
 *
 * @author rolfhaenni
 * @param <MS>  Message space
 * @param <ME>  Message element
 * @param <ES>  Encryption space
 * @param <EE>  Encryption element
 * @param <RS>  Randomization space
 * @param <RE>  Randomization element
 * @param <EKS> Encryption key space
 * @param <DKS> Decryption key space
 * @param <KG>  Key pair generator
 */
public abstract class AbstractReEncryptionScheme<MS extends Monoid, ME extends Element, ES extends Monoid, EE extends Element, RS extends Monoid, RE extends Element, EKS extends Set, DKS extends Set, KG extends KeyPairGenerator>
	   extends AbstractRandomizedEncryptionScheme<MS, ME, ES, EE, RS, RE, EKS, DKS, KG>
	   implements ReEncryptionScheme {

	private Function identityEncryptionFunction;
	private Function reEncryptionFunction;

	public AbstractReEncryptionScheme(MS messageSpace, ES encryptionSpace, RS randomizationSpace) {
		super(messageSpace, encryptionSpace, randomizationSpace);
	}

	@Override
	public final Function getIdentityEncryptionFunction() {
		if (this.identityEncryptionFunction == null) {
			this.identityEncryptionFunction = this.getEncryptionFunction().partiallyApply(this.getMessageSpace().getIdentityElement(), 1);
		}
		return this.identityEncryptionFunction;
	}

	@Override
	public final Function getReEncryptionFunction() {
		if (this.reEncryptionFunction == null) {
			ProductSet inputSpace = ProductSet.getInstance(this.getEncryptionKeySpace(), this.getEncryptionSpace(), this.getRandomizationSpace());
			this.reEncryptionFunction = CompositeFunction.getInstance(
				   SharedDomainFunction.getInstance(SelectionFunction.getInstance(inputSpace, 1),
													CompositeFunction.getInstance(RemovalFunction.getInstance(inputSpace, 1),
																				  this.getIdentityEncryptionFunction())),
				   ApplyFunction.getInstance(this.getEncryptionSpace()));
		}
		return this.reEncryptionFunction;
	}

	@Override
	public final EE reEncrypt(final Element publicKey, final Element ciphertext) {
		return this.reEncrypt(publicKey, ciphertext, HybridRandomByteSequence.getInstance());
	}

	@Override
	public final EE reEncrypt(final Element publicKey, final Element ciphertext, RandomByteSequence randomByteSequence) {
		return this.reEncrypt(publicKey, ciphertext, this.getRandomizationSpace().getRandomElement(randomByteSequence));
	}

	@Override
	public final EE reEncrypt(final Element publicKey, final Element ciphertext, final Element randomization) {
		return (EE) this.getReEncryptionFunction().apply(publicKey, ciphertext, randomization);
	}

}
