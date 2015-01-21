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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes;

import ch.bfh.unicrypt.helper.Polynomial;
import ch.bfh.unicrypt.math.algebra.dualistic.abstracts.AbstractDualisticElement;
import ch.bfh.unicrypt.math.algebra.dualistic.interfaces.DualisticElement;
import ch.bfh.unicrypt.math.algebra.dualistic.interfaces.SemiRing;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Pair;
import java.util.HashMap;

/**
 *
 * @author rolfhaenni
 * @param <V>
 */
public class PolynomialElement<V>
	   extends AbstractDualisticElement<PolynomialSemiRing<V>, PolynomialElement<V>, Polynomial<? extends DualisticElement<V>>> {

	protected PolynomialElement(final PolynomialSemiRing<V> semiRing, Polynomial<? extends DualisticElement<V>> polynomial) {
		super(semiRing, polynomial);
	}

	public DualisticElement<V> evaluate(DualisticElement element) {
		if (element == null || !this.getSet().getSemiRing().contains(element)) {
			throw new IllegalArgumentException();
		}

		if (this.getSet().isBinary()) {
			SemiRing semiRing = this.getSet().getSemiRing();
			if (semiRing.getZeroElement().isEquivalent(element)) {
				return this.getValue().getCoefficient(0);
			} else {
				return (this.getValue().getIndices().getLength() % 2) == 0 ? semiRing.getZeroElement() : semiRing.getOneElement();
			}
		}

		// TBD! (n*x^2 < q*x^3    with x = log(modulus), n = order of poly and q = number of non-zero terms in poly)
		int n = this.getValue().getDegree();
		int q = this.getValue().getIndices().getLength();
		if (n > 0 && ((double) q / n) < 0.01) {
			DualisticElement<V> result = this.getSet().getSemiRing().getZeroElement();
			for (Integer index : this.getValue().getIndices()) {
				result = result.add(this.getValue().getCoefficient(index).multiply(element.power(index)));
			}
			return result;
		} else {
			// Horner
			DualisticElement<V> r = this.getSet().getSemiRing().getZeroElement();
			for (int i = this.getValue().getDegree(); i >= 0; i--) {
				r = r.add(this.getValue().getCoefficient(i));
				if (i > 0) {
					r = r.multiply(element);
				}
			}
			return r;
		}
	}

	public Pair getPoint(DualisticElement element) {
		if (element == null || !this.getSet().getSemiRing().contains(element)) {
			throw new IllegalArgumentException();
		}
		return Pair.getInstance(element, this.evaluate(element));
	}

	public boolean isIrreducible() {
		if (!this.getSet().isRing()) {
			throw new UnsupportedOperationException();
		}
		return ((PolynomialRing<V>) this.getSet()).isIrreduciblePolynomial(this);
	}

	public PolynomialElement<V> reduce(DualisticElement<V> value) {
		if (!this.getSet().getSemiRing().isField()) {
			throw new UnsupportedOperationException();
		}
		if (value == null || !this.getSet().getSemiRing().contains(value)) {
			throw new IllegalArgumentException();
		}
		HashMap map = new HashMap();
		for (int i = 0; i <= this.getValue().getDegree(); i++) {
			DualisticElement<V> c = this.getValue().getCoefficient(i);
			if (!c.isZero()) {
				map.put(i, c.divide(value));
			}
		}
		return this.getSet().getElementUnchecked(map);
	}

}
