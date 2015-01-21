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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.converter.classes.string;

import lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper.array.classes.ByteArray;
import ch.bfh.unicrypt.helper.converter.abstracts.AbstractStringConverter;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author Rolf Haenni <rolf.haenni@bfh.ch>
 */
public class ByteArrayToString
	   extends AbstractStringConverter<ByteArray> {

	public enum Radix {

		BINARY, HEX, BASE64

	};

	private final Radix radix;
	private final boolean reverse;
	private final String delimiter;
	private final String binaryRegExp;
	private final String hexRegExp;

	protected ByteArrayToString(Radix radix, boolean reverse) {
		super(ByteArray.class);
		this.radix = radix;
		this.reverse = reverse;
		this.delimiter = "";
		this.binaryRegExp = "^([0-1]{8}([0-1]{8})*)?$";
		this.hexRegExp = "^([0-9A-F]{2}([0-9A-F]{2})*)?$";
	}

	protected ByteArrayToString(Radix radix, boolean reverse, String delimiter) {
		super(ByteArray.class);
		this.radix = radix;
		this.reverse = reverse;
		this.delimiter = delimiter;
		this.binaryRegExp = "^([0-1]{8}(\\|[0-1]{8})*)?$";
		this.hexRegExp = "^([0-9A-F]{2}(\\|[0-9A-F]{2})*)?$";
	}

	@Override
	public String abstractConvert(ByteArray byteArray) {
		switch (this.radix) {
			case BINARY: {
				StringBuilder sb = new StringBuilder(byteArray.getLength() * (Byte.SIZE + 1));
				String delim = "";
				for (Byte b : this.reverse ? byteArray.reverse() : byteArray) {
					int i = b & 0xFF;
					sb.append(delim);
					sb.append(String.format("%8s", Integer.toBinaryString(i)).replace(' ', '0'));
					delim = this.delimiter;
				}
				return sb.toString();
			}
			case HEX: {
				StringBuilder sb = new StringBuilder(byteArray.getLength() * 3);
				String delim = "";
				for (Byte b : this.reverse ? byteArray.reverse() : byteArray) {
					sb.append(delim);
					sb.append(String.format("%02X", b));
					delim = this.delimiter;
				}
				return sb.toString();
			}
			case BASE64:
				return DatatypeConverter.printBase64Binary(byteArray.getBytes());
			default:
				// impossible case
				throw new IllegalStateException();
		}
	}

	@Override
	public ByteArray abstractReconvert(String string) {
		if (this.delimiter.length() > 0) {
			string = string.replace(this.delimiter.charAt(0), '|');
		}
		switch (this.radix) {
			case BINARY: {
				if (!string.matches(this.binaryRegExp)) {
					throw new IllegalArgumentException();
				}
				int subLength = 8 + this.delimiter.length();
				byte[] bytes = new byte[(string.length() + this.delimiter.length()) / subLength];
				for (int i = 0; i < bytes.length; i++) {
					bytes[i] = (byte) Integer.parseInt(string.substring(i * subLength, i * subLength + 8), 2);
				}
				ByteArray result = ByteArray.getInstance(bytes);
				return this.reverse ? result.reverse() : result;
			}
			case HEX: {
				string = string.toUpperCase();
				if (!string.matches(this.hexRegExp)) {
					throw new IllegalArgumentException();
				}
				int subLength = 2 + this.delimiter.length();
				byte[] bytes = new byte[(string.length() + this.delimiter.length()) / subLength];
				for (int i = 0; i < bytes.length; i++) {
					bytes[i] = (byte) Integer.parseInt(string.substring(i * subLength, i * subLength + 2), 16);
				}
				ByteArray result = ByteArray.getInstance(bytes);
				return this.reverse ? result.reverse() : result;
			}
			case BASE64:
				return ByteArray.getInstance(DatatypeConverter.parseBase64Binary(string));
			default:
				// impossible case
				throw new IllegalStateException();
		}
	}

	public static ByteArrayToString getInstance() {
		return ByteArrayToString.getInstance(ByteArrayToString.Radix.HEX, false);
	}

	public static ByteArrayToString getInstance(Radix radix) {
		return ByteArrayToString.getInstance(radix, false);
	}

	public static ByteArrayToString getInstance(boolean reverse) {
		return ByteArrayToString.getInstance(ByteArrayToString.Radix.HEX, reverse);
	}

	public static ByteArrayToString getInstance(Radix radix, boolean reverse) {
		if (radix == null || (radix == Radix.BASE64 && reverse)) {
			throw new IllegalArgumentException();
		}
		return new ByteArrayToString(radix, reverse);
	}

	public static ByteArrayToString getInstance(String delimiter) {
		return ByteArrayToString.getInstance(Radix.HEX, delimiter, false);
	}

	public static ByteArrayToString getInstance(String delimiter, boolean reverse) {
		return ByteArrayToString.getInstance(Radix.HEX, delimiter, reverse);
	}

	public static ByteArrayToString getInstance(Radix radix, String delimiter) {
		return ByteArrayToString.getInstance(radix, delimiter, false);
	}

	public static ByteArrayToString getInstance(Radix radix, String delimiter, boolean reverse) {
		if (radix == null || radix == Radix.BASE64 || delimiter == null || delimiter.length() != 1
			   || (radix == Radix.BINARY && "01".indexOf(delimiter.charAt(0)) >= 0)
			   || (radix == Radix.HEX && "0123456789ABCDEFabcdef".indexOf(delimiter.charAt(0)) >= 0)) {
			throw new IllegalArgumentException();
		}
		return new ByteArrayToString(radix, reverse, delimiter);
	}

}
