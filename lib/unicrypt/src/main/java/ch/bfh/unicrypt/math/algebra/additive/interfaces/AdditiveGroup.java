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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.additive.interfaces;

import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Group;

/**
 * This interface represents an additively written {link Group}. It provides the renaming of one group operation. No
 * functionality is added.
 * <p>
 * @author R. Haenni
 * @author R. E. Koenig
 * @version 2.0
 * @param <V> Generic type of values stored in the elements of the group
 */
public interface AdditiveGroup<V extends Object>
	   extends Group<V>, AdditiveMonoid<V> {

	/**
	 * This method is a synonym for {@link #Group.applyInverse(Element, Element)}.
	 * <p>
	 * @param element1 the same as in {@link #Group.applyInverse(Element, Element)}
	 * @param element2 the same as in {@link #Group.applyInverse(Element, Element)}
	 * @return the same as in {@link #Group.applyInverse(Element, Element)}
	 * @throws IllegalArgumentException if {@code element1} or {@code element2} does not belong to the group
	 */
	public AdditiveElement<V> subtract(Element element1, Element element2);

	/**
	 * Computes and returns the inverse of a given group element.
	 * <p>
	 * @param element The given element
	 * @return The inverse element of the input element
	 */
	public AdditiveElement<V> negate(Element element);

	// The following methods are overridden from Monoid with an adapted return type
	@Override
	public AdditiveElement<V> invert(Element element);

	@Override
	public AdditiveElement<V> applyInverse(Element element1, Element element2);

}
