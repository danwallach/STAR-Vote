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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.helper;

/**
 *
 * @author Rolf Haenni <rolf.haenni@bfh.ch
 */
public class Alphabet
	   extends UniCrypt {

	public static final Alphabet UNARY = Alphabet.getInstance('1', '1');
	public static final Alphabet BINARY = Alphabet.getInstance('0', '1');
	public static final Alphabet OCTAL = Alphabet.getInstance('0', '7');
	public static final Alphabet DECIMAL = Alphabet.getInstance('0', '9');
	public static final Alphabet HEXADECIMAL = Alphabet.getInstance("0123456789ABCDEF", "[0-9A-F]");
	public static final Alphabet LOWER_CASE = Alphabet.getInstance('a', 'z');
	public static final Alphabet UPPER_CASE = Alphabet.getInstance('A', 'Z');
	public static final Alphabet LETTERS = Alphabet.getInstance("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz", "[A-Za-z]");
	public static final Alphabet ALPHANUMERIC = Alphabet.getInstance("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789", "[A-Za-z0-9]");
	public static final Alphabet BASE64 = Alphabet.getInstance("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789\\+\\/", "[A-Za-z0-9\\+\\/]");
	public static final Alphabet PRINTABLE_ASCII = Alphabet.getInstance('\u0020', '\u007E');
	public static final Alphabet UNICODE_BMP = Alphabet.getInstance('\u0000', '\uFFFF');

	private final String characters;
	private final String regExp;
	private final char minChar;
	private final char maxChar;

	protected Alphabet(String characters) {
		this.characters = characters;
		this.regExp = null;
		this.minChar = '\u0000';
		this.maxChar = '\u0000';
	}

	protected Alphabet(String characters, String regExp) {
		this.characters = characters;
		this.regExp = "^(" + regExp + ")*$";
		this.minChar = '\u0000';
		this.maxChar = '\u0000';
	}

	protected Alphabet(char minChar, char maxChar) {
		this.characters = null;
		this.regExp = null;
		this.minChar = minChar;
		this.maxChar = maxChar;
	}

	public int getSize() {
		if (this.characters == null) {
			return (int) this.maxChar - (int) this.minChar + 1;
		}
		return this.characters.length();
	}

	public char getCharacter(int i) {
		if (i < 0 || i >= this.getSize()) {
			throw new IndexOutOfBoundsException();
		}
		if (this.characters == null) {
			return (char) (this.minChar + i);
		}
		return this.characters.charAt(i);
	}

	public boolean contains(char c) {
		if (this.characters == null) {
			return c >= this.minChar && c <= this.maxChar;
		}
		return this.characters.lastIndexOf(c) >= 0;
	}

	public int getIndex(char c) {
		if (!this.contains(c)) {
			throw new IllegalArgumentException();
		}
		if (this.characters == null) {
			return (int) c - (int) this.minChar;
		}
		return this.characters.lastIndexOf(c);
	}

	public boolean isValid(String string) {
		if (this.regExp == null) {
			for (int i = 0; i < string.length(); i++) {
				if (!this.contains(string.charAt(i))) {
					return false;
				}
			}
			return true;
		} else {
			return string.matches("^(" + this.regExp + ")*$");
		}
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 41 * hash + (this.characters != null ? this.characters.hashCode() : 0);
		hash = 41 * hash + this.minChar;
		hash = 41 * hash + this.maxChar;
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
		final Alphabet other = (Alphabet) obj;
		if ((this.characters == null) ? (other.characters != null) : !this.characters.equals(other.characters)) {
			return false;
		}
		if (this.minChar != other.minChar) {
			return false;
		}
		return this.maxChar == other.maxChar;
	}

	public static Alphabet getInstance(String characters) {
		return new Alphabet(characters);
	}

	public static Alphabet getInstance(String characters, String regExp) {
		if (characters == null) {
			throw new IllegalArgumentException();
		}
		return new Alphabet(characters, regExp);
	}

	public static Alphabet getInstance(char minChar, char maxChar) {
		if (minChar > maxChar) {
			throw new IllegalArgumentException();
		}
		return new Alphabet(minChar, maxChar);
	}

	@Override
	protected String defaultToStringValue() {
		if (this.characters == null) {
			return this.minChar + "..." + this.maxChar;
		}
		return this.characters;
	}

}
