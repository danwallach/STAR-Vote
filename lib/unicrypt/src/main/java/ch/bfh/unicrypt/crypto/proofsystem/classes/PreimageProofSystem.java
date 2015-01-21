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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.proofsystem.classes;

import ch.bfh.unicrypt.crypto.proofsystem.abstracts.AbstractPreimageProofSystem;
import ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.classes.RandomOracleSigmaChallengeGenerator;
import ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.interfaces.SigmaChallengeGenerator;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.SemiGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.interfaces.Function;

public class PreimageProofSystem
	   extends AbstractPreimageProofSystem<SemiGroup, Element, SemiGroup, Element, Function> {

	protected PreimageProofSystem(final SigmaChallengeGenerator challengeGenerator, final Function function) {
		super(challengeGenerator, function);
	}

	public static PreimageProofSystem getInstance(final Function proofFunction) {
		return PreimageProofSystem.getInstance((Element) null, proofFunction);
	}

	public static PreimageProofSystem getInstance(final Element proverId, final Function proofFunction) {
		SigmaChallengeGenerator challengeGenerator = RandomOracleSigmaChallengeGenerator.getInstance(proofFunction, proverId);
		return PreimageProofSystem.getInstance(challengeGenerator, proofFunction);
	}

	public static PreimageProofSystem getInstance(final SigmaChallengeGenerator challengeGenerator, final Function proofFunction) {
		if (challengeGenerator == null || proofFunction == null || !proofFunction.getDomain().isSemiGroup() || !proofFunction.getCoDomain().isSemiGroup()) {
			throw new IllegalArgumentException();
		}
		if (PreimageProofSystem.checkSpaceEquality(challengeGenerator, proofFunction)) {
			throw new IllegalArgumentException("Spaces of challenge generator and proof function are unequal.");
		}

		return new PreimageProofSystem(challengeGenerator, proofFunction);
	}

}
