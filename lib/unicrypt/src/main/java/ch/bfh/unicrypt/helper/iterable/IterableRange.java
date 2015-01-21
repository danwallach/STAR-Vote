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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.iterable;

import ch.bfh.unicrypt.helper.UniCrypt;
import java.util.Iterator;

/**
 *
 * @author Rolf Haenni <rolf.haenni@bfh.ch>
 * @param <T>
 */
public class IterableRange
	   extends UniCrypt
	   implements Iterable<Integer> {

	private final int from;
	private final int to;

	private IterableRange(int from, int to) {
		this.from = from;
		this.to = to;
	}

	@Override
	public Iterator<Integer> iterator() {
		return new Iterator<Integer>() {
			private int pos = from;

			@Override
			public boolean hasNext() {
				return pos <= to;
			}

			@Override
			public Integer next() {
				return pos++;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	protected String defaultToStringValue() {
		return this.from + "..." + this.to;
	}

	public static IterableRange getInstance(int from, int to) {
		if (to < from - 1) {
			throw new IllegalArgumentException();
		}
		return new IterableRange(from, to);
	}

}
