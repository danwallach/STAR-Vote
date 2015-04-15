package crypto;

import crypto.adder.*;
import crypto.exceptions.BadKeyException;
import crypto.exceptions.CiphertextException;
import crypto.exceptions.KeyNotLoadedException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Matthew Kindy II on 11/5/2014.
 */
public class DHExponentialElGamalCryptoType implements ICryptoType {

    private AdderPrivateKeyShare[] privateKeyShares;
    private AdderPublicKey PEK;

    /**
     * @see crypto.ICryptoType#decrypt(IHomomorphicCiphertext)
     */
    public byte[] decrypt(IHomomorphicCiphertext ciphertext) throws InvalidKeyException, KeyNotLoadedException, CipherException, CiphertextException {

        /* Check if this is the right type of IHomomorphicCiphertext */
        if(!(ciphertext instanceof ExponentialElGamalCiphertext))
            throw new CiphertextException("The ciphertext type did not match the crypto type!");

        /* Check if the private key shares have been loaded */
        if(privateKeyShares == null)
            throw new KeyNotLoadedException("The private key shares have not yet been loaded! [Decryption]");

        /* Partially decrypt to get g^m */
        try { BigInteger mappedPlainText = partialDecrypt((ExponentialElGamalCiphertext) ciphertext);

        BigInteger g = PEK.getG().bigintValue();

        /* Guess the value of m by comparing g^i to g^m and return if/when they're the same -- TODO 100 is chosen arbitrarily for now */
        for(int i=0; i<100; i++) {
            if (g.pow(i).equals(mappedPlainText)) {
                return ByteBuffer.allocate(4).putInt(i).array();
            }
        }

        }
        catch (ClassCastException e) { System.err.println("The IHomomorphicCiphertext given could not be casted to an ExponentialElGamalCiphertext."); }

        throw new SearchSpaceExhaustedException("The decryption could not find a number of votes within the probable search space!");

    }

    /**
     * Partially decrypts the ciphertext for each private key share and then combines them
     * @param ciphertext
     * @return
     */
    private BigInteger partialDecrypt(ExponentialElGamalCiphertext ciphertext) {

        List<AdderInteger> partials = new ArrayList<>();

        /* Partially decrypt for each share */
        for(AdderPrivateKeyShare pks : privateKeyShares)
            partials.add(pks.partialDecrypt(ciphertext));

        /* Combine output */
        AdderInteger total = AdderInteger.ONE;

        for(AdderInteger partial : partials)
            total = total.multiply(partial);

        /* Convert this into BigInteger */
        return total.bigintValue();
    }

    /**
     * @see crypto.ICryptoType#encrypt(byte[])
     */
    public IHomomorphicCiphertext encrypt(byte[] plainText) throws CipherException, InvalidKeyException, KeyNotLoadedException {

        if(PEK == null)
            throw new KeyNotLoadedException("The public key has not yet been loaded! [Encryption]");

        AdderInteger plaintextValue = new AdderInteger(new BigInteger(plainText));

        /* Encrypt our plaintext and store as ExponentialElGamalCiphertext */
        ExponentialElGamalCiphertext ctext = PEK.encrypt(plaintextValue, Arrays.asList(AdderInteger.ZERO, AdderInteger.ONE));

        /* Verify the ciphertext */
        if (!ctext.verify(0,1,PEK))
            throw new InvalidVoteException("We got a bad plaintext!");

        return ctext;
    }

    /**
     * Loads the private key shares from a single filepath
     * @param filePath
     * @throws FileNotFoundException
     */
    public void loadPrivateKeyShares(String filePath) throws FileNotFoundException {

        FileInputStream fileInputStream = new FileInputStream(filePath);

        try {

            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            loadPrivateKeyShares((AdderPrivateKeyShare[]) objectInputStream.readObject());

        } catch (ClassNotFoundException | IOException e) { e.printStackTrace(); }
    }

    /**
     * @param privateKey
     */
    private void loadPrivateKeyShares(AdderPrivateKeyShare privateKey[]) {
        this.privateKeyShares = privateKey;
    }

    /**
     * Loads the public key from a filepath
     * @param filePath
     * @throws FileNotFoundException
     */
    public void loadPublicKey(String filePath) throws FileNotFoundException {

        FileInputStream fileInputStream = new FileInputStream(filePath);

        try {

            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            loadPublicKey((AdderPublicKey) objectInputStream.readObject());

        } catch (ClassNotFoundException | IOException e) { e.printStackTrace(); }
    }

    /**
     * @param publicKey
     */
    private void loadPublicKey(AdderPublicKey publicKey) {
        this.PEK = publicKey;
    }

    /**
     *@see ICryptoType#loadAllKeys(String[])
     */
    public void loadAllKeys(String[] filePaths) throws FileNotFoundException {

        /* List to load the keys into */
        List<AdderKey> keys = new ArrayList<>();

        /* Load the keys from the file paths */
        for(String path : filePaths) {

            try {

                FileInputStream fileInputStream = new FileInputStream(path);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                keys.add((AdderKey) objectInputStream.readObject());

            }
            catch (ClassNotFoundException | IOException e) { e.printStackTrace(); }

        }

        try { loadAllKeys(keys.toArray(new AdderKey[keys.size()])); }
        catch (BadKeyException e) { e.printStackTrace(); }
    }


    /**
     *
     * @param keys
     * @throws BadKeyException
     */
    private void loadAllKeys(AdderKey[] keys) throws BadKeyException {

        /* Check to make sure we're getting at least */
        if(keys.length > 2)
            throw new BadKeyException("Invalid number of keys!");


        int privateKeySharesNum = 0;
        int publicKeyNum = 0;

        /* Check to make sure that the keys are in the correct order / of the correct type */
        for (AdderKey key : keys) {

            if (key instanceof AdderPrivateKeyShare) privateKeySharesNum++;

            if (key instanceof AdderPublicKey) publicKeyNum++;
        }

        if (privateKeySharesNum < 1)
            throw new BadKeyException("Not enough private key shares found!");

        if (publicKeyNum != 1)
            throw new BadKeyException("Wrong number of public keys!");

        if (!(keys[0] instanceof AdderPublicKey))
            throw new BadKeyException("Public key didn't come first!");


        PEK = (AdderPublicKey)keys[0];
        privateKeyShares = (AdderPrivateKeyShare[]) Arrays.copyOfRange(keys,1,privateKeySharesNum+2);
    }

}
