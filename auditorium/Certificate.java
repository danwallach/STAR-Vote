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
 * This class represents a certificate, which is essentially a signed key. Its
 * S-expression format is:
 * <p>
 * <tt>
 * (cert <br>
 * &nbsp;(signature <i>signer</i> <i>sig-data</i><br>
 * &nbsp;&nbsp;(key <i>id</i> <i>annotation</i> <i>mod</i> <i>exp</i>)))
 * </tt>
 * <p>
 * Note that the <tt>(signature...)</tt> subexpression is the S-expression
 * representation of a {@link Signature}; similarly, the <tt>(key...)</tt>
 * subexpression is a {@link Key}.
 * 
 * @see auditorium.Signature
 * @see auditorium.Key
 * @author Kyle Derr
 */
public class Certificate {

    /** The pattern for a certificate, of the form (cert <signature>) */
    public static final ASExpression PATTERN = new ListExpression(StringExpression.makeString("cert"), Wildcard.SINGLETON);

    /** The signature of this certificate, which it signs messages with, of the form (signature [id] [sigdata] {payload}) */
    private final Signature signature;

    /** The key this certificate uses to sign things with, of the form (key [id] [annotation] [mod] [exp]) */
    private final Key key;

    /**
     * Constructor.
     *
     * @param sig       the signature of this certificate
     *
     * @throws IncorrectFormatException
     */
    public Certificate(Signature sig) throws IncorrectFormatException {
        signature = sig;

        /* Extract the key from the signature */
        key = new Key(sig.getPayload());
    }

    /**
     * Construct a certificate from its s-expression format.
     * 
     * @param cert      the certificate message in its s-expression format.
     *
     * @throws IncorrectFormatException Thrown if the given expression isn't formatted like PATTERN.
     */
    public Certificate(ASExpression cert) throws IncorrectFormatException {

        /* Ensure this S-expression is actually a certificate */
        ASExpression matchResult = PATTERN.match( cert );
        if (matchResult == NoMatch.SINGLETON)
            throw new IncorrectFormatException(cert, new Exception("given expression did not match the pattern"));

        /* Enforce type */
        ListExpression matchList = (ListExpression) matchResult;

        /* Extract the vital information */
        signature = new Signature(matchList.get(0));
        key = new Key(signature.getPayload());
    }

    /**
     * Convert this certificate to its auditorium s-expression format.
     * 
     * @return An S-Expression of the form (cert (signature [signer] [data] (key [id] [annotation] [mod] [exp])))
     */
    public ListExpression toASE() {
        return new ListExpression(StringExpression.makeString("cert"), signature.toASE());
    }

    /**
     * @return       The signature structure that makes up this cert.
     */
    public Signature getSignature() {
        return signature;
    }

    /**
     * @return       The key that this certificate presents.
     */
    public Key getKey() {
        return key;
    }
}
