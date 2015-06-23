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
import auditorium.HostPointer;
import sexpression.StringExpression;

/**
 * Test class that counts the number of packets received through Auditorium. On reaching 10 packets, it terminates.
 *
 * @author Kyle Derr
 */
public class Incrementer {

    public static void main(String[] args) throws Exception {
        AuditoriumHost host = new AuditoriumHost(args[0], TestParams.Singleton, "0000000000");
        host.start();
        for (HostPointer hp : host.discover())
            host.join( hp );

        if (args.length == 2)
            host.announce( StringExpression.makeString( "1" ) );

        while (true) {
            int rec = Integer.parseInt( new String( ((StringExpression) host
                    .listen().message).getBytesCopy() ) );

            if (rec == 9)
                break;

            rec++;
            host.announce( StringExpression
                    .makeString( Integer.toString( rec ) ) );

            if (rec == 10)
                break;
        }

        host.stop();
    }
}
