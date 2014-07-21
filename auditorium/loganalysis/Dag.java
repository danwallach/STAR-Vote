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

import auditorium.IncorrectFormatException;
import auditorium.Message;
import auditorium.MessagePointer;
import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.lexer.Lexer;
import sexpression.parser.Parser;
import sexpression.stream.ASEInputStreamReader;
import sexpression.stream.InvalidVerbatimStreamException;

import java.io.CharArrayReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Builds a representation of the dag of logs based on the auditorium file
 * format. The dag doesn't actually contain the messages, only pointers to them.
 * 
 * @author Kyle Derr
 * 
 */
public class Dag {

    /** The pattern string for reading logged messages */
    private static final String PATTERN_STRING = "(announce(host #string #string #string) #string " +
                                                 "(signed-message (cert " +
                                                 "(signature #string #string (key #string #string #string #string)))" +
                                                 "(signature #string #string " +
                                                 "(succeeds #list:(ptr #string #string #string) #any))) #any)";


    /** Tha actual ASE pattern based on PATTERN_STRING */
    private static final ASExpression PATTERN = new Parser(new Lexer(new CharArrayReader(PATTERN_STRING.toCharArray()))).read();

    /** log file to build the dag from */
    private final String filename;

    /** Map representation of the dag */
    private final HashMap<MessagePointer, ArrayList<MessagePointer>> dag;

    /** map of all the messages to their types */
    private final HashMap<MessagePointer, String> messageTypes;

    /**
     * Constructor. Initializes the fields.
     *
     * @param filename  Build the dag from an auditorium log file found at this path.
     */
    public Dag(String filename) {
        this.filename = filename;
        dag = new HashMap<>();
        messageTypes = new HashMap<>();
    }

    /**
     * Parse the file given at construct time and build the dag based on this file.
     */
    public void build() throws IOException, InvalidVerbatimStreamException, IncorrectFormatException {

        /* Read the file in as an ASE */
        ASEInputStreamReader reader = new ASEInputStreamReader(new FileInputStream(new File(filename)));

        /* Read in the ASEs contained in the file, one by one */
        ASExpression message;
        while ((message = reader.read()) != null) {

            /* build a message pointer based on the read-in message */
            MessagePointer ptr = new MessagePointer(new Message(message));

            /* List of message pointers that predate this message pointer */
            ArrayList<MessagePointer> predateList = new ArrayList<>();

            /* Match the expression to the pattern */
            ListExpression matchResult = (ListExpression) PATTERN.match(message);

            /* Pull out all of the message pointers corresponding to messages that predate this message */
            for (ASExpression ase : (ListExpression) matchResult.get( 12 ))
                predateList.add(new MessagePointer(ase));

            /* map this message to its type */
            messageTypes.put(ptr, ((ListExpression)matchResult.get(13)).get(0).toString());

            /* map the pointer to its predecessors */
            dag.put( ptr, predateList );
        }
    }

    /**
     * @return This method returns the dag structure that is wrapped by this instance.
     */
    public HashMap<MessagePointer, ArrayList<MessagePointer>> getDag() {
        return dag;
    }

    /**
     * @return This method gets the message type associated with a message pointer so graphing is clearer.
     */
    public HashMap<MessagePointer, String> getTypes(){
        return messageTypes;
    }

    /**
     * Query the dag for information about branch rates.
     * 
     * @return This method returns a map which represents the histogram of
     *         branch rates: rate->number (where rate is number of succeeds
     *         pointers and number is how many messages had this given number of
     *         pointers).
     */
    public HashMap<Integer, Integer> getBranchStatistics() {

        /* Intialize the return map */
        HashMap<Integer, Integer> ret = new HashMap<>();

        /* Iterate over the nodes in the graph (MessagePointers) */
        for (MessagePointer mp : dag.keySet()) {

            /* get the inflow to this node */
            int num = dag.get( mp ).size();

            /* Count how many times a node with this inflow has been seen */
            int freq = ret.containsKey(num) ? ret.get(num) + 1 : 1;
            ret.put(num, freq);
        }

        return ret;
    }
}
