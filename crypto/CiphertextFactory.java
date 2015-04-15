package crypto;

import crypto.adder.AdderInteger;
import crypto.adder.AdderPublicKey;

/**
 * Created by Matthew Kindy II on 3/3/2015.
 */
public class CiphertextFactory {

    public static <T extends AHomomorphicCiphertext> T create(Class<T> c, Object... args){
        try {
            return c.getConstructor(args.getClass()).newInstance(args);
        }
        catch (Exception e){ e.printStackTrace(); return null; }
    }

    /* TODO this will eventually reflect a constructor based on c */
    public static <T extends AHomomorphicCiphertext> AHomomorphicCiphertext identity(Class<T> c, IPublicKey PEK){
        if (c == ExponentialElGamalCiphertext.class) {

            AdderPublicKey publicKey = (AdderPublicKey) PEK;

            /* This has a null proof because it will force return for multiply. Always use this as operand */
            return new ExponentialElGamalCiphertext(AdderInteger.ONE, AdderInteger.ONE, publicKey.getP(), null, 0);
        } else
            return null;
    }
}
