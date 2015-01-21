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
import ch.bfh.unicrypt.random.distributionsampler.classes.DistributionSamplerCollector;
import ch.bfh.unicrypt.random.interfaces.TrueRandomByteSequence;

/**
 * This class allows the generation of ephemeral keys. Hence it provides (backward-)security and forward-security to the
 * generated random sequences. Its security is based on the quality of the DistributionSamplerCollector and on the
 * feedback of the PseudoRandomNumberGeneratorCounterMode. The injection of new random bits into the randomization
 * process allows (backward-)security, whilst The feedback (in this case internally requesting a byte[] which is only
 * used for re-fresh of the internal state) provides forward-security.
 * <p>
 * <p>
 * @author Reto E. Koenig <reto.koenig@bfh.ch>
 */
public class HybridRandomByteSequence
	   extends OutputFeedbackRandomByteSequence
	   implements TrueRandomByteSequence {

	private static HybridRandomByteSequence DEFAULT;
	private DistributionSamplerCollector distributionSampler;
	private final int backwardSecurityInBytes;

	/**
	 *
	 * @param hashAlgorithm
	 * @param forwardSecurityInBytes
	 * @param backwardSecurityInBytes
	 */
	protected HybridRandomByteSequence(final HashAlgorithm hashAlgorithm, final int forwardSecurityInBytes, final int backwardSecurityInBytes) {
		super(hashAlgorithm, forwardSecurityInBytes, ByteArray.getInstance());

		this.backwardSecurityInBytes = backwardSecurityInBytes;
		HybridRandomByteSequence.super.setSeed(HybridRandomByteSequence.this.getDistributionSampler().getDistributionSamples(backwardSecurityInBytes));
	}

	@Override
	public int getBackwardSecurityInBytes() {
		return this.backwardSecurityInBytes;
	}

	@Override
	public void setFreshData(ByteArray byteArray) {
		if (byteArray == null) {
			throw new IllegalArgumentException();
		}
		super.update(byteArray);
	}

	@Override
	protected byte[] getNextBytes(int length) {
		return super.getNextBytes(length);
	}

	/**
	 * This will return the default HybridRandomByteSequence.
	 * <p>
	 * @return
	 */
	public static HybridRandomByteSequence getInstance() {
		if (DEFAULT == null) {
			HashAlgorithm defaultHashAlgorithm = HashAlgorithm.getInstance();
			DEFAULT = HybridRandomByteSequence.getInstance(defaultHashAlgorithm, defaultHashAlgorithm.getHashLength() / 2, defaultHashAlgorithm.getHashLength());
		}
		return DEFAULT;
	}

	public static HybridRandomByteSequence getInstance(HashAlgorithm hashAlgorithm, int forwardSecurityInBytes, int securityParameterInBytes) {
		if (hashAlgorithm == null) {
			throw new IllegalArgumentException();
		}
		if (forwardSecurityInBytes < 0 || forwardSecurityInBytes > hashAlgorithm.getHashLength() - 1) {
			throw new IllegalArgumentException();
		}
		if (securityParameterInBytes < 0) {
			throw new IllegalArgumentException();
		}
		return new HybridRandomByteSequence(hashAlgorithm, forwardSecurityInBytes, securityParameterInBytes);
	}

	@Override
	public synchronized DistributionSamplerCollector getDistributionSampler() {
		if (distributionSampler == null) {
			distributionSampler = DistributionSamplerCollector.getInstance(this);
		}
		return this.distributionSampler;
	}

}
