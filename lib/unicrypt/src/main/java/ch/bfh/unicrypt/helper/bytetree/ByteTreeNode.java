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
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.array.classes.DenseArray;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.hash.HashAlgorithm;
import java.nio.ByteBuffer;
import java.util.Iterator;

/**
 *
 * @author Reto E. Koenig <reto.koenig@bfh.ch>
 */
public class ByteTreeNode
	   extends ByteTree
	   implements Iterable<ByteTree> {

	public static final byte IDENTIFIER = 0;

	private final DenseArray<ByteTree> byteTrees;

	protected ByteTreeNode(DenseArray<ByteTree> byteTrees) {
		this.byteTrees = byteTrees;
		this.length = LENGTH_OF_PREAMBLE;
		for (ByteTree byteTree : byteTrees) {
			this.length += byteTree.length;
		}
	}

	public DenseArray<ByteTree> getByteTrees() {
		return byteTrees;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 79 * hash + this.byteTrees.hashCode();
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
		final ByteTreeNode other = (ByteTreeNode) obj;
		return this.byteTrees.equals(other.byteTrees);
	}

	@Override
	protected String defaultToStringValue() {
		return this.byteTrees.toString();
	}

	@Override
	protected ByteArray abstractGetRecursiveHashValue(HashAlgorithm hashAlgorithm) {
		int amount = this.byteTrees.getLength();
		ByteArray[] hashValues = new ByteArray[amount];
		int i = 0;
		for (ByteTree byteTree : this.byteTrees) {
			hashValues[i++] = byteTree.getRecursiveHashValue(hashAlgorithm);
		}
		return ByteArray.getInstance(hashValues).getHashValue(hashAlgorithm);
	}

	@Override
	protected void abstractConstructByteArray(ByteBuffer buffer, ByteArray byteArray) {
		buffer.put(IDENTIFIER);
		buffer.putInt(this.byteTrees.getLength());
		for (ByteTree byteTree : this) {
			byteTree.constructByteArray(buffer, byteArray);
		}
	}

	@Override
	public Iterator<ByteTree> iterator() {
		return this.byteTrees.iterator();
	}

}
