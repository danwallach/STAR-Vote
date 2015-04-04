package crypto;

import java.io.Serializable;
import java.util.List;

/**
 * An abstract class to organise proofs and what they should minimally do/have
 * Created by Matthew Kindy II on 3/27/2015.
 */
public abstract class AProof implements Serializable {
    /* TODO should this be typed? Should this be an interface? */

    /* Fields? */

    /* Verify */
    public abstract boolean verify(Provable p, IPublicKey PEK, List<Integer> domain);

    /* Multiply? Homomorphic Proof? */
}