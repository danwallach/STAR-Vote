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

import java.io.IOException;

/**
 * This class represents a single link in the group of outgoing links from a
 * given host. Each link has a listen thread associated with it. It's job is to
 * simply listen for incoming traffic on said link, and relay said traffic to
 * the host. The host will then, of course, decide what to do with it. Because
 * each link is a keeper of this listening thread, you must call start() and
 * stop() on it before it will behave in the expected way. The link's thread
 * will operate at a priority one less than the calling thread. This is to aid
 * auditorium in being able to keep up with many links flooding messages onto
 * its queues.
 * 
 * @author Kyle Derr
 * 
 */
public class Link {

    /** A reference to the host that holds this link */
    private final IAuditoriumHost host;

    /** The socket over which this link communicates */
    private final MessageSocket socket;

    /** The address of the host to which this link corresponds */
    private final HostPointer address;

    /** Denotes whether start() has been called on the link and its thread is running*/
    private volatile boolean running;

    /**
     * Construct a new auditorium link structure to wrap a socket that has
     * already been established with another auditorium host.
     * 
     * @param host          The AuditoriumHost that is using this link.
     * @param socket        The socket to the other auditorium host.
     * @param address       The address that the socket is connected to.
     */
    public Link(IAuditoriumHost host, MessageSocket socket, HostPointer address) {
        this.host = host;
        this.socket = socket;
        this.address = address;
        running = false;
    }

    /**
     * Start the thread.
     */
    public void start() {
        /* Note that we're starting on the console */
        Bugout.msg("Link " + address + ": STARTING");

        /* listenThread will stop immediately if not set here */
        running = true;

        /* Start the listening thread */
        Thread t = new Thread(new Runnable() {

            public void run() {
                listenThread();
            }
        });

        /* Establish the priority of this thread as one less than the host thread */
        t.setPriority(Thread.currentThread().getPriority() + 1);
        t.start();

    }

    /**
     * Stop the thread.
     */
    public void stop() {
        /* Note that we're stopping on the console */
        Bugout.err("Link " + address + ": STOPPING");

        /* Close the socket */
        running = false;
        try {
            socket.close();
        }
        catch (IOException e) {
            Bugout.err("Link " + address + ": while stopping: " + e.getMessage());
        }
    }

    /**
     * Get the address of the other end of this link.
     * 
     * @return The address of who this link is with.
     */
    public HostPointer getAddress() {
        return address;
    }

    /**
     * Get the message socket that is established for this link.
     * 
     * @return The message socket that is established for this link.
     */
    public MessageSocket getSocket() {
        return socket;
    }

    /**
     * Check if this link is currently running
     * 
     * @return Returns true if the link is running or false if it isn't.
     */
    public boolean running() {
        return running;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof Link && this.address.equals(((Link) o).address);
    }


    /**
     * Thread that will handle messages coming across the socket
     */
    private void listenThread() {

        /* Note that the thread is starting */
        Bugout.msg( "Link " + address + ": THREAD START" );

        /* Now read in data from the socket and pass it to the host */
        try {
            Message message;
            while (running && (message  = socket.receive()) != null) {
                Bugout.msg("Link " + address + ": received: " + new MessagePointer(message));
                host.receiveAnnouncement(message);
            }
        } catch (NetworkException e) {
            Bugout.err("Link " + address + ": " + e.getMessage());
        } catch (IncorrectFormatException e) {
            Bugout.err("Link " + address + ": received a message that is incorrectly formatted:" + e.getMessage());
        }

        /* If we exit the while loop, remove and close this link */
        host.removeLink(this);
        Bugout.msg("Link " + address + ": THREAD END");
        stop();
    }
}
