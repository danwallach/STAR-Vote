package utilities;

import auditorium.Bugout;
import crypto.*;
import crypto.adder.*;
import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.StringExpression;
import supervisor.model.Ballot;

import java.util.*;

/**
 * This class is the web-server half of the STAR-Vote tallier model. Its function is to
 * decrypt challenged ballots and tally and decrypt final vote counts across and within precincts.
 *
 * @author Matthew Kindy II
 */
public class WebServerTallier {

    /**
     * Sum every vote in cast and return a Ballot with encrypted sums that includes every race
     * across all Ballots.
     *
     * @param ID            the ID to be assigned to this ballot post-tallying, used as a ballot id
     * @param toSum         the list of cast ballots that should be homomorphically summed
     * @param PEK           the public key used for vote proofs
     * @return              a Ballot containing the encrypted sums for each race
     */
    public static <T extends AHomomorphicCiphertext> Ballot<EncryptedRaceSelection<T>> tally(
            String ID, List<Ballot<EncryptedRaceSelection<T>>> toSum, IPublicKey PEK){

        int size = 0;

        /* The results of the election are stored by race ID in this map */
        Map<String, Race<T>> results = new HashMap<>();

        /* For each ballot, get each vote and build a results mapping between race ids and elections */
        for (Ballot<EncryptedRaceSelection<T>> bal : toSum) {

            try {

                List<EncryptedRaceSelection<T>> raceSelections = bal.getRaceSelections();

                /* Cycle through each of the races */
                for(EncryptedRaceSelection<T> rs: raceSelections){

                    /* Get all the candidate choices */
                    List<String> possibleCandidates = new ArrayList<>(rs.getRaceSelectionsMap().keySet());

                    /* Confirm that the rs proof is valid */
                    System.out.println("In WebserverTallier.tally() -- verifying the race selections. ");

                    if (!rs.verify(0, 1, PEK)) {
                        Bugout.err("!!!Ballot failed NIZK test!!!");
                        return null;
                    }
                    else
                        System.out.println("Race selection was successfully verified!\n");

                    /* Code these results as a race so the ciphers can be summed homomorphically */
                    String raceID = rs.getTitle();
                    Race<T> race = results.get(raceID);

                    /* If we haven't seen this specific race before, initialize it */
                    if (race == null)
                        race = new Race<>(PEK, possibleCandidates);

                    /* This will ready race to homomorphically tally the rs */
                    race.castRaceSelection(rs);

                    /* Now save the result until we're ready to decrypt the totals */
                    results.put(raceID, race);
                }

                size += bal.getSize();
            }
            catch (Exception e) {
                Bugout.err("Malformed ballot received <" + e.getMessage() + ">");
                Bugout.err("Rejected ballot:\n" + bal);
            }
        }

        /* This will hold the final list of summed Votes to be put into a Ballot */
        ArrayList<EncryptedRaceSelection<T>> raceSelections = new ArrayList<>();

        /* This will be used to create the nonce eventually */
        ArrayList<ASExpression> raceSelectionsASE = new ArrayList<>();

        /* Now go through each race */
        for(String id :  results.keySet()) {

            /* Get the race */
            Race<T> thisRace = results.get(id);

            /* Get the homomorphically tallied race selection for this race */
            System.out.println("Entering Race.sumRaceSelections(): ");
            EncryptedRaceSelection<T> summedRS = results.get(id).sumRaceSelections();

            /* Verify the race selection proof and error off if bad */

            System.out.println("In WebserverTallier.tally() -- Verifying this summed race ");
            if(summedRS.verify(0, thisRace.getRaceSelections().size(), PEK)) {
                raceSelections.add(summedRS);
                raceSelectionsASE.add(summedRS.toASE());
                System.out.println("This race was successfully added to the Ballot!");
            }
            else System.err.println("There was a bad summed race that was not added to the ballot!");
        }

        /* Create the nonce */
        ListExpression rsList = new ListExpression(raceSelectionsASE);
        String nonce = StringExpression.makeString(rsList.getSHA256()).toString();

        /* Return the Ballot of all the summed race results */
        return new Ballot<>(ID, raceSelections, nonce, size);
    }

    /**
     * Calculates the individual vote totals for each of the candidates in each of the races in the Ballot
     *
     * @param toTotal       the previously tallied Ballot from which to extract the candidate sums
     * @return              a mapping of candidates to vote totals (mapped to race names) for each race in a Ballot
     */
    public static Map<String, Map<String,Integer>> getVoteTotals(Ballot<PlaintextRaceSelection> toTotal) {


        Map<String, Map<String, Integer>> voteTotals = new TreeMap<>();

        /* Iterate over each of the race selections */
        for (PlaintextRaceSelection rs: toTotal.getRaceSelections()) {

            String raceName = rs.getTitle();

            /* Put in the tallied totals for each race */
            voteTotals.put(raceName, rs.getRaceSelectionsMap());
        }

        return voteTotals;
    }

}