package votebox.middle.writein;

import votebox.middle.view.IView;
import votebox.middle.view.widget.WriteInToggleButton;

import javax.swing.*;
import java.awt.*;

/**
 * @author Mircea C. Berechet
 * @version 0.0.1
 * Added to STAR-Vote: 8/19/13
 *
 * This class represents a simple GUI that enables the voter to type in the name of a candidate.
 * The race that contains a trigger which starts this GUI must contain one write-in option.
 * The class is only ever created by an instance of the WriteInToggleButton class. No other types of
 * objects should instantiate WriteInCandidateSimpleGUI objects.
 */
public class WriteInCandidateSimpleGUI {

    /* The width of the drawable/viewable space on the screen. */
    private static final int GUI_WIDTH = 800;
    /* The height of the drawable/viewable space on the screen. */
    private static final int REGULAR_GUI_HEIGHT = 225;
    private static final int PRESIDENTIAL_GUI_HEIGHT = 365;

    /* STAR-Vote colors. */
    private static final Color STAR_VOTE_BLUE = new Color (48, 149, 242);
    private static final Color STAR_VOTE_PINK = Color.PINK;

    /* The UID of the write-in candidate whose name will be entered in this GUI prompt. */
    private String CANDIDATE_UID;
    /* The type of the write-in candidate. */
    private String CANDIDATE_TYPE;

    /* The main view, on which to draw Drawable elements. */
    IView mainView;
    /* The parent of this GUI. When stop is called, the GUI will call the guiStopped() method on the parent. */
    WriteInToggleButton parent;
    /* The main panel used by this GUI. */
    JPanel mainPanel;


    /**
     * Start displaying the GUI.
     */
    public void start ()
    {
        mainPanel.setVisible(true);
    }

    /**
     * Stop displaying the GUI.
     */
    public void stop ()
    {
//        parent.guiStopped("Name1", CANDIDATE_TYPE.equals("Presidential") ? "Name2" : "");
        mainPanel.setVisible(false);
        mainPanel.setEnabled(false);

    }

    /**
     * Constructor for the WriteInCandidateSimpleGUI.
     * Creates the GUI and builds its GUI Elements.
     * @param cX the x-coordinate of the center of the GUI
     * @param cY the y-coordinate of the center of the GUI
     * @param uid the UID of the candidate
     * @param guiType the Type of GUI to start (Regular or Presidential)
     */
    public WriteInCandidateSimpleGUI (int cX, int cY, String uid, String guiType, IView view, WriteInToggleButton parentObject)
    {
        // TODO Create the GUI
        // Set the UID.
        CANDIDATE_UID = uid;
        // Set the TYPE.
        CANDIDATE_TYPE = guiType;
        // Set the main view.
        mainView = view;
        // Set the parent.
        parent = parentObject;
        // Build GUI Elements.
        buildGUIElements(cX, cY);
    }

    /**
     * Builds all GUI Elements of the Write-In-Candidate GUI.
     */
    private void buildGUIElements (int centerX, int centerY)
    {
        /* Create the main panel. */
        mainPanel = new JPanel();

        /* Set position properties. */
        // Set the appropriate height for the GUI, based on the type of the candidate.
        int GUI_HEIGHT = CANDIDATE_TYPE.equals("Regular") ? REGULAR_GUI_HEIGHT : PRESIDENTIAL_GUI_HEIGHT;
        mainPanel.setBounds(centerX - GUI_WIDTH / 2, centerY - GUI_HEIGHT / 2, GUI_WIDTH, GUI_HEIGHT);
        //mainView.draw(mainPanel); //FIXME Need to find a way to draw a JPanel (or at least 2 text fields) on the view. It is currently impossible, as they are not IDrawables.
    }
}
