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

import auditorium.Certificate;
import auditorium.HostPointer;
import auditorium.Signature;
import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.StringExpression;
import sexpression.StringWildcard;
import sexpression.stream.ASEInputStreamReader;

import java.io.File;
import java.io.FileInputStream;



/**
 *Some odd debugging function for taking apart logs. I don't know why -Matt Bernhard
 *
 * @author Kyle Derr
 */
public class PointerExtractor {

    // 0: announce
    // 1: (host id ip port)
    // 2: sequence
    // 4: cert
    // 5: signature
    // 6: list-of-pointers
    // 7: datum
    public static final ASExpression PATTERN = new ListExpression(
            StringExpression.makeString( "announce" ), HostPointer.PATTERN, StringWildcard.SINGLETON,
            new ListExpression(StringExpression.make("signed-message"), Certificate.PATTERN, Signature.PATTERN) );

    private final String path;

    public PointerExtractor(String path) {
        this.path = path;
    }

    public void extract() throws Exception {
        ASEInputStreamReader reader = new ASEInputStreamReader(new FileInputStream(new File(path)));

        ASExpression read;
        while ((read = reader.read()) != null) {

            System.out.println("############################################################################");
            try {
                ListExpression result = (ListExpression) PATTERN.match( read );
                System.out.println("------------------------------------------------------------------");
                System.out.println( "ID:" + result.get( 0 ) + " SEQUENCE:"
                        + result.get( 3 ) + " MESSAGE:" + ((ListExpression)result.get(7)).get(2) );
                System.out.println( "Pointers:" );
                System.out.println(">>>>>>>>>>>>>" + result.get(7));

//                result = result.get(7).
                for (ASExpression ase : (ListExpression) ((ListExpression)result.get(7)).get(1)) {
                    ListExpression le = (ListExpression) ase;
                    System.out.println( "    " + le.get( 1 ) + " / "
                            + le.get( 2 ) );
                }

                System.out.println("------------------------------------------------------------------");
            }
            catch (ClassCastException e) {
                System.out.println( "Skipping malformed expression" );
                System.out.println("********************************************************************");
                System.out.println(read);
                System.out.println("********************************************************************");
            }
            System.out.println("############################################################################");
            System.out.println("\n\n\n");

        }

    }


    /**
     * @param args list of names of log files of any length
     */
    public static void main(String[] args) {
        System.out.println( "Reading files" );
        for (String s : args)
            try {
                new PointerExtractor(s).extract();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
    }
}
