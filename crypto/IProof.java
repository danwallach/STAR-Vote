package crypto;

import java.io.Serializable;
import java.util.List;

/**
 * An abstract class to organise proofs and what they should minimally do/have
 * Created by Matthew Kindy II on 3/27/2015.
 */
public interface IProof<T extends AHomomorphicCiphertext> extends Serializable {

    /**
     * Verifies that the AHomomorphic ciphertext given satisfies the IProof over the given domain
     * @param c         the ciphertext against which the IProof checks
     * @param PEK       the public encryption key
     * @param domain    the domain over which to check the IProof
     *
     * @return          true if the IProof verifies, false otherwise
     */
    public abstract boolean verify(T c, IPublicKey PEK, List<Integer> domain);

}
