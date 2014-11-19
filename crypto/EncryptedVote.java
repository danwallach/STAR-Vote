package crypto;

import java.util.List;

/**
 * Created by Matthew Kindy II on 11/19/2014.
 */
public class EncryptedVote {

    /** This vote's list of cipher texts, i.e. its encrypted selections */
    private List<ICiphertext> cipherList;

    /** The proof representing the validity of this vote */
    private IProof proof;

    /** List of the race ID's of the possible choices in this race */
    private List<String> choices;

    /** The title for the race corresponding to this vote. Note that it will simply be a UID */
    private String title;
}
