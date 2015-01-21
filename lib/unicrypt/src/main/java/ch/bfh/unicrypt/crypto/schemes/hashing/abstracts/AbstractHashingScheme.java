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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.hashing.abstracts;

import ch.bfh.unicrypt.crypto.schemes.hashing.interfaces.HashingScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.scheme.abstracts.AbstractScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.BooleanElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductSet;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.CompositeFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.EqualityFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.SelectionFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.SharedDomainFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.interfaces.Function;

public abstract class AbstractHashingScheme<MS extends Set, ME extends Element, HS extends Set, HE extends Element>
	   extends AbstractScheme<MS>
	   implements HashingScheme {

	protected final HS hashSpace;
	protected Function hashFunction;
	protected Function checkFunction;

	public AbstractHashingScheme(MS messageSpace, HS hashSpace) {
		super(messageSpace);
		this.hashSpace = hashSpace;
	}

	@Override
	public final HS getHashSpace() {
		return this.hashSpace;
	}

	@Override
	public final Function getHashFunction() {
		if (this.hashFunction == null) {
			this.hashFunction = this.abstractGetHashFunction();
		}
		return this.hashFunction;
	}

	@Override
	public final Function getCheckFunction() {
		if (this.checkFunction == null) {
			ProductSet checkDomain = ProductSet.getInstance(this.getMessageSpace(), this.getHashSpace());
			this.checkFunction = CompositeFunction.getInstance(
				   SharedDomainFunction.getInstance(
						  CompositeFunction.getInstance(
								 SelectionFunction.getInstance(checkDomain, 0),
								 this.getHashFunction()),
						  SelectionFunction.getInstance(checkDomain, 1)),
				   EqualityFunction.getInstance(this.getHashSpace()));
		}
		return this.checkFunction;
	}

	@Override
	public final HE hash(Element message) {
		return (HE) this.getHashFunction().apply(message);
	}

	@Override
	public final BooleanElement check(Element message, Element hashValue) {
		return (BooleanElement) this.getCheckFunction().apply(message, hashValue);
	}

	protected abstract Function abstractGetHashFunction();

}
