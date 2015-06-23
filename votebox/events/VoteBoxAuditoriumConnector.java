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

package votebox.events;

import auditorium.*;
import auditorium.AuditoriumHost.Pair;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

/**
 * The VoteBoxAuditoriumConnecter is the "middleman" that starts an instance of
 * Auditorium, handles the VoteBox messaging protocol, and generates events to
 * the application.
 * 
 * @author Corey Shaw
 */

public class VoteBoxAuditoriumConnector {

    /** A reference to the network */
    private final AuditoriumHost auditorium;

    /** A reference to the event dispatcher */
    private final VoteBoxEventNotifier notifier;

    /** A matcher for events that come across the network */
    private final VoteBoxEventMatcher matcher;

    /**
     * Constructs a new VoteBoxAuditoriumConnector with the given serial number
     * and matcher rules to check against incoming announcements. The connector will
     * initially only listen for join and leave events.
     * 
     * @param serial        this machine's serial number
     * @param params        parameters for configuring the connector
     * @param rules         the matchers for messages this machine needs
     * @throws NetworkException
     */
    public VoteBoxAuditoriumConnector(int serial, IAuditoriumParams params, String launchCode, MatcherRule... rules) throws NetworkException {

        /* Initialize the event notifier */
        notifier = new VoteBoxEventNotifier();

        /* Initialize the network  with the provided serial number and parameters*/
        auditorium = new AuditoriumHost( Integer.toString( serial ), params, launchCode );

        /* Initialize the message matcher */
        matcher = new VoteBoxEventMatcher( rules );

        /* Register the network to listen for and handle join events */
        auditorium.registerForJoined( new Observer() {
            public void update(Observable o, Object arg) {

                /* The arguments to this method will be the host that broadcast the join event */
                HostPointer host = (HostPointer) arg;

                /* Trigger the observers for the join event */
                notifier.joined(new JoinEvent(Integer.parseInt( host.getNodeId())));
            }
        } );

        /* Register the network to listen for and handle join events */
        auditorium.registerForLeft( new Observer() {
            public void update(Observable o, Object arg) {

                /* The arguments to this method will be the host that broadcast the join event */
                HostPointer host = (HostPointer) arg;

                /* Trigger the observers for the leave event */
                notifier.left( new LeaveEvent(Integer.parseInt(host.getNodeId())));
            }
        } );

        /* Start the network */
        auditorium.start();

        /* Start the event handler thread */
        initEventThread();
    }

    /**
     * Adds a listener for VoteBox events. The listeners are called in the order
     * that they are added.
     * 
     * @param l the listener
     */
    public void addListener(VoteBoxEventListener l) {
        notifier.addListener( l );
    }

    /**
     * Attempts to connect to an auditorium by running discover once.
     * 
     * @throws NetworkException
     */
    public void connect() throws NetworkException {
        connect( 0, 0 );
    }

    /**
     * Attempts to connect to an auditorium, and if no hosts are discovered,
     * will wait a number of seconds and then try again
     * 
     * @param delay         the wait time in between attempts to attempt connecting
     * @param repeats       the number of repeats (-1 to repeat forever)
     * @throws NetworkException
     */
    @SuppressWarnings("EmptyCatchBlock")
    public void connect(final int delay, final int repeats) throws NetworkException {

        /* Obtain the list of host pointers */
        HostPointer[] hosts = auditorium.discover();

        /* Iterate through each host pointer and attempt to join the network via that host */
        for (HostPointer host : hosts) {

            /* Only join hosts that aren't ourselves */
            if (!host.getNodeId().equals( auditorium.getNodeId() )) {
                try {
                    /* try to join. This will throw an error if it fails */
                    auditorium.join(host);

                    /* Now we've successfully joined, notify the observers */
                    notifier.joined(new JoinEvent(Integer.parseInt(host.getNodeId())));
                }

                /* If we catch an error, don't do anything, just go on to the next host */
                catch (NetworkException e) {}
            }
        }

        /* Repeat if necessary */
        if (hosts.length == 0 && repeats > 0 && delay > 0) {

            /* This timer will attempt to connect to the host based on the number of tries and interval specified. */
            /* TODO This functionality is never used, so should we change it? */
            Timer timer = new Timer( delay * 1000, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try { connect( delay, repeats - 1 ); }
                    catch (NetworkException e1) {
                    	/* NetworkException represents a recoverable error so just note it and continue */
                        System.out.println("Recoverable error occurred: "+e1.getMessage());
                        e1.printStackTrace(System.err);
                    }
                }
            } );
            timer.setRepeats( false );
            timer.start();
        }
    }

    /**
     * Broadcasts an announcement on the auditorium network. The event is then
     * fired, as if this node had heard the announcement from someone else. All
     * code that reacts to a message, whether sent by this machine or another,
     * should always exist in the listeners that are registered to the
     * VoteBoxAuditoriumConnector. This allows for an abstraction based on who
     * sent the message.
     * 
     * @param e the event to announce
     */
    public void announce(IAnnounceEvent e) {
        auditorium.announce( e.toSExp() );
        e.fire(notifier);
    }

    /**
     * Runs continuously in a dedicated thread for auditorium events for which this instance is configured to
     * hear (to hear a specific event, the events Matcher Rule must be sent in as part of the constructor's arguments)
     */
    private void initEventThread() {
        new Thread() {

            @Override
            public void run() {
                while (true) {
                    try {

                        Pair announce = auditorium.listen();
                        IAnnounceEvent event = matcher.match(Integer.parseInt( announce.from.getNodeId()), announce.message);
                        
                        if (event != null) event.fire(notifier);
                    }

                    catch (ReleasedQueueException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        }.start();
    }
}
