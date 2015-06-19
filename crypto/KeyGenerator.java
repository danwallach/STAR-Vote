package crypto;

import auditorium.Key;
import crypto.exceptions.KeyGenerationException;

import java.io.*;
import java.security.*;

/**
 * @author Matt Bernhard
 */
public class KeyGenerator {

    public enum Type {
        ELGAMAL,
        ECC
    }

    private Type type;

    public KeyGenerator(Type type){
        this.type = type;
    }

    public KeyPair generateKeyPair(String directory) throws KeyGenerationException {


        switch (type) {
            case ELGAMAL:
                return generateElGamalKeyPair(directory);

            default:
                throw new KeyGenerationException("Couldn't find the correct cryptographic protocol!");
        }

    }


    private KeyPair generateElGamalKeyPair(String directory) throws KeyGenerationException {

        try {
            ObjectOutputStream publicOut = new ObjectOutputStream(new FileOutputStream(directory + File.separator+ "ElGamalPublic.key"));
            ObjectOutputStream privateOut = new ObjectOutputStream(new FileOutputStream(directory + File.separator+ "ElGamalPrivate.key"));

            KeyPairGenerator generator = KeyPairGenerator.getInstance("ElGamal", "BC");
            SecureRandom random = new SecureRandom();

            generator.initialize(512, random);

            KeyPair pair = generator.generateKeyPair();


            PublicKey pubKey = pair.getPublic();
            PrivateKey privKey = pair.getPrivate();

            publicOut.writeObject(pubKey);
            privateOut.writeObject(privKey);

            publicOut.close();
            privateOut.close();

            return pair;

        } catch (NoSuchAlgorithmException | NoSuchProviderException | IOException e) {
            e.printStackTrace();
            throw new KeyGenerationException("Error during key generation.");
        }

    }



}
