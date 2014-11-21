package crypto;

import sexpression.ASExpression;
import sexpression.ListExpression;

import java.util.Map;

/**
 * Created by Matthew Kindy II on 11/19/2014.
 */
public class PlaintextVote extends AVote {

    /** The title for the race corresponding to this vote. Note that it will simply be a UID */
    private String title;

    private Map<String, Integer> voteMap;

    public PlaintextVote(Map<String, Integer> voteMap, String title) {
        super(title);
        this.voteMap = voteMap;
    }

    public Map<String, Integer> getVoteMap(){
        return voteMap;
    }

    public String getTitle(){
        return title;
    }

    public ASExpression toASE(){
        return new ListExpression("");
    }

    public String toString() {
        return "";
    }
}
