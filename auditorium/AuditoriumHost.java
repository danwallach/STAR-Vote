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
import sexpression.*;

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
     * @return This method returns the IP address of this host.
     */
    public static String getMyIP() {
        Enumeration<NetworkInterface> interfaces;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        }
        catch (SocketException e) {
            throw new FatalNetworkException(
                    "Cannot access network interfaces.", e );
        }
        Enumeration<InetAddress> addresses;
        NetworkInterface i;
        InetAddress a;
        while (interfaces.hasMoreElements()) {
            i = interfaces.nextElement();
            addresses = i.getInetAddresses();
            while (addresses.hasMoreElements()) {
                a = addresses.nextElement();
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
        public final HostPointer from;
        public final ASExpression message;

        public Pair(HostPointer from, ASExpression message) {
            this.from = from;
            this.message = message;
        }
    }

    // Core state
    private final HashMap<String, IAuditoriumLayer> layers;
    private final IAuditoriumLayer head;
    private final AuditoriumDiscoveryHost discover;
    private final IAuditoriumParams constants;
    private final SynchronizedQueue<Pair> inQueue;
    private final SynchronizedQueue<ASExpression> outQueue;
    private final SynchronizedQueue<Message> pendingQueue;

    // People
    private final HostPointer me;
    private final String nodeID;
    private final ArrayList<Link> hosts;

    // Events
    private final Event<HostPointer> hostJoined;
    private final Event<HostPointer> hostLeft;

    // Verifier
//    private final Verifier verifier;
//    private final ASExpression rule;
//    private final IncrementalAuditoriumLog verifierPlugin;
    private final Log log;

    // Sockets
    private ServerSocket listenSocket;

    // Thread state
    private volatile boolean running;
    private volatile long sequence;

    /**
     * @param machineName
     *            The host needs to be given an ID unique across the network.
     *            There also needs to be an entry in the keystore that
     *            corresponds to this ID.
     * @param constants
     *            The host will get constant information from this instance
     *            (timeouts, file locations, etc.)
     */
    public AuditoriumHost(String machineName, IAuditoriumParams constants) {
        // People
        nodeID = machineName;
        me = new HostPointer( machineName, getMyIP(), constants
                .getListenPort() );
        hosts = new ArrayList<>();
        
        //Core state
        layers = new HashMap<>();
        AuditoriumIntegrityLayer integrity = new AuditoriumIntegrityLayer(
                AAuditoriumLayer.BOTTOM, this, constants.getKeyStore() );

        AuditoriumTemporalLayer temporal = new AuditoriumTemporalLayer(
                integrity, this );
        layers.put( "Integrity", integrity );
        layers.put( "Temporal", temporal );
        head = temporal;
        discover = new AuditoriumDiscoveryHost( this, constants );
        this.constants = constants;
        inQueue = new SynchronizedQueue<>();
        outQueue = new SynchronizedQueue<>();
        pendingQueue = new SynchronizedQueue<>();

        // Events
        hostJoined = new Event<>();
        hostLeft = new Event<>();
        
        // ASExpression loadedRule = null;

        // Verifier
        try {
//        	String ruleFile = constants.getRuleFile();
//        	if (ruleFile != null) {
//        		loadedRule = Verifier.readRule( constants.getRuleFile() );
//        	}
            log = new Log( new File( constants.getLogLocation() ) );
        }
        catch (FileNotFoundException e) {
            throw new FatalNetworkException( "Can't open file: "
                    + e.getMessage(), e );
        }
        
        /*
        // XXX: remove comments when the verifier compiles again 
        if (loadedRule != null) {
        	rule = loadedRule;
	        verifierPlugin = new IncrementalAuditoriumLog();
	        verifier = new Verifier( new NoOpStackTraceWriter() );
	        verifier.registerPlugin( verifierPlugin );
        } else {
        	rule = null;
    		verifierPlugin = null;
			verifier = null;
        }
        */
	        
        // Thread state
        running = false;
        sequence = 0;
    }

    /**
     * Start the threads.
     * 
     * @throws NetworkException
     *             This method throws if ports couldn't be bound successfully.
     */
    public void start() throws NetworkException {
        Bugout.msg( "Host: STARTING" );
        discover.start();
        running = true;
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
        if (!running)
            return;

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

        /*
        // XXX: uncomment when verifier works
        if (verifier != null) {
        	System.out.println( "Verification result:" + verifier.eval( rule ) );
        }
        */
    }

    /**
     * Disconnect all links, but continue running threads (and continue
     * accepting join requests).
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
     * @return This method returns an array of pointers to nearby hosts.
     */
    public HostPointer[] discover() throws NetworkException {
        return discover.discover();
    }

    /**
     * Create a link to a specific host. The join is carried out in the calling
     * thread. Expect this call to block for a short amount of time.
     * 
     * @param host
     *            Join this host
     */
    public void join(HostPointer host) throws NetworkException {
        synchronized (this) {
            for (Link l : hosts)
                if (l.getAddress().equals( host )) {
                    Bugout.msg( "Host: already joined " + host );
                    return;
                }
        }

        // Send the join
        Message joinMsg = new Message( "join", me, nextSequence(), head
                .makeJoin( StringExpression.EMPTY ) );
        MessageSocket socket = new MessageSocket( host, constants
                .getJoinTimeout() );
        Bugout.msg( "Host: sending join: " + new MessagePointer( joinMsg ) );
        socket.send( joinMsg );

        // Receive the reply
        Message joinReply;
        try {
            joinReply = socket.receive();
            head.receiveJoinReply(joinReply.getDatum());
        }
        catch (IncorrectFormatException e) {
            throw new NetworkException( "Couldn't join, malformed reply", e );
        }
        Bugout.msg( "Host: received reply: " + new MessagePointer( joinReply ) );

        // Add the link
        synchronized (this) {
            for (Link l : hosts)
                if (l.getAddress().equals(joinReply.getFrom())) {
                    try {
                        socket.close();
                    }
                    catch (IOException ignored) {}
                    return;
                }
            Link l = new Link( this, socket, joinReply.getFrom() );
            l.start();
            hosts.add( l );
        }

    }

    /**
     * Place an announcement on the wire and log it. This call returns quickly
     * and the behavior is carried out in an auditorium-managed worker thread.
     * 
     * @param announcement
     *            Place this announcement on the wire.
     */
    public void announce(ASExpression announcement) {
        outQueue.push(announcement);
    }

    /**
     * Listen for a broadcast message, and return when it happens. This method
     * blocks unless/until messages have been/are received.
     * 
     * @return This method returns the broadcast message at the front of the
     *         queue if the queue is non-empty, or the next-heard broadcast
     *         message if it is empty.
     * @throws ReleasedQueueException
     *             This method throws if stop is called while you're waiting for
     *             messages.
     */
    public Pair listen() throws ReleasedQueueException {
        return inQueue.pop();
    }

    /**
     * Ask the host if it knows of a layer with the given name. If so, get a
     * reference to it.
     * 
     * @param layer
     *            Ask for a reference to the layer that has this name.
     * @return This method returns a reference to the layer which has the
     *         provided name, if such a layer exists
     * @throws LayerNotFoundException
     *             This method throws if no layer exists which matches the given
     *             name.
     */
    public IAuditoriumLayer getLayerByName(String layer)
            throws LayerNotFoundException {
        if (layers.containsKey( layer ))
            return layers.get( layer );
        throw new LayerNotFoundException( layer );
    }

    /**
     * Register an observer to be notified when new nodes join this one.
     * 
     * @param observer
     *            Register this observer. In the update method, expect that the
     *            argument will be of type HostPointer.
     */
    public void registerForJoined(Observer observer) {
        hostJoined.addObserver( observer );
    }

    /**
     * Register an observer to be notified when a particular node leaves the
     * network.
     * 
     * @param observer
     *            This observer will be notified when a host leaves the network.
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

    // Join thread.
    private void joinListenerThread() {
        Bugout.msg( "Listen: THREAD START" );
        try {
        	listenSocket = new ServerSocket( constants.getListenPort() );
        }
        catch (IOException e1) {
            Bugout.err( "Couldn't bind socket." );
            return;
        }

        while (running) {
            // Get an incoming socket connection.
            MessageSocket socket;
            try {
                Bugout.msg( "Listen: waiting for connection on "
                        + constants.getListenPort() );
                socket = new MessageSocket( listenSocket.accept() );
                Bugout.msg( "Listen: connection received." );
            }
            catch (NetworkException e) {
                Bugout.err( "Listen: " + e.getMessage() );
                continue;
            }
            catch (IOException e) {
                Bugout.err( "Listen: " + e.getMessage() );
                stop();
                break;
            }

            // Get the join request, make the response.
            Message jrq;
            try {
                jrq = socket.receive();
                Bugout.msg( "Listen: received " + new MessagePointer( jrq ) );
                if (!jrq.getType().equals( "join" )) {
                    Bugout.err( "Listen: received non-join message" );
                    try {
                        socket.close();
                    }
                    catch (IOException ignored) {}
                    continue;
                }
            }
            catch (NetworkException e) {
                Bugout.err( "Listen: " + e.getMessage() );
                try {
                    socket.close();
                }
                catch (IOException ignored) {}
                continue;
            }
            catch (IncorrectFormatException e) {
                Bugout.err( "Listen: " + e.getMessage() );
                try {
                    socket.close();
                }
                catch (IOException ignored) {}
                continue;
            }

            // Send the join response, set up the auditorium link.
            synchronized (this) {
                try {
                    socket.send( new Message( "join-reply", me,
                            nextSequence(), head
                                    .makeJoinReply( Nothing.SINGLETON ) ) );
                }
                catch (NetworkException e) {
                    try {
                        socket.close();
                    }
                    catch (IOException ignored) {}
                    continue;
                }
//                for (Link l : hosts)
//                    }
                Link l = new Link( this, socket, jrq.getFrom() );
                l.start();
                hosts.add( l );
                hostJoined.notify( jrq.getFrom() );
                Bugout.msg( "Listen: Connection successful to "
                        + l.getAddress() );
            }
        }
        Bugout.msg( "Listen: THREAD END" );
        stop();
    }

    // Announce thread
    private void announceThread() {
        Bugout.msg( "Announce: THREAD START" );
        while (running) {
            try {
                ASExpression announcement = outQueue.pop();
                synchronized (this) {
                    // Make the announcement
                    Message msg = new Message( "announce", me, nextSequence(),
                            head.makeAnnouncement( announcement ) );

                    // Flood the message.
                    Bugout.msg( "Announce: flooding "
                            + new MessagePointer( msg ) 
							+ " (" + (announcement instanceof ListExpression
									? ((ListExpression)announcement).get(0)
									: "<string>")
							+ " ...)");
                    logMessage( msg );
                }
            }
            catch (ReleasedQueueException ignored) {}
            catch (IOException e) {
                throw new FatalNetworkException(
                        "Can't serialize to the log file", e );
            }
            Thread.yield();
        }
        Bugout.msg( "Announce: THREAD END" );
    }

    // Receive thread
    private void receiveThread() {
        Bugout.msg( "Receive: THREAD START." );
        while (running) {
            try {
                Message message = pendingQueue.pop();

                synchronized (this) {
                    Bugout.msg( "Announce: flooding "
                            + new MessagePointer( message ) );
                    logMessage( message );
                }

            }
            catch (ReleasedQueueException ignored) {}
            catch (IOException e) {
                throw new FatalNetworkException( "can't serialize to log", e );
            }
            Thread.yield();
        }
        Bugout.msg( "Receive: THREAD END" );
    }

    /**
     * Assume lock is already acquired!
     */
    private void flood(Message message) {
        ArrayList<Link> removeList = new ArrayList<>();

        for (Link l : hosts) {
            try {
                l.getSocket().send( message );
            }
            catch (NetworkException e) {
                removeList.add(l);
            }
        }

        for (Link l : removeList) {
            l.stop();
            hosts.remove( l );
        }
    }

    /**
     * Assume lock is already acquired!
     */
    private void logMessage(Message message) throws IOException {
        if (log.logAnnouncement( message )) {
            verify( message );
            try {
                Bugout.msg( "Host: logging and flooding: "
                        + new MessagePointer( message ) );
                flood( message );
                ASExpression payload = head.receiveAnnouncement( message
                        .getDatum() );
                if (!inQueue.push( new Pair( message.getFrom(), payload ) )) {
                    Bugout.err( "Receive: Application queue push fail" );
                    stop();
                }
            }
            catch (IncorrectFormatException e) {
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
    private void verify(Message message) {
        /*
        // XXX: uncomment when verifier works
		if (verifier != null) {
			verifierPlugin.addLogData( message );
			if (counter % 10 == 0)
				verifier.eval( rule );
		}
		*/
    }
}
