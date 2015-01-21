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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.bytetree;

import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.array.classes.ByteArray;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.hash.HashAlgorithm;
import java.nio.ByteBuffer;

/**
 *
 * @author Reto E. Koenig <reto.koenig@bfh.ch>
 */
public class ByteTreeLeaf
	   extends ByteTree {

	public static final byte IDENTIFIER = 1;

	private final ByteArray value;

	protected ByteTreeLeaf(ByteArray value) {
		this.value = value;
		this.length = LENGTH_OF_PREAMBLE + value.getLength();
	}

	protected ByteTreeLeaf(ByteArray value, ByteArray byteArray) {
		this(value);
		this.byteArray = byteArray;
	}

	public ByteArray getValue() {
		return this.value;
	}

	@Override
	protected String defaultToStringValue() {
		return this.value.toString();
	}

	@Override
	protected ByteArray abstractGetRecursiveHashValue(HashAlgorithm hashAlgorithm) {
		return this.value.getHashValue(hashAlgorithm);
	}

	@Override
	protected void abstractConstructByteArray(ByteBuffer buffer, ByteArray byteArray) {
		buffer.put(IDENTIFIER);
		buffer.putInt(this.value.getLength());
		buffer.put(this.value.getBytes());
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 17 * hash + this.value.hashCode();
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
		final ByteTreeLeaf other = (ByteTreeLeaf) obj;
		return this.value.equals(other.value);
	}

}
