/**
  * This file is part of VoteBox.
  * 
  * VoteBox is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License version 3 as published by
  * the Free Software Foundation.
  * 
  * You should have received a copy of the GNU General Public License
  * along with VoteBox, found in the root of any distribution or
  * repository containing all or part of VoteBox.
  * 
  * THIS SOFTWARE IS PROVIDED BY WILLIAM MARSH RICE UNIVERSITY, HOUSTON,
  * TX AND IS PROVIDED 'AS IS' AND WITHOUT ANY EXPRESS, IMPLIED OR
  * STATUTORY WARRANTIES, INCLUDING, BUT NOT LIMITED TO, WARRANTIES OF
  * ACCURACY, COMPLETENESS, AND NONINFRINGEMENT.  THE SOFTWARE USER SHALL
  * INDEMNIFY, DEFEND AND HOLD HARMLESS RICE UNIVERSITY AND ITS FACULTY,
  * STAFF AND STUDENTS FROM ANY AND ALL CLAIMS, ACTIONS, DAMAGES, LOSSES,
  * LIABILITIES, COSTS AND EXPENSES, INCLUDING ATTORNEYS' FEES AND COURT
  * COSTS, DIRECTLY OR INDIRECTLY ARISING OUR OF OR IN CONNECTION WITH
  * ACCESS OR USE OF THE SOFTWARE.
 */

package supervisor.model.tallier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import sexpression.*;
import sexpression.stream.*;

/**
 * Temporary class for tallying votes as they are seen (unencrypted) on the
 * network
 * 
 * @author cshaw
 */
public class Tallier implements ITallier{

    /**
     * This is the pattern that ballots in this tallier will take, of the form
     * (String (List...)) where String is any valid string and List... is a null
     * list node or another list expression.
     */
	private static ASExpression pattern = new ListWildcard(new ListExpression(
			StringWildcard.SINGLETON, new ListWildcard(Wildcard.SINGLETON)));


    /** A map of votes to subvotes */
	private TreeMap<String, HashMap<String, Integer>> votes;

	/**
	 * Constructs a new Tallier with its counts zeroed out
	 */
	public Tallier() {
		votes = new TreeMap<String, HashMap<String, Integer>>();
	}

	/**
	 * @see supervisor.model.tallier.ITallier#getReport()
	 */
	public Map<String, BigInteger> getReport() {
		Map<String, BigInteger> results = new HashMap<String, BigInteger>();

        /*
         * Build the map to be returned by counting each candidate's
         * votes and then mapping them to that candidate's name
         */
		for(Map<String, Integer> race : votes.values()){
			for(String candidate : race.keySet())
				results.put(candidate, new BigInteger(""+race.get(candidate)));
		}

		return results;
		
	}

	/**
	 * @see supervisor.model.tallier.ITallier#recordVotes(byte[], ASExpression nonce)
	 */
	public void recordVotes(byte[] ballot, ASExpression ignoredNonce) {
        /* This will read in the ballot as a byte array and then parse the data as read in */
		ASEInputStreamReader in = new ASEInputStreamReader(
				new ByteArrayInputStream(ballot));
		try {
			ASExpression sexp = in.read();

            /* If the read in SExpression is valid, parse it accordingly */
			if (pattern.match(sexp) != NoMatch.SINGLETON) {

                /* Since we've matched it to the pattern, we know the expression is a ListExpression */
				ListExpression list = (ListExpression) sexp;

                /* Relying on the ListExpression's array representation, parse the expression */
				for (ASExpression s : list.getArray()) {

                    /* Definitionally, any component of a ListExpression is another ListExpression */
					ListExpression vote = (ListExpression) s;

                    /*
                     * The choice expression will look something like this:
                     *  (B0 (B1 0) (B2 1) (B3 0))
                     * Where B0 is the race Identifier, and B1, B2, and B3 are candidates paired with how many
                     * votes each candidate received.
                     */

                    /* The first entry in the list will be the race identifier */
					String race = vote.get(0).toString();

                    /* The second entry in the list will be the expression of choice. */
					ListExpression choiceExp = (ListExpression) vote.get(1);

                    /* Check to make sure the expression is non-null */
					if (choiceExp.size() > 0) {

                        /* Pull out the choice for this candidate */
						String choice = choiceExp.get(0).toString();

                        /* Now get the corresponding race in the map of tallied results */
						HashMap<String, Integer> raceVals = votes.get(race);

                        /* If we haven't seen this race before, add it to the map of results */
						if (raceVals == null) {
							raceVals = new HashMap<String, Integer>();
							votes.put(race, raceVals);
						}

                        /* Now that we have an entry in the results map, add this vote to the total */
						Integer val = raceVals.get(choice);

                        /* If the race hasn't been tallied before, put in a new result */
						if (val == null)
							votes.get(race).put(choice, 1);

                        /* Otherwise add 1 to the previous tally */
						 else
							votes.get(race).put(choice, val + 1);

					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidVerbatimStreamException e) {
			System.err.println("Tallier.recordVotes(): error: ballot wasn't correctly formatted, so couldn't do the tally");
			System.err.println("Ballot data: " + Arrays.toString(ballot));
			System.err.println("exception: " + e);
		}
	}

    /**
     * @see supervisor.model.tallier.ITallier#confirmed(sexpression.ASExpression)
     */
	public void confirmed(ASExpression nonce) {
		throw new RuntimeException("Tallier.confirmed NOT IMPLEMENTED");
	}
}
