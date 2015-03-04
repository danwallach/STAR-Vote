package crypto;

import crypto.adder.AdderInteger;

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
    public static <T extends IHomomorphicCiphertext> IHomomorphicCiphertext identity(Class<T> c, APublicKey PEK){
        if (c == ExponentialElGamalCiphertext.class)
            return new ExponentialElGamalCiphertext(AdderInteger.ONE, AdderInteger.ONE, PEK.getP());
        else
            return null;
    }
}
