package lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.encoder.classes;

import ch.bfh.unicrypt.crypto.encoder.abstracts.AbstractEncoder;
import ch.bfh.unicrypt.crypto.encoder.exceptions.ProbabilisticEncodingException;
import ch.bfh.unicrypt.crypto.encoder.interfaces.ProbabilisticEncoder;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.additive.classes.ECPolynomialElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.additive.classes.ECPolynomialField;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.PolynomialElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModPrime;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModTwo;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.abstracts.AbstractFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.interfaces.Function;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
import java.math.BigInteger;
/*
 * UniCrypt
 *
 * UniCrypt(tm) : Cryptographical framework allowing the implementation of cryptographic protocols e.g. e-voting
 * Copyright (C) 2014 Bern University of Applied Sciences (BFH), Research Institute for Security in the Information
 * Society (RISIS), E-Voting Group (EVG) Quellgasse 21, CH-2501 Biel, Switzerland
 *
 * Licensed under Dual License consisting of: 1. GNU Affero General Public License (AGPL) v3 and 2. Commercial license
 *
 *
 * 1. This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 *
 * 2. Licensees holding valid commercial licenses for UniCrypt may use this file in accordance with the commercial
 * license agreement provided with the Software or, alternatively, in accordance with the terms contained in a written
 * agreement between you and Bern University of Applied Sciences (BFH), Research Institute for Security in the
 * Information Society (RISIS), E-Voting Group (EVG) Quellgasse 21, CH-2501 Biel, Switzerland.
 *
 *
 * For further information contact <e-mail: unicrypt@bfh.ch>
 *
 *
 * Redistributions of files must retain the above copyright notice.
 */

public class ZModToECPolynomialFieldEncoder
	   extends AbstractEncoder<ZModPrime, ZModElement, ECPolynomialField, ECPolynomialElement>
	   implements ProbabilisticEncoder {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private final ZModPrime zMod;
	private final ECPolynomialField ecPoly;
	private int shift;

	private ZModToECPolynomialFieldEncoder(ZModPrime zMod, ECPolynomialField ecPoly,int shift) {
		super();
		this.zMod = zMod;
		this.ecPoly = ecPoly;
		this.shift=shift;
	}

	@Override
	protected Function abstractGetEncodingFunction() {
		return new ECF2mEncodingFunction(this.zMod, this.ecPoly,shift);
	}

	@Override
	protected Function abstractGetDecodingFunction() {
		return new ECF2mDecodingFunction(this.getCoDomain(), this.getDomain(),shift);
	}

	public static ZModToECPolynomialFieldEncoder getInstance(final ZModPrime zMod, final ECPolynomialField ec,int shift) {
		if (ec == null || zMod == null) {
			throw new IllegalArgumentException();
		}
		return new ZModToECPolynomialFieldEncoder(zMod, ec,shift);
	}

	static class ECF2mEncodingFunction
		   extends AbstractFunction<ECF2mEncodingFunction, ZModPrime, ZModElement, ECPolynomialField, ECPolynomialElement> {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private int shift;
		
		protected ECF2mEncodingFunction(ZModPrime domain, ECPolynomialField coDomain,int shift) {
			super(domain, coDomain);
			this.shift=shift;
		}

		@Override
		protected ECPolynomialElement abstractApply(ZModElement element,
			   RandomByteSequence randomByteSequence) {
			boolean firstOption = true;
			
			ZModPrime zModPrime=this.getDomain();
			ECPolynomialField ec=this.getCoDomain();
			
			int msgSpace=zModPrime.getOrder().toString(2).length();
			int msgBitLength=element.getValue().toString(2).length()+2;
			
			BigInteger c;
			
			
			if(msgSpace/3>msgBitLength){
				c=BigInteger.ZERO;
				this.shift=msgSpace/3*2;
			}
			else if(msgSpace/2>msgBitLength){
				c=BigInteger.ONE;
				this.shift=msgSpace/2;
			}
			else if (msgSpace/3*2>msgBitLength) {
				c=new BigInteger("2");
				this.shift=msgSpace/3;
			}
			else{
				c=new BigInteger("3");
			}
			
			BigInteger e = element.getValue();
			e = e.shiftLeft(shift+2);
			e=e.add(c);
			
			
			ZModElement zModElement = this.getDomain().getElement(e);

			ZModToBinaryPolynomialEncoder enc = ZModToBinaryPolynomialEncoder.getInstance(zModPrime, ec.getFiniteField());
			PolynomialElement<ZModTwo> x = enc.encode(zModElement);
			ZModElement stepp=zModPrime.getElement(4);

			int count = 0;
			while (!ec.contains(x)) {
				if (count >= (1 << shift)) {
					throw new ProbabilisticEncodingException(e + " can not be encoded");
				}

				
				zModElement = zModElement.add(stepp);
				x = enc.encode(zModElement);
				count++;


			}

			if (firstOption) {
				ECPolynomialElement[] y = ec.getY(x);
				PolynomialElement<ZModTwo> y1 = y[0].getY();
				PolynomialElement<ZModTwo> y2 = y[1].getY();
				if (isBigger(y1, y2)) {
					return ec.getElement(x, y1);

				}
				return ec.getElement(x, y2);
			} else {

				zModElement=element.invert();
				msgBitLength=zModElement.getValue().toString(2).length();
				
				if(msgSpace/3>msgBitLength){
					c=BigInteger.ZERO;
					this.shift=msgSpace/3*2;
				}
				else if(msgSpace/2>msgBitLength){
					c=BigInteger.ONE;
					this.shift=msgSpace/2;
				}
				else if (msgSpace/3*2>msgBitLength) {
					c=new BigInteger("2");
					this.shift=msgSpace/3;
				}
				else{
					c=new BigInteger("3");
				}
				
				e = zModElement.getValue();
				e = e.shiftLeft(shift+2);
				e=e.add(c);
				
				x = enc.encode(zModElement);

				count = 0;
				while (!ec.contains(x)) {
					if (count >= (1 << shift)) {
						throw new ProbabilisticEncodingException(e + " can not be encoded");
					}

					
					zModElement = zModElement.add(stepp);
					x = enc.encode(zModElement);
					count++;


				}

				ECPolynomialElement[] y = ec.getY(x);
				PolynomialElement<ZModTwo> y1 = y[0].getY();
				PolynomialElement<ZModTwo> y2 = y[1].getY();

				if (isBigger(y1, y2)) {
					return ec.getElement(x, y2);
				}
				return ec.getElement(x, y1);
			}

		}

	}

	static class ECF2mDecodingFunction
		   extends AbstractFunction<ECF2mDecodingFunction, ECPolynomialField, ECPolynomialElement, ZModPrime, ZModElement> {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private int shift;
		
		public ECF2mDecodingFunction(ECPolynomialField domain, ZModPrime coDomain,int shift) {
			super(domain, coDomain);
			this.shift=shift;
		}

		@Override
		protected ZModElement abstractApply(ECPolynomialElement element,
			   RandomByteSequence randomByteSequence) {
			
			ZModPrime zModPrime=this.getCoDomain();
			ECPolynomialField ec=this.getDomain();
			int msgSpace=zModPrime.getOrder().toString(2).length();
			ZModToBinaryPolynomialEncoder enc = ZModToBinaryPolynomialEncoder.getInstance(zModPrime, ec.getFiniteField());
			
			PolynomialElement<ZModTwo> x = element.getX();
			PolynomialElement<ZModTwo> y = element.getY();
			ECPolynomialElement[] yTuple = ec.getY(x);
			PolynomialElement<ZModTwo> y1 = yTuple[0].getY();
			PolynomialElement<ZModTwo> y2 = yTuple[1].getY();

			BigInteger x1=enc.decode(x).getBigInteger();
			
			BigInteger c=x1.subtract(x1.shiftRight(2).shiftLeft(2));
			
			if(c.equals(BigInteger.ZERO)){
				this.shift=msgSpace/3*2;
			}
			else if (c.equals(BigInteger.ONE)) {
				this.shift=msgSpace/2;
			}
			else if (c.equals(new BigInteger("2"))) {
				this.shift=msgSpace/3;
			}
			
			x1=x1.shiftRight(shift+2);
			
			if (y.isEquivalent(getBiggerY(y1, y2))) {
				return zModPrime.getElement(x1);
			} else {
				return zModPrime.getElement(x1).invert();			}

		}

	}

	/**
	 * Compares the two polynomial elements and return the element with the most significant coefficient not in common.
	 * <p>
	 * @param y1
	 * @param y2
	 * @return
	 */
	public static PolynomialElement<ZModTwo> getBiggerY(PolynomialElement<ZModTwo> y1, PolynomialElement<ZModTwo> y2) {
		int deg = y1.add(y2).getValue().getDegree();
		BigInteger y1Coeff = y1.getValue().getCoefficient(deg).getBigInteger();
		BigInteger y2Coeff = y2.getValue().getCoefficient(deg).getBigInteger();

		if (y1Coeff.compareTo(y2Coeff) > 0) {
			return y1;
		}
		return y2;
	}

	/**
	 * Compares y1 and y2 and returns if y1 is bigger then y2 -> getBiggerY
	 * <p>
	 * @param y1
	 * @param y2
	 * @return
	 */
	public static boolean isBigger(PolynomialElement<ZModTwo> y1, PolynomialElement<ZModTwo> y2) {
		return y1.isEquivalent(getBiggerY(y1, y2));
	}

}
