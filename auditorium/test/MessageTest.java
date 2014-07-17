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

import auditorium.HostPointer;
import auditorium.IncorrectFormatException;
import auditorium.Message;
import org.junit.Test;
import sexpression.ListExpression;
import sexpression.NoMatch;
import sexpression.Nothing;
import sexpression.StringExpression;

import static org.junit.Assert.assertEquals;

/**
 * JUnit test of the auditorium.Message class.
 * 
 * @author Kyle Derr
 * 
 */
public class MessageTest {

    // ** <init>(String, HostPointer, ASExpression) **
    @Test
    public void test1constructor1() {
        Message msg = new Message( "message",
                new HostPointer( "id", "ip", 9000 ), "TEST",
                StringExpression.EMPTY );

        assertEquals( "message", msg.getType() );
        assertEquals( new HostPointer( "id", "ip", 9000 ), msg.getFrom() );
        assertEquals( StringExpression.EMPTY, msg.getDatum() );
        assertEquals( "TEST", msg.getSequence() );
        assertEquals( "(message (host id ip 9000) TEST )", msg.toString() );
        assertEquals( new ListExpression( StringExpression.makeString( "message" ),
                new HostPointer( "id", "ip", 9000 ).toASE(),
                StringExpression.makeString( "TEST" ), StringExpression.EMPTY ), msg
                .toASE() );
    }

    @Test
    public void test1constructor2() {
        Message msg = new Message( "otherMessage", new HostPointer( "otherID",
                "otherIP", 9000 ), "YARRGH", new ListExpression( "How", "Are",
                "you" ) );

        assertEquals( "otherMessage", msg.getType() );
        assertEquals( new HostPointer( "otherID", "otherIP", 9000 ), msg
                .getFrom() );
        assertEquals( new ListExpression( "How", "Are", "you" ), msg.getDatum() );
        assertEquals( "YARRGH", msg.getSequence() );
        assertEquals(
            "(otherMessage (host otherID otherIP 9000) YARRGH (How Are you))",
            msg.toString() );
        assertEquals( new ListExpression(
                StringExpression.makeString( "otherMessage" ), new ListExpression(
                        "host", "otherID", "otherIP", "9000" ),
                StringExpression.makeString( "YARRGH" ), new ListExpression( "How",
                        "Are", "you" ) ), msg.toASE() );
    }

    // ** <init>(ASExpression) tests **
    // junk
    @Test(expected = IncorrectFormatException.class)
    public void test2constructor1() throws Exception {
        new Message( Nothing.SINGLETON );
    }

    @Test(expected = IncorrectFormatException.class)
    public void test2constructor2() throws Exception {
        new Message( NoMatch.SINGLETON );
    }

    @Test(expected = IncorrectFormatException.class)
    public void test2constructor3() throws Exception {
        new Message( ListExpression.EMPTY );
    }

    @Test(expected = IncorrectFormatException.class)
    public void test2constructor4() throws Exception {
        new Message( StringExpression.EMPTY );
    }

    @Test(expected = IncorrectFormatException.class)
    public void test2constructor5() throws Exception {
        new Message( StringExpression.makeString( "TEST" ) );
    }

    @Test(expected = IncorrectFormatException.class)
    public void test2constructor6() throws Exception {
        new Message( new ListExpression( StringExpression.makeString( "TEST" ),
                StringExpression.makeString( "TEST" ) ) );
    }

    // [0] !instanceof String
    @Test(expected = IncorrectFormatException.class)
    public void test2constructor8() throws Exception {
        new Message( new ListExpression( ListExpression.EMPTY, new HostPointer(
                "test", "test", 1 ).toASE(), StringExpression.makeString( "test" ) ) );
    }

    // [1] !instanceof HostPointer
    @Test(expected = IncorrectFormatException.class)
    public void test2constructor9() throws Exception {
        new Message( new ListExpression( StringExpression.makeString( "test" ),
                new ListExpression( "notAPointer" ), StringExpression.makeString(
                        "test" ) ) );
    }

    // length > 4
    @Test(expected = IncorrectFormatException.class)
    public void test2constructor10() throws Exception {
        new Message( new ListExpression(
                StringExpression.makeString( "announcement" ), new HostPointer(
                        "test", "test", 1 ).toASE(), StringExpression.makeString(
                        "sequence" ), StringExpression.makeString( "datum" ),
                ListExpression.EMPTY, ListExpression.EMPTY ) );

    }

    // the datum is no longer optional!
    @Test(expected = IncorrectFormatException.class)
    public void test2constructor12() throws Exception {
        new Message( new ListExpression( StringExpression.makeString( "type" ),
                new HostPointer( "id", "ip", 1 ).toASE() ) );
    }

    // Good checking
    @Test
    public void test2constructor11() throws Exception {
        Message msg = new Message( new ListExpression( StringExpression.makeString(
                "type" ), new HostPointer( "id", "ip", 1 ).toASE(),
                StringExpression.makeString( "seq" ), StringExpression.makeString( "datum" ) ) );

        assertEquals( "type", msg.getType() );
        assertEquals( new HostPointer( "id", "ip", 1 ), msg.getFrom() );
        assertEquals( "seq", msg.getSequence() );
        assertEquals( StringExpression.makeString( "datum" ), msg.getDatum() );
        assertEquals( new ListExpression( StringExpression.makeString( "type" ),
                new HostPointer( "id", "ip", 1 ).toASE(), StringExpression.makeString(
                        "seq" ), StringExpression.makeString( "datum" ) ), msg.toASE() );
        assertEquals( "(type (host id ip 1) seq datum)", msg.toString() );
    }

    // ** getHash() tests **
    @Test
    public void testGetHash1() throws Exception {
        Message message = new Message( "type",
                new HostPointer( "id", "ip", 1 ), "TEST", NoMatch.SINGLETON );
        assertEquals( StringExpression.makeString( message.toASE().getSHA1() ),
            message.getHash() );
    }

    @Test
    public void testGetHash2() throws Exception {
        Message message = new Message( "type",
                new HostPointer( "id", "ip", 1 ), "TEST", Nothing.SINGLETON );

        assertEquals( StringExpression.makeString( message.toASE().getSHA1() ),
            message.getHash() );
    }

    @Test
    public void testGetHash3() throws Exception {
        Message message = new Message( "type",
                new HostPointer( "id", "ip", 1 ), "TEST",
                StringExpression.EMPTY );

        assertEquals( StringExpression.makeString( message.toASE().getSHA1() ),
            message.getHash() );
    }

    @Test
    public void testGetHash4() throws Exception {
        Message message = new Message( "type",
                new HostPointer( "id", "ip", 1 ), "TEST", ListExpression.EMPTY );

        assertEquals( StringExpression.makeString( message.toASE().getSHA1() ),
            message.getHash() );
    }
}
