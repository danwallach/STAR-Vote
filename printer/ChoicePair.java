package printer;

/**
 * @author Mircea Berechet
 * @version 1.0
 */

/**
 * This class manages a mapping of a label and a flag, where the
 * label represents an ID of an image file and the flag represents
 * whether or not the candidate(s) to which the image file corresponds
 * was selected by the voter.
 */
public class ChoicePair {

    /* The ID of the image file. */
    private final String _label;
    /* Flag that indicates the selected status ('selected' or 'not selected') */
    private final Boolean _selected;


    public ChoicePair (String label, Integer status)
    {
        _label = label;
        if (status.intValue() == 1)
            _selected = true;
        else
            _selected = false;
    }

    /* Getters */
    public String getLabel()
    {
        return _label;
    }
    public int getStatus()
    {
        if (_selected)
            return 1;
        return 0;
    }

    /**
     * ... but no setters. Since this class is supposed to emulate a tuple,
     * its fields should should be immutable.
     */
}
