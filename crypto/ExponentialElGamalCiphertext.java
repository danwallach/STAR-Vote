package crypto;

import crypto.adder.AdderInteger;

/**
 * Created by Matthew Kindy II on 12/1/2014.
 */
public class ExponentialElGamalCiphertext implements IHomomorphicCiphertext<ExponentialElGamalCiphertext> {

    /** A generator for the ElGamal keys, is the generator of the group mod p */
    private AdderInteger g;

    /** The public key component gained by taking g^r, for some private random value r */
    private AdderInteger h;

    /** The random value used as a private key */
    private AdderInteger r;

    /** The prime modulus (order) for group G over which all computations are performed */
    private AdderInteger p;

    /** A proof of the membership of an element to group G */
    private IProof proof;

    public ExponentialElGamalCiphertext(AdderInteger g, AdderInteger h, AdderInteger r, AdderInteger p) {
        this.g = g;
        this.h = h;
        this.r = r;
        this.p = p;
    }

    public ExponentialElGamalCiphertext operate(ExponentialElGamalCiphertext operand) {
        return null;
    }

    public byte[] asBytes() {

        byte[] gbytes = g.bigintValue().toByteArray();
    }
}
