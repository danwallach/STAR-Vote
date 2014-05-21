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

    //    forms for searching for ballots in DBs
    static Form<ChallengedBallot> challengeForm = form(ChallengedBallot.class);
    static Form<CastBallot> confirmForm = form(CastBallot.class);

    static String adminusrhash = "administrator";
    static String adminpasshash = "veryimportant";

    static boolean init = false;


    /**
     * Serves the Home Page of the site
     */
    public static Result index() {
        //if(!init){
        //    init = true;
        //    BallotLoader.init();
        //}
        return ok(index.render());
    }

    /**
     * Page for requesting cast ballot hash lookup for confirming cast ballots.
     */
    public static Result confirm() {
        return ok(confirmballot.render(CastBallot.all(), confirmForm, null));
    }

    /**
     * Serves up the About Us page, accessible from menu bar.
     */
    public static Result aboutUs() {
        return ok(aboutUs.render());
    }

    public static Result test(){
        //return ok(imgTest.render());
        return null;
    }

    /**
     * Confirms ballot was cast by looking for hash in cast ballot database
     */
    public static Result getCastBallot(String bid){
//      db lookup
        CastBallot ballot = CastBallot.getBallot(bid);
        if (ballot != null) {
            return ok(castballotfound.render(ballot, bid));
        } else {
            return ok(confirmballot.render(CastBallot.all(), confirmForm,
            "**Could not locate a cast ballot with Ballot Identification Number (BID): " + bid + ". If the BID you entered is correctly displayed, please contact your local election office.**"));
        }
    }

    /**
     * Verifies that the username and password entered at the admin login screen are correct
     *
     * @return the admin page of the website
     */
    public static Result adminverify(){
        final Map<String, String[]> values = request().body().asFormUrlEncoded();
        final String usr = values.get("username")[0];
        final String pass = values.get("password")[0];
        if(usr.equals(adminusrhash) && pass.equals(adminpasshash)){
            session("pass", pass);
            return ok(admin.render(null));
        }else{
            return ok(adminlogin.render("Username or Password is not correct"));
        }
    }

    /**
     * Serves the admin login screen.
     */
    public static Result adminlogin(){
        return ok(adminlogin.render(null));
    }

    /**
     * Verifies that the admin is currently logged in, then clears data from the ebean database.
     * Serves up admin page with success message if admin logged in, or error page if admin not logged in.
     */
    public static Result adminclear(){
        if(session("pass")!=null&&session("pass").equals(adminpasshash)){
            for(CastBallot cb: CastBallot.all()){
                CastBallot.remove(cb);
            }
            for(ChallengedBallot cb: ChallengedBallot.all()){
                ChallengedBallot.remove(cb);
            }
            return ok(admin.render("**Data has been cleared***"));
        }
        else
            return ok(badPage.render());
    }

    /**
     * Page for requesting challenged ballot render
     */
    public static Result challenge() {
        return ok(challengeballot.render(ChallengedBallot.all(), challengeForm, null));
    }

    /**
     * Retrieves challenged ballot database entry
     */
    public static Result getChallengedBallot(String bid){
//      db lookup
        ChallengedBallot ballot = ChallengedBallot.getBallot(bid);
        if (ballot == null) {
            return ok(challengeballot.render(ChallengedBallot.all(), challengeForm,
                    "**Could not locate a challenged ballot with Ballot Identification Number (BID): " + bid + ". If the BID you entered is correctly displayed, please contact your local election office.**"));
        }

        return ok(challengedballotfound.render(ballot, bid));
    }

    /**
     * Used to determine whether a ballot, given the ballot ID, is challenged or cast under the system
     * @param bid ballot ID
     */
    public static Result handleBallotState(String bid){
        if(bid.equals("none")) return ok(index.render());

        if(CastBallot.getBallot(bid) != null){
            return getCastBallot(bid);
        } else if(ChallengedBallot.getBallot(bid) != null){
            return getChallengedBallot(bid);
        } else {
            return ok(ballotnotfound.render(bid));
        }
    }

    /*public static Result addToDB(String bid){
        if(!bid.equals("none")){
                ChallengedBallot cb = new ChallengedBallot(bid, "futurama547", "hash", "nounce");
                ChallengedBallot.create(cb);
        }
        System.out.println(ChallengedBallot.find.all().size());
        return ok(index.render());
    }

    public static Result getFromDB(String bid){
        ChallengedBallot cb = ChallengedBallot.getBallot(bid);
        System.out.println(cb);
        return ok(index.render());
    }

    public static Result postAttempt(){
        final Map<String, String[]> values = request().body().asFormUrlEncoded();
        final String name = values.get("name")[0];
        System.out.println(name);
        return ok(index.render());
    }*/

    /**
     * Socket handling for ballot end-of-election dump/upload from each voting station
     * Parses and stores new cast and challenged ballots
     */
    public static Result ballotDump() {

        /* code for this method in handling a POST command are found at http://www.vogella.com/articles/ApacheHttpClient/article.html */

        System.out.println("Connection SUCCESSFUL!");

        final Map<String, String[]> values = request().body().asFormUrlEncoded();
        final String event = values.get("message")[0];

        StringTokenizer typeParser = new StringTokenizer(event, ":");
//                      todo: add machine-unique keys/ids to prevent any source from dumping and/or for discarding ballots from unknown sources
//                      todo: make the transfer less hodgepodge and create a protocol
        String ballotType = typeParser.nextToken();
        String ballotID = typeParser.nextToken();
        String ballotPrecinct = typeParser.nextToken();
        String params = typeParser.nextToken();
        StringTokenizer paramParser = new StringTokenizer(params, ";");
//                        todo: check for duplicates?
        if ("cast".equals(ballotType)) {
            CastBallot.create(new CastBallot(ballotID, String.valueOf(paramParser.nextToken().hashCode())));
        } else if ("chall".equals(ballotType)){
            ChallengedBallot cb = new ChallengedBallot(ballotID, ballotPrecinct, String.valueOf(paramParser.nextToken().hashCode()), paramParser.nextToken());
            ChallengedBallot.create(cb);
            WebPrinter printer = new WebPrinter(BallotLoader.getBallotFileByPrecinct(ballotPrecinct), BallotLoader.getRaceGroupByPrecinct(ballotPrecinct));
            ListExpression ballot = new ListExpression(ListExpression.make(cb.decryptedBallot));
            ballot = (ListExpression)ballot.getArray()[0];
            System.out.println("Decrypted Ballot" + cb.decryptedBallot);
            printer.printCommittedBallot(ballot, cb.ballotid);
        }
        return ok(index.render());
    }

    /**
     * retrieves an html file (from a BID) from the internal storage of the web-server and serves file as content
     *
     * @param ballotid ballot ID
     * @return content of an html file from a predetermined directory.
     */
    public static Result getBallotHtmlFile(String ballotid){
        File file = new File("htmls/ChallengedBallot_" + ballotid + ".html");
        return ok(file);
    }

    public static Result getAPI(){
        return redirect("/assets/api/index.html");
    }
}
