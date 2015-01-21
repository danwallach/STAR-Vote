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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.proofsystem.classes;

import ch.bfh.unicrypt.crypto.proofsystem.abstracts.AbstractProofSystem;
import ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.classes.RandomOracleSigmaChallengeGenerator;
import ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.interfaces.SigmaChallengeGenerator;
import ch.bfh.unicrypt.crypto.proofsystem.interfaces.SigmaProofSystem;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZMod;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Pair;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductSemiGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductSet;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Triple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.CyclicGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Group;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.SemiGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.ApplyInverseFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.CompositeFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.GeneratorFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.SelectionFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.SharedDomainFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.interfaces.Function;
import ch.bfh.unicrypt.random.classes.PseudoRandomOracle;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
import ch.bfh.unicrypt.random.interfaces.RandomOracle;

//
// @see [cs03] Camenisch, J. & Shoup, V., 2003. Practical verifiable encryption and decryption of discrete logarithms.
//             -> 5 Proving the Inequality of Discrete Logarithms
//
public class InequalityOfPreimagesProofSystem
	   extends AbstractProofSystem<SemiGroup, Element, ProductGroup, Pair, Set, Pair> {

	private final SigmaChallengeGenerator challengeGenerator;
	private final Function firstFunction;
	private final Function secondFunction;

	protected InequalityOfPreimagesProofSystem(final SigmaChallengeGenerator challengeGenerator, final Function firstFunction, final Function secondFunction) {
		this.challengeGenerator = challengeGenerator;
		this.firstFunction = firstFunction;
		this.secondFunction = secondFunction;
	}

	public static InequalityOfPreimagesProofSystem getInstance(final Function firstFunction, final Function secondFunction) {
		return InequalityOfPreimagesProofSystem.getInstance((Element) null, firstFunction, secondFunction);
	}

	public static InequalityOfPreimagesProofSystem getInstance(final Element proverId, final Function firstFunction, final Function secondFunction) {
		SigmaChallengeGenerator challengeGenerator = InequalityOfPreimagesProofSystem.createNonInteractiveChallengeGenerator(firstFunction, secondFunction, proverId);
		return InequalityOfPreimagesProofSystem.getInstance(challengeGenerator, firstFunction, secondFunction);
	}

	public static InequalityOfPreimagesProofSystem getInstance(final SigmaChallengeGenerator challengeGenerator, final Function firstFunction, final Function secondFunction) {
		if (challengeGenerator == null || firstFunction == null || secondFunction == null || !firstFunction.getDomain().isEquivalent(secondFunction.getDomain())
			   || !firstFunction.getDomain().isSemiGroup() || !firstFunction.getCoDomain().isCyclic() || !secondFunction.getCoDomain().isCyclic()) {
			throw new IllegalArgumentException();
		}

		ProductSet codomain = ProductSet.getInstance(secondFunction.getCoDomain(), firstFunction.getCoDomain());
		ZMod cs = ZMod.getInstance(ProductSet.getInstance(firstFunction.getDomain(), secondFunction.getDomain()).getMinimalOrder());
		if (!codomain.isEquivalent(challengeGenerator.getPublicInputSpace()) || !codomain.isEquivalent(challengeGenerator.getCommitmentSpace())
			   || !cs.isEquivalent(challengeGenerator.getChallengeSpace())) {
			throw new IllegalArgumentException("Spaces of challenge generator don't match proof functions.");
		}
		return new InequalityOfPreimagesProofSystem(challengeGenerator, firstFunction, secondFunction);
	}

	// Service method to prove inequality fo descrete logarithms
	// f1(x) = g1^x, f2(x) = g2^x
	public static InequalityOfPreimagesProofSystem getInstance(final SigmaChallengeGenerator challengeGenerator, final Element firstGenerator, final Element secondGenerator) {
		if (firstGenerator == null || secondGenerator == null || !firstGenerator.getSet().isEquivalent(secondGenerator.getSet())
			   || !firstGenerator.getSet().isCyclic() || !firstGenerator.isGenerator() || !secondGenerator.isGenerator()) {
			throw new IllegalArgumentException();
		}
		Function f1 = GeneratorFunction.getInstance(firstGenerator);
		Function f2 = GeneratorFunction.getInstance(secondGenerator);

		return InequalityOfPreimagesProofSystem.getInstance(challengeGenerator, f1, f2);
	}

	@Override
	protected SemiGroup abstractGetPrivateInputSpace() {
		return (SemiGroup) this.getFirstFunction().getDomain();
	}

	@Override
	protected ProductGroup abstractGetPublicInputSpace() {
		return ProductGroup.getInstance((Group) this.getFirstFunction().getCoDomain(), (Group) this.getSecondFunction().getCoDomain());
	}

	@Override
	protected ProductSet abstractGetProofSpace() {
		return ProductSet.getInstance(this.getPreimageProofSpace(), this.getSecondFunction().getCoDomain());
	}

	public ZMod getChallengeSpace() {
		return ZMod.getInstance(this.getFirstFunction().getDomain().getOrder());
	}

	public ProductSet getPreimageProofSpace() {
		return ProductSet.getInstance(ProductGroup.getInstance(this.getFirstFunction().getCoDomain(), this.getSecondFunction().getCoDomain()),
									  this.getChallengeSpace(),
									  ProductGroup.getInstance(this.getPrivateInputSpace(), 2));
	}

	public Triple getPreimageProof(Pair proof) {
		return (Triple) proof.getFirst();
	}

	public Element getProofCommitment(Pair proof) {
		return proof.getSecond();
	}

	public Function getFirstFunction() {
		return this.firstFunction;
	}

	public Function getSecondFunction() {
		return this.secondFunction;
	}

	public SigmaChallengeGenerator getChallengeGenerator() {
		return this.challengeGenerator;
	}

	@Override
	protected Pair abstractGenerate(Element privateInput, Pair publicInput, RandomByteSequence randomByteSequence) {

		// 1. Create commitment:
		//    C = (f2(x)/z)^r with random r            |==> C = (h^x/z)^r
		Element r = this.getSecondFunction().getCoDomain().getZModOrder().getRandomElement(randomByteSequence);
		Element x = privateInput;
		Element z = publicInput.getSecond();

		Element c = this.secondFunction.apply(x).apply(z.invert()).selfApply(r);

		// 2. Create preimage proof: for a=xr, b=r
		//    f(a,b) = (f2(a)/z^b, f1(a)/y^b) = (C,1)  |==> f(a,b) = (h^a/z^b, g^a/y^b)
		SigmaProofSystem preimageProofGenerator = this.createPreimageProofGenerator(publicInput);

		Triple preimageProof = preimageProofGenerator.generate(
			   Tuple.getInstance(x.selfApply(r), r),
			   Tuple.getInstance(c, ((CyclicGroup) this.getFirstFunction().getCoDomain()).getIdentityElement()),
			   randomByteSequence);

		return Pair.getInstance(preimageProof, c);
	}

	@Override
	protected boolean abstractVerify(Pair proof, Pair publicInput) {

		// 1. Verify preimage proof
		SigmaProofSystem preimageProofGenerator = this.createPreimageProofGenerator(publicInput);
		boolean v = preimageProofGenerator.verify(
			   this.getPreimageProof(proof),
			   Tuple.getInstance(this.getProofCommitment(proof), ((CyclicGroup) this.getFirstFunction().getCoDomain()).getIdentityElement()));

		// 2. Check C != 1
		boolean c = !this.getProofCommitment(proof).isEquivalent(((CyclicGroup) this.getFirstFunction().getCoDomain()).getIdentityElement());

		return v && c;
	}

	// f(a,b) = (f2(a)/z^b, f1(a)/y^b)                 |==> f(a,b) = (h^a/z^b, g^a/y^b)
	private SigmaProofSystem createPreimageProofGenerator(Pair publicInput) {

		Element y = publicInput.getFirst();
		Element z = publicInput.getSecond();

		Function[] functions = new Function[2];
		ProductSet domain = ProductSet.getInstance(this.getPrivateInputSpace(), 2);
		functions[0] = this.createSinglePreimageProofFunction(domain, this.getSecondFunction(), z);
		functions[1] = this.createSinglePreimageProofFunction(domain, this.getFirstFunction(), y);

		return PreimageEqualityProofSystem.getInstance(this.getChallengeGenerator(), functions);
	}

	// f(a,b) = f(a)/y^b                               |==> f(a,b) = g1^a/g2^b
	private Function createSinglePreimageProofFunction(ProductSet domain, Function f, Element y) {
		return CompositeFunction.getInstance(
			   SharedDomainFunction.getInstance(CompositeFunction.getInstance(SelectionFunction.getInstance(domain, 0), f),
												CompositeFunction.getInstance(SelectionFunction.getInstance(domain, 1),
																			  GeneratorFunction.getInstance(y))),
			   ApplyInverseFunction.getInstance((Group) f.getCoDomain()));
	}

	public static RandomOracleSigmaChallengeGenerator createNonInteractiveChallengeGenerator(final Function firstFunction, final Function secondFunction) {
		return InequalityOfPreimagesProofSystem.createNonInteractiveChallengeGenerator(firstFunction, secondFunction, PseudoRandomOracle.DEFAULT);
	}

	public static RandomOracleSigmaChallengeGenerator createNonInteractiveChallengeGenerator(final Function firstFunction, final Function secondFunction, final Element proverId) {
		return InequalityOfPreimagesProofSystem.createNonInteractiveChallengeGenerator(firstFunction, secondFunction, proverId, PseudoRandomOracle.DEFAULT);
	}

	public static RandomOracleSigmaChallengeGenerator createNonInteractiveChallengeGenerator(final Function firstFunction, final Function secondFunction, final RandomOracle randomOracle) {
		return InequalityOfPreimagesProofSystem.createNonInteractiveChallengeGenerator(firstFunction, secondFunction, (Element) null, randomOracle);
	}

	public static RandomOracleSigmaChallengeGenerator createNonInteractiveChallengeGenerator(final Function firstFunction, final Function secondFunction, final Element proverId, final RandomOracle randomOracle) {
		if (firstFunction == null || secondFunction == null || randomOracle == null
			   || !firstFunction.getCoDomain().isSemiGroup() || !secondFunction.getCoDomain().isSemiGroup()) {
			throw new IllegalArgumentException();
		}
		ProductSemiGroup codomain = ProductSemiGroup.getInstance((SemiGroup) secondFunction.getCoDomain(), (SemiGroup) firstFunction.getCoDomain());
		ZMod cs = ZMod.getInstance(ProductSet.getInstance(firstFunction.getDomain(), secondFunction.getDomain()).getMinimalOrder());
		return RandomOracleSigmaChallengeGenerator.getInstance(codomain, codomain, cs, proverId, randomOracle);

	}

}
