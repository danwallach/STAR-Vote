package crypto;

import java.io.Serializable;
import java.util.List;

/**
 * An abstract class to organise proofs and what they should minimally do/have
 * Created by Matthew Kindy II on 3/27/2015.
 */
public interface IProof<T extends AHomomorphicCiphertext> extends Serializable {

    /**
     *
     * @param p
     * @param PEK
     * @param domain
     * @return
     */
    public abstract boolean verify(T p, IPublicKey PEK, List<Integer> domain);

    /**
     *
     * @param proof
     * @return
     */
    public abstract IProof<T> operate(IProof<T> proof);

}
