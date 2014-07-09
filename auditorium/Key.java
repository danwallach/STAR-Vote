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

import java.math.BigInteger;

/**
 * This class represents one half of an asymmetric cryptographic key; as such it
 * can be used for signing, as well as <i>either</i> encryption or decryption.
 * It is an RSA key defined by a modulus and a single exponent (both using
 * BigInteger math). Also included is an identifier (helpful for
 * associating the key with, for example, an external entity) and a free-form
 * annotation.
 * <p>
 * Its S-expression representation (used for wire-protocol transmission as well
 * as storage, e.g. in a {@link auditorium.SimpleKeyStore}) is:
 * <p>
 * <tt>
 * (key <i>id</i> <i>annotation</i> <i>mod</i> <i>exp</i>)
 * </tt>
 * 
 * @see sexpression
 * @author Kyle Derr
 */
public class Key {

    /** The pattern for key s-expressions (key <i>id</i> <i>annotation</i> <i>mod</i> <i>exp</i>) */
    public static final ASExpression PATTERN = new ListExpression(
            StringExpression.makeString( "key" ), StringWildcard.SINGLETON,
            StringWildcard.SINGLETON, StringWildcard.SINGLETON,
            StringWildcard.SINGLETON );

    /** The serial of the host holding this key */
    private final String id;

    /** information about this key, e.g. that it is private */
    private final String annotation;

    /** the key's modulus, the product of two primes */
    private final BigInteger mod;

    /** The key itself */
    private final BigInteger key;

    /**
     * @param id                The key belongs to the host that has this ID
     * @param annotation        The key is annotated with this string (usually something about how the key is supposed to be used)
     * @param mod               This is the key's modulus, the product of two large primes.
     * @param key               This is the actual key material (in RSA, it is an exponent).
     */
    public Key(String id, String annotation, BigInteger mod, BigInteger key) {
        this.id = id;
        this.annotation = annotation;
        this.mod = mod;
        this.key = key;
    }

    /**
     * @param expression        Construct the key from this expression.
     *                          This should be the get in s-expression format (like what is returned from toASE())
     * @throws IncorrectFormatException Thrown if the given expression is not formatted correctly.
     */
    public Key(ASExpression expression) throws IncorrectFormatException {
        try {
            /* Match the expression against the pattern */
            ASExpression matchresult = PATTERN.match(expression);
            if (matchresult == NoMatch.SINGLETON)
                throw new IncorrectFormatException(expression, new Exception("did not match the pattern for key"));

            /* get out the match results */
            ListExpression matchList = (ListExpression)matchresult;

            /* Get out the necessary information from the expression */
            id = matchList.get(0).toString();
            annotation = matchList.get(1).toString();
            mod = new BigInteger(((StringExpression)matchList.get(2)).getBytesCopy());
            key = new BigInteger(((StringExpression)matchList.get(3)).getBytesCopy());
        }
        catch (ClassCastException e) {
            throw new IncorrectFormatException( expression, e );
        }
    }

    /**
     * Construct an s-expression which represents this key.
     * 
     * @return This method returns (key [id] [annotation] [mod] [exp]).
     */
    public ASExpression toASE() {
        return new ListExpression( StringExpression.makeString( "key" ),
                StringExpression.makeString(id), StringExpression
                        .makeString(annotation), StringExpression
                        .makeString( mod.toByteArray() ), StringExpression
                        .makeString(key.toByteArray()) );
    }

    /**
     * @return This method returns the ID of the host that this key belongs to.
     */
    public String getId() {
        return id;
    }

    /**
     * @return This method returns the annotation string given to this key.
     */
    public String getAnnotation() {
        return annotation;
    }

    /**
     * @return This method returns the key's modulus number.
     */
    public BigInteger getMod() {
        return mod;
    }

    /**
     * @return This method returns the key's exponent.
     */
    public BigInteger getKey() {
        return key;
    }
}
