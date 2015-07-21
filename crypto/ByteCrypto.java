package crypto;

import crypto.exceptions.CiphertextException;
import crypto.exceptions.KeyNotLoadedException;

import java.io.FileNotFoundException;
import java.security.InvalidKeyException;

/**
 * A crypto class used as a black box operating over byte arrays performing
 * cryptographic functions. Behaviour depends on a specified cryptographic
 * protocol contained within the cryptoType field which is set upon construction of
 * BallotCrypto.
 *
 * Created by Matthew Kindy II and Matt Bernhard on 11/3/2014.
 */
public class ByteCrypto<T extends AHomomorphicCiphertext<T>> {

    private ICryptoType<T> cryptoType;

    public ByteCrypto(ICryptoType<T> cryptoType){
        this.cryptoType = cryptoType;
    }

    /**
     * Decrypts an AHomomorphicCiphertext.
     *
     * @param cipherText    the encrypted plaintext for a single vote-candidate value
     *
     * @return              the decrypted ciphertext as a byte[]
     *
     * @throws KeyNotLoadedException
     * @throws InvalidKeyException
     * @throws CipherException
     * @throws CiphertextException
     */
    public byte[] decrypt(T cipherText) throws KeyNotLoadedException, InvalidKeyException, CipherException, CiphertextException {

        return cryptoType.decrypt(cipherText);
    }

    /**
     * Encrypts the plaintext for a single vote-candidate value.
     *
     * @param plainText     the plaintext for a single vote-candidate value
     *
     * @return              the encrypted ciphertext as an AHomomorphicCiphertext
     *
     * @throws KeyNotLoadedException
     * @throws InvalidKeyException
     * @throws CipherException
     */
    public T encrypt(byte[] plainText) throws KeyNotLoadedException, InvalidKeyException, CipherException {

        return cryptoType.encrypt(plainText);
    }

    /**
     * Loads the keys from the files specified by the filePaths
     *
     * @param filePaths     the file paths of the files from which the keys will be loaded
     * @see crypto.ICryptoType#loadAllKeys(String[])
     *
     * @throws FileNotFoundException
     */
    public void loadKeys(String... filePaths) throws FileNotFoundException {

         cryptoType.loadAllKeys(filePaths);
    }

    public String toString() {
        return "CryptoType: " + cryptoType.toString();
    }

}
