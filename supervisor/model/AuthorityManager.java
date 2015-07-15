package supervisor.model;

import crypto.ExponentialElGamalCiphertext;
import crypto.adder.*;
import crypto.exceptions.KeyGenerationException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Authority procedure adapted from Adder paper
 *
 * Created by Matthew Kindy II
 */
public class AuthorityManager {

    private static SortedMap<String, AdderPublicKeyShare> keyShares = new TreeMap<>();
    private static SortedMap<String, AdderPrivateKeyShare> prkeyShares = new TreeMap<>();
    private static Map<String, List<ExponentialElGamalCiphertext>> polyMap = new LinkedHashMap<>();
    private static Map<String, AdderInteger> GMap = new LinkedHashMap<>();

    private static int safetyThreshold = 1;
    private static int decryptionThreshold = 1;
    private static int maxAuth = 3;

    private static SortedSet<String> stage1participants = new TreeSet<>();
    private static Set<String> stage2participants = new LinkedHashSet<>();
    private static Set<String> stage3participants = new LinkedHashSet<>();

    private static Map<String, Integer> indexMap = new HashMap<>();
    private static Map<AdderPrivateKeyShare, Integer> keyIndex = new HashMap<>();

    /* Source of randomness */
    private static AdderPublicKeyShare seedKey = AdderPublicKeyShare.makePublicKeyShare(128);

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

            if(stage2participants.isEmpty()) {
                if (stage1participants.size() < maxAuth){
                    if (!stage1participants.contains(auth)) {

                        /* Create the PublicKeyShare for this authority */
                        keyShares.put(auth, new AdderPublicKeyShare(seedKey.getP(), seedKey.getG(), seedKey.getF()));
                        prkeyShares.put(auth, keyShares.get(auth).genKeyPair());

                        /* Add this to list of stage1participants */
                        stage1participants.add(auth);
                        indexMap.put(auth, stage1participants.size());

                        /* Return the PrivateKeyShare for this authority */
                        return prkeyShares.get(auth);

                    } else throw new KeyGenerationException("An authority (" + auth + ") attempted to generate a keyPair more than once!");
                } else throw new KeyGenerationException("An authority (" + auth + ") with invalid authNum attempted to generate a keyPair");
            } else throw new KeyGenerationException("KeySharePair generation stage cannot operate once polynomial stage has begun.");
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
                        Polynomial authPoly = new Polynomial(seedKey.getP(), seedKey.getG(), seedKey.getF(), decryptionThreshold - 1);
                        List<ExponentialElGamalCiphertext> valueList = new ArrayList<>();

                        for (String participant : stage1participants) {

                            /* Add in P_authNum(auth) */
                            AdderInteger aPosition = new AdderInteger(indexMap.get(participant));
                            valueList.add(keyShares.get(participant).encryptPoly(authPoly.evaluate(new AdderInteger(aPosition, seedKey.getQ()))));
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

                    AdderPrivateKeyShare authKeyShare = prkeyShares.get(auth);

                    AdderInteger g = authKeyShare.getG();
                    AdderInteger q = authKeyShare.getQ();
                    AdderInteger x = authKeyShare.getX();
                    AdderInteger p = authKeyShare.getP();
                    AdderInteger f = authKeyShare.getF();

                    AdderInteger total = new AdderInteger(AdderInteger.ZERO, q);

                    for (List<ExponentialElGamalCiphertext> encryptedAuthorityPolyValues : polyMap.values()) {

                        /* Decrypt the polynomial manually (reverse polynomial operation) */
                        AdderInteger eL = encryptedAuthorityPolyValues.get(indexMap.get(auth)-1).getG();
                        AdderInteger eR = encryptedAuthorityPolyValues.get(indexMap.get(auth)-1).getH();
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

                    AdderPrivateKeyShare authPrivKey = new AdderPrivateKeyShare(p, g, total, f);
                    keyIndex.put(authPrivKey, indexMap.get(auth));

                    return authPrivKey;

            } else throw new KeyGenerationException("This authority (" + auth + ") did not complete polynomial stage!");
        } else throw new KeyGenerationException("PrivateKeyShare regeneration stage cannot be initiated due to safety threshold.");

    }

    /**
     * Create the public encryption key used during all phases in the election.
     *
     * @return  the AdderPublicKey to be used in all phases of an election
     */
    public static AdderPublicKey generatePublicEncryptionKey() throws KeyGenerationException {

        if (stage3participants.size() >= safetyThreshold) {

            AdderInteger finalH = new AdderInteger(AdderInteger.ONE, new AdderInteger(seedKey.getP()));

            for (String participant : stage2participants) {
                finalH = finalH.multiply(GMap.get(participant));
            }

            return new AdderPublicKey(new AdderInteger(seedKey.getP()), new AdderInteger(seedKey.getG()), finalH, new AdderInteger(seedKey.getF()));

        } else throw new KeyGenerationException("Public key creation stage cannot be initiated due to safety threshold.");
    }

    public static List<AdderInteger> getPolynomialCoefficients(List<AdderPrivateKeyShare> pksList) {

        List<AdderInteger> coeffs = new ArrayList<>();

        coeffs.addAll(pksList.stream().map(pks -> new AdderInteger(keyIndex.get(pks))).collect(Collectors.toList()));

        return coeffs;

    }

    public static void newSession(int safetyThreshold, int decryptionThreshold, int maxAuth) {

        if (safetyThreshold >= decryptionThreshold && decryptionThreshold > 0 && maxAuth >= safetyThreshold) {

            AuthorityManager.safetyThreshold = safetyThreshold;
            AuthorityManager.decryptionThreshold = decryptionThreshold;
            AuthorityManager.maxAuth = maxAuth;

            keyShares.clear();
            prkeyShares.clear();
            polyMap.clear();
            GMap.clear();

            stage1participants.clear();
            stage2participants.clear();
            stage3participants.clear();

            indexMap.clear();
            keyIndex.clear();

            seedKey = AdderPublicKeyShare.makePublicKeyShare(128);

        } else throw new RuntimeException("Tried to start a new session with bad inputs!");
    }

}