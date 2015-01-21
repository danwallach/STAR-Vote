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

import ch.bfh.unicrypt.helper.UniCrypt;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.array.classes.ByteArray;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.array.classes.DenseArray;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.hash.HashAlgorithm;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Iterator;

/**
 * This class represents the ByteTree described in Wikstroms Verifier.
 * http://www.csc.kth.se/utbildning/kth/kurser/SA104X/fkand13/vmnv-1.1.0.pdf
 * <p>
 * This implementation aims for a possible interchange of Wikstroms library.
 * <p>
 * The following description of the ByteTree is mainly adopted from the referenced document:
 * <p>
 * "We use a byte-oriented format to represent objects on file and to turn them into arrays of bytes. The goal of this
 * format is to be as simple as possible."
 * <p>
 * A byte tree is either a leaf containing an array of bytes, or a node containing other byte trees.
 * <p>
 * We use a 8k-bit two’s-complement representation of n in big endian byte order.
 * <p>
 * A byte tree is represented by an array of bytes as follows: • Leaf: Concatenation of 1 byte 01 indicating the leaf 4
 * bytes indicating the number of data bytes
 * <p>
 * • Node: Concatenation of 1 byte 00 indicating the node 4 bytes bytes indicating the number of children (either Node /
 * Leaf)
 * <p>
 * Example: node(node(leaf(1), leaf(23)), leaf(45)) is represented as
 * <p>
 * [0, 0, 0, 0, 2, 0, 0, 0, 0, 2, 1, 0, 0, 0, 1, 1, 1, 0, 0, 0, 1, 23, 1, 0, 0, 0, 1, 45]
 * <p>
 * node1........(..node2.......(..leaf1.............leaf2.............)leaf3............)
 * <p>
 * Even though NIO is in use {@link ByteBuffer} this class does not (yet) work with direct buffers, as
 * allocation/deallocation handling costs more than indirectly used (See API)
 * <p>
 * @author Reto E. Koenig <reto.koenig@bfh.ch>
 */
public abstract class ByteTree
	   extends UniCrypt {

	public final static int LENGTH_OF_INTEGER = Integer.SIZE / Byte.SIZE;
	public final static int LENGTH_OF_PREAMBLE = LENGTH_OF_INTEGER + 1;

	protected ByteArray byteArray;
	protected int length;

	/**
	 * Calculates the serialized version of a ByteTree.
	 * <p>
	 * @return Serialized ByteTree
	 */
	public final ByteArray getByteArray() {
		if (this.byteArray == null) {
			ByteBuffer buffer = ByteBuffer.allocate(this.length);
			ByteArray byteArray = new SafeByteArray(buffer.array());
			this.constructByteArray(buffer, byteArray);
		}
		return this.byteArray;
	}

	public final ByteArray getHashValue() {
		return this.getHashValue(HashAlgorithm.getInstance());
	}

	public final ByteArray getHashValue(HashAlgorithm hashAlgorithm) {
		if (hashAlgorithm == null) {
			throw new IllegalArgumentException();
		}
		return this.getByteArray().getHashValue(hashAlgorithm);
	}

	public final ByteArray getRecursiveHashValue() {
		return this.getRecursiveHashValue(HashAlgorithm.getInstance());
	}

	public final ByteArray getRecursiveHashValue(HashAlgorithm hashAlgorithm) {
		if (hashAlgorithm == null) {
			throw new IllegalArgumentException();
		}
		return this.abstractGetRecursiveHashValue(hashAlgorithm);
	}

	protected abstract ByteArray abstractGetRecursiveHashValue(HashAlgorithm hashAlgorithm);

	// fills up the buffer and (if necessary) sets the byte arrays recursively
	protected final void constructByteArray(ByteBuffer buffer, ByteArray byteArray) {
		if (this.byteArray == null) {
			int offset = buffer.position();
			this.abstractConstructByteArray(buffer, byteArray);
			this.byteArray = byteArray.extract(offset, this.length);
		} else {
			for (byte b : this.byteArray) {
				buffer.put(b);
			}
		}
	}

	protected abstract void abstractConstructByteArray(ByteBuffer buffer, ByteArray byteArray);

	public final int getLength() {
		return this.length;
	}

	public final boolean isLeaf() {
		return this instanceof ByteTreeLeaf;
	}

	public final static ByteTreeLeaf getInstance(ByteArray byteArray) {
		if (byteArray == null) {
			throw new IllegalArgumentException();
		}
		return new ByteTreeLeaf(byteArray);
	}

	@Override
	protected String defaultToStringName() {
		return ByteTree.class.getSimpleName();
	}

	/**
	 * Returns a new instance of ByteTree given one or more ByteTree instances.
	 * <p>
	 * @param byteTrees The ByteTree instances that will be connected as children to the new instance of ByteTree
	 * @return new instance of a ByteTree with at least one node-element and one leaf-element.
	 */
	public final static ByteTree getInstance(ByteTree... byteTrees) {
		return new ByteTreeNode(DenseArray.getInstance(byteTrees));
	}

	/**
	 * Returns the ByteTree representation of a given ByteArray.
	 * <p>
	 * @param byteArray the serialized ByteTree
	 * @return ByteTree representation
	 */
	public final static ByteTree getInstanceFrom(ByteArray byteArray) {
		if (byteArray == null) {
			throw new IllegalArgumentException();
		}
		Iterator<ByteArray> iterator = ByteTree.getByteArrayIterator(byteArray);
		ByteTree result = ByteTree.getInstanceFrom(iterator);
		if (iterator.hasNext()) {
			throw new IllegalArgumentException();
		}
		return result;
	}

	private static ByteTree getInstanceFrom(Iterator<ByteArray> iterator) {
		ByteArray byteArray = iterator.next();
		if (ByteTree.extractIdentifier(byteArray) == ByteTreeLeaf.IDENTIFIER) {
			return new ByteTreeLeaf(ByteTree.extractBinaryData(byteArray), byteArray);
		} else {
			int amount = ByteTree.extractAmount(byteArray);
			ByteTree[] byteTrees = new ByteTree[amount];
			for (int i = 0; i < amount; i++) {
				byteTrees[i] = ByteTree.getInstanceFrom(iterator);
			}
			return new ByteTreeNode(DenseArray.getInstance(byteTrees));
		}
	}

	private static byte extractIdentifier(ByteArray byteArray) {
		return byteArray.getByteAt(0);
	}

	private static int extractAmount(ByteArray byteArray) {
		return new BigInteger(1, byteArray.extract(1, LENGTH_OF_INTEGER).getBytes()).intValue();
	}

	private static ByteArray extractBinaryData(ByteArray byteArray) {
		return byteArray.extractSuffix(byteArray.getLength() - LENGTH_OF_PREAMBLE);
	}

	private static Iterator<ByteArray> getByteArrayIterator(final ByteArray byteArray) {
		return new Iterator<ByteArray>() {

			ByteArray currentByteArray = byteArray;

			@Override
			public boolean hasNext() {
				return this.currentByteArray.getLength() > 0;
			}

			@Override
			public ByteArray next() {
				if (this.currentByteArray.getLength() < LENGTH_OF_PREAMBLE) {
					throw new IllegalStateException();
				}
				int nextLength;
				byte identifier = ByteTree.extractIdentifier(this.currentByteArray);
				if (identifier == ByteTreeLeaf.IDENTIFIER) {
					nextLength = LENGTH_OF_PREAMBLE + ByteTree.extractAmount(this.currentByteArray);
				} else if (identifier == ByteTreeNode.IDENTIFIER) {
					nextLength = LENGTH_OF_PREAMBLE;
				} else {
					throw new IllegalArgumentException();
				}
				ByteArray[] splitByteArrays = this.currentByteArray.split(nextLength);
				this.currentByteArray = splitByteArrays[1];
				return splitByteArrays[0];
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

		};
	}

}

// this local class allows creating instances of ByteArray without copying the array
class SafeByteArray
	   extends ByteArray {

	protected SafeByteArray(byte[] bytes) {
		super(bytes);
	}

}
