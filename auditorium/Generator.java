/**
  * This file is part of VoteBox.
  * 
  * VoteBox is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License version 3 as published by
  * the Free Software Foundation.
  * 
  * You should have received a copy of the GNU General Public License
  * along with VoteBox, found in the root of any distribution or
  * repository containing all or part of VoteBox.
  * 
  * THIS SOFTWARE IS PROVIDED BY WILLIAM MARSH RICE UNIVERSITY, HOUSTON,
  * TX AND IS PROVIDED 'AS IS' AND WITHOUT ANY EXPRESS, IMPLIED OR
  * STATUTORY WARRANTIES, INCLUDING, BUT NOT LIMITED TO, WARRANTIES OF
  * ACCURACY, COMPLETENESS, AND NONINFRINGEMENT.  THE SOFTWARE USER SHALL
  * INDEMNIFY, DEFEND AND HOLD HARMLESS RICE UNIVERSITY AND ITS FACULTY,
  * STAFF AND STUDENTS FROM ANY AND ALL CLAIMS, ACTIONS, DAMAGES, LOSSES,
  * LIABILITIES, COSTS AND EXPENSES, INCLUDING ATTORNEYS' FEES AND COURT
  * COSTS, DIRECTLY OR INDIRECTLY ARISING OUR OF OR IN CONNECTION WITH
  * ACCESS OR USE OF THE SOFTWARE.
 */

package auditorium;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import sexpression.*;
import sexpression.stream.*;

/**
 * Use an instance of this class to generate RSA keys. It's main method will
 * generate a set of keys, all signed by the same CA key (which it will also generate).
 * 
 * @author Kyle Derr
 * 
 */
public class Generator {

    /**
     * A public/private key pair. This class is used by the generator to wrap
     * the generated return value.
     */
    public static class Keys {

        /** The public key */
        private final Key publicKey;

        /** The private key */
        private final Key privateKey;

        /**
         * Constructor.
         *
         * @param publicKey        This is the public key for the pair.
         * @param privateKey       This is the private key for the pair.
         */
        public Keys(Key publicKey, Key privateKey) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }

        /**
         * @return Get this pair's public part.
         */
        public Key getPublic() {
            return publicKey;
        }

        /**
         * @return Get this pair's private part.
         */
        public Key getPrivate() {
            return privateKey;
        }
    }

    /** A format for error output if the program is run with incorrect arguments */
    public static final String FORMAT = "Can't parse arguments:\n [0] is the number of keys you'd like to generate.\n [1] is the directory where you want to put them.";

    /**
     * @param args [0] is the number of keys you'd like to generate. [1] is the directory where you want to put them.
     */
    public static void main(String... args) throws AuditoriumCryptoException, IOException, IncorrectFormatException {
        /* parse args */
        if (args.length != 2) {
            System.err.println( FORMAT );
            return;
        }

        int numKeys;
        try { numKeys = Integer.parseInt(args[0]); }
        catch (NumberFormatException e) {
            System.err.println( "BAD NUMBER!" );
            System.err.println( FORMAT );
            return;
        }

        File outDir = new File(args[1]);
        if (!outDir.isDirectory() && outDir.canWrite() && outDir.canRead()) {
            System.err.println( "BAD DIRECTORY!" );
            System.err.println( FORMAT );
        }

        Generator gen = new Generator();

        /* generate a key for the CA. */
        Keys certificateAuthorityKeys = gen.generateKey("ca", "ca");
        Certificate caCert = new Certificate(RSACrypto.SINGLETON.sign(certificateAuthorityKeys.getPublic().toASE(), certificateAuthorityKeys.getPrivate()));

        /* Write out the keys */
        write(certificateAuthorityKeys.getPrivate().toASE(), outDir.getPath() + File.separator + "ca.key");
        write(caCert.toASE(), outDir.getPath() + File.separator + "ca.cert");

        /* generate numKeys keys and write them to files. */
        for (int lcv = 0; lcv < numKeys; lcv++) {
            Keys keys = gen.generateKey(Integer.toString( lcv ), "booth");
            Certificate cert = new Certificate( RSACrypto.SINGLETON.sign(keys.getPublic().toASE(), certificateAuthorityKeys.getPrivate()));
            write(keys.getPrivate().toASE(), outDir.getPath() + File.separator + Integer.toString( lcv ) + ".key");
            write(cert.toASE(), outDir.getPath() + File.separator + Integer.toString( lcv ) + ".cert");
        }
    }

    /**
     * A File IO method that writes out the inputs as S-expressions
     *
     * @param exp                   the expression to write out
     * @param absolutePath          the file to write to
     *
     * @throws IOException if the writing doesn't go well
     */
    private static void write(ASExpression exp, String absolutePath) throws IOException {
        File f = new File( absolutePath );
        new ASEWriter( new FileOutputStream( f ) ).writeASE( exp );
    }

    /** The actual generator that will be used to generate the keys */
    private final KeyPairGenerator generator;

    /**
     * Constructor.
     */
    public Generator() {

        /* Try to initialize the key generator */
        try { generator = KeyPairGenerator.getInstance("RSA"); }
        catch (NoSuchAlgorithmException e) { throw new RuntimeException("problem creating generator", e); }
    }

    /**
     * Generate a key pair.
     * 
     * @param id                The pair will be assigned to the host with this ID.
     * @param annotation        Annotate the key with this string. (This field is used for capability assignment).
     * @return                  This method returns the generated key pair.
     */
    public Keys generateKey(String id, String annotation) {

        /* Generate a key pair */
        KeyPair kp = generator.generateKeyPair();

        /* Extract the public and private keys from the pair */
        RSAPublicKey rsaPublicKey = (RSAPublicKey)kp.getPublic();
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey)kp.getPrivate();

        /* Wrap and return the keys as a pair */
        return new Keys(new Key(id, annotation, rsaPublicKey.getModulus(), rsaPublicKey.getPublicExponent()),
                        new Key(id, annotation, rsaPrivateKey.getModulus(), rsaPrivateKey.getPrivateExponent()));
    }

    /**
     * Create a certificate (a signed key).
     * 
     * @param signer        The key of the signer of the certificate.
     * @param signee        The key that will be signed and placed into the certificate.
     * @return              The newly created certificate.
     *
     * @throws AuditoriumCryptoException Thrown if there are platform issues preventing the signature from happening.
     */
    public Certificate createCert(Key signer, Key signee) throws AuditoriumCryptoException {
        try { return new Certificate(RSACrypto.SINGLETON.sign(signee.toASE(), signer)); }
        catch (IncorrectFormatException e) { throw new RuntimeException("signed key was found to not actually have a key", e); }
    }
}
