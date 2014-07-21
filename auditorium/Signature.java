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
 * This class represents the auditorium signature data structure. Its format is
 * (signature [signer-id] [sigdata] {payload}).
 * 
 * @author Kyle Derr
 * 
 */
public class Signature {

    /** The pattern for an ASE Signature, (signature [signer-id] [sigdata] {payload}) */
    public static final ASExpression PATTERN = new ListExpression(
            StringExpression.makeString( "signature" ),
            StringWildcard.SINGLETON, StringWildcard.SINGLETON,
            Wildcard.SINGLETON );

    /** The serial of the signing machine
     * */
    private final String id;

    /** The signature data */
    private final StringExpression sigdata;

    /** The actual data that was signed */
    private final ASExpression payload;

    /**
     * @param id            The ID of the signer.
     * @param sigdata       The actual digital signature bytes.
     * @param payload       The thing that is signed.
     */
    public Signature(String id, StringExpression sigdata, ASExpression payload) {
        this.id = id;
        this.sigdata = sigdata;
        this.payload = payload;
    }

    /**
     * Construct a signature structure based on its s-expression format.
     * 
     * @param expression        The expression that will be converted.
     * @throws IncorrectFormatException Thrown if the given expression is not formatted as (signature [signer-id] [sigdata] {payload}).
     */
    public Signature(ASExpression expression) throws IncorrectFormatException {

        /* Attempt to match the aSE */
        ASExpression matchResult = PATTERN.match(expression);
        if (matchResult == NoMatch.SINGLETON)
            throw new IncorrectFormatException(expression, new Exception("did not match the pattern for key"));

        /* Now extract the data from the ASE*/
        ListExpression matchList = (ListExpression) matchResult;

        id = matchList.get(0).toString();
        sigdata = (StringExpression) matchList.get(1);
        payload = matchList.get(2);
    }

    /**
     * @return This method returns this signature in its s-expression form.
     */
    public ASExpression toASE() {
        return new ListExpression(StringExpression.makeString("signature"), StringExpression.makeString(id), sigdata, payload);
    }

    /**
     * @return This method returns the ID of the signer.
     */
    public String getId() {
        return id;
    }

    /**
     * @return This method returns the signature data.
     */
    public StringExpression getSigData() {
        return sigdata;
    }

    /**
     * @return This method returns the payload i.e. the expression that was signed.
     */
    public ASExpression getPayload() {
        return payload;
    }
}
