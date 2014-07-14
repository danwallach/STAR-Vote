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
 * This class represents the ptr auditorium data structure. Its format is (ptr [nodeID] [sequence num] [hash]).
 *
 * @author Kyle Derr
 *
 */
public class MessagePointer {

    /** Pattern to match ASE message pointers, of the format (ptr [nodeID] [sequence num] [hash]) */
    public static final ASExpression PATTERN = new ListExpression(
            StringExpression.makeString( "ptr" ), StringWildcard.SINGLETON,
            StringWildcard.SINGLETON, StringWildcard.SINGLETON );

    /** A null reference for this object */
    public static final MessagePointer NULL = new MessagePointer( "", "",
            StringExpression.EMPTY );

    /** The serial of the machine that sent the message this points to */
    private final String nodeID;

    /** The place in teh sequence of messages in which this message was sent */
    private final String number;

    /** A hash of the message this points to */
    private final StringExpression hash;

    /** The ASE representation of this pointer, for lazy evaluation */
    private ASExpression aseForm;

    /**
     * Constructor.
     *
     * @param machine       Point to this machine.
     * @param number        Point to this message number from machine.
     * @param hash          The hash of the message this points to.
     */
    public MessagePointer(String machine, String number, StringExpression hash) {
        nodeID = machine;
        this.number = number;
        this.hash = hash;
    }

    /**
     * Convert a message pointer from its s-expression form to it's object form.
     *
     * @param exp       Convert this s-expression.

     * @throws IncorrectFormatException This method throws if the given expression is not (ptr [nodeID] [sequence num] [hash]).
     */
    public MessagePointer(ASExpression exp) throws IncorrectFormatException {

        /* Try to match the ASE */
        ASExpression result = PATTERN.match(exp);
        if (result == NoMatch.SINGLETON)
            throw new IncorrectFormatException(exp, new Exception(exp + " doesn't match the pattern: " + PATTERN));

        /* Extract the information from the ASE */
        nodeID = ((ListExpression)result).get(0).toString();
        number = ((ListExpression)result).get(1).toString();
        hash = (StringExpression)((ListExpression)result).get(2);
    }

    /**
     * Construct a pointer to a given message.
     *
     * @param message Construct a pointer to this message.
     */
    public MessagePointer(Message message) {
        nodeID = message.getFrom().getNodeId();
        hash = message.getHash();
        number = message.getSequence();
    }

    /**
     * Get the machine ID of the message this points to.
     *
     * @return This method returns the machine ID of the message this points to.
     */
    public String getNodeId() {
        return nodeID;
    }

    /**
     * Get the message number of the message that this points to.
     *
     * @return The message number of the message that this points to.
     */
    public String getNumber() {
        return number;
    }

    /**
     * Get the hash of the message that this points to.
     *
     * @return The hash of the message that this points to.
     */
    public StringExpression getHash() {
        return hash;
    }

    /**
     * Convert this message pointer to its s-expression form.
     *
     * @return This message pointer in its s-expression form.
     */
    public ASExpression toASE() {
        if (aseForm == null)
            aseForm = new ListExpression(
                    StringExpression.makeString( "ptr" ), StringExpression
                            .makeString(nodeID), StringExpression
                            .makeString(number), hash);

        return aseForm;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MessagePointer))
            return false;

        MessagePointer other = (MessagePointer) o;
        return this.number.equals( other.number)
                && this.nodeID.equals( other.nodeID)
                && this.hash.equals( other.hash);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return hash.hashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "{machine:" + nodeID + " message:" + number + "}";
    }
}
