/**
 *
 * TODO Revise this?
 *
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

import auditorium.Bugout;
import auditorium.Key;
import crypto.ElGamalCrypto;
import crypto.Pair;
import sexpression.*;
import sexpression.stream.ASEInputStreamReader;
import sexpression.stream.InvalidVerbatimStreamException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * A tallier which uses exponential ElGamal encryption to homomorphically tally ballots and
 * then only decrypts the total.
 */
public class EncryptedTallier implements ITallier {
    /** @see supervisor.model.tallier.Tallier#pattern */
    private static ASExpression PATTERN = new ListWildcard(new ListExpression(StringWildcard.SINGLETON, Wildcard.SINGLETON));

    /** This private key will allow for final decryption */
    private Key _privateKey = null;

    /** @see supervisor.model.tallier.Tallier#votes */
    private Map<String, Pair<BigInteger>> _votes = new HashMap<String, Pair<BigInteger>>();

    /**
     * Constructor which stores the key that will be used for decryption
     *
     * @param privateKey ElGamal private key which is used in decryption
     */
    public EncryptedTallier(Key privateKey){
        _privateKey = privateKey;
    }

    /**
     * Uses the private key to decrypt the homomorphically tallied votes, race by race.
     *
     * @see ITallier#getReport()
     */
    public Map<String, BigInteger> getReport() {

        /* This map will store the final results */
        Map<String, BigInteger> results = new HashMap<String, BigInteger>();

        /* For each candidate, retrieve the encrypted vote totals */
        for(String candidate : _votes.keySet()){

            /* This is the encrypted total for the given candidate */
            Pair<BigInteger> value = _votes.get(candidate);

            /* Decrypt the total, and store the results to be returned */
            BigInteger decryptedValue = ElGamalCrypto.SINGLETON.decrypt(_privateKey, value);
            results.put(candidate, decryptedValue);
        }

        return results;
    }

    /**
     * @see supervisor.model.tallier.ITallier#recordVotes(byte[], sexpression.ASExpression)
     *
     * @param ignoredNonce the nonce here isn't used since we don't do NIZKs TODO verify this
     */
    public void recordVotes(byte[] ballotBytes, ASExpression ignoredNonce) {
        /* This will parse the raw ballot bytes */
        ASEInputStreamReader in = new ASEInputStreamReader(
                new ByteArrayInputStream(ballotBytes));
        try {
            ASExpression sexp = in.read();

			/* Check that the ballot is well-formed */
            if(PATTERN.match(sexp) != NoMatch.SINGLETON){
                /* Now that we know the ballot is a ListExpression, enforce the type */
                ListExpression ballot = (ListExpression)sexp;

                /* For each encrypted vote in the ballot */
                for(ASExpression voteE : ballot){

                    /* Enforce its ListExpression type */
                    ListExpression vote = (ListExpression)voteE;

                    /* Grab the race information for this vote */
                    String raceID = vote.get(0).toString();

                    /* Grab the vote choice cipher text */
                    ListExpression encryptedVote = (ListExpression)vote.get(1);

                    /* As per ElGamal, our cipher texts are pairs */
                    String pairPart1 = encryptedVote.get(0).toString();
                    String pairPart2 = encryptedVote.get(1).toString();

                    /* Wrap our ciphers in the Pair class */
                    Pair<BigInteger> pair = new Pair<BigInteger>(new BigInteger(pairPart1), new BigInteger(pairPart2));

                    /* Get the current encrypted total out of the mapping of races to votes */
                    Pair<BigInteger> currentTotal = _votes.get(raceID);

                    /* Ensure that the total exists */
                    if(currentTotal != null)
						/*
						 * We generate a new cyphertext which has a plain text equivalent to
						 * D(pair) + D(currentTotal) - the sum of the decrypted pair and currentTotal values -
						 * by multiplying pair and currentTotal.
						 */
                        currentTotal = ElGamalCrypto.SINGLETON.mult(pair, currentTotal);


                    /* If the given race doesn't have a sum yet, make this cipher pair the sum */
                    else
                        currentTotal = pair;

                    /* Put the newly summed total back into the map */
                    _votes.put(raceID, currentTotal);
                }
            }

            /* If the ballot is not well formed, we cannot continue and must error */
            /* TODO Maybe improve the error displaying? */
            else
                Bugout.err("Received a malformed ballot.\n"+sexp+" does not match "+PATTERN);

        }catch(IOException e){
            Bugout.err("Encountered IOException when counting encrypted vote: "+e.getMessage());
        } catch (InvalidVerbatimStreamException e) {
            Bugout.err("Encountered InvalidVerbatimStream when counting encrypted vote: "+e.getMessage());
        }
    }

    /**
     * @see supervisor.model.tallier.ITallier#confirmed(sexpression.ASExpression)
     */
    public void confirmed(ASExpression nonce) {
        throw new RuntimeException("EncryptedTallier.confirmed NOT IMPLEMENTED");
    }

}