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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.hash;

import ch.bfh.unicrypt.helper.UniCrypt;
import ch.bfh.unicrypt.helper.array.classes.ByteArray;
import ch.bfh.unicrypt.helper.converter.classes.ConvertMethod;

/**
 *
 * @author Rolf Haenni <rolf.haenni@bfh.ch>
 */
public class HashMethod
	   extends UniCrypt {

	public enum Mode {

		BYTEARRAY, BYTETREE, RECURSIVE;

	};

	private final HashAlgorithm hashAlgorithm;
	private final ConvertMethod<ByteArray> convertMethod;
	private final Mode mode;

	protected HashMethod(HashAlgorithm hashAlgorithm, ConvertMethod<ByteArray> convertMethod, Mode mode) {
		this.hashAlgorithm = hashAlgorithm;
		this.convertMethod = convertMethod;
		this.mode = mode;
	}

	public HashAlgorithm getHashAlgorithm() {
		return this.hashAlgorithm;
	}

	public ConvertMethod<ByteArray> getConvertMethod() {
		return this.convertMethod;
	}

	public Mode getMode() {
		return this.mode;
	}

	@Override
	protected String defaultToStringValue() {
		return this.hashAlgorithm.toString() + "," + this.convertMethod.toString() + "," + this.mode.toString();
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 97 * hash + this.hashAlgorithm.hashCode();
		hash = 97 * hash + this.convertMethod.hashCode();
		hash = 97 * hash + this.mode.hashCode();
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
		final HashMethod other = (HashMethod) obj;
		return this.hashAlgorithm.equals(other.hashAlgorithm) && this.convertMethod.equals(other.convertMethod) && this.mode.equals(other.mode);
	}

	public static HashMethod getInstance() {
		return HashMethod.getInstance(HashAlgorithm.getInstance(), ConvertMethod.<ByteArray>getInstance(), Mode.RECURSIVE);
	}

	public static HashMethod getInstance(HashAlgorithm hashAlgorithm) {
		return HashMethod.getInstance(hashAlgorithm, ConvertMethod.<ByteArray>getInstance(), Mode.RECURSIVE);
	}

	public static HashMethod getInstance(ConvertMethod<ByteArray> convertMethod) {
		return HashMethod.getInstance(HashAlgorithm.getInstance(), convertMethod, Mode.RECURSIVE);
	}

	public static HashMethod getInstance(Mode mode) {
		return HashMethod.getInstance(HashAlgorithm.getInstance(), ConvertMethod.<ByteArray>getInstance(), mode);
	}

	public static HashMethod getInstance(HashAlgorithm hashAlgorithm, ConvertMethod<ByteArray> convertMethod) {
		return HashMethod.getInstance(hashAlgorithm, convertMethod, Mode.RECURSIVE);
	}

	public static HashMethod getInstance(ConvertMethod<ByteArray> convertMethod, Mode mode) {
		return HashMethod.getInstance(HashAlgorithm.getInstance(), convertMethod, mode);
	}

	public static HashMethod getInstance(HashAlgorithm hashAlgorithm, Mode mode) {
		return HashMethod.getInstance(hashAlgorithm, ConvertMethod.getInstance(), mode);
	}

	public static HashMethod getInstance(HashAlgorithm hashAlgorithm, ConvertMethod<ByteArray> convertMethod, Mode mode) {
		if (hashAlgorithm == null || convertMethod == null || mode == null) {
			throw new IllegalArgumentException();
		}
		return new HashMethod(hashAlgorithm, convertMethod, mode);
	}

}
