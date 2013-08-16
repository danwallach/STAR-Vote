package votebox.middle.writein;

import printer.PrintImageUtils;
import votebox.VoteBox;
import votebox.middle.ballot.Card;
import votebox.middle.ballot.WriteInCardElement;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Author: Mircea C. Berechet
 * Added to project: 07/18/2013
 */

/**
 * A graphical user interface that allows the voter to type in a candidate's name.
 * Based on the name that the voter enters, it renders an image that display the candidate's name
 * and adds it to the ballot files.
 */
public class WriteInCandidateGUI extends JDialog {

    /* The width of the drawable/viewable space on the screen. */
    private static final int GUI_WIDTH = 800;
    /* The height of the drawable/viewable space on the screen. */
    private static final int REGULAR_GUI_HEIGHT = 225;
    private static final int PRESIDENTIAL_GUI_HEIGHT = 365;
    /* The path to the directory that contains the images. */
    public static final String SLASH = System.getProperty("file.separator");
    //Current working directory
    File path = new File(System.getProperty("user.dir"));
    public static final String pathToImages = System.getProperty("user.dir") + SLASH + "tmp" + SLASH + "ballots" + SLASH + "ballot" + SLASH + "data" + SLASH + "media" + SLASH + "writein" + SLASH;
    /* The standard size of the character images. */
    public static final int IMAGE_STANDARD_WIDTH = 14;
    public static final int IMAGE_STANDARD_HEIGHT = 14;
    /* The size of the canvas. */
    public static final int CANVAS_WIDTH = 700;
    public static final int CANVAS_HEIGHT = 112;
    /* The location of the upper left corner of the next image to be drawn. */
    public static int nextUpperLeftX = 0;
    public static int nextUpperLeftY = 0;
    /* STAR-Vote colors. */
    private static final Color STAR_VOTE_BLUE = new Color (48, 149, 242);
    private static final Color STAR_VOTE_PINK = Color.PINK;

    /* The UID of the write-in candidate whose name will be entered in this GUI prompt. */
    private String CANDIDATE_UID;
    /* The type of the write-in candidate. */
    private String CANDIDATE_TYPE;

    /* Enable KeyListeners. */
    private Boolean USE_KEY_LISTENERS;

    private JTextField primaryCandidateNameTextField;
    private JTextField secondaryCandidateNameTextField;
    private JPanel primaryCandidateNamePanel;
    private JPanel secondaryCandidateNamePanel;

    private Boolean DONE;
    private Card parent;

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
        parent.setWriteInValue(primaryCandidateNameTextField.getText(), CANDIDATE_TYPE.equals("Presidential") ? secondaryCandidateNameTextField.getText() : "");
        setVisible(false);
        DONE = true;
        dispose();
    }

    /**
     * Launch the application.
     */
//    public static void main(String[] args) {
//        EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                try {
//                    WriteInCandidateGUI frame = new WriteInCandidateGUI(680, 384, "Z22", "Regular", true);
//                    frame.setVisible(true);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }

    /**
     * Create the GUI and build its GUI Elements.
     * @param cX the x-coordinate of the center of the GUI
     * @param cY the y-coordinate of the center of the GUI
     * @param uid the UID of the candidate
     * @param guiType the Type of GUI to start (Regular or Presidential)
     * @param useKeyListeners whether or not to use KeyListeners
     */
    public WriteInCandidateGUI(int cX, int cY, String uid, String guiType, Boolean useKeyListeners, Card parent)
    {
        System.out.println("Event Dispatch: " + SwingUtilities.isEventDispatchThread());

        DONE = false;

        this.parent = parent;

        // Set the UID.
        CANDIDATE_UID = uid;
        // Set the TYPE.
        CANDIDATE_TYPE = guiType;

        // Set the KeyListener flag.
        USE_KEY_LISTENERS = useKeyListeners;

        // Set the appropriate height for the GUI, based on the type of the candidate.
        int GUI_HEIGHT = CANDIDATE_TYPE.equals("Regular") ? REGULAR_GUI_HEIGHT : PRESIDENTIAL_GUI_HEIGHT;

        // Set Frame properties.
        setTitle("Type in Candidate Name");
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setBounds(cX - GUI_WIDTH / 2, cY - GUI_HEIGHT / 2, GUI_WIDTH, GUI_HEIGHT);

        // Build GUI Elements.
        buildGUIElements();
    }

    /**
     * Builds all GUI Elements of the Write-In-Candidate GUI.
     */
    private void buildGUIElements ()
    {
        /*
		 * CONTENT PANE
		 */
        JPanel contentPane = new JPanel();
        contentPane.setBackground(STAR_VOTE_BLUE);
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        /* Main panel of the GUI. */
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(STAR_VOTE_BLUE);
        contentPane.add(mainPanel, BorderLayout.CENTER);

        /* GUI Elements for the primary candidate. */
        primaryCandidateNameTextField = new JTextField();

        JLabel primaryEnterNameLabel = new JLabel("Enter your preferred " + CANDIDATE_TYPE + " candidate's name (" + CANDIDATE_UID + "):");
        mainPanel.add(primaryEnterNameLabel);
        mainPanel.add(primaryCandidateNameTextField);
        primaryCandidateNameTextField.setColumns(10);

        primaryCandidateNamePanel = new JPanel();
        primaryCandidateNamePanel.setBackground(Color.WHITE);
        primaryCandidateNamePanel.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
        mainPanel.add(primaryCandidateNamePanel);
        primaryCandidateNamePanel.setLayout(null);

        if (CANDIDATE_TYPE.equals("Presidential"))
        {
            /* GUI Elements for the secondary candidate. */
            secondaryCandidateNameTextField = new JTextField();

            JLabel secondaryEnterNameLabel = new JLabel("Enter your preferred " + CANDIDATE_TYPE + " vice-candidate's name (" + CANDIDATE_UID + "):");
            mainPanel.add(secondaryEnterNameLabel);
            mainPanel.add(secondaryCandidateNameTextField);
            secondaryCandidateNameTextField.setColumns(10);

            secondaryCandidateNamePanel = new JPanel();
            secondaryCandidateNamePanel.setBackground(Color.WHITE);
            secondaryCandidateNamePanel.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
            mainPanel.add(secondaryCandidateNamePanel);
            secondaryCandidateNamePanel.setLayout(null);
        }

        /* Button used to submit the entered name(s) and close the prompt. */
        JButton submitAndStopButton = new JButton("Submit name and close editor");
        mainPanel.add(submitAndStopButton);

        /*
         * Listeners for events.
        */
        /*
           Listens for an Enter key being pressed for the primary candidate.
           It builds and displays a candidate name based on the text that was entered in the text field.
        */
        if (USE_KEY_LISTENERS)
        {
            primaryCandidateNameTextField.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent arg0) {
                    if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
                        /* Clear the canvas panel. */
                        Graphics g = primaryCandidateNamePanel.getGraphics();
                        g.setColor(Color.WHITE);
                        g.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
                        nextUpperLeftX = 0;
                        nextUpperLeftY = 0;

                        /* Render the candidate's name. */
                        BufferedImage canvas = renderCandidateName(primaryCandidateNameTextField.getText());

                        /* Draw the canvas into the JPanel. */
                        g.drawImage(canvas, 0, 0, null);

                        /* Trim the image. */
                        canvas = PrintImageUtils.trimImageVertically(canvas, false, Integer.MAX_VALUE); // Above
                        canvas = PrintImageUtils.trimImageVertically(canvas, true, Integer.MAX_VALUE);  // Below
                        canvas = PrintImageUtils.trimImageHorizontally(canvas, false, Integer.MAX_VALUE); // Left
                        canvas = PrintImageUtils.trimImageHorizontally(canvas, true, Integer.MAX_VALUE); // Right

                        /* Save the image to a file. */
                        File file = new File(pathToImages, "result.png");
                        try {
                            ImageIO.write(canvas, "png", file);
                        } catch (IOException e) {
                            System.out.println("Canvas image creation failed!");
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        if (CANDIDATE_TYPE.equals("Presidential"))
        {
            /*
               Listens for an Enter key being pressed for the secondary candidate.
               It builds and displays a candidate name based on the text that was entered in the text field.
            */
            if (USE_KEY_LISTENERS)
            {
                secondaryCandidateNameTextField.addKeyListener(new KeyAdapter() {
                    public void keyPressed(KeyEvent arg0) {
                        if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
                            /* Clear the canvas panel. */
                            Graphics g = secondaryCandidateNamePanel.getGraphics();
                            g.setColor(Color.WHITE);
                            g.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
                            nextUpperLeftX = 0;
                            nextUpperLeftY = 0;

                            /* Render the candidate's name. */
                            BufferedImage canvas = renderCandidateName(secondaryCandidateNameTextField.getText());

                            /* Draw the canvas into the JPanel. */
                            g.drawImage(canvas, 0, 0, null);

                            /* Trim the image. */
                            canvas = PrintImageUtils.trimImageVertically(canvas, false, Integer.MAX_VALUE); // Above
                            canvas = PrintImageUtils.trimImageVertically(canvas, true, Integer.MAX_VALUE);  // Below
                            canvas = PrintImageUtils.trimImageHorizontally(canvas, false, Integer.MAX_VALUE); // Left
                            canvas = PrintImageUtils.trimImageHorizontally(canvas, true, Integer.MAX_VALUE); // Right

                            /* Save the image to a file. */
                            File file = new File(pathToImages, "result2.png");
                            try {
                                ImageIO.write(canvas, "png", file);
                            } catch (IOException e) {
                                System.out.println("Canvas image creation failed!");
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }

        /*
           Listens for the submit and stop button being pressed. Once it is pressed, this should render
           the appropriate images and dispose of the frame.
         */
        submitAndStopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                /* Render the appropriate images. */
                if (CANDIDATE_TYPE.equals("Regular"))
                {
                    VoteBox.renderWriteInImages(CANDIDATE_UID, CANDIDATE_TYPE, primaryCandidateNameTextField.getText());
                }
                else
                {
                    VoteBox.renderWriteInImages(CANDIDATE_UID, CANDIDATE_TYPE, primaryCandidateNameTextField.getText(), secondaryCandidateNameTextField.getText());
                }
                /* Stop the prompt. */
                stop();
            }
        });
    }

    public Boolean isDone()
    {
        return DONE;
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
