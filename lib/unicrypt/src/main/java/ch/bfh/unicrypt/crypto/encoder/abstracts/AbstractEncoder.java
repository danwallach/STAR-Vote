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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.encoder.abstracts;

import ch.bfh.unicrypt.crypto.encoder.interfaces.Encoder;
import ch.bfh.unicrypt.helper.UniCrypt;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.interfaces.Function;

public abstract class AbstractEncoder<D extends Set, DE extends Element, C extends Set, CE extends Element>
	   extends UniCrypt
	   implements Encoder {

	private Function encodingFunction;
	private Function decodingFunction;

	@Override
	public Function getEncodingFunction() {
		if (this.encodingFunction == null) {
			this.encodingFunction = this.abstractGetEncodingFunction();
		}
		return this.encodingFunction;
	}

	@Override
	public Function getDecodingFunction() {
		if (this.decodingFunction == null) {
			this.decodingFunction = this.abstractGetDecodingFunction();
		}
		return this.decodingFunction;
	}

	@Override
	public CE encode(final Element element) {
		return (CE) this.getEncodingFunction().apply(element);
	}

	@Override
	public DE decode(final Element element) {
		return (DE) this.getDecodingFunction().apply(element);
	}

	@Override
	public D getDomain() {
		return (D) this.getEncodingFunction().getDomain();
	}

	@Override
	public C getCoDomain() {
		return (C) this.getEncodingFunction().getCoDomain();
	}

	@Override
	protected String defaultToStringValue() {
		return this.getDomain() + " <=> " + this.getCoDomain();
	}

	protected abstract Function abstractGetEncodingFunction();

	protected abstract Function abstractGetDecodingFunction();

}
