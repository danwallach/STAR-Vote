package crypto.adder;

/**
 * Created by Matthew Kindy II on 1/30/2015.
 */
public abstract class AdderKey {

    protected AdderInteger p;
    protected AdderInteger q;
    protected AdderInteger g;
    protected AdderInteger f;

    protected AdderKey(AdderInteger p, AdderInteger q, AdderInteger g, AdderInteger f) {
        this.p = p;
        this.q = q;
        this.g = g;
        this.f = f;
    }

    protected AdderKey(AdderInteger p, AdderInteger g, AdderInteger f) {
        this(p, p.subtract(AdderInteger.ONE).divide(AdderInteger.TWO), g, f);
    }


}
