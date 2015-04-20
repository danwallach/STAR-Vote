package crypto.adder;

import crypto.ExponentialElGamalCiphertext;
import crypto.IPublicKey;
import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.StringExpression;

import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Elgamal public key.
 *
 * The main component of a public key is the value \f$h \in
 * \langle g \rangle\f$.  To create a public key from scratch, you
 * probably first want to generate a safe prime and then generate
 * a key pair. As an example:
 *
 * \code
 * // Create a context object for random number generation.
 * Context ctx;
 *
 * // Create an empty public key.
 * PublicKey pub_key(&ctx);
 *
 * // Generate a prime of length 1024 and a generator.
 * pub_key.make_partial_key(1024);
 *
 * // Create the public key and return the corresponding private key.
 * PrivateKey priv_key = pub_key.gen_key_pair();
 * \endcode
 *
 * @author David Walluck
 * @version $LastChangedRevision$ $LastChangedDate$
 * @since 0.0.1
 */
public class AdderPublicKeyShare extends AdderKey implements IPublicKey, Serializable {

    private AdderInteger h;

    /**
     * Creates a new PublicKey with the specified parameter values.
     *
     * @param p     the prime
     * @param g     the generator of the key
     * @param f     the message base, generator used for homomorphic encryption
     */
    public AdderPublicKeyShare(AdderInteger p, AdderInteger g, AdderInteger f) {
        super(p,g,f);
    }

    /**
     * Creates a new PublicKey with the specified parameter values.
     *
     * @param p     the prime
     * @param g     the generator of the key
     * @param h     the public value
     * @param f     the message base, generator used for homomorphic encryption
     */
    public AdderPublicKeyShare(AdderInteger p, AdderInteger g, AdderInteger h, AdderInteger f) {

        super(p,g,f);
        this.h = h;
    }

    /**
     * Creates a new PublicKey with the specified parameter values.
     *
     * @param p     the prime
     * @param q     the order of the group
     * @param g     the generator of the key
     * @param h     the public value
     * @param f     the message base, generator used for homomorphic encryption
     */
    private AdderPublicKeyShare(AdderInteger p, AdderInteger q, AdderInteger g, AdderInteger h, AdderInteger f) {
        super(p,q,g, f);
    }

    /**
     * Generates key parameters \f$g, f\f$ given a prime \f$p\f$.
     *
     * @param p     the prime
     * @return      the public key
     */
   public static AdderPublicKeyShare makePublicKeyShare(AdderInteger p) {

        AdderInteger t;
        AdderInteger a;

        do {
            t = AdderInteger.random(p);
        } while (t.compareTo(AdderInteger.ONE) <= 0);

        AdderInteger g = t.pow(AdderInteger.TWO);

        AdderInteger q = p.subtract(AdderInteger.ONE).divide(AdderInteger.TWO);

        do {
            a = AdderInteger.random(q);
        } while (a.compareTo(AdderInteger.ONE) <= 0);

        AdderInteger f = g.pow(a);

        return new AdderPublicKeyShare(p, q, g, null, f);
    }

    /**
     * Generates key parameters \f$p, g, f\f$ given a key length.
     * This function generates a safe prime \f$p\f$.
     *
     * @param length        the length of the key in bits.  That is, \f$p\f$ will
     *                      be chosen to be a \e length - bit prime number.
     * @return              the public key
     */
    public static AdderPublicKeyShare makePublicKeyShare(int length) {
        return makePublicKeyShare(AdderInteger.safePrime(length));
    }

    /**
     * Creates the corresponding private key of this public key.
     *
     * @return      the private key
     */
    public AdderPrivateKeyShare genKeyPair() {
        AdderInteger x = AdderInteger.random(q);

        this.h = g.pow(x);

        return new AdderPrivateKeyShare(p, g, x, f);
    }


    /**
     * Encrypts a polynomial value destined for an authority. The
     * ciphertext returned is of the form \f$\langle g^r, h^r
     * (m+1)^2 \rangle\f$, where \f$m\f$ is the value of the source's
     * polynomial evaluated at the ID of the destination.
     *
     * @param m         the destination authority's ID
     * @return          the encrypted ID
     */
    public ExponentialElGamalCiphertext encryptPoly(AdderInteger m) {

        AdderInteger r = AdderInteger.random(q);
        AdderInteger bigG = g.pow(r);
        AdderInteger mPlusOne = new AdderInteger(m.add(AdderInteger.ONE), p);
        AdderInteger bigH = h.pow(r).multiply(mPlusOne.pow(AdderInteger.TWO));

        return new ExponentialElGamalCiphertext(bigG, bigH, r, p, null);
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
     * Returns the sub-prime <tt>q</tt>.
     *
     * @return the sub-prime <tt>q</tt>
     */
    public AdderInteger getQ() {
        return q;
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
     * Returns the message base <tt>f</tt>.
     *
     * @return the message base <tt>f</tt>
     */
    public AdderInteger getF() {
        return f;
    }

    /**
     * Creates a <tt>PublicKey</tt> from the string standard representation as
     * described in the {@link #toString} method.
     *
     * @param  s a string that specifies a <tt>PublicKey</tt>
     * @return a <tt>PublicKey</tt> with the specified values
     */
    public static AdderPublicKeyShare fromString(String s) {
        StringTokenizer st = new StringTokenizer(s, "pghf", true);

        try {
            if (!st.nextToken().equals("p")) {
                throw new InvalidPublicKeyException("expected token: `p\'");
            }

            AdderInteger p = new AdderInteger(st.nextToken());
            AdderInteger q = p.subtract(AdderInteger.ONE)
                              .divide(AdderInteger.TWO);

            if (!st.nextToken().equals("g")) {
                throw new InvalidPublicKeyException("expected token: `g\'");
            }

            AdderInteger g = new AdderInteger(st.nextToken(), p);

            if (!st.nextToken().equals("h")) {
                throw new InvalidPublicKeyException("expected token: `h\'");
            }

            AdderInteger h = new AdderInteger(st.nextToken(), p);

            if (!st.nextToken().equals("f")) {
                throw new InvalidPublicKeyException("expected token: `f\'");
            }

            AdderInteger f = new AdderInteger(st.nextToken(), p);

            if (st.hasMoreTokens()) {
                throw new InvalidPublicKeyException("too many tokens");
            }

            return new AdderPublicKeyShare(p, q, g, h, f);
        } catch (NoSuchElementException | NumberFormatException nsee) {
            throw new InvalidPublicKeyException(nsee.getMessage());
        }
    }

    /**
     * Returns a <code>String</code> object representing this
     * <code>PublicKey</code>.
     * @return the string representation of this public key
     */
    public String toString() {
        StringBuilder sb = new StringBuilder(4096);

        sb.append("p");
        sb.append(p.toString());
        sb.append("g");
        sb.append(g.toString());
        sb.append("h");

        if (h != null) {
            sb.append(h.toString());
        } else {
            sb.append("0");
        }

        sb.append("f");
        sb.append(f.toString());

        return sb.toString();
    }
    
    /**
     * Method for interop with VoteBox's S-Expression system.
     * 
     * @return the S-Expression equivalent of this PublicKey
     */
    public ASExpression toASE(){
    	AdderInteger ourH = h;
    	
    	if(ourH == null) ourH = AdderInteger.ZERO;
    	
    	return new ListExpression(
    			StringExpression.makeString("public-key"),
    			p.toASE(),
    			g.toASE(),
    			ourH.toASE(),
    			f.toASE());
    }
    
    /**
     * Method for interop with VoteBox's S-Expression system.
     * 
     * @param ase - S-Expression representation of a PublicKey
     * @return the PublicKey equivalent of ase
     */
    public static AdderPublicKeyShare fromASE(ASExpression ase){
    	ListExpression exp = (ListExpression)ase;
    	if(!(exp.get(0).toString()).equals("public-key"))
    		throw new RuntimeException("Not public-key");
    	
    	AdderInteger p = AdderInteger.fromASE(exp.get(1));
    	AdderInteger g = AdderInteger.fromASE(exp.get(2));
    	AdderInteger h = AdderInteger.fromASE(exp.get(3));
    	AdderInteger f = AdderInteger.fromASE(exp.get(4));
    	
    	AdderInteger q = p.subtract(AdderInteger.ONE).divide(AdderInteger.TWO);
    	
    	return new AdderPublicKeyShare(p, q, g, h, f);
    }
    
    /**
     * We need to test for equality between two keys in some places, just for sanities sake.
     * 
     * @param o - object to test against
     */
    @Override
    public boolean equals(Object o){

        return o instanceof AdderPublicKeyShare && o.toString().equals(toString());

    }
}
