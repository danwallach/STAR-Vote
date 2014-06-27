package supervisor.model.tallier;

import auditorium.Bugout;
import crypto.adder.*;
import crypto.interop.AdderKeyManipulator;
import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.stream.ASEInputStreamReader;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tallier for elections run with NIZKs but without the commit-challenge model enabled.
 *
 * @author Montrose
 */
public class EncryptedTallierWithNIZKs implements ITallier {

    /** An ElGamal private key for the decryption of votes and for the NIZK proof process */
    protected PrivateKey _privateKey = null;

    /** The ElGamal public key for the NIZK proof process */
    protected PublicKey _publicKey = null;

    /** The final public key, to ensure that the public key remains the same throughout the election process */
    protected PublicKey _finalPublicKey = null;

    /** The final private key, to ensure that the same private key is used througout the election */
    protected PrivateKey _finalPrivateKey = null;

    /** The results of the election are stored by race ID in this map */
    protected Map<String, Election> _results = new HashMap<String, Election>();

    /**
     * Constructor.
     *
     * @param pub - The PublicKey used to encrypt votes to be tallied.
     * @param priv - The PrivateKey to be used to decrypt the totals.
     */
    public EncryptedTallierWithNIZKs(PublicKey pub, PrivateKey priv){
        _privateKey = priv;
        _publicKey = pub;

    }

    /**
     * This is not a challenge-delayed tallier. Votes are tallied when they are first recorded by this class.
     *
     * @see supervisor.model.tallier.ITallier#confirmed(sexpression.ASExpression)
     */
    public void confirmed(ASExpression nonce) {
        throw new RuntimeException("EncryptedTallierWithNIZKs.confirmed NOT IMPLEMENTED");
    }

    /**
     * This will decrypt and return the final sum at the end of the election.
     *
     * @see ITallier#getReport()
     */
    @SuppressWarnings("unchecked")
    public Map<String, BigInteger> getReport() {
        /* Ensure the private key is still valid before decryption */
        _finalPrivateKey = AdderKeyManipulator.generateFinalPrivateKey(_publicKey, _privateKey);

        /* this map will house the final results after they've been decrypted */
        Map<String, BigInteger> report = new HashMap<>();

        /* For each race group (analogous to each race), decrypt the sums */
        for(String group : _results.keySet()){

            /* Here our races are represented as "Elections", a class provided in the UConn encryption code */
            Election election = _results.get(group);

            /* From the election, we can get the sum of cipher texts */
            Vote cipherSum = election.sumVotes();

            /*
             * As per the Adder decryption process, partially decrypt the ciphertext to generate some necessary
             * information for the final decryption.
             */
            List<AdderInteger> partialSum = _finalPrivateKey.partialDecrypt(cipherSum);

            /* This is a LaGrange coefficient used as part of the decryption computations */
            AdderInteger coeff = AdderInteger.ZERO;

            /* Rely on the Adder election class to perform the final decryption of the election sums */
            List<AdderInteger> results = election.getFinalSum(partialSum, cipherSum, _finalPublicKey);

            /* Split off the results by candidate ID*/
            String[] ids = group.split(",");

            /* For each candidate in the race, put the decrypted sums in the results map */
            for(int i = 0; i < ids.length; i++)
                report.put(ids[i], results.get(i).bigintValue());
        }

        return report;
    }

    /**
     * @see supervisor.model.tallier.ITallier#recordVotes(byte[], sexpression.ASExpression)
     */
    public void recordVotes(byte[] ballotBytes, ASExpression nonce) {
        /* Verify that the keys used to encrypt this ballot are the same keys we used for the whole election */
        if(_finalPublicKey == null)
            _finalPublicKey = AdderKeyManipulator.generateFinalPublicKey(_publicKey);
        else{
            PublicKey copy = AdderKeyManipulator.generateFinalPublicKey(_publicKey);

            if(!_finalPublicKey.equals(copy))
                //throw new RuntimeException("Final public key changed!\n"+_finalPublicKey+"\n\n"+copy);
                Bugout.err("Final public key changed!\n"+_finalPublicKey+"\n\n"+copy);
        }

        /* Parse the ballot byte array */
        ASEInputStreamReader in = new ASEInputStreamReader(
                new ByteArrayInputStream(ballotBytes));

        try {
            /* read in the ballot to an SExpresssion */
            ASExpression sexp = in.read();

			 /* Check that the ballot is well-formed */
            ListExpression ballot = (ListExpression)sexp;

            /* Iterate through each of the races on the ballot */
            for(int i = 0; i < ballot.size(); i++){
                /* Retrieve the corresponding race information from this selection */
                ListExpression raceGroup = (ListExpression)ballot.get(i);

                /* The first entry in ballot is the vote itself */
                ListExpression voteE = (ListExpression)raceGroup.get(0);

                /* The second entry is the candidate identifier */
                ListExpression voteIdsE = (ListExpression)raceGroup.get(1);

                /* The third entry is a validity (TODO validity or integrity?) proof for the vote */
                ListExpression proofE = (ListExpression)raceGroup.get(2);

                /* The final entry is the public key that the vote was encrypted with */
                ListExpression publicKeyE = (ListExpression)raceGroup.get(3);

                /* Ensure that all of these fields are valid */
                confirmValid(voteE, voteIdsE, proofE, publicKeyE);

                /* Now that we know the vote is valid, read it in as an Adder Vote object */
                Vote vote = Vote.fromASE(voteE.get(1));
                List<ASExpression> voteIds = new ArrayList<>();

                /* Add the candidates to a list */
                for(int j = 0; j < voteIdsE.get(1).size(); j++)
                    voteIds.add(((ListExpression)voteIdsE.get(1)).get(j));

                /* Compute the validity proof */
                VoteProof voteProof = VoteProof.fromASE(proofE.get(1));

                /* Grab the supplied public key */
                PublicKey suppliedPublicKey = PublicKey.fromASE(publicKeyE.get(1));

                /* Confirm that the keys are the same */
                if(!(suppliedPublicKey.toString().trim().equals(_finalPublicKey.toString().trim()))){
                    Bugout.err("!!!Expected supplied final PublicKey to match generated\nSupplied: "+suppliedPublicKey+"\nGenerated: "+_finalPublicKey+"!!!");
                    return;
                }

                /* Confirm that the vote proof is valid */
                if(!voteProof.
                        verify(vote, _finalPublicKey, 0, 1)){
                    Bugout.err("!!!Ballot failed NIZK test!!!");
                    return;
                }

                /* Code these results as a subelection so the ciphers can be summed homomorphically */
                String subElectionId = makeId(voteIds);
                Election election = _results.get(subElectionId);

                /* If we haven't seen this specific election before, initialize it */
                if(election == null)
                    election = new Election(_publicKey, voteIds);

                /* This will homomorphically tally the vote */
                election.castVote(vote);

                /* Now save the result until we're ready to decrypt the totals */
                _results.put(subElectionId, election);
            }
        }catch(Exception e){
            Bugout.err("Malformed ballot received <"+e.getMessage()+">");
            Bugout.err("Rejected ballot:\n"+new String(ballotBytes));
        }
    }

    /**
     * Using NIZKs, imposes structure on our race format we haven't had before.
     *
     * @param voteIds a list of strings representing vote identifiers
     * @return a string representation of the list of voteIDs
     */
    protected String makeId(List<ASExpression> voteIds){
        String str = voteIds.get(0).toString();
        for(int i = 1; i < voteIds.size(); i++)
            str+=","+voteIds.get(i);

        return str;
    }

    /**
     * Confirms that the vote, voteIds, proof, and publicKey fields pulled out of a ballot are well-formed.
     */
    protected void confirmValid(ListExpression vote, ListExpression voteIds, ListExpression proof, ListExpression publicKey){
        if(!vote.get(0).toString().equals("vote"))
            throw new RuntimeException("Missing \"vote\"");

        if(!voteIds.get(0).toString().equals("vote-ids"))
            throw new RuntimeException("Missing \"vote-ids\"");

        if(!proof.get(0).toString().equals("vote-proof"))
            throw new RuntimeException("Missing \"proof\"");

        if(!publicKey.get(0).toString().equals("public-key"))
            throw new RuntimeException("Missing \"public-key\"");
    }
}