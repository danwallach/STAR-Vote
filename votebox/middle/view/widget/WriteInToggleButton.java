package votebox.middle.view.widget;

import votebox.middle.Properties;
import votebox.middle.view.IView;
import votebox.middle.view.IViewImage;

/**
 * @author Matt Bernhard
 * @version 0.0.1
 *          Date: 8/13/13
 *
 * This class is a specific kind of ToggleButton that will launch a GUI specifically for writing
 * in candidates. It is functionally identical to a ToggleButton, but when it's selected it triggers
 * a GUI in addition to toggling.
 */
public class WriteInToggleButton extends ToggleButton {

    IView view;

    /**
     * This is the public constructor for ToggleButton. It invokes super.
     *
     * @param group         the group to which this WriteInToggleButton will belong.
     * @param uid           universal identifier of this WriteInToggleButton.
     * @param properties    properties associated with this WriteInToggleButton.
     */
    public WriteInToggleButton(ToggleButtonGroup group, String uid, Properties properties, IView view) {
        super(group, uid, properties);
        this.view = view;
    }


    /**
     * This is the getter for _selectedImage
     *
     * @return      _selectedImage
     */
    public IViewImage getSelectedImage() {

        String imgPath = imagePath(_vars, getUniqueID() + "_selected", _viewManager.getSize(), _viewManager.getLanguage());
        _selectedImage = _factory.makeImage(imgPath, true);

        return _selectedImage;
    }

    /**
     * This is the getter for _focusedSelectedImage
     *
     * @return      _focusedSelectedImage
     */
    public IViewImage getFocusedSelectedImage() {

        String imgPath = imagePath(_vars, getUniqueID() + "_focusedSelected", _viewManager.getSize(), _viewManager.getLanguage());
        _focusedSelectedImage = _factory.makeImage(imgPath, false);

        return _focusedSelectedImage;
    }

    /**
     * @see FocusableLabel#getReviewImage()
     */
    @Override
    public IViewImage getReviewImage() {

        String imgPath = imageToggleButtonPath(_vars, getUniqueID() + "_review", _viewManager.getLanguage());
        _reviewImage = _factory.makeImage(imgPath, false);

        return _reviewImage;
    }

    public void select(){
        super.select();
    }



}
