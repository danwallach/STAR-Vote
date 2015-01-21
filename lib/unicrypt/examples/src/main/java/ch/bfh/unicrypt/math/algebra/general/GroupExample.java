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
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.additive.interfaces.AdditiveElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.N;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.Z;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZMod;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.multiplicative.classes.ZStarMod;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.multiplicative.classes.ZStarModPrime;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.multiplicative.interfaces.MultiplicativeElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.factorization.Factorization;
import java.math.BigInteger;

public class GroupExample {

	public static void example1() {

		AdditiveElement a1;
		AdditiveElement a2;
		MultiplicativeElement m1;
		MultiplicativeElement m2;

		// TEST Z
		final Z group1 = Z.getInstance();
		System.out.println(group1.getIdentityElement());
		System.out.println(group1.getOrder());
		System.out.println(group1.getDefaultGenerator());
		for (int i = 1; i <= 3; i++) {
			System.out.println(group1.getRandomGenerator());
		}
		a1 = group1.getElement(3);
		a2 = group1.getElement(-5);
		System.out.println(group1.invert(a1));
		System.out.println(group1.invert(a2));
		System.out.println(group1.apply(a1, a2));
		System.out.println(group1.add(a1, a2));
		System.out.println(group1.selfApply(a1, a2));
		System.out.println(group1.times(a1, a2));
		System.out.println(group1.apply(a1, a1, a1, a1));
		System.out.println();

		// TEST Z
		final N group_n = N.getInstance();
		System.out.println(group_n.getIdentityElement());
		System.out.println(group_n.getOrder());

		a1 = group_n.getElement(3);
		a2 = group_n.getElement(5);

		System.out.println(group_n.apply(a1, a2));
		System.out.println(group_n.add(a1, a2));
		System.out.println(group_n.selfApply(a1, a2));
		System.out.println(group_n.times(a1, a2));
		System.out.println(group_n.apply(a1, a1, a1, a1));
		System.out.println();

		// TEST ZPLUSMOD
		final ZMod group2 = ZMod.getInstance(BigInteger.valueOf(10));
		System.out.println(group2.getIdentityElement());
		System.out.println(group2.getOrder());
		System.out.println(group2.getDefaultGenerator());
		for (int i = 1; i <= 3; i++) {
			System.out.println(group2.getRandomGenerator());
		}
		a1 = group2.getElement(3);
		a2 = group2.getElement(5);
		System.out.println(group2.invert(a1));
		System.out.println(group2.invert(a2));
		System.out.println(group2.apply(a1, a2));
		System.out.println(group2.add(a1, a2));
		System.out.println(group2.selfApply(a1, a2));
		System.out.println(group2.times(a1, a2));
		System.out.println(group2.apply(a1, a1, a1, a1));
		System.out.println();

		// TEST ZSTARMOD
		// 1) unknown order
		final ZStarMod group3 = ZStarMod.getInstance(BigInteger.valueOf(10));
		System.out.println(group3.getIdentityElement());
		System.out.println(group3.getOrder());
		m1 = group3.getElement(3);
		m2 = group3.getElement(7);
		System.out.println(group3.invert(m1));
		System.out.println(group3.invert(m2));
		System.out.println(group3.apply(m1, m2));
		System.out.println(group3.multiply(m1, m2));
		System.out.println(group3.selfApply(m1, m2));
		System.out.println(group3.power(m1, m2));
		System.out.println(group3.apply(m1, m1, m1, m1));
		System.out.println();

		// 2) known order
		final ZStarMod group4 = ZStarModPrime.getInstance(Factorization.getInstance(BigInteger.valueOf(2), BigInteger.valueOf(2), BigInteger.valueOf(5)));
		System.out.println(group4.getIdentityElement());
		System.out.println(group4.getOrder()); // 8
		m1 = group4.getElement(3);
		m2 = group4.getElement(11);
		System.out.println(group4.invert(m1));
		System.out.println(group4.invert(m2));
		System.out.println(group4.apply(m1, m2));
		System.out.println(group4.multiply(m1, m2));
		System.out.println(group4.selfApply(m1, m2));
		System.out.println(group4.power(m1, m2));
		System.out.println(group4.apply(m1, m1, m1, m1));
		System.out.println();

		// TEST MULTISELFAPPLY IN ADDITIVE GROUP
		final Element e1 = group1.getElement(2);
		final Element e2 = group1.getElement(3);
		final Element e3 = group1.getElement(5);
		final BigInteger i1 = BigInteger.valueOf(3);
		final BigInteger i2 = BigInteger.valueOf(2);
		final BigInteger i3 = BigInteger.valueOf(1);
		System.out.println(group1.multiSelfApply(new Element[]{e1, e2, e3}, new BigInteger[]{i1, i2, i3}));

	}

	public static void main(final String[] args) {
		Example.runExamples();
	}

}
