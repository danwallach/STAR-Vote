package crypto;

import sexpression.ASExpression;

import java.util.Map;

/**
 * Created by Matthew Kindy II on 11/21/2014.
 */
public abstract class AVote {

    private String title;

    protected AVote(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public abstract Map getVoteMap();

    public abstract ASExpression toASE();

}
