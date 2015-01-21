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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.sharing.abstracts;

import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.scheme.abstracts.AbstractScheme;
import ch.bfh.unicrypt.crypto.schemes.sharing.interfaces.SecretSharingScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductSet;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import ch.bfh.unicrypt.random.classes.HybridRandomByteSequence;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;

public abstract class AbstractSecretSharingScheme<MS extends Set, ME extends Element, SS extends Set, SE extends Element>
	   extends AbstractScheme<MS>
	   implements SecretSharingScheme {

	protected final SS shareSpace;
	protected final int size;

	protected AbstractSecretSharingScheme(MS messageSpace, SS shareSpace, int size) {
		super(messageSpace);
		this.shareSpace = shareSpace;
		this.size = size;
	}

	@Override
	public final SS getShareSpace() {
		return this.shareSpace;
	}

	@Override
	public final int getSize() {
		return this.size;
	}

	@Override
	public final Tuple share(Element message) {
		return this.share(message, HybridRandomByteSequence.getInstance());
	}

	@Override
	public final Tuple share(Element message, RandomByteSequence randomByteSequence) {
		if (message == null || !this.getMessageSpace().contains(message) || randomByteSequence == null) {
			throw new IllegalArgumentException();
		}
		return this.abstractShare(message, randomByteSequence);
	}

	@Override
	public final ME recover(Element... shares) {
		return this.recover(Tuple.getInstance(shares));
	}

	@Override
	public final ME recover(Tuple shares) {
		if (shares == null || shares.getArity() < this.getThreshold() || shares.getArity() > this.getSize()
			   || !ProductSet.getInstance(this.getShareSpace(), shares.getArity()).contains(shares)) {
			throw new IllegalArgumentException();
		}
		return abstractRecover(shares);
	}

	// this method is not really needed here, but it simplifies the method recover
	protected int getThreshold() {
		return this.getSize();
	}

	protected abstract Tuple abstractShare(Element message, RandomByteSequence randomByteSequence);

	protected abstract ME abstractRecover(Tuple shares);

}
