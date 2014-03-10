package votebox.middle.writein;

import votebox.VoteBox;
import votebox.middle.ballot.Card;
import votebox.middle.ballot.CardException;
import votebox.middle.view.AWTView;
import votebox.middle.view.IView;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

/**
 * @author Mircea C. Berechet
 * @version 0.0.1
 * Added to STAR-Vote: 8/21/13
 */
public class WriteInCardGUI extends Panel{
    public static final String SLASH = System.getProperty("file.separator");
    //Current working directory
    File path = new File(System.getProperty("user.dir"));
    public static final String pathToImages = System.getProperty("user.dir") + SLASH + "tmp" + SLASH + "ballots" + SLASH + "ballot" + SLASH + "data" + SLASH + "media" + SLASH;
    /* The standard size of the character images. */
    public static final int IMAGE_STANDARD_WIDTH = 14;
    public static final int IMAGE_STANDARD_HEIGHT = 14;
    /* The size of the canvas. */
    public static final int CANVAS_WIDTH = 700;
    public static final int CANVAS_HEIGHT = 112;
    /* The location of the upper left corner of the next image to be drawn. */
    public static int nextUpperLeftX = 0;
    public static int nextUpperLeftY = 0;


    /* The width of the drawable/viewable space on the screen. */
    private static final int GUI_WIDTH = 1600;//1366;
    /* The height of the drawable/viewable space on the screen. */
    private static final int GUI_HEIGHT = 900;//710;
    /* The height of the title bar (the bar at the top of the GUI display window that contains the name, program, minimize button, exit button and others). */
    private static final int TITLE_BAR_HEIGHT = 50;
    /* The combined width of the side bars. The side bars are the bars on both sides of the display window. */
    private static final int SIDE_BARS_WIDTH = 25;
    /* The horizontal offset of items in a panel, from the left size of the panel. */
    @SuppressWarnings("unused")
    private static final int PANEL_CONTENTS_X_OFFSET = 5;
    /* The vertical offset of items in a panel, from the top of the panel. */
    private static final int PANEL_CONTENTS_Y_OFFSET = 5;

    /* The width of the title panel of the editor. */
    private static final int NAME_PANEL_WIDTH = GUI_WIDTH - SIDE_BARS_WIDTH;
    /* The height of the title panel of the editor. */
    private static final int NAME_PANEL_HEIGHT = (GUI_HEIGHT - TITLE_BAR_HEIGHT) / 2 - PANEL_CONTENTS_Y_OFFSET;
    /* The width of the bottom panel of the editor. */
    private static final int KEYBOARD_PANEL_WIDTH = GUI_WIDTH - SIDE_BARS_WIDTH;
    /* The height of the bottom panel of the editor. */
    private static final int KEYBOARD_PANEL_HEIGHT = (GUI_HEIGHT - TITLE_BAR_HEIGHT) / 2  - PANEL_CONTENTS_Y_OFFSET;

    /* COLOURS */
    private static final Color STAR_VOTE_BLUE = new Color (48, 149, 242);
    private static final Color STAR_VOTE_PINK = Color.PINK;
    private static final Color SELECTED_GREEN = new Color(0, 255, 150);

    /* GUI COLOURS */
    private static final Color CONTENT_PANE_COLOR = Color.BLACK;
    private static final Color NAME_PANEL_COLOR = STAR_VOTE_PINK;
    private static final Color KEYBOARD_PANEL_COLOR = STAR_VOTE_BLUE;

    /* FONTS */
    private static final Font CANDIDATE_NAME_FONT = new Font("Tahoma", Font.PLAIN, 64);
    private static final Font DEFAULT_KEY_FONT = new Font("Tahoma", Font.PLAIN, 48);
    private static final Font ENTER_KEY_FONT = new Font("Tahoma", Font.PLAIN, 28);
    private static final Font BACKSPACE_KEY_FONT = new Font("Tahoma", Font.PLAIN, 28);
    private static final Font ARROW_KEY_FONT = new Font("Tahoma", Font.PLAIN, 32);


    /* NAMES */
    /* The number of columns to be displayed in the text fields. */
    private static final int NAMES_TEXT_FIELD_COLUMNS = 20;
    /* The standard width of the clear-selection button. */
    private static final int NAMES_CLEAR_BUTTON_WIDTH = 100;
    /* The standard height of the clear-selection button. */
    private static final int NAMES_CLEAR_BUTTON_HEIGHT = 80;


    /* KEYBOARD */
    /* The standard width of a keyboard button. */
    private static final int KEYBOARD_BUTTON_WIDTH = 100;
    /* The standard height of a keyboard button. */
    private static final int KEYBOARD_BUTTON_HEIGHT = 100;
    /* The upper-left corner of the upper-left button. */
    private static final int KEYBOARD_POSITION_UPPER_LEFT_X = 35;
    private static final int KEYBOARD_POSITION_UPPER_LEFT_Y = 5;
    /* The amount of space between two adjacent buttons. */
    private static final int KEYBOARD_BUTTON_SPACING_X = 5;
    private static final int KEYBOARD_BUTTON_SPACING_Y = 5;

    /* The elements on the name panel. */
    private JTextField primaryCandidateNameTextField;
    private JTextField secondaryCandidateNameTextField;
    private JPanel namePanel;
    private int primaryCandidateNameIndex;
    private int secondaryCandidateNameIndex;
    private Boolean primaryTextFieldSelected;
    private Boolean secondaryTextFieldSelected;

    /* The special buttons on the keyboard. */
    private JButton spaceButton;
    private JButton hyphenButton;
    private JButton apostropheButton;
    private JButton backspaceButton;
    private JButton leftButton;
    private JButton rightButton;
    private JButton clickButton;
    private JButton enterButton;


    private Boolean DONE;
    private Card parent;

    /* The UID of the write-in candidate whose name will be entered in this GUI prompt. */
    private String CANDIDATE_UID;
    /* The type of the write-in candidate. */
    private String CANDIDATE_TYPE;

    //this denotes whether the frame is done being constructed or not
    private boolean ready = false;




    /**
     * Start displaying the GUI.
     */
    public void start ()
    {
        while(!ready);
        setVisible(true);

        selectPrimaryTextField();
        parent.getParent().getViewAdapter().getView().getFrame().add(this);
        parent.getParent().getViewAdapter().getView().getFrame().pack();
        parent.getParent().getViewAdapter().getView().getFrame().setVisible(true);


    }

    /**
     * Stop displaying the GUI.
     */
    public void stop ()
    {
        System.out.println(">>>>" + primaryCandidateNameTextField.getText());
        parent.setWriteInValue(primaryCandidateNameTextField.getText(), CANDIDATE_TYPE.equals("Presidential") ? secondaryCandidateNameTextField.getText() : "");
        setVisible(false);
        DONE = true;
    }

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    JFrame frame = new JFrame();
                    WriteInCardGUI panel = new WriteInCardGUI(680, 384, "U0", "Presidential", null);
                    frame.add(panel);
                    panel.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the GUI and build its GUI Elements.
     * @param ulX the x-coordinate of the center of the GUI
     * @param ulY the y-coordinate of the center of the GUI
     */
    public WriteInCardGUI(int ulX, int ulY, String uid, String guiType, Card parent) {
        setBounds(ulX - GUI_WIDTH / 2, ulY - GUI_HEIGHT / 2, GUI_WIDTH, GUI_HEIGHT);

        this.CANDIDATE_UID = uid;
        this.CANDIDATE_TYPE = guiType;
        this.parent = parent;

        // Build GUI Elements.
        buildGUIElements();
    }

    private void buildGUIElements()
    {
        invalidate();
        setPreferredSize(new Dimension(GUI_WIDTH, GUI_HEIGHT));
        setLayout(new BorderLayout(0, 0));
        setBackground(CONTENT_PANE_COLOR);


        /**
         * NAME PANEL
         */
		/* Clear the selection flags. */
        primaryCandidateNameIndex = 0;
        secondaryCandidateNameIndex = 0;
        primaryTextFieldSelected = false;
        secondaryTextFieldSelected = false;

        namePanel = new JPanel();
        namePanel.setPreferredSize(new Dimension(NAME_PANEL_WIDTH, NAME_PANEL_HEIGHT));
        namePanel.setBackground(NAME_PANEL_COLOR);
        add(namePanel, BorderLayout.NORTH);

		/* Element separator. */

        JLabel titleLabel = new JLabel("WRITE-IN PAGE");
        titleLabel.setFont(CANDIDATE_NAME_FONT);
        namePanel.add(titleLabel);
        JSeparator candidateNameSeparator = new JSeparator();
        candidateNameSeparator.setPreferredSize(new Dimension(NAME_PANEL_WIDTH - 20, 1));
        namePanel.add(candidateNameSeparator);
        primaryCandidateNameTextField = new JTextField();
        primaryCandidateNameTextField.setFont(CANDIDATE_NAME_FONT);
        namePanel.add(primaryCandidateNameTextField);
        primaryCandidateNameTextField.setColumns(NAMES_TEXT_FIELD_COLUMNS);

        JButton primaryCandidateClearButton = new JButton("CLEAR");
        primaryCandidateClearButton.setPreferredSize(new Dimension(NAMES_CLEAR_BUTTON_WIDTH, NAMES_CLEAR_BUTTON_HEIGHT));
        namePanel.add(primaryCandidateClearButton);

		/* Element separator. */
        candidateNameSeparator = new JSeparator();
        candidateNameSeparator.setPreferredSize(new Dimension(NAME_PANEL_WIDTH - 20, 1));
        namePanel.add(candidateNameSeparator);

		/* Secondary candidate name text field and clear button. */
        secondaryCandidateNameTextField = new JTextField();
        secondaryCandidateNameTextField.setFont(CANDIDATE_NAME_FONT);
        if(CANDIDATE_TYPE.equals("PRESIDENTIAL"))
            namePanel.add(secondaryCandidateNameTextField);
        secondaryCandidateNameTextField.setColumns(NAMES_TEXT_FIELD_COLUMNS);

        JButton secondaryCandidateClearButton = new JButton("CLEAR");
        secondaryCandidateClearButton.setPreferredSize(new Dimension(NAMES_CLEAR_BUTTON_WIDTH, NAMES_CLEAR_BUTTON_HEIGHT));
        if(CANDIDATE_TYPE.equals("PRESIDENTIAL"))
            namePanel.add(secondaryCandidateClearButton);


        /**
         * KEYBOARD PANEL
         */
        JPanel keyboardPanel = new JPanel();
        keyboardPanel.setPreferredSize(new Dimension(KEYBOARD_PANEL_WIDTH, KEYBOARD_PANEL_HEIGHT));
        keyboardPanel.setBackground(KEYBOARD_PANEL_COLOR);
        add(keyboardPanel, BorderLayout.SOUTH);
        keyboardPanel.setLayout(null);

        /* Buttons on the keyboard. */
        JButton aButton;
        JButton bButton;
        JButton cButton;
        JButton dButton;
        JButton eButton;
        JButton fButton;
        JButton gButton;
        JButton hButton;
        JButton iButton;
        JButton jButton;
        JButton kButton;
        JButton lButton;
        JButton mButton;
        JButton nButton;
        JButton oButton;
        JButton pButton;
        JButton qButton;
        JButton rButton;
        JButton sButton;
        JButton tButton;
        JButton uButton;
        JButton vButton;
        JButton wButton;
        JButton xButton;
        JButton yButton;
        JButton zButton;

		/* Add the buttons to the keyboard: */
		/* 1. Instantiate the buttons. */
		/* 2. Position the buttons. */
		/* 3. Add the buttons to the container. */

		/* ROW 1 */
        ArrayList<JButton> firstRow = new ArrayList<JButton> ();
		/*qButton = new JButton("Q") {
			@Override
		    public void paintComponent (Graphics g) {
		        super.paintComponent (g);
		        g.drawImage (new ImageIcon(pathToImages + "Key_Q.png").getImage(), 0, 0, getWidth (), getHeight (), null);
		    }
		};*/
        qButton = new JButton("Q");qButton.setFont(DEFAULT_KEY_FONT);firstRow.add(qButton);
        wButton = new JButton("W");wButton.setFont(DEFAULT_KEY_FONT);firstRow.add(wButton);
        eButton = new JButton("E");eButton.setFont(DEFAULT_KEY_FONT);firstRow.add(eButton);
        rButton = new JButton("R");rButton.setFont(DEFAULT_KEY_FONT);firstRow.add(rButton);
        tButton = new JButton("T");tButton.setFont(DEFAULT_KEY_FONT);firstRow.add(tButton);
        yButton = new JButton("Y");yButton.setFont(DEFAULT_KEY_FONT);firstRow.add(yButton);
        uButton = new JButton("U");uButton.setFont(DEFAULT_KEY_FONT);firstRow.add(uButton);
        iButton = new JButton("I");iButton.setFont(DEFAULT_KEY_FONT);firstRow.add(iButton);
        oButton = new JButton("O");oButton.setFont(DEFAULT_KEY_FONT);firstRow.add(oButton);
        pButton = new JButton("P");pButton.setFont(DEFAULT_KEY_FONT);firstRow.add(pButton);
		/* ROW 2 */
        ArrayList<JButton> secondRow = new ArrayList<JButton> ();
        aButton = new JButton("A");aButton.setFont(DEFAULT_KEY_FONT);secondRow.add(aButton);
        sButton = new JButton("S");sButton.setFont(DEFAULT_KEY_FONT);secondRow.add(sButton);
        dButton = new JButton("D");dButton.setFont(DEFAULT_KEY_FONT);secondRow.add(dButton);
        fButton = new JButton("F");fButton.setFont(DEFAULT_KEY_FONT);secondRow.add(fButton);
        gButton = new JButton("G");gButton.setFont(DEFAULT_KEY_FONT);secondRow.add(gButton);
        hButton = new JButton("H");hButton.setFont(DEFAULT_KEY_FONT);secondRow.add(hButton);
        jButton = new JButton("J");jButton.setFont(DEFAULT_KEY_FONT);secondRow.add(jButton);
        kButton = new JButton("K");kButton.setFont(DEFAULT_KEY_FONT);secondRow.add(kButton);
        lButton = new JButton("L");lButton.setFont(DEFAULT_KEY_FONT);secondRow.add(lButton);
		/* ROW 3 */
        ArrayList<JButton> thirdRow = new ArrayList<JButton> ();
        zButton = new JButton("Z");zButton.setFont(DEFAULT_KEY_FONT);thirdRow.add(zButton);
        xButton = new JButton("X");xButton.setFont(DEFAULT_KEY_FONT);thirdRow.add(xButton);
        cButton = new JButton("C");cButton.setFont(DEFAULT_KEY_FONT);thirdRow.add(cButton);
        vButton = new JButton("V");vButton.setFont(DEFAULT_KEY_FONT);thirdRow.add(vButton);
        bButton = new JButton("B");bButton.setFont(DEFAULT_KEY_FONT);thirdRow.add(bButton);
        nButton = new JButton("N");nButton.setFont(DEFAULT_KEY_FONT);thirdRow.add(nButton);
        mButton = new JButton("M");mButton.setFont(DEFAULT_KEY_FONT);thirdRow.add(mButton);
		/* SPECIAL */
        spaceButton = new JButton("SPACE");spaceButton.setFont(DEFAULT_KEY_FONT);
        hyphenButton = new JButton("-");hyphenButton.setFont(DEFAULT_KEY_FONT);
        apostropheButton = new JButton("'");apostropheButton.setFont(DEFAULT_KEY_FONT);
        clickButton = new JButton("!");clickButton.setFont(DEFAULT_KEY_FONT);
        leftButton = new JButton("\u2190");leftButton.setFont(ARROW_KEY_FONT);
        rightButton = new JButton("\u2192");rightButton.setFont(ARROW_KEY_FONT);
        backspaceButton = new JButton("BACKSPACE");backspaceButton.setFont(BACKSPACE_KEY_FONT);
        enterButton = new JButton("ENTER");enterButton.setFont(ENTER_KEY_FONT);

		/* Position the buttons. */
        setPositionsForButtons(firstRow, secondRow, thirdRow);

		/* Add the buttons to the container. */
		/* ROW 1 */
        keyboardPanel.add(qButton);
        keyboardPanel.add(wButton);
        keyboardPanel.add(eButton);
        keyboardPanel.add(rButton);
        keyboardPanel.add(tButton);
        keyboardPanel.add(yButton);
        keyboardPanel.add(uButton);
        keyboardPanel.add(iButton);
        keyboardPanel.add(oButton);
        keyboardPanel.add(pButton);
		/* ROW 2 */
        keyboardPanel.add(aButton);
        keyboardPanel.add(sButton);
        keyboardPanel.add(dButton);
        keyboardPanel.add(fButton);
        keyboardPanel.add(gButton);
        keyboardPanel.add(hButton);
        keyboardPanel.add(jButton);
        keyboardPanel.add(kButton);
        keyboardPanel.add(lButton);
		/* ROW 3 */
        keyboardPanel.add(zButton);
        keyboardPanel.add(xButton);
        keyboardPanel.add(cButton);
        keyboardPanel.add(vButton);
        keyboardPanel.add(bButton);
        keyboardPanel.add(nButton);
        keyboardPanel.add(mButton);
		/* SPECIAL */
        keyboardPanel.add(spaceButton);
        keyboardPanel.add(hyphenButton);
        keyboardPanel.add(apostropheButton);
        keyboardPanel.add(clickButton);
        keyboardPanel.add(leftButton);
        keyboardPanel.add(rightButton);
        keyboardPanel.add(backspaceButton);
        keyboardPanel.add(enterButton);

		/* ACTION/EVENT LISTENERS. */

		/* If the clear buttons are pressed, clear the appropriate text field. */
        primaryCandidateClearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                primaryCandidateNameTextField.setText("");
                primaryCandidateNameIndex = 0;
            }
        });
        secondaryCandidateClearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                secondaryCandidateNameTextField.setText("");
                secondaryCandidateNameIndex = 0;
            }
        });

		/* Select the text field which the user focuses on. */
        primaryCandidateNameTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent arg0) {
                selectPrimaryTextField();
            }
        });
        secondaryCandidateNameTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent arg0) {
                selectSecondaryTextField();
            }
        });

		/* When a key is pressed, write the letter in the appropriate text field. */
        aButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processKey("A");
            }
        });
        bButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processKey("B");
            }
        });
        cButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processKey("C");
            }
        });
        dButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processKey("D");
            }
        });
        eButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processKey("E");
            }
        });
        fButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processKey("F");
            }
        });
        gButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processKey("G");
            }
        });
        hButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processKey("H");
            }
        });
        iButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processKey("I");
            }
        });
        jButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processKey("J");
            }
        });
        kButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processKey("K");
            }
        });
        lButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processKey("L");
            }
        });
        mButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processKey("M");
            }
        });
        nButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processKey("N");
            }
        });
        oButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processKey("O");
            }
        });
        pButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processKey("P");
            }
        });
        qButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processKey("Q");
            }
        });
        rButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processKey("R");
            }
        });
        sButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processKey("S");
            }
        });
        tButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processKey("T");
            }
        });
        uButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processKey("U");
            }
        });
        vButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processKey("V");
            }
        });
        wButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processKey("W");
            }
        });
        xButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processKey("X");
            }
        });
        yButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processKey("Y");
            }
        });
        zButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processKey("Z");
            }
        });
        spaceButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processKey(" ");
            }
        });
        hyphenButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processKey("-");
            }
        });
        apostropheButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processKey("'");
            }
        });
        clickButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processKey("!");
            }
        });
        leftButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processKey("LEFT");
            }
        });
        rightButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processKey("RIGHT");
            }
        });
        backspaceButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processKey("BACKSPACE");
            }
        });
        enterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
//                /* Clear the canvas panel. */
//                Graphics g = namePanel.getGraphics();
//                g.setColor(Color.WHITE);
//                g.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
//                nextUpperLeftX = 0;
//                nextUpperLeftY = 0;
//
//                        /* Render the candidate's name. */
//                BufferedImage canvas = renderCandidateName(primaryCandidateNameTextField.getText());
//
//                        /* Draw the canvas into the JPanel. */
//                g.drawImage(canvas, 0, 0, null);
//
//                        /* Trim the image. */
//                canvas = PrintImageUtils.trimImageVertically(canvas, false, Integer.MAX_VALUE); // Above
//                canvas = PrintImageUtils.trimImageVertically(canvas, true, Integer.MAX_VALUE);  // Below
//                canvas = PrintImageUtils.trimImageHorizontally(canvas, false, Integer.MAX_VALUE); // Left
//                canvas = PrintImageUtils.trimImageHorizontally(canvas, true, Integer.MAX_VALUE); // Right
//
//                        /* Save the image to a file. */
//                File file = new File(pathToImages, "result.png");
//                try {
//                    ImageIO.write(canvas, "png", file);
//                } catch (IOException ex) {
//                    System.out.println("Canvas image creation failed!");
//                    ex.printStackTrace();
//                }
//
                   /* Render the appropriate images. */
                if (CANDIDATE_TYPE.equals("Regular"))
                {
                    VoteBox.renderWriteInImages(CANDIDATE_UID, CANDIDATE_TYPE, primaryCandidateNameTextField.getText());
                }
                else
                {
                    VoteBox.renderWriteInImages(CANDIDATE_UID, CANDIDATE_TYPE, primaryCandidateNameTextField.getText(), secondaryCandidateNameTextField.getText());
                }

                stop();
            }
        });

        System.out.println("Done building the GUI!");
        ready = true;

    }

    /**
     * Sets positions for all the buttons on the keyboard.
     * @param row1 The first row of buttons.
     * @param row2 The second row of buttons.
     * @param row3 The third row of buttons.
     */
    public void setPositionsForButtons(ArrayList<JButton> row1, ArrayList<JButton> row2, ArrayList<JButton> row3)
    {
		/* The current coordinates of the upper left corner where a button should be placed. */
		/* Set the upper-left corner for Row 1. */
        int ulX = KEYBOARD_POSITION_UPPER_LEFT_X;
        int ulY = KEYBOARD_POSITION_UPPER_LEFT_Y;
        for (JButton button : row1)
        {
            button.setBounds(ulX, ulY, KEYBOARD_BUTTON_WIDTH, KEYBOARD_BUTTON_HEIGHT);
            ulX += KEYBOARD_BUTTON_WIDTH + KEYBOARD_BUTTON_SPACING_X;
        }

        int specialULX = ulX + 3 * KEYBOARD_BUTTON_SPACING_X;

		/* Set the upper-left corner for Row 2. */
        ulX = KEYBOARD_POSITION_UPPER_LEFT_X + KEYBOARD_BUTTON_WIDTH / 2;
        ulY += KEYBOARD_BUTTON_HEIGHT + KEYBOARD_BUTTON_SPACING_Y;
        for (JButton button : row2)
        {
            button.setBounds(ulX, ulY, KEYBOARD_BUTTON_WIDTH, KEYBOARD_BUTTON_HEIGHT);
            ulX += KEYBOARD_BUTTON_WIDTH + KEYBOARD_BUTTON_SPACING_X;
        }

		/* Set the upper-left corner for Row 3. */
        ulX = KEYBOARD_POSITION_UPPER_LEFT_X + KEYBOARD_BUTTON_WIDTH;
        ulY += KEYBOARD_BUTTON_HEIGHT + KEYBOARD_BUTTON_SPACING_Y;
        for (JButton button : row3)
        {
            button.setBounds(ulX, ulY, KEYBOARD_BUTTON_WIDTH, KEYBOARD_BUTTON_HEIGHT);
            ulX += KEYBOARD_BUTTON_WIDTH + KEYBOARD_BUTTON_SPACING_X;
        }

		/* Set the position of the space button. */
        ulX = KEYBOARD_POSITION_UPPER_LEFT_X + 2 * KEYBOARD_BUTTON_WIDTH + KEYBOARD_BUTTON_SPACING_X;
        ulY += KEYBOARD_BUTTON_HEIGHT + KEYBOARD_BUTTON_SPACING_Y;
        spaceButton.setBounds(ulX, ulY, 5 * KEYBOARD_BUTTON_WIDTH + 4 * KEYBOARD_BUTTON_SPACING_X, KEYBOARD_BUTTON_HEIGHT);

		/* Set the position of the arrow buttons. */
        ulX = NAME_PANEL_WIDTH - 2 * KEYBOARD_BUTTON_SPACING_X - 2 * KEYBOARD_BUTTON_WIDTH;
        leftButton.setBounds(ulX, ulY, KEYBOARD_BUTTON_WIDTH, KEYBOARD_BUTTON_HEIGHT);
        ulX += KEYBOARD_BUTTON_WIDTH + KEYBOARD_BUTTON_SPACING_X;
        rightButton.setBounds(ulX, ulY, KEYBOARD_BUTTON_WIDTH, KEYBOARD_BUTTON_HEIGHT);

		/* Set the position of the backspace button. */
        ulX = NAME_PANEL_WIDTH - KEYBOARD_BUTTON_SPACING_X - 2 * KEYBOARD_BUTTON_WIDTH;
        ulY = KEYBOARD_POSITION_UPPER_LEFT_Y;
        backspaceButton.setBounds(ulX, ulY, 2 * KEYBOARD_BUTTON_WIDTH, KEYBOARD_BUTTON_HEIGHT);

        /* Set the position for the enter button */
        ulX = NAME_PANEL_WIDTH - KEYBOARD_BUTTON_SPACING_X - 2 * KEYBOARD_BUTTON_WIDTH;
        ulY = KEYBOARD_POSITION_UPPER_LEFT_Y + KEYBOARD_BUTTON_HEIGHT;
        enterButton.setBounds(ulX, ulY, 2 * KEYBOARD_BUTTON_WIDTH, KEYBOARD_BUTTON_HEIGHT);

		/* Set the position of the special characters buttons. */
        ulY = KEYBOARD_POSITION_UPPER_LEFT_Y;
        hyphenButton.setBounds(specialULX, ulY, KEYBOARD_BUTTON_WIDTH, KEYBOARD_BUTTON_HEIGHT);
        ulY += KEYBOARD_BUTTON_HEIGHT + KEYBOARD_BUTTON_SPACING_Y;
        apostropheButton.setBounds(specialULX, ulY, KEYBOARD_BUTTON_WIDTH, KEYBOARD_BUTTON_HEIGHT);
        ulY += KEYBOARD_BUTTON_HEIGHT + KEYBOARD_BUTTON_SPACING_Y;
        clickButton.setBounds(specialULX, ulY, KEYBOARD_BUTTON_WIDTH, KEYBOARD_BUTTON_HEIGHT);

    }

    /**
     * Processed the letter (whose corresponding key on the keyboard was pressed) for the selected text field.
     * @param letter The letter to be added.
     */
    public void processKey(String letter)
    {
        if (primaryTextFieldSelected)
        {
            String tfText = primaryCandidateNameTextField.getText();
            if (letter.equals("BACKSPACE"))
            {
                if (primaryCandidateNameIndex > 0)
                {
                    primaryCandidateNameTextField.setText(tfText.substring(0, primaryCandidateNameIndex - 1) + tfText.substring(primaryCandidateNameIndex, tfText.length()));
                    primaryCandidateNameIndex--;
                }
            }
            else if (letter.equals("LEFT"))
            {
                primaryCandidateNameIndex = Math.max(primaryCandidateNameIndex - 1, 0);
            }
            else if (letter.equals("RIGHT"))
            {
                primaryCandidateNameIndex = Math.min(primaryCandidateNameIndex + 1, tfText.length());
            }
            else
            {
                primaryCandidateNameTextField.setText(tfText.substring(0, primaryCandidateNameIndex) + letter + tfText.substring(primaryCandidateNameIndex, tfText.length()));
                primaryCandidateNameIndex++;
            }
        }
        if (secondaryTextFieldSelected)
        {
            String tfText = secondaryCandidateNameTextField.getText();
            if (letter.equals("BACKSPACE"))
            {
                if (secondaryCandidateNameIndex > 0)
                {
                    secondaryCandidateNameTextField.setText(tfText.substring(0, secondaryCandidateNameIndex - 1) + tfText.substring(secondaryCandidateNameIndex, tfText.length()));
                    secondaryCandidateNameIndex--;
                }
            }
            else if (letter.equals("LEFT"))
            {
                secondaryCandidateNameIndex = Math.max(secondaryCandidateNameIndex - 1, 0);
            }
            else if (letter.equals("RIGHT"))
            {
                secondaryCandidateNameIndex = Math.min(secondaryCandidateNameIndex + 1, tfText.length());
            }
            else
            {
                secondaryCandidateNameTextField.setText(tfText.substring(0, secondaryCandidateNameIndex) + letter + tfText.substring(secondaryCandidateNameIndex, tfText.length()));
                secondaryCandidateNameIndex++;
            }
        }
    }

    /**
     * Selects the primary candidate's text field.
     */
    public void selectPrimaryTextField()
    {
        primaryTextFieldSelected = true;
        secondaryTextFieldSelected = false;
        primaryCandidateNameTextField.setBackground(SELECTED_GREEN);
        secondaryCandidateNameTextField.setBackground(Color.WHITE);
    }

    /**
     * Selects the secondary candidate's text field.
     */
    public void selectSecondaryTextField()
    {
        primaryTextFieldSelected = false;
        secondaryTextFieldSelected = true;
        primaryCandidateNameTextField.setBackground(Color.WHITE);
        secondaryCandidateNameTextField.setBackground(SELECTED_GREEN);
    }

    /**
     * Creates a standardized image of the candidate's name.
     *
     * @param candidateName name of candidate
     * @return a BufferedImage representing the candidate's name
     */
    public BufferedImage renderCandidateName (String candidateName)
    {
		/* Create the canvas on which the images will be drawn. */
        BufferedImage canvas = new BufferedImage(CANVAS_WIDTH, CANVAS_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
		/* Add each image to the canvas. */
        for (String character: candidateName.split(""))
        {
            if (!character.equals(""))
            {
                String imageName = pathToImages + "W_" + character.toUpperCase() + ".png";
				/* Create a file, which will be used to read in the image. */
                File file = new File(imageName);
                try
                {
					/* Read the image. */
                    BufferedImage currentImage = ImageIO.read(file);
					/* Draw the image on the canvas. */
                    g.drawImage(currentImage, nextUpperLeftX, nextUpperLeftY, null);
					/* Update the coordinates of the location where the next image is to be drawn. */
                    nextUpperLeftX += IMAGE_STANDARD_WIDTH;
					/* Check if the end of the row has been reached. */
                    if (CANVAS_WIDTH - nextUpperLeftX < IMAGE_STANDARD_WIDTH)
                    {
						/* Go to the next row. */
                        nextUpperLeftY += IMAGE_STANDARD_HEIGHT;
						/* Reset the horizontal offset in the row. */
                        nextUpperLeftX = 0;
                    }
                }
                catch (IOException e) {
                    System.out.println("Character entered: " + character);
                    System.out.println("Trying to load file: " + imageName);
                    //e.printStackTrace();
                }
            }
        }
		/* Return the canvas. */
        return canvas;
    }


}