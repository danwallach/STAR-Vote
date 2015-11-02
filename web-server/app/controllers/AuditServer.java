package controllers;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import crypto.*;
import crypto.adder.AdderInteger;
import crypto.adder.AdderPrivateKeyShare;
import crypto.adder.AdderPublicKey;
import crypto.adder.Polynomial;
import models.*;
import org.yaml.snakeyaml.Yaml;
import play.data.Form;
import play.data.validation.Constraints;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import security.Secured;
import sexpression.ASEConverter;
import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.stream.Base64;
import supervisor.model.AuthorityManager;
import supervisor.model.Ballot;
import supervisor.model.Precinct;
import utilities.WebServerTallier;
import views.html.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
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
    static AdderPublicKey PEK;
    static BallotCrypter<ExponentialElGamalCiphertext> ballotCrypter;

    static boolean init = false;


    /* =======================  PSYC STUFF =============================== */

    /**
     * Serves the psyc Home Page of the site
     *
     * @return      the home page of the site
     */
    public static Result homePage() {
        return ok(psycHomepage.render());
    }


    /**
     * Serves the "check my vote" page of the site
     *
     * @return      the CMV page
     */
    public static Result check() {
        return ok(CMVpage.render());
    }


    /**
     * Parses the submitted id and returns a page with the status
     *
     * @return      the CMV results page with the proper status
     */
    public static Result checkballot(String bid) {

        String status = (CastBallot.getBallot(bid) != null) ? "cast" :
                        (ChallengedBallot.getBallot(bid) != null) ? "challenged" :
                        "invalid";

        return ok(CMVresults.render(status, bid));
    }

    /**
     * Serves the "poll results" page of the site
     *
     * @return      the poll results page
     */
    public static Result pollresults() {

        return ok(pollResults.render());

    }

    /**
     * Serves the "tools" page of the site
     *
     * @return      the tools page
     */
    public static Result tools() {

        return ok(tools.render());

    }

    /**
     * Serves the "report" page of the site
     *
     * @return      the report page
     */
    public static Result report(String id) {

        return ok(Report.render(id));

    }

    /**
     * Handles submitted report information and serves the result
     *
     * @return      the result of submitted report information page
     */
    public static Result reportissue(String id) {

        String issue = request().getQueryString("issue");
        String comments = request().getQueryString("comments");
        String print = request().getQueryString("print");
        String text = request().getQueryString("text");
        String email = request().getQueryString("email");

        return ok(reportConfirmation.render(email, print, text, id));
    }



    /* ====================  PSYC STUFF END ============================== */





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

        Map<String, Map<String, Precinct<ExponentialElGamalCiphertext>>> records = new HashMap<>();

        for(int i = 0; i < 3; i++) {

            Map<String, Precinct<ExponentialElGamalCiphertext>> hashes = new HashMap<>();

            for(int j = 0; j < 3; j++)
                hashes.put(j+"", new Precinct<>(j+"", ""));

            records.put("record" + i, hashes);

            VotingRecord.create(new VotingRecord("Precinct" + i, records));
        }

        for(int i = 4; i < 7; i++) {

            records = new HashMap<>();

            Map<String, Precinct<ExponentialElGamalCiphertext>> hashes = new HashMap<>();

            hashes.put("1", new Precinct<>("1", ""));

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

    public static Result results() {
        return ok(resultspage.render(DecryptedResult.find.all()));
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

    @Security.Authenticated(Secured.class)
    @Restrict(@Group("admin"))
    public static Result adminmain() {
        return ok(adminmain.render());
    }

    /**
     * Verifies that the admin is currently logged in, then clears data from the ebean database.
     * Serves up admin page with success message if admin logged in, or error page if admin not logged in.
     *
     * @return      the admin page with success/error dependent on login success
     */
    @Security.Authenticated(Secured.class)
    @Restrict(@Group("admin"))
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
    @Restrict(@Group("admin"))
    public static Result adminconflicts() {
        return ok(adminconflicts.render(VotingRecord.getConflicted()));
    }
    
    @Security.Authenticated(Secured.class)
    @Restrict(@Group("admin"))
    public static Result resolveconflict(String id, String hash) {

        hash = hash.substring(0, hash.length()-1);
        VotingRecord.getRecord(id).resolveConflict(hash);
        return ok(adminconflicts.render(VotingRecord.getConflicted()));
    }

    /**
     * Generates and renders the page for publishing results 
     */    
    @Security.Authenticated(Secured.class)
    @Restrict(@Group("admin"))
    public static Result adminpublish() {
        return ok(adminpublish.render(VotingRecord.getUnpublished(), VotingRecord.getPublished(), PEK != null, ""));
    }
    
    @Security.Authenticated(Secured.class)
    @Restrict(@Group("admin"))
    public static Result publishresults() {



            /* Reverse routing */
            String records = request().getQueryString("records");
            String message = "There was an issue publishing the last set of records!";
            int start = 0;

        try {

            Map<String, Ballot<EncryptedRaceSelection<ExponentialElGamalCiphertext>>> summedTotals = getSummedTotals();
            Map<String, List<Ballot<EncryptedRaceSelection<ExponentialElGamalCiphertext>>>> precinctTotals = new TreeMap<>();
            Map<String, Precinct<ExponentialElGamalCiphertext>> precinctMap;

            /* Grab each selected precinct and publish it */
            while (start < records.length()) {

                int end = records.indexOf(",", start);

                if (end == -1)
                    end = records.length();

                /* Get the precinct ID from the query string */
                String precinctID = records.substring(start, end);

                System.out.println(precinctID);

                /* Pull out the current record to be published */
                VotingRecord vr = VotingRecord.getRecord(precinctID);

                /* Publish the record */
                vr.openRecord();

                /* Get the Precincts from this VotingRecord (Encrypted!) */
                System.out.println("Getting the Precinct Map... ");
                precinctMap = vr.getPrecinctMap();
                System.out.println("\t" + precinctMap);

                /* Add the results from the Precincts in this VotingRecord to precinctTotals */
                System.out.println("Updating Precinct Totals... ");
                precinctTotals = updatePrecinctTotals(precinctMap);

                /* Move on to the next VotingRecord to be published */
                start = end + 1;
            }

            System.out.println("Summed totals! " + summedTotals);

            /* Combine the newly published result totals with the old totals */
            System.out.println("Updating Summed Totals... ");
            updateSummedTotals(summedTotals, precinctTotals);

            System.out.println("Summed totals! " + summedTotals);
            /* Decrypt and store the final updated totals for each Precinct */
            System.out.println("Storing Decrypted Summed Totals... ");
            storeDecryptedSummedTotals(summedTotals);

            start =0;

            while (start < records.length()) {

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
                start= end+1;
            }

            message = "Published!";
        }
        catch (Exception e) {

            e.printStackTrace();

            /* If we run into a problem publishing, make sure to close all of the records */
            while (start < records.length()) {

                int end = records.indexOf(",", start);

                if (end == -1)
                    end = records.length();

                /* Get the precinct ID from the query string */
                String precinctID = records.substring(start, end);

                /* Pull out the current record to be closed */
                VotingRecord vr = VotingRecord.getRecord(precinctID);

                /* Close the record */
                vr.closeRecord();
            }


        }

        /* Return the webpage */
        return ok(adminpublish.render(VotingRecord.getUnpublished(), VotingRecord.getPublished(),
                PEK != null, message));
    }

    @Security.Authenticated(Secured.class)
    @Restrict(@Group("admin"))
    public static Result uploadPEK() {

        /* Load the seed key */
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(".key files", "key");
        chooser.setFileFilter(filter);

        int returnVal = chooser.showOpenDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            System.out.println("You chose to open this file: " +
                    chooser.getSelectedFile().getName());
        }

        try {

            File PEKFile = chooser.getSelectedFile();
            Path PEKPath = PEKFile.toPath();

            byte[] verbatimPEK = Files.readAllBytes(PEKPath);
            ASExpression PEKASE = ASExpression.makeVerbatim(verbatimPEK);
            System.out.println(PEKASE);
            PEK = ASEConverter.convertFromASE((ListExpression) PEKASE);

        }
        catch (Exception e) {
            System.err.println("Couldn't upload the key file due to " + e.getClass());

        }

        return ok(adminpublish.render(VotingRecord.getUnpublished(), VotingRecord.getPublished(), PEK != null, ""));
    }

    /*---------------------------------------- ADMIN METHODS -----------------------------------------------*/






    /*-------------------------------------- AUTHORITY METHODS -----------------------------------------------*/

    /**
     * Verifies that the username and password entered at the authority login screen are correct
     *
     * @return      the authority login page of the website
     */
    public static Result authorityverify() {
        return ok(authoritylogin.render(form("authoritylogin", Login.class), null));
    }

    /**
     * This will take the session user and show a page for key generation for the specific user
     * @return
     */
    @Security.Authenticated(Secured.class)
    @Restrict(@Group("authority"))
    public static Result authority() {
        return ok(authoritymain.render(request().username(), "Main Page" ,null));
    }

    @Security.Authenticated(Secured.class)
    @Restrict(@Group("authority"))
    public static Result keygeneration() {

        int stage = AuthorityManager.SESSION.getStage(request().username());
        String message = stage == 1 ? "KeySharePair generation stage (1)" :
                         stage == 2 ? "Polynomial generation stage (2)" :
                         stage == 3 ? "Private Key-share generation stage (3)" : "Complete!";

        if (message.equals("Complete!")) {
            writePEKtoFile();
            storeAuthorityData();
        }

        return ok(authorityprocedure.render(message, request().username(), stage));
    }

    private static void writePEKtoFile(){

        try {

            PEK = AuthorityManager.SESSION.generatePublicEncryptionKey();

            File destDir = new File("keys");

            /* Checks whether the destination directory already exists, if not then make the directory. */
            if(!destDir.exists()){
                destDir.mkdirs();
            }

            /* If it exists then it checks whether its a directory or not. */
            else{

                if(!destDir.isDirectory()){
                    System.out.println("Usage: java " + AuthorityManager.class.getName() + " [destination directory]");
                    System.exit(-1);
                }
            }

            File pekFile = new File("keys", "PEK.adder.key");

            FileOutputStream fos = new FileOutputStream(pekFile);
            fos.write(ASEConverter.convertToASE(PEK).toVerbatim());
            fos.flush();
            fos.close();
        }
        catch (Exception e) { e.printStackTrace(); }
    }

    private static void storeAuthorityData(){

        try {
            File userFile = new File("conf", "user-data.yml");
            File authorityFile = new File("conf", "authority-data.inf");

            FileOutputStream fos = new FileOutputStream(authorityFile);
            fos.write(ASEConverter.convertToASE(AuthorityManager.SESSION).toVerbatim());
            fos.flush();
            fos.close();

            Yaml yaml = new Yaml();
            FileWriter writer = new FileWriter(userFile);
            yaml.dump(User.find.all(), writer);
            writer.close();
        }
        catch (IOException e) { System.err.println("Could not write the authority file!"); }
    }

    @Security.Authenticated(Secured.class)
    @Restrict(@Group("authority"))
    public static Result updateprocedure() {

        String auth = request().username();

        /* Process for this */
        int stage = AuthorityManager.SESSION.getStage(auth);

        try {
            switch (stage) {

                case 1:
                    AuthorityManager.SESSION.generateAuthorityKeySharePair(auth);
                    break;

                case 2:
                    AuthorityManager.SESSION.generateAuthorityPolynomialValues(auth);
                    break;

                /* TODO Need to know if we can keep these on the webserver */
                case 3:
                    User u = User.find.byId(request().username());
                    u.setKey(AuthorityManager.SESSION.generateRealPrivateKeyShare(auth));
                    break;

                default:
                    break;
            }

        } catch (Exception e) {e.printStackTrace();}
        return keygeneration();
    }

    /* TODO will talk about whether this is necessary or if current system is fine */
    @Security.Authenticated(Secured.class)
    @Restrict(@Group("authority"))
    public static Result uploadkey() {

        /* Get key from form submission -- s expression? -- and load into adderkeymanipulator for stage 3 */
        return null;
    }


    /*-------------------------------------- AUTHORITY METHODS -----------------------------------------------*/







    /**
     * @return a Map of the current summed results Ballot for each Precinct by precinct ID
     */
    private static Map<String, Ballot<EncryptedRaceSelection<ExponentialElGamalCiphertext>>> getSummedTotals() {

        Map<String, Ballot<EncryptedRaceSelection<ExponentialElGamalCiphertext>>> summedTotals = new TreeMap<>();

        /* Extract the precinct ID and ENCRYPTED results Ballot from each DecryptedResult */
        for(DecryptedResult result : DecryptedResult.all())
                summedTotals.put(result.precinctID, result.getResultsBallot());

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
    private static Map<String, List<Ballot<EncryptedRaceSelection<ExponentialElGamalCiphertext>>>> updatePrecinctTotals(
            Map<String, Precinct<ExponentialElGamalCiphertext>> precinctMap) {

        Map<String, List<Ballot<EncryptedRaceSelection<ExponentialElGamalCiphertext>>>> precinctTotals = new TreeMap<>();

        /* Get the cast ballot totals for each precinct in this voting record */
        for (Map.Entry<String, Precinct<ExponentialElGamalCiphertext>> entry : precinctMap.entrySet()) {

            Precinct<ExponentialElGamalCiphertext> p = entry.getValue();
            String precinctID = entry.getKey();

            /* Initialise the list for this precinct if we haven't yet seen it */
            if (precinctTotals.get(precinctID) == null)
                precinctTotals.put(precinctID, new ArrayList<>());

            System.out.println("\tPrecinct totals: " + precinctTotals);
            System.out.println("\tP: " + p);
            System.out.println("\tPrecinct ID: " + precinctID);

            Ballot<EncryptedRaceSelection<ExponentialElGamalCiphertext>> total = p.getCastBallotTotal(PEK);

            for (int i=0; i<total.getRaceSelections().size(); i++)
                System.out.println("\t\t" + p.getCastBallotTotal(PEK).getRaceSelections().get(i).getRaceSelectionsMap());

            /* Store the total for that precinct in the list */
            precinctTotals.get(precinctID).add(p.getCastBallotTotal(PEK));
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

            /* Tally the new precinctTotals using WebServerTallier*/
            Ballot<EncryptedRaceSelection<T>> b = WebServerTallier.tally(precinctID, thisPrecinctTotal, PEK);

            /* Replace old totals with new totals */
            summedTotals.put(precinctID, b);
        }

    }

    /**
     * Decrypts and stores the summed totals (the public running tally). Helper method for publishresults()
     *
     * @param summedTotals  the public running tally of totals mapped from precinct ID to precinct results Ballot
     */
    private static void storeDecryptedSummedTotals(Map<String, Ballot<EncryptedRaceSelection<ExponentialElGamalCiphertext>>> summedTotals) throws Exception {

        System.out.println("Decrypting summedTotals...");

        /* Decrypt summedTotals */
        for(Map.Entry<String, Ballot<EncryptedRaceSelection<ExponentialElGamalCiphertext>>> entry : summedTotals.entrySet()) {

            Ballot<EncryptedRaceSelection<ExponentialElGamalCiphertext>> b = entry.getValue();
            String precinctID = entry.getKey();

            Ballot<PlaintextRaceSelection> decryptB;

            /* Load the ICryptoType */
            DHExponentialElGamalCryptoType t = new DHExponentialElGamalCryptoType();

            /* Get all the privateKeyShares from the authorities database */
            List<User> authList = User.find.where().eq("userRole","authority").ne("key", null).findList();
            List<AdderPrivateKeyShare> privateKeyShares = new ArrayList<>();

            int threshold = AuthorityManager.SESSION.getDecryptionThreshold();
            if (threshold > authList.size()) throw new RuntimeException("Decryption threshold was greater than number of private keys present!");

            AdderPrivateKeyShare[] privateKeySharesArray = new AdderPrivateKeyShare[threshold];

            /* Add all the authority keys */
            for (User authority : authList) {

                while(privateKeyShares.size() < threshold)
                    privateKeyShares.add(authority.getKey());
            }


            /* Load the privateKeyShares into the ICryptoType */
            System.out.println("Loading the CryptoType...");
            t.loadPrivateKeyShares(privateKeyShares.toArray(privateKeySharesArray));
            System.out.println("PEK: " + PEK);
            t.loadPublicKey(PEK);

            /* Now set it so that we can decrypt */
            ballotCrypter = new BallotCrypter<>(t);

            System.out.println("Calculating partials...");
            /* Get these in case we want to publish them */
            Map<String, Map<String, Map<String, AdderInteger>>> partials = calculatePartials(b, privateKeyShares, authList);

            System.out.println("Decrypting...");
            /* Will want to publish partials to the bulletin board */
            decryptB = ballotCrypter.decrypt(b);

            System.out.println("Calculating vote totals...");
            /* This will be the decrypted representation of the results by race */
            Map<String, Map<String, Integer>> decryptedResults = WebServerTallier.getVoteTotals(decryptB);

            System.out.println("Creating database representation...");
            /* Store totals in database */
            DecryptedResult.create(new DecryptedResult(precinctID, decryptedResults, b));

        }
    }

    private static Map<String, Map<String, Map<String, AdderInteger>>> calculatePartials(
            Ballot<EncryptedRaceSelection<ExponentialElGamalCiphertext>> b, List<AdderPrivateKeyShare> privateKeyShares, List<User> authList) {

        /* This will be a map for each encrypted race selection to a map of candidates to map of partial decryptions by authority */
        Map<String, Map<String, Map<String, AdderInteger>>> partialsMap = new TreeMap<>();

        for (EncryptedRaceSelection<ExponentialElGamalCiphertext> ers : b.getRaceSelections()) {

            System.out.println("Creating entry for <" + ers.getTitle() + ">...");
            partialsMap.put(ers.getTitle(), new TreeMap<>());

            for (Map.Entry<String, ExponentialElGamalCiphertext> entry : ers.getRaceSelectionsMap().entrySet()) {

                System.out.println("Creating entry for <" + entry.getKey() + ">...");
                ExponentialElGamalCiphertext ctext = entry.getValue();

                partialsMap.get(ers.getTitle()).put(entry.getKey(), new TreeMap<>());

                List<AdderInteger> coeffs = new ArrayList<>();

                for (int i=0; i<privateKeyShares.size(); i++) {
                    coeffs.add(new AdderInteger(i));
                }

                System.out.println("Calculating polynomial...");
                Polynomial poly = new Polynomial(PEK.getP(), PEK.getG(), PEK.getF(), coeffs);
                List<AdderInteger> lagrangeCoeffs = poly.lagrange();

                for (int i=0; i<privateKeyShares.size();i++) {

                    /* Partially decrypt for each share */
                    AdderInteger partial = privateKeyShares.get(i).partialDecrypt(ctext).pow(lagrangeCoeffs.get(i));
                    partialsMap.get(ers.getTitle()).get(entry.getKey()).put(authList.get(i).name, partial);

                }

            }
        }

        return partialsMap;
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

        /* Note that a VotingRecord maps of all the Supervisor serials to their (conflicting) precinct maps
         * We refer to each of these precinct maps as a SupervisorRecord (it is a record written by each Supervisor)
         */

        /* Code for this method in handling a POST command are found at http://www.vogella.com/articles/ApacheHttpClient/article.html */

        final Map<String, String[]> values = request().body().asFormUrlEncoded();
        final String record = values.get("record")[0];

        /* TODO Note that this is random right now, but will be the origin precinctID */
        final String precinctID = values.get("precinctID")[0];

        /* Decode from base64 */
        byte[] bytes = Base64.decode(record);
        Map<String, Map<String, Precinct<ExponentialElGamalCiphertext>>> votingRecord = null;

        try {
            ObjectInputStream o = new ObjectInputStream(new ByteArrayInputStream(bytes));
            votingRecord = (Map<String, Map<String, Precinct<ExponentialElGamalCiphertext>>>) o.readObject();
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

        if (!loginForm.hasErrors()) {
            session().clear();
            session("username", loginForm.get().username);
            System.out.println(session().toString());
            return loginForm.get().role.equals("admin") ? redirect(routes.AuditServer.adminmain()) :
                                                          redirect(routes.AuditServer.authority());
        }
        return null;
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

        @Constraints.Required
        public String username;

        @Constraints.Required
        public String password;

        @Constraints.Required
        public String role;

        public void setUsername(String username) {
            this.username = username;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public void setRole(String role) {
            this.role = role;
        }

        /** This will validate the username and password */
        public String validate() {

            if(!(role.equals("admin") || role.equals("authority")))
                return "Invalid Role";

            if (!User.authenticate(username, password, role))
              return "Invalid user or password!";

            return null;
        }

        public String toString() {
            return "Username: " + username + ", Password: " + password + ", Role: " + role;
        }

    }
    
}
