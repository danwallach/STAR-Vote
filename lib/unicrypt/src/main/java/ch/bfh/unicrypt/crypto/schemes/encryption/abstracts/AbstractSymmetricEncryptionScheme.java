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

import ch.bfh.unicrypt.crypto.keygenerator.interfaces.SecretKeyGenerator;
import ch.bfh.unicrypt.crypto.schemes.encryption.interfaces.SymmetricEncryptionScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Set;

/**
 *
 * @author rolfhaenni
 * @param <MS> Message space
 * @param <ME> Message element
 * @param <ES> Encryption space
 * @param <EE> Encryption element
 * @param <KS> Key space
 * @param <KE> Key element
 * @param <KG> Key generator
 */
public abstract class AbstractSymmetricEncryptionScheme<MS extends Set, ME extends Element, ES extends Set, EE extends Element, KS extends Set, KE extends Element, KG extends SecretKeyGenerator>
	   extends AbstractEncryptionScheme<MS, ME, ES, EE>
	   implements SymmetricEncryptionScheme {

	private KG keyGenerator;

	public AbstractSymmetricEncryptionScheme(MS messageSpace, ES encryptionSpace) {
		super(messageSpace, encryptionSpace);
	}

	@Override
	public final KG getSecretKeyGenerator() {
		if (this.keyGenerator == null) {
			this.keyGenerator = this.abstractGetKeyGenerator();
		}
		return this.keyGenerator;
	}

	@Override
	public final KS getEncryptionKeySpace() {
		return (KS) this.getSecretKeyGenerator().getSecretKeySpace();
	}

	@Override
	public final KS getDecryptionKeySpace() {
		return (KS) this.getSecretKeyGenerator().getSecretKeySpace();
	}

	@Override
	public final KE generateSecretKey() {
		return (KE) this.getSecretKeyGenerator().generateSecretKey();
	}

	protected abstract KG abstractGetKeyGenerator();

}
