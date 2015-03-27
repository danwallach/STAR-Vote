package crypto;

import java.io.Serializable;

/**
 * An interface for the functionality of proving the validity of an EncryptedVote without decrypting. [NIZK]
 * Created by Matthew Kindy II on 12/1/2014.
 */
public interface Provable extends Serializable {

    public boolean verify(int min, int max, APublicKey PEK);

}
