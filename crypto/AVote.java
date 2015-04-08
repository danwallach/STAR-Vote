package crypto;

import sexpression.ASExpression;

import java.util.Map;

/**
 * Created by Matthew Kindy II on 11/21/2014.
 */
public abstract class AVote {

    /** The title for the race corresponding to this vote. Note that it will simply be a UID */
    protected String title;
    protected int size;

    protected AVote(String title, int size) {
        this.title = title;
        this.size = size;
    }

    public abstract Map<String,?> getVoteMap();

    public abstract ASExpression toASE();

}
