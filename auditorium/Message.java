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
 * An instance of this class represents an auditorium wire message. Messages on
 * the wire are of the form ([name] [host] [sequence] [datum]).
 * 
 * @author Kyle Derr
 * 
 */
public class Message {

    /** Pattern for message ASE's, of the form ([name] [host] [sequence] [datum]) */
    public static final ASExpression PATTERN = new ListExpression(
            StringWildcard.SINGLETON, HostPointer.PATTERN,
            StringWildcard.SINGLETON, Wildcard.SINGLETON );

    /** Denotes the type of the message (e.g. "join", "join-reply", "discover", "discover-reply", or "announce")*/
    private final String type;

    /** The sender of the message */
    private final HostPointer from;

    /** The serial of the sender of the message*/
    private final String sequence;

    /** The contents of the message */
    private final ASExpression datum;

    /** A hash of the message object */
    private StringExpression hash = null;

    /**
     * Constructor
     *
     * @param type          Construct a message of this type. This should be one of "join", "join-reply", "discover", "discover-reply", and "announce".
     * @param from          Construct a message as being from this host.
     * @param sequence      Construct of a message that has this sequence number.
     * @param datum         Construct a message that has this datum.
     */
    public Message(String type, HostPointer from, String sequence, ASExpression datum) {
        this.type = type;
        this.from = from;
        this.sequence = sequence;
        this.datum = datum;
    }

    /**
     * Construct a message from its s-expression format.
     * 
     * @param message       Construct a message from this s-expression
     *
     * @throws IncorrectFormatException This method throws if the s-expression given is not in the correct format: ([name] [host] [sequence] [datum]).
     */
    public Message(ASExpression message) throws IncorrectFormatException {

        /* Attempt to match the pattern */
        if (PATTERN.match( message ) == NoMatch.SINGLETON)
            throw new IncorrectFormatException(message, new Exception(message + " didn't match the pattern:" + PATTERN));

        /* Extract data from the now-matched expression */
        ListExpression lst = (ListExpression) message;
        type = lst.get( 0 ).toString();
        from = new HostPointer( lst.get( 1 ) );
        sequence = lst.get( 2 ).toString();
        datum = lst.get( 3 );
    }

    /**
     * Convert this message into a form that can be placed on the wire.
     * 
     * @return This method returns ([name] [host] [datum]).
     */
    public ASExpression toASE() {
        return new ListExpression(StringExpression.makeString(type), from.toASE(), StringExpression.makeString(sequence), datum);
    }

    /**
     * Convert the message to an ASE, including its hash value. This is so messages with chained hash values can be
     * logged.
     *
     * @return Return an ASE of the form ([name] [host] [datum] [chained hash value])
     */
    public ASExpression toASTWithHash() {
        return new ListExpression(StringExpression.makeString(type), from.toASE(), StringExpression.makeString(sequence), datum, hash);
    }

    /**
     * Get the hash of this message.
     * 
     * @return The hash of this message.
     */
    public StringExpression getHash() {
        if (hash == null)
            hash = StringExpression.makeString( toASE().getSHA1() );
        return hash;
    }

    /**
     * Get the type field for this message.
     * 
     * @return The type field for this message.
     */
    public String getType() {
        return type;
    }

    /**
     * Get the from field for this message.
     * 
     * @return The from field for this message.
     */
    public HostPointer getFrom() {
        return from;
    }

    /**
     * Get the sequence number of this message.
     * 
     * @return The sequence number of this message.
     */
    public String getSequence() {
        return sequence;
    }

    /**
     * Get the datum field for this message.
     * 
     * @return The datum field for this message.
     */
    public ASExpression getDatum() {
        return datum;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return toASE().toString();
    }

    /**
     * Relies on MessagePointers equality method
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Message))
            return false;
        try {
            return new MessagePointer(toASE()).equals(new MessagePointer(((Message)o).toASE()));
        }
        catch (IncorrectFormatException e) {
            return false;
        }
    }

    /**
     * @param lastChainedHash the past hash that will be hashed along with this to form the hash chain
     */
    public void chain(StringExpression lastChainedHash) {
        String newData = getHash().toString() + lastChainedHash.toString();

        hash = StringExpression.makeString(StringExpression.makeString(newData).getSHA1());

    }
}
