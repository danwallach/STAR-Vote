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

import ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.interfaces.ChallengeGenerator;
import ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.interfaces.SigmaChallengeGenerator;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZMod;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.ProductSet;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Triple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.CyclicGroup;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Group;
import java.math.BigInteger;

/**
 *
 * @author philipp
 */
public abstract class AbstractShuffleProofSystem
	   extends AbstractProofSystem<ProductGroup, Triple, ProductGroup, Tuple, ProductGroup, Triple> {

	final public static int DEFAULT_KR = 20;

	final private SigmaChallengeGenerator sigmaChallengeGenerator;
	final private ChallengeGenerator eValuesGenerator;
	final private CyclicGroup cyclicGroup;
	final private int size;
	final private int ke;
	final private int kc;
	final private int kr;
	final private Tuple independentGenerators;

	protected AbstractShuffleProofSystem(SigmaChallengeGenerator sigmaChallengeGenerator, ChallengeGenerator eValuesGenerator,
		   CyclicGroup cyclicGroup, int size, int kr, Tuple independentGenerators) {
		this.sigmaChallengeGenerator = sigmaChallengeGenerator;
		this.eValuesGenerator = eValuesGenerator;
		this.cyclicGroup = cyclicGroup;
		this.size = size;
		this.kr = kr;
		this.independentGenerators = independentGenerators;

		this.ke = ((ZMod) ((ProductSet) this.eValuesGenerator.getChallengeSpace()).getFirst()).getModulus().subtract(BigInteger.ONE).bitLength();
		this.kc = this.sigmaChallengeGenerator.getChallengeSpace().getModulus().subtract(BigInteger.ONE).bitLength();
	}

	//===================================================================================
	// Public Interface
	//
	// Proof:   (SigmaProof-Triple, public inputs for sigma proof cV)
	@Override
	protected ProductGroup abstractGetProofSpace() {
		return this.getPreimageProofSpace();
	}

	// PreimageProof: (t,c,s)
	public ProductGroup getPreimageProofSpace() {
		return ProductGroup.getInstance(this.getCommitmentSpace(),
										this.getChallengeSpace(),
										this.getResponseSpace());
	}

	// c: [0,...,2^kc - 1]
	public ZMod getChallengeSpace() {
		return this.getSigmaChallengeGenerator().getChallengeSpace();
	}

	public SigmaChallengeGenerator getSigmaChallengeGenerator() {
		return this.sigmaChallengeGenerator;
	}

	public ChallengeGenerator getEValuesGenerator() {
		return this.eValuesGenerator;
	}

	public CyclicGroup getCyclicGroup() {
		return this.cyclicGroup;
	}

	public int getSize() {
		return this.size;
	}

	public int getKe() {
		return this.ke;
	}

	public int getKc() {
		return this.kc;
	}

	public int getKr() {
		return this.kr;
	}

	public Tuple getIndependentGenerators() {
		return this.independentGenerators;
	}

	//===================================================================================
	// Helpers
	//
	// Helper to compute the inner product
	// - Additive:       Sum(t1_i*t2_i)
	// - Multiplicative: Prod(t1_i^(t2_i))
	protected static Element computeInnerProduct(Tuple t1, Tuple t2) {
		if (!t1.getSet().isGroup() || t1.getArity() < 1) {
			throw new IllegalArgumentException();
		}
		Element innerProduct = ((Group) t1.getSet().getAt(0)).getIdentityElement();
		for (int i = 0; i < t1.getArity(); i++) {
			innerProduct = innerProduct.apply(t1.getAt(i).selfApply(t2.getAt(i)));
		}
		return innerProduct;
	}

	//===================================================================================
	// Helpers to create spaces
	//
	// [0,...,2^kc - 1] \subseteq Z
	protected static ZMod createChallengeSpace(int k) {
		return ZMod.getInstance(BigInteger.valueOf(2).pow(k));
	}

	// [0,...,2^ke - 1]^N \subseteq Z^N
	protected static ProductGroup createEValuesGeneratorChallengeSpace(int k, int size) {
		return ProductGroup.getInstance(createChallengeSpace(k), size);
	}

	//===================================================================================
	// Abstract Methods
	//
	abstract public ProductGroup getCommitmentSpace();

	abstract public ProductGroup getResponseSpace();

}
