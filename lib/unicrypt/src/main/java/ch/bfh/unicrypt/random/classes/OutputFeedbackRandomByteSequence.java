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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This PseudoRandomGeneratorOutputFeedbackMode creates is the most conservative pseudo random generator. It updates its
 * internal state at each request by cryptographically hashing the internal state. In its extreme this guarantees
 * maximum forward security with leakage of at most 8 bits. In order to speed up this kind of random generator, the
 * security parameter (forward security) can be adjusted. The lower the security parameter, the faster the generator.
 * <p>
 * Please note that this PseudoRandomGenerator does not provide any 'backward' security once the internal state is known
 * to the adversary.
 * <p>
 * <p>
 * @author R. Haenni
 * @author R. E. Koenig
 * @version 1.0
 */
public class OutputFeedbackRandomByteSequence
	   extends AbstractRandomByteSequence
	   implements PseudoRandomByteSequence {

	private final HashAlgorithm hashAlgorithm;
	private ByteArray internalState;
	private final int forwardSecurityInBytes;

	private synchronized ByteArray getInternalState() {
		while (this.internalState == null) {
			try {
				wait(10000);
			} catch (InterruptedException ex) {
				//
			}
		}
		return this.internalState;
	}

	private synchronized void setInternalState(ByteArray internalState) {
		if (internalState == null) {
			return;
		}
		this.internalState = internalState;
		this.notifyAll();
	}

	// Random random;
	protected OutputFeedbackRandomByteSequence(HashAlgorithm hashAlgorithm, int forwardSecurityInBytes, final ByteArray seed) {
		this.hashAlgorithm = hashAlgorithm;
		this.forwardSecurityInBytes = forwardSecurityInBytes;
		this.setSeed(seed);
	}

	/**
	 * This implementation allows to bring in (XOR) fresh auxiliary data to the internalState. Then it updates the
	 * internal State using the full hash of the internal state.
	 * <p>
	 * @param freshData
	 */
	protected void update(ByteArray freshData) {
		if (freshData != null) {
			setInternalState(getInternalState().xorFillZero(freshData));
		}
		setInternalState(getInternalState().xorFillZero(getInternalState().getHashValue(this.hashAlgorithm)));
	}

	/**
	 * This implementation takes the resulting cryptographic hash and splits it into two parts: Internal State: length
	 * of forwardSecurityInBytes. Output: remaining
	 * <p>
	 * @return
	 */
	protected ByteArray update() {
		ByteArray[] full = getInternalState().getHashValue(this.hashAlgorithm).split(this.forwardSecurityInBytes);
		setInternalState(getInternalState().xorFillZero(full[0]));

		//Careful: Due to the underlying implementation of ByteArray, this  leaks information within the internal array.
		return full[1];
		//return ByteArray.getInstance(full[1].getBytes());

	}

	public int getForwardSecurityInBytes() {
		return this.forwardSecurityInBytes;
	}

	@Override
	public void setSeed(ByteArray seed) {
		setInternalState(seed.getHashValue(this.hashAlgorithm));
	}

	@Override
	public HashAlgorithm getHashAlgorithm() {
		return this.hashAlgorithm;
	}

	/**
	 * <p>
	 * @param length
	 * @return
	 */
	protected byte[] getNextBytes(int length) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		while (bytes.size() < length) {
			try {
				bytes.write(update().getBytes());
			} catch (IOException ex) {
				Logger.getLogger(OutputFeedbackRandomByteSequence.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		return Arrays.copyOf(bytes.toByteArray(), length);
	}

	@Override
	public ByteArray getNextByteArray(int length) {
		return new RandomizedByteArray(this.getNextBytes(length));
	}

	@Override
	public byte getNextByte() {
		return this.getNextBytes(1)[0];
	}

	/**
	 * This class allows the access to the protected constructor of ByteArray... This way no copy of the array is
	 * needed.
	 */
	class RandomizedByteArray
		   extends ByteArray {

		private RandomizedByteArray(byte[] randomBytes) {
			super(randomBytes);
		}

	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 17 * hash + (this.hashAlgorithm != null ? this.hashAlgorithm.hashCode() : 0);
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
		final OutputFeedbackRandomByteSequence other = (OutputFeedbackRandomByteSequence) obj;
		if (this.hashAlgorithm != other.hashAlgorithm && (this.hashAlgorithm == null || !this.hashAlgorithm.equals(other.hashAlgorithm))) {
			return false;
		}
		if (this.hashCode() != other.hashCode()) {
			return false;
		}
		if (!this.getInternalState().equals(other.getInternalState())) {
			return false;
		}
		return true;
	}

	public static OutputFeedbackRandomByteSequence getInstance(ByteArray seed) {
		return OutputFeedbackRandomByteSequence.getInstance(HashAlgorithm.getInstance(), seed);
	}

	public static OutputFeedbackRandomByteSequence getInstance(HashAlgorithm hashAlgorithm, ByteArray seed) {
		return OutputFeedbackRandomByteSequence.getInstance(HashAlgorithm.getInstance(), HashAlgorithm.getInstance().getHashLength() / 2, seed);
	}

	public static OutputFeedbackRandomByteSequence getInstance(HashAlgorithm hashAlgorithm, int forwardSecurityInBytes, ByteArray seed) {
		if (seed == null) {
			throw new IllegalArgumentException();
		}
		if (hashAlgorithm == null) {
			throw new IllegalArgumentException();
		}
		if (forwardSecurityInBytes < 0 || forwardSecurityInBytes > hashAlgorithm.getHashLength() - 1) {
			throw new IllegalArgumentException();
		}
		return new OutputFeedbackRandomByteSequence(hashAlgorithm, forwardSecurityInBytes, seed);
	}

}
