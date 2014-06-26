package crypto.adder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.StringExpression;


/**
 \brief Ciphertext-vector vote.

 A vote consists of a vector of ciphertexts. Each ciphertext
 represents the encryption of a yes/no for one candidate.
 */
public class Vote {

    /** This vote's list of cipher texts, i.e. its encrypted selections */
    private List<ElgamalCiphertext> cipherList;

    /** The proof representing the validity of this vote */
    private VoteProof proof;

    /** List of the race ID's of the possible choices in this race */
    private List<ASExpression> choices;

    /**
     * Default constructor. Use when you want to load a vote from
     * a string.
     */
    public Vote() {

    }

    /**
     * Initializes a vote from a vector of ciphertexts.
     */
    public Vote(List<ElgamalCiphertext> cipherList, List<ASExpression> choices) {
        this.cipherList = cipherList;

        this.choices = choices;

        proof = new VoteProof();
    }

    /**
     * Initializes a vote from a vector of ciphertexts.
     */
    public Vote(List<ElgamalCiphertext> cipherList, List<ASExpression> choices, VoteProof proof) {
        this.cipherList = cipherList;
        this.choices = choices;
        this.proof = proof;
    }

    /**
     * Initializes a vote from a vector of ciphertexts.
     */
    private Vote(List<ElgamalCiphertext> cipherList, List<ASExpression> choices, List<MembershipProof> proofList) {
        this.cipherList = cipherList;
        this.choices = choices;

        proof = new VoteProof();
        proof.setProofList(proofList);
    }

    /**
     * Accessor function to retrieve the cipherList.
     * @return the vector of ciphertexts.
     */
    public List<ElgamalCiphertext> getCipherList() {
        return cipherList;
    }

    /**
     * @return the proof for this vote
     */
    public VoteProof getProof() {
        return proof;
    }

    public boolean verifyVoteProof(PublicKey publicKey, int min, int max){
        return proof.verify(this, publicKey, min, max);
    }

    /**
     * Multiplies this and another Vote component-wise and returns the result.
     * Note that this will result in a partial proof until computeSumProof() is
     * called after all Votes have been homomorphically tallied.
     *
     * @param otherVote     the Vote to multiply against
     * @return              the product of the two votes.
     */
    public Vote multiply(Vote otherVote) {

        List<ElgamalCiphertext> vec = new ArrayList<>();

        for (int i = 0; i < this.getCipherList().size(); i++) {
            ElgamalCiphertext ciphertext1 = this.getCipherList().get(i);
            ElgamalCiphertext ciphertext2 = otherVote.getCipherList().get(i);
            vec.add(ciphertext1.multiply(ciphertext2));
        }

        List<MembershipProof> otherList = proof.multiply(otherVote.proof);

        return new Vote(vec, choices, otherList);
    }

    /**
     * Computes the proof for a Vote which has been created as a homomorphic sum of
     * all the votes in the election. This is to allow for NIZK verification after
     * we've homomorphically tallied all of the votes. ONLY CALL THIS AFTER ALL VOTES
     * HAVE BEEN SUMMED IN AN ELECTION.
     *
     * @param numVotes          the total number of votes cast in this race
     * @param publicKey         the public key for the votes
     */
    public void computeSumProof(int numVotes, PublicKey publicKey){

        ElgamalCiphertext sumCipher = cipherList.get(0);

        /* Multiply all the ciphertexts together - needed for sumProof.compute() */
        for (int i=1; i<cipherList.size(); i++)
            sumCipher = cipherList.get(i).multiply(sumCipher);

        List<AdderInteger> totalDomain = new ArrayList<>(numVotes + 1);

        /* Create a new totalDomain (the range of 0 to the total number of votes cast in this race) */
        for (int i=0; i<numVotes+1; i++)
            totalDomain.add(new AdderInteger(i));

        /* Compute the sumProof so that we can make a proof for the homomorphically tallied votes */
        MembershipProof sumProof = new MembershipProof();
        sumProof.compute(sumCipher, publicKey, new AdderInteger(numVotes), totalDomain);

        /* Set the new proof */
        proof = new VoteProof(sumProof, proof.getProofList());
    }


   /**
    * Constructs a Vote from a String
    *
    * @param s      the string representation of a Vote.
    *
    * @see ElgamalCiphertext#fromString(String)
    */
    public static Vote fromString(String s) {

        StringTokenizer st = new StringTokenizer(s, " ");
        List<ElgamalCiphertext> cList = new ArrayList<>(25); // XXX: what size?

        while (st.hasMoreTokens()) {
            String s2 = st.nextToken();

            try {
                ElgamalCiphertext ciphertext = ElgamalCiphertext.fromString(s2);
                cList.add(ciphertext);
            }
            catch (InvalidElgamalCiphertextException iece) { throw new InvalidVoteException(iece.getMessage()); }
        }

        /* TODO Maybe implement this constructor properly */
        return new Vote(cList, null);
    }

    /**
     * Returns a string representation of the vote.  This is
     * represented a list of ElgamalCiphertext strings, separated by
     * whitespace.
     *
     * @return      the string representation of the vote.
     *
     * @see crypto.adder.ElgamalCiphertext#toString()
     */
    public String toString() {

        /* TODO update this to involve race ids */

        StringBuilder sb = new StringBuilder(4096);

        for (ElgamalCiphertext ciphertext : cipherList) {
            sb.append(ciphertext.toString());
            sb.append(" ");
        }

        return sb.toString().trim();
    }
 
    /**
     * Method for interop with VoteBox's S-Expression system.
     * 
     * @return      the S-Expression equivalent of this Vote
     *
     * @see ElgamalCiphertext#toASE()
     */
    public ListExpression toASE(){

    	List<ASExpression> cList = new ArrayList<>();

    	for(ElgamalCiphertext text : cipherList)
    		cList.add(text.toASE());

        ListExpression vote = new ListExpression(StringExpression.makeString("vote"), new ListExpression(cList));

        ListExpression choicesExp = new ListExpression(StringExpression.makeString("vote-ids"), new ListExpression(choices));
    	
    	return new ListExpression(vote, choicesExp, proof.toASE());
    }
    
    /**
     * Method for interop with VoteBox's S-Expression system.
     *
     * Expecting an ASE of the form ((vote [vote...])(vote-ids [ids...])(vote-proof [proofs...]))
     * 
     * @param ase       S-Expression representation of a Vote
     * @return          the Vote equivalent of ase
     *
     * @see ElgamalCiphertext#fromASE(sexpression.ASExpression)
     */
    public static Vote fromASE(ASExpression ase){

    	ListExpression exp = (ListExpression)ase;

        ListExpression voteExp = (ListExpression) exp.get(0);
        ListExpression choiceExp = (ListExpression) exp.get(1);
        ListExpression proofExp = (ListExpression) exp.get(2);

        List<ElgamalCiphertext> vote = new ArrayList<>();
        List<ASExpression> choices = new ArrayList<>();

        if(!(voteExp.get(0)).toString().equals("vote"))
            throw new RuntimeException("Not vote");

        ListExpression votesE = (ListExpression) voteExp.get(1);

        for(int i = 0; i < votesE.size(); i++)
            vote.add(ElgamalCiphertext.fromASE(votesE.get(i)));

        if(!(choiceExp.get(0)).toString().equals("vote-ids"))
            throw new RuntimeException("Not vote ids!");

        ListExpression choiceList = (ListExpression) choiceExp.get(1);

        for(ASExpression choice : choiceList)
            choices.add(choice);

    	return new Vote(vote, choices, VoteProof.fromASE(proofExp));
    }
}
