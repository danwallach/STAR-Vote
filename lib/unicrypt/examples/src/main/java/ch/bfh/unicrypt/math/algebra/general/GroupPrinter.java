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
package lib.unicrypt.examples.src.main.java.ch.bfh.unicrypt.math.algebra.general;

import ch.bfh.unicrypt.Example;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class GroupPrinter {

	public static void example1() {

		Set<BigInteger> cyclic = new HashSet<BigInteger>();
		cyclic = new TreeSet<BigInteger>(cyclic);

		final boolean doubling = false;

		for (int i = 23; i <= 23; i++) {
			for (int j = 1; j <= 1; j++) {
				if (BigInteger.valueOf(i).isProbablePrime(20)) {
					BigInteger value = BigInteger.valueOf(i).pow(j);
					if (doubling) {
						value = value.multiply(BigInteger.valueOf(2));
					}
					cyclic.add(value);
				}
			}
		}
		System.out.println(cyclic);
		System.out.println();
		List<BigInteger> set;

		for (final BigInteger bi : cyclic) {
			set = new ArrayList<BigInteger>();
			for (int j = 1; j < bi.intValue(); j++) {
				final BigInteger bj = BigInteger.valueOf(j);
				if (bi.gcd(bj).equals(BigInteger.ONE)) {
					set.add(bj);
				}
			}
			final int order = set.size();
			System.out.println("n=" + bi + ", order=" + order);
			System.out.println(set);
			for (final BigInteger a : set) {
				BigInteger x = a;
				System.out.format("%2d: [ ", a);
				int k = 0;
				do {
					k++;
					System.out.format("%2d ", x);
					x = x.multiply(a).mod(bi);
				} while (!x.equals(a));
				System.out.println("] => " + k);
			}
			for (int j = 1; j <= order; j++) {
				if (BigInteger.valueOf(j).gcd(BigInteger.valueOf(order)).equals(BigInteger.valueOf(j))) {
					System.out.print("k=" + j + ": ");
					@SuppressWarnings("unused")
					Set<BigInteger> candSet = new HashSet<BigInteger>();
					for (final BigInteger alpha : set) {
						candSet.add(alpha.modPow(BigInteger.valueOf(j), bi));
					}
					candSet = new TreeSet<BigInteger>(candSet);
					System.out.println(candSet);
				}
			}
			System.out.println();
		}

	}

	public static void main(final String[] args) {
		Example.runExamples();
	}

}
