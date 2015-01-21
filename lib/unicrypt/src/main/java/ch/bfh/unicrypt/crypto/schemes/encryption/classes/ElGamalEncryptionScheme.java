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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.encryption.classes;

import ch.bfh.unicrypt.crypto.keygenerator.classes.DiscreteLogarithmKeyGenerator;
import ch.bfh.unicrypt.crypto.schemes.encryption.abstracts.AbstractReEncryptionScheme;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZMod;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Pair;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductSet;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.CyclicGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.ApplyFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.ApplyInverseFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.CompositeFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.GeneratorFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.RemovalFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.SelectionFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.SelfApplyFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.SharedDomainFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.interfaces.Function;

/**
 *
 * @author rolfhaenni
 */
public class ElGamalEncryptionScheme
	   extends AbstractReEncryptionScheme<CyclicGroup, Element, ProductGroup, Pair, ZMod, ZModElement, CyclicGroup, ZMod, DiscreteLogarithmKeyGenerator> {

	private final CyclicGroup cyclicGroup;
	private final Element generator;
	private Function encryptionFunctionLeft;
	private Function encryptionFunctionRight;

	protected ElGamalEncryptionScheme(CyclicGroup cyclicGroup, Element generator) {
		super(cyclicGroup, ProductSet.getInstance(cyclicGroup, 2), cyclicGroup.getZModOrder());
		this.cyclicGroup = cyclicGroup;
		this.generator = generator;
	}

	public final CyclicGroup getCyclicGroup() {
		return this.cyclicGroup;
	}

	public final Element getGenerator() {
		return this.generator;
	}

	@Override
	protected Function abstractGetEncryptionFunction() {
		ProductGroup encryptionDomain = ProductGroup.getInstance(this.getEncryptionKeySpace(), this.messageSpace, this.randomizationSpace);
		return SharedDomainFunction.getInstance(CompositeFunction.getInstance(SelectionFunction.getInstance(encryptionDomain, 2),
																			  this.getEncryptionFunctionLeft()),
												this.getEncryptionFunctionRight());
	}

	@Override
	protected Function abstractGetDecryptionFunction() {
		ProductGroup decryptionDomain = ProductGroup.getInstance(this.getDecryptionKeySpace(), this.encryptionSpace);
		return CompositeFunction.getInstance(
			   SharedDomainFunction.getInstance(SelectionFunction.getInstance(decryptionDomain, 1, 1),
												CompositeFunction.getInstance(SharedDomainFunction.getInstance(SelectionFunction.getInstance(decryptionDomain, 1, 0),
																											   SelectionFunction.getInstance(decryptionDomain, 0)),
																			  SelfApplyFunction.getInstance(this.cyclicGroup))),
			   ApplyInverseFunction.getInstance(this.cyclicGroup));
	}

	@Override
	protected DiscreteLogarithmKeyGenerator abstractGetKeyPairGenerator() {
		return DiscreteLogarithmKeyGenerator.getInstance(this.getGenerator());
	}

	public Function getEncryptionFunctionLeft() {
		if (this.encryptionFunctionLeft == null) {
			this.encryptionFunctionLeft = GeneratorFunction.getInstance(this.getGenerator());
		}
		return this.encryptionFunctionLeft;
	}

	public Function getEncryptionFunctionRight() {
		if (this.encryptionFunctionRight == null) {
			ProductGroup encryptionDomain = ProductGroup.getInstance(this.getEncryptionKeySpace(), this.messageSpace, this.randomizationSpace);
			this.encryptionFunctionRight = CompositeFunction.getInstance(
				   SharedDomainFunction.getInstance(SelectionFunction.getInstance(encryptionDomain, 1),
													CompositeFunction.getInstance(RemovalFunction.getInstance(encryptionDomain, 1),
																				  SelfApplyFunction.getInstance(this.cyclicGroup))),
				   ApplyFunction.getInstance(this.cyclicGroup));
		}
		return this.encryptionFunctionRight;
	}

	public static ElGamalEncryptionScheme getInstance(CyclicGroup cyclicGroup) {
		if (cyclicGroup == null) {
			throw new IllegalArgumentException();
		}
		return new ElGamalEncryptionScheme(cyclicGroup, cyclicGroup.getDefaultGenerator());
	}

	public static ElGamalEncryptionScheme getInstance(Element generator) {
		boolean nul=generator == null;
		boolean cyclic=generator.getSet().isCyclic();
		boolean gen=generator.isGenerator();
		
		if (generator == null || !generator.getSet().isCyclic() || !generator.isGenerator()) {
			throw new IllegalArgumentException();
		}
		return new ElGamalEncryptionScheme((CyclicGroup) generator.getSet(), generator);
	}

}
