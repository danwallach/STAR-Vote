package crypto;

import java.util.Map;

/**
 * A framework for a selection in a Race (i.e. an individual part of a Ballot)
 * Created by Matthew Kindy II on 11/21/2014.
 */
public abstract class ARaceSelection {

    /** The title for the race corresponding to this race selection. Note that it will simply be a UID */
    protected String title;
    protected int size;

    protected ARaceSelection(String title, int size) {
        this.title = title;
        this.size = size;
    }

    public abstract Map<String,?> getRaceSelectionsMap();

    public abstract String getTitle();

}
