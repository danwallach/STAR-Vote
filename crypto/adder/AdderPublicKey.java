package crypto.adder;

import crypto.ExponentialElGamalCiphertext;
import crypto.IPublicKey;

import java.io.Serializable;
import java.util.List;
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
public class AdderPublicKey extends AdderKey implements IPublicKey, Serializable {

    private AdderInteger h;

    /**
     * Creates a new PublicKey with the specified parameter values.
     *
     * @param p     the safe prime
     * @param g     the generator of the key
     * @param f     the message base, generator used for homomorphic encryption
     */
    public AdderPublicKey(AdderInteger p, AdderInteger g, AdderInteger f) {
        super(p,g,f);
    }

    /**
     * Creates a new PublicKey with the specified parameter values.
     *
     * @param p     the safe prime
     * @param g     the generator of the key
     * @param h     the public value
     * @param f     the message base, generator used for homomorphic encryption
     */
    public AdderPublicKey(AdderInteger p, AdderInteger g, AdderInteger h, AdderInteger f) {

        super(p,g,f);
        this.h = h;
    }

    /**
     * Creates a new PublicKey with the specified parameter values.
     *
     * @param p     the safe prime
     * @param q     the prime (order of the group)
     * @param g     the generator of the key
     * @param h     the public value
     * @param f     the message base, generator used for homomorphic encryption
     */
    private AdderPublicKey(AdderInteger p, AdderInteger q, AdderInteger g, AdderInteger h, AdderInteger f) {
        super(p,q,g, f);
    }

    /**
     * Encrypts a message as an additive homomorphic Elgamal ciphertext.
     * The ciphertext returned is of the form \f$\langle g^r, h^r
     * f^m\rangle\f$, where \f$m\f$ is the message.
     *
     * @param m     the message
     * @return      the encrypted of the message
     */
    public ExponentialElGamalCiphertext encrypt(AdderInteger m, List<AdderInteger> domain) {
        AdderInteger r = AdderInteger.random(q);
        AdderInteger bigG = g.pow(r);
        AdderInteger bigH = h.pow(r).multiply(f.pow(m));

        EEGMembershipProof proof = new EEGMembershipProof(bigG, bigH, r, this, m, domain);

        return new ExponentialElGamalCiphertext(bigG, bigH, r, p, proof);
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
    public static AdderPublicKey fromString(String s) {
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

            return new AdderPublicKey(p, q, g, h, f);
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
     * We need to test for equality between two keys in some places, just for sanities sake.
     * 
     * @param o - object to test against
     */
    @Override
    public boolean equals(Object o){

        return o instanceof AdderPublicKey && o.toString().equals(toString());

    }
}
