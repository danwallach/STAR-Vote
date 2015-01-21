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
import ch.bfh.unicrypt.random.abstracts.AbstractRandomByteSequence;
import ch.bfh.unicrypt.random.interfaces.PseudoRandomByteSequence;
import java.io.Serializable;
import java.math.BigInteger;

/**
 * This PseudoRandomGeneratorCounterMode creates the hash value of the seed and stores this internally as a
 * ByteArrayElement. The hash will be done according to the given hashAlgorithm. Then the internal counter will be
 * created as another ByteArrayElement. These two byteArrayElement will be hashed as in @see
 * AbstractElement#getHashValueValueValue(HashAlgorithm hashAlgorithm); The resulting bytes will be used for
 * pseudoRandomness Please note that this PseudoRandomGenerator does not provide any security at all once the internal
 * state is known. This includes a total lack of forward security.
 * <p>
 * <p>
 * <p>
 * @author R. Haenni
 * @author R. E. Koenig
 * @version 1.0
 */
public class CounterModeRandomByteSequence
	   extends AbstractRandomByteSequence
	   implements PseudoRandomByteSequence, Serializable {

	public static final ByteArray DEFAULT_SEED = ByteArray.getInstance();
	/**
	 * This is the DEFAULT_PSEUDO_RANDOM_GENERATOR_COUNTER_MODE pseudoRandomGenerator At each start of the JavaVM this
	 * generator will restart deterministically. Do not use it for ephemeral keys!
	 */
	public static final CounterModeRandomByteSequence DEFAULT_PSEUDO_RANDOM_GENERATOR_COUNTER_MODE = CounterModeRandomByteSequence.getInstance(HashAlgorithm.getInstance(), DEFAULT_SEED);

	private final HashAlgorithm hashAlgorithm;
	private ByteArray seed;
	private ByteArray hashedSeed;
	private int counter;
	// TODO: Change it to ByteArray when it becomes iterable;
	private byte[] randomByteBuffer;
	private int randomByteBufferPosition;

	// Random random;
	protected CounterModeRandomByteSequence(HashAlgorithm hashAlgorithm, final ByteArray seed) {
		this.hashAlgorithm = hashAlgorithm;
		//The following lines are needed in order to speed up calculation of randomBytes. @see#fillRandomByteBuffer
		this.randomByteBuffer = new byte[hashAlgorithm.getHashLength()];
		setSeed(seed);
	}

	protected byte[] getRandomByteBuffer(int counter) {
		//Even though the following is the nice way to program it with unicrypt, it is too expensive. Reason: If the first part of a pair is a big tuple, it has to be hashed each time... Reprogram?!
		//this.digestBytes=Pair.getInstance(seed,Z.getInstance().getElement(counter)).getHashValue(hashAlgorithm).getByteArray();
		//-->This is, why the following implementation exists.
		return hashedSeed.append(ByteArray.getInstance(BigInteger.valueOf(counter).toByteArray())).getHashValue(this.hashAlgorithm).getBytes();
//		return digest.digest(hashedSeed.append(ByteArrayMonoid.getInstance().getElement(counter).getByteArray()).getBytes());
	}

	@Override
	public HashAlgorithm getHashAlgorithm() {
		return hashAlgorithm;
	}

	public ByteArray getSeed() {
		return this.seed;
	}

	@Override
	public void setSeed(ByteArray seed) {
		if (seed == null) {
			throw new IllegalArgumentException();
		}
		this.seed = seed;
		this.hashedSeed = seed.getHashValue();
		this.counter = -1;
		reset();
	}

	public int getCounter() {
		return this.counter;
	}

	protected void reset() {
		setCounter(0);

	}

	protected boolean isReset() {
		return counter == 0 && this.randomByteBufferPosition == 0;
	}

	protected void setCounter(final int counter) {
		//Do not re-calculate the hash if it is only the digestBytesPosition to be reset to 0
		if (this.counter != counter) {
			this.counter = counter;
			this.randomByteBuffer = getRandomByteBuffer(counter);
		}
		this.randomByteBufferPosition = 0;

	}

	/**
	 * Counter goes up after digest.length bytes, after initialization with sha256, 32bytes are ready to be read and
	 * counter is at 0 after having read 32 bytes counter jumps to 1 and another 32 bytes are ready to be read
	 * <p>
	 * @param length
	 * @return a new byte[] which will not be touched anymore.
	 */
	protected byte[] getNextBytes(int length) {
		byte[] randomBytes = new byte[length];
		int randomBytesPosition = 0;
		while (randomBytesPosition < length) {
			int amount = Math.min((length - randomBytesPosition), (randomByteBuffer.length - randomByteBufferPosition));
			System.arraycopy(randomByteBuffer, randomByteBufferPosition, randomBytes, randomBytesPosition, amount);
			randomBytesPosition += amount;
			randomByteBufferPosition += amount;
			if (randomByteBufferPosition == randomByteBuffer.length) {
				setCounter(getCounter() + 1);
			}
		}
		return randomBytes;
	}

	@Override
	public ByteArray getNextByteArray(int length) {
		return new InternalByteArray(getNextBytes(length));
	}

	/**
	 * This internal class allows to create a new ByteArray without having to clone the backing byte[]
	 */
	class InternalByteArray
		   extends ByteArray {

		private InternalByteArray(byte[] bytes) {
			super(bytes);
		}

	}

	@Override
	public byte getNextByte() {
		return getNextBytes(1)[0];
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 17 * hash + (this.hashAlgorithm != null ? this.hashAlgorithm.hashCode() : 0);
		hash = 17 * hash + (hashedSeed != null ? hashedSeed.hashCode() : 0);
		hash = 17 * hash + this.counter;
		hash = 17 * hash + this.randomByteBufferPosition;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final CounterModeRandomByteSequence other = (CounterModeRandomByteSequence) obj;
		if (this.hashAlgorithm != other.hashAlgorithm && (this.hashAlgorithm == null || !this.hashAlgorithm.equals(other.hashAlgorithm))) {
			return false;
		}
		if (this.hashCode() != other.hashCode()) {
			return false;
		}
		if (this.counter != other.counter) {
			return false;
		}
		if (this.randomByteBufferPosition != other.randomByteBufferPosition) {
			return false;
		}
		return true;
	}

	/**
	 * This will return the DEFAULT_PSEUDO_RANDOM_GENERATOR_COUNTER_MODE PseudoRandomGeneratorCounterMode.
	 * <p>
	 * @return
	 */
	public static CounterModeRandomByteSequence getInstance() {
		return CounterModeRandomByteSequence.DEFAULT_PSEUDO_RANDOM_GENERATOR_COUNTER_MODE;
	}

	public static CounterModeRandomByteSequence getInstance(HashAlgorithm hashAlgorithm) {
		return new CounterModeRandomByteSequence(hashAlgorithm, DEFAULT_SEED);
	}

	public static CounterModeRandomByteSequence getInstance(ByteArray seed) {
		return new CounterModeRandomByteSequence(HashAlgorithm.getInstance(), seed);
	}

	public static CounterModeRandomByteSequence getInstance(HashAlgorithm hashAlgorithm, ByteArray seed) {
		if (seed == null) {
			throw new IllegalArgumentException();
		}
		if (hashAlgorithm == null) {
			throw new IllegalArgumentException();
		}
		return new CounterModeRandomByteSequence(hashAlgorithm, seed);
	}

}
