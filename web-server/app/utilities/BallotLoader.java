package utilities;
import tap.BallotImageHelper;
import votebox.middle.ballot.Ballot;
import votebox.middle.ballot.BallotParserException;
import votebox.middle.driver.Driver;

import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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

    private static HashMap<String, File> ballotMap = new HashMap<String, File>();
    private static HashMap<String, List<List<String>>> raceMap = new HashMap<String, List<List<String>>>();

    /**
     * called by a boot-loader, startup task of play! framework. Extracts all ballot .zip files and generates
     * a mapping between precincts and their corresponding ballot files and race groups.
     */
    public static void init(){
        File ballotDirectory = new File("public/ballots/");
        File[] ballots = ballotDirectory.listFiles();


        for(int i=0; i<ballots.length; i++){

            //directories are needed
            if(ballots[i].isDirectory()){
                //ballotMap
                File zipFile = new File(ballots[i].getAbsolutePath() + ".zip");
                String ballotName = ballots[i].getName();
                String precinct = ballotName.substring(ballotName.length()-3);
                ballotMap.put(precinct, zipFile);

                //raceMap
                Ballot ballot = null;
                try {
                    ballot = BallotImageHelper.getBallot(zipFile.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (BallotParserException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                raceMap.put(precinct, ballot.getRaceGroups());
            }
        }
    }

    /**
     * @param name 3-digit precinct number
     * @return a File object of the correct ballot for this precinct
     */
    public static File getBallotFileByPrecinct(String name){
        return ballotMap.get(name);
    }

    /**
     * @param name 3-digit precinct number
     * @return a List of the correct race groups for this precincts ballot
     */
    public static List<List<String>> getRaceGroupByPrecinct(String name){
        return raceMap.get(name);
    }
}
