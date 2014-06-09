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

package preptool.model.ballot.module;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import preptool.model.Party;
import preptool.model.ballot.CardElement;
import preptool.model.language.Language;
import preptool.view.AModuleView;
import preptool.view.IMovableTableModel;
import preptool.view.View;
import preptool.view.dragndrop.TableTransferHandler;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * A CandidatesModule is a module that can be used on a card to represent the
 * candidates in a race. Candidates are stored as CardElements. The view of this
 * module is a table with the appropriate buttons.
 * 
 * @author Corey Shaw, Mircea C. Berechet
 */ /* TODO Find a better way to handle:  if (candidatesTable.getSelectedRow() != -1) */
public class CandidatesModule extends AModule {

    /**
     * An inner class for the CandidatesModule's view
     *
     * @author Corey Shaw, Mircea C. Berechet
     */
    private class ModuleView extends AModuleView {


        /**
         * Table model for the CandidatesModule's view. The table model contains
         * references directly to the CandidatesModule's data structures,
         * eliminating the need for the data to be in two places.
         *
         * @author Corey Shaw
         */
        private class CandidateTableModel extends AbstractTableModel implements IMovableTableModel {

            /**
             * Returns the name of the object at the given row, in the given language
             *
             * @param language the language the name should be returned in
             * @param rowIndex the row containing the desired name
             * @return the name of the object in the specified row
             */
            public String getSelectionName(Language language, int rowIndex) {
                return data.get(rowIndex).getName(language, 0);
            }

            /**
             * Adds a row to the module, so a new candidate can be entered
             */
            public void addRow() {
                data.add( new CardElement( columns ) );
                fireTableRowsInserted( data.size(), data.size() );
                setChanged();
                notifyObservers();
            }

            /**
             * Adds a row containing a write-in candidate. We have to do a little
             * extra gyration to get this to work properly.
             */
            public void addWriteInRow() {

                /* Create a mapping of language name to CardElement name. */
                HashMap<String, String> writeInNames = CardElement.writeInNames;

                /* Create a new CardElement with the name based on the language information defined above. */
                CardElement writeInCardElement = new CardElement( columns );

                /* Get the list of all the languages. */
                ArrayList<Language> languages = Language.getAllLanguages();

                /* Set the name based on the language. */
                for (Language language : languages) {

                    /* Look through each column  */
                    for (int columnIndex = 0; columnIndex < columns; columnIndex++) {

                        /* If we support the given language, set the column in this language to the correct translation */
                        if (writeInNames.keySet().contains(language.getName()))
                            writeInCardElement.setColumn(language, columnIndex, writeInNames.get(language.getName()));

                        /* If we don't support the language, fill the column with a message noting as much */
                        else
                            writeInCardElement.setColumn(language, columnIndex, "MISSING TRANSLATION INFORMATION");
                    }
                }

                /* The card element will be distinguished from a regular candidate by its name being one of the LocalizedStrings defined in CardElement. */
                data.add(writeInCardElement);

                /* Notify the view observer that a row has been inserted */
                fireTableRowsInserted( data.size(), data.size() );
                setChanged();
                notifyObservers();
            }

            /**
             * Returns the type of the data stored in the specified column.
             *
             * @param columnIndex the column whose data type we're trying to get.
             * @return the Party type, if the requested column is that last; otherwise it's a String.
             */
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == columns)
                    return Party.class;
                else
                    return String.class;
            }


            /**
             * @return the number of columns
             */
            public int getColumnCount() {
                return colNames.length;
            }

            /**
             * @param column the columns whose name we want
             * @return the name of the specified columns
             */
            @Override
            public String getColumnName(int column) {
                return colNames[column];
            }

            /**
             * @return the number of rows, i.e. candidates
             */
            public int getRowCount() {
                return data.size();
            }

            /**
             * @param row the row in the table
             * @param column the column in the table
             * @return the value in the specified row and column
             */
            public Object getValueAt(int row, int column) {
                return data.get( row ).getColumn( getLanguage(), column );
            }

            /**
             * @param row inserts a new row after this row
             */
            /* This method is never used. */
            @SuppressWarnings("unused")
            public void insertRow(int row) {
                data.add( row, new CardElement( columns ) );
                fireTableRowsInserted( row, row );
                setChanged();
                notifyObservers();
            }

            /**
             * Queries the specified location to see if it is writable
             * @param row the row in the table
             * @param column the column in the table
             * @return true since all cells are editable in this table
             */
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }

            /**
             * Moves the specified row to the specified location
             *
             * @param from the row to move
             * @param to   the position to move to
             */
            public void moveRow(int from, int to) {
                CardElement element = data.remove( from );
                data.add( to, element );
                //noinspection MagicConstant
                fireTableChanged( new TableModelEvent( this, from, to,
                        TableModelEvent.ALL_COLUMNS, MOVE ) );
                setChanged();
                notifyObservers();
            }

            /**
             * @param row the row to remove
             */
            public void removeRow(int row) {
                data.remove( row );
                fireTableRowsDeleted( row, row );
                setChanged();
                notifyObservers();
            }

            /**
             * Put a value in the table
             *
             * @param aValue the value to put in the table
             * @param row the row in the table to put
             * @param column the column in the table to put
             */
            @Override
            public void setValueAt(Object aValue, int row, int column) {

                /*
                 * Check to make sure we aren't trying to add anything to a non-existent location,
                 * as can happen when deleting a row
                 */
                if(row >= getRowCount() || column > columns) {
                    JOptionPane.showMessageDialog(null, "That is not a valid location in the table");
                    return;
                }

                data.get( row ).setColumn( getLanguage(), column, aValue );
                fireTableCellUpdated( row, column );
                setChanged();
                notifyObservers();
            }

        }

        /**
         * Renderer for a Party object in a ComboBox or a Table
         */
        private class PartyRenderer extends JLabel implements TableCellRenderer, ListCellRenderer {

            /**
             * Constructs a new PartyRenderer
             */
            public PartyRenderer() {
                setOpaque( true );
                setHorizontalAlignment( LEFT );
                setVerticalAlignment( CENTER );
            }


            /**
             * Renders a party in a combo box with the name of the party (in the current language)
             *
             * @param list the list of parties
             * @param value the party object to render
             * @param index the index of the party in the comboBox
             * @param isSelected whether or not the party is currently selected
             * @param cellHasFocus whether or not the party is currently focused
             * @return a rendered component for the party
             */
            public Component getListCellRendererComponent(JList list,
                    Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {

                /* Set the colors to the appropriate selected or not colors */
                if (isSelected) {
                    setBackground( list.getSelectionBackground() );
                    setForeground( list.getSelectionForeground() );
                }
                else {
                    setBackground( list.getBackground() );
                    setForeground( list.getForeground() );
                }

                /* If the component is not a party object, just set the text to its toString representation and return this */
                /* TODO This seems bad... */
                if (!(value instanceof Party))
                    setText(value.toString());
                else {
                    /* Find the party and its translation, then return this component */
                    Party party = (Party) value;
                    setText(party.getName( getLanguage() ) + " ");
                }

                return this;
            }


            /**
             * Renders a party in a table with the name of the party (in the
             * current language)
             * 
             * @param table the context this is getting rendered in
             * @param value the object to render
             * @param isSelected whether or not this is currently selected
             * @param hasFocus whether or not this is currently focused
             * @param row the row in the table
             * @param column the column in the table
             * @return a rendered version of this
             */
            public Component getTableCellRendererComponent(JTable table,
                    Object value, boolean isSelected, boolean hasFocus,
                    int row, int column) {

               /* If the component is not a party object, just set the text to its toString representation and return this */
                /* TODO This seems bad... */
                if (!(value instanceof Party))
                    setText(value.toString());
                else {
                    /* Find the party and its translation, then return this component */
                    Party party = (Party) value;
                    setText(party.getName( getLanguage() ) + " ");
                }

                return this;
            }
        }

        /**
         * The main view
         */
        private View view;

        /**
         * List of parties
         */
        private ArrayList<Party> allParties;

        /**
         * Toolbar with buttons to manipulate the candidates
         */
        private JToolBar candidatesToolbar;

        /**
         * Adds a candidate to the table
         */
        private JButton addCandidateButton;

        /**
         * Adds a write-in candidate to the table
         */
        private JButton addWriteInCandidateButton;

        /**
         * Removes a candidate from the table
         */
        private JButton deleteCandidateButton;

        /**
         * Moves the selected candidate up one
         */
        private JButton moveUpCandidateButton;

        /**
         * Moves the selected candidate down one
         */
        private JButton moveDownCandidateButton;

        /**
         * Scroll pane that holds the table
         */
        private JScrollPane candidatesScrollPane;

        /**
         * Table of the candidates
         */
        private JTable candidatesTable;

        /**
         * Model of the candidates table
         */
        private CandidateTableModel tableModel;

        /**
         * Menu item to copy all candidates from the primary language
         */
        private JMenuItem tableCopyAllFromItem;


        /**
         * Default Preptool language, to be used when the other 2 language variables are not instantiated.
         */
        private Language defaultLanguage = Language.getLanguageForName("English");

        /**
         * Constructs a new ModuleView with the given main view
         *
         * @param view the main view
         * @param enableWriteIn whether or not write-in candidates should be enabled for this view
         */
        protected ModuleView(View view, boolean enableWriteIn) {
            /* Set the new view and its layout */
            this.view = view;
            setLayout( new GridBagLayout() );

            /* Title the view appropriately */
            setBorder( BorderFactory.createTitledBorder( "Candidates" ) );
            GridBagConstraints c = new GridBagConstraints();

            /* Populate the parties list */
            allParties = view.getParties();

            /* Initialize and add the candidates table */
            initializeCandidatesTable();
            c.gridx = 0;
            c.gridy = 0;
            c.fill = GridBagConstraints.BOTH;
            c.weighty = 1;
            c.weightx = 1;
            add( candidatesScrollPane, c );

            /* Initialize and add the toolbar */
            initializeCandidatesToolbar(enableWriteIn);
            c.gridy = 1;
            c.weighty = 0;
            c.anchor = GridBagConstraints.LAST_LINE_START;
            add( candidatesToolbar, c );
        }

        /**
         * Adds a blank candidate to the table
         */
        public void addCandidateButtonPressed() {
            tableModel.addRow();
        }

        /**
         * Adds a write-in candidate to the table
         */
        public void addWriteInCandidateButtonPressed() {
            tableModel.addWriteInRow();
        }

        /**
         * Deletes the currently selected candidate from the table
         */
        public void deleteCandidateButtonPressed() {

            /*
             * If a row is selected, check that it is inside the range of valid row selections, then delete it
             * This is necessary with the Parties card, as there are no text fields to selected
             */
            if (candidatesTable.getEditingRow() == -1 || candidatesTable.isRowSelected(candidatesTable.getSelectedRow())) {

                /* This will give the user the option to delete the row */
                int answer = JOptionPane.showConfirmDialog(this.getRootPane(),
                        "Are you sure you want to delete this row?",
                        "Delete row", JOptionPane.YES_NO_OPTION);

                if (answer == JOptionPane.YES_OPTION) {

                    /* Get the index of the row selected for deletion*/
                    int idx = candidatesTable.getSelectedRow();

                    /* Write-in Check: If the candidate to be deleted is a write-in, then re-enable the Add Write-In button. */
                    /* TODO Check this for consistency with the rest of the write-in UI */
                    String candidateNameToBeDeleted = tableModel.getValueAt(idx, 0).toString();

                    if (isWriteInCandidate(candidateNameToBeDeleted))
                        addWriteInCandidateButton.setEnabled(true);

                    /* End Write-In Check. */

                    /* If this is not the only row in the table */
                    if (tableModel.getRowCount() > 1) {

                        /* Shift the succeeding rows to compensate for the deleted row*/
                        int newIdx;
                        if (idx == tableModel.getRowCount() - 1)
                            newIdx = idx - 1;
                        else
                            newIdx = idx + 1;

                        /* Deselect everything after the delete */
                        candidatesTable.clearSelection();

                        /* Select the nearest row */
                        candidatesTable.addRowSelectionInterval(newIdx, newIdx);
                    }

                    /* Remove the row */
                    tableModel.removeRow(idx);
                }

            }
        }

        /**
         * Updates the fields with the information for the new language
         *
         * @param lang the new language
         */
        public void languageSelected(Language lang) {
            setLanguage(lang);
            updatePartyDropDown();
            validate();
            repaint();
        }

        /**
         * Moves the current candidate down one
         */
        public void moveDownCandidateButtonPressed() {
            /* If none of the rows are currently being edited */
            /* TODO Should this have an else clause, maybe throw up a dialog to tell user to stop editing or something? */
            if (candidatesTable.getEditingRow() == -1) {

                /* Grab the index for the selected row to move and check its validity*/
                int idx = candidatesTable.getSelectedRow();
                if (idx < tableModel.getRowCount() - 1) {

                    /* Increment the row index for the selected rows*/
                    int newIdx = idx + 1;

                    /* Relies on the table's own method to move the row to a new index*/
                    tableModel.moveRow( idx, newIdx );

                    /* clear the selected row */
                    candidatesTable.clearSelection();

                    /* Add back the moved row */
                    candidatesTable.addRowSelectionInterval( newIdx, newIdx );
                }
            }
        }

        /**
         * Moves the current candidate up one
         *
         * @see preptool.model.ballot.module.CandidatesModule.ModuleView#moveDownCandidateButtonPressed()
         */
        public void moveUpCandidateButtonPressed() {
            if (candidatesTable.getEditingRow() == -1) {
                int idx = candidatesTable.getSelectedRow();
                if (idx > 0) {
                    int newIdx = idx - 1;

                    tableModel.moveRow( idx, newIdx );
                    candidatesTable.clearSelection();
                    candidatesTable.addRowSelectionInterval( newIdx, newIdx );
                }
            }
        }

        /**
         * @param lang the language to check translation into
         * @return whether or not the CandidatesModule that this view
         * corresponds to is missing any translations
         */
        public boolean needsTranslation(Language lang) {
            return CandidatesModule.this.needsTranslation( lang );
        }

        /**
         * Updates the list of parties in the drop down
         */
        public void updatePartyDropDown() {
            /* Get the parties column */
            TableColumn column = candidatesTable.getColumnModel().getColumn(columns);

            /* Create a new dropdown to put the parties on */
            final JComboBox<Party> dropDown = new JComboBox<Party>();
            //noinspection unchecked
            dropDown.setRenderer( new PartyRenderer() );

            /* Add a no party (null) option */
            dropDown.addItem( Party.NO_PARTY );

            /* Iterate through the list of parties and add them to the dropdown */
            for (Party party : allParties) {
                dropDown.addItem( party );
            }

            /* Add an edit option that will allow the addition or removal of parties */
            dropDown.addItem(Party.getEditParty());

            /* The only case where we do anything is when the edit option is selected */
            dropDown.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (dropDown.getSelectedItem().equals(Party.getEditParty())) {
                        /* Shown the dialog to add, remove, or edit the parties */
                        allParties = view.showPartiesDialog();

                        /* After editing, update the dropdown */
                        updatePartyDropDown();
                    }
                }
            } );

            /* Set the editors and renderers for the dropdown */
            column.setCellEditor( new DefaultCellEditor( dropDown ) );
            column.setCellRenderer( new PartyRenderer() );
        }

        /**
         * Called when the primary language has changed
         *
         * @param lang the new primary language
         */
        public void updatePrimaryLanguage(Language lang) {
            setPrimaryLanguage(lang);

            /* Sets the copy options for the table in the right-click dropdown*/
            getCopyFromItem().setText("Copy selected candidate from " + lang.getName());
            tableCopyAllFromItem.setText( "Copy all candidates from " + lang.getName() );
        }

        /**
         * Initializes the candidates table with the data from the card
         */
        private void initializeCandidatesTable() {

            /* Initialized the table model and the table*/
            tableModel = new CandidateTableModel();
            candidatesTable = new JTable( tableModel );

            /* Allow dragging within the table and initialize the drag and drop handler */
            candidatesTable.setDragEnabled( true );
            candidatesTable.setTransferHandler( new TableTransferHandler() );

            /* Only allow one thing to be selected at a time in the table */
            candidatesTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION );

            /* Add a selection listener to listen for item selection inside the table */
            candidatesTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        int idx = candidatesTable.getSelectedRow();

                        /*
                         * If the index of the selected row is invalid, then disable the buttons since one of them was pressed
                         * or because there is nothing selectable in the table
                         */
                        if (idx == -1) {
                            deleteCandidateButton.setEnabled( false );
                            moveUpCandidateButton.setEnabled( false );
                            moveDownCandidateButton.setEnabled( false );
                        }

                        /* Otherwise enable the buttons that pertain to the selection and its position */
                        else {
                            deleteCandidateButton.setEnabled( true );
                            moveUpCandidateButton.setEnabled( (idx > 0) );
                            moveDownCandidateButton
                                    .setEnabled( (idx < tableModel
                                            .getRowCount() - 1) );
                        }
                    }
                } );

            /* This allows the module to have right-click functionality */
            JPopupMenu tableContextMenu = new JPopupMenu();

            /* This will be the menu item that handles the "copy from" selection*/
            setCopyFromItem(new JMenuItem());
            getCopyFromItem().addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (candidatesTable.getSelectedRow() != -1) {
                        /* Find the selection's text in the primary language and copy it to this language*/
                        int idx = candidatesTable.getSelectedRow();
                        data.get(idx).copyFromPrimary(getLanguage(), getPrimaryLanguage());
                        languageSelected(getLanguage());
                    }
                }
            });
            tableContextMenu.add( getCopyFromItem() );

            /* This will be the menu item that handles "copy all from" selections*/
            tableCopyAllFromItem = new JMenuItem();
            tableCopyAllFromItem.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    /* Look up all elements on this card and copy their primary language text to this language */
                    for (CardElement cardElement : data)
                        cardElement.copyFromPrimary( getLanguage(),
                            getPrimaryLanguage() );
                    languageSelected( getLanguage() );
                }
            } );
            tableContextMenu.add( tableCopyAllFromItem );

            /* Set the table's right-click popup to the newly created copy menu */
            candidatesTable.setComponentPopupMenu( tableContextMenu );

            /* Now put the table in a new scroll pane */
            candidatesScrollPane = new JScrollPane( candidatesTable );
        }

        /**
         * Initializes the candidates toolbar (the add, remove, etc buttons), with graceful
         * erroring if the required images are not found
         */
        private void initializeCandidatesToolbar(boolean enableWriteIn) {

            /* Initialize the toolbar and lock it in place */
            candidatesToolbar = new JToolBar();
            candidatesToolbar.setFloatable( false );

            /* These will be temporarily used to hold information about toolbar components*/
            ImageIcon icon;
            String text;

            /* TODO Can we clean this up at all? */

            /* Load the add button */
            try {
                icon = new ImageIcon( ClassLoader.getSystemClassLoader()
                        .getResource( "images/list-add.png" ) );
                text = "";
            }

            /* If the add button cannot be loaded, simply set the button's text to "Add" */
            catch (Exception e) {
                icon = null;
                text = "Add";
            }

            /* Set this button to do its intended action */
            addCandidateButton = new JButton( new AbstractAction( text, icon ) {

                public void actionPerformed(ActionEvent e) {
                    addCandidateButtonPressed();
                }
            } );

            /* Set a tooltip and add the button to the toolbar */
            addCandidateButton.setToolTipText("Add");
            candidatesToolbar.add( addCandidateButton );

            try {
                icon = new ImageIcon( ClassLoader.getSystemClassLoader()
                        .getResource( "images/list-remove.png" ) );
                text = "";
            }
            catch (Exception e) {
                icon = null;
                text = "Remove";
            }
            deleteCandidateButton = new JButton(
                    new AbstractAction( text, icon ) {

                        private static final long serialVersionUID = 1L;

                        public void actionPerformed(ActionEvent e) {
                            deleteCandidateButtonPressed();
                        }
                    } );

            /* Initially, there's nothing to delete, so this button cannot be used */
            deleteCandidateButton.setEnabled( false );
            deleteCandidateButton.setToolTipText("Remove");
            candidatesToolbar.add( deleteCandidateButton );

            try {
                icon = new ImageIcon( ClassLoader.getSystemClassLoader()
                        .getResource( "images/go-up.png" ) );
                text = "";
            }
            catch (Exception e) {
                icon = null;
                text = "Up";
            }
            moveUpCandidateButton = new JButton(
                    new AbstractAction( text, icon ) {
                        private static final long serialVersionUID = 1L;

                        public void actionPerformed(ActionEvent e) {
                            moveUpCandidateButtonPressed();
                        }
                    } );
            moveUpCandidateButton.setEnabled( false );
            moveUpCandidateButton.setToolTipText("Up");
            candidatesToolbar.add( moveUpCandidateButton );

            try {
                icon = new ImageIcon( ClassLoader.getSystemClassLoader()
                        .getResource( "images/go-down.png" ) );
                text = "";
            }
            catch (Exception e) {
                icon = null;
                text = "Down";
            }
            moveDownCandidateButton = new JButton( new AbstractAction( text,
                    icon ) {
                private static final long serialVersionUID = 1L;

                public void actionPerformed(ActionEvent e) {
                    moveDownCandidateButtonPressed();
                }
            } );
            moveDownCandidateButton.setEnabled( false );
            moveDownCandidateButton.setToolTipText("Down");
            candidatesToolbar.add( moveDownCandidateButton );

            if (enableWriteIn)
            {
                /* Add the option to write-in a candidate, if the card is not a Party Card. */
                try {
                    icon = new ImageIcon( ClassLoader.getSystemClassLoader()
                            .getResource( "images/list-add-write-in.png" ) );
                    text = "";
                }
                catch (Exception e) {
                    icon = null;
                    text = "Add Write-in";
                }
                addWriteInCandidateButton = new JButton( new AbstractAction( text, icon ) {
                    private static final long serialVersionUID = 1L;

                    public void actionPerformed(ActionEvent e) {
                        addWriteInCandidateButtonPressed();
                        addWriteInCandidateButton.setEnabled(false);
                    }
                } );
                addWriteInCandidateButton.setToolTipText("Add Write-in");
                candidatesToolbar.add( addWriteInCandidateButton );

                /* If there is a write-in candidate already in the race, then disable the button. */
                for (int currentIndex = 0; currentIndex < tableModel.getRowCount(); currentIndex++)
                {
                    /* Write-in Check: If the candidate to be deleted is a write-in, then re-enable the Add Write-In button. */
                    String candidateName = tableModel.getSelectionName(defaultLanguage, currentIndex);
                    if (isWriteInCandidate(candidateName))
                    {
                        addWriteInCandidateButton.setEnabled(false);
                    }
                    /* End Write-In Check. */
                }
            }
        }

        /**
         * Given a candidateName, it returns whether or not the candidate is a Write-In Candidate or not.
         *
         * @param candidateName the name of the candidate
         * @return whether or not this candidate is a Write-In Candidate
         */
        public Boolean isWriteInCandidate (String candidateName)
        {
            /* Create a mapping of language name to Write-in Candidate name. */
            /* This is used to identify names of write-in candidates. */
            HashMap<String, String> writeInNames = new HashMap<String, String>();
            writeInNames.put("English", "Write-In Candidate");
            writeInNames.put("Español", "Escribe el nombre de su selección");
            writeInNames.put("Français", "Écrivez le nom de votre sélection");
            writeInNames.put("Deutsch", "Schreiben Sie die Namen Ihrer Auswahl");
            writeInNames.put("Italiano", "Scrivi il nome della tua selezione");
            writeInNames.put("Русский", "Напишите имя вашего выбора");
            writeInNames.put("中文", "撰写您的选择的名称");
            writeInNames.put("日本語", "あなたの選択の名前を書く");
            writeInNames.put("한국말", "선택의 이름을 작성");
            writeInNames.put("العربية", "كتابة اسم من اختيارك");

            Boolean isWriteIn = false;

            /* Check if the ToggleButton is a write-in candidate, regardless of language. */
            for (Language language : Language.getAllLanguages())
            {
                Boolean isValidLanguage = false;
                /* Make sure that this language is a valid language for which write-in candidates are enabled. */
                for (String languageName : writeInNames.keySet())
                {
                    if (language.getName().equals(languageName))
                    {
                        isValidLanguage = true;
                        break;
                    }
                }

                /* If the language is valid, check the name of the candidate for equality with the default write-in name. */
                if (isValidLanguage && candidateName.equals(writeInNames.get(language.getName())))
                {
                    isWriteIn = true;
                    break;
                }
            }

            return isWriteIn;
        }

    }

    /**
     * Parses this Element into a CandidatesModule
     * 
     * @param elt the XML Element to read from
     * @return the CandidatesModule, from the provided XML
     */
    public static CandidatesModule parseXML(Element elt) {
        /* Ensure that this is actually a CandidatesModule */
        assert elt.getAttribute( "type" ).equals( "CandidatesModule" );

        /* get the name */
        String name = elt.getAttribute( "name" );

        /* Figure out if write-ins are enabled*/
        String writeInEnabledString = elt.getAttribute( "writein" );
        boolean enableWriteIn = writeInEnabledString.equals("true");

        /* Construct the table's columns, based on the names found in the XML */
        ArrayList<String> columns = new ArrayList<String>();
        NodeList list = elt.getElementsByTagName( "Column" );
        for (int i = 0; i < list.getLength(); i++) {
            Element child = (Element) list.item( i );
            columns.add( child.getAttribute( "name" ) );
        }

        /* Now construct the actual argument */
        CandidatesModule module = new CandidatesModule( name, columns.toArray(new String[columns.size()]) , enableWriteIn);

        /* Populate the rest of the module's data based on the rest of the XML */
        list = elt.getElementsByTagName( "CardElement" );
        for (int i = 0; i < list.getLength(); i++) {
            Element child = (Element) list.item( i );
            module.data.add( CardElement.parseXML( child, columns.size() - 1 ) );
        }

        return module;
    }

    /** the CardElements, i.e. candidate info, contained in this module */
    private ArrayList<CardElement> data;

    /** The number of columns this modules has */
    private int columns;

    /** The header for each column */
    private String[] colNames;

    /** whether or not this module supports write-ins */
    private boolean writeInEnabled = false;

    /**
     * Constructs a new CandidatesModule with the given module name and names of
     * the columns. The number of candidates is assumed to be the number of
     * column names less one, as the last column should always be the Party.
     * 
     * @param name the module name
     * @param colNames column names
     */
    public CandidatesModule(String name, String[] colNames, boolean allowWriteIn) {
        super(name);
        data = new ArrayList<CardElement>();
        columns = colNames.length - 1;
        this.colNames = colNames;
        this.writeInEnabled = allowWriteIn;
    }

    /**
     * @return the generated table view for this module
     */
    @Override
    public AModuleView generateView(View view) {
        return new ModuleView( view , writeInEnabled );
    }

    /**
     * @return the data as an array of CardElements
     */
    public ArrayList<CardElement> getData() {
        return data;
    }

    /**
     * @param lang the language to check
     * @return whether or not anything in the table needs translation
     */
    @Override
    public boolean needsTranslation(Language lang) {
        boolean result = false;
        for (CardElement row : data)
            result |= row.needsTranslation( lang );
        return result;
    }

    /**
     * Formats the CandidatesModule as a savable XML Element
     * 
     * @param doc the document
     * @return the XML Element representation of a CandidatesModule
     */
    @Override
    public Element toSaveXML(Document doc) {
        /* Write the header information out */
        Element moduleElt = doc.createElement( "Module" );
        moduleElt.setAttribute( "type", "CandidatesModule" );
        moduleElt.setAttribute( "name", getName() );
        moduleElt.setAttribute( "writein", writeInEnabled ? "true" : "false" );

        /* Now write the column names */
        for (String col : colNames) {
            Element columnElt = doc.createElement( "Column" );
            columnElt.setAttribute( "name", col );
            moduleElt.appendChild( columnElt );
        }

        /* Now write the rest of the card data, i.e. candidate names, parties, etc. */
        for (CardElement ce : data)
            moduleElt.appendChild( ce.toSaveXML( doc ) );

        return moduleElt;
    }

}
