package votebox.middle.view.widget;

import votebox.middle.Properties;
import votebox.middle.view.IView;
import votebox.middle.view.IViewImage;
import votebox.middle.writein.WriteInCandidateSimpleGUI;

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
     * @param group      This is the group to which this WriteInToggleButton will belong.
     * @param uid        Universal identifier of this WriteInToggleButton.
     * @param properties Properties associated with this WriteInToggleButton.
     */
    public WriteInToggleButton(ToggleButtonGroup group, String uid, Properties properties, IView view) {
        super(group, uid, properties);

        this.view = view;
    }


    /**
     * This is the getter for _selectedImage
     *
     * @return _selectedImage
     */
    public IViewImage getSelectedImage() {
        //if (_selectedImage == null) {
        System.out.println("> ToggleButton is attempting to open image with UID: " + getUniqueID() + " by passing: " + _vars.getBallotPath() + " | " + getUniqueID() +  "_selected | " + _viewManager.getSize() + " | " + _viewManager.getLanguage());
        _selectedImage = _factory.makeImage( imagePath( _vars,
                getUniqueID() + "_selected", _viewManager.getSize(),
                _viewManager.getLanguage() ), true);
        //}
        return _selectedImage;
    }

    /**
     * This is the getter for _focusedSelectedImage
     *
     * @return _focusedSelectedImage
     */
    public IViewImage getFocusedSelectedImage() {
        //if (_focusedSelectedImage == null) {
        _focusedSelectedImage = _factory.makeImage( imagePath( _vars,
                getUniqueID() + "_focusedSelected", _viewManager.getSize(),
                _viewManager.getLanguage() ), false);
        //}
        return _focusedSelectedImage;
    }

    /**
     * @see FocusableLabel#getReviewImage()
     */
    @Override
    public IViewImage getReviewImage() {
//        if (_reviewImage == null) {
        //System.out.println("> ToggleButton is attempting to open image with UID: " + getUniqueID() + " by passing: " + _vars.getBallotPath() + " | " + getUniqueID() + "_review" + " | " + _viewManager.getSize() + " | " + _viewManager.getLanguage());
        _reviewImage = _factory.makeImage( imageToggleButtonPath( _vars, getUniqueID() + "_review", _viewManager.getLanguage() ), false);
//        }
        return _reviewImage;
    }

    public void select(){
        super.select();
    }



}
