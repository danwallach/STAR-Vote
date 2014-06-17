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

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;

import auditorium.*;

/**
 * Given a log file, generate a visual dag of the messages and print a
 * statistics file. The main takes a list of files to generate statistics about --
 * for each file A, A.pdf and A.stat will be generated. The pdf is the graphviz
 * generated visual DAG, and the stat file gives you some interesting
 * statistics.
 * 
 * @author Kyle Derr
 * 
 */
public class Graph {

    /**
     * @param args:
     *            this is a list of files to parse and output graphs of.
     */
    public static void main(String[] args) throws Exception {
        for (String s : args)
            graph( s );
    }

    public static void graph(String filePath) throws Exception {
        Dag d = new Dag( filePath );
        d.build();
        HashMap<MessagePointer, String> types = d.getTypes();
        GraphViz gvz = new GraphViz();
        gvz.addln( "digraph gr {" );
        gvz.addln( "nodesep=1.0;" );
        gvz.addln( "mode=\"circuit\";" );
        gvz.addln( "rankdir=\"TB\";" );
        gvz
                .addln( "edge [ arrowsize = .4, fontsize = 11, labeldistance = 1.5, labelfloat=\"false\"];" );

        for (MessagePointer mp : d.getDag().keySet()) {
            String str = "A" + mp.getNodeId() + "_" + mp.getNumber()
                    + " [label=\"" + mp.toString() + " " + types.get(mp) + "\"]";
            gvz.addln( str );
        }

        for (MessagePointer from : d.getDag().keySet()) {
            for (MessagePointer to : d.getDag().get( from )) {
                String str = "A" + to.getNodeId() + "_" + to.getNumber()
                        + " -> " + "A" + from.getNodeId() + "_" + from.getNumber()
                        + ";";
                gvz.addln( str );
            }
        }
        gvz.addln( "}" );

        String[] rawFileName = filePath.split(System.getProperty("file.separator"));
        String fileName = rawFileName[rawFileName.length-1];

        gvz.writeGraph( new File(  "logdata" + System.getProperty("file.separator") + fileName ) );
        stats( d, fileName );
    }

    public static void stats(Dag dag, String filePath) throws Exception {
        String[] rawFileName = filePath.split(System.getProperty("file.separator"));
        String fileName = rawFileName[rawFileName.length-1];

        PrintWriter writer = new PrintWriter( new FileOutputStream( new File( "logdata" + System.getProperty("file.separator") +
                fileName + ".stat" ) ) );
        HashMap<Integer, Integer> stats = dag.getBranchStatistics();
        for (Integer i : stats.keySet())
            writer.write( i + ":" + stats.get( i ) + "\n" );
        writer.close();
    }
}
