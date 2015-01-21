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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.interfaces;

import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.unicrypt.math.algebra.multiplicative.interfaces.MultiplicativeGroup;

/**
 * This interface represents the mathematical concept of a field. A field is a commutative ring in which one can divide
 * by any nonzero element. It is implemented as a specialization of {@link Ring}.
 * <p>
 * @author rolfhaenni
 * @param <V> Generic type of values stored in the elements of this field
 */
public interface Field<V extends Object>
	   extends Ring<V> {

	/**
	 * Returns the multiplicative group of this field.
	 * <p>
	 * @return the multiplicative group of this field
	 */
	public MultiplicativeGroup<V> getMultiplicativeGroup();

	/**
	 * Returns the fraction element1 over element2.
	 * <p>
	 * @param element1 The given numerator
	 * @param element2 The given denominator
	 * @return the result from dividing element1 with element2
	 */
	public DualisticElement<V> divide(Element element1, Element element2);

	/**
	 * Returns the fraction one over the given element.
	 * <p>
	 * @param element The given element
	 * @return one over the given element
	 */
	public DualisticElement<V> oneOver(Element element);

}
