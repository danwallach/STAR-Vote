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
import ch.bfh.unicrypt.crypto.encoder.exceptions.ProbabilisticEncodingException;
import ch.bfh.unicrypt.crypto.encoder.interfaces.ProbabilisticEncoder;
import ch.bfh.unicrypt.helper.MathUtil;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.additive.classes.ECZModElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.additive.classes.ECZModPrime;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZMod;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModPrime;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.abstracts.AbstractFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.interfaces.Function;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;
import java.math.BigInteger;
/**
 * 
 * @author Christian Lutz
 *
 */
public class ZModToECZModPrimeEncoder
	   extends AbstractEncoder<ZModPrime, ZModElement, ECZModPrime, ECZModElement>
	   implements ProbabilisticEncoder {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected int shift;
	private final ECZModPrime ec;
	private final ZModPrime zModPrime;
	
	protected ZModToECZModPrimeEncoder(ECZModPrime ec,int shift) {
		this.ec = ec;
		this.zModPrime=ec.getFiniteField();
		this.shift=shift;
	}

	@Override
	protected Function abstractGetEncodingFunction() {
		return new ECEncodingFunction(this.zModPrime, this.ec,this.shift);
	}

	@Override
	protected Function abstractGetDecodingFunction() {
		return new ECDecodingFunction(ec, this.zModPrime,this.shift);
	}

	public static ZModToECZModPrimeEncoder getInstance(final ECZModPrime ec,int shift) {
		if (ec == null) {
			throw new IllegalArgumentException();
		}
		return new ZModToECZModPrimeEncoder(ec,shift);
	}

	static class ECEncodingFunction
		   extends AbstractFunction<ECEncodingFunction, ZModPrime, ZModElement, ECZModPrime, ECZModElement> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private int shift;
		protected ECEncodingFunction(ZModPrime domain, ECZModPrime coDomain,int shift) {
			super(domain, coDomain);
			this.shift=shift;
		}

		@Override
		protected ECZModElement abstractApply(ZModElement element, RandomByteSequence randomByteSequence) {
			boolean firstOption=true;
			ZModPrime zModPrime=this.getCoDomain().getFiniteField();
			ECZModPrime ecPrime = this.getCoDomain();

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

			if (!zModPrime.contains(e)) {
				throw new ProbabilisticEncodingException(e + " can not be encoded");
			}

			ZModElement x = zModPrime.getElement(e);
			ZModElement stepp = zModPrime.getElement(4);

			int count = 0;
			while (!ecPrime.contains(x)) {
				if (count >= (1 << shift)) {
					throw new ProbabilisticEncodingException(e + " can not be encoded");
				}
				x = x.add(stepp);
				count++;
			}
			
			if(firstOption){
				ZModElement y1 = x.power(3).add(ecPrime.getA().multiply(x)).add(ecPrime.getB());
				ZModElement y = zModPrime.getElement(MathUtil.sqrtModPrime(y1.getValue(), zModPrime.getModulus()));
				return ecPrime.getElement(x, y);
			}
			
			
			element=element.invert();
			msgBitLength=element.getValue().toString(2).length();
			
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
			
			e = element.getValue();
			e = e.shiftLeft(shift+2);
			e=e.add(c);


			
			
			if (!zModPrime.contains(e)) {
				throw new ProbabilisticEncodingException(e + " can not be encoded");
			}

			x = zModPrime.getElement(e);
			

			count = 0;
			while (!ecPrime.contains(x)) {
				if (count >= (1 << shift)) {
					throw new ProbabilisticEncodingException(e + " can not be encoded");
				}
				x = x.add(stepp);
				count++;
			}
			
			
			ZModElement y1 = x.power(3).add(ecPrime.getA().multiply(x)).add(ecPrime.getB());
			ZModElement y = zModPrime.getElement(MathUtil.sqrtModPrime(y1.getValue(), zModPrime.getModulus()));
			y=y.invert();
			return ecPrime.getElement(x, y);
			
			
		}
					

	}

	static class ECDecodingFunction
		   extends AbstractFunction<ECDecodingFunction, ECZModPrime, ECZModElement, ZMod, ZModElement> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private int shift;
		
		protected ECDecodingFunction(ECZModPrime domain, ZMod coDomain,int shift) {
			super(domain, coDomain);
			this.shift=shift;
		}

		@Override
		protected ZModElement abstractApply(ECZModElement element, RandomByteSequence randomByteSequence) {
			ECZModPrime ecPrime = this.getDomain();
			ZModPrime zModPrime=this.getDomain().getFiniteField();
			int msgSpace=zModPrime.getOrder().toString(2).length();
			
			ZModElement x=(ZModElement) element.getX();
			ZModElement y=(ZModElement) element.getY();
			
			ZModElement y1 = x.power(3).add(ecPrime.getA().multiply(x)).add(ecPrime.getB());
			ZModElement yEnc = zModPrime.getElement(MathUtil.sqrtModPrime(y1.getValue(), zModPrime.getModulus()));
			
			BigInteger x1=element.getX().getBigInteger();
			
			
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
			
			if(y.isEquivalent(yEnc)){
				return zModPrime.getElement(x1);
				
			}
			else{
				return zModPrime.getElement(x1).invert();
			}
			
		}

	}

}
