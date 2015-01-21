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
import ch.bfh.unicrypt.crypto.schemes.encryption.interfaces.AsymmetricEncryptionScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Set;

/**
 *
 * @author rolfhaenni
 * @param <MS>  Message space
 * @param <ME>  Message element
 * @param <ES>  Encryption space
 * @param <EE>  Encryption element
 * @param <EKS> Encryption key space
 * @param <DKS> Decryption key space
 * @param <KG>  Key pair generator
 */
public abstract class AbstractAsymmetricEncryptionScheme<MS extends Set, ME extends Element, ES extends Set, EE extends Element, EKS extends Set, DKS extends Set, KG extends KeyPairGenerator>
	   extends AbstractEncryptionScheme<MS, ME, ES, EE>
	   implements AsymmetricEncryptionScheme {

	private KG keyPairGenerator;

	public AbstractAsymmetricEncryptionScheme(MS messageSpace, ES encryptionSpace) {
		super(messageSpace, encryptionSpace);
	}

	@Override
	public final KG getKeyPairGenerator() {
		if (this.keyPairGenerator == null) {
			this.keyPairGenerator = this.abstractGetKeyPairGenerator();
		}
		return this.keyPairGenerator;
	}

	@Override
	public final EKS getEncryptionKeySpace() {
		return (EKS) this.getKeyPairGenerator().getPublicKeySpace();
	}

	@Override
	public final DKS getDecryptionKeySpace() {
		return (DKS) this.getKeyPairGenerator().getPrivateKeySpace();
	}

	protected abstract KG abstractGetKeyPairGenerator();

}
