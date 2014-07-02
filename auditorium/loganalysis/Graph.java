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

import java.io.*;
import java.util.HashMap;

import auditorium.*;
import sexpression.stream.InvalidVerbatimStreamException;

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
@SuppressWarnings("WeakerAccess")
public class Graph {

    /**
     * @param args list of files to parse and output graphs of.
     */
    public static void main(String[] args) throws Exception {
        for (String s : args) graph(s);
    }

    /**
     * Build a DAG and then graph it using GraphViz
     *
     * @param filePath      the location of the Auditorium log to graph
     *
     * @throws IOException if there is an issue opening the file
     * @throws InvalidVerbatimStreamException if there is an issue reading the file (i.e. the data is corrupted)
     * @throws IncorrectFormatException if the data is not correctly formatted
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void graph(String filePath) throws IncorrectFormatException, IOException, InvalidVerbatimStreamException {

        /* Construct a DAG using teh file */
        Dag d = new Dag(filePath);
        d.build();
        HashMap<MessagePointer, String> types = d.getTypes();

        /* Initialize GraphViz */
        GraphViz gvz = new GraphViz();

        /* this is a directed graph named gr */
        gvz.addln( gvz.start_graph() );

        /* separate the nodes by at least one inch */
        gvz.addln( "nodesep=1.0;" );

        /* use circuit resistance model to compute distances between nodes  */
        gvz.addln( "model=\"circuit\";" );

        /* Set the direction of the graph as top-to-bottom*/
        gvz.addln( "rankdir=\"TB\";" );

        /*
         * set the edge properties so that arrows are of size .4 (out of 1),
         * font sized 11, set the head label at a distance scale of 1.5, and label using whole integer values
         */
        gvz.addln("edge [ arrowsize = .4, fontsize = 11, labeldistance = 1.5, labelfloat=\"false\"];");

        /* Iterate over the message pointers and add them as nodes to the graph */
        for (MessagePointer mp : d.getDag().keySet()) {

            /* Create a node, with the given ID and order number, and with a label corresponding to the message pointer's name and type */
            String str = "A" + mp.getNodeId() + "_" + mp.getNumber() + " [label=\"" + mp.toString() + " " + types.get(mp) + "\"]";
            gvz.addln( str );
        }

        /* Now iterate through the nodes and draw directed edges between them */
        for (MessagePointer from : d.getDag().keySet()) {
            /* Note that this is O(N^2) */
            for (MessagePointer to : d.getDag().get( from )) {

                /* Create the labels indicating which nodes have an edge between them, and its direction */
                String str = "A" + to.getNodeId() + "_" + to.getNumber() + " -> " + "A" + from.getNodeId() + "_" + from.getNumber() + ";";
                gvz.addln( str );
            }
        }

        /* close the graphViz file */
        gvz.addln( gvz.end_graph() );

        /* Set up the directories and files to write the graph */
        String[] rawFileName = filePath.split(System.getProperty("file.separator"));
        String fileName = rawFileName[rawFileName.length-1];

        File dir = new File("logdata" + System.getProperty("file.separator") + fileName );
        dir.mkdirs();

        /* Write the graph out */
        gvz.writeGraph(dir);

        /* write out the graph's stats */
        stats( d, fileName );
    }

    /**
     * Writes out the statistics of the graph graphed using
     * @see Graph#graph(String)
     *
     * @param dag           the dag to generate statistics for
     * @param filePath      a file to write the results into
     *
     * @throws FileNotFoundException if the results file can't be found or created
     */
    public static void stats(Dag dag, String filePath) throws FileNotFoundException {
        /* Get out the file name */
        String[] rawFileName = filePath.split(System.getProperty("file.separator"));
        String fileName = rawFileName[rawFileName.length-1];

        /* Build the .stat file */
        File dir = new File("logdata" + System.getProperty("file.separator") + fileName + ".stat");

        /* Prepare to write data to the file */
        PrintWriter writer = new PrintWriter( new FileOutputStream(dir) );

        /* get the statistics out of the dag */
        HashMap<Integer, Integer> stats = dag.getBranchStatistics();

        /* Iterate over the stats and write out the branch rates */
        for (Integer i : stats.keySet())
            writer.write( i + ":" + stats.get( i ) + "\n" );

        /* close the file */
        writer.close();
    }
}
