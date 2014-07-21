package printer;

import java.awt.Image;

/**
 * @author Mircea Berechet
 * @version 1.0
 */

/**
 * This class manages a mapping of a label and an image. These should
 * represent the label and image, respectively, for the race titles.
 */
public class RaceTitlePair {

    /* The ID of the image file. */
    private final String _label;

    /* Image that corresponds to the label above. */
    private final Image _image;

    /**
     * Constructor
     *
     * @param label     the label for the race title
     * @param image     the image for the race title
     */
    public RaceTitlePair (String label, Image image) {
        _label = label;
        _image = image;
    }

    /* Getters */
    public String getLabel()
    {
        return _label;
    }

    public Image getImage()
    {
        return _image;
    }

    /**
     * ... but no setters. Since this class is supposed to emulate a tuple,
     * its fields should should be immutable.
     */
}
