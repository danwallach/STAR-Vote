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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes;

import ch.bfh.unicrypt.helper.array.classes.DenseArray;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductSet;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import ch.bfh.unicrypt.math.function.abstracts.AbstractCompoundFunction;
import ch.bfh.unicrypt.math.function.interfaces.Function;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;

/**
 * This class represents the concept of a product function f:(X_1x...xX_n)->(Y_1x...xY_n). It consists of multiple
 * individual functions f_i:X_i->Y_i, which are applied in parallel to respective input elements. To be compatible with
 * {@link Function}, these input elements must be given as a tuple element of the product domain X_1x...xX_n. In the
 * same way, the output elements are returned as a tuple element of the product domain Y_1x...xY_n.
 * <p>
 * @author R. Haenni
 * @author R. E. Koenig
 * @version 1.0
 */
public final class SharedDomainFunction
	   extends AbstractCompoundFunction<SharedDomainFunction, Set, Element, ProductSet, Tuple> {

	/**
	 * This is the general constructor of this class. It takes a list of functions as input and produces the
	 * corresponding product function.
	 * <p>
	 * @param domain
	 * @param coDomain
	 * @param functions
	 * @throws IllegalArgumentException if {@literal functions} is null or contains null
	 */
	protected SharedDomainFunction(final Set domain, ProductSet coDomain, DenseArray<Function> functions) {
		super(domain, coDomain, functions);
	}

	//
	// The following protected method implements the abstract method from {@code AbstractFunction}
	//
	@Override
	protected Tuple abstractApply(final Element element, final RandomByteSequence randomByteSequence) {
		final Element[] elements = new Element[this.getArity()];
		for (int i : this.getAllIndices()) {
			elements[i] = this.getAt(i).apply(element, randomByteSequence);
		}
		return this.getCoDomain().getElement(elements);
	}

	@Override
	protected SharedDomainFunction abstractGetInstance(DenseArray<Function> functions) {
		return SharedDomainFunction.getInstance(functions);
	}

	@Override
	protected Class getArrayClass() {
		return SharedDomainFunction.class;
	}

	//
	// STATIC FACTORY METHODS
	//
	public static SharedDomainFunction getInstance(DenseArray<Function> functions) {
		if (functions == null || functions.getLength() == 0) {
			throw new IllegalArgumentException();
		}
		Set domain;
		ProductSet coDomain;
		if (functions.isUniform()) {
			domain = functions.getFirst().getDomain();
			coDomain = ProductSet.getInstance(functions.getFirst().getCoDomain(), functions.getLength());
		} else {
			domain = null;
			Set[] coDomains = new Set[functions.getLength()];
			for (int i = 0; i < functions.getLength(); i++) {
				Set nextDomain = functions.getAt(i).getDomain();
				if (domain == null) {
					domain = nextDomain;
				} else if (!domain.isEquivalent(nextDomain)) {
					throw new IllegalArgumentException();
				}
				coDomains[i] = functions.getAt(i).getCoDomain();
			}
			coDomain = ProductSet.getInstance(coDomains);
		}
		return new SharedDomainFunction(domain, coDomain, functions);
	}

	public static SharedDomainFunction getInstance(final Function... functions) {
		return SharedDomainFunction.getInstance(DenseArray.getInstance(functions));
	}

	public static SharedDomainFunction getInstance(final Function function, final int arity) {
		return SharedDomainFunction.getInstance(DenseArray.getInstance(function, arity));
	}

}
