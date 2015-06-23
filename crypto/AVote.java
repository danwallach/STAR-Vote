package crypto;

import java.util.Map;

/**
 * Created by Matthew Kindy II on 11/21/2014.
 */
public abstract class AVote {

    /** The title for the race corresponding to this vote. Note that it will simply be a UID */
    private String title;

    protected AVote(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public abstract Map<String,?> getVoteMap();
    
    public abstract String toString();

}
