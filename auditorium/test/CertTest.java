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

package auditorium.test;

import static org.junit.Assert.*;

import java.math.BigInteger;

import org.junit.*;
import sexpression.*;
import auditorium.*;

/**
 * This class offers JUnit testing of the auditorium.Cert class.
 * 
 * @author Kyle Derr
 */
public class CertTest {

    /** Auditorium key for easy certification */
    private final Key key = new Key("KEYID", "KEYANNOTATION", new BigInteger("1"), new BigInteger("2"));

    /** ASE representation of the key */
    private final ASExpression keyASE = key.toASE();

    /** Auditorium signature based on the key */
    private final Signature sig = new Signature("signer", StringExpression.makeString( "sigdata" ), key.toASE());

    // ** <init>(String, StringExpression, Key) tests **
    // the payload of the signature isn't a key.
    @Test(expected = IncorrectFormatException.class)
    public void constructor11() throws Exception {
        new Cert( new Signature( "signer", StringExpression.makeString( "sigdata" ),
                ListExpression.EMPTY ) );
    }

    // Good
    @Test
    public void constructor12() throws Exception {
        Cert c = new Cert( sig );

        assertEquals( sig.toASE(), c.getSignature().toASE() );
        assertEquals( key.toASE(), c.getKey().toASE() );
        assertEquals( new ListExpression( StringExpression.makeString( "cert" ),
                new ListExpression( StringExpression.makeString( "signature" ),
                        StringExpression.makeString( "signer" ), StringExpression.makeString(
                                "sigdata" ), keyASE) ), c.toASE() );

    }

    // ** <init>(ASExpression) tests **
    // Junk
    @Test(expected = IncorrectFormatException.class)
    public void constructor21() throws IncorrectFormatException {
        new Cert( StringExpression.EMPTY );
    }

    @Test(expected = IncorrectFormatException.class)
    public void constructor22() throws IncorrectFormatException {
        new Cert( new ListExpression( "non", "sense" ) );
    }

    // len < 2
    @Test(expected = IncorrectFormatException.class)
    public void constructor23() throws IncorrectFormatException {
        new Cert( new ListExpression( "cert" ) );
    }

    // len > 2
    @Test(expected = IncorrectFormatException.class)
    public void constructor24() throws IncorrectFormatException {
        new Cert( new ListExpression( StringExpression.makeString( "cert" ),
                new ListExpression( StringExpression.makeString( "signature" ),
                        StringExpression.makeString( "signer" ), StringExpression.makeString(
                                "Sig" ), keyASE), StringExpression.makeString(
                        "extra" ) ) );
    }

    // [0] != cert
    @Test(expected = IncorrectFormatException.class)
    public void constructor25() throws IncorrectFormatException {
        new Cert( new ListExpression( StringExpression.makeString( "notcert" ),
                new ListExpression( StringExpression.makeString( "signature" ),
                        StringExpression.makeString( "signer" ), StringExpression.makeString(
                                "Sig" ), keyASE) ) );
    }

    // [1] !signature
    @Test(expected = IncorrectFormatException.class)
    public void constructor26() throws IncorrectFormatException {
        new Cert( new ListExpression( StringExpression.makeString( "cert" ),
                new ListExpression( StringExpression.makeString( "notsignature" ),
                        StringExpression.makeString( "signer" ), StringExpression.makeString(
                                "Sig" ), keyASE) ) );
    }

    // Good
    @Test
    public void constructor27() throws IncorrectFormatException {
        Cert c = new Cert( new ListExpression( StringExpression.makeString( "cert" ),
                new ListExpression( StringExpression.makeString( "signature" ),
                        StringExpression.makeString( "signer" ), StringExpression.makeString(
                                "Sig" ), keyASE) ) );

        assertEquals( new Signature( "signer", StringExpression.makeString( "Sig" ),
                keyASE).toASE(), c.getSignature().toASE() );
        assertEquals(keyASE, c.getKey().toASE() );
        assertEquals( new ListExpression( StringExpression.makeString( "cert" ),
                new ListExpression( StringExpression.makeString( "signature" ),
                        StringExpression.makeString( "signer" ), StringExpression.makeString(
                                "Sig" ), keyASE) ), c.toASE() );

    }
}
