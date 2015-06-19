package crypto;

import crypto.adder.AdderInteger;
import crypto.adder.AdderPublicKey;
import crypto.adder.EEGMembershipProof;

import java.lang.reflect.Constructor;

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

    public static <T extends AHomomorphicCiphertext> T identity(Class<? extends AHomomorphicCiphertext> c, IPublicKey PEK){

        AdderPublicKey publicKey = (AdderPublicKey) PEK;

        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) c;

        /* This has a null proof because it will force return for multiply. Always use this as operand */
        try {
            Constructor<T> constructor = clazz.getConstructor(AdderInteger.class, AdderInteger.class, AdderInteger.class, EEGMembershipProof.class, int.class);
            return constructor.newInstance(AdderInteger.ONE, AdderInteger.ONE, publicKey.getP(), null, 0);
        }
        catch (Exception e) { e.printStackTrace(); }

        return null;

    }
}
