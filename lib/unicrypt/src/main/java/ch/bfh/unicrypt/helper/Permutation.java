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

import ch.bfh.unicrypt.random.classes.HybridRandomByteSequence;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;

public class Permutation
	   extends UniCrypt
	   implements Iterable<Integer> {

	private final int[] permutationVector;

	private Permutation(int[] permutationVector) {
		this.permutationVector = permutationVector;
	}

	/**
	 * Returns the size of the permutation element, which is the length of the corresponding permutation vector. The
	 * size of a permutation element is the same as the size of the corresponding group.
	 * <p>
	 * @return The size of the permutation element
	 */
	public int getSize() {
		return this.permutationVector.length;
	}

	// Ranking algorithm by Myrvold and Ruskey: "Ranking and Unranking Permutations in Linear Time"
	public BigInteger getRank() {
		int size = this.getSize();
		int[] invertedPermutation = new int[size];
		for (int i = 0; i < size; i++) {
			invertedPermutation[this.permutationVector[i]] = i;
		}
		return computeRank(size, this.permutationVector.clone(), invertedPermutation);
	}

	public static BigInteger computeRank(int n, int[] permutation, int[] invertedPermutation) {
		if (n <= 1) {
			return BigInteger.ZERO;
		}
		int s = permutation[n - 1];
		swap(permutation, n - 1, invertedPermutation[n - 1]);
		swap(invertedPermutation, s, n - 1);
		return BigInteger.valueOf(s).add(BigInteger.valueOf(n).multiply(computeRank(n - 1, permutation, invertedPermutation)));
	}

	public static void swap(int[] permutation, int i, int j) {
		int x = permutation[i];
		permutation[i] = permutation[j];
		permutation[j] = x;
	}

//		// code copied from http://rosettacode.org/wiki/Permutations/Rank_of_a_permutation
//		int n = this.getSize();
//		BitSet usedDigits = new BitSet();
//		BigInteger rank = BigInteger.ZERO;
//		for (int i = 0; i < n; i++) {
//			rank = rank.multiply(BigInteger.valueOf(n - i));
//			int digit = 0;
//			int v = -1;
//			while ((v = usedDigits.nextClearBit(v + 1)) < this.permutationVector[i]) {
//				digit++;
//			}
//			usedDigits.set(v);
//			rank = rank.add(BigInteger.valueOf(digit));
//		}
//		return rank;
//}
	/**
	 * Returns the result of applying the permutation vector to a given index.
	 * <p>
	 * @param index The given index
	 * @return The permuted index
	 * @throw IndexOutOfBoundsException if {@code index} is negative or greater than {@code getSize()-1}
	 */
	public int permute(int index) {
		if (index < 0 || index >= this.getSize()) {
			throw new IndexOutOfBoundsException();
		}
		return this.permutationVector[index];
	}

	public Permutation compose(Permutation other) {
		if (other == null) {
			throw new IllegalArgumentException();
		}
		int size = this.getSize();
		final int[] vector = new int[size];
		for (int i = 0; i < size; i++) {
			vector[i] = this.permute(other.permute(i));
		}
		return new Permutation(vector);
	}

	public Permutation invert() {
		int size = this.getSize();
		final int[] vector = new int[size];
		for (int i = 0; i < size; i++) {
			vector[this.permute(i)] = i;
		}
		return new Permutation(vector);
	}

	public int[] getPermutationVector() {
		return this.permutationVector.clone();
	}

	/**
	 * Checks if an array of integers is a permutation vector, i.e., a permutation of the values from 0 to n-1. For
	 * example {3,0,1,2,4} but not {1,4,3,2}.
	 * <p>
	 * @param permutationVector The given array of integers to test
	 * @return {@literal true} if {@literal permutationVector} is a permutation vector, {@literal false} otherwise
	 * @throws IllegalArgumentException if {@literal permutationVector} is null
	 */
	public static boolean isValid(final int[] permutationVector) {
		if (permutationVector == null) {
			throw new IllegalArgumentException();
		}
		final int[] sortedVector = permutationVector.clone();
		Arrays.sort(sortedVector);
		for (int i = 0; i < permutationVector.length; i++) {
			if (sortedVector[i] != i) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected String defaultToStringName() {
		return "";
	}

	@Override
	protected String defaultToStringValue() {
		String str = Arrays.toString(this.permutationVector);
		return "(" + str.substring(1, str.length() - 1) + ")";
	}

	@Override
	public Iterator<Integer> iterator() {
		return new Iterator<Integer>() {

			int currentIndex = 0;

			@Override
			public boolean hasNext() {
				return currentIndex < permutationVector.length;
			}

			@Override
			public Integer next() {
				return permutationVector[currentIndex++];
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 97 * hash + Arrays.hashCode(this.permutationVector);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		final Permutation other = (Permutation) obj;
		return Arrays.equals(this.permutationVector, other.permutationVector);
	}

	public static Permutation getInstance(int size) {
		if (size < 0) {
			throw new IllegalArgumentException();
		}
		int[] permutationVector = new int[size];
		for (int i = 0; i < size; i++) {
			permutationVector[i] = i;
		}
		return new Permutation(permutationVector);
	}

	public static Permutation getInstance(int[] permutationVector) {
		if (!isValid(permutationVector)) {
			throw new IllegalArgumentException();
		}
		return new Permutation(permutationVector.clone());
	}

	// Unranking algorithm by Myrvold and Ruskey: "Ranking and Unranking Permutations in Linear Time"
	public static Permutation getInstance(int size, BigInteger rank) {
		if (size < 0 || rank.signum() < 0) {
			throw new IllegalArgumentException();
		}
		int[] permutationVector = new int[size];
		for (int i = 0; i < size; i++) {
			permutationVector[i] = i;
		}
		for (int i = size; i > 0; i--) {
			BigInteger iBig = BigInteger.valueOf(i);
			swap(permutationVector, rank.mod(iBig).intValue(), i - 1);
			rank = rank.divide(iBig);
		}
		// original rank >= factorial(size)
		if (rank.signum() != 0) {
			throw new IllegalArgumentException();
		}
		return new Permutation(permutationVector);
	}

	public static Permutation getRandomInstance(int size) {
		return Permutation.getRandomInstance(size, HybridRandomByteSequence.getInstance());
	}

	public static Permutation getRandomInstance(int size, RandomByteSequence randomByteSequence) {
		if (size < 0 || randomByteSequence == null) {
			throw new IllegalArgumentException();
		}
		int[] permutationVector = new int[size];
		int randomIndex;
		for (int i = 0; i < size; i++) {
			randomIndex = randomByteSequence.getRandomNumberGenerator().nextInteger(i);
			permutationVector[i] = permutationVector[randomIndex];
			permutationVector[randomIndex] = i;
		}
		return new Permutation(permutationVector);
	}

}
