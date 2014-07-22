package controllers;

import models.CastBallot;
import models.ChallengedBallot;
import play.data.Form;
import play.data.validation.ValidationError;
import play.libs.F.*;
import play.mvc.*;
import sexpression.ASExpression;
import sexpression.ListExpression;
import utilities.BallotLoader;
import utilities.WebPrinter;
import views.html.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Map;

import static play.data.Form.form;

/**
 * AuditServer is the main controller for the STAR-Vote Web-Sever project. It handles most of the HTTP requests
 * from the routes file. All pages are fed through this controller, and POST requests for pushing up challenged
 * and cast ballots are also handled here
 *
 * @author Mitchell Douglass
 */
public class AuditServer extends Controller {

    /* Forms for searching for ballots in DBs */
    static Form<ChallengedBallot> challengeForm = form(ChallengedBallot.class);
    static Form<CastBallot> confirmForm = form(CastBallot.class);

    static String adminusrhash  = "administrator";
    static String adminpasshash = "veryimportant";

    static boolean init = false;


    /**
     * Serves the Home Page of the site
     *
     * @return      the home page of the site
     */
    public static Result index() { return ok(index.render()); }

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
     * Returns the test page image render
     *
     * @return      the test page
     */
    public static Result test(){
        /* TODO return ok(imgTest.render()); */
        return null;
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

        /* Pull the page */
        final Map<String, String[]> values = request().body().asFormUrlEncoded();

        /* Get the username and password fields */
        final String usr  = values.get("username")[0];
        final String pass = values.get("password")[0];

        /* Check the input hash against the credentials hashes -- if good, send to admin page */
        if (usr.equals(adminusrhash) && pass.equals(adminpasshash)) {
            session("pass", pass);
            return ok(admin.render(null));
        } /* If it's no good, return the error */
        else return ok(adminlogin.render("Username or Password is not correct"));
    }

    /**
     * Serves the admin login screen.
     *
     * @return      the admin login screen
     */
    public static Result adminlogin(){
        return ok(adminlogin.render(null));
    }

    /**
     * Verifies that the admin is currently logged in, then clears data from the ebean database.
     * Serves up admin page with success message if admin logged in, or error page if admin not logged in.
     *
     * @return      the admin page with success/error dependent on login success
     */
    public static Result adminclear() {

        /* Check the password hash against the actual hash -- if good, clear data */
        if (session("pass") != null && session("pass").equals(adminpasshash)) {

            /* Destroy all the cast ballot / challenged ballot info from the database */
            for (CastBallot cb: CastBallot.all())  CastBallot.remove(cb);
            for (ChallengedBallot cb: ChallengedBallot.all()) ChallengedBallot.remove(cb);

            /* Send to the data cleared page */
            return ok(admin.render("**Data has been cleared***"));
        }
        /* If not, send to error page */
        else return ok(badPage.render());
    }

    /**
     * Page for requesting challenged ballot render
     *
     * @return      the challenged ballot page with rendered ballot
     */
    public static Result challenge() { return ok(challengeballot.render(ChallengedBallot.all(), challengeForm, null)); }

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
        return bid.equals("none")                       ?   ok(index.render())       :
               CastBallot.getBallot(bid) != null        ?   getCastBallot(bid)       :
               ChallengedBallot.getBallot(bid) != null  ?   getChallengedBallot(bid) : ok(ballotnotfound.render(bid));
    }

    /**
     * Socket handling for ballot end-of-election dump/upload from each voting station
     * Parses and stores new cast and challenged ballots
     *
     * @return
     */
    public static Result ballotDump() {

       /* Strategy:
        *
        * - Load the Map<String, Map<String, Precinct>>.
        *   Each Map.Entry<String, Map> is <Supervisor-Hash, PrecinctMap>
        *   Each Map.Entry<String, Precinct> is <PrecinctID, PrecinctObject>
        *
        * - If the size of the original map is only one, load the Ballots in the Precincts into the database
        *   Otherwise, create a Conflict associated with the Map
        *
        * - Load non-conflicted Maps to a "Publish" page where they can be published publicly. At this step, HTML print
        *   challenged Ballots, and allow verification of cast Ballots. Probably want to re-tally and publish results by
        *   Precinct after each new "Publish" event.
        *
        * - Add all the Map.Entries for a conflicted Map to a "Conflicts" page as a single entry where the user
        *   can choose the proper Map<String,Precinct> by Supervisor-Hash to resolve the conflict.
        *
        * - When a conflicted Map is resolved, add its Precincts to the "Publish" page
        */

        /* Code for this method in handling a POST command are found at http://www.vogella.com/articles/ApacheHttpClient/article.html */

        final Map<String, String[]> values = request().body().asFormUrlEncoded();
        final String event = values.get("message")[0];

        StringTokenizer typeParser = new StringTokenizer(event, ":");

        /*
            todo: add machine-unique keys/ids to prevent any source from dumping and/or for discarding ballots from unknown sources
            todo: make the transfer less hodgepodge and create a protocol
        */

        /* Separate ballot data into different fields */
        String ballotType       = typeParser.nextToken();
        String ballotID         = typeParser.nextToken();
        String ballotPrecinct   = typeParser.nextToken();
        String params           = typeParser.nextToken();

        /* Set up a new parser for parameter parsing */
        StringTokenizer paramParser = new StringTokenizer(params, ";");

        /* todo: check for duplicates? */

        /* Check the ballot type -- if cast, create a new CastBallot with the info */
        if ("cast".equals(ballotType)) CastBallot.create(new CastBallot(ballotID, String.valueOf(paramParser.nextToken().hashCode())));

        /* If challenged... */
        else if ("chall".equals(ballotType)) {

            /* Create a new ChallengedBallot */
            ChallengedBallot cb = new ChallengedBallot(ballotID, ballotPrecinct, String.valueOf(paramParser.nextToken().hashCode()), paramParser.nextToken());
            ChallengedBallot.create(cb);

            /* Set up a new WebPrinter for the race */
            WebPrinter printer = new WebPrinter(BallotLoader.getBallotFileByPrecinct(ballotPrecinct), BallotLoader.getRaceGroupByPrecinct(ballotPrecinct));

            /* Create a new ListExpression from the decrypted ballot */
            ListExpression ballot = new ListExpression(ListExpression.make(cb.decryptedBallot));

            /* TODO related to ChallengedBallotUploadEvent */
            ballot = (ListExpression)ballot.getArray()[0];

            /* TODO why is this committed ballot? */
            /* Render the ballot */
            printer.printCommittedBallot(ballot, cb.ballotid);
        }

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
}
