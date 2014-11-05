package crypto;

import crypto.exceptions.BadKeyException;
import crypto.exceptions.KeyNotLoadedException;
import crypto.exceptions.UninitialisedException;

import java.io.FileNotFoundException;
import java.security.InvalidKeyException;

/**
 * A crypto class used as a black box operating over byte arrays performing
 * cryptographic functions. Behaviour depends on a specified cryptographic
 * protocol contained within the cryptoType field.
 *
 * Created by Matthew Kindy II and Matt Bernhard on 11/3/2014.
 */
public class ByteCrypto {

    public static ByteCrypto SINGLETON = new ByteCrypto();

    private ICryptoType cryptoType;

    private ByteCrypto(){
    }

    public byte[] decrypt(byte[] cipherText) throws UninitialisedException, KeyNotLoadedException, InvalidKeyException, CipherException {

        if (cryptoType != null)
            return cryptoType.decrypt(cipherText);
        else throw new UninitialisedException("The crypto type has not yet been loaded.");
    }

    public byte[] encrypt(byte[] plainText) throws UninitialisedException, KeyNotLoadedException, InvalidKeyException, CipherException {

        if (cryptoType != null)
                return cryptoType.encrypt(plainText);
        else throw new UninitialisedException("The crypto type has not yet been loaded.");
    }

    public void loadKeys(String... filePaths) throws FileNotFoundException, BadKeyException, UninitialisedException {

        if (cryptoType != null) {
                cryptoType.loadKeys(filePaths);
        }
        else throw new UninitialisedException("The crypto type has not yet been loaded.");
    }

    public void setCryptoType(ICryptoType cryptoType) throws UninitialisedException{

        if (cryptoType != null)
            this.cryptoType = cryptoType;
        else
            throw new UninitialisedException("The crypto type was not properly loaded!");
    }

    public String toString() {
        return "CryptoType: " + cryptoType.toString();
    }

}
