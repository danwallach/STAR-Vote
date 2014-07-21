package votebox.middle.view.widget;

import votebox.middle.view.IViewImage;

/**
 * @author Matt Bernhard
 * @version 0.0.1
 *          Date: 7/25/13
 */
public class FocusedLabelState extends ALabelState {
    /**
     * Singleton Design Pattern
     */
    public static FocusedLabelState Singleton = new FocusedLabelState();

    /**
     * Singleton Design Pattern
     */
    private FocusedLabelState() {
    }

    /**
     * When the button asks to be focused, do nothing, it already is.
     */
    public void focus(FocusableLabel context) { }

    /**
     * When the button asks to be unfocused, change the button's state to
     * default.
     */
    public void unfocus(FocusableLabel context) {
        context.setState(DefaultLabelState.Singleton);
        context.getUnfocusedEvent().notifyObservers();
    }

    /**
     * @see votebox.middle.view.widget.AButtonState#getImage(votebox.middle.view.widget.Button)
     */
    @Override
    public IViewImage getImage(FocusableLabel context) {
        return context.getFocusedImage();
    }
}
