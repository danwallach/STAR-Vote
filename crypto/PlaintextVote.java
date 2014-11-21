package crypto;

import sexpression.ASExpression;

import java.util.Map;

/**
 * Created by Matthew Kindy II on 11/19/2014.
 */
public class PlaintextVote {

    /** The title for the race corresponding to this vote. Note that it will simply be a UID */
    private String title;

    private Map<String, Integer> voteMap;

    public PlaintextVote(Map<String, Integer> voteMap, String title) {
        this.voteMap = voteMap;
        this.title = title;
    }

    public PlaintextVote(ASExpression vote){
        /* Parse this ASExpression */
    }

    public Map<String, Integer> getVoteMap(){
        return voteMap;
    }

    public String getTitle(){
        return title;
    }
}
