package configurationutility;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Dimension;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.awt.Font;
import java.awt.FlowLayout;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

/**
 * Author: Mircea C. Berechet
 * Added to project: 07/02/20113
 */
public class ConfigurationFileEditor extends JFrame {

	/*
	 * CLASS CONSTANTS
	 */
    // My main monitor's screen resolution: 1366 x 768
    // My second monitor's screen resolution: 1920 x 1080

    /* The width of the drawable/viewable space on the screen. */
    private static final int GUI_WIDTH = 1366;
    /* The height of the drawable/viewable space on the screen. */
    private static final int GUI_HEIGHT = 710;
    /* The height of the title bar (the bar at the top of the GUI display window that contains the name, program, minimize button, exit button and others). */
    private static final int TITLE_BAR_HEIGHT = 50;
    /* The combined width of the side bars. The side bars are the bars on both sides of the display window. */
    private static final int SIDE_BARS_WIDTH = 25;
    /* The horizontal offset of items in a panel, from the left size of the panel. */
    private static final int PANEL_CONTENTS_X_OFFSET = 5;
    /* The vertical offset of items in a panel, from the top of the panel. */
    private static final int PANEL_CONTENTS_Y_OFFSET = 5;
    /* The vertical offset of items in a TabbedPane (including the vertical offset of items in a panel and the height of the tab label). */
    private static final int TABBED_CONTENTS_Y_OFFSET = PANEL_CONTENTS_Y_OFFSET + 35; /* 35 is the size of the actual tab label */

    /* The width of the title panel of the editor. */
    private static final int TITLE_PANEL_WIDTH = GUI_WIDTH;
    /* The height of the title panel of the editor. */
    private static final int TITLE_PANEL_HEIGHT = 60;
    /* The width of the bottom panel of the editor. */
    private static final int BOTTOM_PANEL_WIDTH = GUI_WIDTH;
    /* The height of the bottom panel of the editor. */
    private static final int BOTTOM_PANEL_HEIGHT = 30;
    /* The height of the middle panels (functions, main, log) of the editor. */
    private static final int MIDDLE_PANELS_HEIGHT = GUI_HEIGHT - TITLE_BAR_HEIGHT - TITLE_PANEL_HEIGHT - BOTTOM_PANEL_HEIGHT;
    /* The width of the functions panel of the editor. */
    private static final int FUNCTIONS_PANEL_WIDTH = 300;
    /* The width of the log panel of the editor. */
    private static final int LOG_PANEL_WIDTH = 500;
    /* The width of the main panel of the editor. This is based on the total width, the width of the other middle panels and the width of the side bars. */
    private static final int MAIN_PANEL_WIDTH = GUI_WIDTH - SIDE_BARS_WIDTH - FUNCTIONS_PANEL_WIDTH - LOG_PANEL_WIDTH;

    /* The height of the file IO panel. */
    private static final int FILE_IO_PANEL_HEIGHT = 140;
    /* The number of columns to be displayed in the file name text fields. */
    private static final int FILE_IO_FILENAME_TEXTFIELD_NUMCOLUMNS = 15;
    /* The number of columns to be displayed in the message text field. */
    private static final int FILE_IO_MESSAGE_TEXTFIELD_NUMCOLUMNS = 25;
    /* The standard width of a file IO button. */
    private static final int FILE_IO_BUTTON_WIDTH = 90;
    /* The standard height of a file IO button. */
    private static final int FILE_IO_BUTTON_HEIGHT = 23;

    /* The height of the attribute editor content/main panel. */
    // Subtract twice the vertical offset: one for the top space, one for the space between file IO panel and attribute editor panel.
    private static final int ATTRIBUTE_EDITOR_PANEL_HEIGHT = MIDDLE_PANELS_HEIGHT - 2 * PANEL_CONTENTS_Y_OFFSET - FILE_IO_PANEL_HEIGHT;
    /* The height of the attribute editor button panel. */
    private static final int AE_BUTTON_PANEL_HEIGHT = 40;
    /* The height of the attribute editor display panel. */
    // Same explanation as above: One subtraction for the space at the top of the attribute editor panel and one for the space between the button panel and the editor panel.
    private static final int AE_DISPLAY_PANEL_HEIGHT = ATTRIBUTE_EDITOR_PANEL_HEIGHT - 2 * PANEL_CONTENTS_Y_OFFSET - AE_BUTTON_PANEL_HEIGHT;

    /* The number of columns to be displayed in the attribute name/value text fields. */
    private static final int AE_DISPLAY_ATTRIBUTE_TEXTFIELD_NUMCOLUMNS = 25;
    /* The height of the text area that displays the comments for an attribute. */
    private static final int AE_ATTRIBUTE_COMMENT_TEXT_AREA_HEIGHT = 70;

    /* The width of the Update File Contents button. */
    private static final int UPDATE_FILE_CONTENTS_BUTTON_WIDTH = 155;

    /* STAR-Vote colors. */
    private static final Color STAR_VOTE_BLUE = new Color (48, 149, 242);
    private static final Color STAR_VOTE_PINK = Color.PINK;

    /* Title font. */
    private static final Font TITLE_FONT = new Font("Tahoma", Font.PLAIN, 40);

    /* Group IDs for the valid attribute groups. */
    public static final int INVALID_GROUP = -1;
    public static final int GENERAL_GROUP = 0;
    public static final int NETWORK_GROUP = 1;
    public static final int PRINT_GROUP = 2;

    /* Patterns of text to be contained by print-specific attributes. */
    private static final String[] printPatterns = new String[] {"PRINT", "PAPER", "VVPAT"};
    /* Patterns of text to be contained by network-specific attributes. */
    private static final String[] networkPatterns = new String[] {"PORT"};



	/*
	 * CLASS FIELDS AND VARIABLES
	 */

    /* The main panel of the GUI. It contains all other components. */
    private JPanel contentPane;

    /* Text fields for file IO */
    private JTextField fileInTextField;
    private JTextField fileOutTextField;
    private JTextField fileIOMessageTextField;

    /* The text area that shows the contents of the file that has been read. */
    private final JTextArea inputFileTextArea;

    /* The text area that shows the current contents of the file to be written, including the modifications made in the current session. */
    private final JTextArea outputFileTextArea;

    /* The text area that shows the file description comments. */
    private final JTextArea fileDescriptionTextArea;

    /* The list of lines of text, as they are read from a file. */
    private ArrayList<String> fileLines = new ArrayList<String> ();

    /* The configuration attribute names. */
    private ArrayList<String> configurationAttributeNames = new ArrayList<String> ();

    /* The configuration attribute values. */
    private ArrayList<String> configurationAttributeValues = new ArrayList<String> ();

    /* The configuration attribute comments. */
    private ArrayList<String> configurationAttributeComments = new ArrayList<String> ();

    /* The file comments. */
    private ArrayList<String> fileComments = new ArrayList<String> ();

    /* Models for the JLists of the tabs in the main tabbed pane. These are used to add names/values to the JLists. */
    private final DefaultListModel<String> generalNamesListModel;
    private final DefaultListModel<String> generalValuesListModel;
    private final DefaultListModel<String> networkNamesListModel;
    private final DefaultListModel<String> networkValuesListModel;
    private final DefaultListModel<String> printNamesListModel;
    private final DefaultListModel<String> printValuesListModel;

    /* ArrayLists for storing the comments associated with the displayed attributes. Comments are not displayed. */
    private ArrayList<String> generalComments = new ArrayList<String> ();
    private ArrayList<String> networkComments = new ArrayList<String> ();
    private ArrayList<String> printComments = new ArrayList<String> ();

    /* Stores the ID of the group of attributes that the last attribute name was placed into. Used to place the attribute value in the same group. */
    private int groupIndexOfLastFoundAttributeName = INVALID_GROUP;

    /* Displays the name of the currently selected attribute. */
    private JTextField attributeNameTextField;
    /* Displays the value of the currently selected attribute. */
    private JTextField attributeValueTextField;
    /* Displays the comment of the currently selected attribute. */
    private JTextArea attributeCommentTextArea;

    /* Stores the index (in the configurationAttribute ArrayLists) of the currently selected attribute. */
    private int currentlySelectedAttributeIndex = -1;

    /* A file chooser, to be used when reading and writing files. */
    final JFileChooser fileChooser = new JFileChooser();

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    ConfigurationFileEditor frame = new ConfigurationFileEditor();
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
    public ConfigurationFileEditor() {

        setPreferredSize(new Dimension(GUI_WIDTH, GUI_HEIGHT));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(0, 0, GUI_WIDTH, GUI_HEIGHT);

		/*
		 * CONTENT PANE
		 */

        contentPane = new JPanel();
        contentPane.setPreferredSize(new Dimension(GUI_WIDTH, GUI_HEIGHT));
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

		/*
		 * TITLE PANEL
		 */
        /* The title panel of the GUI. It contains the title of the application. */
        JPanel titlePanel;
        titlePanel = new JPanel();
        titlePanel.setPreferredSize(new Dimension(TITLE_PANEL_WIDTH, TITLE_PANEL_HEIGHT));
        titlePanel.setBackground(Color.BLACK);
        contentPane.add(titlePanel, BorderLayout.NORTH);

        JLabel titleLabel = new JLabel("CONFIGURATION FILE EDITOR");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(TITLE_FONT);
        titlePanel.add(titleLabel);

		/*
		 * MAIN PANEL
		 */
		/* Two scroll bars, used for panel synchronizing. */
        JScrollBar namesScrollBar;
        JScrollBar valuesScrollBar;

        /* The main panel of the GUI. It contains the tabbed pane with the attribute names and values. */
        JPanel mainPanel;
        mainPanel = new JPanel();
        mainPanel.setPreferredSize(new Dimension(MAIN_PANEL_WIDTH, MIDDLE_PANELS_HEIGHT));
        mainPanel.setBackground(Color.WHITE);
        contentPane.add(mainPanel, BorderLayout.CENTER);

        final JTabbedPane mainTabbedPane = new JTabbedPane(JTabbedPane.TOP);
        mainTabbedPane.setPreferredSize(new Dimension(MAIN_PANEL_WIDTH - PANEL_CONTENTS_X_OFFSET, MIDDLE_PANELS_HEIGHT - PANEL_CONTENTS_Y_OFFSET));
        mainPanel.add(mainTabbedPane);

		/*
		 * General Tab
		 */
        JPanel generalTabPanel = new JPanel();
        FlowLayout flowLayout_1 = (FlowLayout) generalTabPanel.getLayout();
        flowLayout_1.setAlignment(FlowLayout.LEFT);
        mainTabbedPane.addTab("General", null, generalTabPanel, null);

        JScrollPane generalAttributesScrollPane = new JScrollPane();
        generalTabPanel.add(generalAttributesScrollPane);
        generalAttributesScrollPane.setPreferredSize(new Dimension(3 * (MAIN_PANEL_WIDTH - PANEL_CONTENTS_X_OFFSET) / 5 - 15, MIDDLE_PANELS_HEIGHT - TABBED_CONTENTS_Y_OFFSET));

        generalNamesListModel = new DefaultListModel<String>();
        final JList<String> generalAttributesList = new JList<String> (generalNamesListModel);
        generalAttributesScrollPane.setViewportView(generalAttributesList);

        JScrollPane generalValuesScrollPane = new JScrollPane();
        generalTabPanel.add(generalValuesScrollPane);
        generalValuesScrollPane.setPreferredSize(new Dimension(2 * (MAIN_PANEL_WIDTH - PANEL_CONTENTS_X_OFFSET) / 5, MIDDLE_PANELS_HEIGHT - TABBED_CONTENTS_Y_OFFSET));

		/* Synchronize the two scroll bars. */
        namesScrollBar = generalAttributesScrollPane.getVerticalScrollBar();
        valuesScrollBar = generalValuesScrollPane.getVerticalScrollBar();
        valuesScrollBar.setModel(namesScrollBar.getModel());

        generalValuesListModel = new DefaultListModel<String>();
        final JList<String> generalValuesList = new JList<String> (generalValuesListModel);
        generalValuesScrollPane.setViewportView(generalValuesList);

		/*
		 * Network Tab
		 */
        JPanel networkTabPanel = new JPanel();
        FlowLayout flowLayout = (FlowLayout) networkTabPanel.getLayout();
        flowLayout.setAlignment(FlowLayout.LEFT);
        mainTabbedPane.addTab("Network", null, networkTabPanel, null);

        JScrollPane networkAttributesScrollPane = new JScrollPane();
        networkTabPanel.add(networkAttributesScrollPane);
        networkAttributesScrollPane.setPreferredSize(new Dimension(3 * (MAIN_PANEL_WIDTH - PANEL_CONTENTS_X_OFFSET) / 5 - 15, MIDDLE_PANELS_HEIGHT - TABBED_CONTENTS_Y_OFFSET));

        networkNamesListModel = new DefaultListModel<String>();
        final JList<String> networkAttributesList = new JList<String> (networkNamesListModel);
        networkAttributesScrollPane.setViewportView(networkAttributesList);

        JScrollPane networkValuesScrollPane = new JScrollPane();
        networkTabPanel.add(networkValuesScrollPane);
        networkValuesScrollPane.setPreferredSize(new Dimension(2 * (MAIN_PANEL_WIDTH - PANEL_CONTENTS_X_OFFSET) / 5, MIDDLE_PANELS_HEIGHT - TABBED_CONTENTS_Y_OFFSET));

		/* Synchronize the two scroll bars. */
        namesScrollBar = networkAttributesScrollPane.getVerticalScrollBar();
        valuesScrollBar = networkValuesScrollPane.getVerticalScrollBar();
        valuesScrollBar.setModel(namesScrollBar.getModel());

        networkValuesListModel = new DefaultListModel<String>();
        final JList<String> networkValuesList = new JList<String> (networkValuesListModel);
        networkValuesScrollPane.setViewportView(networkValuesList);

		/*
		 * Print Tab
		 */
        JPanel printTabPanel = new JPanel();
        FlowLayout flowLayout_2 = (FlowLayout) printTabPanel.getLayout();
        flowLayout_2.setAlignment(FlowLayout.LEFT);
        mainTabbedPane.addTab("Print", null, printTabPanel, null);

        JScrollPane printAttributesScrollPane = new JScrollPane();
        printTabPanel.add(printAttributesScrollPane);
        printAttributesScrollPane.setPreferredSize(new Dimension(3 * (MAIN_PANEL_WIDTH - PANEL_CONTENTS_X_OFFSET) / 5 - 15, MIDDLE_PANELS_HEIGHT - TABBED_CONTENTS_Y_OFFSET));

        printNamesListModel = new DefaultListModel<String>();
        final JList<String> printAttributesList = new JList<String> (printNamesListModel);
        printAttributesScrollPane.setViewportView(printAttributesList);

        JScrollPane printValuesScrollPane = new JScrollPane();
        printTabPanel.add(printValuesScrollPane);
        printValuesScrollPane.setPreferredSize(new Dimension(2 * (MAIN_PANEL_WIDTH - PANEL_CONTENTS_X_OFFSET) / 5, MIDDLE_PANELS_HEIGHT - TABBED_CONTENTS_Y_OFFSET));

		/* Synchronize the two scroll bars. */
        namesScrollBar = printAttributesScrollPane.getVerticalScrollBar();
        valuesScrollBar = printValuesScrollPane.getVerticalScrollBar();
        valuesScrollBar.setModel(namesScrollBar.getModel());

        printValuesListModel = new DefaultListModel<String>();
        final JList<String> printValuesList = new JList<String> (printValuesListModel);
        printValuesScrollPane.setViewportView(printValuesList);

        /*
         * File comments Tab
         */
        JPanel descriptionTabPanel = new JPanel();
        FlowLayout flowLayout_5 = (FlowLayout) descriptionTabPanel.getLayout();
        flowLayout_5.setAlignment(FlowLayout.LEFT);
        mainTabbedPane.addTab("File Description", null, descriptionTabPanel, null);

        JScrollPane fileDescriptionScrollPane = new JScrollPane();
        descriptionTabPanel.add(fileDescriptionScrollPane);
        fileDescriptionScrollPane.setPreferredSize(new Dimension(MAIN_PANEL_WIDTH - 2 * PANEL_CONTENTS_X_OFFSET, MIDDLE_PANELS_HEIGHT - TABBED_CONTENTS_Y_OFFSET));

        fileDescriptionTextArea = new JTextArea();
        fileDescriptionScrollPane.setViewportView(fileDescriptionTextArea);
        fileDescriptionTextArea.setTabSize(4);
        fileDescriptionTextArea.setWrapStyleWord(true);
        fileDescriptionTextArea.setLineWrap(true);



		/*
		 * BOTTOM PANEL
		 */
        /* The bottom panel of the GUI. It contains descriptions of each section of the GUI. */
        JPanel bottomPanel;
        bottomPanel = new JPanel();
        bottomPanel.setPreferredSize(new Dimension(BOTTOM_PANEL_WIDTH, BOTTOM_PANEL_HEIGHT));
        bottomPanel.setBackground(Color.WHITE);
        contentPane.add(bottomPanel, BorderLayout.SOUTH);

        JLabel functionsDescriptionLabel = new JLabel("Utilities");
        functionsDescriptionLabel.setPreferredSize(new Dimension(FUNCTIONS_PANEL_WIDTH, 14));
        bottomPanel.add(functionsDescriptionLabel);

        JLabel namesListsDescriptionLabel = new JLabel("Attribute Names");
        namesListsDescriptionLabel.setPreferredSize(new Dimension(3 * (MAIN_PANEL_WIDTH - PANEL_CONTENTS_X_OFFSET) / 5 - 15, 14));
        bottomPanel.add(namesListsDescriptionLabel);

        JLabel valuesListsDescriptionLabel = new JLabel("Attribute Values");
        valuesListsDescriptionLabel.setPreferredSize(new Dimension(2 * (MAIN_PANEL_WIDTH - PANEL_CONTENTS_X_OFFSET) / 5, 14));
        bottomPanel.add(valuesListsDescriptionLabel);

        JLabel logsDescriptionLabel = new JLabel("File Contents");
        logsDescriptionLabel.setPreferredSize(new Dimension(LOG_PANEL_WIDTH - UPDATE_FILE_CONTENTS_BUTTON_WIDTH - PANEL_CONTENTS_X_OFFSET, 14));
        bottomPanel.add(logsDescriptionLabel);

        JButton updateFileContentsButton = new JButton("Update File Contents");
        bottomPanel.add(updateFileContentsButton);

		/*
		 * LOG PANEL
		 */

        JPanel logPanel = new JPanel();
        logPanel.setPreferredSize(new Dimension(LOG_PANEL_WIDTH, MIDDLE_PANELS_HEIGHT));
        logPanel.setBackground(Color.WHITE);
        contentPane.add(logPanel, BorderLayout.EAST);

		/*
		 * This is where the log tab pane is created.
		 */
        JTabbedPane logTabbedPane = new JTabbedPane(JTabbedPane.TOP);
        logTabbedPane.setPreferredSize(new Dimension(LOG_PANEL_WIDTH - PANEL_CONTENTS_X_OFFSET, MIDDLE_PANELS_HEIGHT - PANEL_CONTENTS_Y_OFFSET));
        logPanel.add(logTabbedPane);

		/*
		 * Input File Tab
		 */
        JPanel inputFileTabPanel = new JPanel();
        FlowLayout flowLayout_3 = (FlowLayout) inputFileTabPanel.getLayout();
        flowLayout_3.setAlignment(FlowLayout.LEFT);
        logTabbedPane.addTab("Input File", null, inputFileTabPanel, null);

        JScrollPane inputFileScrollPane = new JScrollPane();
        inputFileScrollPane.setPreferredSize(new Dimension(LOG_PANEL_WIDTH - PANEL_CONTENTS_X_OFFSET - 15, MIDDLE_PANELS_HEIGHT - TABBED_CONTENTS_Y_OFFSET));
        inputFileTabPanel.add(inputFileScrollPane);
        inputFileTextArea = new JTextArea();
        inputFileTextArea.setTabSize(4);
        inputFileTextArea.setEditable(false);
        inputFileScrollPane.setViewportView(inputFileTextArea);
        inputFileTextArea.setWrapStyleWord(true);
        inputFileTextArea.setLineWrap(true);
        inputFileTextArea.setText("");

		/*
		 * Output File Tab
		 */
        JPanel outputFileTabPanel = new JPanel();
        FlowLayout flowLayout_4 = (FlowLayout) outputFileTabPanel.getLayout();
        flowLayout_4.setAlignment(FlowLayout.LEFT);
        logTabbedPane.addTab("Output File", null, outputFileTabPanel, null);

        JScrollPane outputFileScrollPane = new JScrollPane();
        outputFileScrollPane.setPreferredSize(new Dimension(LOG_PANEL_WIDTH - PANEL_CONTENTS_X_OFFSET - 15, MIDDLE_PANELS_HEIGHT - TABBED_CONTENTS_Y_OFFSET));
        outputFileTabPanel.add(outputFileScrollPane);
        outputFileTextArea = new JTextArea();
        outputFileTextArea.setTabSize(4);
        outputFileTextArea.setEditable(false);
        outputFileScrollPane.setViewportView(outputFileTextArea);
        outputFileTextArea.setWrapStyleWord(true);
        outputFileTextArea.setLineWrap(true);
        outputFileTextArea.setText("");

		/*
		 * FUNCTIONS PANEL
		 */

        JPanel functionsPanel = new JPanel();
        functionsPanel.setPreferredSize(new Dimension(FUNCTIONS_PANEL_WIDTH, MIDDLE_PANELS_HEIGHT));
        functionsPanel.setBackground(Color.WHITE);
        contentPane.add(functionsPanel, BorderLayout.WEST);

		/* The file IO panel. */
        JPanel fileIOPanel = new JPanel();
        fileIOPanel.setBackground(ConfigurationFileEditor.STAR_VOTE_PINK);
        fileIOPanel.setPreferredSize(new Dimension(FUNCTIONS_PANEL_WIDTH, FILE_IO_PANEL_HEIGHT));
        functionsPanel.add(fileIOPanel);

        JLabel filenameLabel = new JLabel("Enter the name of the configuration file:");
        fileIOPanel.add(filenameLabel);

        JSeparator fileIOHorizontalSeparator1 = new JSeparator();
        fileIOHorizontalSeparator1.setPreferredSize(new Dimension(FUNCTIONS_PANEL_WIDTH - 20, 1));
        fileIOPanel.add(fileIOHorizontalSeparator1);

        fileInTextField = new JTextField();
        fileInTextField.setColumns(FILE_IO_FILENAME_TEXTFIELD_NUMCOLUMNS);
        fileIOPanel.add(fileInTextField);

        JButton openFileButton = new JButton("Open file");
        openFileButton.setPreferredSize(new Dimension(FILE_IO_BUTTON_WIDTH, FILE_IO_BUTTON_HEIGHT));
        fileIOPanel.add(openFileButton);

        fileOutTextField = new JTextField();
        fileOutTextField.setColumns(FILE_IO_FILENAME_TEXTFIELD_NUMCOLUMNS);
        fileIOPanel.add(fileOutTextField);

        JButton saveFileButton = new JButton("Save file");
        saveFileButton.setPreferredSize(new Dimension(FILE_IO_BUTTON_WIDTH, FILE_IO_BUTTON_HEIGHT));
        fileIOPanel.add(saveFileButton);

        JSeparator fileIOHorizontalSeparator2 = new JSeparator();
        fileIOHorizontalSeparator2.setPreferredSize(new Dimension(FUNCTIONS_PANEL_WIDTH - 20, 1));
        fileIOPanel.add(fileIOHorizontalSeparator2);

        JLabel fileIOMessageLabel = new JLabel("Message:");
        fileIOPanel.add(fileIOMessageLabel);

        fileIOMessageTextField = new JTextField();
        fileIOMessageTextField.setEditable(false);
        fileIOMessageTextField.setColumns(FILE_IO_MESSAGE_TEXTFIELD_NUMCOLUMNS);
        fileIOPanel.add(fileIOMessageTextField);

		/* The attribute editor panel. */
        JPanel attributeEditorPanel = new JPanel();
        attributeEditorPanel.setBackground(ConfigurationFileEditor.STAR_VOTE_BLUE);
        attributeEditorPanel.setPreferredSize(new Dimension(FUNCTIONS_PANEL_WIDTH, ATTRIBUTE_EDITOR_PANEL_HEIGHT));
        functionsPanel.add(attributeEditorPanel);

        JPanel mainAEButtonPanel = new JPanel();
        mainAEButtonPanel.setBackground(ConfigurationFileEditor.STAR_VOTE_BLUE);
        mainAEButtonPanel.setPreferredSize(new Dimension(FUNCTIONS_PANEL_WIDTH, AE_BUTTON_PANEL_HEIGHT));
        attributeEditorPanel.add(mainAEButtonPanel);

        JButton saveChangesButton = new JButton("Save Changes");
        mainAEButtonPanel.add(saveChangesButton);

        JButton addAttributeButton = new JButton("Add Attribute");
        mainAEButtonPanel.add(addAttributeButton);

        JPanel mainAEDisplayPanel = new JPanel();
        mainAEDisplayPanel.setBackground(ConfigurationFileEditor.STAR_VOTE_BLUE);
        mainAEDisplayPanel.setPreferredSize(new Dimension(FUNCTIONS_PANEL_WIDTH, AE_DISPLAY_PANEL_HEIGHT));
        attributeEditorPanel.add(mainAEDisplayPanel);

        JSeparator mainAEDisplayHorizontalSeparator1 = new JSeparator();
        mainAEDisplayHorizontalSeparator1.setPreferredSize(new Dimension(FUNCTIONS_PANEL_WIDTH - 20, 1));
        mainAEDisplayPanel.add(mainAEDisplayHorizontalSeparator1);

		/* Attribute Name */
        JLabel attributeNameLabel = new JLabel("Label:");
        mainAEDisplayPanel.add(attributeNameLabel);

        attributeNameTextField = new JTextField();
        mainAEDisplayPanel.add(attributeNameTextField);
        attributeNameTextField.setColumns(AE_DISPLAY_ATTRIBUTE_TEXTFIELD_NUMCOLUMNS);

        JSeparator mainAEDisplayHorizontalSeparator2 = new JSeparator();
        mainAEDisplayHorizontalSeparator2.setPreferredSize(new Dimension(FUNCTIONS_PANEL_WIDTH - 20, 1));
        mainAEDisplayPanel.add(mainAEDisplayHorizontalSeparator2);

		/* Attribute Value */
        JLabel attributeValueLabel = new JLabel("Value:");
        mainAEDisplayPanel.add(attributeValueLabel);

        attributeValueTextField = new JTextField();
        mainAEDisplayPanel.add(attributeValueTextField);
        attributeValueTextField.setColumns(AE_DISPLAY_ATTRIBUTE_TEXTFIELD_NUMCOLUMNS);

        JSeparator mainAEDisplayHorizontalSeparator3 = new JSeparator();
        mainAEDisplayHorizontalSeparator3.setPreferredSize(new Dimension(FUNCTIONS_PANEL_WIDTH - 20, 1));
        mainAEDisplayPanel.add(mainAEDisplayHorizontalSeparator3);

		/* Attribute Comment */
        JLabel attributeCommentLabel = new JLabel("Comment:");
        mainAEDisplayPanel.add(attributeCommentLabel);

        JScrollPane attributeCommentScrollPane = new JScrollPane();
        attributeCommentScrollPane.setPreferredSize(new Dimension(FUNCTIONS_PANEL_WIDTH - PANEL_CONTENTS_X_OFFSET, AE_ATTRIBUTE_COMMENT_TEXT_AREA_HEIGHT));
        mainAEDisplayPanel.add(attributeCommentScrollPane);

        attributeCommentTextArea = new JTextArea();
        attributeCommentScrollPane.setViewportView(attributeCommentTextArea);

        JSeparator mainAEDisplayHorizontalSeparator4 = new JSeparator();
        mainAEDisplayHorizontalSeparator4.setPreferredSize(new Dimension(FUNCTIONS_PANEL_WIDTH - 20, 1));
        mainAEDisplayPanel.add(mainAEDisplayHorizontalSeparator4);


		/*
		 * LISTENERS FOR VARIOUS ACTIONS AND EVENTS.
		 */
		/* Action listener for a button click on the Open File button. */
        openFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                openFileAndReadText();
            }
        });

		/* Action listener for a button click on the Add Attribute button. */
        saveFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                openFileAndWriteText();
            }
        });

		/*
		 * JList selection change listeners for each group:
		 */
		/* 1. General */
		/* List selection listener for a value change on the attribute Name. */
        generalAttributesList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent arg0) {
                loadAttributeAtPosition(generalAttributesList.getSelectedIndex());
            }
        });

		/* List selection listener for a value change on the attribute Value. */
        generalValuesList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent arg0) {
                loadAttributeAtPosition(generalValuesList.getSelectedIndex());
            }
        });

		/* 2. Network */
		/* List selection listener for a value change on the attribute Name. */
        networkAttributesList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent arg0) {
                loadAttributeAtPosition(networkAttributesList.getSelectedIndex() + generalNamesListModel.getSize());
            }
        });

		/* List selection listener for a value change on the attribute Value. */
        networkValuesList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent arg0) {
                loadAttributeAtPosition(networkValuesList.getSelectedIndex() + generalValuesListModel.getSize());
            }
        });

		/* 3. Print */
		/* List selection listener for a value change on the attribute Name. */
        printAttributesList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent arg0) {
                loadAttributeAtPosition(printAttributesList.getSelectedIndex() + generalNamesListModel.getSize() + networkNamesListModel.getSize());
            }
        });

		/* List selection listener for a value change on the attribute Value. */
        printValuesList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent arg0) {
                loadAttributeAtPosition(printValuesList.getSelectedIndex() + generalValuesListModel.getSize() + networkValuesListModel.getSize());
            }
        });

		/* Action listener for a button click on the Save Changes button. */
        saveChangesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                saveAttributeAtPosition(currentlySelectedAttributeIndex);
            }
        });

		/* Action listener for a button click on the Add Attribute button. */
        addAttributeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                addAttributeToGroup(mainTabbedPane.getSelectedIndex());
            }
        });

		/* Action listener for a button click on the Add Attribute button. */
        updateFileContentsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                updateFileContents();
            }
        });
    }


    private void openFileAndReadText ()
    {
        // Open the file chooser.
        int openFileChooserStatus = fileChooser.showOpenDialog(contentPane);

        if (openFileChooserStatus == JFileChooser.APPROVE_OPTION)
        {
            fileInTextField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }

        // Get the filename.
        String filename = fileInTextField.getText();

        if (!filename.equals(""))
        {
            // Open the file.
            // C:\Users\Mircea\Desktop\VoteBoxPrinting\vb.conf
            File file = new File (filename);

            // If the file does not exist, then print error.
            if (!file.exists())
            {
                fileIOMessageTextField.setText("OPEN FILE: File not found!");
                System.err.println("The specified file could not be found: " + filename);

            }
            else // File DOES exist.
            {
                // Create the reader.
                BufferedReader reader;
                fileLines.clear();
                try
                {
                    reader = new BufferedReader(new FileReader(file.getAbsoluteFile()));
                    String currentLine;
                    while ((currentLine = reader.readLine()) != null)
                    {
                        fileLines.add(currentLine);
                    }
                    reader.close();
                }
                catch (IOException e)
                {
                    System.out.println("Unable to read from file " + filename);
                    inputFileTextArea.setText("FILE COULD NOT BE OPENED!\nThis could be caused by one of:\n\t1. Invalid filename\n\t2. Corrupted file\n\t3. Aliens");
                    fileIOMessageTextField.setText("OPEN FILE: Unable to read from file.");
                    e.printStackTrace();
                    return;
                }

                // Clear the text-storing variables and components.
                inputFileTextArea.setText("");
                outputFileTextArea.setText("");
                fileDescriptionTextArea.setText("");
                configurationAttributeNames.clear();
                configurationAttributeValues.clear();
                configurationAttributeComments.clear();
                fileComments.clear();
                generalNamesListModel.clear();
                generalValuesListModel.clear();
                generalComments.clear();
                networkNamesListModel.clear();
                networkValuesListModel.clear();
                networkComments.clear();
                printNamesListModel.clear();
                printValuesListModel.clear();
                printComments.clear();

                // Store the current comment somewhere, so it can be added to the appropriate group.
                String currentComment = "";

                // Display the file contents in the main text area.
                for (String line : fileLines)
                {
                    inputFileTextArea.setText(inputFileTextArea.getText() + line + "\n");
                    outputFileTextArea.setText(outputFileTextArea.getText() + line + "\n");

                    if (!line.equals(""))
                    {
                        if (!line.startsWith("#")) // Line is not a comment.
                        {

                            if (line.contains("_")) // Line is an attribute name.
                            {
				        		/* Keeps track of whether or not the current attribute name was placed in one of the attribute groups (tabs). */
                                Boolean isNotPlaced = true;

					        	/* Try to place the current attribute name in the Print attribute group. */
                                for (String printPattern : printPatterns)
                                {
                                    if (line.contains(printPattern))
                                    {
                                        // Add the current line as an attribute name in the Print group.
                                        printNamesListModel.add(printNamesListModel.getSize(), line);
                                        // Because there is always a space added in front of each line of comment, the current comment will always contain a leading space.
                                        if (!currentComment.equals(""))
                                        {
                                            printComments.add(currentComment.substring(1)); // Remove that leading space.
                                        }
                                        else
                                        {
                                            printComments.add(currentComment);
                                        }
                                        // Reset the current comment.
                                        currentComment = "";
                                        // Set the index of the last found attribute to be this group's index.
                                        groupIndexOfLastFoundAttributeName = ConfigurationFileEditor.PRINT_GROUP;
                                        // Now that the attribute was placed in a group, set the flag to false.
                                        isNotPlaced = false;
                                        // Stop trying to find more print patterns in this line.
                                        break;
                                    }
                                }

				        		/* If it is still not placed, try to place the current attribute name in the Network attribute group. */
                                if (isNotPlaced)
                                {
                                    for (String networkPattern : networkPatterns)
                                    {
                                        if (line.contains(networkPattern))
                                        {
                                            // Add the current line as an attribute name in the Network group.
                                            networkNamesListModel.add(networkNamesListModel.getSize(), line);
                                            // Because there is always a space added in front of each line of comment, the current comment will always contain a leading space.
                                            if (!currentComment.equals(""))
                                            {
                                                networkComments.add(currentComment.substring(1)); // Remove that leading space.
                                            }
                                            else
                                            {
                                                networkComments.add(currentComment);
                                            }
                                            // Reset the current comment.
                                            currentComment = "";
                                            // Set the index of the last found attribute to be this group's index.
                                            groupIndexOfLastFoundAttributeName = ConfigurationFileEditor.NETWORK_GROUP;
                                            // Now that the attribute was placed in a group, set the flag to false.
                                            isNotPlaced = false;
                                            // Stop trying to find more network patterns in this line.
                                            break;
                                        }
                                    }
                                }

				        		/* If it is still not placed, place the current attribute name in the General attribute group. */
                                if (isNotPlaced)
                                {
                                    // Add the current line as an attribute name in the General group.
                                    generalNamesListModel.add(generalNamesListModel.getSize(), line);
                                    // Because there is always a space added in front of each line of comment, the current comment will always contain a leading space.
                                    if (!currentComment.equals(""))
                                    {
                                        generalComments.add(currentComment.substring(1)); // Remove that leading space.
                                    }
                                    else
                                    {
                                        generalComments.add(currentComment);
                                    }
                                    // Reset the current comment.
                                    currentComment = "";
                                    // Set the index of the last found attribute to be this group's index.
                                    groupIndexOfLastFoundAttributeName = ConfigurationFileEditor.GENERAL_GROUP;
                                }
                            }
                            else // Line is an attribute value.
                            {
                                switch (groupIndexOfLastFoundAttributeName)
                                {
                                    case ConfigurationFileEditor.INVALID_GROUP:
                                        generalValuesListModel.add(generalValuesListModel.getSize(), "ERROR: " + line + " has no associated attribute name!");
                                        break;
                                    case ConfigurationFileEditor.GENERAL_GROUP:
                                        generalValuesListModel.add(generalValuesListModel.getSize(), line);
                                        break;
                                    case ConfigurationFileEditor.NETWORK_GROUP:
                                        networkValuesListModel.add(networkValuesListModel.getSize(), line);
                                        break;
                                    case ConfigurationFileEditor.PRINT_GROUP:
                                        printValuesListModel.add(printValuesListModel.getSize(), line);
                                        break;
                                    default:
                                        generalValuesListModel.add(generalValuesListModel.getSize(), "ERROR: " + line + " is associated to an attribute name that got placed in an invalid group!");
                                        break;
                                }

                                // Reset the group index. A value has already been found for that particular attribute.
                                groupIndexOfLastFoundAttributeName = ConfigurationFileEditor.INVALID_GROUP;
                            }
                        }
                        else // Line IS a comment.
                        {
                            if (line.startsWith("##")) // Line is a file comment.
                            {
                                fileComments.add(line.substring(line.startsWith("## ") ? 3 : 2)); // Add this line to the file comments ArrayList and stip the leading '##'.
                            }
                            else // Line is an attribute comment.
                            {
                                currentComment += " " + line.substring(line.startsWith("# ") ? 2 : 1); // Add this line to the current comment and strip the leading '#'.
                            }
                        }
                    } // End of if(!line.equals(""))

                }

		        /* Add the attribute names and values to the list of attribute names and the list of attribute values, respectively. */
                // From the General tab...
                for (int idx = 0; idx < generalNamesListModel.getSize(); idx++)
                {
                    configurationAttributeNames.add(generalNamesListModel.getElementAt(idx));
                    configurationAttributeValues.add(generalValuesListModel.getElementAt(idx));
                    configurationAttributeComments.add(generalComments.get(idx));
                }

                // ... and the Network tab ...
                for (int idx = 0; idx < networkNamesListModel.getSize(); idx++)
                {
                    configurationAttributeNames.add(networkNamesListModel.getElementAt(idx));
                    configurationAttributeValues.add(networkValuesListModel.getElementAt(idx));
                    configurationAttributeComments.add(networkComments.get(idx));
                }

                // ... and finally, the Print tab.
                for (int idx = 0; idx < printNamesListModel.getSize(); idx++)
                {
                    configurationAttributeNames.add(printNamesListModel.getElementAt(idx));
                    configurationAttributeValues.add(printValuesListModel.getElementAt(idx));
                    configurationAttributeComments.add(printComments.get(idx));
                }

                // Write the file description comments to the appropriate text area.
                for (String comment : fileComments)
                {
                    fileDescriptionTextArea.append(comment + "\n");
                }

                // Print a success message.
                fileIOMessageTextField.setText("OPEN FILE: File read successfully.");
            }
        }
        else // Filename DOES equal "".
        {
            fileIOMessageTextField.setText("OPEN FILE: File name not specified!");
        }
    }

    private void openFileAndWriteText ()
    {
        // Open the file chooser.
        int saveFileChooserStatus = fileChooser.showSaveDialog(contentPane);

        if (saveFileChooserStatus == JFileChooser.APPROVE_OPTION)
        {
            fileOutTextField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }

        // Get the filename.
        String filename = fileOutTextField.getText();

        if (!filename.equals(""))
        {
            // Open the file.
            File file = new File (filename);

            // If the file does not exist, then print error.
            boolean fileExisted = file.exists();
            boolean newFileCreated = false;
            if (!fileExisted)
            {
                try
                {
                    newFileCreated = file.createNewFile();
                }
                catch (IOException e)
                {
                    System.out.println("Unable to create new file " + filename);
                    fileIOMessageTextField.setText("SAVE FILE: Unable to create new file.");
                    e.printStackTrace();
                    return;
                }
            }
            // Create the writer.
            BufferedWriter writer;
            try
            {
                if (fileExisted || newFileCreated)
                {
                    writer = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
                    writer.write(outputFileTextArea.getText());
                    writer.close();
                }
            }
            catch (IOException e)
            {
                System.out.println("Unable to write to file " + filename);
                fileIOMessageTextField.setText("SAVE FILE: Unable to write to file.");
                e.printStackTrace();
                return;
            }

            // Print a success message.
            fileIOMessageTextField.setText("SAVE FILE: File written successfully.");
        }
        else // Filename DOES equal "".
        {
            fileIOMessageTextField.setText("SAVE FILE: File name not specified!");
        }
    }

    private void loadAttributeAtPosition (int position)
    {
        // Reset the active attribute.
        currentlySelectedAttributeIndex = -1;
        // If the position is valid, activate a new attribute.
        if ((configurationAttributeNames.size() > position) && (position != -1))
        {
            attributeNameTextField.setText(configurationAttributeNames.get(position));
            attributeValueTextField.setText(configurationAttributeValues.get(position));
            attributeCommentTextArea.setText(configurationAttributeComments.get(position));
            currentlySelectedAttributeIndex = position;
        }
    }

    private void saveAttributeAtPosition (int position)
    {
        // If the position is valid, replace the values in the configurationAttribute ArrayLists.
        if (position != -1)
        {
            // Read in the values to be saved.
            String attributeName = attributeNameTextField.getText();
            String attributeValue = attributeValueTextField.getText();
            String attributeComment = attributeCommentTextArea.getText();

            // Replace the entries in the ArrayLists.
            configurationAttributeNames.set(position, attributeName);
            configurationAttributeValues.set(position, attributeValue);
            configurationAttributeComments.set(position, attributeComment);

            // Replace the entries in the JListModels.
            if (position < generalNamesListModel.size()) // If the position represents a position in the General tab, update it.
            {
                generalNamesListModel.set(position, attributeName);
                generalValuesListModel.set(position, attributeValue);
                generalComments.set(position, attributeComment);
                return;
            }
            else // Otherwise, update the position by subtracting the number of items in the General tab.
            {
                position -= generalNamesListModel.getSize();
            }

            if (position < networkNamesListModel.size()) // If the position represents a position in the Network tab, update it.
            {
                networkNamesListModel.set(position, attributeName);
                networkValuesListModel.set(position, attributeValue);
                networkComments.set(position, attributeComment);
                return;
            }
            else // Otherwise, update the position by subtracting the number of items in the Network tab.
            {
                position -= networkNamesListModel.getSize();
            }

            if (position < printNamesListModel.size()) // If the position represents a position in the Print tab, update it.
            {
                printNamesListModel.set(position, attributeName);
                printValuesListModel.set(position, attributeValue);
                printComments.set(position, attributeComment);
            }
            else // Otherwise, print an error message.
            {
                System.err.println("Invalid position! No such attribute group exists.");
            }

        }
    }

    private void addAttributeToGroup (int group)
    {
        // Read in the values to be saved.
        String attributeName = attributeNameTextField.getText();
        String attributeValue = attributeValueTextField.getText();
        String attributeComment = attributeCommentTextArea.getText();

        switch (group)
        {
            case ConfigurationFileEditor.GENERAL_GROUP:
                // Add the attribute to the ArrayLists.
                int generalIndex = generalNamesListModel.getSize();

                configurationAttributeNames.add(generalIndex, attributeName);
                configurationAttributeValues.add(generalIndex, attributeValue);
                configurationAttributeComments.add(generalIndex, attributeComment);

                // Add the attribute to the group.
                generalNamesListModel.add(generalNamesListModel.getSize(), attributeName);
                generalValuesListModel.add(generalValuesListModel.getSize(), attributeValue);
                generalComments.add(attributeComment);
                break;
            case ConfigurationFileEditor.NETWORK_GROUP:
                // Add the attribute to the ArrayLists.
                int networkIndex = networkNamesListModel.getSize() + generalNamesListModel.getSize();

                configurationAttributeNames.add(networkIndex, attributeName);
                configurationAttributeValues.add(networkIndex, attributeValue);
                configurationAttributeComments.add(networkIndex, attributeComment);

                // Add the attribute to the group.
                networkNamesListModel.add(networkNamesListModel.getSize(), attributeName);
                networkValuesListModel.add(networkValuesListModel.getSize(), attributeValue);
                networkComments.add(attributeComment);
                break;
            case ConfigurationFileEditor.PRINT_GROUP:
                // Add the attribute to the ArrayLists.
                int printIndex = printNamesListModel.getSize() + generalNamesListModel.getSize() + networkNamesListModel.getSize();

                configurationAttributeNames.add(printIndex, attributeName);
                configurationAttributeValues.add(printIndex, attributeValue);
                configurationAttributeComments.add(printIndex, attributeComment);

                // Add the attribute to the group.
                printNamesListModel.add(printNamesListModel.getSize(), attributeName);
                printValuesListModel.add(printValuesListModel.getSize(), attributeValue);
                printComments.add(attributeComment);
                break;
            default:
                System.out.println("NO TAB SELECTED");
                break;
        }
    }

    private void updateFileContents ()
    {
        // Clear the output file text.
        outputFileTextArea.setText("");

        // Update the file comments and add them to the output file text.
        fileComments.clear();
        for (String fileComment : fileDescriptionTextArea.getText().split("\n"))
        {
            fileComments.add(fileComment);
            outputFileTextArea.append("## " + fileComment + "\n");
        }

        // Add in the attributes.
        for (int index = 0; index < configurationAttributeNames.size(); index++)
        {
            outputFileTextArea.append("\n"); // Add a blank line.
            if (!configurationAttributeComments.get(index).equals(""))
            {
                outputFileTextArea.append("# " + configurationAttributeComments.get(index) + "\n"); // Add the attribute comment.
            }
            outputFileTextArea.append(configurationAttributeNames.get(index) + "\n"); // Add the attribute name.
            outputFileTextArea.append(configurationAttributeValues.get(index) + "\n"); // Add the attribute value.
        }
    }
}