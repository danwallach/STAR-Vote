package crypto;

import crypto.adder.AdderInteger;
import crypto.adder.AdderPublicKey;
import crypto.adder.EEGMembershipProof;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matthew Kindy II on 3/3/2015.
 */
public class CiphertextFactory {

    public static <T extends AHomomorphicCiphertext<T>> T create(Class<T> c, Object... args){
        try {
            return c.getConstructor(args.getClass()).newInstance(args);
        }
        catch (Exception e){ e.printStackTrace(); return null; }
    }

    public static AHomomorphicCiphertext identity(Class<? extends AHomomorphicCiphertext> c, IPublicKey PEK){

        AdderPublicKey publicKey = (AdderPublicKey) PEK;

        /* This has a null proof because it will force return for multiply. Always use this as operand */
        try {

            List<AdderInteger> zeroDomain = new ArrayList<>();
            zeroDomain.add(AdderInteger.ZERO);

            EEGMembershipProof proof = new EEGMembershipProof(AdderInteger.ONE, AdderInteger.ONE, AdderInteger.ZERO, publicKey, AdderInteger.ZERO, zeroDomain);

            Constructor<? extends AHomomorphicCiphertext> constructor = c.getConstructor(AdderInteger.class, AdderInteger.class, AdderInteger.class, EEGMembershipProof.class, int.class);
            return constructor.newInstance(AdderInteger.ONE, AdderInteger.ONE, publicKey.getP(), proof, 0);
        }
        catch (Exception e) { e.printStackTrace(); }

        return null;

    }
}
