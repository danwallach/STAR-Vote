package crypto.adder;

import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.StringExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


/**
 * Proof of ciphertext vector vote validity.
 *
 * To show the validity of a vote, two statements must be proved:
 * - Each ciphertext in the vote encrypts a 0-1 value.
 * - The total number of 1s in the vote is within the required range.
 */
public class VoteProof {

    /** the list of proofs for each candidate-vote pairing */
    private List<MembershipProof> proofList;

    /** The proof for ths sum of candidate-vote pairings */
    private MembershipProof sumProof;

    /**
     * Default constructor.
     */
    public VoteProof() {
        proofList = new ArrayList<>();
    }

    public VoteProof(MembershipProof sumProof, List<MembershipProof> proofList) {
        this.sumProof = sumProof;
        this.proofList = proofList;
    }


    /**
     * @return the list of proofs for each candidate-vote pairing
     */
    public List<MembershipProof> getProofList() {
        return proofList;
    }

    /**
     * Computes a proof of vote validity.
     *
     * First, this function proves that each ciphertext encrypts a
     * 0-1 value.  To do this, a proof is generated for each
     * ciphertext in the vote, with a domain of \f$\{0, 1\}\f$.
     * Then, all of the ciphertexts are multiplied together and a
     * final proof is computed over the product of the
     * ciphertexts, with a domain of \f$\{\mathit{min},\ldots,
     * \mathit{max}\}\f$.
     *
     * @param vote          the vote the proof will be computed over.
     * @param pubKey        the public key used to encrypt the vote.
     * @param choices       the vector of \em true/ \em false plaintext choices.
     * @param min           the minimum number of candidates required to be selected.
     * @param max           the maximum number of candidates required to be selected.
     *
     * @see MembershipProof#compute(ElgamalCiphertext, PublicKey, AdderInteger, List)
     */
        public void compute(Vote vote, PublicKey pubKey, List<AdderInteger> choices, int min, int max) {

        List<ElgamalCiphertext> cipherList = vote.getCipherList();

        /* Create the domain of possible selection options */
        List<AdderInteger> cipherDomain = new ArrayList<>(2);
        cipherDomain.add(AdderInteger.ZERO);
        cipherDomain.add(AdderInteger.ONE);

        ElgamalCiphertext sumCipher = new ElgamalCiphertext(AdderInteger.ONE, AdderInteger.ONE, pubKey.getP());

        int numChoices = 0;
        int size = cipherList.size();
        this.proofList = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {

            MembershipProof proof = new MembershipProof();

            /* Get the encrypted vote and the plaintext */
            ElgamalCiphertext ciphertext = cipherList.get(i);
            AdderInteger choice = choices.get(i);

            /* Compute the NIZK and add it to the proofList */
            proof.compute(ciphertext, pubKey, choice, cipherDomain);
            this.proofList.add(proof);

            /* Multiply this ciphertext into the sum */
            sumCipher = sumCipher.multiply(ciphertext);

            /* Keep track of how many total selections have been made */
            if (choice.equals(AdderInteger.ONE))
                numChoices++;
        }

        List<AdderInteger> totalDomain = new ArrayList<>(max + 1);

        /* Create the domain of possible selection totals */
        for (int j = min; j <= max; j++)
            totalDomain.add(new AdderInteger(j));

        /* Compute the sumProof */
        this.sumProof = new MembershipProof();
        this.sumProof.compute(sumCipher, pubKey, new AdderInteger(numChoices), totalDomain);
    }

    /**
     * Verifies the proof.
     *
     * @param vote          the vote the proof is computed over.
     * @param pubKey        the public key used to encrypt the vote.
     * @param min           the minimum number of candidates required to be selected.
     * @param max           the maximum number of candidates required to be selected.
     * @return              \b true if the proof is valid, \b false otherwise.
     *
     * @see MembershipProof#verify(ElgamalCiphertext, PublicKey, java.util.List)
     */
    public boolean verify(Vote vote, PublicKey pubKey, int min, int max) {

        List<ElgamalCiphertext> cipherList = vote.getCipherList();
        List<AdderInteger> cipherDomain = new ArrayList<>(2);

        cipherDomain.add(AdderInteger.ZERO);
        cipherDomain.add(AdderInteger.ONE);

        ElgamalCiphertext sumCipher = new ElgamalCiphertext(AdderInteger.ONE, AdderInteger.ONE, pubKey.getP());
        int size = this.proofList.size();

        for (int i = 0; i < size; i++) {
            MembershipProof proof = this.proofList.get(i);
            ElgamalCiphertext ciphertext
                = cipherList.get(i);

            if (!proof.verify(ciphertext, pubKey, cipherDomain))
                return false;

            sumCipher = sumCipher.multiply(ciphertext);
        }

        List<AdderInteger> totalDomain = new ArrayList<>(max + 1);

        for (int j = min; j <= max; j++)
            totalDomain.add(new AdderInteger(j));

        return this.sumProof.verify(sumCipher, pubKey, totalDomain);

    }

    /**
     * A method used for constructing a new proof based on two previous proofs, for use
     * when homomorphically adding two Votes together.
     *
     * @see Vote#multiply(Vote)
     *
     * @param otherProof        the proof to multiply with this one
     * @return                  the concatenated membership proofs of this VoteProof
     */
    public List<MembershipProof> multiply(VoteProof otherProof) {

        List<MembershipProof> otherList = new ArrayList<>();

        otherList.addAll(proofList);
        otherList.addAll(otherProof.proofList);

        return otherList;
    }

    /**
     * Constructs a VoteProof from a string.
     *
     * @param s             the string representation of a proof.
     * @return              the VoteProof constructed from the string
     *
     * @see MembershipProof#fromString(String)
     */
    public static VoteProof fromString(String s) {
        StringTokenizer st = new StringTokenizer(s, " ");
        List<MembershipProof> pList
            = new ArrayList<>(25); // XXX: what size?
        MembershipProof sumProof = MembershipProof.fromString(st.nextToken());

        while (st.hasMoreTokens()) {
            String s2 = st.nextToken();
            MembershipProof proof = MembershipProof.fromString(s2);
            pList.add(proof);
        }

        return new VoteProof(sumProof, pList);
    }

    /**
     * Returns a string representation of the proof. The string is
     * in the form \f$\mathcal{P} \parallel P_1 \parallel \cdots
     * \parallel P_c\f$, where \f$\mathcal{P}\f$ is the proof that
     * the vote encrypts the proper number of choices, and
     * \f$P_i\f$ is the proof that the \f$i\f$ th candidate is a 0
     * or 1.
     *
     * @return          the string representation of the proof.
     *
     * @see MembershipProof::str
     */
    public String toString() {
        StringBuilder sb = new StringBuilder(4096);

        sb.append(sumProof.toString());

        for (MembershipProof proof : proofList) {
            sb.append(" ");
            sb.append(proof.toString());
        }

        return sb.toString();
    }
    
    /**
     * Method for interop with VoteBox's S-Expression system.
     * 
     * @return the S-Expression equivalent of this VoteProof
     */
    public ASExpression toASE(){
    	List<ASExpression> proofListL = new ArrayList<>();

    	for(MembershipProof proof : proofList)
    		proofListL.add(proof.toASE());
    	
    	return new ListExpression(StringExpression.makeString("vote-proof"), 
    			sumProof.toASE(), 
    			new ListExpression(proofListL));
    }
    
    /**
     * Method for interop with VoteBox's S-Expression system.
     * 
     * @param ase       S-Expression representation of a VoteProof
     * @return          the VoteProof equivalent of ase
     */
    public static VoteProof fromASE(ASExpression ase){
    	ListExpression exp = (ListExpression)ase;
    	if(!exp.get(0).toString().equals("vote-proof"))
    		throw new RuntimeException("Not vote-proof");
    	
    	MembershipProof sumProof = MembershipProof.fromASE(exp.get(1));
    	
    	List<MembershipProof> proofList = new ArrayList<>();
    	ListExpression proofListE = (ListExpression)exp.get(2);
    	
    	for(int i = 0; i < proofListE.size(); i++)
    		proofList.add(MembershipProof.fromASE(proofListE.get(i)));
    	
    	return new VoteProof(sumProof, proofList);
    }

    public void setProofList(List<MembershipProof> proofList) {
        this.proofList = proofList;
    }
}
