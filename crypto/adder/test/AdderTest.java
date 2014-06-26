package crypto.adder.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import crypto.adder.AdderInteger;
import crypto.adder.ElgamalCiphertext;
import crypto.adder.Election;
import crypto.adder.Polynomial;
import crypto.adder.PrivateKey;
import crypto.adder.PublicKey;
import crypto.adder.SearchSpaceExhaustedException;
import crypto.adder.Vote;
import crypto.adder.VoteProof;

/**
 * Adder test.
 *
 * @version $LastChangedRevision$ $LastChangedDate$
 * @since 0.0.1
 * @author David Walluck
 */
public class AdderTest extends TestCase {
    /**
     * Constructs a new Adder test.
     *
     * @param name the name of the test
     */
    public AdderTest(String name) {
        super(name);
    }

    /**
     * Test.
     */
    public void test() {
        PublicKey pubKey = PublicKey.makePartialKey(128);

        System.out.println("pubKey = " + pubKey);

        AdderInteger p = pubKey.getP();
        AdderInteger q = pubKey.getQ();
        AdderInteger g = pubKey.getG();
        AdderInteger f = pubKey.getF();

        int maxAuths = 1;
        int numAuths = AdderInteger.random(1, maxAuths + 1);
        int maxChoices = 10;
        int numChoices = AdderInteger.random(1, maxChoices + 1);
        int maxVoters = 20;
        int numVoters = AdderInteger.random(1, maxVoters + 1);

        Map<Integer, Integer>/*<Integer, Integer>*/ voteMap
            = new HashMap<>/*<Integer, Integer>*/(numVoters);

        for (int choice = 0; choice < numChoices; choice++) {
             voteMap.put(choice, 0);
        }

        System.out.println("Creating an election with " + maxVoters
                           + " maximum voters and " + numChoices + " choices");
        
        Election election = new Election(p, null);

        System.out.println("Authorities start");

        List<PublicKey>/*<PublicKey>*/ pubKeys = new ArrayList<>/*<PublicKey>*/(numAuths);
        List<PrivateKey>/*<PrivateKey>*/ privKeys = new ArrayList<>/*<PrivateKey>*/(numAuths);
        List<Polynomial>/*<Polynomial>*/ polys = new ArrayList<>/*<Polynomial>*/(numAuths);

        for (int i = 0; i < numAuths; i++) {
            PublicKey authPubKey = new PublicKey(p, g, f);
            pubKeys.add(authPubKey);
            PrivateKey authPrivKey = authPubKey.genKeyPair();
            privKeys.add(authPrivKey);
            Polynomial authPoly = new Polynomial(p, g, f, numAuths - 1);
            polys.add(authPoly);
        }

        Map<Integer, List<ElgamalCiphertext>>/*<Integer, List<ElgamlCiphertext>>*/ polyMap
            = new HashMap<>/*<Integer, List<ElgamlCiphertext>>*/(numAuths);

        for (int i = 0; i < numAuths; i++) {
            List<ElgamalCiphertext>/*<ElgamlCiphertext>*/ ciphertexts = new ArrayList<>/*<ElgamlCiphertext>*/(numAuths);

            for (int j = 0; j < numAuths; j++) {
                ElgamalCiphertext ciphertext
                    = (pubKeys.get(j)).
                                   encryptPoly((polys.get(i)).
                                   evaluate(new AdderInteger(j, q)));
                ciphertexts.add(ciphertext);
            }

            polyMap.put(i, ciphertexts);
        }

        List<PrivateKey>/*<PrivateKey>*/ finprivKeys
            = new ArrayList<>/*<PrivateKey>*/(numAuths);
        AdderInteger finalH = new AdderInteger(AdderInteger.ONE, p);

        for (int i = 0; i < numAuths; i++) {
            PrivateKey authFinPrivKey
                = (privKeys.get(i))
                  .getFinalPrivKey(polyMap.get(i));
            finprivKeys.add(authFinPrivKey);
            AdderInteger gvalue
                = g.pow((polys.get(i)).
                                      evaluate(new AdderInteger(AdderInteger.ZERO, q)));
            finalH = finalH.multiply(gvalue);
        }

        PublicKey finalPubKey = new PublicKey(p, g, finalH, f);

        System.out.println("finalPubKey = " + finalPubKey);

        System.out.println("Authorities end");

        System.out.println("Voters begin");

        for (int i = 0; i < numVoters; i++) {
            int choice = AdderInteger.random(numChoices).intValue();

            System.out.println("Voter " + (i + 1) + " attempting to cast vote for " + choice);

            List<AdderInteger>/*<AdderInteger>*/ choices
                = new ArrayList<>/*<AdderInteger>*/(numChoices);

            for (int j = 0; j < numChoices; j++) {
                if (j == choice) {
                    choices.add(AdderInteger.ONE);
                } else {
                    choices.add(AdderInteger.ZERO);
                }
            }

            System.out.println("Vote " + (i + 1) + " cast for " + choice);
           
            System.out.println("Encrypting vote " + (i + 1));
            /* TODO Fix this null? */
            Vote vote = finalPubKey.encrypt(choices, null);
            System.out.println("Proving vote " + (i + 1));
            VoteProof proof = new VoteProof();
            proof.compute(vote, finalPubKey, choices, 1, 1);
            System.out.println("Verifying vote " + (i + 1));
            assertTrue(proof.verify(vote, finalPubKey, 1, 1));
            System.out.println("Casting vote " + (i + 1));
            election.castVote(vote);
            voteMap.put(choice,
                    voteMap.get(choice) + 1);
            System.out.println("Vote " + (i + 1) + " casted");
        }

        System.out.println("Voters end");
        
        Vote cipherSum = election.sumVotes();

        System.out.println("cipherSum = " + cipherSum);


        List<AdderInteger>/*<AdderInteger>*/ partial
            = (finprivKeys.get(0)).partialDecrypt(cipherSum);

        assertEquals(numVoters, election.getVotes().size());
        assertEquals(numChoices, numChoices);

        System.out.println("Election performed with " + numChoices
                           + " / " + maxChoices + " choices and "
                           + numVoters + " / " + maxVoters + " voters and "
                           + numAuths + " / " + maxAuths + " authorities");

        System.out.println("trying to get final sum...");

        try {
            List<AdderInteger>/*<AdderInteger>*/ results =
                election.getFinalSum(partial, cipherSum, finalPubKey);
            System.out.println("Got " + results.size() + " results");
            
            System.out.println("++++++\nResult\n++++++\n");

            int i = 0;

            /*for (AdderInteger result : results) {*/
            for (AdderInteger result : results) {
                int realResult
                        = voteMap.get(i);
                System.out.println("o Choice " + i + ": " + result + "|"
                        + realResult);
                //assertEquals(AdderInteger.ZERO, result.getModulus());
                i++;
            }

            int sum = 0;
            i = 0;

            /*for (AdderInteger result : results) {*/
            for (AdderInteger result : results) {
                int realResult = voteMap.get(i);
                sum += realResult;

                assertEquals(new Integer(realResult),
                        new Integer(result.intValue()));
                i++;
            }

            assertEquals(sum, numVoters);

            System.out.println("++++++");
        } catch (SearchSpaceExhaustedException ssee) {
            System.out.println("failed: " + ssee);
            fail();
        }
    }

    /**
     * The main method.
     *
     * @param args the main parameters
     */
    public static void main(String[] args) {
        TestRunner.run(AdderTest.class);
    }
}
