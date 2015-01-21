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
package lib.unicrypt.examples.src.main.java.ch.bfh.unicrypt.crypto.mixer;

import ch.bfh.unicrypt.Example;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.mixer.classes.IdentityMixer;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.PermutationElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.CyclicGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModSafePrime;

/**
 *
 * @author philipp
 */
public class IdentityMixerExample {

	public static void example1() {

		// Create cyclic group (modulo 20 bits)
		CyclicGroup G_q = GStarModSafePrime.getRandomInstance(20);

		// Set size
		int size = 10;

		// Create identities
		Tuple identities = ProductGroup.getInstance(G_q, size).getRandomElement();

		// Create mixer and perform shuffle
		IdentityMixer mixer = IdentityMixer.getInstance(G_q, size);
		Tuple shuffledIdentities = mixer.shuffle(identities);

		Example.printLines("Identities", identities);
		Example.printLines("Shuffled Identities", shuffledIdentities);
	}

	public static void example2() {

		// Create cyclic group (modulo 20 bits)
		CyclicGroup G_q = GStarModSafePrime.getRandomInstance(20);

		// Set size
		int size = 10;

		// Create identities
		Tuple identities = ProductGroup.getInstance(G_q, size).getRandomElement();

		// Create mixer and shuffle
		IdentityMixer mixer = IdentityMixer.getInstance(G_q, size);

		// Create permutation
		PermutationElement permutation = mixer.getPermutationGroup().getRandomElement();

		// Create randomization
		Element randomization = mixer.generateRandomization();

		// Perfom shuffle
		Tuple shuffledIdentities = mixer.shuffle(identities, permutation, randomization);

		Example.printLines("Identities", identities);
		Example.printLines("Shuffled Identities", shuffledIdentities);
	}

	public static void main(final String[] args) {
		Example.runExamples();
	}

}
