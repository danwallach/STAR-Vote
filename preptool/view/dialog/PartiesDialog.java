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

package preptool.view.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import preptool.model.Party;
import preptool.model.language.Language;
import preptool.view.IMovableTableModel;
import preptool.view.IMultiLanguageEditor;
import preptool.view.LanguageBar;
import preptool.view.MovableTableModel;
import preptool.view.View;
import preptool.view.dragndrop.TableTransferHandler;


/**
 * A Dialog that allows the user to edit the parties that are used in the
 * ballot.
 * 
 * @author Corey Shaw
 */
public class PartiesDialog extends JDialog implements IMultiLanguageEditor {

    private static final long serialVersionUID = 1L;

    /**
     * The list of parties
     */
    private ArrayList<Party> parties;

    /**
     * Table model of the parties table
     */
    private MovableTableModel partiesTableModel;

    /**
     * Table to edit the parties
     */
    private JTable partiesTable;

    /**
     * Table model listener
     */
    private TableModelListener tableListener;

    /**
     * Panel that contains the parties table and toolbar
     */
    private JPanel partiesPanel;

    /**
     * Button to add a party
     */
    private JButton addButton;

    /**
     * Button to remove a party
     */
    private JButton deleteButton;

    /**
     * Toolbar for the add/remove buttons
     */
    private JToolBar partiesToolbar;

    /**
     * An OK button
     */
    private JButton okButton;

    /**
     * Panel containing the OK and cancel buttons
     */
    private JPanel buttonPanel;

    /**
     * Whether the OK button was pressed to close this dialog
     */
    private boolean okButtonWasPressed;

    /**
     * Language bar
     */
    private LanguageBar languageBar;

    /**
     * Menu item to copy a candidate from the primary language
     */
    private JMenuItem tableCopyFromItem;

    /**
     * Menu item to copy all candidates from the primary language
     */
    private JMenuItem tableCopyAllFromItem;

    /**
     * Constructs a new PartiesDialog
     * 
     * @param view              the view
     * @param parties           the parties
     * @param languages         the languages
     * @param startLang         the initial language
     */
    public PartiesDialog(View view, ArrayList<Party> parties, ArrayList<Language> languages, Language startLang) {

        /* Make a call to super and set parties*/
        super(view, "Parties", true);
        this.parties = parties;

        /* Setup GUI */
        setSize(400, 400);
        setLocationRelativeTo(view);
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        /* Create a new label for the title and position and add it to the form */
        JLabel titleLabel = new JLabel("Edit Parties:");
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(15, 15, 0, 15);
        c.weightx = 1;
        add(titleLabel, c);

        /* Initialise the table pane and position and add it to the form */
        initializeTablePane(startLang);
        c.gridy = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.fill = GridBagConstraints.BOTH;
        add(partiesPanel, c);

        /* Initialise the buttons and position and add the button panel to the form */
        initializeButtons();
        c.gridy = 2;
        c.insets = new Insets(15, 15, 15, 15);
        c.weighty = 0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.PAGE_END;
        add(buttonPanel, c);

        /* Create a new language bar and position and add it to the form */
        languageBar = new LanguageBar(view, this, languages, startLang);
        languageBar.refreshEditorLanguage();
        c.gridy = 3;
        c.insets = new Insets(0, 0, 0, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        add(languageBar, c);
    }

    /**
     * Initializes the OK button
     */
    private void initializeButtons() {

        /* Create a new button panel */
        buttonPanel = new JPanel();

        /* Create an OK button */
        okButton = new JButton("OK");

        /* Add an action listener to the OK button*/
        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                okButtonWasPressed = true;
                setVisible(false);
            }
        } );

        /* Add the OK button to the button panel */
        buttonPanel.add(okButton);
    }

    /**
     * Initializes the table panel
     */
    private void initializeTablePane(Language lang) {

        /* Create a new panel and layout */
        partiesPanel = new JPanel();
        partiesPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        /* Create a new table listener */
        tableListener = new TableModelListener() {

            public void tableChanged(TableModelEvent e) {

                /* If the model is moved, remove the first row and add it to the last row */
                if (e.getType() == IMovableTableModel.MOVE) {
                    Party p = parties.remove(e.getFirstRow());
                    parties.add(e.getLastRow(), p);
                }

                else {

                    /* Otherwise, go through the rows */
                    for (int i = e.getFirstRow(); i <= e.getLastRow(); i++) {

                        /* If it is an insert event, add a new party at the index */
                        if (e.getType() == TableModelEvent.INSERT) parties.add(i, new Party());

                        /* If an update event */
                        else if (e.getType() == TableModelEvent.UPDATE) {

                            /* Get the party and the name */
                            Party p = parties.get(i);
                            String str = (String) partiesTableModel.getValueAt(i, 0);

                            /* If there is a name, keep the name */
                            if (str != null)
                                p.setName(languageBar.getCurrentLanguage(), str);

                            /* If there is no name, set it to an empty String */
                            else
                                p.setName(languageBar.getCurrentLanguage(), "");

                            /* Pull the abbreviation */
                            str = (String) partiesTableModel.getValueAt(i, 1);

                            /* If there is an abbreviation, keep it -- otherwise, empty String */
                            if (str != null)
                                p.setAbbrev(languageBar.getCurrentLanguage(), str);
                            else
                                p.setAbbrev(languageBar.getCurrentLanguage(), "");
                        }

                        /* In a delete event, remove it */
                        else if (e.getType() == TableModelEvent.DELETE) parties.remove(i);
                    }
                }
            }
        };

        /* Instantiate the table model and add a listener */
        partiesTableModel = new MovableTableModel(new String[] {"Name", "Abbreviation"}, parties.size());
        partiesTableModel.addTableModelListener(tableListener);

        /* Set the language selected */
        languageSelected(lang);

        /* Instantiate the parties table (and some housekeeping) */
        partiesTable = new JTable(partiesTableModel);
        partiesTable.setDragEnabled(true);
        partiesTable.setTransferHandler(new TableTransferHandler());
        partiesTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        /* Add a listener for the table model */
        partiesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) { deleteButton.setEnabled(partiesTable.getSelectedRow() != -1); }
        } );

        /* Create a new popup menu */
        JPopupMenu tableContextMenu = new JPopupMenu();

        /* Create a new menu item for copying (and an action listener for the menu item) */
        tableCopyFromItem = new JMenuItem();
        tableCopyFromItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                /* If something is currently selected */
                if (partiesTable.getSelectedRow() != -1) {

                    /* Get what is currently selected */
                    Party p = parties.get(partiesTable.getSelectedRow());

                    /* Set the name and the abbreviation based on the translation of the primary language */
                    p.setName(languageBar.getCurrentLanguage(), p.getName(languageBar.getPrimaryLanguage()));
                    p.setAbbrev(languageBar.getCurrentLanguage(), p.getAbbrev(languageBar.getPrimaryLanguage()));

                    /* Set the language selected */
                    languageSelected(languageBar.getCurrentLanguage());
                }
            }
        } );

        /* Add the menu item to the menu */
        tableContextMenu.add(tableCopyFromItem);

        /* Create a new menu item for copying everything */
        tableCopyAllFromItem = new JMenuItem();

        /* Add an action listener */
        tableCopyAllFromItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                for (Party p : parties) {
                    p.setName(languageBar.getCurrentLanguage(), p.getName(languageBar.getPrimaryLanguage()));
                    p.setAbbrev(languageBar.getCurrentLanguage(), p.getAbbrev(languageBar.getPrimaryLanguage()));
                }

                languageSelected( languageBar.getCurrentLanguage() );
            }
        } );

        /* Add the menu item to the menu */
        tableContextMenu.add(tableCopyAllFromItem);

        /* Set the component popup menu to the tableContextMenu */
        partiesTable.setComponentPopupMenu(tableContextMenu);

        /* Create a new scroll pane and position and add it to the parties panel */
        JScrollPane tableScrollPane = new JScrollPane(partiesTable);
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.weightx = 1;
        partiesPanel.add(tableScrollPane, c);

        /* Create a new toolbar for parties */
        partiesToolbar = new JToolBar();
        partiesToolbar.setFloatable(false);

        /* Load the image and text (text blank because we only need image)*/
        ImageIcon icon = new ImageIcon("rsrc/preptool/images/list-add.png");
        String text = "";

        /* Create a new button for adding */
        addButton = new JButton(new AbstractAction(text, icon) {
            public void actionPerformed(ActionEvent e) {
                addButtonPressed();
            } } );

        /* Add the button to the toolbar */
        partiesToolbar.add(addButton);

        /* Load the image and text (text blank because we only need image) */
        icon = new ImageIcon("rsrc/preptool/images/list-remove.png");
        text = "";

        /* Create a new button for deleting */
        deleteButton = new JButton(new AbstractAction(text, icon) {
            public void actionPerformed(ActionEvent e) { deleteButtonPressed(); } } );

        /* Disable the button by default and add it to the toolbar */
        deleteButton.setEnabled(false);
        partiesToolbar.add(deleteButton);

        /* Position and add the toolbar to the panel */
        c.gridy = 1;
        c.weighty = 0;
        c.anchor = GridBagConstraints.LAST_LINE_START;
        partiesPanel.add(partiesToolbar, c);

        /* Set the border for the panel */
        partiesPanel.setBorder(BorderFactory.createTitledBorder("Parties"));
    }

    /**
     * Adds a new row to the end of the table
     */
    public void addButtonPressed() { partiesTableModel.addRow(new String[2]); }

    /**
     * Deletes the selected row from this table
     */
    public void deleteButtonPressed() {

        /* Make sure nothing is currently being edited  */
        if (partiesTable.getEditingRow() == -1) {

            /* Prompt the user to be sure to delete */
            String prompt = "Are you sure you want to delete this party?";
            int answer = JOptionPane.showConfirmDialog(this, prompt, "Delete Party", JOptionPane.YES_NO_OPTION);

            /* Check for yes */
            if (answer == JOptionPane.YES_OPTION) {

                /* Get the row that is selected */
                int idx = partiesTable.getSelectedRow();

                /* Check if there are multiple rows */
                if (partiesTableModel.getRowCount() > 1) {

                    int newIdx;

                    /* If currently the last row is selected, the new selected will be one before */
                    if (idx == partiesTableModel.getRowCount() - 1)
                        newIdx = idx - 1;

                    /* Otherwise, the new selected will be the next one */
                    else
                        newIdx = idx + 1;

                    /* Clear the selection */
                    partiesTable.clearSelection();

                    /* Selects the new row */
                    partiesTable.addRowSelectionInterval(newIdx, newIdx);
                }

                /* Remove the row to be deleted */
                partiesTableModel.removeRow(idx);
            }
        }
    }

    /**
     * @return the okButtonWasPressed
     */
    public boolean okButtonWasPressed() {
        return okButtonWasPressed;
    }

    /**
     * Updates this dialog to show the selected language
     */
    public void languageSelected(Language lang) {

        /* Get rid of the current listener */
        partiesTableModel.removeTableModelListener(tableListener);

        /* Go through each of the parties in the model and set the values */
        for (int i = 0; i < parties.size(); i++) {
            partiesTableModel.setValueAt(parties.get(i).getName(lang), i, 0);
            partiesTableModel.setValueAt(parties.get(i).getAbbrev(lang), i, 1);
        }

        /* Read the listener */
        partiesTableModel.addTableModelListener(tableListener);
    }

    /**
     * Returns true if there are missing translations in the list of parties
     */
    public boolean needsTranslation(Language lang) {

        /* Cycle through the parties and make sure there is a translation for each */
        for (Party party : parties)
            if (party.getAbbrev(lang).equals("") || party.getName(lang).equals(""))
                return true;

        /* Return false if no translations needed */
        return false;
    }

    /**
     * Called when the primary language has changed
     */
    public void updatePrimaryLanguage(Language lang) {

        tableCopyFromItem.setText("Copy selected party from " + lang.getName());
        tableCopyAllFromItem.setText("Copy all parties from " + lang.getName());
    }

}
