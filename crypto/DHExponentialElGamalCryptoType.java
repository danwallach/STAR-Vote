package crypto;

import crypto.adder.*;
import crypto.exceptions.BadKeyException;
import crypto.exceptions.CiphertextException;
import crypto.exceptions.KeyNotLoadedException;
import crypto.exceptions.UninitialisedException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Matthew Kindy II on 11/5/2014.
 */
public class DHExponentialElGamalCryptoType implements ICryptoType {

    private final Cipher cipher;
    private AdderPrivateKeyShare[] privateKeyShares;
    private AdderPublicKey publicKey;
    private final SecureRandom random = new SecureRandom();

    public DHExponentialElGamalCryptoType() throws UninitialisedException {

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        try { cipher = Cipher.getInstance("ElGamal/None/NoPadding", "BC"); }
        catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException e)
        { throw new UninitialisedException("The cipher could not be successfully initialised! (" + e.getClass() + ")"); }
    }

    /**
     * @see crypto.ICryptoType#decrypt(ICiphertext)
     */
    public byte[] decrypt(ICiphertext ciphertext) throws InvalidKeyException, KeyNotLoadedException, CipherException, CiphertextException {

        /* Check if this is the right type of ICiphertext */
        if(!(ciphertext instanceof ExponentialElGamalCiphertext))
            throw new CiphertextException("The ciphertext type did not match the crypto type!");

        /* Check if the private key shares have been loaded */
        if(privateKeyShares == null)
            throw new KeyNotLoadedException("The private key shares have not yet been loaded! [Decryption]");


        try {

            /* Partially decrypt to get g^m */
            BigInteger mappedPlainText = partialDecrypt(ciphertext);
            BigInteger g = publicKey.getG().bigintValue();

            /* Guess the value of m by comparing g^i to g^m and return if/when they're the same --
                TODO 100 is chosen arbitrarily for now */
            for(int i=0; i<100; i++) {
                if (g.pow(i).equals(mappedPlainText)) {
                    return ByteBuffer.allocate(4).putInt(i).array();
                }
            }

        }
        catch (BadPaddingException | IllegalBlockSizeException e) { throw new CipherException(e.getClass() + ": " + e.getMessage()); }

        throw new SearchSpaceExhaustedException("The decryption could not find a number of votes within the probable search space!");

    }

    /**
     *
     * @param ciphertext
     * @return
     */
    private BigInteger partialDecrypt(ICiphertext ciphertext) {

        for(AdderPrivateKeyShare pks : privateKeyShares) {
            /* Partially decrypt for each share */
            /* Combine output */
        }

        /* Convert this into BigInteger */
        return new BigInteger("0");
    }

    /**
     * @see crypto.ICryptoType#encrypt(byte[])
     */
    public ICiphertext encrypt(byte[] plainText) throws CipherException, InvalidKeyException, KeyNotLoadedException {

        if(publicKey == null)
            throw new KeyNotLoadedException("The public key has not yet been loaded! [Encryption]");

        /* Encrypt our plaintext and store as ICiphertext *//* TODO change this to return ExponentialElgamalCiphertext */
        ICiphertext c = publicKey.encrypt(new AdderInteger(new BigInteger(plainText)));

        return c;
    }

    /**
     * Loads the private key from a filepath
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
        this.publicKey = publicKey;
    }

    /**
     *@see crypto.ICryptoType#loadKeys(String[])
     */
    public void loadKeys(String[] filePaths) throws FileNotFoundException {

        /* List to load the keys into */
        List<AdderKey> keys = new ArrayList<>();

        /* Load the keys from the file paths */
        for(String path : filePaths) {

            try {

                FileInputStream fileInputStream = new FileInputStream(path);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                keys.add((AdderKey) objectInputStream.readObject());

            } catch (ClassNotFoundException | IOException e) { e.printStackTrace(); }

        }

        try {
            loadKeys(keys.toArray(new AdderKey[keys.size()]));
        } catch (BadKeyException e) {
            e.printStackTrace();
        }
    }


    private void loadKeys(AdderKey[] keys) throws BadKeyException {

        /* Check to make sure we're getting at least */
        if(keys.length > 2) {
            throw new BadKeyException("Invalid number of keys!");
        }

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


        publicKey = (AdderPublicKey)keys[0];
        privateKeyShares = (AdderPrivateKeyShare[]) Arrays.copyOfRange(keys,1,privateKeySharesNum+2);
    }

}
