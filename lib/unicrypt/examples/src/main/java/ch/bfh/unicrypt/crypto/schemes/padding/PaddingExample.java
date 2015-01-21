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
package lib.unicrypt.examples.src.main.java.ch.bfh.unicrypt.crypto.schemes.padding;

import ch.bfh.unicrypt.Example;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.padding.classes.ANSIPaddingScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.padding.classes.IECPaddingScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.padding.classes.ISOPaddingScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.padding.classes.PKCSPaddingScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.padding.classes.ZeroPaddingScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.concatenative.classes.ByteArrayElement;

/**
 *
 * @author Rolf Haenni <rolf.haenni@bfh.ch>
 */
public class PaddingExample {

	public static void example1() {
		// Create padding scheme
		ZeroPaddingScheme ps = ZeroPaddingScheme.getInstance(10);

		// Padding of first random message
		ByteArrayElement e1 = ps.getMessageSpace().getRandomElement(6);
		ByteArrayElement p1 = ps.pad(e1);

		// Padding of second random message
		ByteArrayElement e2 = ps.getMessageSpace().getRandomElement(10);
		ByteArrayElement p2 = ps.pad(e2);

		// Padding of third random message
		ByteArrayElement e3 = ps.getMessageSpace().getRandomElement(13);
		ByteArrayElement p3 = ps.pad(e3);

		Example.printLines(ps);
		Example.printLines("Padding of e1", e1, p1);
		Example.printLines("Padding of e2", e2, p2);
		Example.printLines("Padding of e3", e3, p3);

	}

	public static void example2() {
		// Create padding scheme
		ANSIPaddingScheme ps = ANSIPaddingScheme.getInstance(10);

		// Padding/unpadding of first random message
		ByteArrayElement e1 = ps.getMessageSpace().getRandomElement(6);
		ByteArrayElement p1 = ps.pad(e1);
		ByteArrayElement u1 = ps.unpad(p1);

		// Padding/unpadding of second random message
		ByteArrayElement e2 = ps.getMessageSpace().getRandomElement(10);
		ByteArrayElement p2 = ps.pad(e2);
		ByteArrayElement u2 = ps.unpad(p2);

		// Padding/unpadding of third random message
		ByteArrayElement e3 = ps.getMessageSpace().getRandomElement(13);
		ByteArrayElement p3 = ps.pad(e3);
		ByteArrayElement u3 = ps.unpad(p3);

		Example.printLines(ps);
		Example.printLines("Padding of e1", e1, p1, u1);
		Example.printLines("Padding of e2", e2, p2, u2);
		Example.printLines("Padding of e3", e3, p3, u3);

	}

	public static void example3() {
		// Create padding scheme
		PKCSPaddingScheme ps = PKCSPaddingScheme.getInstance(10);

		// Padding/unpadding of first random message
		ByteArrayElement e1 = ps.getMessageSpace().getRandomElement(6);
		ByteArrayElement p1 = ps.pad(e1);
		ByteArrayElement u1 = ps.unpad(p1);

		// Padding/unpadding of second random message
		ByteArrayElement e2 = ps.getMessageSpace().getRandomElement(10);
		ByteArrayElement p2 = ps.pad(e2);
		ByteArrayElement u2 = ps.unpad(p2);

		// Padding/unpadding of third random message
		ByteArrayElement e3 = ps.getMessageSpace().getRandomElement(13);
		ByteArrayElement p3 = ps.pad(e3);
		ByteArrayElement u3 = ps.unpad(p3);

		Example.printLines(ps);
		Example.printLines("Padding of e1", e1, p1, u1);
		Example.printLines("Padding of e2", e2, p2, u2);
		Example.printLines("Padding of e3", e3, p3, u3);

	}

	public static void example4() {
		// Create padding scheme
		ISOPaddingScheme ps = ISOPaddingScheme.getInstance(10);

		// Padding/unpadding of first random message
		ByteArrayElement e1 = ps.getMessageSpace().getRandomElement(6);
		ByteArrayElement p1 = ps.pad(e1);
		ByteArrayElement u1 = ps.unpad(p1);

		// Padding/unpadding of second random message
		ByteArrayElement e2 = ps.getMessageSpace().getRandomElement(10);
		ByteArrayElement p2 = ps.pad(e2);
		ByteArrayElement u2 = ps.unpad(p2);

		// Padding/unpadding of third random message
		ByteArrayElement e3 = ps.getMessageSpace().getRandomElement(13);
		ByteArrayElement p3 = ps.pad(e3);
		ByteArrayElement u3 = ps.unpad(p3);

		Example.printLines(ps);
		Example.printLines("Padding of e1", e1, p1, u1);
		Example.printLines("Padding of e2", e2, p2, u2);
		Example.printLines("Padding of e3", e3, p3, u3);

	}

	public static void example5() {
		// Create padding scheme
		IECPaddingScheme ps = IECPaddingScheme.getInstance(10);

		// Padding/unpadding of first random message
		ByteArrayElement e1 = ps.getMessageSpace().getRandomElement(6);
		ByteArrayElement p1 = ps.pad(e1);
		ByteArrayElement u1 = ps.unpad(p1);

		// Padding/unpadding of second random message
		ByteArrayElement e2 = ps.getMessageSpace().getRandomElement(10);
		ByteArrayElement p2 = ps.pad(e2);
		ByteArrayElement u2 = ps.unpad(p2);

		// Padding/unpadding of third random message
		ByteArrayElement e3 = ps.getMessageSpace().getRandomElement(13);
		ByteArrayElement p3 = ps.pad(e3);
		ByteArrayElement u3 = ps.unpad(p3);

		Example.printLines(ps);
		Example.printLines("Padding of e1", e1, p1, u1);
		Example.printLines("Padding of e2", e2, p2, u2);
		Example.printLines("Padding of e3", e3, p3, u3);

	}

	public static void main(final String[] args) {
		Example.runExamples();
	}

}
