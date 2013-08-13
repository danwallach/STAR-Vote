package votebox.middle.view.widget;

import votebox.middle.Properties;
import votebox.middle.view.IView;

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
     * @param group      This is the group to which this ToggleButton will belong.
     * @param uid        Universal identifier of this ToggleButton.
     * @param properties Properties associated with this ToggleButon.
     */
    public WriteInToggleButton(ToggleButtonGroup group, String uid, Properties properties, IView view) {
        super(group, uid, properties);

        this.view = view;
    }


    public void select(){
        //launch GUI
        super.select();
    }

}
