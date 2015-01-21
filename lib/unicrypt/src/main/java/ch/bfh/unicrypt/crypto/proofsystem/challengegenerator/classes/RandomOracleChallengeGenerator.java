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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.classes;

import ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.abstracts.AbstractNonInteractiveChallengeGenerator;
import ch.bfh.unicrypt.helper.array.classes.ByteArray;
import ch.bfh.unicrypt.helper.converter.classes.ConvertMethod;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import ch.bfh.unicrypt.random.classes.PseudoRandomOracle;
import ch.bfh.unicrypt.random.classes.ReferenceRandomByteSequence;
import ch.bfh.unicrypt.random.interfaces.RandomOracle;

public class RandomOracleChallengeGenerator
	   extends AbstractNonInteractiveChallengeGenerator {

	protected final RandomOracle randomOracle;
	protected final ConvertMethod<ByteArray> convertMethod;

	protected RandomOracleChallengeGenerator(Set inputSpace, Set challengeSpace, Element proverId, final RandomOracle randomOracle, ConvertMethod<ByteArray> convertMethod) {
		super(inputSpace, challengeSpace, proverId);
		this.randomOracle = randomOracle;
		this.convertMethod = convertMethod;
	}

	public RandomOracle getRandomOracle() {
		return this.randomOracle;
	}

	public ConvertMethod<ByteArray> getConvertMethod() {
		return this.convertMethod;
	}

	@Override
	protected Element abstractAbstractGenerate(Element input) {
		ReferenceRandomByteSequence randomByteSequence = this.randomOracle.getReferenceRandomByteSequence(input.getByteArray(this.convertMethod));
		return this.getChallengeSpace().getRandomElement(randomByteSequence);
	}

	public static RandomOracleChallengeGenerator getInstance(final Set inputSpace, Set challengeSpace) {
		return RandomOracleChallengeGenerator.getInstance(inputSpace, challengeSpace, (Element) null, PseudoRandomOracle.getInstance(), ConvertMethod.<ByteArray>getInstance());
	}

	public static RandomOracleChallengeGenerator getInstance(final Set inputSpace, Set challengeSpace, Element proverId) {
		return RandomOracleChallengeGenerator.getInstance(inputSpace, challengeSpace, proverId, PseudoRandomOracle.getInstance(), ConvertMethod.<ByteArray>getInstance());
	}

	public static RandomOracleChallengeGenerator getInstance(final Set inputSpace, Set challengeSpace, RandomOracle randomOracle) {
		return RandomOracleChallengeGenerator.getInstance(inputSpace, challengeSpace, (Element) null, randomOracle, ConvertMethod.<ByteArray>getInstance());
	}

	public static RandomOracleChallengeGenerator getInstance(final Set inputSpace, Set challengeSpace, Element proverId, RandomOracle randomOracle) {
		return RandomOracleChallengeGenerator.getInstance(inputSpace, challengeSpace, proverId, randomOracle, ConvertMethod.<ByteArray>getInstance());

	}

	public static RandomOracleChallengeGenerator getInstance(final Set inputSpace, Set challengeSpace, ConvertMethod<ByteArray> convertMethod) {
		return RandomOracleChallengeGenerator.getInstance(inputSpace, challengeSpace, (Element) null, PseudoRandomOracle.getInstance(), convertMethod);
	}

	public static RandomOracleChallengeGenerator getInstance(final Set inputSpace, Set challengeSpace, Element proverId, ConvertMethod<ByteArray> convertMethod) {
		return RandomOracleChallengeGenerator.getInstance(inputSpace, challengeSpace, proverId, PseudoRandomOracle.getInstance(), convertMethod);
	}

	public static RandomOracleChallengeGenerator getInstance(final Set inputSpace, Set challengeSpace, RandomOracle randomOracle, ConvertMethod<ByteArray> convertMethod) {
		return RandomOracleChallengeGenerator.getInstance(inputSpace, challengeSpace, (Element) null, randomOracle, convertMethod);
	}

	public static RandomOracleChallengeGenerator getInstance(final Set inputSpace, Set challengeSpace, Element proverId, RandomOracle randomOracle, ConvertMethod<ByteArray> convertMethod) {
		if (inputSpace == null || challengeSpace == null || randomOracle == null || convertMethod == null) {
			throw new IllegalArgumentException();
		}
		return new RandomOracleChallengeGenerator(inputSpace, challengeSpace, proverId, randomOracle, convertMethod);
	}

}
