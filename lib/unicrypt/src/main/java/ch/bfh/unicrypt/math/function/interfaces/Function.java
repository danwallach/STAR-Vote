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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.interfaces;

import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Group;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.PartiallyAppliedFunction;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;

/**
 * This interface represents the concept a unary mathematical function f:X->Y. It takes an input element from one group
 * (the domain X) and maps it into an output element from another group (the co-domain Y). Although such functions
 * always operate with single input and output elements, it is possible that these values are tuple elements of
 * corresponding product groups. In such cases, the respective arities of these groups define thus the input and output
 * arities of the function. Most functions will be proper deterministic mathematical functions, but some functions may
 * be randomized and thus output different values when called multiple times.
 * <p>
 * @see Group
 * @see ProductGroup
 * @see <a
 * href="http://en.wikipedia.org/wiki/Function_(mathematics)">http://en.wikipedia.org/wiki/Function_(mathematics)</a>
 * <p>
 * @author R. Haenni
 * @author R. E. Koenig
 * @version 2.0
 */
public interface Function {

	public boolean isCompound();

	/**
	 * Applies the function to an input element from the domain and returns the resulting output element from the
	 * co-domain.
	 * <p>
	 * @param element The given input element
	 * @return The resulting output element
	 * @throws IllegalArgumentException if {@literal element} is null or not contained in the domain
	 */
	public Element apply(Element element);

	/**
	 * Applies the function to an input element from the domain and returns the resulting output element from the
	 * co-domain. In case of a randomized function, a random generator can be given as a second parameter. If no random
	 * generator is specified, i.e., if {@literal random} is null, then the system-wide random generator is taken. If
	 * the function is deterministic, then {@literal random} is ignored. This is the main the method to implement for
	 * any type of function.
	 * <p>
	 * @param element            The given input element
	 * @param randomByteSequence
	 * @return The resulting output element
	 * @throws IllegalArgumentException if {@literal element} is null or not contained in the domain
	 */
	public Element apply(Element element, RandomByteSequence randomByteSequence);

	/**
	 * This method provides a shortcut for applying a function with multiple input values. The specified elements are
	 * used to create a corresponding tuple element first, which is then used to call the ordinary method
	 * {@literal apply(Element element)}.
	 * <p>
	 * @param elements The given input elements
	 * @return The resulting output element
	 * @throws IllegalArgumentException if {@literal elements} is or contains null
	 * @throws IllegalArgumentException if the elements in {@literal elements} are not contained in the corresponding
	 *                                  sub-domains
	 * @throws IllegalArgumentException if the the length of {@literal elements} is different from
	 *                                  {@literal getArityIn()}
	 */
	public Element apply(Element... elements);

	/**
	 * This method provides a shortcut for applying a function with multiple input values. The specified elements are
	 * used to create a corresponding tuple element first, which is then used to call the ordinary method
	 * {@literal apply(Element element, Random random)}. In case of a randomized function, a random generator can be
	 * given as an additional parameter. If no random generator is specified, i.e., if {@literal random} is null, then
	 * the system-wide random generator is taken. If the function is deterministic, then {@literal random} is ignored.
	 * <p>
	 * @param elements           The given input elements
	 * @param randomByteSequence Either {@literal null} or a given random generator
	 * @return The resulting output element
	 * @throws IllegalArgumentException if {@literal elements} is or contains null
	 * @throws IllegalArgumentException if the elements in {@literal elements} are not contained in the corresponding
	 *                                  sub-domains
	 * @throws IllegalArgumentException if the the length of {@literal elements} is different from
	 *                                  {@literal getArityIn()}
	 */
	public Element apply(Element[] elements, RandomByteSequence randomByteSequence);

	public Element apply(RandomByteSequence randomByteSequence);

	/**
	 * Returns the domain of this function.
	 * <p>
	 * @return The domain
	 */
	public Set getDomain();

	/**
	 * Returns the co-domain of this function.
	 * <p>
	 * @return The co-domain
	 */
	public Set getCoDomain();

	/**
	 * This method applies a single input value to a given function. The result is a new function with an input arity
	 * decreased by 1.
	 * <p>
	 * @param element The given input value
	 * @param index   The index of the corresponding group in the product (or power group) domain
	 * @return The resulting partially applied function
	 * @throws IllegalArgumentException  if {@literal element} is null or not an element of the corresponding sub-domain
	 *                                   group
	 * @throws IndexOutOfBoundsException if {@literal index} is an invalid index
	 */
	public PartiallyAppliedFunction partiallyApply(Element element, int index);

	public boolean isEquivalent(Function function);

}
