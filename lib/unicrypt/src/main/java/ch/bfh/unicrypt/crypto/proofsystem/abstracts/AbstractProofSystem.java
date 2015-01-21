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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.proofsystem.abstracts;

import ch.bfh.unicrypt.crypto.proofsystem.interfaces.ProofSystem;
import ch.bfh.unicrypt.random.classes.HybridRandomByteSequence;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Set;

public abstract class AbstractProofSystem<PRS extends Set, PRE extends Element, PUS extends Set, PUE extends Element, PS extends Set, PE extends Element>
	   implements ProofSystem {

	@Override
	public final PE generate(final Element privateInput, final Element publicInput) {
		return this.generate(privateInput, publicInput, HybridRandomByteSequence.getInstance());
	}

	@Override
	public final PE generate(final Element privateInput, final Element publicInput, final RandomByteSequence randomByteSequence) {
		if (!this.getPrivateInputSpace().contains(privateInput) || !this.getPublicInputSpace().contains(publicInput) || randomByteSequence == null) {
			throw new IllegalArgumentException();
		}
		return this.abstractGenerate((PRE) privateInput, (PUE) publicInput, randomByteSequence);
	}

	@Override
	public final boolean verify(Element proof, Element publicInput) {
		if (!this.getProofSpace().contains(proof) || !this.getPublicInputSpace().contains(publicInput)) {
			throw new IllegalArgumentException();
		}
		return this.abstractVerify((PE) proof, (PUE) publicInput);
	}

	@Override
	public final PRS getPrivateInputSpace() {
		return this.abstractGetPrivateInputSpace();
	}

	@Override
	public final PUS getPublicInputSpace() {
		return this.abstractGetPublicInputSpace();
	}

	@Override
	public final PS getProofSpace() {
		return this.abstractGetProofSpace();
	}

	protected abstract PE abstractGenerate(PRE secretInput, PUE publicInput, RandomByteSequence randomByteSequence);

	protected abstract boolean abstractVerify(PE proof, PUE publicInput);

	protected abstract PRS abstractGetPrivateInputSpace();

	protected abstract PUS abstractGetPublicInputSpace();

	protected abstract PS abstractGetProofSpace();

}
