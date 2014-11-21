package crypto.adder;

import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.StringExpression;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


/**
 \brief Ciphertext-vector vote.

 A vote consists of a vector of ciphertexts. Each ciphertext
 represents the encryption of a yes/no for one candidate.
 */
public class AdderVote implements Serializable {

    /** This vote's list of cipher texts, i.e. its encrypted selections */
    private List<ElgamalCiphertext> cipherList;

    /** The proof representing the validity of this vote */
    private VoteProof proof;

    /** List of the race ID's of the possible choices in this race */
    private List<ASExpression> choices;

    /** The title for the race corresponding to this vote. Note that it will simply be a UID */
    private String title;

    /**
     * Default constructor. Use when you want to load a vote from
     * a string.
     * @param cipherList
     * @param valueIds
     * @param proof
     * @param title
     */
    public AdderVote(List<ElgamalCiphertext> cipherList, List<ASExpression> valueIds, VoteProof proof, String title) {
        this(cipherList, valueIds, proof);
        this.title = title;

    }

    public AdderVote(AdderVote vote) {
       cipherList = vote.cipherList;
       proof = vote.proof;
       choices = vote.choices;
    }

    /**
     * Initializes a vote from a vector of ciphertexts.
     * TODO check this usage to see if we can kill this
     */
    public AdderVote(List<ElgamalCiphertext> cipherList, List<ASExpression> choices) {
        this.cipherList = cipherList;

        this.choices = choices;


    }

    /**
     * Initializes a vote from a vector of ciphertexts.\
     * TODO kill usages of this without title
     */
    public AdderVote(List<ElgamalCiphertext> cipherList, List<ASExpression> choices, VoteProof proof) {
        this.cipherList = cipherList;
        this.choices = choices;
        this.proof = proof;
    }

    /**
     * Initializes a vote from a vector of ciphertexts.
     */
    private AdderVote(List<ElgamalCiphertext> cipherList, List<ASExpression> choices, List<MembershipProof> proofList, String title) {
        this.cipherList = cipherList;
        this.choices = choices;
        this.title = title;

        proof = new VoteProof();
        proof.setProofList(proofList);
    }


    public List<ASExpression> getChoices() { return choices; }

    /**
     * Accessor function to retrieve the cipherList.
     * @return the vector of ciphertexts.
     */
    public List<ElgamalCiphertext> getCipherList() {
        return cipherList;
    }

    public String getRaceTitle() {
        return title;
    }

    public void setRaceTitle(String title) {
        this.title = title;
    }

    /**
     *
     * @param publicKey
     * @param min
     * @param max
     * @return
     */
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
    public AdderVote multiply(AdderVote otherVote) {

        List<ElgamalCiphertext> vec = new ArrayList<>();

        for (int i = 0; i < this.getCipherList().size(); i++) {

            ElgamalCiphertext ciphertext1 = this.getCipherList().get(i);
            ElgamalCiphertext ciphertext2 = otherVote.getCipherList().get(i);

            vec.add(ciphertext1.multiply(ciphertext2));
        }

        List<MembershipProof> otherList = proof.multiply(otherVote.proof);

        return new AdderVote(vec, choices, otherList, otherVote.title);
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

        /* Create a multiplicative identity */
        ElgamalCiphertext sumCipher = new ElgamalCiphertext(AdderInteger.ONE, AdderInteger.ONE, publicKey.getP());

        System.out.println("Cipherlist size in computeSumProof: " + cipherList.size());

        /* Multiply all the ciphertexts together - needed for sumProof.compute() */
        for (ElgamalCiphertext ciphertext : cipherList)
            sumCipher = ciphertext.multiply(sumCipher);

        List<AdderInteger> totalDomain = new ArrayList<>(numVotes + 1);

        /* Create a new totalDomain (the range of 0 to the total number of votes cast in this race) */
        for (int i=0; i<=numVotes; i++)
            totalDomain.add(new AdderInteger(i));

        /* Compute the sumProof so that we can make a proof for the homomorphically tallied votes */
        MembershipProof sumProof = new MembershipProof();
        sumProof.compute(sumCipher, publicKey, new AdderInteger(numVotes), totalDomain);


        /* Todo check that compute and verify handle values greater than 1 */
        System.out.println("In Vote.computeSumProof() -- verifying the sumProof post calculation: " + sumProof.verify(sumCipher, publicKey, totalDomain));

        proof = new VoteProof(sumProof, new ArrayList<MembershipProof>());
        //return sumProof;
    }


   /**
    * Constructs a Vote from a String
    *
    * @param s      the string representation of a Vote.
    *
    * @see ElgamalCiphertext#fromString(String)
    */
    public static AdderVote fromString(String s) {

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

        /* TODO Maybe implement this constructor properly (maybe w/o null?)*/
        return new AdderVote(cList, null);
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
     * Method for interop with VoteBox's S-Expression system. Creates ListExpressions of the form
     * ((vote [vote]) (vote-ids ([id1], [id2], ...)) (proof [proof]))
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

        System.out.println("Title: " + title);
        ListExpression titleExp = new ListExpression("title", title);

        ASExpression proofExp;

        if(proof != null)
            proofExp = proof.toASE();
        else
            proofExp = ListExpression.EMPTY;
    	
    	return new ListExpression(vote, choicesExp, proofExp, titleExp);
    }
    
    /**
     * Method for interop with VoteBox's S-Expression system.
     *
     * Expecting an ASE of the form ((vote [vote...])(vote-ids [ids...])(vote-proof [proofs...](title [title])
     * 
     * @param ase       S-Expression representation of a Vote
     * @return          the Vote equivalent of ase
     *
     * @see ElgamalCiphertext#fromASE(sexpression.ASExpression)
     */
    public static AdderVote fromASE(ASExpression ase){

    	ListExpression exp = (ListExpression)ase;

        ListExpression voteExp = (ListExpression) exp.get(0);
        ListExpression choiceExp = (ListExpression) exp.get(1);
        ListExpression proofExp = (ListExpression) exp.get(2);
        ListExpression titleExp = (ListExpression) exp.get(3);

        List<ElgamalCiphertext> vote = new ArrayList<>();
        List<ASExpression> choices = new ArrayList<>();

        if(!(voteExp.get(0)).toString().equals("vote"))
            throw new RuntimeException("Not vote");

        ListExpression votesE = (ListExpression) voteExp.get(1);

        for(int i = 0; i < votesE.size(); i++)
            vote.add(ElgamalCiphertext.fromASE(votesE.get(i)));

        if(!(choiceExp.get(0)).toString().equals("vote-ids"))
            throw new RuntimeException("Not vote ids!");

        if(!(titleExp.get(0)).toString().equals("title"))
            throw new RuntimeException("No title!");

        ListExpression choiceList = (ListExpression) choiceExp.get(1);

        for(ASExpression choice : choiceList)
            choices.add(choice);

        VoteProof proof;
        if(proofExp.equals(ListExpression.EMPTY))
            proof = null;
        else
            proof = VoteProof.fromASE(proofExp);

    	return new AdderVote(vote, choices, proof, titleExp.get(1).toString());
    }

}
