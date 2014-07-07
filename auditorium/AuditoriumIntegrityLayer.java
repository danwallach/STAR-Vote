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

import sexpression.*;

/**
 * This layer handles signatures.
 * 
 * @author Kyle Derr
 * 
 */
public class AuditoriumIntegrityLayer extends AAuditoriumLayer {

    /** The pattern for signed messages, of the form (signed-message <certificate> <signature>) */
    public static final ASExpression PATTERN = new ListExpression(StringExpression.makeString("signed-message"), Wildcard.SINGLETON, Wildcard.SINGLETON);

    /** The serial number of the machine running this layer */
    private final String nodeID;

    /** The keystore containing keys and certifications for signing messages */
    private final IKeyStore keystore;

    /** The certification this layer uses to sign messages */
    private Certificate myCert;

    /** All certificate authority keys are expected to be annotated thusly */
    public static final String CA_ANNOTATION = "ca";

    /**
     * Constructor.
     *
     * @param child         The layer below this layer.
     * @param host          The host that is using this stack of layers.
     * @param keystore      The keystore to use for locating keys to perform cryptographic operations.
     */
    public AuditoriumIntegrityLayer(AAuditoriumLayer child, IAuditoriumHost host, IKeyStore keystore) {
        /* use AAuditoriumLayer to handle other layers */
        super( child, host );

        /* Initialize our fields */
        nodeID = host.getNodeId();
        this.keystore = keystore;

        /* Check to make sure we actually have a way of signing messages */
        if (keystore == null) Bugout.msg( "warning: keystore is NULL in AuditoriumIntegrityLayer()" );


        /* Try to initialize */
        try { init(); }
        catch (AuditoriumCryptoException e) { throw new FatalNetworkException("Can't perform necessary cryptographic operations", e); }
    }

    /**
     * @see auditorium.IAuditoriumLayer#makeAnnouncement(sexpression.ASExpression)
     */
    public ASExpression makeAnnouncement(ASExpression datum) {
        /* Make new datum */
        ASExpression newDatum;

        /* Attempt to construct a signed message by signing the message */
        try {

            newDatum = new ListExpression(StringExpression.makeString("signed-message"), myCert.toASE(), RSACrypto.SINGLETON.sign(datum, keystore.loadKey(nodeID)).toASE());

        } catch (AuditoriumCryptoException e) {

            throw new FatalNetworkException("Couldn't make an announcement because of a crypto error.", e);

        }

        /* Decorated method call to allow child layers to handle the message */
        return getChild().makeAnnouncement(newDatum);
    }

    /**
     * We don't do anything to Joins, so just pass it down
     *
     * @see auditorium.IAuditoriumLayer#makeJoin(sexpression.ASExpression)
     */
    public ASExpression makeJoin(ASExpression datum) {
        return getChild().makeJoin(datum);
    }

    /**
     * We don't do anything to join replies, so just pass it down.
     *
     * @see auditorium.IAuditoriumLayer#makeJoinReply(sexpression.ASExpression)
     */
    public ASExpression makeJoinReply(ASExpression datum) {
        return getChild().makeJoinReply(datum);
    }

    /**
     * @see auditorium.IAuditoriumLayer#receiveAnnouncement(sexpression.ASExpression)
     */
    public ASExpression receiveAnnouncement(ASExpression datum) throws IncorrectFormatException {

        try {
            /* Match the incoming message to ensure that it is signed properly */
            ASExpression matchResult = PATTERN.match(getChild().receiveAnnouncement(datum));
            if (matchResult == NoMatch.SINGLETON) throw new IncorrectFormatException(datum, new Exception(datum + " doesn't match the pattern:" + PATTERN));

            /* Now we can impose type */
            ListExpression matchList = (ListExpression) matchResult;

            /* Get out the certificate and signature */
            Certificate cer = new Certificate(matchList.get(0));
            Signature sig = new Signature(matchList.get(1));

            /* Verify the signature */
            RSACrypto.SINGLETON.verify(sig, cer);

            /* get the ID of the key that signed the *certificate* */
            String signingKeyId = cer.getSignature().getId();

            /* the certificate (Cert object) that signed the, er, certificate */
            Certificate signingCert = keystore.loadCert( signingKeyId );

            /* verify that the signature on the certificate itself is correct */
            if (signingCert.getKey().getAnnotation().equals(CA_ANNOTATION))
                RSACrypto.SINGLETON.verify(cer.getSignature(), signingCert);
            else
            	throw new SignerValidityException("Certificate on message signature was signed by non-authoritative key '"
                                                 + signingKeyId + "' (annotation: '"
                                                 + signingCert.getKey().getAnnotation() + "')");


            /* Send the rest upwards. */
            return sig.getPayload();
        }
        catch (AuditoriumCryptoException | SignerValidityException e) {
            throw new IncorrectFormatException(datum, e);
        }
    }

    /**
     * We don't handle receiving join replies, pass it on
     *
     * @see auditorium.IAuditoriumLayer#receiveJoinReply(sexpression.ASExpression)
     */
    public ASExpression receiveJoinReply(ASExpression datum) throws IncorrectFormatException {
        return getChild().receiveJoinReply(datum);
    }

    /**
     * We don't handle receiving joins, pass it on.
     *
     * @see auditorium.IAuditoriumLayer#receiveJoin(sexpression.ASExpression)
     */
    public ASExpression receiveJoin(ASExpression datum) throws IncorrectFormatException {
        return datum;
    }

    /**
     * Run some initial tests to make sure that this host can do all the cryptographic things it needs to be able to do.
     */
    private void init() throws AuditoriumCryptoException {
        StringExpression message = StringExpression.makeString( "test" );

        /* load the cert associated with this node */
        myCert = keystore.loadCert( nodeID );

        /* Make a test signature and verify it. (Check that "my" cert and key are on disk) */
        Signature sig = RSACrypto.SINGLETON.sign(message, keystore.loadKey(nodeID));
        RSACrypto.SINGLETON.verify(sig, myCert);
    }
}
