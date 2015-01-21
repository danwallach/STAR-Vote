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
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductSemiGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.ProductFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.interfaces.Function;

public class PreimageAndProofSystem
	   extends AbstractPreimageProofSystem<ProductSemiGroup, Tuple, ProductSemiGroup, Tuple, ProductFunction> {

	protected PreimageAndProofSystem(final SigmaChallengeGenerator challengeGenerator, final ProductFunction proofFunction) {
		super(challengeGenerator, proofFunction);
	}

	public static PreimageAndProofSystem getInstance(final ProductFunction proofFunction) {
		return PreimageAndProofSystem.getInstance((Element) null, proofFunction);
	}

	public static PreimageAndProofSystem getInstance(final Element proverId, final ProductFunction proofFunction) {
		SigmaChallengeGenerator challengeGenerator = RandomOracleSigmaChallengeGenerator.getInstance(proofFunction, proverId);
		return PreimageAndProofSystem.getInstance(challengeGenerator, proofFunction);
	}

	public static PreimageAndProofSystem getInstance(final SigmaChallengeGenerator challengeGenerator, final Function... proofFunctions) {
		return PreimageAndProofSystem.getInstance(challengeGenerator, ProductFunction.getInstance(proofFunctions));
	}

	public static PreimageAndProofSystem getInstance(final SigmaChallengeGenerator challengeGenerator, final Function proofFunction, int arity) {
		return PreimageAndProofSystem.getInstance(challengeGenerator, ProductFunction.getInstance(proofFunction, arity));
	}

	public static PreimageAndProofSystem getInstance(final SigmaChallengeGenerator challengeGenerator, final ProductFunction proofFunction) {
		if (challengeGenerator == null || proofFunction == null || proofFunction.getArity() < 1
			   || !proofFunction.getDomain().isSemiGroup() || !proofFunction.getCoDomain().isSemiGroup()) {
			throw new IllegalArgumentException();
		}
		if (PreimageAndProofSystem.checkSpaceEquality(challengeGenerator, proofFunction)) {
			throw new IllegalArgumentException("Spaces of challenge generator and proof function are inequal.");
		}
		return new PreimageAndProofSystem(challengeGenerator, proofFunction);
	}

}
