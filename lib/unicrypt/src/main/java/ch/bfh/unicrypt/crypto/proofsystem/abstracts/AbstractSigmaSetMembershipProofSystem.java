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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.proofsystem.abstracts;

import ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.interfaces.SigmaChallengeGenerator;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.proofsystem.classes.PreimageOrProofSystem;
import ch.bfh.unicrypt.crypto.proofsystem.interfaces.SigmaSetMembershipProofSystem;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Pair;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductSet;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Subset;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Triple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.SemiGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.CompositeFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.MultiIdentityFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.ProductFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.SelectionFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.SharedDomainFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.interfaces.Function;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;

//
// setMembershipProofFunction: f(x,r)
// deltaFunction: f(x,y)
// preiamgeProofFunction: setMemebershipFunction_x(r) o deltaFunction_x(y)
//
public abstract class AbstractSigmaSetMembershipProofSystem<PUS extends SemiGroup, PUE extends Element>
	   extends AbstractSigmaProofSystem<ProductSet, Pair, PUS, PUE, ProductFunction>
	   implements SigmaSetMembershipProofSystem {

	private final Subset members;
	private Function setMembershipProofFunction;
	private Function deltaFunction;
	private ProductFunction preimageProofFunction;
	private PreimageOrProofSystem orProofGenerator;

	protected AbstractSigmaSetMembershipProofSystem(final SigmaChallengeGenerator challengeGenerator, final Subset members) {
		super(challengeGenerator);
		this.members = members;
	}

	@Override
	public Subset getMembers() {
		return this.members;
	}

	@Override
	public Function getSetMembershipProofFunction() {
		if (this.setMembershipProofFunction == null) {
			this.setMembershipProofFunction = this.abstractGetSetMembershipFunction();
		}
		return this.setMembershipProofFunction;
	}

	@Override
	public Function getDeltaFunction() {
		if (this.deltaFunction == null) {
			this.deltaFunction = this.abstractGetDeltaFunction();
		}
		return this.deltaFunction;
	}

	@Override
	protected ProductSet abstractGetPrivateInputSpace() {
		return this.getOrProofGenerator().getPrivateInputSpace();
	}

	@Override
	protected PUS abstractGetPublicInputSpace() {
		return (PUS) this.getPreimageProofFunction().getAt(0).getCoDomain();
	}

	@Override
	protected ProductSet abstractGetProofSpace() {
		return ProductSet.getInstance(
			   this.getCommitmentSpace(),
			   ProductSet.getInstance(this.getChallengeSpace(), this.members.getOrder().intValue()),
			   this.getResponseSpace());
	}

	@Override
	protected ProductFunction abstractGetPreimageProofFunction() {
		if (this.preimageProofFunction == null) {
			this.preimageProofFunction = this.createPreimageProofFunction();
		}
		return this.preimageProofFunction;
	}

	@Override
	protected Triple abstractGenerate(Pair privateInput, PUE publicInput, RandomByteSequence randomByteSequence) {
		return this.getOrProofGenerator().generate(privateInput, this.createProofImages(publicInput), randomByteSequence);
	}

	@Override
	protected boolean abstractVerify(Triple proof, PUE publicInput) {
		return this.getOrProofGenerator().verify(proof, this.createProofImages(publicInput));
	}

	public Pair createPrivateInput(Element secret, int index) {
		return (Pair) this.getOrProofGenerator().createPrivateInput(secret, index);
	}

	private Tuple createProofImages(PUE publicInput) {
		final Element[] images = new Element[this.members.getOrder().intValue()];
		int i = 0;
		for (Element memberElement : this.members.getElements()) {
			images[i++] = this.getDeltaFunction().apply(memberElement, publicInput);
		}
		return Tuple.getInstance(images);
	}

	private PreimageOrProofSystem getOrProofGenerator() {
		if (this.orProofGenerator == null) {
			this.orProofGenerator = PreimageOrProofSystem.getInstance(this.getChallengeGenerator(), this.getPreimageProofFunction());
		}
		return this.orProofGenerator;
	}

	private ProductFunction createPreimageProofFunction() {

		// proofFunction = composite( sharedDomainFunction(selction(0), setMembershipProofFunction), deltaFunction)
		final ProductSet setMembershipPFDomain = (ProductSet) this.getSetMembershipProofFunction().getDomain();
		final Function proofFunction = CompositeFunction.getInstance(SharedDomainFunction.getInstance(SelectionFunction.getInstance(setMembershipPFDomain, 0),
																									  this.getSetMembershipProofFunction()),
																	 this.getDeltaFunction());

		// proofFunction_x = composite( multiIdentity(1), proofFunction.partiallyApply(x, 0))
		final Function[] proofFunctions = new Function[this.members.getOrder().intValue()];
		final Set rSet = setMembershipPFDomain.getAt(1);
		int i = 0;
		for (Element memberElement : this.members.getElements()) {
			proofFunctions[i++] = CompositeFunction.getInstance(MultiIdentityFunction.getInstance(rSet, 1),
																proofFunction.partiallyApply(memberElement, 0));
		}

		return ProductFunction.getInstance(proofFunctions);
	}

	protected abstract Function abstractGetSetMembershipFunction();

	protected abstract Function abstractGetDeltaFunction();

}
