package crypto;

import java.io.Serializable;

/**
 * An interface for the functionality of proving the validity of an EncryptedVote without decrypting. [NIZK]
 * Created by Matthew Kindy II on 12/1/2014.
 */
public interface Provable extends Serializable {

    /**
     * Verifies that the Provable is within a range [min, max]
     *
     * @param min       the minimum acceptable value
     * @param max       the maximum acceptable value
     * @param PEK       the public encryption key used in verifying the Provable
     *
     * @return          true if the Provable is within range
     */
    public boolean verify(int min, int max, IPublicKey PEK);

}
