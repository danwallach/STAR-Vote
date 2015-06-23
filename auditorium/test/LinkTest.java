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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.StringExpression;

import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.Assert.*;

/**
 * Tests for Auditorium Links
 *
 * @author Kyle Derr
 */
public class LinkTest {

    // the host will put stuff in here when its methods are called.
    private SynchronizedQueue<Message> received;
    private SynchronizedQueue<Link> links;

    // Use this stream to send shit to the Link.
    private volatile OutputStream stream;
    // This is the thing we're testing.
    private Link link;

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
            throw new RuntimeException( "unused" );
        }

        public void receiveAnnouncement(Message message) {
            received.push( message );
        }

        public void removeLink(Link link) {
            links.push( link );
        }

        public String nextSequence() {
            throw new RuntimeException( "unused" );
        }
    };

    // Don't touch this.
    @Before
    public void setup() throws Exception {
        Thread.sleep( 100 );
        received = new SynchronizedQueue<>();
        links = new SynchronizedQueue<>();

        new Thread( new Runnable() {

            public void run() {
                try {
                    ServerSocket serversocket = new ServerSocket( 9000 );
                    Socket toLink = serversocket.accept();
                    stream = toLink.getOutputStream();
                    serversocket.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    fail();
                }
            }
        } ).start();

        Thread.sleep( 100 );
        HostPointer hp = new HostPointer( "", "127.0.0.1", 9000 );
        link = new Link( host, new MessageSocket( hp, 8000 ), hp );
        link.start();
        Thread.sleep( 100 );
    }

    @After
    public void tear() throws Exception {
        Thread.sleep(100);
        stream.close();
        link.stop();
        Thread.sleep(100);
    }

    // Send random bytes
    @Test
    public void randomBytes() throws Exception {
        assertTrue( link.running() );
        stream.write( 234 );
        Thread.sleep( 100 );

        assertFalse( link.running() );

        assertEquals( 1, links.size() );
        assertEquals( 0, received.size() );

        assertSame( link, links.pop() );
    }

    // send s-expressions that aren't formatted well, make sure they don't
    // percolate to the top.
    @Test
    public void nonMessage1() throws Exception {
        assertTrue( link.running() );
        stream.write( ListExpression.EMPTY.toVerbatim() );
        Thread.sleep( 100 );

        assertFalse( link.running() );

        assertEquals( 0, links.size() );
        assertEquals( 0, received.size() );
    }

    @Test
    public void nonMessage2() throws Exception {
        assertTrue( link.running() );
        stream.write( new ListExpression( "Test", "Message" ).toVerbatim() );
        Thread.sleep( 100 );

        assertTrue( link.running() );

        assertEquals( 0, links.size() );
        assertEquals( 0, received.size() );
    }

    @Test
    public void nonMessage3() throws Exception {
        assertTrue( link.running() );
        stream.write( new ListExpression(
                StringExpression.makeString( "announcement" ), new ListExpression(
                        "nothost", "id", "ip", "9000" ), ListExpression.EMPTY )
                .toVerbatim() );
        Thread.sleep( 100 );

        assertTrue( link.running() );

        assertEquals( 0, links.size() );
        assertEquals( 0, received.size() );
    }

    // Send good expressions, check that they get received.
    @Test
    public void message1() throws Exception {
        Message m = new Message( "announcement", new HostPointer( "id", "ip",
                9000 ), "TEST", ListExpression.EMPTY );

        assertTrue( link.running() );
        stream.write( m.toASE().toVerbatim() );
        Thread.sleep( 100 );

        assertTrue( link.running() );

        assertEquals( 0, links.size() );
        assertEquals( 1, received.size() );

        assertEquals( m.toASE(), received.pop().toASE() );
    }

    // Check that when you close the socket, the thing stops running
    @Test
    public void closeSocket() throws Exception {
        stream.close();
        Thread.sleep( 1000 );
        assertFalse( link.running() );

        assertEquals( 1, links.size() );
        assertEquals( 0, received.size() );

        assertSame( link, links.pop() );
    }
}
