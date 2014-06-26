package crypto.adder;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.StringExpression;


/**
 * Represents a vote, and optionally, the corresponding proof.
 * \brief Additive homomorphic Elgamal ciphertext.
 *
 * An Elgamal ciphertext is represented as a pair \f$\langle G, H
 * \rangle = \langle g^r, h^r f^m\rangle \in \mathrm{Z}_p \times \mathrm{Z}_p\f$.
 *
 * To form a ciphertext, you probably want to
 * @see PublicKey#encrypt(AdderInteger) function.
 *
 * @author David Walluck
 * @version $LastChangedRevision$ $LastChangedDate$
 * @since 0.0.1
 */
public class ElgamalCiphertext {

    /** A generator for the ElGamal keys, is the generator of the group mod p */
    private AdderInteger g;

    /** The public key component gained by taking g^r, for some private random value r */
    private AdderInteger h;

    /** The random value used as a private key */
    private AdderInteger r;

    /** The prime modulus (order) for group G over which all computations are performed */
    private AdderInteger p;

    /** A proof of the membership of an element to group G */
    private MembershipProof proof;

    /**
     * Creates a new ElgamalCiphertext with the specified parameter values.
     *
     * @param p         the prime / modulus
     * @param g         the generator, first component of the ciphertext
     * @param h         the public value, second component of the ciphertext
     */
    public ElgamalCiphertext(AdderInteger g, AdderInteger h, AdderInteger p) {
        this.p = p;
        this.g = new AdderInteger(g, p);
        this.h = new AdderInteger(h, p);
        this.r = AdderInteger.ZERO;
    }

    /**
     * Creates a new ElgamalCiphertext with the specified parameter values.
     *
     * @param p         the prime / modulus
     * @param g         the generator, first component of the ciphertext
     * @param h         the public value, second component of the ciphertext
     * @param r         the private random value, random component of the ciphertext
     */
    public ElgamalCiphertext(AdderInteger g, AdderInteger h, AdderInteger r, AdderInteger p) {

        this.p = p;
        this.g = new AdderInteger(g, p);
        this.h = new AdderInteger(h, p);
        this.r = r;
    }

    /**
     * Returns the short hash of this vote, ignoring the ballot proof.
     *
     * @return          the short hash
     */
    public String shortHash() {

        String str = toString();
        int idx = str.indexOf(" ");

        if (idx != -1)
            str = str.substring(0, idx);

        return Util.sha1(str).substring(0, 5);
    }


    /**
     * Multiply this and another ciphertext together. This is accomplished by
     * multiplying them component-wise.
     *
     * @param ciphertext        the ciphertext to multiply against this
     * @return                  the product of the two ciphertexts.
     */
    ElgamalCiphertext multiply(ElgamalCiphertext ciphertext) {

        /* Get the requisite numbers and multiply */
        AdderInteger p = this.getP();
        AdderInteger g = this.getG().multiply(ciphertext.getG());
        AdderInteger h = this.getH().multiply(ciphertext.getH());
        AdderInteger r = this.getR().add(ciphertext.getR());

        /* Create a new ciphertext */

        return new ElgamalCiphertext(g, h, r, p);
    }

    /**
     * Returns the prime <tt>p</tt>.
     *
     * @return the prime <tt>p</tt>
     */
    public AdderInteger getP() {
        return p;
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

    /**
     * Returns the private random value <tt>r</tt>.
     *
     * @return the private random value <tt>r</tt>
     */
    public AdderInteger getR() {
        return r;
    }

    /**
     * Returns the proof associated with this vote.
     *
     * @return the proof
     */
    public MembershipProof getProof() {
        return proof;
    }

    /**
     * Sets the proof to the given proof.
     *
     * @param proof         the proof
     */
    public void setProof(MembershipProof proof) {
        this.proof = proof;
    }

    /**
     * Creates a <tt>ElgamalCiphertext</tt> from the string standard representation as
     * described in the {@link #toString} method.
     *
     * @param  s        a string that specifies a <tt>ElgamalCiphertext</tt>
     * @return a        <tt>ElgamalCiphertext</tt> with the specified values
     */
    public static ElgamalCiphertext fromString(String s) {

        /* Set up the StringTokenizer */
        StringTokenizer st0 = new StringTokenizer(s, " ");

        try {

            /* Set up another StringTokenizer for regex-ing 'p', 'G', and 'H' */
            StringTokenizer st = new StringTokenizer(st0.nextToken(), "pGH", true);

            /* Error if not 'p' */
            if (!st.nextToken().equals("p"))
                throw new InvalidElgamalCiphertextException("expected token: `p\'");

            /* Get p */
            AdderInteger p = new AdderInteger(st.nextToken());

            /* Error if not 'G' */
            if (!st.nextToken().equals("G"))
                throw new InvalidElgamalCiphertextException("expected token: `G\'");

            /* Get g */
            AdderInteger g = new AdderInteger(st.nextToken(), p);

            /* Error if not 'H' */
            if (!st.nextToken().equals("H"))
                throw new InvalidElgamalCiphertextException("expected token: `H\'");

            /* Get h */
            AdderInteger h = new AdderInteger(st.nextToken(), p);

            /* Error if too many tokens */
            if (st.hasMoreTokens())
                throw new InvalidElgamalCiphertextException("too many tokens");

            /* Create a new vote from g, h, p */
            ElgamalCiphertext vote = new ElgamalCiphertext(g, h, p);

            /* If there are more tokens for the other one, look for the vote proof */
            if (st0.hasMoreTokens())
                try { vote.setProof(MembershipProof.fromString(st0.nextToken())); }
                catch (InvalidMembershipProofException ibpe) { throw new InvalidElgamalCiphertextException(ibpe.getMessage()); }

            /* Error if there are still more tokens */
            if (st0.hasMoreTokens())
                throw new InvalidElgamalCiphertextException("too many tokens");

            return vote;

        }
        catch (NoSuchElementException | NumberFormatException nsee) { throw new InvalidElgamalCiphertextException(nsee.getMessage()); }
    }

    /**
     * Returns a <code>String</code> object representing this
     * <code>ElgamalCiphertext</code>.
     *
     * @return      the string representation of this vote
     */
    public String toString() {

        /* Create a new StringBuffer */
        StringBuilder sb = new StringBuilder(4096);

        /* Build the pGH String representing ciphertext */
        sb.append("p");
        sb.append(p);
        sb.append("G");
        sb.append(g);
        sb.append("H");
        sb.append(h);

        /* Add the proof if it exists */
        if (proof != null) {
            sb.append(" ");
            sb.append(proof.toString());
        }

        return sb.toString();
    }
    
    /**
     * Method for interop with VoteBox's S-Expression system.
     * 
     * @return      the S-Expression equivalent of this AdderInteger
     */
    public ASExpression toASE() {

        StringExpression label = StringExpression.makeString("elgamal-ciphertext");

        /* TODO check if this can be simplified to just proofString = proof == null ? "" : proof.toASE() */
        return (proof == null) ? new ListExpression(label, p.toASE(), g.toASE(), h.toASE()) :
    	                         new ListExpression(label, p.toASE(), g.toASE(), h.toASE(), proof.toASE());
    }
    
    /**
     * Method for interop with VoteBox's S-Expression system.
     * 
     * @param ASE       S-Expression representation of an ElgamalCiphertext
     * @return          the ElgamalCiphertext equivalent of ase
     */
    public static ElgamalCiphertext fromASE(ASExpression ASE){

        /* Cast ASE to a ListExpression */
    	ListExpression list = (ListExpression)ASE;

        /* Check the size of the ListExpression */
    	if(list.size() != 4 && list.size() != 5)
    		throw new RuntimeException("Not an elgamal-ciphertext");

        /* Check the label of the ListExpression */
    	if(!list.get(0).toString().equals("elgamal-ciphertext"))
    		throw new RuntimeException("Not an elgamal-ciphertext");

        /* Extract the numbers */
    	AdderInteger p = AdderInteger.fromASE(list.get(1));
    	AdderInteger g = AdderInteger.fromASE(list.get(2));
    	AdderInteger h = AdderInteger.fromASE(list.get(3));
    	MembershipProof proof = null;

        /* Expect a proof if of size 5 -- then extract it */
    	if(list.size() == 5)
    		proof = MembershipProof.fromASE(list.get(4));

        /* Create a new ciphertext from the numbers */
    	ElgamalCiphertext text = new ElgamalCiphertext(g,h,p);

        /* Set the proof if we got one */
    	if(proof != null)
    		text.setProof(proof);
    	
    	return text;
    }
}
