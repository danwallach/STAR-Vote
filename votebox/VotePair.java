package votebox;

import sexpression.ASExpression;
import sexpression.ListExpression;

/**
 * @author Matt Bernhard
 * @version 0.0.1
 *          Date: 8/1/13
 *
 * A wrapper for two S-Expressions, which will be used to pair short codes with votes for auditing purposes
 *
 */
public class VotePair {

    private ASExpression shortcode, vote;

    /* Just set the fields */
    public VotePair(ASExpression shortcode, ASExpression vote){
        this.shortcode = shortcode;
        this.vote = vote;
    }

    /* Getters */
    public ASExpression getShortcode(){
        return shortcode;
    }

    public ASExpression getVote() {
        return vote;
    }

    /**
     * Converts the @VotePair to an @ASExpression
     * @return the @VotePair represented as an @ASExpression (@ListExpression)
     */
    public ASExpression toSExp(){
        return new ListExpression(shortcode, vote);
    }

    /**
     * Creates a @VotePair from an @ASExpression
     * @param e the @ASExpression from which to create a @VotePair
     */
    public void fromSExp(ASExpression e){
        if(!(e instanceof ListExpression))
            throw new RuntimeException("Malformed VotePair expression!");

        shortcode = ((ListExpression)e).get(0);
        vote =  ((ListExpression)e).get(1);
    }
}
