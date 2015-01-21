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
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.commitment.classes.PedersenCommitmentScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.array.classes.ByteArray;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.BooleanElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.CyclicGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModSafePrime;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.random.classes.ReferenceRandomByteSequence;
import java.math.BigInteger;

/**
 *
 * @author Rolf Haenni <rolf.haenni@bfh.ch>
 */
public class PedersenCommitmentExample {

	public static void example1() {

		// Create cyclic group G_q (modulo 167)
		CyclicGroup cyclicGroup = GStarModSafePrime.getInstance(167);

		// Create commitment scheme to be used
		PedersenCommitmentScheme commitmentScheme = PedersenCommitmentScheme.getInstance(cyclicGroup);

		// Create message and randomization to commit
		Element message = commitmentScheme.getMessageSpace().getElement(42);
		Element randomization = commitmentScheme.getRandomizationSpace().getRandomElement();

		// Create commitment
		Element commitment = commitmentScheme.commit(message, randomization);

		// Decommit
		BooleanElement result = commitmentScheme.decommit(message, randomization, commitment);

		Example.printLine("Cylic Group", cyclicGroup);
		Example.printLine("Message Generator", commitmentScheme.getMessageGenerator());
		Example.printLine("Randomization Generator", commitmentScheme.getRandomizationGenerator());
		Example.printLine("Message", message);
		Example.printLine("Randomization", randomization);
		Example.printLine("Commitment", commitment);
		Example.printLine("Result", result);
	}

	public static void example2() {
		//This example demonstrates the different steps in three Contexts:
		// Here it is assumed that the generators and the randomization are provided from 'outside'

		//1. Public parameters. Within this context the math and the generators are defined.
		//2. Commitment. This sub-context covers the commitment phase.
		//3. Decommitment. This sub-context cover the decommitment phase.
		//Public context
		{
			// Create cyclic group G_q (modulo 167)
			CyclicGroup cyclicGroup = GStarModSafePrime.getInstance(167);

			// Create two independent generators g=98, h=27;
			Element messageGenerator = cyclicGroup.getElementFrom(BigInteger.valueOf(27));
			Element randomElementGenerator = cyclicGroup.getElementFrom(BigInteger.valueOf(98)); //h=g^2 \mod{167} not really independent

			Example.printLine("Cylic Group (G_q)", cyclicGroup);
			Example.printLine("Message Generator (g)", messageGenerator);
			Example.printLine("Randomization Generator (h)", randomElementGenerator);

			Element commitmentElement = null;
			Element randomizationElement = null;
			Element messageElement = null;

			// Commitment context
			{
				// Create commitment scheme to be used
				PedersenCommitmentScheme commitmentScheme = PedersenCommitmentScheme.getInstance(randomElementGenerator, messageGenerator);

				// Create message (m) and randomization (r) to commit
				Element message = commitmentScheme.getMessageSpace().getElementFrom(42);
				Element randomization = commitmentScheme.getRandomizationSpace().getElementFrom(7); //Not really random!

				// Create commitment (h^m * g^r mod p)
				Element commitment = commitmentScheme.commit(message, randomization);

				//Publish commitment
				commitmentElement = commitment;

				Example.printLine("Commitment", commitment);

				//Publish for decommitment
				randomizationElement = randomization;
				messageElement = message;
			}

			//Decommitment context
			{

				// (Re-)Create commitment scheme to be used
				PedersenCommitmentScheme commitmentScheme = PedersenCommitmentScheme.getInstance(randomElementGenerator, messageGenerator);

				// Get the commitment
				Element commitment = commitmentElement;

				// Get the message (m)
				Element message = messageElement;

				// Get the randomization (r)
				Element randomization = randomizationElement;

				// Decommit
				BooleanElement result = commitmentScheme.decommit(message, randomization, commitment);

				Example.printLine("Result", result);
			}
		}
	}

	public static void example3() {
		// Here the generators are chosen by the use of a common reference string (consensed from outside).
		// randomization is provided from 'outside'

		//This example demonstrates the different steps in three Contexts:
		//1. Public parameters. Within this context the math and the common reference string is defined.
		//2. Commitment. This sub-context covers the commitment phase.
		//3. Decommitment. This sub-context cover the decommitment phase.
		//Public context
		{
			// Create cyclic group G_q (modulo 167)
			CyclicGroup cyclicGroup = GStarModSafePrime.getInstance(167);

			// Establish the common reference String by a given ReferenceRandomByteSequence
			ReferenceRandomByteSequence referenceRandomByteSequence = ReferenceRandomByteSequence.getInstance(ByteArray.getInstance("Hello World".getBytes()));

			Example.printLine("Cylic Group (G_q)", cyclicGroup);

			Element commitmentElement = null;
			Element randomizationElement = null;
			Element messageElement = null;

			// Commitment context
			{
				// Create commitment scheme to be used
				PedersenCommitmentScheme commitmentScheme = PedersenCommitmentScheme.getInstance(cyclicGroup, referenceRandomByteSequence);

				// Create message (m) and randomization (r) to commit
				Element message = commitmentScheme.getMessageSpace().getElementFrom(42);
				Element randomization = commitmentScheme.getRandomizationSpace().getElementFrom(7); //Not really random!

				// Create commitment (h^m * g^r mod p)
				Element commitment = commitmentScheme.commit(message, randomization);

				//Publish commitment
				commitmentElement = commitment;

				Example.printLine("Message Generator (g)", commitmentScheme.getMessageGenerator());
				Example.printLine("Randomization Generator (h)", commitmentScheme.getRandomizationGenerator());
				Example.printLine("Commitment", commitment);

				//Publish for decommitment
				randomizationElement = randomization;
				messageElement = message;

			}

			//Decommitment context
			{
				// (Re-)Create commitment scheme to be used
				PedersenCommitmentScheme commitmentScheme = PedersenCommitmentScheme.getInstance(cyclicGroup, referenceRandomByteSequence);

				// Get the commitment
				Element commitment = commitmentElement;

				// Get the message (m)
				Element message = messageElement;

				// Get the randomization (r)
				Element randomization = randomizationElement;

				// Decommit
				BooleanElement result = commitmentScheme.decommit(message, randomization, commitment);

				Example.printLine("Message Generator (g)", commitmentScheme.getMessageGenerator());
				Example.printLine("Randomization Generator (h)", commitmentScheme.getRandomizationGenerator());
				Example.printLine("Result", result);
			}
		}
	}

	public static void example4() {
		// Here the generators are chosen by the use of a common reference string (consensed from outside).
		// randomization is provided by using the internal true random byte sequence generator

		// randomization is provided by the true random byte sequence.
		//This example demonstrates the different steps in three Contexts:
		//1. Public parameters. Within this context the math and the common reference string is defined.
		//2. Commitment. This sub-context covers the commitment phase.
		//3. Decommitment. This sub-context cover the decommitment phase.
		//Public context
		{
			// Create cyclic group G_q (modulo 167)
			CyclicGroup cyclicGroup = GStarModSafePrime.getInstance(167);

			ReferenceRandomByteSequence referenceRandomByteSequence = ReferenceRandomByteSequence.getInstance(ByteArray.getInstance("Stock Exchange".getBytes()));

			Example.printLine("Cylic Group (G_q)", cyclicGroup);

			Element commitmentElement = null;
			Element randomizationElement = null;
			Element messageElement = null;

			// Commitment context
			{
				// Create commitment scheme to be used
				PedersenCommitmentScheme commitmentScheme = PedersenCommitmentScheme.getInstance(cyclicGroup, referenceRandomByteSequence);

				// Create message (m) and randomization (r) to commit
				Element message = commitmentScheme.getMessageSpace().getElementFrom(42);
				Element randomization = commitmentScheme.getRandomizationSpace().getRandomElement();

				// Create commitment (h^m * g^r mod p)
				Element commitment = commitmentScheme.commit(message, randomization);

				//Publish commitment
				commitmentElement = commitment;

				Example.printLine("Message Generator (g)", commitmentScheme.getMessageGenerator());
				Example.printLine("Randomization Generator (h)", commitmentScheme.getRandomizationGenerator());
				Example.printLine("Commitment", commitment);

				//Publish for decommitment
				randomizationElement = randomization;
				messageElement = message;
			}

			//Decommitment context
			{
				// (Re-)Create commitment scheme to be used
				PedersenCommitmentScheme commitmentScheme = PedersenCommitmentScheme.getInstance(cyclicGroup, referenceRandomByteSequence);

				// Get the commitment
				Element commitment = commitmentElement;

				// Get the message (m)
				Element message = messageElement;

				// Get the randomization (r)
				Element randomization = randomizationElement;

				// Decommit
				BooleanElement result = commitmentScheme.decommit(message, randomization, commitment);

				Example.printLine("Message Generator (g)", commitmentScheme.getMessageGenerator());
				Example.printLine("Randomization Generator (h)", commitmentScheme.getRandomizationGenerator());
				Example.printLine("Result", result);
			}
		}
	}

	public static void main(final String[] args) {
		Example.runExamples();
	}

}
