package crypto;

import sexpression.ASExpression;

import java.util.Map;

/**
 * Created by Matthew Kindy II on 11/19/2014.
 */
public class PlaintextVote {

    /** The title for the race corresponding to this vote. Note that it will simply be a UID */
    private String title;

    Map<String, Integer> vote;

    public PlaintextVote(Map<String, Integer> vote, String title) {
        this.vote = vote;
        this.title = title;
    }

    public PlaintextVote(ASExpression vote){
        /* Parse this ASExpression */
    }

    public Map<String, Integer> getVoteMap(){
        return vote;
    }

    public String getTitle(){
        return title;
    }
}
