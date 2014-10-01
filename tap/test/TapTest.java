package tap.test;

import auditorium.IAuditoriumParams;
import junit.framework.TestCase;
import tap.Tap;
import votebox.AuditoriumParams;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * A class for testing various aspects of tap.
 *
 * @author Matt Bernhard
 */
public class TapTest extends TestCase {

    private Tap tap;

    /* Create a new socket address */
    InetSocketAddress addr;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        addr = new InetSocketAddress("starvote.cs.rice.edu", 80);
        /* Try to establish a socket connection */
        Socket localCon = new Socket();
        localCon.connect(addr);

        IAuditoriumParams params = new AuditoriumParams("tap.conf");
        //tap = new Tap(0, localCon.getOutputStream(), "launchCode", params);



    }


    public void testBallotDump() {
        List<String> ballotList = new ArrayList<>();

        for(int i = 0; i < 10; i++)
            ballotList.add("ballot" + i);

        //tap.uploadToServer(ballotList);
    }
}
