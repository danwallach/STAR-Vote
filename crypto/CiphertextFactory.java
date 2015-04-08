package crypto;

import crypto.adder.AdderInteger;
import crypto.adder.AdderPublicKey;

/**
 * Created by Matthew Kindy II on 3/3/2015.
 */
public class CiphertextFactory {

    public static <T extends IHomomorphicCiphertext> T create(Class<T> c, Object... args){
        try {
            return c.getConstructor(args.getClass()).newInstance(args);
        }
        catch (Exception e){ e.printStackTrace(); return null; }
    }


    /* TODO maybe this should return type T...*/
    /* TODO this will eventually reflect a constructor based on c */
    public static <T extends IHomomorphicCiphertext> IHomomorphicCiphertext identity(Class<T> c, IPublicKey PEK){
        if (c == ExponentialElGamalCiphertext.class)
            return new ExponentialElGamalCiphertext(AdderInteger.ONE, AdderInteger.ONE, ((AdderPublicKey)PEK).getP());
        else
            return null;
    }
}
