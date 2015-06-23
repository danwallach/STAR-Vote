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

package auditorium.loganalysis;

import auditorium.Message;
import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.lexer.Lexer;
import sexpression.parser.Parser;
import sexpression.stream.ASEInputStreamReader;

import java.io.CharArrayReader;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Parse a log file and print out some simple information: <br>
 * [machine number]\n<br>
 * [message num] [message num] ... (if ** that means it's out of order)
 * 
 * [branch factor]:[branch number]
 * 
 * @author Kyle Derr
 * 
 */
public class LogCounter {

    /* FIXME This patter is probably broken with the new hash chain */
    /** The pattern string for reading logged messages */
    private static final String PATTERN_STRING = "(announce(host #string #string #string) #string " +
                                                 "(signed-message (cert " +
                                                 "(signature #string #string (key #string #string #string #string)))" +
                                                 "(signature #string #string " +
                                                 "(succeeds #list:(ptr #string #string #string) #any))))";

    /** Tha actual ASE pattern based on PATTERN_STRING */
    public static final ASExpression PATTERN = new Parser(new Lexer(new CharArrayReader(PATTERN_STRING.toCharArray()))).read();

    public static void main(String[] args) throws Exception {
        long count = 0;
        ASEInputStreamReader rd = new ASEInputStreamReader(
                new FileInputStream( args[0] ) );
        HashMap<String, ArrayList<Integer>> map = new HashMap<>();
        int[] branches = new int[1000];

        ASExpression read;

        while ((read = rd.read()) != null) {
            Message m = new Message(read);
            ArrayList<Integer> message;
            if (map.containsKey( m.getFrom().getNodeId() ))
                message = map.get( m.getFrom().getNodeId() );
            else {
                message = new ArrayList<>();
                map.put( m.getFrom().getNodeId(), message );
            }
            message.add( Integer.parseInt( m.getSequence() ) );
            ListExpression list = (ListExpression) PATTERN
                    .match( m.toASE() );
            branches[list.get( 12 ).size()]++;
            count++;
        }

        for (String key : map.keySet()) {
            System.err.println( key );
            ArrayList<Integer> lst = map.get( key );
            int tmp = lst.get( 0 );
            for (Integer i : map.get( key )) {
                System.err.print( i );
                if (i != tmp + 1)
                    System.err.print( "**" );
                System.err.print( " " );
                tmp = i;
            }
            System.err.println();
        }
        System.out.println( count );
        for (int lcv = 0; lcv < 1000; lcv++)
            if (branches[lcv] != 0)
                System.out.println( lcv + ":" + branches[lcv] );
    }
}
