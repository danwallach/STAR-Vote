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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.converter.classes.biginteger;

import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.array.classes.ByteArray;
import ch.bfh.unicrypt.helper.converter.abstracts.AbstractBigIntegerConverter;
import ch.bfh.unicrypt.helper.MathUtil;
import java.math.BigInteger;
import java.util.LinkedList;

/**
 *
 * @author Rolf Haenni <rolf.haenni@bfh.ch>
 */
public class ByteArrayToBigInteger
	   extends AbstractBigIntegerConverter<ByteArray> {

	private final int blockLength;

	protected ByteArrayToBigInteger(int blockLength) {
		super(ByteArray.class);
		this.blockLength = blockLength;
	}

	@Override
	public BigInteger abstractConvert(ByteArray value) {
		// For blocklLength=1, there is 1 bytearray of length 0, 256 of length 1,
		// 65536 of length 2, etc. Therefore:
		//   lenght=0 -> 0
		//   length=1 -> 1,...,256
		//   length=2 -> 257,...,65792
		// etc.
		BigInteger result = BigInteger.ZERO;
		if (value.getLength() > 0) {
			byte[] bytes = new byte[value.getLength()];
			int amount = bytes.length / this.blockLength;
			for (int i = 0; i < amount; i++) {
				bytes[(bytes.length - 1) - (i * this.blockLength)] = 1;
			}
			result = new BigInteger(1, bytes);
		}
		return result.add(new BigInteger(1, value.getBytes()));
	}

	@Override
	public ByteArray abstractReconvert(BigInteger value) {
		LinkedList<Byte> byteList = new LinkedList<Byte>();
		BigInteger byteSize = MathUtil.powerOfTwo(Byte.SIZE);
		BigInteger blockSize = MathUtil.powerOfTwo(Byte.SIZE * this.blockLength);
		while (!value.equals(BigInteger.ZERO)) {
			value = value.subtract(BigInteger.ONE);
			BigInteger remainder = value.mod(blockSize);
			for (int i = 0; i < this.blockLength; i++) {
				byteList.addFirst(remainder.mod(byteSize).byteValue());
				remainder = remainder.divide(byteSize);
			}
			value = value.divide(blockSize);
		}
		return ByteArray.getInstance(byteList);
	}

	public static ByteArrayToBigInteger getInstance() {
		return new ByteArrayToBigInteger(1);
	}

	public static ByteArrayToBigInteger getInstance(int blockLength) {
		return new ByteArrayToBigInteger(blockLength);
	}

}
