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
        this.p = p;
        this.g = new AdderInteger(g, p);
        this.h = new AdderInteger(h, p);
        this.r = r;
    }

    public ExponentialElGamalCiphertext(AdderInteger g, AdderInteger h, AdderInteger p) {
        this(g,h, AdderInteger.ZERO, p);
    }

    /**
     *
     * @param operand The ciphertext to "add" yourself to
     * @return
     */
    public ExponentialElGamalCiphertext operate(ExponentialElGamalCiphertext operand) {
        return this.multiply(operand);
    }

    /**
     * Multiply this and another ciphertext together. This is accomplished by
     * multiplying them component-wise.
     *
     * @param ciphertext        the ciphertext to multiply against this
     * @return                  the product of the two ciphertexts.
     */
    private ExponentialElGamalCiphertext multiply(ExponentialElGamalCiphertext ciphertext) {

        /* Get the requisite numbers and multiply */
        AdderInteger p = this.p;
        AdderInteger g = this.g.multiply(ciphertext.g);
        AdderInteger h = this.h.multiply(ciphertext.h);
        AdderInteger r = this.r.add(ciphertext.r);

        /* Create a new ciphertext */
        return new ExponentialElGamalCiphertext(g, h, r, p);
    }

    /**
     * Returns the proof associated with this vote.
     *
     * @return the proof
     */
    public IProof getProof() {
        return proof;
    }

    /**
     * Sets the proof to the given proof.
     *
     * @param proof         the proof
     */
    public void setProof(IProof proof) {
        this.proof = proof;
    }

    public byte[] asBytes() {

        /*byte[] gbytes = g.bigintValue().toByteArray();*/

        return null;
    }
}
