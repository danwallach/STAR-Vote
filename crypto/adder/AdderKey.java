package crypto.adder;

/**
 * Abstract class for Adder system based key.
 * Created by Matthew Kindy II on 1/30/2015.
 */
public abstract class AdderKey {

    protected AdderInteger p;
    protected AdderInteger q;
    protected AdderInteger g;
    protected AdderInteger f;

    /**
     * @param p     the safe prime (2q+1)
     * @param q     the prime (order of the group)
     * @param g     the generator
     * @param f     the message base
     */
    protected AdderKey(AdderInteger p, AdderInteger q, AdderInteger g, AdderInteger f) {
        this.p = p;
        this.q = q;
        this.g = g;
        this.f = f;
    }

    /**
     * Follows protocol for safe-prime/prime as in Adder paper
     * @param p     the safe prime
     * @param g     the generator
     * @param f     the message base
     */
    protected AdderKey(AdderInteger p, AdderInteger g, AdderInteger f) {
        this(p, p.subtract(AdderInteger.ONE).divide(AdderInteger.TWO), g, f);
    }


}
