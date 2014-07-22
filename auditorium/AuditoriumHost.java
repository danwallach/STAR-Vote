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
import sexpression.ListExpression;
import sexpression.Nothing;
import sexpression.StringExpression;
import verifier.InvalidLogEntryException;
import verifier.Verifier;
import verifier.auditoriumverifierplugins.HashChainVerifier;
import verifier.auditoriumverifierplugins.IncrementalAuditoriumLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Observer;

/**
 * This is the top level class that an application should interface with if it
 * wishes to use auditorium.<br>
 * <br>
 * In addition to providing the library API for accessing the auditorium
 * network, this class is the keeper of three threads. In order to make sure
 * these threads are running when they should be, only interleave calls to the
 * API between a call to start() and stop().<br>
 * <b>Announce</b>: This thread takes announcements placed on the queue by the
 * user, formats them as messages (this includes signing), floods them to the
 * network and puts them into the log. <br>
 * <b>Receive</b>: This thread takes messages that are heard by individual
 * links and does the appropriate thing with them (ignore or flood/tell
 * application). <b>Join</b>: This thread listens for join requests, responds
 * appropriately, and sets up link structures.<br>
 * <br>
 * All thread synchronization (including the three aforementioned threads and
 * each link thread) is done in this class. This means that all other auditorium
 * classes are not thread safe. This is done to simplify matters.
 * 
 * 
 * @author Kyle Derr
 * 
 */
public class AuditoriumHost implements IAuditoriumHost {

    /**
     * Get the IP address of this machine. This method returns the first
     * non-loopback or link local address it can find.
     * 
     * @return the IP address of this host.
     */
    public static String getMyIP() {

        /* Attempt to get a list of the network interfaces */
        Enumeration<NetworkInterface> interfaces;
        try { interfaces = NetworkInterface.getNetworkInterfaces(); }
        catch (SocketException e) { throw new FatalNetworkException("Cannot access network interfaces.", e ); }

        /* Iterate over the network interfaces */
        while (interfaces.hasMoreElements()) {

            /* Get the next interface */
            NetworkInterface i = interfaces.nextElement();

            /* Get the IP addresses of that interface */
            Enumeration<InetAddress> addresses = i.getInetAddresses();

            /* Iterate over the IP addresses */
            while (addresses.hasMoreElements()) {
                InetAddress a = addresses.nextElement();

                /* Take the first non-loopback address and return it's address (e.g. 192.168.0.1) */
                if (!(a.isLoopbackAddress())){
                	return a.getHostAddress();
                }
            }
        }
        throw new FatalNetworkException( "Cannot find a bound interface.", null );
    }

    /**
     * Messages that get passed to the application layer are wrapped with a
     * pointer to the host that they came from.
     * 
     * @author Kyle Derr
     */
    public static class Pair {
        /** A reference to the sender of the message */
        public final HostPointer from;

        /** the message to send */
        public final ASExpression message;

        /**
         * Constructs a new pair.
         *
         * @param from          the sender of the message
         * @param message       the message to send
         */
        public Pair(HostPointer from, ASExpression message) {
            this.from = from;
            this.message = message;
        }
    }

    /** The top layer of the network, in essence the head of a singly-linked list */
    private final IAuditoriumLayer head;

    /** A reference to the discover host, through which connections are made */
    private final AuditoriumDiscoveryHost discover;

    /** A collection of constants, like default ports, log file paths, etc. */
    private final IAuditoriumParams constants;

    /** Queue for all incoming messages */
    private final SynchronizedQueue<Pair> inQueue;

    /** Queue for all outgoing messages */
    private final SynchronizedQueue<ASExpression> outQueue;

    /** Queue for all messages waiting to be processed */
    private final SynchronizedQueue<Message> pendingQueue;

    /** Pointer to this class */
    private final HostPointer me;

    /** ID of this machine */
    private final String nodeID;

    /** List of all the hosts to which we're connected */
    private final ArrayList<Link> hosts;

    // Events
    /** Reference to an event that gets fired when a host joins the network  */
    private final Event<HostPointer> hostJoined;

    /** Reference to an event for when a host leaves the network */
    private final Event<HostPointer> hostLeft;

    // Verifier
    /** A verifier to dynamically audit and verify the logs we write out */
    private final Verifier verifier;

    /** The rules to feed to the verifier so it knows what to look for */
    private final ASExpression rule;

    /** Plugin to the verifier so it can incrementally audit the log */
    private final IncrementalAuditoriumLog verifierPlugin;

    /** Reference to the log, where events are logged and sent out */
    private final Log log;

    /** A counter to be used with the incremental log counter */
    private long counter;

    // Sockets
    /** The socket on which messages are received */
    private ServerSocket listenSocket;

    // Thread state
    /** A flag to denote if a thread is running */
    private volatile boolean running;

    /** A thread to denote the order that a message is sent out (essentially a monotonically increasing counter */
    private volatile long sequence;

    /**
     * Constructor.
     *
     * @param machineName The host needs to be given an ID unique across the network. There also needs to be an entry in the key store that corresponds to this ID.
     * @param constants   The host will get constant information from this instance (timeouts, file locations, etc.)
     */
    public AuditoriumHost(String machineName, IAuditoriumParams constants) {
        /* Initialize the info fields */
        nodeID = machineName;
        me = new HostPointer( machineName, getMyIP(), constants.getListenPort() );
        hosts = new ArrayList<>();
        
        /* Initialize the state fields */
        /* A mapping of all the layers of the network, referenced by their names */
        AuditoriumIntegrityLayer integrity = new AuditoriumIntegrityLayer(
                AAuditoriumLayer.BOTTOM, this, constants.getKeyStore() );

        head = new AuditoriumTemporalLayer(integrity, this);
        discover = new AuditoriumDiscoveryHost( this, constants );
        this.constants = constants;
        inQueue = new SynchronizedQueue<>();
        outQueue = new SynchronizedQueue<>();
        pendingQueue = new SynchronizedQueue<>();

        /* Initialize the events */
        hostJoined = new Event<>();
        hostLeft = new Event<>();

        /* Initialize the log auditor */
        ASExpression loadedRule = null;

        // Verifier
        try {
        	String ruleFile = constants.getRuleFile();
        	if (ruleFile != null) {
        		loadedRule = Verifier.readRule(constants.getRuleFile());
        	}
            log = new Log( new File( constants.getLogLocation() ) );
        }
        catch (FileNotFoundException e) {
            throw new FatalNetworkException( "Can't open file: "
                    + e.getMessage(), e );
        }
        

        /* Plugin to the verifier so it can ensure the integrity of logged messages */
        HashChainVerifier hashChainVerifier;
        if (loadedRule != null) {

            rule = loadedRule;
            hashChainVerifier = new HashChainVerifier();
            verifierPlugin = new IncrementalAuditoriumLog(hashChainVerifier);

            verifier = new Verifier(new HashMap<String, String>(), verifierPlugin);
        } else {
            Bugout.err("Verifier failed to successfully load the rule");
        	rule = null;
    		verifierPlugin = null;
			verifier = null;
        }

	        
        /* Initialize the thread state */
        running = false;
        sequence = 0;
    }

    /**
     * Start the threads.
     * 
     * @throws NetworkException thrown if ports couldn't be bound successfully.
     */
    public void start() throws NetworkException {
        Bugout.msg( "Host: STARTING" );

        /* Start the discover thread and set us to running */
        discover.start();
        running = true;

        /* Start ALL the threads! */
        new Thread( new Runnable() {

            public void run() {
                joinListenerThread();
            }

        } ).start();
        new Thread( new Runnable() {

            public void run() {
                announceThread();
            }

        } ).start();
        new Thread( new Runnable() {

            public void run() {
                receiveThread();
            }

        } ).start();
    }

    /**
     * Stop the threads.
     */
    public void stop() {
        /* If we aren't running, how can we stop? */
        if (!running) return;

        /* Note that we're stopping and update the state to reflect that */
        Bugout.msg( "Host: STOPPING" );
        running = false;
        discover.stop();
        disconnect();
        inQueue.releaseThreads();
        outQueue.releaseThreads();
        pendingQueue.releaseThreads();
        try {
            listenSocket.close();
        }
        catch (IOException ignored) {}

        /* Note the results of the auditing */
        if (verifier != null) {
        	System.out.println( "Verification result:" + verifier.eval( rule ) );
        }
    }

    /**
     * Disconnect all links, but continue running threads (and continue accepting join requests).
     */
    public synchronized void disconnect() {
        for (Link l : hosts)
            l.stop();
        hosts.clear();
    }

    /**
     * Discover if there are other hosts nearby. The discovery process is
     * carried out in the calling thread. Expect this call to block for 5
     * seconds.
     * 
     * @return      an array of pointers to nearby hosts.
     */
    public HostPointer[] discover() throws NetworkException {
        return discover.discover();
    }

    /**
     * Create a link to a specific host. The join is carried out in the calling
     * thread. Expect this call to block for a short amount of time.
     * 
     * @param host     Join this host
     */
    public void join(HostPointer host) throws NetworkException {
        /* Avoid any asynchronicity while we add a host */
        synchronized (this) {
            for (Link l : hosts)
                /* Make sure the host hasn't already joined us */
                if (l.getAddress().equals( host )) {
                    Bugout.msg( "Host: already joined " + host );
                    return;
                }
        }

        /* Send the join */
        Message joinMsg = new Message("join", me, nextSequence(), head.makeJoin( StringExpression.EMPTY ));
        MessageSocket socket = new MessageSocket(host, constants.getJoinTimeout());
        Bugout.msg("Host: sending join: " + new MessagePointer(joinMsg));
        socket.send(joinMsg);

        /* Receive the reply */
        Message joinReply;
        try {
            joinReply = socket.receive();
            head.receiveJoinReply(joinReply.getDatum());
        }
        catch (IncorrectFormatException e) {
            throw new NetworkException("Couldn't join, malformed reply", e);
        }
        Bugout.msg("Host: received reply: " + new MessagePointer(joinReply));

        /* Add the link for the new host */
        synchronized (this) {
            for (Link l : hosts) {
                if (l.getAddress().equals(joinReply.getFrom())) {
                    try {
                        socket.close();
                    } catch (IOException ignored) {
                    }
                    return;
                }
            }

            /* Create the link, start it, and add it */
            Link l = new Link( this, socket, joinReply.getFrom() );
            l.start();
            hosts.add( l );
        }

    }

    /**
     * Place an announcement on the wire and log it. This call returns quickly
     * and the behavior is carried out in an auditorium-managed worker thread.
     * 
     * @param announcement      Place this announcement on the wire.
     */
    public void announce(ASExpression announcement) {
        outQueue.push(announcement);
    }

    /**
     * Listen for a broadcast message, and return when it happens. This method
     * blocks unless/until messages have been/are received.
     * 
     * @return the broadcast message at the front of the queue if the queue is non-empty, or the next-heard broadcast message if it is empty.
     * @throws ReleasedQueueException thrown if stop is called while you're waiting for messages.
     */
    public Pair listen() throws ReleasedQueueException {
        return inQueue.pop();
    }

    /**
     * Register an observer to be notified when new nodes join this one.
     * 
     * @param observer       Register this observer. In the update method, expect that the argument will be of type HostPointer.
     */
    public void registerForJoined(Observer observer) {
        hostJoined.addObserver( observer );
    }

    /**
     * Register an observer to be notified when a particular node leaves the network.
     * 
     * @param observer       This observer will be notified when a host leaves the network.
     */
    public void registerForLeft(Observer observer) {
        hostLeft.addObserver( observer );
    }

    /**
     * @see auditorium.IAuditoriumHost#getMe()
     */
    public HostPointer getMe() {
        return me;
    }

    /**
     * @see auditorium.IAuditoriumHost#getNodeId()
     */
    public String getNodeId() {
        return nodeID;
    }

    /**
     * @see auditorium.IAuditoriumHost#getLog()
     */
    public Log getLog() {
        return log;
    }

    /**
     * @see auditorium.IAuditoriumHost#nextSequence()
     */
    public String nextSequence() {
        sequence++;
        return Long.toString( sequence );
    }

    /**
     * @see auditorium.IAuditoriumHost#getAddresses()
     */
    public ListExpression getAddresses() {
        // ArrayList<ASExpression> lst = new ArrayList<ASExpression>();
        // for (Link l : hosts)
        // lst.add( l.getAddress().toASE() );
        // lst.add( me.toASE() );
        // return new ListExpression( lst );
        throw new RuntimeException( "Do not use this method yet." );
    }

    /**
     * @see auditorium.IAuditoriumHost#receiveAnnouncement(auditorium.Message)
     */
    public void receiveAnnouncement(Message message) {
        pendingQueue.push(message);
    }

    /**
     * @see auditorium.IAuditoriumHost#removeLink(auditorium.Link)
     */
    public synchronized void removeLink(Link link) {
        link.stop();
        hosts.remove( link );
        hostLeft.notify( link.getAddress() );
    }


    /**
     * The thread for listening to Join requests
     */
    private void joinListenerThread() {
        /* Note the thread is starting */
        Bugout.msg("Listen: THREAD START");

        /* Try to bind the socket */
        try { listenSocket = new ServerSocket( constants.getListenPort() ); }
        catch (IOException e1) {
            Bugout.err("Couldn't bind socket.");
            return;
        }

        while (running) {
            /* Get an incoming socket connection. */
            MessageSocket socket;
            try {
                Bugout.msg( "Listen: waiting for connection on " + constants.getListenPort() );
                socket = new MessageSocket( listenSocket.accept() );
                Bugout.msg( "Listen: connection received." );
            }
            catch (NetworkException e) {
                /* If there is a network error, try again later */
                Bugout.err( "Listen: " + e.getMessage() );
                continue;
            }
            catch (IOException e) {
                /* If there's an IO problem we cannot continue */
                Bugout.err( "Listen: " + e.getMessage() );
                stop();
                break;
            }

            /* Get the join request, make the response. */
            Message jrq;
            try {
                jrq = socket.receive();
                Bugout.msg( "Listen: received " + new MessagePointer( jrq ) );

                /* If we don't get a join request, we can't do anything, so close the socket  */
                if (!jrq.getType().equals( "join" )) {
                    Bugout.err( "Listen: received non-join message" );
                    try { socket.close(); }
                    catch (IOException ignored) {}
                    continue;
                }
            }
            catch (NetworkException | IncorrectFormatException e) {
                /* If we can't write back, try to close the socket */
                Bugout.err( "Listen: " + e.getMessage() );
                try { socket.close(); }
                catch (IOException ignored) {}
                continue;
            }


            /* Send the join response, set up the auditorium link. */
            synchronized (this) {
                try { socket.send(new Message("join-reply", me, nextSequence(), head.makeJoinReply(Nothing.SINGLETON))); }
                catch (NetworkException e) {
                    /* If there's an error close the socket and try again */
                    try { socket.close(); }
                    catch (IOException ignored) {}
                    continue;
                }

                /* Build the new link, start it, and add it */
                Link l = new Link( this, socket, jrq.getFrom() );
                l.start();
                hosts.add( l );

                /* Update the observers for the new host */
                hostJoined.notify( jrq.getFrom() );
                Bugout.msg("Listen: Connection successful to " + l.getAddress());
            }
        }
        Bugout.msg("Listen: THREAD END");
        stop();
    }


    /**
     * Thread that handles announce events
     */
    private void announceThread() {
        Bugout.msg( "Announce: THREAD START" );
        while (running) {
            try {
                ASExpression announcement = outQueue.pop();
                synchronized (this) {
                    /* Make the announcement, sending it to the top-most network layer */
                    Message msg = new Message( "announce", me, nextSequence(), head.makeAnnouncement(announcement));

                    /* Broadcast the message by sending it to the log . */
                    Bugout.msg("Announce: flooding "
                            + new MessagePointer( msg ) 
							+ " (" + (announcement instanceof ListExpression ? ((ListExpression)announcement).get(0) : "<string>")
							+ " ...)");
                    logMessage( msg );
                }
            }
            catch (ReleasedQueueException ignored) {}
            catch (IOException e) { throw new FatalNetworkException("Can't serialize to the log file", e ); }
            Thread.yield();
        }
        Bugout.msg( "Announce: THREAD END" );
    }


    /**
     * Thread for handing incoming messages
     */
    private void receiveThread() {
        Bugout.msg( "Receive: THREAD START." );
        while (running) {
            try {

                /* Try to pop a message of the queue */
                Message message = pendingQueue.pop();

                /* Try to log and send the message */
                synchronized (this) {
                    Bugout.msg("Announce: flooding " + new MessagePointer(message));
                    logMessage(message);
                }
            }
            catch (ReleasedQueueException ignored) {}
            catch (IOException e) { throw new FatalNetworkException("can't serialize to log", e); }
            Thread.yield();
        }
        Bugout.msg( "Receive: THREAD END" );
    }

    /**
     * This broadcasts the message over the network.
     * Assume lock is already acquired!
     *
     * @param message       the message to send
     */
    private void flood(Message message) {
        ArrayList<Link> removeList = new ArrayList<>();

        /* iterate over the hosts and send the message to each of them */
        for (Link l : hosts) {
            try { l.getSocket().send( message ); }
            catch (NetworkException e) {
                /* If there is an error with this host, kick it out */
                removeList.add(l);
            }
        }

        /* Remove the hosts who could not receive the message*/
        for (Link l : removeList) {
            l.stop();
            hosts.remove( l );
        }
    }

    /**
     * This logs the message being sent, and also calls flood to broadcast the message
     * Assume lock is already acquired!
     *
     * @param message       the message to send and log
     */
    private void logMessage(Message message) throws IOException {

        /* Log the message and ensure it hasn't already been sent */
        if (log.logAnnouncement(message)) {

            /* verify that the message was logged properly*/
            try {
                verify( message );
            } catch (InvalidLogEntryException e) {
                throw new RuntimeException("Couldn't verify message: " + message.getDatum());
            }

            /* Now send the message */
            try {
                Bugout.msg("Host: logging and flooding: " + new MessagePointer(message));
                flood(message);
                ASExpression payload = head.receiveAnnouncement(message.getDatum());

                /* Put the message on the queue so its sending can be awaited */
                if (!inQueue.push(new Pair(message.getFrom(), payload))) {
                    /* If there was a problem with the queue, it is fatal */
                    Bugout.err("Receive: Application queue push fail");
                    stop();
                }
            } catch (IncorrectFormatException e) {
                Bugout.err( "Receive: malformed message:" + e.getMessage() );
            }
        }
    }

    /**
     * Add a message to the verifier's log data, and if the counter is a
     * multiple of 10, run the verifier.
	 *
	 * verify() is a no-op if no verifier is instantiated, which in turn
	 * occurs if no rule file is specified; see AuditoriumHost() and
	 * IAuditoriumParams.getRuleFile().
	 *
	 * TODO: 10 is totally arbitrary. This needs to be reconsidered (possibly
	 * parameterized, or removed entirely in the face of a new iterative
	 * verifier).  Also, some applications may want verification but not
	 * incremental (per-message) verification.
     * 
     * @param message
     *            Add this message.
     */
    private void verify(Message message) throws InvalidLogEntryException {
        counter++;

		if (verifier != null) {
			verifierPlugin.addLogData( message );
			if (counter % 10 == 0)
				verifier.eval( rule );
		}

    }
}
