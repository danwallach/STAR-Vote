package controllers;

import models.CastBallot;
import models.ChallengedBallot;
import models.User;
import models.VotingRecord;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import sexpression.stream.Base64;
import supervisor.model.Ballot;
import supervisor.model.Precinct;
import supervisor.model.WebServerTallier;
import views.html.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigInteger;
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
         Map<String, Map<String, Precinct>> records = new HashMap<>();
        
        for(int i = 0; i < 3; i++) {
           
            Map<String, Precinct> hashes = new HashMap<>();
            
            for(int j = 0; j < 3; j++)
                hashes.put(j+"", new Precinct(j+"", "", null));
           
            records.put("record" + i, hashes);
            
            VotingRecord.create(new VotingRecord("Precinct" + i, records));
        }
        
        for(int i = 4; i < 7; i++) {
           
            records = new HashMap<>();
           
            Map<String, Precinct> hashes = new HashMap<>();
            
            hashes.put("1", new Precinct("1", "", null));
           
            records.put("record" + i, hashes);
            
            VotingRecord.create(new VotingRecord("Precinct"+ i, records));
        }
        
        
 
        return ok(index.render()); 
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

    /**
     * Verifies that the username and password entered at the admin login screen are correct
     *
     * @return      the admin page of the website
     */
    public static Result adminverify() {
        return ok(adminlogin.render(form(Login.class), null));
    }

    /**
     * Serves the admin login screen.
     *
     * @return      the admin login screen
     */
    public static Result adminlogin(){
        return ok(adminlogin.render(form(Login.class), ""));
    }

    @Security.Authenticated(Secured.class)
    public static Result adminmain() {
        return ok(adminmain.render());
    }
    
    /**
     * Verifies that the admin is currently logged in, then clears data from the ebean database.
     * Serves up admin page with success message if admin logged in, or error page if admin not logged in.
     *
     * @return      the admin page with success/error dependent on login success
     */
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
    @Security.Authenticated(Secured.class)
    public static Result adminconflicts() {
        return ok(adminconflicts.render(VotingRecord.getConflicted()));
    }
    
    @Security.Authenticated(Secured.class)
    public static Result resolveconflict(String id, String hash) {

        hash = hash.substring(0, hash.length()-1);
        VotingRecord.getRecord(id).resolveConflict(hash);
        return ok(adminconflicts.render(VotingRecord.getConflicted()));
    }

    /**
     * Generates and renders the page for publishing results 
     */    
    @Security.Authenticated(Secured.class)
    public static Result adminpublish() {
        return ok(adminpublish.render(VotingRecord.getUnpublished(), VotingRecord.getPublished()));
    }
    
    @Security.Authenticated(Secured.class)
    public static Result publishresults() {
        
        /* Reverse routing */
        String records = request().getQueryString("records");

        int start = 0;

        Map<String, Ballot> bigTotal = null /* get this from database*/;
        Map<String, List<Ballot>> precinctTotals = new TreeMap<>();
        Map<String, Precinct> allPrecincts = new TreeMap<>();

        /* For size? or add field to ballot? */Map<String, Integer> size = new TreeMap<>();
        
        /* Grab each checked precinct and publish it */
        while(start < records.length()) {

            int end = records.indexOf(",", start);

            if (end == -1)
                end = records.length();

            String precinctID = records.substring(start, end);
            System.out.println(precinctID);

            VotingRecord vr = VotingRecord.getRecord(precinctID);
            
            /* Publish the record */
            vr.publish();
            
            /* Get the Precincts */
            Map<String, Precinct> precinctMap = vr.getPrecinctMap();
            allPrecincts.putAll(precinctMap);

            /* Get the cast ballot totals for each precinct in this voting record */
            for (Map.Entry<String, Precinct> entry : precinctMap.entrySet()) {

                Precinct p = entry.getValue();
                precinctID = entry.getKey();

                /* If we haven't yet seen this precinct, initialise the list */
                if(precinctTotals.get(precinctID) == null)
                    precinctTotals.put(precinctID, new ArrayList<Ballot>());

                /* Store the total for that precinct in the list */
                precinctTotals.get(precinctID).add(p.getCastBallotTotal());
            }

            /* TODO keep track of the size of the tallied ballots */

            /* Move on to the next VotingRecord to be published */
            start = end+1;
        }

        /* Update summed totals */
        for (Map.Entry<String, Ballot> entry : bigTotal.entrySet()) {

            String precinctID = entry.getKey();

            /* Add in any pre-existing totals */
            precinctTotals.get(precinctID).add(entry.getValue());

            /* Tally the new precinctTotals using WebServerTallier*/
            Ballot b = WebServerTallier.tally(precinctID, precinctTotals.get(precinctID), allPrecincts.get(precinctID).getPublicKey());

            /* Replace old total with new total */
            bigTotal.put(precinctID, b);
        }

        /* This will be the decrypted representation of the results by precinct */
        Map<String, Map<String, BigInteger>> decryptedResults = new TreeMap<>();

        /* Decrypt bigTotal */
        for(Map.Entry<String, Ballot> entry : bigTotal.entrySet()) {

            Ballot b = entry.getValue();
            String precinctID = entry.getKey();

            decryptedResults.put(precinctID, WebServerTallier.getVoteTotals(b, size, allPrecincts.get(precinctID).getPublicKey(), privateKey));

        }

        /* Store decrypted bigTotal ballot in database */

        /* Return the webpage */
        return ok(adminpublish.render(VotingRecord.getUnpublished(), VotingRecord.getPublished()));
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

        final Map<String, String[]> values = request().body().asFormUrlEncoded();

        /* TODO get stuff from tap properly */
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
            return badRequest(adminlogin.render(loginForm, null));
        } else {
            session().clear();
            session("username", loginForm.get().username);
            return redirect(
                routes.AuditServer.adminmain()
            );
        }
    }
    
    /**
     * This ends an authenticated administrator session
     */
    public static Result logout() {
        session().clear();
        flash("success", "You've been logged out");
        return redirect(
            routes.AuditServer.adminverify()
        );
    }
    
    public static Result adminlogout() {
        return ok(adminlogout.render());
    }
    
    /** 
     * An inner class for a login
     */
    public static class Login {

        public String username;
        public String password;

        /** This will validate the username and password */
        public String validate() {

            if (User.authenticate(username, password) == null)
              return "Invalid user or password";

            return null;
        }

    }
    
}
