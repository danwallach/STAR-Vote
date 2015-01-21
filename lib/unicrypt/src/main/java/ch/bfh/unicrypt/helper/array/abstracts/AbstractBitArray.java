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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.array.abstracts;

import ch.bfh.unicrypt.helper.array.interfaces.ImmutableArray;

/**
 *
 * @author Rolf Haenni <rolf.haenni@bfh.ch>
 * @param <A>
 * @param <V>
 */
//abstract public class AbstractBitArray<A extends ImmutableArray<V>, V extends Object>
public class AbstractBitArray<A extends AbstractBitArray<A, V>, V extends Object>
	   extends AbstractDefaultValueArray<A, V> {

	protected final long[] bits;
	protected final int blockSize;
	protected final int blockAmount;
	protected final int[] shifts;
	protected final long[] masks;

	protected AbstractBitArray(long[] bits, int blockSize, Class valueClass, V defaultValue, int trailer, int header, int length, int offset, boolean reverse) {
		super(valueClass, defaultValue, trailer, header, length, offset, reverse);
		this.bits = bits;
		this.blockSize = blockSize;
		this.blockAmount = Long.SIZE / blockSize;
		this.shifts = new int[this.blockAmount];
		this.masks = new long[this.blockAmount];
		for (int i = 0; i < this.blockAmount; i++) {
			this.shifts[i] = i * blockSize;
			this.masks[i] = ((1L << blockSize) - 1) << this.shifts[i];
		}
	}

	@Override
	protected A abstractGetInstance(int offset, int length, int trailer, int header, boolean reverse) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	protected V abstractGetAt(int index) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	protected A abstractInsertAt(int index, V value) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	protected A abstractReplaceAt(int index, V value) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	protected A abstractAppend(ImmutableArray<V> other) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	// TESTING
	public void print() {
		for (int i = 0; i < this.blockAmount; i++) {
			System.out.println(this.shifts[i]);
		}
		for (int i = 0; i < this.blockAmount; i++) {
			System.out.println(this.masks[i]);
		}
	}

	// TESTING
	public static AbstractBitArray getInstance(int width) {
		return new AbstractBitArray(null, width, null, null, 0, 0, 0, 0, true);
	}

	// TESTING
	public static void main(String[] args) {
		AbstractBitArray a = AbstractBitArray.getInstance(1);
		a.print();

	}

}
