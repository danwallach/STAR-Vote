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

import ch.bfh.unicrypt.helper.converter.classes.TrivialConverter;
import ch.bfh.unicrypt.helper.converter.interfaces.Converter;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import ch.bfh.unicrypt.math.function.abstracts.AbstractFunction;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;

/**
 * This class represents the the concept of a function f:X->Y, which outputs the element of Y that corresponds to the
 * integer value of the input element.
 * <p/>
 * @author R. Haenni
 * @author R. E. Koenig
 * @version 2.0
 * @param <V>
 * @param <W>
 */
public class ConvertFunction<V extends Object, W extends Object>
	   extends AbstractFunction<ConvertFunction, Set<V>, Element<V>, Set<W>, Element<W>> {

	private final Converter<V, W> converter;

	private ConvertFunction(final Set<V> domain, final Set<W> coDomain, Converter<V, W> converter) {
		super(domain, coDomain);
		this.converter = converter;
	}

	@Override
	protected Element<W> abstractApply(final Element<V> element, final RandomByteSequence randomByteSequence) {
		return this.getCoDomain().getElement(this.converter.convert(element.getValue()));
	}

	/**
	 * This is the general factory method for this class. It creates an function that converts values from the domain
	 * into values from the co-domain.
	 * <p/>
	 * @param <V>
	 * @param <W>
	 * @param domain    The given domain
	 * @param coDomain  The given co-domain
	 * @param converter
	 * @return The resulting function
	 * @throws IllegalArgumentException if the domain or coDomain is null
	 */
	public static <V, W> ConvertFunction<V, W> getInstance(final Set<V> domain, final Set<W> coDomain, Converter<V, W> converter) {
		return new ConvertFunction<V, W>(domain, coDomain, converter);
	}

	public static <V> ConvertFunction<V, V> getInstance(Set<V> domain, Set<V> coDomain) {
		return new ConvertFunction<V, V>(domain, coDomain, TrivialConverter.<V>getInstance());
	}

}
