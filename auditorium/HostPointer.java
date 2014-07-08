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

package auditorium;

import sexpression.*;

/**
 * An instance of this class simply wraps an ID, IP and port together. This
 * class represents a data structure in the auditorium format.
 * 
 * @author Kyle Derr
 * 
 */
public class HostPointer {

    /** The pattern for matching host pointers, of the form: (host [node-id] [ip] [port]) */
    public static final ASExpression PATTERN = new ListExpression(StringExpression.makeString("host"), StringWildcard.SINGLETON, StringWildcard.SINGLETON, StringWildcard.SINGLETON);

    /** The serial number of the machine to which this host pointer corresponds */
    private final String nodeID;

    /** The IP address of this host */
    private final String ip;

    /** The port of this host */
    private final int port;

    /**
     * Constructor.
     *
     * @param nodeID        the id of the host.
     * @param ip            the host in dotted decimal format.
     * @param port          the port that the host is listening on.
     */
    public HostPointer(String nodeID, String ip, int port) {
        this.nodeID = nodeID;
        this.port = port;
        this.ip = ip;
    }

    /**
     * Construct a new host address from its s-expression representation.
     *
     * @param hostExp       This should be of the format (host [id] [ip] [port])
     *
     * @throws IncorrectFormatException thrown if the given exp is not correctly formatted.
     */
    public HostPointer(ASExpression hostExp) throws IncorrectFormatException {
        try {
            /* Make sure the expression is a host pointer expression */
            ASExpression result = PATTERN.match( hostExp );
            if (result == NoMatch.SINGLETON) throw new IncorrectFormatException(hostExp, new Exception(hostExp + " didn't match the pattern " + PATTERN));

            /* fill in the fields from the expression */
            nodeID = ((ListExpression)result).get(0).toString();
            ip = ((ListExpression)result).get(1).toString();
            port = Integer.parseInt(((ListExpression)result).get(2).toString());

        } catch (NumberFormatException e) { throw new IncorrectFormatException( hostExp, e ); }
    }

    /**
     * Get the id of the node this references.
     *
     * @return      the node id.
     */
    public String getNodeId() {
        return nodeID;
    }

    /**
     * Get the IP of this host.
     *
     * @return      the IP of this host.
     */
    public String getIP() {
        return ip;
    }

    /**
     * Get the port of this host.
     *
     * @return      the port of this host.
     */
    public int getPort() {
        return port;
    }

    /**
     * Return the S-Expression representation of this host pointer.
     *
     * @return      (host nodeID ip port)
     */
    public ListExpression toASE() {
       return new ListExpression(StringExpression.makeString("host"), StringExpression.makeString(nodeID),
                                 StringExpression.makeString(ip), StringExpression.makeString(Integer.toString(port)));
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HostPointer))
            return false;
        HostPointer hpo = (HostPointer) o;

        return nodeID.equals( hpo.nodeID) && ip.equals(hpo.ip)
                && port == hpo.port;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return nodeID + "@" + ip + ":" + Integer.toString(port);
    }

}
