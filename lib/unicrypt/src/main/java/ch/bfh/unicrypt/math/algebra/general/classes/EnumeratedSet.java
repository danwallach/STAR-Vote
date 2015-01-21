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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes;

import ch.bfh.unicrypt.helper.converter.abstracts.AbstractBigIntegerConverter;
import ch.bfh.unicrypt.helper.converter.interfaces.BigIntegerConverter;
import ch.bfh.unicrypt.math.algebra.general.abstracts.AbstractSet;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Rolf Haenni <rolf.haenni@bfh.ch>
 * @param <V>
 */
public class EnumeratedSet<V extends Object>
	   extends AbstractSet<EnumeratedSetElement<V>, V> {

	protected final Map<Integer, V> valueMap;
	protected final Map<V, Integer> indexMap;

	public EnumeratedSet(Map<Integer, V> valueMap, Map<V, Integer> indexMap, Class<V> valueClass) {
		super(valueClass);
		this.valueMap = valueMap;
		this.indexMap = indexMap;
	}

	@Override
	protected BigInteger abstractGetOrder() {
		return BigInteger.valueOf(this.valueMap.size());
	}

	@Override
	protected boolean abstractContains(V value) {
		return this.indexMap.containsKey(value);
	}

	@Override
	protected EnumeratedSetElement<V> abstractGetElement(V value) {
		return new EnumeratedSetElement<V>(this, value);
	}

	@Override
	protected BigIntegerConverter<V> abstractGetBigIntegerConverter() {
		return new AbstractBigIntegerConverter<V>(null) { // class parameter not needed

			@Override
			protected BigInteger abstractConvert(V value) {
				return BigInteger.valueOf(indexMap.get(value));
			}

			@Override
			protected V abstractReconvert(BigInteger value) {
				return valueMap.get(value.intValue());
			}
		};
	}

	@Override
	protected EnumeratedSetElement<V> abstractGetRandomElement(RandomByteSequence randomByteSequence) {
		int index = randomByteSequence.getRandomNumberGenerator().nextInteger(this.getOrder().intValue() - 1);
		return this.abstractGetElement(this.valueMap.get(index));
	}

	@Override
	protected boolean abstractEquals(Set set) {
		EnumeratedSet<?> other = (EnumeratedSet<?>) set;
		return this.valueMap.equals(other.valueMap);
	}

	@Override
	protected int abstractHashCode() {
		return this.valueMap.hashCode();
	}

	@Override
	protected String defaultToStringValue() {
		String str = this.valueMap.values().toString();
		return str.substring(1, str.length() - 1);
	}

	public static <V> EnumeratedSet<V> getInstance(V... values) {
		if (values == null || values.length == 0) {
			throw new IllegalArgumentException();
		}
		// both maps are needed for efficiency reasons
		Map<Integer, V> valueMap = new HashMap<Integer, V>();
		Map<V, Integer> indexMap = new HashMap<V, Integer>();
		int index = 0;
		for (V value : values) {
			if (value == null || valueMap.containsValue(value)) {
				throw new IllegalArgumentException();
			}
			valueMap.put(index, value);
			indexMap.put(value, index);
			index++;
		}
		return new EnumeratedSet<V>(valueMap, indexMap, (Class<V>) values.getClass().getComponentType());
	}

}
