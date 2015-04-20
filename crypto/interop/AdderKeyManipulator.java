package crypto.interop;

import crypto.ExponentialElGamalCiphertext;
import crypto.adder.AdderInteger;
import crypto.adder.AdderPrivateKeyShare;
import crypto.adder.AdderPublicKey;
import crypto.adder.Polynomial;

import java.util.ArrayList;
import java.util.List;

public class AdderKeyManipulator {

    /** A cached key that will let us compare the public key that is used throughout the election process */
	private static AdderPublicKey _cachedKey = null;

    /** A LaGrange polynomial for the Exponential-ElGamal homomorphic process. */
	private static Polynomial _poly = null;




    /* 1. somewhere need to have authorities generate public/private keyshare
     * 2. The public keyshare is saved here
     * 3. Each of the authorities will create a polynomial
     * 4. The polynomial will create a ciphertext for _every_ other authority's (j)
     *    published publickeyshare at j mod q: pubKeys.get(j)).encryptPoly((polys.get(i)).evaluate(new AdderInteger(j, q)))
     * 5. */

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
	
	/**
	 * Generates the "final" PrivateKey from the pre-generated one.
	 * This is needed to decrypt the totals calculated with the corresponding final public key.
	 * 
	 * @return the new PrivateKey
	 */
	public static AdderPrivateKeyShare generateFinalPrivateKey(AdderPublicKey publicKey, AdderPrivateKeyShare privateKeyShare){

		/* Generate the final private key */
		List<ExponentialElGamalCiphertext> ciphertexts = new ArrayList<>();
        ExponentialElGamalCiphertext ciphertext = publicKey.encryptPoly(_poly.evaluate(new AdderInteger(0, publicKey.getQ())));
        ciphertexts.add(ciphertext);

        return privateKeyShare.getRealPrivateKeyShare(ciphertexts);
	}

    public static void setPolynomial(Polynomial poly) {
        _poly = poly;
    }
}