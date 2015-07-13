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
 * The crypto-type associated with an Adder-type system, or more generally with
 * an exponential elGamal cryptosystem.
 *
 * Created by Matthew Kindy II on 11/5/2014.
 */
public class DHExponentialElGamalCryptoType implements ICryptoType {

    private List<AdderPrivateKeyShare> privateKeyShares;
    private AdderPublicKey PEK;

    /**
     * Decrypts the AHomomorphicCiphertext
     *
     * @param ciphertext    the encrypted plaintext for a single vote-candidate value
     *
     * @see crypto.ICryptoType#decrypt(AHomomorphicCiphertext)
     *
     * @return              the decrypted ciphertext as a byte[]
     *
     * @throws InvalidKeyException
     * @throws KeyNotLoadedException
     * @throws CipherException
     * @throws CiphertextException
     */
    public <T extends AHomomorphicCiphertext> byte[] decrypt(T ciphertext) throws InvalidKeyException, KeyNotLoadedException, CipherException, CiphertextException {

        /* Check if this is the right type of AHomomorphicCiphertext */
        if(!(ciphertext instanceof ExponentialElGamalCiphertext))
            throw new CiphertextException("The ciphertext type did not match the crypto type!");

        ExponentialElGamalCiphertext eegCiphertext = ((ExponentialElGamalCiphertext) ciphertext);

        /* Check if the private key shares have been loaded */
        if(privateKeyShares == null)
            throw new KeyNotLoadedException("The private key shares have not yet been loaded! [Decryption]");

        /* Partially decrypt to get g^m */
        List<AdderInteger> partialDecryptions = partialDecrypt(eegCiphertext);

        return decryptMappedPlaintext(partialDecryptions, eegCiphertext.size, eegCiphertext.getH());


    }

    /**
     * Using the partial decryptions (i.e. mapped plaintexts) for a ciphertext, calculates the
     * final decryption.
     *
     * @param partials  the partial decryptions of a ciphertext
     * @param size      the size of the original ciphertext
     * @param H         h^r * f^m
     *
     * @return          the full decryption as a byte[]
     */
    private byte[] decryptMappedPlaintext(List<AdderInteger> partials, int size, AdderInteger H) {

        /*

    	  Adder encrypt is of m (public initial g, p, h) [inferred from code]
    	                    m = {0, 1} for one vote
    	                    g' = g^r = g^y
    	                    h' = h^r * f^m = h^y * m'

    	  Quick decrypt (given r) [puzzled out by Kevin Montrose]
    	                    confirm g^r = g'
    	                    m' = (h' / (h^r)) = h' / h^y
    	                    if(m' == f) m = 1
    	                    if(m' == 1) m = 0
    	                    etc.

    	*/

        /* Use this to multiply all the values together */
        AdderInteger total = AdderInteger.ONE;

        for (AdderInteger partial : partials) {
            total = total.multiply(partial);
        }

        /* This will be the mapped ciphertext */
        /* total = h^y, H = h' = h^y * f^m, so this is f^m */
        AdderInteger mappedPlaintext = H.divide(total);

        AdderInteger f = PEK.getF();
        AdderInteger p = PEK.getP();
        AdderInteger q = PEK.getQ();

        /* Indicates if we have successfully resolved the ciphertext */
        boolean gotResult = false;

        AdderInteger j = new AdderInteger(0, q);

        /* Iterate over the number of votes to try to guess n */
        for (int k = 0; k <= size; k++) {

            /* Create a guess */
            j = new AdderInteger(k, q);

            System.out.println("DOES " + new AdderInteger(f,p).pow(j) + " equal " + mappedPlaintext + "?");

            /* Check the guess and get out when found */
            if (f.pow(j).equals(mappedPlaintext)) {
                gotResult = true;
                break;
            }
        }

        /* Keep track of found result, otherwise error */
        if (gotResult) return ByteBuffer.allocate(4).putInt(j.intValue()).array();
        else throw new SearchSpaceExhaustedException("The decryption could not find a number of votes " +
                                                     "within the probable search space for " + mappedPlaintext + "!");

    }

    /**
     * Partially decrypts the ciphertext for each private key share and then returns them
     * @param ciphertext    the ciphertext to be partially decrypted
     *
     * @return              the list of partial decryptions
     */
    private List<AdderInteger> partialDecrypt(ExponentialElGamalCiphertext ciphertext) {

        List<AdderInteger> coeffs = new ArrayList<>();

        for (int i=0; i<privateKeyShares.size(); i++) {
            coeffs.add(new AdderInteger(i));
        }

        List<AdderInteger> partials = new ArrayList<>();
        Polynomial poly = new Polynomial(PEK.getP(), PEK.getG(), PEK.getF(), coeffs);
        List<AdderInteger> lagrangeCoeffs = poly.lagrange();

        /* Partially decrypt for each share */
        for(int i=0; i<lagrangeCoeffs.size(); i++ ) {

            AdderInteger partial = privateKeyShares.get(i).partialDecrypt(ciphertext);
            partials.add(partial.pow(lagrangeCoeffs.get(i)));
        }

        return partials;
    }

    /**
     * Encrypts a plaintext (formatted as a byte[])
     * @param plainText     a byte array to be encrypted
     *
     * @see crypto.ICryptoType#encrypt(byte[])
     *
     * @return              the encrypted plaintext as an ExponentialElGamalCiphertext
     *
     * @throws CipherException
     * @throws InvalidKeyException
     * @throws KeyNotLoadedException
     */
    public ExponentialElGamalCiphertext encrypt(byte[] plainText) throws CipherException, InvalidKeyException, KeyNotLoadedException {

        if(PEK == null)
            throw new KeyNotLoadedException("The public key has not yet been loaded! [Encryption]");

        AdderInteger plaintextValue = new AdderInteger(new BigInteger(plainText));

        /* Encrypt our plaintext and store as ExponentialElGamalCiphertext */
        ExponentialElGamalCiphertext ctext = PEK.encrypt(plaintextValue, Arrays.asList(AdderInteger.ZERO, AdderInteger.ONE));

        /* Verify the ciphertext */
        if (!ctext.verify(0,1,PEK))
            throw new InvalidRaceSelectionException("We got a bad plaintext!");

        return ctext;
    }

    /**
     * Loads the private key shares from a file path.
     *
     * @param filePath      the file path of the file from which to load the private key shares
     * TODO this might need to be changed to load one key at a time until the threshold is met
     *
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
     * Loads the private key shares.
     *
     * @param privateKey      the array of AdderPrivateKeyShares to be loaded
     * TODO this might need to be changed to load one key at a time until the threshold is met or something
     *
     * @see #loadPrivateKeyShares(String)
     *
     * @throws FileNotFoundException
     */
    public void loadPrivateKeyShares(AdderPrivateKeyShare[] privateKey) {
        this.privateKeyShares = Arrays.asList(privateKey);
    }

    /**
     * Loads the public key from a file path
     *
     * @param filePath      the file path of the file from which to load the PEK
     *
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
     * Loads the PEK
     * @param publicKey     the AdderPublicKey to be loaded
     *
     * @see #loadPublicKey(String)
     */
    public void loadPublicKey(AdderPublicKey publicKey) {
        this.PEK = publicKey;
    }

    /**
     * Loads all the keys
     * @param filePaths     the array of file paths from which to load the PEK and private key shares
     *
     * @see ICryptoType#loadAllKeys(String[])
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
     * Loads an array of AdderKeys (should consist of private key shares and PEK)
     * @param keys      the array of AdderKeys to load
     *
     * @see #loadAllKeys(String[])
     *
     * @throws BadKeyException
     */
    private void loadAllKeys(AdderKey[] keys) throws BadKeyException {

        /* TODO take a careful look at this, should we allow if current keys exist? If so, how to replace? */
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
        privateKeyShares = Arrays.asList((AdderPrivateKeyShare[]) Arrays.copyOfRange(keys,1,privateKeySharesNum+2));
    }

}
