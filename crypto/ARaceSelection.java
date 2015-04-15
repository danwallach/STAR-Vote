package crypto;

import sexpression.ASExpression;

import java.util.Map;

/**
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

    public abstract ASExpression toASE();

}
