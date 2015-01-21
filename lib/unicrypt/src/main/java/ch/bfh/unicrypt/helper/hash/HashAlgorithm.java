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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author Rolf Haenni <rolf.haenni@bfh.ch>
 */
public class HashAlgorithm
	   extends UniCrypt {

//	public static HashAlgorithm MD2 = new HashAlgorithm("MD2"); Not supported by Android (OpenSSL implementation)
	public static HashAlgorithm MD5 = new HashAlgorithm("MD5");
	public static HashAlgorithm SHA1 = new HashAlgorithm("SHA-1");
	public static HashAlgorithm SHA256 = new HashAlgorithm("SHA-256");
	public static HashAlgorithm SHA384 = new HashAlgorithm("SHA-384");
	public static HashAlgorithm SHA512 = new HashAlgorithm("SHA-512");

	private final MessageDigest messageDigest;

	private HashAlgorithm(String algorithmName) {
		try {
			this.messageDigest = MessageDigest.getInstance(algorithmName);
		} catch (final NoSuchAlgorithmException e) {
			throw new IllegalArgumentException();
		}
	}

	public byte[] getHashValue(byte[] bytes) {
		if (bytes == null) {
			throw new IllegalArgumentException();
		}
		return this.messageDigest.digest(bytes);
	}

	public byte[] getHashValue(byte[] bytes, int offset, int length) {
		if (bytes == null || offset < 0 || offset + length > bytes.length) {
			throw new IllegalArgumentException();
		}
		this.messageDigest.update(bytes, offset, length);
		return this.messageDigest.digest();
	}

	// length of hash values in bytes
	public int getHashLength() {
		return this.messageDigest.getDigestLength();
	}

	public static HashAlgorithm getInstance() {
		return HashAlgorithm.SHA256;
	}

}
