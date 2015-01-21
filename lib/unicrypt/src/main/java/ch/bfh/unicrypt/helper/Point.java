/*
 * UniCrypt
 *
 *  UniCrypt(tm) : Cryptographical framework allowing the implementation of cryptographic protocols e.g. e-voting
 *  Copyright (C) Error: on line 7, column 34 in file:///Users/rolfhaenni/GIT/unicrypt/license-dualLicense.txt
 The string doesn't match the expected date/time format. The string to parse was: "17-Jan-2014". The expected format was: "MMM d, yyyy". Bern University of Applied Sciences (BFH), Research Institute for
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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper;

/**
 *
 * @author Rolf Haenni <rolf.haenni@bfh.ch>
 * @param <T>
 */
public class Point<T>
	   extends UniCrypt {

	private final T x, y;

	protected Point(T x, T y) {
		this.x = x;
		this.y = y;
	}

	public T getX() {
		return x;
	}

	public T getY() {
		return y;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 59 * hash + (this.x != null ? this.x.hashCode() : 0);
		hash = 59 * hash + (this.y != null ? this.y.hashCode() : 0);
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
		final Point<?> other = (Point<?>) obj;
		if (this.x != other.x && (this.x == null || !this.x.equals(other.x))) {
			return false;
		}
		if (this.y != other.y && (this.y == null || !this.y.equals(other.y))) {
			return false;
		}
		return true;
	}

	public static <T> Point<T> getInstance() {
		return new Point<T>((T) null, (T) null);
	}

	public static <T> Point<T> getInstance(T x, T y) {
		if (x == null || y == null) {
			throw new IllegalArgumentException();
		}
		return new Point<T>(x, y);
	}

}
