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

import auditorium.AuditoriumHost;
import auditorium.Bugout;
import auditorium.HostPointer;
import auditorium.ReleasedQueueException;
import sexpression.StringExpression;
import votebox.events.PollsClosedEvent;
import votebox.events.PollsOpenEvent;

/**
 * A test to see if Auditorium can handle lots of traffic
 *
 * @author Kyle Derr
 */
public class StressTest {

    private static volatile boolean running = true;


    /**
     * @param args ([serial])
     */
    public static void main(String[] args) throws Exception {
        Bugout.ERR_OUTPUT_ON = true;
        Bugout.MSG_OUTPUT_ON = true;
        AuditoriumHost host = new AuditoriumHost( /*args[0]*/ "0",
                TestParams.Singleton, "0000000000" );
        host.start();
        listenThread(host);
        HostPointer[] pointers = host.discover();
        System.err.println( "Discover complete...waiting" );
        Thread.sleep( 1000 );

        for (HostPointer ptr : pointers)
            host.join( ptr );

        host.announce(new PollsOpenEvent(0, 0, "keyword").toSExp());

        for (int lcv = 0; lcv < 100; lcv++) {
            Thread.sleep( 300 );
            host.announce( StringExpression.makeString( Integer.toString( lcv ) ) );
        }

        host.announce(new PollsClosedEvent(0, 1000).toSExp());

        System.err.println( "Done, letting the queues empty" );
        Thread.sleep( 30000 );
        System.err.println( "Killing." );
        running = false;
        host.stop();
    }

    private static void listenThread(final AuditoriumHost host) {
        new Thread( new Runnable() {

            public void run() {
                while (running)
                    try {
                        host.listen();
                    }
                    catch (ReleasedQueueException e) {
                        break;
                    }
            }
        } ).start();
    }
}
