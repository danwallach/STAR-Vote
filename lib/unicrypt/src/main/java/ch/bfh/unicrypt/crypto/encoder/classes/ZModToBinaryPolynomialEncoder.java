package lib.unicrypt.src.main.java.ch.bfh.unicrypt.crypto.encoder.classes;

import java.math.BigInteger;
import java.nio.ByteOrder;

import ch.bfh.unicrypt.crypto.encoder.abstracts.AbstractEncoder;
import ch.bfh.unicrypt.helper.array.classes.ByteArray;
import ch.bfh.unicrypt.helper.converter.classes.bytearray.BigIntegerToByteArray;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.PolynomialElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.PolynomialField;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZMod;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModElement;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModTwo;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.abstracts.AbstractFunction;
import lib.unicrypt.src.main.java.ch.bfh.unicrypt.math.function.interfaces.Function;
import ch.bfh.unicrypt.random.interfaces.RandomByteSequence;

/**
 * Encodes a ZModElement as a binary polynomial by taking the bit-array representation of the ZModElement to create the
 * polynomial
 * <p>
 * @author lutzch
 * <p>
 */
public class ZModToBinaryPolynomialEncoder
	   extends AbstractEncoder<ZMod, ZModElement, PolynomialField<ZModTwo>, PolynomialElement<ZModTwo>> {

	private final ZMod zMod;
	private final PolynomialField<ZModTwo> binaryPolynomial;
	private final BigIntegerToByteArray converter;

	public ZModToBinaryPolynomialEncoder(ZMod zMod, PolynomialField<ZModTwo> binaryPolynomial) {
		this.zMod = zMod;
		this.binaryPolynomial = binaryPolynomial;
		this.converter = BigIntegerToByteArray.getInstance(ByteOrder.LITTLE_ENDIAN);
	}

	@Override
	protected Function abstractGetEncodingFunction() {
		return new EncodingFunction(this.zMod, this.binaryPolynomial);
	}

	@Override
	protected Function abstractGetDecodingFunction() {
		return new DecodingFunction(this.binaryPolynomial, this.zMod);
	}

	public static ZModToBinaryPolynomialEncoder getInstance(ZMod zMod, PolynomialField<ZModTwo> polynomialField) {
		if (zMod == null || polynomialField == null) {
			throw new IllegalArgumentException();
		}
		return new ZModToBinaryPolynomialEncoder(zMod, polynomialField);
	}

	class EncodingFunction
		   extends AbstractFunction<EncodingFunction, ZMod, ZModElement, PolynomialField<ZModTwo>, PolynomialElement<ZModTwo>> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		protected EncodingFunction(final ZMod domain, final PolynomialField<ZModTwo> coDomain) {
			super(domain, coDomain);
		}

		@Override
		protected PolynomialElement<ZModTwo> abstractApply(final ZModElement element, final RandomByteSequence randomByteSequence) {
			ByteArray byteArray = converter.convert(element.getValue());
			return this.getCoDomain().getElement(byteArray);

//			final BigInteger value = element.getValue();
//			final BinaryPolynomialField coDomain = this.getCoDomain();
//			return coDomain.getElementFromBitString(value.toString(2));
		}

	}

	class DecodingFunction
		   extends AbstractFunction<DecodingFunction, PolynomialField<ZModTwo>, PolynomialElement<ZModTwo>, ZMod, ZModElement> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		protected DecodingFunction(final PolynomialField<ZModTwo> domain, final ZMod coDomain) {
			super(domain, coDomain);
		}

		@Override
		protected ZModElement abstractApply(final PolynomialElement<ZModTwo> element, final RandomByteSequence randomByteSequence) {
			ByteArray byteArray = element.getValue().getCoefficients();
			BigInteger bigInteger = converter.reconvert(byteArray);
			return this.getCoDomain().getElement(bigInteger.mod(this.getCoDomain().getModulus()));

//			final String value = new StringBuilder(arr.toBitString()).reverse().toString();
//			BigInteger res = new BigInteger(value, 2);
//			return this.getCoDomain().getElement(res);
		}

	}

}
