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
package lib.unicrypt.examples.src.main.java.ch.bfh.unicrypt.crypto.schemes.commitment;

import ch.bfh.unicrypt.Example;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.commitment.classes.StandardCommitmentScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.BooleanElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModSafePrime;

/**
 *
 *
 */
public class StandardCommitmentExample {

	public static void example1() {

		// Create cyclic group G_q (modulo 167) and wth generator=98
		GStarModSafePrime cyclicGroup = GStarModSafePrime.getInstance(167);
		Element generator = cyclicGroup.getElement(98);

		// Create commitment scheme to be used
		StandardCommitmentScheme commitmentScheme = StandardCommitmentScheme.getInstance(generator);

		// Create message to commit
		Element message = commitmentScheme.getMessageSpace().getElement(42);

		// Create commitment
		Element commitment = commitmentScheme.commit(message);

		// Decommit
		BooleanElement result = commitmentScheme.decommit(message, commitment);

		Example.printLine("Cylic Group", cyclicGroup);
		Example.printLine("Message", message);
		Example.printLine("Commitment", commitment);
		Example.printLine("Result", result);
	}

	public static void main(final String[] args) {
		Example.runExamples();
	}

}
