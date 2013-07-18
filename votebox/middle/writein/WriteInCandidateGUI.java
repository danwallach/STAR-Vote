package votebox.middle.writein;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.Dimension;
import java.awt.Color;
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
public class WriteInCandidateGUI extends JFrame {

    /* The path to the directory that contains the images. */
    public static final String SLASH = System.getProperty("file.separator");
    public static final String pathToImages = "C:" + SLASH + "Users" + SLASH + "Mircea" + SLASH + "Desktop" + SLASH + "Ballots" + SLASH + "WriteIn" + SLASH;
    /* The standard size of the character images. */
    public static final int IMAGE_STANDARD_WIDTH = 14;//24;
    public static final int IMAGE_STANDARD_HEIGHT = 14;
    /* The size of the canvas. */
    public static final int CANVAS_WIDTH = 700;
    public static final int CANVAS_HEIGHT = 600;
    /* The location of the upper left corner of the next image to be drawn. */
    public static int nextUpperLeftX = 0;
    public static int nextUpperLeftY = 0;


    private JTextField candidateNameTextField;
    private JPanel candidateNamePanel;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    WriteInCandidateGUI frame = new WriteInCandidateGUI();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the frame.
     */
    public WriteInCandidateGUI() {

        // Set Frame properties.
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(0, 0, 800, 600);

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
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        JPanel mainPanel = new JPanel();
        contentPane.add(mainPanel, BorderLayout.CENTER);

        candidateNameTextField = new JTextField();
        candidateNameTextField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent arg0) {
                if(arg0.getKeyCode() == KeyEvent.VK_ENTER)
                {
					/* Clear the canvas panel. */
                    Graphics g = candidateNamePanel.getGraphics();
                    g.setColor(Color.WHITE);
                    g.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
                    nextUpperLeftX = 0;
                    nextUpperLeftY = 0;

                    /* Render the candidate's name. */
                    BufferedImage canvas = renderCandidateName(candidateNameTextField.getText());

					/* Draw the canvas into the JPanel. */
                    g.drawImage(canvas, 0, 0, null);

					/* Save the image to a file. */
                    File file = new File(pathToImages, "result.png");
                    try
                    {
                        ImageIO.write(canvas, "png", file);
                    }
                    catch (IOException e)
                    {
                        System.out.println("Canvas image creation failed!");
                        e.printStackTrace();
                    }
                }
            }
        });

        JLabel enterNameLabel = new JLabel("Enter your preferred candidate's name:");
        mainPanel.add(enterNameLabel);
        mainPanel.add(candidateNameTextField);
        candidateNameTextField.setColumns(10);

        candidateNamePanel = new JPanel();
        candidateNamePanel.setBackground(Color.WHITE);
        candidateNamePanel.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
        mainPanel.add(candidateNamePanel);
        candidateNamePanel.setLayout(null);
    }

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
