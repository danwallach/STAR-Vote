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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.random.classes;

import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.array.classes.ByteArray;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.hash.HashAlgorithm;
import java.io.Serializable;
import java.util.HashMap;

/**
 * This is a special 'instance' of a PseudoRandomGeneratorCounterMode which allows to
 * <p>
 * @author Rolf Haenni <rolf.haenni@bfh.ch>
 */
public class ReferenceRandomByteSequence
	   extends CounterModeRandomByteSequence
	   implements Serializable {

	/**
	 * This is the DEFAULT_PSEUDO_RANDOM_GENERATOR_COUNTER_MODE ReferenceRandomByteSequence. It uses the default
	 * HashAlgorithm and the default seed of PseudoRandomGeneratorCounterMode. TODO: Break the is-a relationship and
	 * make it a uses relationship! TODO: Bring in new methods for direct access to a certain position within the
	 * sequence!
	 */
	public static final ReferenceRandomByteSequence DEFAULT = ReferenceRandomByteSequence.getInstance(HashAlgorithm.getInstance(), CounterModeRandomByteSequence.DEFAULT_SEED);

	private transient HashMap<Integer, byte[]> randomByteBufferMap;
	private int javaHashValue;

	protected ReferenceRandomByteSequence(HashAlgorithm hashAlgorithm, ByteArray seed) {
		super(hashAlgorithm, seed);
	}

	@Override
	protected byte[] getRandomByteBuffer(int counter) {
		if (this.randomByteBufferMap == null) {
			this.randomByteBufferMap = new HashMap<Integer, byte[]>();
		}
		if (!this.randomByteBufferMap.containsKey(counter)) {
			this.randomByteBufferMap.put(counter, super.getRandomByteBuffer(counter));
		}
		return this.randomByteBufferMap.get(counter);
	}

	@Override
	public void reset() {
		super.reset();
	}

	@Override
	public boolean isReset() {
		return super.isReset();
	}

	public static ReferenceRandomByteSequence getInstance() {
		ReferenceRandomByteSequence sequence = new ReferenceRandomByteSequence(HashAlgorithm.getInstance(), DEFAULT_SEED);
		sequence.randomByteBufferMap = DEFAULT.randomByteBufferMap;
		return sequence;
	}

	public static ReferenceRandomByteSequence getInstance(ByteArray seed) {
		return ReferenceRandomByteSequence.getInstance(HashAlgorithm.getInstance(), seed);
	}

	public static ReferenceRandomByteSequence getInstance(HashAlgorithm hashAlgorithm) {
		return ReferenceRandomByteSequence.getInstance(hashAlgorithm, CounterModeRandomByteSequence.DEFAULT_SEED);
	}

	public static ReferenceRandomByteSequence getInstance(HashAlgorithm hashAlgorithm, ByteArray seed) {
		if (hashAlgorithm == null || seed == null) {
			throw new IllegalArgumentException();
		}
		return new ReferenceRandomByteSequence(hashAlgorithm, seed);
	}

	@Override
	public int hashCode() {
		if (this.javaHashValue == 0) {
			this.javaHashValue = getHashAlgorithm().hashCode() + getSeed().hashCode();
		}
		return this.javaHashValue;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ReferenceRandomByteSequence other = (ReferenceRandomByteSequence) obj;
		if (getHashAlgorithm() != getHashAlgorithm() && (!this.getHashAlgorithm().equals(other.getHashAlgorithm()))) {
			return false;
		}
		return this.getSeed().equals(other.getSeed());
	}

	class StatefulCounterModeRandomByteSequence
		   extends CounterModeRandomByteSequence {

		private transient HashMap<Integer, byte[]> randomByteBufferMap;

		public StatefulCounterModeRandomByteSequence(HashAlgorithm hashAlgorithm, ByteArray seed) {
			super(hashAlgorithm, seed);
		}

		@Override
		protected byte[] getRandomByteBuffer(int counter) {
			if (this.randomByteBufferMap == null) {
				this.randomByteBufferMap = new HashMap<Integer, byte[]>();
			}
			if (!this.randomByteBufferMap.containsKey(counter)) {
				this.randomByteBufferMap.put(counter, super.getRandomByteBuffer(counter));
			}
			return this.randomByteBufferMap.get(counter);
		}

	}

}
