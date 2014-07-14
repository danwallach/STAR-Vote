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

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sexpression.*;
import auditorium.*;

/**
 * A test of the temporal layer (the layer that orders events with respect to time)
 *
 * @author Kyle Derr
 */
public class TemporalLayerTest {

	private File tmpFile;
    private Log log;
    private IAuditoriumHost host = new IAuditoriumHost() {

        public ASExpression getAddresses() {
            throw new RuntimeException( "not used" );
        }

        public Log getLog() {
            return log;
        }

        public HostPointer getMe() {
            throw new RuntimeException( "not used" );
        }

        public String getNodeId() {
            throw new RuntimeException( "not used" );
        }

        public void receiveAnnouncement(Message message) {
            throw new RuntimeException( "not used" );
        }

        public void removeLink(Link link) {
            throw new RuntimeException( "not used" );
        }

        public String nextSequence() {
            throw new RuntimeException( "not used" );
        }
    };
    private AuditoriumTemporalLayer layer;

    @Before
    public void setup() throws Exception {
    	tmpFile = File.createTempFile("tmp", "test");
    	
        log = new Log( tmpFile );
        layer = new AuditoriumTemporalLayer( AAuditoriumLayer.BOTTOM, host );
    }

    @After
    public void tear() {
        if(!tmpFile.delete())
            throw new RuntimeException("Couldn't delete file!");
    }

    // ** makeAnnouncement(ASExpression) tests **
    // Empty log.
    /**
     * Creates an announcement, makes sure that the thing that was wrapped ends
     * up in the datum section, then returns the pointer list for checking by
     * the caller.
     */
    private ASExpression testMakeAnnouncement(ASExpression wrapThis)
            throws Exception {
        ListExpression datum = (ListExpression) layer
                .makeAnnouncement( wrapThis );
        ListExpression result = (ListExpression) AuditoriumTemporalLayer.PATTERN
                .match( datum );

        assertNotSame( result, NoMatch.SINGLETON );
        assertEquals( wrapThis, result.get( 1 ) );
        return result.get( 0 );
    }

    @Test
    public void testMakeAnnouncement4() throws Exception {
        assertEquals( ListExpression.EMPTY,
            testMakeAnnouncement( ListExpression.EMPTY ) );
    }

    @Test
    public void testMakeAnnouncement5() throws Exception {
        assertEquals( ListExpression.EMPTY,
            testMakeAnnouncement( StringExpression.EMPTY ) );
    }

    @Test
    public void testMakeAnnouncement6() throws Exception {
        assertEquals( ListExpression.EMPTY,
            testMakeAnnouncement( StringExpression.makeString( "TEST" ) ) );
    }

    @Test
    public void testMakeAnnouncement7() throws Exception {
        assertEquals( ListExpression.EMPTY,
            testMakeAnnouncement( new ListExpression( StringExpression.makeString(
                    "TEST" ), StringExpression.makeString( "TEST2" ) ) ) );
    }

    // Two things in the log.
    private HostPointer hp = new HostPointer( "host", "ip", 200 );
    private Message m1 = new Message( "announcement", hp, "1", StringExpression.makeString(
            "msg" ) );
    private Message m2 = new Message( "announcement2", hp, "2", StringExpression.makeString(
            "msg" ) );

    @Test
    public void testMakeAnnouncement8() throws Exception {
        log.logAnnouncement( m1 );
        log.logAnnouncement( m2 );

        assertEquals( new ListExpression( new MessagePointer( m1 ).toASE(),
                new MessagePointer( m2 ).toASE() ),
            testMakeAnnouncement( ListExpression.EMPTY ) );
    }

    // ** makeJoinReply(ASExpression) tests **
    @Test
    public void testMakeJoin11() throws Exception {
        assertEquals( ListExpression.EMPTY, layer
                .makeJoinReply( ListExpression.EMPTY ) );
    }

    @Test
    public void testMakeJoin12() throws Exception {
        assertEquals( ListExpression.EMPTY, layer
                .makeJoinReply( Nothing.SINGLETON ) );
    }

    @Test
    public void testMakeJoin13() throws Exception {
        log.logAnnouncement( m1 );

        assertEquals( new ListExpression( new MessagePointer( m1 ).toASE() ),
            layer.makeJoinReply( StringExpression.makeString( "asf" ) ) );
    }

    @Test
    public void testMakeJoin14() throws Exception {
        log.logAnnouncement( m1 );
        log.logAnnouncement( m2 );

        assertEquals( new ListExpression( new MessagePointer( m1 ).toASE(),
                new MessagePointer( m2 ).toASE() ), layer
                .makeJoinReply( StringExpression.EMPTY ) );
    }

    // ** receiveAnnouncement(ASExpression) tests **
    // Junk
    @Test(expected = IncorrectFormatException.class)
    public void testReceiveAnnouncement1() throws Exception {
        layer.receiveAnnouncement( NoMatch.SINGLETON );
    }

    @Test(expected = IncorrectFormatException.class)
    public void testReceiveAnnouncement2() throws Exception {
        layer.receiveAnnouncement( Wildcard.SINGLETON );
    }

    @Test(expected = IncorrectFormatException.class)
    public void testReceiveAnnouncement3() throws Exception {
        layer.receiveAnnouncement( Nothing.SINGLETON );
    }

    @Test(expected = IncorrectFormatException.class)
    public void testReceiveAnnouncement4() throws Exception {
        layer.receiveAnnouncement( Nothing.SINGLETON );
    }

    @Test(expected = IncorrectFormatException.class)
    public void testReceiveAnnouncement5() throws Exception {
        layer
                .receiveAnnouncement( new ListExpression( "Check", "one", "two" ) );
    }

    // [0] != succeeds
    @Test(expected = IncorrectFormatException.class)
    public void testReceiveAnnouncement6() throws Exception {
        layer
                .receiveAnnouncement( new ListExpression( StringExpression.makeString(
                        "not-succeeds" ), ListExpression.EMPTY,
                        StringExpression.EMPTY ) );
    }

    // pointer list spot isn't a list at all
    @Test(expected = IncorrectFormatException.class)
    public void testReceiveAnnouncement7() throws Exception {
        layer.receiveAnnouncement( new ListExpression( StringExpression.makeString(
                "succeeds" ), StringExpression.EMPTY, StringExpression.EMPTY ) );
    }

    // pointer list has one element that doesn't match the pattern
    @Test(expected = IncorrectFormatException.class)
    public void testReceiveAnnouncement8() throws Exception {
        layer.receiveAnnouncement( new ListExpression( StringExpression.makeString(
                "succeeds" ), new ListExpression( new ListExpression( "ptr",
                "node", "1", "" ), new ListExpression( "ptr", "node", "1", "",
                "extra" ), new ListExpression( "ptr", "node", "1", "" ) ),
                StringExpression.EMPTY ) );
    }

    @Test(expected = IncorrectFormatException.class)
    public void testReceiveAnnouncement9() throws Exception {
        layer.receiveAnnouncement( new ListExpression( StringExpression.makeString(
                "succeeds" ), new ListExpression( new ListExpression( "ptr",
                "node", "1", "" ), new ListExpression( StringExpression.makeString(
                "ptr" ), StringExpression.makeString( "node" ), StringExpression.makeString(
                "1" ), ListExpression.EMPTY ), new ListExpression( "ptr",
                "node", "1", "" ) ), StringExpression.EMPTY ) );
    }

    // pointer list has one element that matches the pattern, but can't be
    // parsed into a pointer
    @Test(expected = IncorrectFormatException.class)
    public void testReceiveAnnouncement10() throws Exception {
        layer.receiveAnnouncement( new ListExpression( StringExpression.makeString(
                "succeeds" ), new ListExpression( new ListExpression( "ptr",
                "node", "1", "" ), new ListExpression( "notptr", "node", "1",
                "" ), new ListExpression( "ptr", "node", "1", "" ) ),
                StringExpression.EMPTY ) );
    }

    // length > 3
    @Test(expected = IncorrectFormatException.class)
    public void testReceiveAnnouncement11() throws Exception {
        layer.receiveAnnouncement( new ListExpression( StringExpression.makeString(
                "succeeds" ), new ListExpression( new ListExpression( "ptr",
                "node", "1", "" ),
                new ListExpression( "ptr", "node", "1", "" ),
                new ListExpression( "ptr", "node", "1", "" ) ),
                StringExpression.EMPTY, StringExpression.makeString( "extra" ) ) );
    }

    // Good
    @Test
    public void testReceiveAnnouncement12() throws Exception {
        log.logAnnouncement( m1 );
        assertEquals( 1, log.getLastTest().size() );
        assertEquals( new MessagePointer( m1 ), log.getLastTest().get( 0 ) );

        layer.receiveAnnouncement( new ListExpression( StringExpression.makeString(
                "succeeds" ), new ListExpression( new MessagePointer( m1 )
                .toASE() ), StringExpression.makeString( "TEST DATUM" ) ) );

        assertEquals( 0, log.getLastTest().size() );
    }

    @Test
    public void testReceiveAnnouncement13() throws Exception {
        log.logAnnouncement( m1 );
        log.logAnnouncement( m2 );
        assertEquals( 2, log.getLastTest().size() );
        assertEquals( new MessagePointer( m1 ), log.getLastTest().get( 0 ) );
        assertEquals( new MessagePointer( m2 ), log.getLastTest().get( 1 ) );

        layer.receiveAnnouncement( new ListExpression( StringExpression.makeString(
                "succeeds" ), new ListExpression( new MessagePointer( m1 )
                .toASE() ), StringExpression.makeString( "TEST DATUM" ) ) );

        assertEquals( 1, log.getLastTest().size() );
        assertEquals( new MessagePointer( m2 ), log.getLastTest().get( 0 ) );
    }

    // ** receiveJoinReply(ASExpression) tests **
    // Junk
    @Test(expected = IncorrectFormatException.class)
    public void testReceiveJoinReply1() throws Exception {
        layer.receiveJoinReply( NoMatch.SINGLETON );
    }

    @Test(expected = IncorrectFormatException.class)
    public void testReceiveJoinReply2() throws Exception {
        layer.receiveJoinReply( Wildcard.SINGLETON );
    }

    @Test(expected = IncorrectFormatException.class)
    public void testReceiveJoinReply3() throws Exception {
        layer.receiveJoinReply( Nothing.SINGLETON );
    }

    @Test(expected = IncorrectFormatException.class)
    public void testReceiveJoinReply4() throws Exception {
        layer.receiveJoinReply( Nothing.SINGLETON );
    }

    @Test(expected = IncorrectFormatException.class)
    public void testReceiveJoinReply5() throws Exception {
        layer.receiveJoinReply( new ListExpression( "Check", "one", "two" ) );
    }

    // not a list
    @Test(expected = IncorrectFormatException.class)
    public void testReceiveJoinReply6() throws Exception {
        layer.receiveJoinReply( StringExpression.makeString( "This is not a list" ) );
    }

    // a list, but not all the members match the pattern
    @Test(expected = IncorrectFormatException.class)
    public void testReceiveJoinReply7() throws Exception {
        layer.receiveJoinReply( new ListExpression( new MessagePointer( m1 )
                .toASE(), new ListExpression( "ptr", "hostID", "number",
                "hash", "EXTRA" ), new MessagePointer( m2 ).toASE() ) );
    }

    // all the members match the pattern, but pointers can't be constructed out
    // of them.
    @Test(expected = IncorrectFormatException.class)
    public void testReceiveJoinReply8() throws Exception {
        layer.receiveJoinReply( new ListExpression( new MessagePointer( m1 )
                .toASE(), new ListExpression( "notptr", "hostID", "number",
                "hash" ), new MessagePointer( m2 ).toASE() ) );
    }

    // Good.
    @Test
    public void testReceiveJoinReply9() throws Exception {
        assertEquals( 0, log.getLastTest().size() );

        layer.receiveJoinReply( new ListExpression( new MessagePointer( m1 )
                .toASE() ) );

        assertEquals( 1, log.getLastTest().size() );
        assertEquals( new MessagePointer( m1 ), log.getLastTest().get( 0 ) );
    }

    @Test
    public void testReceiveJoinReply10() throws Exception {
        assertEquals( 0, log.getLastTest().size() );

        layer.receiveJoinReply( new ListExpression( new MessagePointer( m1 )
                .toASE() ) );
        layer.receiveJoinReply( new ListExpression( new MessagePointer( m2 )
                .toASE() ) );

        assertEquals( 2, log.getLastTest().size() );
        assertEquals( new MessagePointer( m1 ), log.getLastTest().get( 0 ) );
        assertEquals( new MessagePointer( m2 ), log.getLastTest().get( 1 ) );
    }
}
