package controllers;

import auditorium.SimpleKeyStore;
import crypto.AHomomorphicCiphertext;
import crypto.BallotCrypto;
import crypto.EncryptedRaceSelection;
import crypto.PlaintextRaceSelection;
import crypto.adder.AdderPrivateKeyShare;
import models.*;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import sexpression.stream.Base64;
import supervisor.model.Ballot;
import supervisor.model.Precinct;
import utilities.AdderKeyManipulator;
import utilities.WebServerTallier;
import views.html.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;

import static play.data.Form.form;

/**
 * AuditServer is the main controller for the STAR-Vote Web-Sever project. It handles most of the HTTP requests
 * from the routes file. All pages are fed through this controller, and POST requests for pushing up challenged
 * and cast ballots are also handled here
 *
 * @author Matt & Matt
 */
public class AuditServer extends Controller {

    /* Forms for searching for ballots in DBs */
    static Form<ChallengedBallot> challengeForm = form(ChallengedBallot.class);
    static Form<CastBallot> confirmForm = form(CastBallot.class);

    static boolean init = false;

    /**
     * Serves the Home Page of the site
     *
     * @return      the home page of the site
     */
    public static Result index() {
        //loadTestRecords();
        return ok(index.render()); 
    }

    /**
     * Loads testing data for the webserver
     */
    private static void loadTestRecords() {

        Map<String, Map<String, Precinct>> records = new HashMap<>();

        for(int i = 0; i < 3; i++) {

            Map<String, Precinct> hashes = new HashMap<>();

            for(int j = 0; j < 3; j++)
                hashes.put(j+"", new Precinct(j+"", ""));

            records.put("record" + i, hashes);

            VotingRecord.create(new VotingRecord("Precinct" + i, records));
        }

        for(int i = 4; i < 7; i++) {

            records = new HashMap<>();

            Map<String, Precinct> hashes = new HashMap<>();

            hashes.put("1", new Precinct("1", ""));

            records.put("record" + i, hashes);

            VotingRecord.create(new VotingRecord("Precinct"+ i, records));
        }

    }

    /**
     * Page for requesting cast ballot hash lookup for confirming cast ballots.
     *
     * @return      page for requesting cast ballot hash lookup
     */
    public static Result confirm() {
        return ok(confirmballot.render(CastBallot.all(), confirmForm, null));
    }

    /**
     * Serves up the About Us page, accessible from menu bar.
     *
     * @return      the About Us page
     */
    public static Result aboutUs() {
        return ok(aboutUs.render());
    }

   /**
     * Confirms ballot was cast by looking for hash in cast ballot database
     *
     * @param bid       the unique ballot identifier for the ballot to get
     * @return          the page for confirmed/cast ballots viewing
     */
    public static Result getCastBallot(String bid) {

        String errorCode = "**Could not locate a cast ballot with Ballot Identification Number (BID): " + bid +
                           ". If the BID you entered is correctly displayed, please contact your local election office.**";

        /* Database lookup */
        CastBallot ballot = CastBallot.getBallot(bid);

        /* Make sure the ballot is okay, then search for it, otherwise return an error page */
        if (ballot != null) return ok(castballotfound.render(ballot, bid));
        else                return ok(confirmballot.render(CastBallot.all(), confirmForm, errorCode));
    }





    /*---------------------------------------- ADMIN METHODS -----------------------------------------------*/

    /**
     * Verifies that the username and password entered at the admin login screen are correct
     *
     * @return      the admin page of the website
     */
    public static Result adminverify() {
        return ok(adminlogin.render(form(Login.class), null));
    }

    @Security.Authenticated(AdminSecured.class)
    public static Result adminmain() {
        return ok(adminmain.render());
    }
    
    /**
     * Verifies that the admin is currently logged in, then clears data from the ebean database.
     * Serves up admin page with success message if admin logged in, or error page if admin not logged in.
     *
     * @return      the admin page with success/error dependent on login success
     */
    @Security.Authenticated(AdminSecured.class)
    public static Result adminclear() {

        String message = "No data to clear!";

        if(CastBallot.all().size() > 0 || ChallengedBallot.all().size() > 0) {
            /* Destroy all the cast ballot / challenged ballot info from the database */
            for (CastBallot cb: CastBallot.all())  CastBallot.remove(cb);
            for (ChallengedBallot cb: ChallengedBallot.all()) ChallengedBallot.remove(cb);
            
            
            message = "";
        }
        
        /* Send to the data cleared page */
        return ok(adminclear.render(message));
    }
    
    /**
     *  Generates and renders the page for handling conflicts
     */
    @Security.Authenticated(AdminSecured.class)
    public static Result adminconflicts() {
        return ok(adminconflicts.render(VotingRecord.getConflicted()));
    }
    
    @Security.Authenticated(AdminSecured.class)
    public static Result resolveconflict(String id, String hash) {

        hash = hash.substring(0, hash.length()-1);
        VotingRecord.getRecord(id).resolveConflict(hash);
        return ok(adminconflicts.render(VotingRecord.getConflicted()));
    }

    /**
     * Generates and renders the page for publishing results 
     */    
    @Security.Authenticated(AdminSecured.class)
    public static Result adminpublish() {
        return ok(adminpublish.render(VotingRecord.getUnpublished(), VotingRecord.getPublished()));
    }
    
    @Security.Authenticated(AdminSecured.class)
    public static Result publishresults() {

        /* Reverse routing */
        String records = request().getQueryString("records");

        int start = 0;

        Map<String, Ballot<EncryptedRaceSelection>> summedTotals = getSummedTotals();
        Map<String, List<Ballot<EncryptedRaceSelection>>> precinctTotals = new TreeMap<>();
        Map<String, Precinct> precinctMap = new TreeMap<>();
        
        /* Grab each selected precinct and publish it */
        while(start < records.length()) {

            int end = records.indexOf(",", start);

            if (end == -1)
                end = records.length();

            /* Get the precinct ID from the query string */
            String precinctID = records.substring(start, end);

            System.out.println(precinctID);

            /* Pull out the current record to be published */
            VotingRecord vr = VotingRecord.getRecord(precinctID);
            
            /* Publish the record */
            vr.publish();
            
            /* Get the Precincts from this VotingRecord (Encrypted!) */
            System.out.println("Getting the Precinct Map: ");
            precinctMap = vr.getPrecinctMap();

            /* Add the results from the Precincts in this VotingRecord to precinctTotals */
            System.out.println("Updating Precinct Totals: ");
            precinctTotals = updatePrecinctTotals((Map)precinctMap);

            /* Move on to the next VotingRecord to be published */
            start = end+1;
        }

        /* Combine the newly published result totals with the old totals */
        System.out.println("Updating Summed Totals: ");
        updateSummedTotals(summedTotals, precinctTotals);

        /* Decrypt and store the final updated totals for each Precinct */
        System.out.println("Storing Decrypted Summed Totals: ");
        storeDecryptedSummedTotals(summedTotals, precinctMap);

        /* Return the webpage */
        return ok(adminpublish.render(VotingRecord.getUnpublished(), VotingRecord.getPublished()));
    }

    /*---------------------------------------- ADMIN METHODS -----------------------------------------------*/






    /*-------------------------------------- AUTHORITY METHODS -----------------------------------------------*/

    /**
     * Verifies that the username and password entered at the authority login screen are correct
     *
     * @return      the authority login page of the website
     */
    public static Result authorityverify() {
        return ok(authoritylogin.render(form(Login.class), null));
    }

    /**
     * This will take the session user and show a page for key generation for the specific user
     * @return
     */
    @Security.Authenticated(AuthoritySecured.class)
    public static Result authority() {
        return ok(authority.render(request().username(), "Main Page" ,null));
    }

    @Security.Authenticated(AuthoritySecured.class)
    public static Result keygeneration() {

        int stage = AdderKeyManipulator.getStage(request().username());
        String message = stage == 1 ? "KeySharePair generation stage (1)" :
                         stage == 2 ? "Polynomial generation stage (2)" :
                         stage == 3 ? "Private Key-share generation stage (3)" : "Complete!";

        return ok(authorityprocedure.render(message, request().username(), stage));
    }

    @Security.Authenticated(AuthoritySecured.class)
    public static Result updateprocedure() {

        String auth = request().username();

        /* Process for this */
        int stage = AdderKeyManipulator.getStage(auth);

        try {
            switch (stage) {

                case 1:
                    AdderKeyManipulator.generateAuthorityKeySharePair(auth);
                    break;

                case 2:
                    AdderKeyManipulator.generateAuthorityPolynomialValues(auth);
                    break;

            /* Need to know if we can keep these on the webserver */
                case 3:
                    AdderKeyManipulator.generateRealPrivateKeyShare(auth);
                    break;

                default:
                    break;
            }

        } catch (Exception e) {e.printStackTrace();}
        return keygeneration();
    }

    @Security.Authenticated(AuthoritySecured.class)
    public static Result uploadkey() {

        /* Get key from form submission -- s expression? -- and load into adderkeymanipulator for stage 3 */
        return null;
    }


    /*-------------------------------------- AUTHORITY METHODS -----------------------------------------------*/







    /**
     * @return a Map of the current summed results Ballot for each Precinct by precinct ID
     */
    private static Map<String, Ballot<EncryptedRaceSelection>> getSummedTotals() {

        Map<String, Ballot<EncryptedRaceSelection>> summedTotals = new TreeMap<>();

        /* Extract the precinct ID and ENCRYPTED results Ballot from each DecryptedResult */
        for(DecryptedResult result : DecryptedResult.all())
            summedTotals.put(result.precinctID, result.precinctResultsBallot);

        return summedTotals;
    }

    /**
     * Adds the summed result Ballots for each of the Precincts in this precinctMap to precinctTotals. Helper method
     * for publishresults()
     * 
     * @param precinctMap   the map of just-published Precinct result totals, mapped from precinct ID to a precinct of ballots
     *                      
     * @return the updated precinctTotals
     */
    private static <T extends AHomomorphicCiphertext<T>> Map<String, List<Ballot<EncryptedRaceSelection>>> updatePrecinctTotals(Map<String, Precinct<T>> precinctMap) {

        Map<String, List<Ballot<EncryptedRaceSelection>>> precinctTotals = new TreeMap<>();

        /* Get the cast ballot totals for each precinct in this voting record */
        for (Map.Entry<String, Precinct<T>> entry : precinctMap.entrySet()) {

            Precinct<T> p = entry.getValue();
            String precinctID = entry.getKey();

            /* Initialise the list for this precinct if we haven't yet seen it */
            if (precinctTotals.get(precinctID) == null)
                precinctTotals.put(precinctID, new ArrayList<Ballot<EncryptedRaceSelection>>());

            System.out.println("Precinct totals: " + precinctTotals);
            System.out.println("P: " + p);
            System.out.println("Precinct totals.get: " + precinctTotals.get(precinctID));
            System.out.println("Precinct ID: " + precinctID);

            /* Store the total for that precinct in the list */
            precinctTotals.get(precinctID).add(p.getCastBallotTotal(AdderKeyManipulator.generatePublicEncryptionKey()));
        }

        return precinctTotals;
    }

    /**
     * Adds newly published results in precinctTotals to summedTotals (the public running tally). Helper method for
     * publishresults()
     *
     * @param precinctTotals    the map of just-published Precinct result totals, mapped from precinct ID to a list of ballots
     * @param summedTotals      the public running tally of totals mapped from precinct ID to precinct results Ballot
     */
    private static <T extends AHomomorphicCiphertext<T>> void updateSummedTotals(
            Map<String, Ballot<EncryptedRaceSelection<T>>> summedTotals, Map<String, List<Ballot<EncryptedRaceSelection<T>>>> precinctTotals) {

        /* Update summed totals that already exist */
        for (Map.Entry<String, List<Ballot<EncryptedRaceSelection<T>>>> entry : precinctTotals.entrySet()) {

            /* Find for which Precinct this is */
            String precinctID = entry.getKey();

            /* Pull out the preExisting total if it exists */
            Ballot<EncryptedRaceSelection<T>> preExisting = summedTotals.get(precinctID);

            List<Ballot<EncryptedRaceSelection<T>>> thisPrecinctTotal = precinctTotals.get(precinctID);

            /* Add in any pre-existing totals from summedTotals */
            if(preExisting != null)
                thisPrecinctTotal.add(preExisting);

            System.out.println("Updating the precinct totals...");

            /* Tally the new precinctTotals using WebServerTallier*/
            Ballot<EncryptedRaceSelection<T>> b = WebServerTallier.tally(precinctID, thisPrecinctTotal, AdderKeyManipulator.generatePublicEncryptionKey());

            /* Replace old totals with new totals */
            summedTotals.put(precinctID, b);
        }

    }

    /**
     * Decrypts and stores the summed totals (the public running tally). Helper method for publishresults()
     *
     * @param summedTotals  the public running tally of totals mapped from precinct ID to precinct results Ballot
     * @param precinctMap   the map of Precincts from precinct IDs
     */
    private static void storeDecryptedSummedTotals(Map<String, Ballot<EncryptedRaceSelection>> summedTotals, Map<String, Precinct> precinctMap) {

        SimpleKeyStore keyStore = new SimpleKeyStore("/lib/keys/");
        AdderPrivateKeyShare privateKey = keyStore.loadAdderPrivateKey();

        System.out.println("Decrypting summedTotals...");

        /* Decrypt summedTotals */
        for(Map.Entry<String, Ballot<EncryptedRaceSelection>> entry : summedTotals.entrySet()) {

            Ballot<EncryptedRaceSelection> b = entry.getValue();
            String precinctID = entry.getKey();

            Ballot<PlaintextRaceSelection> decryptB = null;
            try { decryptB = BallotCrypto.decrypt(b); }
            catch (Exception e) {e.printStackTrace(); }

            /* This will be the decrypted representation of the results by race */
            Map<String, Map<String, Integer>> decryptedResults = WebServerTallier.getVoteTotals(decryptB);

            /* Store totals in database */
            DecryptedResult.create(new DecryptedResult(precinctID, decryptedResults, b));
        }

    }

    /**
     * Page for requesting challenged ballot render
     *
     * @return      the challenged ballot page with rendered ballot
     */
    public static Result challenge() { 
        return ok(challengeballot.render(ChallengedBallot.all(), challengeForm, null)); 
    }

    /**
     * Retrieves challenged ballot database entry
     */
    public static Result getChallengedBallot(String bid) {

        String errorCode = "**Could not locate a challenged ballot with Ballot Identification Number (BID): " + bid +
                           ". If the BID you entered is correctly displayed, please contact your local election office.**";

        /*  Database lookup */
        ChallengedBallot ballot = ChallengedBallot.getBallot(bid);

        /* Check the ballot to make sure it's not bad -- if it is, send to error page */
        if (ballot == null)
            return ok(challengeballot.render(ChallengedBallot.all(), challengeForm, errorCode));

        /* If good, send to the ballot found page */
        return ok(challengedballotfound.render(ballot, bid));
    }

    /**
     * Used to determine whether a ballot, given the ballot ID, is challenged or cast under the system
     *
     * @param bid       ballot ID
     * @return          the page based on the status of the ballot (whether the ballot is valid/cast/challenged)
     */
    public static Result handleBallotState(String bid) {

        /* Send to the proper page based on what the ballot is */
        return bid.equals("none")                      ?   ok(index.render())       :
               CastBallot.getBallot(bid) != null       ?   getCastBallot(bid)       :
               ChallengedBallot.getBallot(bid) != null ?   getChallengedBallot(bid) : ok(ballotnotfound.render(bid));
    }

    /**
     * Socket handling for ballot end-of-election dump/upload from each voting station
     * Parses and stores new cast and challenged ballots
     *
     * @return a rendering of the home page
     */
    public static Result ballotLoad() {

       /* Strategy:
        *
        * - Load the Map<String, Map<String, Precinct>>.
        *   Each Map.Entry<String, Map> is <Supervisor-Hash, PrecinctMap>
        *   Each Map.Entry<String, Precinct> is <PrecinctID, PrecinctObject>
        */

        /* Code for this method in handling a POST command are found at http://www.vogella.com/articles/ApacheHttpClient/article.html */

        /* TODO check the form encoding load */
        final Map<String, String[]> values = request().body().asFormUrlEncoded();

        System.out.println(request().body());

        final String record = values.get("record")[0];
        final String precinctID = values.get("precinctID")[0];

        /* Decode from base64 */
        byte[] bytes = Base64.decode(record);
        Map<String, Map<String, Precinct>> votingRecord = null;

        try {
            ObjectInputStream o = new ObjectInputStream(new ByteArrayInputStream(bytes));
            votingRecord = (Map<String, Map<String, Precinct>>) o.readObject();
        }
        catch (IOException | ClassNotFoundException | ClassCastException e) { e.printStackTrace(); }

        System.out.println(votingRecord);

        /* Add the record to the database */
        VotingRecord.create(new VotingRecord(precinctID, votingRecord));
        return ok(index.render());
    }

    /**
     * Retrieves an html file (from a BID) from the internal storage of the web-server and serves file as content
     *
     * @param ballotid      ballot ID
     * @return              content of an html file from a predetermined directory.
     */
    public static Result getBallotHtmlFile(String ballotid) {

        File file = new File("htmls/ChallengedBallot_" + ballotid + ".html");
        return ok(file);
    }
    
    /**
     * Redirects to the 
     */
    public static Result getTrac() { return redirect("/assets/trac/index.html"); }

    /**
     * Sends to the API page
     *
     * @return      the API page
     */
    public static Result getAPI() { return redirect("/assets/api/index.html"); }
    
     /**
     * This will authenticate our logins
     */
    public static Result authenticate() {
        Form<Login> loginForm = form(Login.class).bindFromRequest();

        if (loginForm.hasErrors()) {
            return loginForm.name().equals("adminlogin") ? badRequest(adminlogin.render(loginForm, null)) :
                                                           badRequest(authoritylogin.render(loginForm, null));
        } else {
            session().clear();
            session("username", loginForm.get().username);
            return loginForm.name().equals("adminlogin") ? redirect(routes.AuditServer.adminmain()) :
                                                           redirect(routes.AuditServer.authority());
        }
    }
    
    /**
     * This ends an authenticated session
     */
    public static Result logout() {
        session().clear();
        flash("success", "You've been logged out");
        return redirect(routes.AuditServer.index());
    }

    /** 
     * An inner class for a login
     */
    public static class Login {

        public String username;
        public String password;

        /** This will validate the username and password */
        public String validate() {

            if (!User.authenticate(username, password))
              return "Invalid user or password";

            return null;
        }

    }
    
}
