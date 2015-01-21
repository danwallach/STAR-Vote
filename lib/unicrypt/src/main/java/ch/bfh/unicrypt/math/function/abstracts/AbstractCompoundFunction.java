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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.abstracts;

import ch.bfh.unicrypt.helper.array.classes.DenseArray;
import ch.bfh.unicrypt.helper.array.interfaces.ImmutableArray;
import ch.bfh.unicrypt.helper.array.interfaces.RecursiveArray;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import ch.bfh.unicrypt.math.function.interfaces.Function;
import java.util.Iterator;

/**
 *
 * @param <CF>
 * @param <D>
 * @param <DE>
 * @param <C>
 * @param <CE>
 * @author rolfhaenni
 */
public abstract class AbstractCompoundFunction<CF extends AbstractCompoundFunction<CF, D, DE, C, CE>, D extends Set, DE extends Element, C extends Set, CE extends Element>
	   extends AbstractFunction<CF, D, DE, C, CE>
	   implements RecursiveArray<Function> {

	protected final DenseArray<Function> functions;

	protected AbstractCompoundFunction(D domain, C coDomain, DenseArray<Function> functions) {
		super(domain, coDomain);
		this.functions = functions;
	}

	public int getArity() {
		return this.getLength();
	}

	@Override
	public int getLength() {
		return this.functions.getLength();
	}

	@Override
	public final boolean isEmpty() {
		return this.functions.isEmpty();
	}

	@Override
	public final boolean isUniform() {
		return this.functions.isUniform();
	}

	@Override
	public Iterable<Integer> getAllIndices() {
		return this.functions.getAllIndices();
	}

	@Override
	public Iterable<Integer> getIndices(Function function) {
		return this.functions.getIndices(function);
	}

	@Override
	public Iterable<Integer> getIndicesExcept(Function function) {
		return this.functions.getIndicesExcept(function);
	}

	@Override
	public int count(Function function) {
		return this.functions.count(function);
	}

	@Override
	public int countPrefix(Function function) {
		return this.functions.countPrefix(function);
	}

	@Override
	public int countSuffix(Function function) {
		return this.functions.countSuffix(function);
	}

	@Override
	public Function getFirst() {
		return this.functions.getFirst();
	}

	@Override
	public Function getLast() {
		return this.functions.getLast();
	}

	@Override
	public Function getAt(int index) {
		return this.functions.getAt(index);
	}

	@Override
	public Function getAt(int... indices) {
		if (indices == null) {
			throw new IllegalArgumentException();
		}
		Function function = this;
		for (final int index : indices) {
			if (function.isCompound()) {
				function = ((ImmutableArray<Function>) function).getAt(index);
			} else {
				throw new IllegalArgumentException();
			}
		}
		return function;
	}

	@Override
	public CF extract(int offset, int length) {
		return this.abstractGetInstance(this.functions.extract(offset, length));
	}

	@Override
	public CF extractPrefix(int length) {
		return this.abstractGetInstance(this.functions.extractPrefix(length));
	}

	@Override
	public CF extractSuffix(int length) {
		return this.abstractGetInstance(this.functions.extractSuffix(length));
	}

	@Override
	public CF extractRange(int fromIndex, int toIndex) {
		return this.abstractGetInstance(this.functions.extractRange(fromIndex, toIndex));
	}

	@Override
	public CF remove(int offset, int length) {
		return this.abstractGetInstance(this.functions.remove(offset, length));
	}

	@Override
	public CF removePrefix(int length) {
		return this.abstractGetInstance(this.functions.removePrefix(length));
	}

	@Override
	public CF removeSuffix(int length) {
		return this.abstractGetInstance(this.functions.removeSuffix(length));
	}

	@Override
	public CF removeRange(int fromIndex, int toIndex) {
		return this.abstractGetInstance(this.functions.removeRange(fromIndex, toIndex));
	}

	@Override
	public CF removeAt(final int index) {
		return this.abstractGetInstance(this.functions.removeAt(index));
	}

	@Override
	public CF insertAt(int index, Function function) {
		return this.abstractGetInstance(this.functions.insertAt(index, function));
	}

	@Override
	public CF replaceAt(int index, Function function) {
		return this.abstractGetInstance(this.functions.replaceAt(index, function));
	}

	@Override
	public CF add(Function function) {
		return this.abstractGetInstance(this.functions.add(function));
	}

	@Override
	public CF append(ImmutableArray<Function> other) {
		return this.abstractGetInstance(this.functions.append(other));
	}

	@Override
	public CF reverse() {
		return this.abstractGetInstance(this.functions.reverse());
	}

	@Override
	public CF[] split(int... indices) {
		DenseArray<Function>[] functionArray = this.functions.split(indices);
		CF[] result = (CF[]) java.lang.reflect.Array.newInstance(this.getArrayClass(), functionArray.length);
		for (int i = 0; i < functionArray.length; i++) {
			result[i] = this.abstractGetInstance(functionArray[i]);
		}
		return result;
	}

	@Override
	public Iterator<Function> iterator() {
		return this.functions.iterator();
	}

	@Override
	protected boolean defaultIsCompound() {
		return true;
	}

	@Override
	protected boolean defaultIsEquivalent(CF other) {
		if (this.getLength() != other.getLength()) {
			return false;
		}
		for (int i : this.getAllIndices()) {
			if (!this.getAt(i).isEquivalent(other.getAt(i))) {
				return false;
			}
		}
		return true;
	}

	protected abstract CF abstractGetInstance(DenseArray<Function> functions);

	abstract protected Class getArrayClass();

}
