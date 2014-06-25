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

package crypto;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import crypto.adder.AdderInteger;
import crypto.adder.Election;
import crypto.adder.ElgamalCiphertext;
import crypto.adder.PrivateKey;
import crypto.adder.PublicKey;
import crypto.adder.Vote;
import crypto.adder.VoteProof;

import auditorium.Bugout;
import auditorium.Key;

import sexpression.*;
import crypto.interop.AdderKeyManipulator;
import sexpression.stream.InvalidVerbatimStreamException;
import votebox.middle.ballot.*;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * This class is the main interface between STAR-Vote operations and our crypto library.
 * All cryptographic operations on ballots are performed using this class, with few exceptions
 * which rely on the bare-metal of the Adder ElGamal code to homorphically tally and decrypt election
 * results. However, note that these operations are made possible by the encryption performed in this class,
 * so in reality even uses of the Adder library outside of this class are coupled with this.
 */
public class BallotEncrypter {

    /** Since we never need to instantiate this class multiple times, we can use the singleton pattern */
    public static final BallotEncrypter SINGLETON = new BallotEncrypter();

    /** This will hold a list of all the random values used in encrypt each vote, for use with a piecemeal encrypter */
    private List<BigInteger> randomList;

    /** This is a record of the previously encrypted ballot, for use with a piecemeal encrypter */
    private ListExpression recentBallot;

    /** This expounds upon @see randomList */
    private List<List<AdderInteger>> adderRandom;


    /**
     * An empty private constructor to complete the singleton pattern
     */
    private BallotEncrypter() { }

    /**
     * Takes an unencrypted ballot and encrypts it, while also generating a set of NIZKs to prove it is well formed.
     * 
     *
     * @param bid
     * @param ballot         unencrypted ballot of the form ((candidate-id counter)...) counter = {0, 1}, with possible write-in field appended
     *                       e.g. ((B0 0)(B1 0)(B2 1)...) This is a list(list ASExpression).
     *
     * @param raceGroups     a list of of groups of race-ids that are considered "together" in a well formed ballot.
     *
     * @param pubKey         the Adder PublicKey to use to encrypt the ballot and generate the NIZKs
     *
     * @param nonce
     * @return               a ListExpression in the form (((vote [vote]) (vote-ids ([id1], [id2], ...)) (proof [proof])) ... (public-key [key]))
     */
    public ListExpression encryptWithProof(String bid, ListExpression ballot, List<List<String>> raceGroups, PublicKey pubKey, ASExpression nonce){
        adderRandom = new ArrayList<>();
        List<ASExpression> subBallots = new ArrayList<>();

        /* Randomly generate a key for write-in encryption, will be sent over the wire, encrypted */
        byte[] writeInKey = new byte[16];

        for (int i = 0; i < 16; i++)
            writeInKey[i] = (byte) (Math.random() * 16);


        List<AdderInteger> keyParts = new ArrayList<>();

        /* In order to fool Adder into encrypting our key properly, we break it into parts
        Which represent "votes" that will be encrypted using existing ElGamal */
        for (int i = 0; i < 16; i++) {

            /* Break off a byte */
            AdderInteger extractedByte = new AdderInteger(new BigInteger(Arrays.copyOfRange(writeInKey, i, i + 1)));

            /* Add to the list of parts */
            keyParts.add(extractedByte);
        }

        Map<String, ListExpression> ballotMap = new HashMap<>();

        /* Iterate over each of the race vote records (i.e. each candidate) */
        for (int i = 0; i < ballot.size(); i++) {

                /* Extract the ith race vote record from the ballot */
                ListExpression vote = (ListExpression)ballot.get(i);

                /* Pull out the candidate id */
                String id = vote.get(0).toString();

                /* Map the candidate id to the race vote record */
                ballotMap.put(id, vote);
        }


        /* Iterate over the races (pull out each group of candidates) */
        for(List<String> group : raceGroups){

                /* Create an ArrayList to hold the vote records for a single race */
                List<ASExpression> races = new ArrayList<>();

                /* Iterate over the candidates and get the vote corresponding to that candidate and add it to races */
                for(String candidateId : group)
                        races.add(ballotMap.get(candidateId));

                /* Create a new  ListExpression for the entire race from the races ArrayList */
                ListExpression subBallot = new ListExpression(races);

                /* Encrypt the mapped sub-ballot with the elGamal Public key and the random generated writeInKey */
                ListExpression encryptedSubBallot = encryptSubBallotWithProof(subBallot, pubKey, writeInKey);

                /* Add the encrypted sub-ballot to the list of sub-ballots (this will be the entire ballot eventually) */
                subBallots.add(encryptedSubBallot);
        }

        /* Non-homomorphically encrypt the write-in key */
        //ElgamalCiphertext encryptedKey = pubKey.encryptNoHomo(new AdderInteger(new BigInteger(writeInKey)));

        /* Add the s-expression of the encrypted write-in key to the list of encrypted sub-ballots */
        //subBallots.add(encryptedKey.toASE());



        /* Convert this list into a ListExpression and set the most recent ballot to this */
        ListExpression votes = new ListExpression(subBallots);

        ASExpression keyExp = AdderKeyManipulator.generateFinalPublicKey(pubKey).toASE();

        recentBallot = new ListExpression(StringExpression.makeString("ballot"), StringExpression.makeString(bid), votes, nonce, keyExp);

        return recentBallot;
    }
    
    /**
     * Take an unencrypted ballot and make it encrypted, while also generating a NIZK
     * 
     * @param subBallot         This is the pre-encrypt ballot in the form ((race-id counter) ...)
     * @param pubKey            this is an Adder-style public key
     * @param writeInKey        the key used to encrypt the writeIn
     * @return                  A ListExpression of the form ((vote [vote]) (vote-ids ([id1], [id2], ...)) (proof [proof]) (public-key [key]))
     */
    @SuppressWarnings("unchecked")
    private ListExpression encryptSubBallotWithProof(ListExpression subBallot, PublicKey pubKey, byte[] writeInKey){

        List<AdderInteger> value    = new ArrayList<>();
        List<ASExpression> valueIds = new ArrayList<>();
        List<String> writeIns       = new ArrayList<>();

        List<ASExpression> secureWriteIns;

        /* Iterate over each of the race vote records (i.e. each candidate) */
        for(int i = 0; i < subBallot.size(); i++){

            /* Pull out the candidate ID and the vote counter from the sub ballot as a List Expression */
            ListExpression choice = (ListExpression)subBallot.get(i);

            /* Pull out the vote counter for each candidate in the race */
            ASExpression voteCounter = choice.get(1);
            String selection = voteCounter.toString();


//            TODO check the value of the counter
//            Split off the write-in fields
//            if(selection.length() > 1){
//                String[] write = {selection.substring(0,1), selection.substring(1)};
//                selection = write[0];
//                writeIns.add(write[1]);
//
//            }

    		value.add(new AdderInteger(selection));

            ASExpression candidateID = choice.get(0);
            valueIds.add(candidateID);
        }

        PublicKey finalPubKey = AdderKeyManipulator.generateFinalPublicKey(pubKey);

        Vote vote = finalPubKey.encrypt(value);

        /* Important data from the ElGamal Encryption */
        List<ElgamalCiphertext> ciphers = vote.getCipherList();
		
		List<AdderInteger> subRandom = new ArrayList<>();

        /* Building a list of random values that are used to encrypt the vote counters. */
		for(ElgamalCiphertext cipher : ciphers)
			subRandom.add(cipher.getR());

		/* Add this list of random values for the subBallot to the entire ballot list. */
		adderRandom.add(subRandom);

        /* Checking the encrypted subBallots against the proofs*/
		VoteProof proof = new VoteProof();
		proof.compute(vote, finalPubKey, value, 0, 1);

        ASExpression outASE = vote.toASE();

        //Now stick the encrypted write-ins back into the votes
//        for(ASExpression written : secureWriteIns){
//            outASE = StringExpression.make(outASE.toString() + written);
//        }

        /* Create the return list of the vote, vote ids, proof and corresponding public keys */
		ListExpression idList = new ListExpression(StringExpression.makeString("vote-ids"),new ListExpression(valueIds));
		ListExpression pList  = new ListExpression(StringExpression.makeString("proof"),	proof.toASE());

        return new ListExpression(outASE, idList, pList);
    }

    /**
     * A method which encrypts write ins using the AES scheme
     *
     * @param writeIns      the written-in values for candidate names
     * @param key           the key to use for AES encryption, will be encrypted and sent with encrypted ballot
     * @return              result  a List of ASExpressions representing the encrypted bytes of each write-in
     */
    private List<ASExpression> encryptWriteIns(List<String> writeIns, byte[] key) {

        List<ASExpression> encrypted = new ArrayList<>();

        try {

            /* Set up crypto */
            Cipher c = Cipher.getInstance("AES");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            c.init(Cipher.ENCRYPT_MODE, keySpec);

            /* Iterate over the write-ins */
            for (String writeIn : writeIns) {

                /* Extract the write-in values and encrypt them */
                byte [] enc = c.doFinal(writeIn.getBytes());

                /* Convert to an ASExpression and add to the list of encrypted write-in values */
                encrypted.add(ASExpression.makeVerbatim(enc));
            }
        }
        catch (BadPaddingException | NoSuchAlgorithmException | IllegalBlockSizeException | NoSuchPaddingException |
               InvalidKeyException | InvalidVerbatimStreamException e) { e.printStackTrace(); }

        return encrypted;
    }

    /**
     * Take an unencrypted ballot form and make it encrypted.
     * 
     * @param ballot        The pre-encrypt ballot in the form ((race-id counter)...)
     * @param publicKey     The public ElGamal key used to encrypt the ballot
     * @return              Returns the encrypted form of ballot in the form ((race-id E(counter))...)
     */

    public ListExpression encrypt(ListExpression ballot, Key publicKey) {

        /* Reset the encrypter */
    	ElGamalCrypto.SINGLETON.clearRecentRandomness();

        ArrayList<ASExpression> encryptedpairs = new ArrayList<>();

        /* Iterate over the ballot list */
        for (ASExpression ase : ballot) {

            ListExpression le = (ListExpression) ase;

            /* Extracting the candidate-id and the corresponding counter */
            StringExpression id = (StringExpression) le.get(0);
            StringExpression count = (StringExpression) le.get(1);


//            String writeIn = "";
//            if(count.size() > 1){
//                writeIn = count.toString().substring(1);
//                count = StringExpression.makeString(count.toString().substring(0, 1));
//            }

            /* Encrypt the counter corresponding to the candidate-id and store it in cipher (c1,c2) using the elGamal public key */
            Pair<BigInteger> cipher = ElGamalCrypto.SINGLETON.encrypt(publicKey, new BigInteger(count.toString()));


            /* Convert the ciphertexts to StringExpressions c1 and c2 */
            StringExpression gY       = StringExpression.makeString(cipher.get1().toString());
            StringExpression mPrimeS  = StringExpression.makeString(cipher.get2().toString());

            /* Concatenate as a ListExpression (c1,c2) */
            ASExpression cipherASE = new ListExpression(gY, mPrimeS);

            /* Add to the list of ciphertexts */
            encryptedpairs.add(new ListExpression(id, cipherASE));
        }

        /* Set the entire ballot encryption */
        recentBallot = new ListExpression(encryptedpairs);

        /* Save the R values for fast decryption later */
        randomList = ElGamalCrypto.SINGLETON.getRecentRandomness();

        /* Reset the encrypter */
        ElGamalCrypto.SINGLETON.clearRecentRandomness();

        return recentBallot;
    }
    
    /**
     * Decrypt an Adder Election using a PrivateKey.
     * 
     * @param election      Collection of encrypted votes
     * @param publicKey     The public key for the election
     * @param privateKey    The shared private key for the election
     * @return              List of decrypted vote counters
     */
    @SuppressWarnings("unchecked")
	public List<AdderInteger> adderDecryptWithKey(Election election, PublicKey publicKey, PrivateKey privateKey){

        /*

    	  Adder encrypt is of m (public initial g, p, h) [inferred from code]
    	                    m = {0, 1}
    	                    g' = g^r = g^y
    	                    h' = h^r * f^m = h^y * m'

    	  Quick decrypt (given r) [puzzled out by Kevin Montrose]
    	                    confirm g^r = g'
    	                    m' = (h' / (h^r)) = h' / h^y
    	                    if(m' == f) m = 1
    	                    if(m' == 1) m = 0

    	*/


        /* Generate the final private and public keys */
    	PrivateKey finalPrivateKey = AdderKeyManipulator.generateFinalPrivateKey(publicKey, privateKey);
    	PublicKey finalPublicKey = AdderKeyManipulator.generateFinalPublicKey(publicKey);

        /* Homomorphically tally the encrypted votes  */
    	Vote cipherSum = election.sumVotes();

        /* Partially Decrypt the partial sums */
        List<AdderInteger> partialSum = finalPrivateKey.partialDecrypt(cipherSum);

        /*  Add and completely decrypt to get the final sums */
        return election.getFinalSum(partialSum, cipherSum, finalPublicKey);
    }
    
    /**
     * Decrypt an Adder ballot using the random values.
     * 
     * @param ballot
     * @param rVals
     * @return Decrypted ballot, of the form ((race-id [adder integer]) ...)
     */
    public ListExpression adderDecrypt(ListExpression ballot, List<List<AdderInteger>> rVals){
    	Map<String, Vote> idsToVote = new HashMap<>();
    	Map<String, PublicKey> idsToPubKey = new HashMap<>();
    	Map<String, List<AdderInteger>> idsToRs = new HashMap<>();
    	Map<String, List<AdderInteger>> idsToPlaintext = new HashMap<>();

        ListExpression votes = (ListExpression)ballot.get(2);

        /*Extract ballot information - raceids , random value, and the public keys*/
    	for(int i = 0; i < votes.size(); i++){
    		ListExpression race = (ListExpression)votes.get(i);

            Vote vote = Vote.fromASE(race.get(0));
    		ListExpression voteIds = (ListExpression)(race.get(1));

            PublicKey finalPubKey = PublicKey.fromASE(ballot.get(4));
    		
    		idsToVote.put(voteIds.toString(), vote);
    		idsToRs.put(voteIds.toString(), rVals.get(i));
    		idsToPubKey.put(voteIds.toString(), finalPubKey);
    	}
        /* Iterate over the set of ids and decrypt the adder integer using their corresponding public keys */
    	for(String ids : idsToVote.keySet()){
    		Vote vote = idsToVote.get(ids);
    		List<AdderInteger> rs = idsToRs.get(ids);
    		PublicKey finalPubKey = idsToPubKey.get(ids);

    		List<AdderInteger> d = adderDecryptSublist(vote, rs, finalPubKey);

            idsToPlaintext.put(ids, d);
    	}

    	return toTraditionalFormat(idsToPlaintext);
    }

    /**
     *
     * @param idsToPlaintext
     * @return
     */
    private ListExpression toTraditionalFormat(Map<String, List<AdderInteger>> idsToPlaintext){
    	List<ASExpression> subLists = new ArrayList<>();
    	
    	for(String ids : idsToPlaintext.keySet()){
    		List<StringExpression> idList = parseIds(ids);
            List<AdderInteger> plaintexts = idsToPlaintext.get(ids);

    		for(int i = 0; i < idList.size(); i++){
    			StringExpression id = idList.get(i);
    			AdderInteger plaintext = plaintexts.get(i);
    			List<ASExpression> subList = new ArrayList<>();
    			subList.add(id);
    			subList.add(plaintext.toASE());
    			subLists.add(new ListExpression(subList));
    		}
    	}

    	return new ListExpression(subLists);
    }
    
    private List<StringExpression> parseIds(String ids){

        /* Take out the id, er, identifier */
        ids = ids.replaceFirst("vote-ids ", "");

        String[] strs = ids.split(" ");
    	List<StringExpression> toRet = new ArrayList<>();
    	
    	for(String str : strs){
    		toRet.add(StringExpression.makeString(str.replaceAll("\\(", "").replaceAll("\\)", "")));
    	}
    	
    	return toRet;
    }
    
    /**
     * Decrypt a single Adder vote using the provided random values.
     * 
     * @param vote
     * @param rVals
     * 
     * @return Decrypted vote as a list of integers
     */
    @SuppressWarnings("unchecked")
	public List<AdderInteger> adderDecryptSublist(Vote vote, List<AdderInteger> rVals, PublicKey key){


    	
    	List<ElgamalCiphertext> ciphers = vote.getCipherList();
    	List<AdderInteger> ret = new ArrayList<>();
    	
    	int i = 0;
    	
    	for(ElgamalCiphertext cipher : ciphers){
    		AdderInteger r = rVals.get(i);
    		
    		AdderInteger gPrime = cipher.getG();
    		AdderInteger hPrime = cipher.getH();
    		
    		if(!key.getG().pow(r).equals(gPrime)){
    			Bugout.err("Random value does not correspond to ciphertext.");
    			return null;
    		}
    		
    		AdderInteger mPrime = hPrime.divide(key.getH().pow(r));
    		AdderInteger m = null;
    		
    		/*Observe that m was 0 or 1, thus step must be either f or 1 respectively */

    		if(mPrime.equals(AdderInteger.ONE)){
    			m = AdderInteger.ZERO;
    		}//if
    		
    		if(mPrime.equals(key.getF())){
    			m = AdderInteger.ONE;
    		}//if
    		
    		if(m == null){
    			Bugout.err("Expected intermediate step to be f or 1, found "+mPrime+"\n [f = "+key.getF()+"]");
    			return null;
    		}
    		
    		ret.add(m);
    		
    	    i++;
    	}
    	
    	return ret;
    }
    
    /**
     * Decrypt a ballot using the r-values (not the decryption key).
     * 
     * @param ballot        The ballot, formatted ((race-id encrypted-counter)...)
     * @param rVals         The r-values, formatted ((race-id r-value)...)
     * @param publicKey     The ElGamal public key.
     * @return              Returns the decrypted ballot, formatted ((race-id plaintext-counter)...)
     */
    public ListExpression decrypt(ListExpression ballot, ListExpression rVals, Key publicKey) {

        if (ballot.size() != rVals.size())
            throw new RuntimeException("sizes must match");
        if (Ballot.BALLOT_PATTERN.match(ballot) == NoMatch.SINGLETON)
            throw new RuntimeException("ballot incorrectly formatted");
        if (Ballot.BALLOT_PATTERN.match(rVals) == NoMatch.SINGLETON)
            throw new RuntimeException("r-vals incorrectly formatted");

        ArrayList<ASExpression> decryptedpairs = new ArrayList<>(
                ballot.size());
        Iterator<ASExpression> ballotitr = ballot.iterator();
        Iterator<ASExpression> ritr = rVals.iterator();
        while (ballotitr.hasNext()) {
            ListExpression ballotnext = (ListExpression) ballotitr.next();
            ListExpression rnext = (ListExpression) ritr.next();

            if (!ballotnext.get(0).equals(rnext.get(0)))
                throw new RuntimeException("incorrect set of r-values: uids do not match");

            ASExpression uid = ballotnext.get(0);
            BigInteger r = new BigInteger(((StringExpression) rnext.get(1)).toString());
            BigInteger cipher1 = new BigInteger(((ListExpression)ballotnext.get(1)).get(0).toString());
            BigInteger cipher2 = new BigInteger(((ListExpression)ballotnext.get(1)).get(1).toString());
            
            Pair<BigInteger> cipher = new Pair<>(cipher1, cipher2);

            /* decryption is being done using the elGamal crypto - less overhead */
            BigInteger plaincounter = ElGamalCrypto.SINGLETON.decrypt(r,
                    publicKey, cipher);
            decryptedpairs.add(new ListExpression(uid, StringExpression
                    .makeString(plaincounter.toString())));
        }
        return new ListExpression(decryptedpairs);
    }

    /**
     * Get the most recent random list.
     * 
     * @return This method returns the random list in the form ((uid rvalue)...)
     */
    public ListExpression getRecentRandom() {
        ArrayList<ASExpression> pairs = new ArrayList<>();

        Iterator<ASExpression> ballotitr = recentBallot.iterator();
        Iterator<BigInteger> ritr = randomList.iterator();

        while (ballotitr.hasNext()) {
            ListExpression ballotpair = (ListExpression) ballotitr.next();
            BigInteger r = ritr.next();
            pairs.add(new ListExpression(ballotpair.get(0), StringExpression
                    .makeString(r.toString())));
        }

        return new ListExpression(pairs);
    }

    /**
     * Get the most recent random, for the Adder encryption sub-system.
     * 
     * @return      The random list used in the last call to encryptWithProof(...).
     */
    public List<List<AdderInteger>> getRecentAdderRandom(){
    	return adderRandom;
    }
    
    /**
     * Get the result of the most recent encrypt call.
     * 
     * @return      The most recent encryption.
     */
    public ListExpression getRecentEncryptedBallot() {
        return recentBallot;
    }

    /**
     * Clear the state.
     */

    public void clear() {
        recentBallot = null;
        randomList = null;
        adderRandom = new ArrayList<>();
    }

    /**
     * I'm going to use this main as a sandbox for generating performance
     * numbers using this encryption, etc.
     * 
     * @param args
     */
    public static void main(String[] args) throws Exception {


        BallotEncrypter be = new BallotEncrypter();

        /* Randomly generate a key for write-in encryption, will be sent over the wire, encrypted */
        byte[] writeInKey = new byte[16];

        for (int i = 0; i < 16; i++)
            writeInKey[i] = (byte) (Math.random() * 16);

        System.out.println("The write in key is: " + Arrays.toString(writeInKey));
        List<AdderInteger> keyParts = new ArrayList<>();

        /* In order to fool Adder into encrypting our key properly, we break it into parts
        Which represent "votes" that will be encrypted using existing ElGamal */
        for (int i = 0; i < 16; i++) {
            keyParts.add(new AdderInteger(new BigInteger(Arrays.copyOfRange(writeInKey, i, i + 1))));
            System.out.print(keyParts.get(i) + " ");
        }

        String[] ins = {"null", "John Q. Adams"};
        List<String> write = Arrays.asList(ins);

        List<ASExpression> res = be.encryptWriteIns(write, writeInKey);


    }


}
