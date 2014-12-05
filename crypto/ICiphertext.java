package crypto;

import java.io.Serializable;

/**
 * An interface for different Ciphertext classes so that basic functionality of
 * ciphertexts is maintained.
 *
 * @author Matthew Kindy II, Matt Bernhard
 */
public interface ICiphertext extends Serializable {

    /**
     * @return the data contained in this ciphertext
     */
    public byte[] asBytes();

}
