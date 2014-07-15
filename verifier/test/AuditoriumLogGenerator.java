package verifier.test;

import auditorium.*;
import votebox.AuditoriumParams;
import votebox.events.SupervisorEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * This will generate an auditorium log with a prescribed set of contents, so things like hash chaining and
 * verification can be tested.
 *
 * @author Matt Bernhard
 */
public class AuditoriumLogGenerator {

    /** This is the log object that we will use to generate the file */
    private static Log log;

    /** This will ensure that everything will be written to the log by the same host */
    private static HostPointer hp;

    private static AuditoriumIntegrityLayer integrityLayer;

    private static AuditoriumTemporalLayer temporalLayer;

    private static IKeyStore ks;


    /**
     * Set up the generator, including initializing the Log with the log file
     *
     * @param filePath the file where the log will be written
     */
    public static void setUp(String filePath) {

        AuditoriumHost host = new AuditoriumHost("0", new AuditoriumParams(""));

        ks = new SimpleKeyStore("keys");
        integrityLayer = new AuditoriumIntegrityLayer(AAuditoriumLayer.BOTTOM, host, ks);
        temporalLayer = new AuditoriumTemporalLayer(integrityLayer, host);


        hp = new HostPointer("0", "127.0.0.1", 9000);
        try {
            log = new Log(new File(filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * A method that will simply write one event to a log
     */
    public static void generateSimpleLog() throws IOException, IncorrectFormatException {
        SupervisorEvent e = new SupervisorEvent(0, 0, "activated");

        log.logAnnouncement(new Message("announce", hp, "0", temporalLayer.makeAnnouncement(e.toSExp())));
    }
}
