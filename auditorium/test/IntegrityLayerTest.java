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

import auditorium.*;
import auditorium.Generator.Keys;
import crypto.adder.AdderPrivateKeyShare;
import crypto.adder.AdderPublicKey;
import org.junit.Before;
import org.junit.Test;
import sexpression.*;

import static org.junit.Assert.assertEquals;

/**
 * Tests the Auditorium integrity layer
 *
 * @author Kyle Derr
 */
public class IntegrityLayerTest {

    // Stuff we're testing
    private IAuditoriumHost host = new IAuditoriumHost() {

        public ASExpression getAddresses() {
            throw new RuntimeException( "unused" );
        }

        public Log getLog() {
            throw new RuntimeException( "unused" );
        }

        public HostPointer getMe() {
            throw new RuntimeException( "unused" );
        }

        public String getNodeId() {
            return "TEST";
        }

        public void receiveAnnouncement(Message message) {
            throw new RuntimeException( "unused" );
        }

        public void removeLink(Link link) {
            throw new RuntimeException( "unused" );
        }

        public String nextSequence() {
            return "TEST";
        }
    };
    private Certificate myCert;
    private Key myKey;
    private Certificate caCert;
    private IKeyStore keystore = new IKeyStore() {

        public Certificate loadCert(String nodeID) throws AuditoriumCryptoException {
            if (nodeID.equals("ca"))
                return caCert;
            return myCert;
        }

        @Override
        public AdderPublicKey loadAdderPublicKey() throws RuntimeException {
            return null;
        }

        @Override
        public AdderPrivateKeyShare loadAdderPrivateKey() throws RuntimeException {
            return null;
        }

        public Key loadKey(String nodeID) throws AuditoriumCryptoException {
            return myKey;
        }
        
    };
    private AuditoriumIntegrityLayer layer;

    @Before
    public void build() throws Exception {
        Generator gen = new Generator();
        Keys ca = gen.generateKey( "ca", "ca" );
        Keys my = gen.generateKey( "me", "booth" );
        myKey = my.getPrivate();
        myCert = new Certificate( RSACrypto.SINGLETON.sign( my.getPublic().toASE(), ca
                .getPrivate() ) );
        caCert = new Certificate( RSACrypto.SINGLETON.sign( ca.getPublic().toASE(), ca
                .getPrivate() ) );
        layer = new AuditoriumIntegrityLayer( AAuditoriumLayer.BOTTOM, host,
                keystore );
    }

    // ** makeAnnouncement(ASExpression) tests **
    private void makeAnnouncementTest(ASExpression datum) throws Exception {
        // compute actual
        ASExpression wrapped = layer.makeAnnouncement( datum );

        // compute expected
        ASExpression matchAgainst = new ListExpression( StringExpression
                .makeString( "signed-message" ), myCert.toASE(),
                RSACrypto.SINGLETON.sign( datum, myKey).toASE() );

        assertEquals( matchAgainst, wrapped );

    }

    @Test
    public void makeAnnouncement1() throws Exception {
        makeAnnouncementTest(NoMatch.SINGLETON);
    }

    @Test
    public void makeAnnouncement2() throws Exception {
        makeAnnouncementTest(Nothing.SINGLETON);
    }

    @Test
    public void makeAnnouncement3() throws Exception {
        makeAnnouncementTest(StringExpression.EMPTY);
    }

    @Test
    public void makeAnnouncement4() throws Exception {
        makeAnnouncementTest(ListExpression.EMPTY);
    }

    @Test
    public void makeAnnouncement5() throws Exception {
        makeAnnouncementTest(StringExpression.makeString("TEST"));
    }

    @Test
    public void makeAnnouncement6() throws Exception {
        makeAnnouncementTest(new ListExpression(StringExpression
                .makeString("TEST"), StringExpression.makeString("TEST")));
    }

    // ** receiveAnnouncement(ASExpression) tests **
    private void receiveAnnouncementTest(ASExpression wrapThis) throws Exception {
        ASExpression wrapped = layer.makeAnnouncement(wrapThis);
        System.err.println(wrapped);
        assertEquals(wrapThis, layer.receiveAnnouncement(wrapped));
    }

    private void receiveAnnouncementTestFail(ASExpression sendThis)
            throws IncorrectFormatException {
        layer.receiveAnnouncement( sendThis );
    }

    // Good (actually construct a message with makeAnnouncement)
    @Test
    public void receiveAnnouncement1() throws Exception {
        receiveAnnouncementTest(StringExpression.EMPTY);
    }

    @Test
    public void receiveAnnouncement2() throws Exception {
        receiveAnnouncementTest(ListExpression.EMPTY);
    }

    @Test
    public void receiveAnnouncement3() throws Exception {
        receiveAnnouncementTest(StringExpression.makeString("Test"));
    }

    @Test
    public void receiveAnnouncement4() throws Exception {
        receiveAnnouncementTest(new ListExpression(StringExpression
                .makeString("Test")));
    }

    @Test
    public void receiveAnnouncement5() throws Exception {
        receiveAnnouncementTest(new ListExpression(StringExpression
                .makeString("Test"), new ListExpression(StringExpression
                .makeString("Test")), StringExpression.makeString("TEST")));
    }

    // Bad (random stuff)
    @Test(expected = IncorrectFormatException.class)
    public void receiveAnnouncement6() throws Exception {
        receiveAnnouncementTestFail(NoMatch.SINGLETON);
    }

    @Test(expected = IncorrectFormatException.class)
    public void receiveAnnouncement7() throws Exception {
        receiveAnnouncementTestFail(Nothing.SINGLETON);
    }

    @Test(expected = IncorrectFormatException.class)
    public void receiveAnnouncement8() throws Exception {
        receiveAnnouncementTestFail(StringExpression.EMPTY);
    }

    @Test(expected = IncorrectFormatException.class)
    public void receiveAnnouncement9() throws Exception {
        receiveAnnouncementTestFail(ListExpression.EMPTY);
    }

    @Test(expected = IncorrectFormatException.class)
    public void receiveAnnouncement10() throws Exception {
        receiveAnnouncementTestFail(StringExpression.makeString("TEST"));
    }

    // Bad (format the message correctly, but make the cert, sig, or message
    // type not check) (Don't need to check with every single cert/key anymore).

    @Test(expected = IncorrectFormatException.class)
    public void receiveAnnouncement11() throws Exception {
        ASExpression datum = StringExpression.makeString( "TEST" );
        Signature sig = RSACrypto.SINGLETON.sign( datum, myKey);

        layer.receiveAnnouncement( new ListExpression( StringExpression
                .makeString( "not-signed-message" ), myCert.toASE(), sig
                .toASE(), datum ) );
    }

    @Test(expected = IncorrectFormatException.class)
    public void receiveAnnouncement12() throws Exception {
        ASExpression datum = StringExpression.makeString( "TEST" );
        Signature sig = RSACrypto.SINGLETON.sign( datum, myKey);
        byte[] sigBytes = sig.getSigData().getBytesCopy();
        sigBytes[0] = 1;
        Signature notSig = new Signature( "TEST", StringExpression
                .makeString( sigBytes ), datum );

        layer.receiveAnnouncement( new ListExpression( StringExpression
                .makeString( "signed-message" ), myCert.toASE(), notSig
                .toASE(), datum ) );
    }

    @Test(expected = IncorrectFormatException.class)
    public void receiveAnnouncement13() throws Exception {
        ASExpression datum = StringExpression.makeString( "TEST" );

        layer.receiveAnnouncement( new ListExpression( StringExpression
                .makeString( "signature" ), myCert.toASE(),
                StringExpression.EMPTY, datum ) );
    }

    // ** do nothing method tests **
    // (all these tested methods essentially return what they're given)
    private void doNothingTest(ASExpression datum) throws Exception {
        for (int lcv = 1; lcv <= 10; lcv++) {
            assertEquals( datum, layer.makeJoin( datum ) );
            assertEquals( datum, layer.makeJoinReply( datum ) );
            assertEquals( datum, layer.receiveJoin( datum ) );
            assertEquals( datum, layer.receiveJoinReply( datum ) );
        }
    }

    @Test
    public void doNothing1() throws Exception {
        doNothingTest(NoMatch.SINGLETON);
    }

    @Test
    public void doNothing2() throws Exception {
        doNothingTest(Nothing.SINGLETON);
    }

    @Test
    public void doNothing3() throws Exception {
        doNothingTest(StringExpression.EMPTY);
    }

    @Test
    public void doNothing4() throws Exception {
        doNothingTest(ListExpression.EMPTY);
    }

    @Test
    public void doNothing5() throws Exception {
        doNothingTest(StringExpression.makeString("TEST"));
    }
}
