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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.schemes.signature.classes;

import ch.bfh.unicrypt.crypto.keygenerator.classes.DiscreteLogarithmKeyGenerator;
import ch.bfh.unicrypt.crypto.schemes.signature.abstracts.AbstractRandomizedSignatureScheme;
import ch.bfh.unicrypt.helper.array.classes.ByteArray;
import ch.bfh.unicrypt.helper.converter.classes.biginteger.FiniteByteArrayToBigInteger;
import ch.bfh.unicrypt.helper.converter.classes.bytearray.StringToByteArray;
import ch.bfh.unicrypt.helper.converter.interfaces.BigIntegerConverter;
import ch.bfh.unicrypt.helper.hash.HashMethod;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.N;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZMod;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Pair;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductSet;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.CyclicGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.AdapterFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.AdditionFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.ApplyFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.CompositeFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.ConvertFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.EqualityFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.GeneratorFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.HashFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.IdentityFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.InvertFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.ModuloFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.ProductFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.SelectionFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.SelfApplyFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.SharedDomainFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.TimesFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.interfaces.Function;

public class SchnorrSignatureScheme<MS extends Set>
	   extends AbstractRandomizedSignatureScheme<MS, Element, ProductGroup, Pair, ZMod, ZMod, CyclicGroup, DiscreteLogarithmKeyGenerator> {

	private final CyclicGroup cyclicGroup;
	private final Element generator;

	protected SchnorrSignatureScheme(MS messageSpace, CyclicGroup cyclicGroup, Element generator, HashMethod hashMethod) {
		super(messageSpace, ProductSet.getInstance(cyclicGroup.getZModOrder(), 2), cyclicGroup.getZModOrder(), hashMethod);
		this.cyclicGroup = cyclicGroup;
		this.generator = generator;
	}

	@Override
	protected DiscreteLogarithmKeyGenerator abstractGetKeyPairGenerator(StringToByteArray converter) {
		return DiscreteLogarithmKeyGenerator.getInstance(this.generator, converter);
	}

	public final CyclicGroup getCyclicGroup() {
		return this.cyclicGroup;
	}

	public final Element getGenerator() {
		return this.generator;
	}

	@Override
	protected Function abstractGetSignatureFunction() {
		ZMod zMod = this.cyclicGroup.getZModOrder();
		ProductSet inputSpace = ProductSet.getInstance(this.getSignatureKeySpace(), this.messageSpace, this.randomizationSpace);
		ProductSet middleSpace = ProductSet.getInstance(zMod, 3);

		return CompositeFunction.getInstance(
			   SharedDomainFunction.getInstance(
					  SelectionFunction.getInstance(inputSpace, 0),
					  CompositeFunction.getInstance(
							 AdapterFunction.getInstance(inputSpace, 1, 2),
							 ProductFunction.getInstance(
									IdentityFunction.getInstance(this.messageSpace),
									GeneratorFunction.getInstance(this.generator)),
							 this.getHashConvertModuloFunction(zMod)),
					  SelectionFunction.getInstance(inputSpace, 2)),
			   SharedDomainFunction.getInstance(
					  SelectionFunction.getInstance(middleSpace, 1),
					  CompositeFunction.getInstance(
							 SharedDomainFunction.getInstance(
									CompositeFunction.getInstance(
										   AdapterFunction.getInstance(middleSpace, 0, 1),
										   TimesFunction.getInstance(zMod, zMod)),
									SelectionFunction.getInstance(middleSpace, 2)),
							 AdditionFunction.getInstance(zMod))));
	}

	@Override
	protected Function abstractGetVerificationFunction() {
		ZMod zMod = this.cyclicGroup.getZModOrder();
		ProductSet inputSpace = ProductSet.getInstance(this.getVerificationKeySpace(), this.messageSpace, this.signatureSpace);

		return CompositeFunction.getInstance(
			   SharedDomainFunction.getInstance(
					  CompositeFunction.getInstance(
							 SharedDomainFunction.getInstance(
									SelectionFunction.getInstance(inputSpace, 1),
									CompositeFunction.getInstance(
										   SharedDomainFunction.getInstance(
												  CompositeFunction.getInstance(
														 SelectionFunction.getInstance(inputSpace, 2, 1),
														 GeneratorFunction.getInstance(this.generator)),
												  CompositeFunction.getInstance(
														 SharedDomainFunction.getInstance(
																SelectionFunction.getInstance(inputSpace, 0),
																SelectionFunction.getInstance(inputSpace, 2, 0)),
														 SelfApplyFunction.getInstance(this.cyclicGroup, zMod),
														 InvertFunction.getInstance(this.cyclicGroup))),
										   ApplyFunction.getInstance(this.cyclicGroup, 2))),
							 this.getHashConvertModuloFunction(zMod)),
					  SelectionFunction.getInstance(inputSpace, 2, 0)),
			   EqualityFunction.getInstance(zMod));
	}

	private Function getHashConvertModuloFunction(ZMod zMod) {
		HashFunction hashFunction = HashFunction.getInstance(ProductSet.getInstance(this.messageSpace, this.cyclicGroup), this.hashMethod);
		BigIntegerConverter<ByteArray> converter = FiniteByteArrayToBigInteger.getInstance(this.hashMethod.getHashAlgorithm().getHashLength());
		ConvertFunction convertFunction = ConvertFunction.getInstance(hashFunction.getCoDomain(), N.getInstance(), converter);
		ModuloFunction moduloFunction = ModuloFunction.getInstance(N.getInstance(), zMod);

		return CompositeFunction.getInstance(hashFunction, convertFunction, moduloFunction);
	}

	public static <MS extends Set> SchnorrSignatureScheme getInstance(MS messageSpace, CyclicGroup cyclicGroup) {
		return SchnorrSignatureScheme.getInstance(messageSpace, cyclicGroup, HashMethod.getInstance());
	}

	public static <MS extends Set> SchnorrSignatureScheme getInstance(MS messageSpace, CyclicGroup cyclicGroup, HashMethod hashMethod) {
		if (messageSpace == null || cyclicGroup == null || !cyclicGroup.isCyclic() || hashMethod == null) {
			throw new IllegalArgumentException();
		}
		return new SchnorrSignatureScheme<MS>(messageSpace, cyclicGroup, cyclicGroup.getDefaultGenerator(), hashMethod);
	}

	public static <MS extends Set> SchnorrSignatureScheme getInstance(MS messageSpace, Element generator) {
		return SchnorrSignatureScheme.getInstance(messageSpace, generator, HashMethod.getInstance());
	}

	public static <MS extends Set> SchnorrSignatureScheme getInstance(MS messageSpace, Element generator, HashMethod hashMethod) {
		if (messageSpace == null || generator == null || !generator.getSet().isCyclic() || !generator.isGenerator() || hashMethod == null) {
			throw new IllegalArgumentException();
		}
		return new SchnorrSignatureScheme<MS>(messageSpace, (CyclicGroup) generator.getSet(), generator, hashMethod);
	}

}
