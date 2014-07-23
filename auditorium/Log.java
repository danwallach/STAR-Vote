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

import sexpression.ASExpression;
import sexpression.StringExpression;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Log's job is to serialize messages that are heard over auditorium. In
 * addition, the log keeps data structures around that allow it to quickly
 * compute whether or not a given s-expression has been heard before, as well as
 * keep track of what the most recently heard but not pointed to messages are.
 * (This is useful for helping the temporal layer decide what messages should be
 * pointed to when said messages are being constructed.)
 * 
 * @author Kyle Derr
 */
public class Log {

    /** The file to which the log will be written */
    private final FileOutputStream location;

    /** A set of message pointers that have already been seen, so we don't have to handle them */
    private final HashSet<MessagePointer> haveSeen;

    /** A list of message pointers that have been heard by the log but not referenced by an actual message  */
    private final LinkedList<MessagePointer> last;

    /** A reference to that last hash value so we can chain the messages in the log */
    private ASExpression lastChainedHash;

    /**
     * Construct a Log instance that serializes log data to a given location.
     * 
     * @param location      The location that should be written to.
     *
     * @throws FileNotFoundException Thrown if the given location cannot be found.
     */
    public Log(File location) throws FileNotFoundException {
        this.location = new FileOutputStream( location );
        haveSeen = new HashSet<>();
        last = new LinkedList<>();

        /* Initialize that hash chain with string 0000000000 */
        lastChainedHash = StringExpression.makeString(StringExpression.makeString("0000000000").getSHA1());
    }

    /**
     * Call this method to check and see if a message has been seen before, and
     * if it has not, add it into the hash chain and log it.
     * 
     * @param       message The message in question. Log it if it hasn't been seen before.
     * @return      True if something new gets added to the log, and false otherwise.
     *
     * @throws IOException This method throws if there is an IO error when trying to add the message to the log file on disk.
     */
    public boolean logAnnouncement(Message message) throws IOException {
        /* Copy this message without a a reference to the hash chain for reference outside of the log  */
        Message copy = new Message(message.getType(), message.getFrom(), message.getSequence(), message.getDatum());

        /* Chain the hash values */
        message.chain(lastChainedHash);

        /* Update our reference to the chain */
        lastChainedHash = message.getChainedHash();

        MessagePointer toMessage = new MessagePointer( message );
        if (!haveSeen.contains(new MessagePointer(copy))) {

            /* Since the chained value is only used here, we update our lists with the unchained version */
            haveSeen.add(toMessage);
            last.add(toMessage);

            /* Write the chained value to the log */
            write(message);
            return true;
        }
        return false;
    }

    /**
     * @see this#logAnnouncement(Message)
     *
     * This is for testing purposes and shouldn't be used in any official capacity
     */
    public boolean logAnnouncementNoChain(Message message) throws IOException {

        MessagePointer toMessage = new MessagePointer( message );
        if (!haveSeen.contains(toMessage)) {

            /* Since the chained value is only used here, we update our lists with the unchained version */
            haveSeen.add(toMessage);
            last.add(toMessage);

            /* Write the chained value to the log */
            write(message);
            return true;
        }
        return false;
    }

    /**
     * Add a message to the "last" list. This message will be included in the
     * pointer set for the next message sent out.
     * 
     * @param message       Add this message.
     */
    public void updateLast(MessagePointer message) {
        last.add(message);
    }

    /**
     * Remove a pointer from the last list, if it exists in the list.
     * 
     * @param message       Remove this message from the last list.
     */
    public void removeFromLast(MessagePointer message) {
        last.remove(message);
    }

    /**
     * Get a list of messages that have been seen but not yet referenced.
     * Calling this method effectively clears the last list (before saving it as
     * a return value).
     * 
     * @return This method returns the last list.
     */
    public MessagePointer[] getLast() {
        MessagePointer[] ret = last.toArray(new MessagePointer[last.size()]);
        last.clear();
        return ret;
    }

    /**
     * Write messages to the log
     *
     * @param message       The message to write
     *
     * @throws IOException If something goes wrong in trying to write the message to the log, report it
     */
    private void write(Message message) throws IOException {
        location.write(message.toASEWithHash().toVerbatim());
        location.flush();
    }

    // ** Testing Methods ***
    /**
     * THIS METHOD IS ONLY USED FOR TESTING. Use the return value of
     * logAnnouncement to know whether or not you've seen a message (because the
     * lookup is done atomically with the store!)
     */
    public synchronized HashSet<MessagePointer> getSetCopyTest() {
        return new HashSet<>(haveSeen);
    }

    /**
     * THIS METHOD IS ONLY USED FOR TESTING. Use getLast() in practice, because
     * it gets the last set and then subsequently clears it in one atomic
     * operation.
     */
    public synchronized List<MessagePointer> getLastTest() {
        return new LinkedList<>(last);
    }
}
