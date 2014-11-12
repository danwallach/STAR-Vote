package crypto;

import crypto.adder.Vote;
import crypto.exceptions.BadKeyException;
import crypto.exceptions.KeyNotLoadedException;
import crypto.exceptions.UninitialisedException;

import java.io.FileNotFoundException;
import java.security.InvalidKeyException;

/**
 * Created by Matthew Kindy II on 11/9/2014.
 */
public class VoteCrypto {

    private ByteCrypto byteEncrypter;

    public VoteCrypto(ICryptoType cryptoType) {
        byteEncrypter = new ByteCrypto(cryptoType);
    }

    public Vote decrypt(Vote vote) {

    }

    public Vote encrypt(Vote vote) throws UninitialisedException, KeyNotLoadedException, InvalidKeyException, CipherException {

    }

    public void loadKeys(String... filePaths) throws FileNotFoundException, BadKeyException, UninitialisedException {

    }

    public String toString() {
        return "VoteCrypto: " + byteEncrypter.toString();
    }

}
