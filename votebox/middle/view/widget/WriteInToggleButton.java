package votebox.middle.view.widget;

import votebox.middle.Properties;
import votebox.middle.view.IView;
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


    public void select(){
        /* Launch GUI. */
        // TODO How do we get the type of the race?
        WriteInCandidateSimpleGUI writeInGUI = new WriteInCandidateSimpleGUI(680, 384, getUniqueID(), "Regular", view, this);
        writeInGUI.start();
        System.out.println("Now selecting the toggle button");
        super.select();
    }

    public void guiStopped(String primaryCandidateName, String secondaryCandidateName)
    {
        //TODO Use the names for something (rendering and creating events for the Supervisor).
        super.select();
    }

}
