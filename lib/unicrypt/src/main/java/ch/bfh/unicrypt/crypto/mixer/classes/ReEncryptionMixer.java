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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.mixer.classes;

import ch.bfh.unicrypt.crypto.mixer.abstracts.AbstractMixer;
import ch.bfh.unicrypt.crypto.schemes.encryption.interfaces.ReEncryptionScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Monoid;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.interfaces.Function;

/**
 *
 * @author philipp
 */
public class ReEncryptionMixer
	   extends AbstractMixer<Monoid, Monoid> {

	private final ReEncryptionScheme reEncryptionScheme;
	private final Element publicKey;

	private ReEncryptionMixer(ReEncryptionScheme reEncryptionScheme, Element publicKey, int size) {
		super(size);
		this.reEncryptionScheme = reEncryptionScheme;
		this.publicKey = publicKey;
	}

	public ReEncryptionScheme getReEncryptionScheme() {
		return this.reEncryptionScheme;
	}

	public Element getPublicKey() {
		return this.publicKey;
	}

	@Override
	protected Function abstractGetShuffleFunction() {
		return this.getReEncryptionScheme().getReEncryptionFunction().partiallyApply(this.getPublicKey(), 0);
	}

	static public ReEncryptionMixer getInstance(ReEncryptionScheme reEncryptionScheme, Element publicKey, int size) {
		if (reEncryptionScheme == null || publicKey == null || !reEncryptionScheme.getEncryptionKeySpace().contains(publicKey) || size < 0) {
			throw new IllegalArgumentException();
		}
		return new ReEncryptionMixer(reEncryptionScheme, publicKey, size);
	}

}
