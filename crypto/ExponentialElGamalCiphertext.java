package crypto;

import crypto.adder.AdderInteger;
import crypto.adder.AdderPublicKey;
import crypto.adder.EEGMembershipProof;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matthew Kindy II on 12/1/2014.
 */
public class ExponentialElGamalCiphertext extends AHomomorphicCiphertext<ExponentialElGamalCiphertext> {

    /** A generator for the ElGamal keys, is the generator of the group mod p */
    private AdderInteger g;

    /** The public key component gained by taking g^r, for some private random value r */
    private AdderInteger h;

    /** The random value used as a private key */
    private AdderInteger r;

    /** The prime modulus (order) for group G over which all computations are performed */
    private AdderInteger p;

    /** A proof of the membership of an element to group G */
    private EEGMembershipProof proof;

    public ExponentialElGamalCiphertext(AdderInteger g, AdderInteger h, AdderInteger r, AdderInteger p, EEGMembershipProof proof, int size) {
        super(size);
        this.p = p;
        this.g = new AdderInteger(g, p);
        this.h = new AdderInteger(h, p);
        this.r = r;
        this.proof = proof;
    }

    public ExponentialElGamalCiphertext(AdderInteger g, AdderInteger h, AdderInteger r, AdderInteger p, EEGMembershipProof proof) {
        this(g,h,r,p,proof,1);
    }

    public ExponentialElGamalCiphertext(AdderInteger g, AdderInteger h, AdderInteger p, EEGMembershipProof proof) {
        this(g,h, AdderInteger.ZERO, p, proof,1);
    }

    public ExponentialElGamalCiphertext(AdderInteger g, AdderInteger h, AdderInteger p, EEGMembershipProof proof, int size) {
        this(g,h, AdderInteger.ZERO, p, proof, size);
    }

    /**
     * Multiply this and another ciphertext together. This is accomplished by
     * multiplying them component-wise.
     *
     * @param operand   the ciphertext to "add" yourself to
     * @return          the result of the homomorphic operation on the two ciphertexts
     */
    public ExponentialElGamalCiphertext operate(ExponentialElGamalCiphertext operand, IPublicKey PEK) {
        /* Get the requisite numbers and multiply */
        AdderInteger p = this.p;
        AdderInteger g = this.g.multiply(operand.g);
        AdderInteger h = this.h.multiply(operand.h);
        AdderInteger r = this.r.add(operand.r);

        List<AdderInteger> domain = new ArrayList<>();

        for(int i=0; i<=size; i++){
            domain.add(new AdderInteger(i));
        }

        /* Compute a new proof -- size works because "empty" votes have their own spot, so the sum of 2 votes is always 2, etc. */
        EEGMembershipProof proof = new EEGMembershipProof(g, h, r, (AdderPublicKey) PEK, new AdderInteger(size), domain);

        /* Create a new ciphertext with the updated values and proof */
        return new ExponentialElGamalCiphertext(g, h, r, p, proof, this.size+operand.size);
    }

    /**
     * Returns the generator <tt>g</tt>.
     *
     * @return the generator <tt>g</tt>
     */
    public AdderInteger getG() {
        return g;
    }

    /**
     * Returns the public value <tt>h</tt>.
     *
     * @return the public value <tt>h</tt>
     */
    public AdderInteger getH() {
        return h;
    }

    public int getSize(){
        return size;
    }


    /**
     * Verifies this ciphertext encodes a value between min and max and was encrypted with this PEK
     * @param min
     * @param max
     * @param PEK
     * @return
     */
    public boolean verify(int min, int max, IPublicKey PEK) {

        /* Create the container for the domain */
        List<Integer> domain = new ArrayList<>();

        /* Add in from min to max in the domain */
        for(int i=min; i<=max; i++) {
            domain.add(i);
        }

        /* Call the proof's verify method on the domain */
        return proof.verify(this, PEK, domain);
    }

    public byte[] asBytes() {

        /*byte[] gbytes = g.bigintValue().toByteArray();*/

        return null;
    }
}
