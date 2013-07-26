package votebox;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * @author Mircea C. Berechet
 * Added to project: 07/11/2013
 */
public class PINAuthorizationGUI extends JFrame {

    /*
	 * CLASS CONSTANTS
	 */
    /* The width of the drawable/viewable space on the screen. */
    private static final int GUI_WIDTH = 300;
    /* The height of the drawable/viewable space on the screen. */
    private static final int GUI_HEIGHT = 100;
    /* The height of the title bar (the bar at the top of the GUI display window that contains the name, program, minimize button, exit button and others). */
    private static final int TITLE_BAR_HEIGHT = 50;
    /* The combined width of the side bars. The side bars are the bars on both sides of the display window. */
    private static final int SIDE_BARS_WIDTH = 25;

    /* Coordinates and sizes for the GUI Elements. */
	/* The upper-left corner of the label. */
    private static final int LABEL_ULX = 10;
    private static final int LABEL_ULY = 10;
    private static final int LABEL_WIDTH = 200;
    private static final int LABEL_HEIGHT = 14;

    /* The upper-left corner of the text field. */
    private static final int TEXTFIELD_ULX = 10;
    private static final int TEXTFIELD_ULY = 30;
    private static final int TEXTFIELD_WIDTH = GUI_WIDTH - 2 * TEXTFIELD_ULX;
    private static final int TEXTFIELD_HEIGHT = 20;

    /* The number of buttons present. */
    private static final int BUTTON_COUNT = 2;
    /* The upper-left corner of the label. The X-coordinate is computed based on the number of buttons.*/
    private static final int BUTTON_ULY = 56;
    /* The standard width of a button. */
    private static final int BUTTON_ABSOLUTE_WIDTH = 180;
    /* The current width of a button. */
    private static final int BUTTON_WIDTH = BUTTON_ABSOLUTE_WIDTH / BUTTON_COUNT;
    /* The standard height of a button. */
    private static final int BUTTON_HEIGHT = 23;
    /* The amount of space between buttons. */
    private static final int BUTTON_SPACING = 10;

    public JButton okButton;
    public JTextField pinTextField;
    private JLabel enterAuthorizationPinLabel;

    /**
     * Start displaying the GUI.
     */
    public void start ()
    {
        setVisible(true);
    }

    /**
     * Stop displaying the GUI.
     */
    public void stop ()
    {
        setVisible(false);
        dispose();
    }

    /**
     * Create the GUI and build its GUI Elements.
     * @param ulX the x-coordinate of the center of the GUI
     * @param ulY the y-coordinate of the center of the GUI
     */
    public PINAuthorizationGUI(int ulX, int ulY) {

        setTitle("Authorization Required");
        setPreferredSize(new Dimension(GUI_WIDTH, GUI_HEIGHT));
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setBounds(ulX - (GUI_WIDTH + SIDE_BARS_WIDTH) / 2, ulY - (GUI_HEIGHT + TITLE_BAR_HEIGHT) / 2, GUI_WIDTH + SIDE_BARS_WIDTH, GUI_HEIGHT + TITLE_BAR_HEIGHT);
        buildGUIElements();
    }

    /**
     * Builds the GUI Elements for this UI.
     */
    private void buildGUIElements()
    {
        /* Content Pane */
        JPanel contentPane = new JPanel();
        contentPane.setPreferredSize(new Dimension(GUI_WIDTH, GUI_HEIGHT));
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        /* Main Panel */
        JPanel mainPanel = new JPanel();
        contentPane.add(mainPanel, BorderLayout.CENTER);
        mainPanel.setLayout(null);

        /* Label */
        enterAuthorizationPinLabel = new JLabel("Enter Authorization PIN");
        enterAuthorizationPinLabel.setBounds(LABEL_ULX, LABEL_ULY, LABEL_WIDTH, LABEL_HEIGHT);
        mainPanel.add(enterAuthorizationPinLabel);

        /* Text Field */
        pinTextField = new JTextField(new PlainDocument() {
            private int limit = 4;
            public void insertString(int offs, String str, AttributeSet attr) throws BadLocationException {
                if(str == null)
                    return;
                if((getLength() + str.length()) <= this.limit) {
                    super.insertString(offs, str, attr);
                }
            }
        }, "", 10);
        pinTextField.setBounds(TEXTFIELD_ULX, TEXTFIELD_ULY, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT);
        mainPanel.add(pinTextField);
        pinTextField.setColumns(10);

        /* Buttons */
        okButton = new JButton("OK");
        okButton.setBounds(getButtonULX(BUTTON_COUNT, 1), BUTTON_ULY, BUTTON_WIDTH, BUTTON_HEIGHT);
        mainPanel.add(okButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(getButtonULX(BUTTON_COUNT, 2), BUTTON_ULY, BUTTON_WIDTH, BUTTON_HEIGHT);
        mainPanel.add(cancelButton);
    }

    /**
     * Set the text of the Label.
     */
    public void setLabelText (String text)
    {
        enterAuthorizationPinLabel.setText(text);
    }

    /**
     * Get the currently typed pin.
     */
    public String getPin ()
    {
        return pinTextField.getText();
    }

    /**
     * Get the upper-left corner x-coordinate for a button.
     */
    public int getButtonULX (int buttonCount, int buttonIndex)
    {
        switch (buttonCount)
        {
            case 1:
				/* There is only one button. */
                return (GUI_WIDTH - BUTTON_WIDTH) / 2;
            case 2:
				/* There are two buttons. */
                switch (buttonIndex)
                {
                    case 1:
						/* This is the first button. */
                        return (GUI_WIDTH - BUTTON_SPACING) / 2 - BUTTON_WIDTH;
                    case 2:
						/* This is the second button. */
                        return (GUI_WIDTH + BUTTON_SPACING) / 2;
                    default:
                        return -1;
                }
            default:
                return -1;
        }
    }

}
