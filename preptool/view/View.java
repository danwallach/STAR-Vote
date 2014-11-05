/**
  * This file is part of VoteBox.
  * 
  * VoteBox is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License version 3 as published by
  * the Free Software Foundation.
  * 
  * You should have received a copy of the GNU General Public License
  * along with VoteBox, found in the root of any distribution or
  * repository containing all or part of VoteBox.
  * 
  * THIS SOFTWARE IS PROVIDED BY WILLIAM MARSH RICE UNIVERSITY, HOUSTON,
  * TX AND IS PROVIDED 'AS IS' AND WITHOUT ANY EXPRESS, IMPLIED OR
  * STATUTORY WARRANTIES, INCLUDING, BUT NOT LIMITED TO, WARRANTIES OF
  * ACCURACY, COMPLETENESS, AND NONINFRINGEMENT.  THE SOFTWARE USER SHALL
  * INDEMNIFY, DEFEND AND HOLD HARMLESS RICE UNIVERSITY AND ITS FACULTY,
  * STAFF AND STUDENTS FROM ANY AND ALL CLAIMS, ACTIONS, DAMAGES, LOSSES,
  * LIABILITIES, COSTS AND EXPENSES, INCLUDING ATTORNEYS' FEES AND COURT
  * COSTS, DIRECTLY OR INDIRECTLY ARISING OUR OF OR IN CONNECTION WITH
  * ACCESS OR USE OF THE SOFTWARE.
 */

package preptool.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.DefaultEditorKit;

import preptool.controller.exception.BallotExportException;
import preptool.controller.exception.BallotOpenException;
import preptool.controller.exception.BallotPreviewException;
import preptool.controller.exception.BallotSaveException;
import preptool.model.Model;
import preptool.model.Party;
import preptool.model.ballot.ACard;
import preptool.model.ballot.ICardFactory;
import preptool.model.ballot.PartyCard;
import preptool.model.language.Language;
import preptool.view.dialog.LanguagesDialog;
import preptool.view.dialog.PartiesDialog;
import preptool.view.dragndrop.ListTransferHandler;
import preptool.view.dragndrop.ListTransferListener;


/**
 * The View is the entire graphical user interface of the program. It extends a
 * JFrame, which holds all of the components of the View.
 * 
 * @author Corey Shaw
 */
public class View extends JFrame {

    private static final long serialVersionUID = 1L;

    /**
     * A common file chooser for use throughout the preptool.
     * It is generally considered terrible practice to keep dropping
     * the user in their home directory, even after they've navigated away
     * prior.
     */
    private JFileChooser fileChooser;
    
    /**
     * Adapter to make calls to the model
     */
    private Model model;

    /**
     * Main Menu bar
     */
    private JMenuBar menuBar;

    /**
     * Popup menu when user wants to add a card
     */
    private JPopupMenu addCardMenu;
    
    /**
     * Popup menu when the user wants to change the properties of the layout
     */
    private JPopupMenu prefMenu;

    /**
     * Main toolbar (with important buttons on it)
     */
    private JToolBar toolbar;

    /**
     * Button to create a new ballot
     */
    private JButton newBallotButton;

    /**
     * Button to open a ballot from disk
     */
    private JButton openBallotButton;

    /**
     * Button to save a ballot to disk
     */
    private JButton saveBallotButton;

    /**
     * Button to export a ballot to VoteBox format
     */
    private JButton exportBallotButton;

    /**
     * Button to preview the ballot in VoteBox
     */
    private JButton previewButton;
    
    /**
     * Button to edit preferences
     */
    private JButton prefButton;

    /**
     * Panel holding the card list and modifier buttons
     */
    private JPanel listPanel;

    /**
     * List of cards on the current ballot
     */
    private JList<String> cardList;

    /**
     * List model for the card list
     */
    private DefaultListModel<String> cardListModel;

    /**
     * Toolbar containing card list modifier buttons
     */
    private JToolBar listToolbar;

    /**
     * Button to add a new card to this ballot
     */
    private JButton addCardButton;

    /**
     * Button to delete the currently selected card
     */
    private JButton deleteCardButton;

    /**
     * Button to move the currently selected card up one in the list
     */
    private JButton moveUpCardButton;

    /**
     * Button to move the current card down one in the list
     */
    private JButton moveDownCardButton;

    /**
     * Panel that holds the controls to edit the currently selected card
     */
    private JPanel cardPanel;

    /**
     * Currently shown CardView
     */
    private CardView currentCardView;

    /**
     * Panel that shows a live preview of the current card
     */
    private PreviewPane previewPanel;

    /**
     * Panel that is shown when there are no cards in the open ballot
     */
    private JPanel noCardsPanel;

    /**
     * Language bar for the card view
     */
    private LanguageBar languageBar;

    /**
     * Language change listeners
     */
    private ArrayList<LanguageChangeListener> languageChangeListeners;

    private Observer titleObserver;

    /**
     * Constructs a new View, initializing all of the components and adding them
     * in their respective locations. Does *NOT* show the frame: the Controller
     * is responsible for doing that once everything is initialized.
     * 
     * @param m         the model of the program
     */
    public View(Model m) {

        /* Setup */
        super("VoteBox Preparation Tool");


        model = m;
        languageChangeListeners = new ArrayList<>();

        /* Set look and feel */
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception e) { e.printStackTrace(); }

        /*
          TODO Mac OS X only: Use single menu bar at top of screen. Ignored on
          other platforms.

          XXX: I think this doesn't fly if you set it after the UI has been
          initialized. It needs to be an argument to the JVM (e.g.
          -Dapple.laf.useScreenMenuBar=true) or set in the Info.plist of a .app
          (created with Jar Bundler or the jarbundler ant task). Similarly, the
          app's "dock name" and "dock icon" can be set this way. --dsandler
        */
        System.setProperty("apple.laf.useScreenMenuBar", "true");

        /* Create a window listener */
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitButtonPressed();
            }
        } );

        /* Set size, layout, and default close operation */
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setSize(1000, 800);
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        /* Initialise preference menu */
        initializePrefMenu();

        /* Initialise toolbar */
        initializeToolbar();

        /* Positioning... */
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.FIRST_LINE_START;

        /* Add the toolbar */
        add(toolbar, c);

        /* Initialise the list panel */
        initializeListPanel();

        /* Positioning */
        c.gridy = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LAST_LINE_START;

        /* Add the list toolbar and set the border */
        listPanel.add(listToolbar, c);
        listPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

        /* Initialise the card panel */
        initializeCardPanel();

        /* Create a new split pane with divider */
        JSplitPane cardSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, cardPanel, previewPanel);
        cardSplitPane.setDividerLocation(400);

        /* Create a new panel and set the layout */
        JPanel rightSide = new JPanel();
        rightSide.setLayout(new GridBagLayout());
        GridBagConstraints c2 = new GridBagConstraints();
        c2.gridx = 0;
        c2.gridy = 0;
        c2.weightx = 1;
        c2.weighty = 1;
        c2.fill = GridBagConstraints.BOTH;
        rightSide.add(cardSplitPane, c2);
        c2.gridy = 1;
        c2.weighty = 0;

        /* Create a new IMultiLanguageEditor */
        IMultiLanguageEditor editor = new IMultiLanguageEditor() {

            public void languageSelected(Language lang) {
                if (currentCardView != null)
                    currentCardView.languageSelected(lang);
            }

            public boolean needsTranslation(Language lang) {
                return currentCardView != null && currentCardView.needsTranslation(lang);
            }

            public void updatePrimaryLanguage(Language lang) {
                if (currentCardView != null)
                    currentCardView.updatePrimaryLanguage( lang );
            }
        };

        /* Create a new language bar for choosing languages */
        languageBar = new LanguageBar(this, editor, model.getLanguages(), model.getLanguages().get(0));

        /* Add a language select listener */
        languageBar.addLanguageSelectListener(new Observer() {
            public void update(Observable o, Object arg) {
                previewPanel.clear();
            }
        } );

        /* Add the language bar to the form */
        rightSide.add(languageBar, c2);

        /* Create a new split pane with divider, position it, and add it to the form */
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, listPanel, rightSide);
        splitPane.setDividerLocation(300);
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weighty = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.BOTH;
        add(splitPane, c);

        /* Initialise the menu bar */
        initializeMenuBar();

        /* Set the menu bar */
        setJMenuBar(menuBar);

        /* Initialise the popup menu */
        initializePopupMenu();

        /* Add a new title observer */
        titleObserver = new Observer() {
            public void update(Observable o, Object arg) {
                if (languageBar.getCurrentLanguage().equals(
                    model.getLanguages().get(0))) {
                    int idx = cardList.getSelectedIndex();
                    setCardTitle(model.getCardTitle(idx), idx);
                    validate();
                    repaint();
                }
            }
        };

        /* Set up a new file chooser */
        fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
    }

    /**
     * Action when the add card button is pressed: pops the add card menu up so
     * the user can choose which type
     */
    public void addCardButtonPressed() {
        addCardMenu.show(addCardButton, 0, -(int) addCardMenu.getPreferredSize().getHeight());
    }
    
    /**
     * Action when the add card button is pressed: pops the add card menu up so
     * the user can choose which type
     */
    public void prefButtonPressed() {
        prefMenu.show(prefButton, 0, (int) prefButton.getPreferredSize().getHeight());
    }

    /**
     * Adds the language change listener
     * 
     * @param l         the listener
     */
    public void addLanguageChangeListener(LanguageChangeListener l) {
        languageChangeListeners.add(l);
    }

    /**
     * Action for when the selected card has changed: Update the card panel to
     * show the new card
     */
    public void cardListSelectionChanged() {

        /* Get the index of the selected card */
        int idx = cardList.getSelectedIndex();

        /* If there was no card selected, then disable everything */
        if (idx == -1) {
            setCardPane(noCardsPanel);
            deleteCardButton.setEnabled(false);
            moveUpCardButton.setEnabled(false);
            moveDownCardButton.setEnabled(false);
        }
        /* Otherwise, set the card pane based on the selection and enable buttons */
        else {
            setCardPane(new CardView(this, model.getCardType(idx), model.getCardModules(idx)));
            deleteCardButton.setEnabled(true);
            moveUpCardButton.setEnabled((idx > 0));
            moveDownCardButton.setEnabled((idx < cardListModel.size() - 1));
        }
    }

    /**
     * Action for when the user presses the delete card button: Confirms with a
     * popup, then deletes the card and removes from the list
     */
    public void deleteCardButtonPressed() {

        /* Prompt after action */
        String prompt = "Are you sure you want to delete this card?";
        int answer = JOptionPane.showConfirmDialog(this, prompt, "Delete Card", JOptionPane.YES_NO_OPTION);

        /* Check the result of the prompt and, if yes, delete the card */
        if (answer == JOptionPane.YES_OPTION) {

            /* Figure out which card was selected */
            int idx = cardList.getSelectedIndex();

            /* If it's the last card, set the second to last card as selected -- otherwise, set the next card */
            int newIdx = (idx == cardListModel.size() - 1) ? idx - 1 : idx + 1;
            cardList.setSelectedIndex(newIdx);

            /* Delete the originally selected card */
            model.deleteCard(idx);

            /* Re-enable the Straight Party option if it gets removed */
            String removed = cardListModel.remove(idx);

            if(removed.contains("Straight Party"))
                addCardMenu.getComponent(idx).setEnabled(true);
        }
    }

    /**
     * Action for when the user presses the exit button (the X in the top right,
     * or the quit menu option): Confirms and gives the user a chance to save,
     * then exits
     */
    @SuppressWarnings("StatementWithEmptyBody")
    public void exitButtonPressed() {

        /* Prompt after action */
        String prompt = "If you exit, any changes you have made since the last time you saved the current ballot will be lost.  Save?";
        int answer = JOptionPane.showConfirmDialog(this, prompt, "Exit", JOptionPane.YES_NO_CANCEL_OPTION);

        /* Check the result of the prompt and, if yes, save and exit*/
        if (answer == JOptionPane.YES_OPTION) {
            boolean saved = saveBallotButtonPressed();
            if (saved)
                System.exit(0);
        }
        /* If not, just exit */
        else if (answer == JOptionPane.NO_OPTION) {
            System.exit(0);
        }
        /* If cancel, do nothing -- explicitly do not exit */
        else if (answer == JOptionPane.CANCEL_OPTION);
    }

    /**
     * Action for when the user presses the export button: Asks for a location,
     * then forwards to the model (a progress dialog is popped up during this
     * long-running process)
     */
    public void exportButtonPressed() {

        try {

            /* Check translations */
            String[] cardsNeedTranslation = model.checkTranslations();

            /* If more than 0 cards need translations */
            if (cardsNeedTranslation.length > 0) {

                /* Tell the user that they are missing translations and... */
                String body = "You have not entered translations for all text in this ballot.\n\nThe following cards are missing translations:\n";

                for (String s : cardsNeedTranslation)
                    body += s + "\n";

                body += "\nContinue exporting?";

                /* ...ask if they want to continue exporting */
                int answer = JOptionPane.showConfirmDialog(this, body, "Export", JOptionPane.YES_NO_OPTION);

                /* If the answer is no, return -- otherwise, continue */
                if (answer == JOptionPane.NO_OPTION) return;
            }

            /* Ask the user if he wants to export as a ZIP file - will be removed once the runtime supports ballots in ZIP format */
            String prompt = "Would you like to export the ballot as a ZIP file?";
            int answer = JOptionPane.showConfirmDialog(this, prompt, "Export as ZIP", JOptionPane.YES_NO_OPTION);

            /* If the answer is yes */
            if (answer == JOptionPane.YES_OPTION) {

                /* Open a file chooser and show zip files */
            	JFileChooser fc = fileChooser;

                fc.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        String path = f.getAbsolutePath();
                        return (f.isDirectory() || path.length() > 4 && path.substring(path.length() - 4).equals(".zip"));
                    }

                    @Override
                    public String getDescription() {
                        return "Ballot export files";
                    }
                } );

                /* See what is pressed */
                answer = fc.showDialog(this, "Export");

                /* If approval, then export the file as a zip */
                if (answer == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    model.exportAsZip(this, file.getAbsolutePath());
                }
            }
            /* If the answer is no */
            else {

                /* Set up a file chooser to only show directories */
            	JFileChooser fc = fileChooser;
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                /* See what is pressed */
                answer = fc.showDialog(this, "Export");

                /* If approval, then export */
                if (answer == JFileChooser.APPROVE_OPTION) {

                    File file = fc.getSelectedFile();
                    //noinspection ResultOfMethodCallIgnored
                    file.mkdirs();
                    model.export(this, file.getAbsolutePath());
                }
            }
        }
        catch (BallotExportException e) {

            /* Pop up an error message if there's a problem */
            String error = "An error occurred while exporting the ballot.\nPlease verify the directory is writable, and try again.";
            JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);

        }
    }

    /**
     * Fires a languages changed event
     */
    public void fireLanguagesChanged(ArrayList<Language> languages) {

        /* Set the languages */
        model.setLanguages(languages);

        /* Cycle through the cards and set the card title based on the card title */
        /* TODO maybe this should be in the model */
        for (int i = 0; i < model.getNumCards(); i++)
            setCardTitle(model.getCardTitle(i), i);

        /* Calls each of the listeners so they know languages have been changed */
        for (LanguageChangeListener l : languageChangeListeners)
            l.languagesChanged(languages);
    }

    /**
     * Action for when the user presses the move card down button: Moves the
     * current card down one in the list
     */
    public void moveDownCardButtonPressed() {

        /* Find the card that has been selected */
        int idx = cardList.getSelectedIndex();

        /* Check that the selected card is not the last one */
        if (idx < cardListModel.size() - 1) {

            /* Move the card down */
            int newIdx = idx + 1;
            model.moveCard(idx, newIdx);

            /* Invalidate */
            invalidate();

            /* Remove the old card position */
            String element = cardListModel.remove(idx);

            /* Add the element to the model at newIdx */
            cardListModel.add(newIdx, element);

            /* Set the new selected index to newIdx*/
            cardList.setSelectedIndex(newIdx);

            /* Validate */
            validate();

        }
    }

    /**
     * Action for when the user presses the move card up button: Moves the
     * current card up one in the list
     */
    public void moveUpCardButtonPressed() {

        /* Find the card that has been selected */
        int idx = cardList.getSelectedIndex();

        /* Check that the selected card is not the first one */
        if (idx > 0) {

            /* Move the card up */
            int newIdx = idx - 1;
            model.moveCard(idx, newIdx);

            /* Invalidate */
            invalidate();

            /* Remove the card from the old position */
            String element = cardListModel.remove(idx);

            /* Add the element to the model at newIdx */
            cardListModel.add(newIdx, element);

            /* Set the selected index to newIdx */
            cardList.setSelectedIndex(newIdx);

            /* Validate */
            validate();

        }
    }

    /**
     * Action for when the user presses the new ballot button: Confirms, and
     * then loads a fresh ballot
     */
    public void newBallotButtonPressed() {

        /* Prompt after action */
        String prompt = "If you start a new ballot, any changes you have made since the last time you saved the current ballot will be lost.  Continue?";
        int answer = JOptionPane.showConfirmDialog(this, prompt, "New Ballot", JOptionPane.YES_NO_OPTION);

        /* Check if the answer is yes */
        if (answer == JOptionPane.YES_OPTION) {

            /* Start a new ballot */
            model.newBallot();

            /* Remove all elements from the model */
            cardListModel.removeAllElements();

            /* Set the card pane to an empty panel */
            setCardPane(noCardsPanel);

            /* Fire a languages changed event */
            fireLanguagesChanged(model.getLanguages());

            /* Enable the first component in the add card menu */
            addCardMenu.getComponent(0).setEnabled(true);
        }


    }

    /**
     * Action for when the user presses the open ballot button: Pops up a file
     * chooser, then loads the ballot
     */
    public void openBallotButtonPressed() {

        try {

            /* Prompt user after action */
            String prompt = "If you open a ballot, any changes you have made since the last time you saved the current ballot will be lost.  Continue?";
            int answer = JOptionPane.showConfirmDialog(this, prompt, "Open Ballot", JOptionPane.YES_NO_OPTION);

            /* Check if the answer is yes */
            if (answer == JOptionPane.YES_OPTION) {

                /* Open a new file chooser */
                JFileChooser fc = fileChooser;

                /* Only show .bal files */
                fc.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        String path = f.getAbsolutePath();
                        return (f.isDirectory() || (path.length() > 4 && path.substring(path.length() - 4).equals(".bal")));
                    }

                    @Override
                    public String getDescription() {
                        return "Ballot files";
                    }
                } );

                /* See what was pressed */
                answer = fc.showOpenDialog(this);

                /* If approval... */
                if (answer == JFileChooser.APPROVE_OPTION) {

                    /* Get the file an*/
                    File file = fc.getSelectedFile();
                    model.open(file.getAbsolutePath());
                    cardListModel.removeAllElements();

                    /* Add the card title to the card list model */
                    for (int i = 0; i < model.getNumCards(); i++)
                        cardListModel.addElement(model.getCardTitle(i));

                    /* Set the selected index based on the size of the card list model */
                    int set = cardListModel.size() > 0 ? 0 : -1;
                    cardList.setSelectedIndex(set);

                    /* Enable straight party based on the model */
                    boolean sp = !cardListModel.get(0).contains("Straight Party");
                    addCardMenu.getComponent(0).setEnabled(sp);
                }

                /* Fire a languages changed event */
                fireLanguagesChanged(model.getLanguages());
            }
        }
        catch (BallotOpenException e) {

            /* If there is a problem opening the ballot, pop up an error dialog */
            String error = "An error occurred while opening the ballot.\nPlease verify the file is not corrupt, and try again.";
            JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Action for when the user presses the preview button: Exports to a
     * temporary directory and then launches VoteBox
     */
    public void previewButtonPressed() {

        /* Try to preview the ballot */
        try { model.previewBallot(this); }
        catch (BallotPreviewException e) {

            /* If there is a problem previewing the ballot, pop up an error dialog */
            String error = "An error occurred while previewing the ballot.";
            JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE );
        }
    }

    /**
     * Action for when the user presses the save ballot button: Asks for a
     * location (confirming if overwrite), then saves the ballot
     */
    public boolean saveBallotButtonPressed() {

        try {

            /* Open a file chooser */
        	JFileChooser fc = fileChooser;

            /* Show only .bal files */
            fc.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    String path = f.getAbsolutePath();
                    return (f.isDirectory() || path.length() > 4 && path.substring(path.length() - 4).equals(".bal"));
                }

                @Override
                public String getDescription() {
                    return "Ballot files";
                }
            } );

            /* See what was pressed */
            int answer = fc.showSaveDialog(this);

            /* If approval... */
            if (answer == JFileChooser.APPROVE_OPTION) {

                /* Get the file */
                File file = fc.getSelectedFile();

                /* See if the file already exists */
                if (file.exists()) {

                    /* Prompt the user to overwrite */
                    String prompt = "The file you selected already exists. Overwrite?";
                    answer = JOptionPane.showConfirmDialog(this, prompt, "Overwrite Saved Ballot", JOptionPane.YES_NO_OPTION);

                    /* If yes, go ahead and save the file */
                    if (answer == JOptionPane.YES_OPTION) {
                        model.saveAs(file.getAbsolutePath());
                        return true;
                    }
                }

                /* If the file doesn't exist, go ahead and save */
                else {
                    model.saveAs(file.getAbsolutePath());
                    return true;
                }
            }
        }
        catch (BallotSaveException e) {

            /* Popup an error message if there was a problem saving the ballot */
            String error = "An error occurred while saving the ballot.\nPlease verify the directory is writable, and try again.";
            JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
        }

        return false;
    }

    /**
     * Sets the card pane to the given card view
     * 
     * @param panel         the card view
     */
    public void setCardPane(CardView panel) {

        /* Recast and handle as a JPanel */
        setCardPane((JPanel) panel);

        /* Set the current view to be the panel */
        currentCardView = panel;

        /* Refresh the editor language */
        languageBar.refreshEditorLanguage();

        /* Get the cards and pull out the selected card */
        ACard selected = model.getBallot().getCards().get(cardList.getSelectedIndex());

        /* Add an observer to the card */
        selected.addModuleObserver("Title", titleObserver);
    }

    /**
     * Sets the card pane to a generic panel
     * 
     * @param panel         the panel
     */
    public void setCardPane(JPanel panel) {

        /* Clear out the current card view */
        currentCardView = null;

        /* Clear the card panel */
        cardPanel.removeAll();

        /* Set up a layout */
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;

        /* Add a panel to the card panel*/
        cardPanel.add(panel, c);

        /* Clear the preview panel */
        if (previewPanel != null) previewPanel.clear();

        /* Validate and repaint */
        validate();
        repaint();
    }

    /**
     * Sets the card title in the card list
     * 
     * @param title         the new title
     * @param idx           the index
     */
    public void setCardTitle(String title, int idx) {

        /* Check if the title is empty and set the element accordingly */
        String set = title.equals("") ? "<untitled>" : title;
        cardListModel.setElementAt(set, idx);
    }

    /**
     * Action for when the user chooses to edit the languages from any language
     * bar: Pops up a dialog allowing the user to select the languages offered
     * in the ballot
     */
    public void showLanguageDialog() {

        /* Pop up a new language dialog */
        LanguagesDialog dialog = new LanguagesDialog(this, Language.getAllLanguages(), model.getLanguages());
        dialog.setVisible(true);

        /* Check if the ok button was pressed and if so, fire a languages changed event */
        if (dialog.okButtonWasPressed())
            fireLanguagesChanged(dialog.getSelectedLanguages());
    }

    /**
     * Action for when the user presses the parties button: Pops up a dialog
     * allowing the user to edit the parties in the ballot
     */
    public ArrayList<Party> showPartiesDialog() {

        /* Pup up a new parties dialog */
        PartiesDialog dialog = new PartiesDialog(this, model.getParties(), model.getLanguages(), languageBar.getCurrentLanguage());
        dialog.setVisible(true);

        return model.getParties();
    }

    /**
     * Initializes the card panel, the no cards panel, and the preview panel
     */
    private void initializeCardPanel() {

        /* Create a new panel and set the border and layout */
        cardPanel = new JPanel();
        cardPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        cardPanel.setLayout(new GridBagLayout());

        /* Create a new panel, set the layout */
        noCardsPanel = new JPanel();
        noCardsPanel.setLayout(new GridBagLayout());

        /* Create a new layout and add a label to the panel */
        GridBagConstraints c2 = new GridBagConstraints();
        c2.gridx = 0;
        c2.gridy = 0;
        noCardsPanel.add(new JLabel("This ballot is currently empty (it does not contain any races)."), c2);

        /* Position and add a new label to the panel */
        c2.gridy = 1;
        noCardsPanel.add(new JLabel("To get started, click on the '+' button in the lower left corner of the screen."), c2);

        /* Set the card pane to the empty panel */
        setCardPane(noCardsPanel);

        /* Create a new PreviewPane */
        previewPanel = new PreviewPane(new IPreviewPaneGenerator() {

            public ArrayList<JPanel> getPreviewPanels() {

                ArrayList<JPanel> preview = model.previewCard(cardList.getSelectedIndex(), languageBar.getCurrentLanguage());
                return (cardList.getSelectedIndex() != -1) ? preview :  new ArrayList<JPanel>();
            }
        } );

        /* Set a bevel border to the panel */
        previewPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    }

    /**
     * Initializes the list panel: the card list and the buttons associated with it
     */
    private void initializeListPanel() {

        /* Create a new panel and set the layout */
        listPanel = new JPanel();
        listPanel.setLayout(new GridBagLayout());

        /* Create a new model and list */
        cardListModel = new DefaultListModel<>();
        cardList = new JList<>(cardListModel);
        cardList.setDragEnabled(true);

        /* Housekeeping for the list */
        ListTransferHandler lth = new ListTransferHandler();
        lth.addListTransferListener( new ListTransferListener() {
            public void listItemMoved(int from, int to) {
                model.moveCard(from, to);
            }
        } );

        cardList.setTransferHandler(lth);
        cardList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cardList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                cardListSelectionChanged();
            }
        });

        /* Create a new scroll pane and constraints */
        JScrollPane cardListScrollPane = new JScrollPane(cardList);
        GridBagConstraints c = new GridBagConstraints();

        /* Position and add the listPanel */
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        listPanel.add(cardListScrollPane, c);

        /* Create a new toolbar */
        listToolbar = new JToolBar();
        listToolbar.setFloatable(false);

        ImageIcon icon;
        String text;

        /* Try and set the image and text */
        try {
            icon = loadImage("list-add.png");
            text = "";
        }
        catch (Exception e) {
            icon = null;
            text = "Add";
        }

        /* Create the add card button */
        addCardButton = new JButton(new AbstractAction(text, icon) {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                addCardButtonPressed();
            }
        } );

        /* Add the add card button */
        listToolbar.add(addCardButton);

        /* Try and set the image and text */
        try {
            icon =loadImage("list-remove.png");
            text = "";
        }
        catch (Exception e) {
            icon = null;
            text = "Remove";
        }

        /* Create the delete card button */
        deleteCardButton = new JButton(new AbstractAction(text, icon) {

            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                deleteCardButtonPressed();
            }

        } );

        deleteCardButton.setEnabled(false);

        /* Add the delete button */
        listToolbar.add(deleteCardButton);

        /* Try and set the image and text */
        try {
            icon = loadImage("go-up.png");
            text = "";
        }
        catch (Exception e) {
            icon = null;
            text = "Up";
        }

        /* Create the move up card button */
        moveUpCardButton = new JButton(new AbstractAction(text, icon) {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                moveUpCardButtonPressed();
            }
        } );

        moveUpCardButton.setEnabled(false);

        /* Add the move up card button */
        listToolbar.add(moveUpCardButton);

        /* Try to set the image and text */
        try {
            icon = loadImage("go-down.png");
            text = "";
        }
        catch (Exception e) {
            icon = null;
            text = "Down";
        }

        /* Create the move down card button */
        moveDownCardButton = new JButton(new AbstractAction(text, icon) {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                moveDownCardButtonPressed();
            }
        } );

        moveDownCardButton.setEnabled(false);

        /* Add the move down card button */
        listToolbar.add(moveDownCardButton);
    }

    /**
     * Initializes the menu bar
     */
    private void initializeMenuBar() {
        menuBar = new JMenuBar();
        ImageIcon icon;

        /* Not all platforms use Control as the shortcut keymask */
        int shortcutKeyMask = java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

        /* Create a new file menu */
        JMenu fileMenu = new JMenu("File");

        /* Create a "new ballot" menu item and add it to the file menu */
        JMenuItem newBallotMenuItem = new JMenuItem(newBallotButton.getAction());
        newBallotMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, shortcutKeyMask));
        fileMenu.add(newBallotMenuItem);

        /* Create an "open ballot" menu item and add it to the file menu */
        JMenuItem openBallotMenuItem = new JMenuItem(openBallotButton.getAction());
        openBallotMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, shortcutKeyMask));
        fileMenu.add(openBallotMenuItem);

        /* Create a "save ballot" menu item and add it to the file menu */
        JMenuItem saveBallotMenuItem = new JMenuItem(saveBallotButton.getAction());
        saveBallotMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, shortcutKeyMask));
        fileMenu.add(saveBallotMenuItem);

        /* Add a separator */
        fileMenu.addSeparator();

        /* Create new "export ballot" menu item and add it to the file menu */
        JMenuItem exportBallotMenuItem = new JMenuItem(exportBallotButton.getAction());
        fileMenu.add(exportBallotMenuItem);

        /* Create a new "preview" menu item and add it to the file menu */
//        JMenuItem previewMenuItem = new JMenuItem(previewButton.getAction());
//        fileMenu.add(previewMenuItem);

        /* Add a separator */
        fileMenu.addSeparator();

        /* Try to load the image */
        icon = loadImage("system-log-out.png");
        

        /* Create a new "quit" menu item and add it to the file menu */
        JMenuItem quitMenuItem = new JMenuItem("Quit", icon);
        quitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exitButtonPressed();
            }
        } );
        quitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, shortcutKeyMask));
        fileMenu.add(quitMenuItem);

        /* Add the file menu to the menu bar */
        menuBar.add(fileMenu);

        /* Create a new "edit" menu and "cut" menu item */
        JMenu editMenu = new JMenu("Edit");
        JMenuItem cutMenuItem = new JMenuItem(new DefaultEditorKit.CutAction());
        cutMenuItem.setText("Cut");

        /* Try to load the image */
        icon = loadImage("edit-cut.png");
        

        /* Set the icon and try to add this to the edit menu */
        cutMenuItem.setIcon(icon);
        cutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, shortcutKeyMask));
        editMenu.add(cutMenuItem);

        /* Create a new "copy" menu item */
        JMenuItem copyMenuItem = new JMenuItem(new DefaultEditorKit.CopyAction());
        copyMenuItem.setText("Copy");

        /* Try to load the image */
        icon = loadImage("edit-copy.png");
        

        /* Set the icon and add this to the edit menu */
        copyMenuItem.setIcon(icon);
        copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, shortcutKeyMask));
        editMenu.add(copyMenuItem);

        /* Create a new "paste" menu item */
        JMenuItem pasteMenuItem = new JMenuItem(new DefaultEditorKit.PasteAction());
        pasteMenuItem.setText("Paste");

        /* Try to load the image */
        icon = loadImage("edit-paste.png");
        

        /* Set the icon and add this to the edit menu */
        pasteMenuItem.setIcon(icon);
        pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, shortcutKeyMask));
        editMenu.add(pasteMenuItem);

        /* Add the edit menu to the menu bar */
        menuBar.add(editMenu);
    }

    /**
     * Initializes the add card popup menu
     */
    private void initializePopupMenu() {

        /* Create a new popup menu */
        addCardMenu = new JPopupMenu();

        /* Go through each of the factories in the model */
        for (final ICardFactory fac : model.getCardFactories()) {

            /* Create a new menu item based on the factory's menu items */
            final JMenuItem item = new JMenuItem(fac.getMenuString());

            item.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {

                    ACard card = fac.makeCard();

                    int idx;

                    /* Make PartyCards always first so they will be displayed at the beginning of a voting session */
                    if(card instanceof PartyCard){

                        idx = 0;

                        /*
                          For reasons stemming from implementation, it helps to update the data within the card before
                          adding it to the pane, so its name is actually displayed.
                        */
                        card.getReviewBlankText(languageBar.getPrimaryLanguage());

                        /* TODO Should parties automatically be added to the card? */
                        model.addCardAtFront(card);
                        cardListModel.insertElementAt("", 0);

                        /* Only allow for one straight party card at a time */
                        item.setEnabled(false);

                    }
                    else {
                        model.addCard(card);
                        cardListModel.addElement("");
                        idx = model.getNumCards() - 1;
                    }

                    setCardTitle(model.getCardTitle(idx), idx);
                    cardList.setSelectedIndex(idx);
                }
            } );
            addCardMenu.add(item);
        }
    }

    /**
     * Initializes the main toolbar
     */
    private void initializeToolbar() {

        /* Create a new toolbar */
        toolbar = new JToolBar();
        toolbar.setFloatable(false);



        String newImage = "document-new.png";
        String openImage = "document-open.png";
        String saveImage= "document-save.png";
        String exportImage = "media-flash.png";
        String previewImage = "system-search.png";
        String preferencesImage = "system-options.png";

        ImageIcon icon;

        /* Try to load the new icon */
        icon = loadImage(newImage);

        /* Create a "new ballot" button */
        newBallotButton = new JButton(new AbstractAction("New Ballot", icon) {

                    private static final long serialVersionUID = 1L;

                    public void actionPerformed(ActionEvent e) {
                        newBallotButtonPressed();
                    }
        } );

        /* Add the "new ballot" button to the toolbar */
        toolbar.add(newBallotButton);

        /* Try to load the image */
        icon = loadImage(openImage);
        

        /* Create a "open ballot" button */
        openBallotButton = new JButton(new AbstractAction("Open Ballot", icon) {

                    private static final long serialVersionUID = 1L;

                    public void actionPerformed(ActionEvent e) {
                        openBallotButtonPressed();
                    }
        } );

        /* Add the "open ballot" button to the toolbar */
        toolbar.add(openBallotButton);

        /* Try to load the image */
        icon = loadImage(saveImage);

        /* Create a "save ballot" button */
        saveBallotButton = new JButton(new AbstractAction("Save Ballot", icon) {

                    private static final long serialVersionUID = 1L;

                    public void actionPerformed(ActionEvent e) {
                        saveBallotButtonPressed();
                    }
        } );

        /* Add the "save ballot" button to the toolbar */
        toolbar.add(saveBallotButton);

        /* Try to load the image */
        icon = loadImage(exportImage);

        /* Create a "export" button */
        exportBallotButton = new JButton(new AbstractAction("Export to VoteBox", icon) {

            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                exportButtonPressed();
            }

        } );

        /* Add the "export" button to the toolbar */
        toolbar.add(exportBallotButton);

        /* Try to load the image */
//        icon = loadImage(previewImage);
//
//        /* Create a "preview" button */
//        previewButton = new JButton(new AbstractAction("Preview in VoteBox", icon) {
//
//            private static final long serialVersionUID = 1L;
//            public void actionPerformed(ActionEvent e) {
//                previewButtonPressed();
//            }
//
//        } );
//
//        /* FIXME This button is annoying, so disable it for now */
//        previewButton.setEnabled(false);
//
//        /* Add the "preview" button to the toolbar */
//        toolbar.add(previewButton);

        /* Try to load the image */
        icon = loadImage(preferencesImage);

        /* Create a "preferences" button */
        prefButton = new JButton(new AbstractAction("Preferences", icon ) {

            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                prefButtonPressed();
            }

        } );

        /* Add the "preferences" button to the toolbar */
        toolbar.add(prefButton);
    }

    /**
     * Gets the list of parties from the model
     */
    public ArrayList<Party> getParties() {
        return model.getParties();
    }
    
    private void initializePrefMenu() {

        /* Setup */
    	final JFrame frame = this;

        /* Create a new popup menu */
    	prefMenu = new JPopupMenu();

        /* Create a new menu item for the number of races per review card */
    	final JMenuItem races = new JMenuItem("Number of Races per Review Card (Current: " + model.getCardsPerReviewPage() + ")");

        /* Create a new action listener for the menu item */
    	races.addActionListener(new ActionListener() {

    		public void actionPerformed(ActionEvent e) {

                /* Prompt the user for their preferred number of displayed races */
                String prompt = "How many races would you like displayed on each review page?";
    			String temp = (String) JOptionPane.showInputDialog(frame, prompt, "", JOptionPane.PLAIN_MESSAGE, null, null, "10");

                /* Check that the response isn't null */
    			if (temp != null) {

                    /* Set the preferred number of displayed races */
                    model.setCardsPerReviewPage(Integer.parseInt(temp));

                    /* Set the text based on the change */
                    races.setText("Number of Races per Review Card (Current: " + model.getCardsPerReviewPage() + ")");
                }
    		}
    	} );

        /* Add the races menu item to the preference menu */
    	prefMenu.add(races);

        /* Create a new menu item for the font */
    	final JMenuItem font = new JMenuItem("Base Font Size (Current: " + model.getBaseFontSize()+")");

        /* Create a new action listener for the menu item */
    	font.addActionListener(new ActionListener() {

    		public void actionPerformed(ActionEvent e) {

                /* Prompt the user for their preferred font size */
                String prompt = "Please enter your desired font size:";
    			String temp = (String)JOptionPane.showInputDialog(frame, prompt, "", JOptionPane.PLAIN_MESSAGE, null, null, "8");

                /* Check that the response isn't null */
    			if (temp != null) {

                    /* Set the preferred font size */
                    model.setFontSize(Integer.parseInt(temp));

                    /* Set the text based on the change */
                    font.setText("Base Font Size (Current: " + model.getBaseFontSize() + ")");
                }
    		}
    	} );

        /* Add the font menu item to the preference menu */
    	prefMenu.add(font);

        /* Create a new check box menu item for using text-to-speech */
        final JCheckBoxMenuItem sound = new JCheckBoxMenuItem("Use Text-to-Speech");

        /* Set text-to-speech to true */
        sound.setState(true);
        model.setTextToSpeech(true);

        /* Add an action listener for the menu item */
        sound.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {

                model.setTextToSpeech(sound.getState());
            }
        });

        /* Add the text-to-speech menu item to the preference menu */
        prefMenu.add(sound);
    }

    public static ImageIcon loadImage(String name) {

        try {


            String resourcePath = "rsrc/preptool/images/";
            if(!View.class.getResource("View.class").toString().contains("jar"))
                return new ImageIcon(resourcePath + name);

            ZipFile file = new ZipFile(System.getProperty("user.dir") + "/Preptool.jar");
            Enumeration<? extends ZipEntry> entries = file.entries();

            /* Cycle through all the entries */
            while (entries.hasMoreElements()) {

                ZipEntry entry = entries.nextElement();

                /* Make sure it's the type of file we want */
                if (entry.getName().endsWith(".png") && entry.getName().contains(resourcePath + name)) {

                   return new ImageIcon(ImageIO.read(file.getInputStream(entry)));
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }
}
