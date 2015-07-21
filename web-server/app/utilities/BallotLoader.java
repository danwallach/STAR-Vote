package utilities;

import tap.BallotImageHelper;
import votebox.middle.ballot.BallotParserException;
import votebox.middle.ballot.RuntimeBallot;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: mrdouglass95
 * Date: 6/27/13
 * Time: 11:53 AM
 * To change this template use File | Settings | File Templates.
 */

/**
 * A collection of static members that are used to manage ballot files on the server.
 */
public class BallotLoader {

    private static HashMap<String, File> ballotMap = new HashMap<>();
    private static HashMap<String, List<List<String>>> raceMap = new HashMap<>();

    /**
     * Called by a boot-loader, startup task of play! framework. Extracts all ballot .zip files and generates
     * a mapping between precincts and their corresponding ballot files and race groups.
     */
    public static void init() {

        /* Open the ballot directory and get a listing */
        File ballotDirectory = new File("public/ballots/");
        File[] ballots = ballotDirectory.listFiles();

        if (ballots != null) {

            /* Cycle through the ballot files */
            for (File ballot1 : ballots) {

                /* Check for directory (needed) */
                if (ballot1.isDirectory()) {

                    /* --- Create a ballotMap --- */

                    /* Create a new zipfile based on the path of the directory */
                    File zipFile = new File(ballot1.getAbsolutePath() + ".zip");

                    /* Get the ballotName and precinct */
                    String ballotName = ballot1.getName();
                    String precinct = ballotName.substring(ballotName.length() - 3);

                    /* Map the zipFile to the precinct */
                    ballotMap.put(precinct, zipFile);

                    /* --- Create a raceMap --- */
                    RuntimeBallot ballot = null;

                    /* Try to get the ballot from the zipFile */
                    try { ballot = BallotImageHelper.getBallot(zipFile.getAbsolutePath()); }
                    catch (IOException | BallotParserException e) { e.printStackTrace(); }

                    /* Map the race groups to the precinct */
                    assert ballot != null;
                    raceMap.put(precinct, ballot.getRaceGroups());
                }
            }
        }
    }

    /**
     * @param name      a 3-digit precinct number
     * @return          a File object of the correct ballot for this precinct
     */
    public static File getBallotFileByPrecinct(String name) { return ballotMap.get(name); }

    /**
     * @param name      a 3-digit precinct number
     * @return          a List of the correct race groups for this precincts ballot
     */
    public static List<List<String>> getRaceGroupByPrecinct(String name) { return raceMap.get(name); }
}
