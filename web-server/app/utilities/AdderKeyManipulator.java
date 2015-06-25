package utilities;

import crypto.ExponentialElGamalCiphertext;
import crypto.adder.*;
import crypto.exceptions.KeyGenerationException;
import sexpression.ASEParser;

import java.util.*;

/**
 * Authority procedure adapted from Adder paper
 *
 * Created by Matthew Kindy II
 */
public class AdderKeyManipulator {

    /** A LaGrange polynomial for the Exponential-ElGamal homomorphic process. */
    private static Polynomial _poly = null;

    private final static int maxAuth = 1;
    private static Map<String, AdderPublicKeyShare> keyShares = new TreeMap<>();
    private static Map<String, AdderPrivateKeyShare> prkeyShares = new TreeMap<>();
    private static Map<String, List<ExponentialElGamalCiphertext>> polyMap = new TreeMap<>();
    private static Map<String, AdderInteger> GMap = new TreeMap<>();

    private final static int safetyThreshold = 1;
    private final static int decryptionThreshold =1;

    private static TreeSet<String> stage1participants = new TreeSet<>();
    private static TreeSet<String> stage2participants = new TreeSet<>();
    private static TreeSet<String> stage3participants = new TreeSet<>();

    private static AdderPublicKeyShare seedKey;
    private static boolean alreadyGenerated = false;

    /**
     * Sets the initial rnandomness key for this procedure.
     *
     * @param seed      the AdderPublicKeyShare containing the initial randomness to be used in authority key generation
     */
    public static void setSeedKey(AdderPublicKeyShare seed){
        if (seedKey == null)
            seedKey = seed;
        else System.err.println("Seed key was not set because it already has been set!");
    }

    public static int getStage(String auth) {
        return stage3participants.contains(auth) ? 4 :
                stage2participants.contains(auth) ? 3 :
                        stage1participants.contains(auth) ? 2 : 1;
    }

    /**
     * Generates the public and private key shares for this authority and stores the public key share.
     *
     * @param auth   the number identifier for the authority to participate in this step
     * @return          the AdderPrivateKeyShare for this authority
     *
     * @throws KeyGenerationException
     */
    public static AdderPrivateKeyShare generateAuthorityKeySharePair(String auth) throws KeyGenerationException {
        /* TODO? This could take a code linked to a number or something (register authorities?) */
        /* TODO? instead of using a seedKey, perhaps would make sense to make this a singleton */
        if (seedKey != null) {
            if(stage2participants.isEmpty()) {
                if (stage1participants.size() < maxAuth){
                    if (!stage1participants.contains(auth)) {

                        /* Create the PublicKeyShare for this authority */
                        keyShares.put(auth, new AdderPublicKeyShare(seedKey.getP(), seedKey.getG(), seedKey.getF()));
                        prkeyShares.put(auth, keyShares.get(auth).genKeyPair());

                        /* Add this to list of stage1participants */
                        stage1participants.add(auth);

                        /* Return the PrivateKeyShare for this authority */
                        return prkeyShares.get(auth);

                    } else throw new KeyGenerationException("An authority (" + auth + ") attempted to generate a keyPair more than once!");
                } else throw new KeyGenerationException("An authority (" + auth + ") with invalid authNum attempted to generate a keyPair");
            } else throw new KeyGenerationException("KeySharePair generation stage cannot operate once polynomial stage has begun.");
        } else throw new KeyGenerationException("An authority (" + auth + ") attempted to generate a keyPair but no seed key has been loaded.");
    }

    /**
     * Generates the polynomial values for each authority.
     *
     * @param auth   the number identifier for the authority to participate in this step
     *
     * @throws InvalidPolynomialException
     */
    public static void generateAuthorityPolynomialValues(String auth) throws InvalidPolynomialException {

        if (stage1participants.size() >= safetyThreshold) {
            if(stage3participants.isEmpty()) {
                if (stage1participants.contains(auth)) {
                    if (!stage2participants.contains(auth)) {

                        /* Create polynomial */
                        Polynomial authPoly = new Polynomial(seedKey.getP(), seedKey.getG(), seedKey.getF(), keyShares.size() - 1);
                        List<ExponentialElGamalCiphertext> valueList = new ArrayList<>();

                        for (String a : stage1participants) {

                            /* Add in P_authNum(auth) */
                            int authnum = stage1participants.headSet(a).size();

                            valueList.add(keyShares.get(a).encryptPoly(authPoly.evaluate(new AdderInteger(authnum, seedKey.getQ()))));
                        }

                        /* Put this into the encrypted polynomial values map */
                        polyMap.put(auth, valueList);

                        AdderInteger g = seedKey.getG();
                        AdderInteger q = seedKey.getQ();

                        /* Calculate G_{i,l} for l=0 (0 is all used in algorithm, but in paper, l=0...decryptionThreshold-1) */
                        AdderInteger bigG = g.pow(authPoly.evaluate(new AdderInteger(AdderInteger.ZERO, q)));
                        GMap.put(auth, bigG);

                        stage2participants.add(auth);

                    } else throw new InvalidPolynomialException("An authority (" + auth + ") attempted to generate polynomial values more than once!");
                } else throw new InvalidPolynomialException("This authority (" + auth + ") did not complete KeySharePair generation stage!");
            } else throw new InvalidPolynomialException("Polynomial stage cannot operate once PrivateKeyShare regeneration stage has begun.");
        } else throw new InvalidPolynomialException("Polynomial stage cannot be initiated due to safety threshold.");
    }



    /**
     * Computes the real private key share for each authority. The original AdderPrivateKeyShare
     * was sourced from a random initial AdderPublicKeyShare.
     *
     * @param auth       the number identifier for the authority to participate in this step
     * param authKeyShare  the AdderPrivateKeyShare associated with this authority
     *
     * @return the final private key
     */
    public static AdderPrivateKeyShare generateRealPrivateKeyShare(String auth/*, AdderPrivateKeyShare authKeyShare*/) throws KeyGenerationException {

        if (stage2participants.size() >= safetyThreshold) {
            if (stage2participants.contains(auth)) {
                if (!alreadyGenerated) {

                    AdderPrivateKeyShare authKeyShare = prkeyShares.get(auth);

                    AdderInteger g = authKeyShare.getG();
                    AdderInteger q = authKeyShare.getQ();
                    AdderInteger x = authKeyShare.getX();
                    AdderInteger p = authKeyShare.getP();
                    AdderInteger f = authKeyShare.getF();

                    AdderInteger total = new AdderInteger(AdderInteger.ZERO, q);

                    for (ExponentialElGamalCiphertext encryptedAuthorityPoly : polyMap.get(auth)) {

                        /* Decrypt the polynomial manually (reverse polynomial operation) */
                        AdderInteger eL = encryptedAuthorityPoly.getG();
                        AdderInteger eR = encryptedAuthorityPoly.getH();
                        AdderInteger product = eL.pow(x.negate()).multiply(eR);
                        AdderInteger qPlusOneOverTwo = q.add(AdderInteger.ONE).divide(AdderInteger.TWO);
                        AdderInteger posInverse = product.pow(qPlusOneOverTwo);
                        AdderInteger negInverse = posInverse.negate();
                        AdderInteger inverse;

                        if (posInverse.compareTo(negInverse) < 0) {
                            inverse = posInverse;
                        } else {
                            inverse = negInverse;
                        }

                        inverse = inverse.subtract(AdderInteger.ONE);

                        total = total.add(inverse);
                    }

                    stage3participants.add(auth);

                    return new AdderPrivateKeyShare(p, g, total, f);

                } else throw new KeyGenerationException("Public encryption key generation already occurred!");
            } else throw new KeyGenerationException("This authority (" + auth + ") did not complete polynomial stage!");
        } else throw new KeyGenerationException("PrivateKeyShare regeneration stage cannot be initiated due to safety threshold.");

    }

    /**
     * Create the public encryption key used during all phases in the election.
     *
     * @return  the AdderPublicKey to be used in all phases of an election
     */
    public static AdderPublicKey generatePublicEncryptionKey(){

        if (stage3participants.size() >= safetyThreshold) {

            AdderInteger finalH = new AdderInteger(AdderInteger.ONE, seedKey.getP());

            for (String participant : stage2participants) {
                finalH.multiply(GMap.get(participant));
            }

            alreadyGenerated = true;

            AdderPublicKey PEK = new AdderPublicKey(seedKey.getP(),seedKey.getG(),finalH,seedKey.getF());

            System.out.println("Generated PEK: " + ASEParser.convertToASE(PEK));

            return PEK;

        } else throw new InvalidPublicKeyException("Public key creation stage cannot be initiated due to safety threshold.");
    }

}