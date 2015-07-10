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
        this(g,h, AdderInteger.ZERO, p, proof, 1);
    }

    public ExponentialElGamalCiphertext(AdderInteger g, AdderInteger h, AdderInteger p, EEGMembershipProof proof, int size) {
        this(g,h, AdderInteger.ZERO, p, proof, size);
    }

    /**
     * Multiply this and another ciphertext from the same choice set together. This is accomplished by
     * multiplying them component-wise. This makes use of assumptions regarding the
     * origin of the ciphertexts -- specifically that they come from the same RaceSelection
     *
     * @param operands  the ciphertext to "add" yourself to
     * @return          the result of the homomorphic operation on the two ciphertexts
     */
    public ExponentialElGamalCiphertext operateDependent(List<ExponentialElGamalCiphertext> operands, IPublicKey PEK) {

        /* Get the requisite numbers and multiply */
        AdderInteger p = this.p;
        AdderInteger g = this.g;
        AdderInteger h = this.h;
        AdderInteger r = this.r;


        for (ExponentialElGamalCiphertext operand : operands) {
            g.multiply(operand.g);
            h.multiply(operand.g);
            r.add(operand.r);
        }

        /* Max to deal with identity which has size 0 */
        Integer sameSize = operands.get(0).size;
        List<AdderInteger> domain = new ArrayList<>();

        /* For dependent, when these are all added together, we get*/
        for(int i=sameSize; i<=sameSize; i++){
            domain.add(new AdderInteger(i));
        }

        /* Compute a new proof -- size works because abstention votes have their own "candidate", so a RaceSelection
         * of size 1 always has exactly 1 selection, size 2 always has exactly 2 selections , etc. Note that this
         * will NOT work unless these all come from the same RaceSelection
         */
        EEGMembershipProof proof = new EEGMembershipProof(g, h, r, (AdderPublicKey) PEK, new AdderInteger(sameSize), domain);


        System.out.println("Current size: " + (sameSize));
        System.out.println("Current domain: " + domain);

        /* Create a new ciphertext with the updated values and proof */
        return new ExponentialElGamalCiphertext(g, h, r, p, proof, sameSize);
    }

    /**
     * Multiply this and another ciphertext from different choice sets together. This is accomplished by
     * multiplying them component-wise. This makes use of assumptions regarding the
     * origin of the ciphertexts -- specifically that they come from different RaceSelections.
     *
     * @param operand   the ciphertext to "add" yourself to
     * @return          the result of the homomorphic operation on the two ciphertexts
     */
    public ExponentialElGamalCiphertext operateIndependent(ExponentialElGamalCiphertext operand, IPublicKey PEK) {
        /* Get the requisite numbers and multiply */
        AdderInteger p = this.p;
        AdderInteger g = this.g.multiply(operand.g);
        AdderInteger h = this.h.multiply(operand.h);
        AdderInteger r = this.r.add(operand.r);

        List<AdderInteger> domain1 = new ArrayList<>();
        List<AdderInteger> domain2 = new ArrayList<>();
        List<AdderInteger> newDomain = new ArrayList<>();

        for(int i=0; i<=this.size; i++){
            domain1.add(new AdderInteger(i));
        }

        for(int i=0; i<=operand.size; i++){
            domain2.add(new AdderInteger(i));
        }

        for(int i=0; i<=this.size+operand.size; i++){
            newDomain.add(new AdderInteger(i));
        }

        /* Compute a new proof for the correct value */
        EEGMembershipProof proof = new EEGMembershipProof(  this,       this.r,     domain1,
                                                            operand,    operand.r,  domain2,
                                                            newDomain,  (AdderPublicKey) PEK);

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

    public EEGMembershipProof getProof(){
        return proof;
    }

    /**
     * Verifies this ciphertext encodes a value between min and max and was encrypted with this PEK
     * @param min   the minimum acceptable value for this ciphertext
     * @param max   the maximum acceptable value for this ciphertext
     * @param PEK   the public encryption key used in the verification process
     *
     * @return      true if this ciphertext encrypts a value between min and max, false otherwise
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

}
