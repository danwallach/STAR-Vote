package crypto;

import java.io.Serializable;

/**
 * An interface for the functionality of proving the validity of an EncryptedVote without decrypting. [NIZK]
 * Created by Matthew Kindy II on 12/1/2014.
 */
public interface IProof extends Serializable {

    /* IProof may be replaced by Provable, in which case there should be a Proof class that implements Provable
    *  or it should be kept and extend Provable */

    /* Should verify need the publicKey or the ciphertexts? It should probably hold them / what it needs for verification */
    public boolean verify(int min, int max);


}
