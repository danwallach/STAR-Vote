package crypto;

import crypto.exceptions.BadKeyException;
import crypto.exceptions.KeyNotLoadedException;
import crypto.exceptions.UninitialisedException;

import java.io.FileNotFoundException;
import java.security.InvalidKeyException;

/**
 * A crypto class used as a black box operating over Votes performing
 * cryptographic functions. Behaviour depends on a specified cryptographic
 * protocol contained within the byteEncrypter field which is set upon construction of
 * BallotCrypto.
 *
 * Created by Matthew Kindy II on 11/9/2014.
 */
public class VoteCrypto {

    private ByteCrypto byteCrypter;

    public VoteCrypto(ICryptoType cryptoType) {
        byteCrypter = new ByteCrypto(cryptoType);
    }

    public PlaintextVote decrypt(EncryptedVote vote) {

        /* Pull out parts from ICiphertext in vote to pass to byteCrypter in order to contstruct new PlaintextVote */
        return null;
    }

    public EncryptedVote encrypt(PlaintextVote vote) throws UninitialisedException, KeyNotLoadedException, InvalidKeyException, CipherException {

        /* Pull out parts needed to create proper ICipherText for the EncryptedVote and pass to byteCrypter to encrypt one by one */
        return null;
    }

    public void loadKeys(String... filePaths) throws FileNotFoundException, BadKeyException, UninitialisedException {
        byteCrypter.loadKeys(filePaths);
    }

    public String toString() {
        return "VoteCrypto: " + byteCrypter.toString();
    }

}
