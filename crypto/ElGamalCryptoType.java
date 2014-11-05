package crypto;

import crypto.exceptions.BadKeyException;
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
import java.security.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matthew Kindy II on 11/5/2014.
 */
public class ElGamalCryptoType implements ICryptoType {

    private final Cipher cipher;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private final SecureRandom random = new SecureRandom();

    public ElGamalCryptoType() throws UninitialisedException {

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        try { cipher = Cipher.getInstance("ElGamal/None/NoPadding", "BC"); }
        catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException e)
        { throw new UninitialisedException("The cipher could not be successfully initialised! (" + e.getClass() + ")"); }
    }

    /**
     * @see crypto.ICryptoType#decrypt(byte[])
     */
    public byte[] decrypt(byte[] cipherText) throws CipherException, InvalidKeyException, KeyNotLoadedException {

        if(privateKey == null)
            throw new KeyNotLoadedException("The private key has not yet been loaded! [Decryption]");

        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        try { return cipher.doFinal(cipherText);  }
        catch (BadPaddingException | IllegalBlockSizeException e) { throw new CipherException(e.getClass() + ": " + e.getMessage()); }
    }

    /**
     * @see crypto.ICryptoType#encrypt(byte[])
     */
    public byte[] encrypt(byte[] plainText) throws CipherException, InvalidKeyException, KeyNotLoadedException {

        if(publicKey == null)
            throw new KeyNotLoadedException("The public key has not yet been loaded! [Encryption]");

        cipher.init(Cipher.ENCRYPT_MODE, publicKey, random);

        try { return cipher.doFinal(plainText);  }
        catch (BadPaddingException | IllegalBlockSizeException e) { throw new CipherException(e.getClass() + ": " + e.getMessage()); }
    }

    /**
     *
     * @param filePath
     * @throws BadKeyException
     * @throws FileNotFoundException
     */
    public void loadPrivateKey(String filePath) throws BadKeyException, FileNotFoundException {

        FileInputStream fileInputStream = new FileInputStream(filePath);

        try {

            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            loadPrivateKey((Key) objectInputStream.readObject());

        } catch (ClassNotFoundException | IOException e) { e.printStackTrace(); }
    }

    /**
     *
     * @param privateKey
     * @throws BadKeyException
     */
    public void loadPrivateKey(Key privateKey) throws BadKeyException {
        if (!(privateKey instanceof PrivateKey))
            throw new BadKeyException("This key was not a PrivateKey!");

        this.privateKey = (PrivateKey)privateKey;
    }

    /**
     *
     * @param filePath
     * @throws BadKeyException
     * @throws FileNotFoundException
     */
    public void loadPublicKey(String filePath) throws BadKeyException, FileNotFoundException {

        FileInputStream fileInputStream = new FileInputStream(filePath);

        try {

            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            loadPublicKey((Key) objectInputStream.readObject());

        } catch (ClassNotFoundException | IOException e) { e.printStackTrace(); }
    }

    /**
     *
     * @param publicKey
     * @throws BadKeyException
     */
    public void loadPublicKey(Key publicKey) throws BadKeyException {
        if (!(publicKey instanceof PublicKey))
            throw new BadKeyException("This key was not a PublicKey!");

        this.publicKey = (PublicKey)publicKey;
    }

    /**
     *@see crypto.ICryptoType#loadKeys(String[])
     */
    public void loadKeys(String[] filePaths) throws BadKeyException, FileNotFoundException {

        /* List to load the keys into */
        List<Key> keys = new ArrayList<>();

        /* Load the keys from the file paths */
        for(String path : filePaths) {

            try {

                FileInputStream fileInputStream = new FileInputStream(path);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                keys.add((Key) objectInputStream.readObject());

            } catch (ClassNotFoundException | IOException e) { e.printStackTrace(); }

        }

        loadKeys(keys.toArray(new Key[keys.size()]));
    }

    /**
     * @see crypto.ICryptoType#loadKeys(java.security.Key...)
     */
    public void loadKeys(Key[] keys) throws BadKeyException {

        /* Check to make sure we're only getting two keys */
        if(keys.length != 2) {
            throw new BadKeyException("Invalid number of keys!");
        }

        /* Check to make sure that the keys are in the correct order / of the correct type */
        else if (!(keys[0] instanceof PrivateKey) || !(keys[1] instanceof PublicKey)) {
            throw new BadKeyException("At least one of the keys was not of the correct type! [PrivateKey, PublicKey]");
        }

        privateKey = (PrivateKey)keys[0];
        publicKey = (PublicKey)keys[1];
    }

}
