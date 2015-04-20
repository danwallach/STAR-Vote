package crypto.interop;

import crypto.ExponentialElGamalCiphertext;
import crypto.adder.*;
import crypto.exceptions.KeyGenerationException;

import java.util.*;

/**
 *
 */
public class AdderKeyManipulator {

    /** A cached key that will let us compare the public key that is used throughout the election process */
	private static AdderPublicKey _cachedKey = null;

    /** A LaGrange polynomial for the Exponential-ElGamal homomorphic process. */
	private static Polynomial _poly = null;

    private final static int maxAuth =1;
    private static Map<Integer, AdderPublicKeyShare> keyShares = new TreeMap<>();
    private static Map<Integer, List<ExponentialElGamalCiphertext>> polyMap = new TreeMap<>();
    private static Map<Integer, AdderInteger> GMap = new TreeMap<>();

    private final static int safetyThreshold =1;
    private final static int decryptionThreshold =1;

    private static Set<Integer> stage1participants = new TreeSet<>();
    private static Set<Integer> stage2participants = new TreeSet<>();
    private static Set<Integer> stage3participants = new TreeSet<>();

    private static AdderPublicKeyShare seedKey;


    /* 1. Generate public/private keyshare for each authority
     * 2. Each public keyshare is saved here
     * 3. Each of the authorities will create a polynomial
     * 4. The polynomial will create a ciphertext for _every_ other authority's (j)
     *    published publickeyshare at j mod q: pubKeys.get(j)).encryptPoly((polys.get(i)).evaluate(new AdderInteger(j, q)))
     * 5. */

    /**
     *
     * @param seed
     */
    public static void setSeedKey(AdderPublicKeyShare seed){
        if (seedKey != null)
            seedKey = seed;
        else System.err.println("Seed key was not set because it already has been set!");
    }

    /**
     *
     * @param authNum
     * @return
     * @throws KeyGenerationException
     */
    public static AdderPrivateKeyShare generateAuthorityKeySharePair(int authNum) throws KeyGenerationException {
        /* TODO? This could take a code linked to a number or something (register authorities?) */
        /* TODO? instead of using a seedKey, perhaps would make sense to make this a singleton */
        if (seedKey != null) {
            if(stage2participants.isEmpty()) {
                if (authNum > 0 || authNum < maxAuth){
                    if (!stage1participants.contains(authNum)) {

                        /* Create the PublicKeyShare for this authority */
                        keyShares.put(authNum, new AdderPublicKeyShare(seedKey.getP(), seedKey.getG(), seedKey.getF()));

                        /* Add this to list of stage1participants */
                        stage1participants.add(authNum);

                        /* Return the PrivateKeyShare for this authority */
                        return keyShares.get(authNum).genKeyPair();

                    } else throw new KeyGenerationException("An authority attempted to generate a keyPair more than once!");
                } else throw new KeyGenerationException("An authority with invalid authNum attempted to generate a keyPair");
            } else throw new KeyGenerationException("KeySharePair generation stage cannot operate once polynomial stage has begun.");
        } else throw new KeyGenerationException("An authority attempted to generate a keyPair but no seed key has been loaded.");
    }

    public static void generateAuthorityPolynomialValues(int authNum) throws InvalidPolynomialException {

        if (stage1participants.size() >= safetyThreshold) {
            if(stage3participants.isEmpty()) {
                if (polyMap.get(authNum) != null) {

                /* Create polynomial */
                    Polynomial authPoly = new Polynomial(seedKey.getP(), seedKey.getG(), seedKey.getF(), keyShares.size() - 1);
                    List<ExponentialElGamalCiphertext> valueList = new ArrayList<>();

                    for (Integer auth : stage1participants) {

                    /* Add in P_authNum(auth) */
                        valueList.add(keyShares.get(auth).encryptPoly(authPoly.evaluate(new AdderInteger(auth, seedKey.getQ()))));
                    }

                /* Put this into the encrypted polynomial values map */
                    polyMap.put(authNum, valueList);

                    AdderInteger g = seedKey.getG();
                    AdderInteger q = seedKey.getQ();

                /* Calculate G_{i,l} for l=0 (0 is all used in algorithm, but in paper, l=0...decryptionThreshold-1) */
                    AdderInteger bigG = g.pow(authPoly.evaluate(new AdderInteger(AdderInteger.ZERO, q)));
                    GMap.put(authNum, bigG);

                    stage2participants.add(authNum);

                } else throw new InvalidPolynomialException("An authority attempted to generate polynomial values more than once!");
            } else throw new InvalidPolynomialException("Polynomial stage cannot operate once PrivateKeyShare regeneration stage has begun.");
        } else throw new InvalidPolynomialException("Polynomial stage cannot be initiated due to safety threshold.");
    }



    /**
     * Computes the real private key for each authority. The original AdderPrivateKeyShare
     * was sourced from a random initial AdderPublicKeyShare.
     *
     * @param authNum
     * @param authKeyShare
     * @return the final private key
     */
    private static AdderPrivateKeyShare generateRealPrivateKeyShare(int authNum, AdderPrivateKeyShare authKeyShare) {

        if (stage2participants.size() >= safetyThreshold) {

            AdderInteger g = authKeyShare.getG();
            AdderInteger q = authKeyShare.getQ();
            AdderInteger x = authKeyShare.getX();
            AdderInteger p = authKeyShare.getP();
            AdderInteger f = authKeyShare.getF();

            AdderInteger total = new AdderInteger(AdderInteger.ZERO, q);

            for (ExponentialElGamalCiphertext encryptedAuthorityPoly : polyMap.get(authNum)) {

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

            stage3participants.add(authNum);

            return new AdderPrivateKeyShare(p, g, total, f);

        } else throw new InvalidPrivateKeyException("PrivateKeyShare regeneration stage cannot be initiated due to safety threshold.");

    }

    /**
     *
     * @return
     */
    public static AdderPublicKey generatePublicEncryptionKey(){

        if (stage3participants.size() >= safetyThreshold) {

            AdderInteger finalH = new AdderInteger(AdderInteger.ONE, seedKey.getP());

            for (Integer participant : stage2participants) {
                finalH.multiply(GMap.get(participant));
            }

            return new AdderPublicKey(seedKey.getP(),seedKey.getG(),finalH,seedKey.getF());

        } else throw new InvalidPublicKeyException("Public key creation stage cannot be initiated due to safety threshold.");
    }

	/**
	 * Sets the cached final public key.
	 * This is used so VoteBoxes and Supervisors can coordinate their
	 * key usage.
	 *
	 * @param newKey - the key to load into the cache.
	 */
	public static void setCachedKey(AdderPublicKey newKey){
		_cachedKey = newKey;
	}
	
	/**
	 * Generates the "final" public key using the pre-generated public key.
	 * This is needed for tallying and NIZK verification.
	 * This call returns the same key each time, but this key is different
	 * from run to run.
	 * 
	 * @param publicKey - the pre-calculated public key.
	 * @return the new PublicKey
	 */
	public static AdderPublicKey generateFinalPublicKey(AdderPublicKey publicKey){
		if(_cachedKey != null)
			return _cachedKey;
		
		_cachedKey = generateFinalPublicKeyNoCache(publicKey);
		
		return _cachedKey;
	}
	
	/**
	 * Generates the "final" public key using the pre-generated public key.
	 * This is needed to actually tally and perform NIZK verification.
	 * Additionally, this call will return a different variant of the public key
	 * every time it is called.
	 * 
	 * @param pubKey - the pre-calculated public key.
	 * @return the new PublicKey
	 */
	protected static AdderPublicKey generateFinalPublicKeyNoCache(AdderPublicKey pubKey){

		_poly = new Polynomial(pubKey.getP(), pubKey.getG(), pubKey.getF(), 0);

        AdderInteger p = pubKey.getP();
		AdderInteger q = pubKey.getQ();
		AdderInteger g = pubKey.getG();
		AdderInteger f = pubKey.getF();
		AdderInteger finalH = new AdderInteger(AdderInteger.ONE, p);

        /* Theoretically this is g^x, where x is in R Z_q or something*/
		AdderInteger gvalue = g.pow((_poly).evaluate(new AdderInteger(AdderInteger.ZERO, q)));

        /* set h = gvalue, where gvalue = g^x */
		finalH = finalH.multiply(gvalue);
		
		_cachedKey = new AdderPublicKey(p, g, finalH, f);
		
		return _cachedKey;
	}

}