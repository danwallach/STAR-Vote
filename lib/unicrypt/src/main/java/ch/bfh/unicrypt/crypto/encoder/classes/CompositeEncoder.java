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
package lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.encoder.classes;

import ch.bfh.unicrypt.crypto.encoder.abstracts.AbstractEncoder;
import ch.bfh.unicrypt.crypto.encoder.interfaces.Encoder;
import ch.bfh.unicrypt.helper.array.classes.DenseArray;
import ch.bfh.unicrypt.helper.array.interfaces.ImmutableArray;
import ch.bfh.unicrypt.helper.array.interfaces.RecursiveArray;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.general.interfaces.Set;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.classes.CompositeFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.interfaces.Function;
import java.util.Iterator;

public class CompositeEncoder
	   extends AbstractEncoder<Set, Element, Set, Element>
	   implements RecursiveArray<Encoder> {

	private final ImmutableArray<Encoder> encoders;

	protected CompositeEncoder(ImmutableArray<Encoder> encoders) {
		this.encoders = encoders;
	}

	@Override
	public int getLength() {
		return this.encoders.getLength();
	}

	@Override
	public boolean isEmpty() {
		return this.encoders.isEmpty();
	}

	@Override
	public boolean isUniform() {
		return this.encoders.isUniform();
	}

	@Override
	public Iterable<Integer> getAllIndices() {
		return this.encoders.getAllIndices();
	}

	@Override
	public Iterable<Integer> getIndices(Encoder encoder) {
		return this.encoders.getIndices(encoder);
	}

	@Override
	public Iterable<Integer> getIndicesExcept(Encoder encoder) {
		return this.encoders.getIndicesExcept(encoder);
	}

	@Override
	public int count(Encoder encoder) {
		return this.encoders.count(encoder);
	}

	@Override
	public int countPrefix(Encoder encoder) {
		return this.encoders.countPrefix(encoder);
	}

	@Override
	public int countSuffix(Encoder encoder) {
		return this.encoders.countSuffix(encoder);
	}

	@Override
	public Encoder getAt(int index) {
		return this.encoders.getAt(index);
	}

	@Override
	public Encoder getAt(int... indices) {
		if (indices == null) {
			throw new IllegalArgumentException();
		}
		Encoder encoder = this;
		for (final int index : indices) {
			if (encoder instanceof CompositeEncoder) {
				encoder = ((CompositeEncoder) encoder).getAt(index);
			} else {
				throw new IllegalArgumentException();
			}
		}
		return encoder;
	}

	@Override
	public Encoder getFirst() {
		return this.encoders.getFirst();
	}

	@Override
	public Encoder getLast() {
		return this.encoders.getLast();
	}

	@Override
	public CompositeEncoder extract(int offset, int length) {
		return new CompositeEncoder(this.encoders.extract(offset, length));
	}

	@Override
	public CompositeEncoder extractPrefix(int length) {
		return new CompositeEncoder(this.encoders.extractPrefix(length));
	}

	@Override
	public CompositeEncoder extractSuffix(int length) {
		return new CompositeEncoder(this.encoders.extractSuffix(length));
	}

	@Override
	public CompositeEncoder extractRange(int fromIndex, int toIndex) {
		return new CompositeEncoder(this.encoders.extractRange(fromIndex, toIndex));
	}

	@Override
	public CompositeEncoder remove(int offset, int length) {
		return new CompositeEncoder(this.encoders.remove(offset, length));
	}

	@Override
	public CompositeEncoder removePrefix(int n) {
		return new CompositeEncoder(this.encoders.removePrefix(n));
	}

	@Override
	public CompositeEncoder removeSuffix(int n) {
		return new CompositeEncoder(this.encoders.removeSuffix(n));
	}

	@Override
	public CompositeEncoder removeRange(int fromIndex, int toIndex) {
		return new CompositeEncoder(this.encoders.removeRange(fromIndex, toIndex));
	}

	@Override
	public CompositeEncoder removeAt(int index) {
		return new CompositeEncoder(this.encoders.removeAt(index));
	}

	@Override
	public CompositeEncoder insertAt(int index, Encoder encoder) {
		return new CompositeEncoder(this.encoders.insertAt(index, encoder));
	}

	@Override
	public CompositeEncoder replaceAt(int index, Encoder encoder) {
		return new CompositeEncoder(this.encoders.replaceAt(index, encoder));
	}

	@Override
	public CompositeEncoder add(Encoder encoder) {
		return new CompositeEncoder(this.encoders.add(encoder));
	}

	@Override
	public CompositeEncoder append(ImmutableArray<Encoder> other) {
		return new CompositeEncoder(this.encoders.append(other));
	}

	@Override
	public CompositeEncoder reverse() {
		return new CompositeEncoder(this.encoders.reverse());
	}

	@Override
	public CompositeEncoder[] split(int... indices) {
		ImmutableArray<Encoder>[] encoderArray = this.encoders.split(indices);
		CompositeEncoder[] result = new CompositeEncoder[encoderArray.length];
		for (int i = 0; i < encoderArray.length; i++) {
			result[i] = new CompositeEncoder(encoderArray[i]);
		}
		return result;
	}

	@Override
	public Iterator<Encoder> iterator() {
		return this.encoders.iterator();
	}

	@Override
	protected Function abstractGetEncodingFunction() {
		int length = this.getLength();
		Function[] encodingFunctions = new Function[length];
		for (int i : this.getAllIndices()) {
			encodingFunctions[i] = this.encoders.getAt(i).getEncodingFunction();
		}
		return CompositeFunction.getInstance(encodingFunctions);
	}

	@Override
	protected Function abstractGetDecodingFunction() {
		int length = this.getLength();
		Function[] decodingFunctions = new Function[length];
		for (int i : this.getAllIndices()) {
			decodingFunctions[length - i - 1] = this.encoders.getAt(i).getDecodingFunction();
		}
		return CompositeFunction.getInstance(decodingFunctions);
	}

	public static CompositeEncoder getInstance(DenseArray<Encoder> encoders) {
		if (encoders == null || encoders.getLength() == 0) {
			throw new IllegalArgumentException();
		}
		return new CompositeEncoder(encoders);
	}

	public static CompositeEncoder getInstance(Encoder... encoders) {
		return CompositeEncoder.getInstance(DenseArray.getInstance(encoders));
	}

	public static CompositeEncoder getInstance(Encoder encoder, int length) {
		return CompositeEncoder.getInstance(DenseArray.getInstance(encoder, length));
	}

}
