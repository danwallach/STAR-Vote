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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import preptool.model.Party;
import preptool.model.ballot.CardElement;
import preptool.model.language.Language;
import preptool.model.language.LocalizedString;
import preptool.view.AModuleView;
import preptool.view.IMovableTableModel;
import preptool.view.View;
import preptool.view.dragndrop.TableTransferHandler;


/**
 * A CandidatesModule is a module that can be used on a card to represent the
 * candidates in a race. Candidates are stored as CardElements. The view of this
 * module is a table with the appropriate buttons.
 * 
 * @author cshaw, Mircea C. Berechet
 */
public class CandidatesModule extends AModule {

    /**
     * An inner class for the CandidatesModule's view
     *
     * @author cshaw, Mircea C. Berechet
     */
    private class ModuleView extends AModuleView {

        private static final long serialVersionUID = 1L;

        /**
         * Table model for the CandidatesModule's view. The table model contains
         * references directly to the CandidatesModule's data structures,
         * eliminating the need for the data to be in two places.
         *
         * @author cshaw
         */
        private class CandidateTableModel extends AbstractTableModel implements
                IMovableTableModel {

            private static final long serialVersionUID = 1L;

            public String getSelectionName(Language language, int rowIndex)
            {
                return data.get(rowIndex).getName(language, 0);
            }

            public void addRow() {
                data.add( new CardElement( columns ) );
                fireTableRowsInserted( data.size(), data.size() );
                setChanged();
                notifyObservers();
            }

            public void addWriteInRow() {
                /* Create a mapping of language name to CardElement name. */
                HashMap<String, String> writeInNames = CardElement.writeInNames;
                /* Create a new CardElement with the name based on the language information defined above. */
                CardElement writeInCardElement = new CardElement( columns );
                /* Get the list of all the languages. */
                ArrayList<Language> languages = Language.getAllLanguages();
                /* Set the name based on the language. */
                for (Language language : languages)
                {
                    for (int columnIndex = 0; columnIndex < columns; columnIndex++)
                    {
                        if (writeInNames.keySet().contains(language.getName()))
                        {
                            writeInCardElement.setColumn(language, columnIndex, writeInNames.get(language.getName()));
                        }
                        else
                        {
                            writeInCardElement.setColumn(language, columnIndex, "MISSING TRANSLATION INFORMATION");
                        }
                    }
                }
                /* The card element will be distinguished from a regular candidate by its name being one of the LocalizedStrings defined in CardElement. */
                data.add(writeInCardElement);
                fireTableRowsInserted( data.size(), data.size() );
                setChanged();
                notifyObservers();
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == columns)
                    return Party.class;
                else
                    return String.class;
            }

            public int getColumnCount() {
                return colNames.length;
            }

            @Override
            public String getColumnName(int column) {
                return colNames[column];
            }

            public int getRowCount() {
                return data.size();
            }

            public Object getValueAt(int row, int column) {
                return data.get( row ).getColumn( language, column );
            }

            public void insertRow(int row) {
                data.add( row, new CardElement( columns ) );
                fireTableRowsInserted( row, row );
                setChanged();
                notifyObservers();
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }

            public void moveRow(int from, int to) {
                CardElement element = data.remove( from );
                data.add( to, element );
                fireTableChanged( new TableModelEvent( this, from, to,
                        TableModelEvent.ALL_COLUMNS, MOVE ) );
                setChanged();
                notifyObservers();
            }

            public void removeRow(int row) {
                data.remove( row );
                fireTableRowsDeleted( row, row );
                setChanged();
                notifyObservers();
            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                data.get( row ).setColumn( language, column, aValue );
                fireTableCellUpdated( row, column );
                setChanged();
                notifyObservers();
            }

        }

        /**
         * Renderer for a Party object in a ComboBox or a Table
         */
        private class PartyRenderer extends JLabel implements
                TableCellRenderer, ListCellRenderer {

            private static final long serialVersionUID = 1L;

            /**
             * Constructs a new PartyRenderer
             */
            public PartyRenderer() {
                setOpaque( true );
                setHorizontalAlignment( LEFT );
                setVerticalAlignment( CENTER );
            }

            /**
             * Renders a party in a combo box with the name of the party (in the
             * current language)
             */
            public Component getListCellRendererComponent(JList list,
                    Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {

                if (isSelected) {
                    setBackground( list.getSelectionBackground() );
                    setForeground( list.getSelectionForeground() );
                }
                else {
                    setBackground( list.getBackground() );
                    setForeground( list.getForeground() );
                }

                // Get the selected index. (The index param isn't
                // always valid, so just use the value.)
                if (!(value instanceof Party)) {
                    setText( value.toString() );
                    return this;
                }
                Party party = (Party) value;

                if (party != null)
                    setText( party.getName( language ) + " " );
                else
                    setText( " " );

                return this;
            }

            /**
             * Renders a party in a table with the name of the party (in the
             * current language)
             */
            public Component getTableCellRendererComponent(JTable table,
                    Object value, boolean isSelected, boolean hasFocus,
                    int row, int column) {
                // Get the selected index. (The index param isn't
                // always valid, so just use the value.)
                if (!(value instanceof Party)) {
                    setText( value.toString() );
                    return this;
                }
                Party party = (Party) value;

                if (party != null)
                    setText( party.getName( language ) + " " );
                else
                    setText( " " );

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
         * Menu item to copy a candidate from the primary language
         */
        private JMenuItem tableCopyFromItem;

        /**
         * Menu item to copy all candidates from the primary language
         */
        private JMenuItem tableCopyAllFromItem;

        private Language language;

        private Language primaryLanguage;

        /**
         * Constructs a new ModuleView with the given main view
         *
         * @param view
         */
        protected ModuleView(View view, boolean enableWriteIn) {
            this.view = view;
            setLayout( new GridBagLayout() );
            setBorder( BorderFactory.createTitledBorder( "Candidates" ) );
            GridBagConstraints c = new GridBagConstraints();

            allParties = view.getParties();

            initializeCandidatesTable();
            c.gridx = 0;
            c.gridy = 0;
            c.fill = GridBagConstraints.BOTH;
            c.weighty = 1;
            c.weightx = 1;
            add( candidatesScrollPane, c );

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
            if (candidatesTable.getEditingRow() == -1) {
                int answer = JOptionPane.showConfirmDialog( this.getRootPane(),
                    "Are you sure you want to delete this row?",
                    "Delete row", JOptionPane.YES_NO_OPTION );
                if (answer == JOptionPane.YES_OPTION) {
                    int idx = candidatesTable.getSelectedRow();
                    /* Write-in Check: If the candidate to be deleted is a write-in, then re-enable the Add Write-In button. */
                    String candidateNameToBeDeleted = (String) tableModel.getValueAt(idx, 0);
                    if (isWriteInCandidate(candidateNameToBeDeleted))
                    {
                        addWriteInCandidateButton.setEnabled(true);
                    }
                    /* End Write-In Check. */
                    if (tableModel.getRowCount() > 1) {
                        int newIdx;
                        if (idx == tableModel.getRowCount() - 1)
                            newIdx = idx - 1;
                        else
                            newIdx = idx + 1;
                        candidatesTable.clearSelection();
                        candidatesTable
                                .addRowSelectionInterval( newIdx, newIdx );
                    }
                    tableModel.removeRow( idx );
                }
            } else if(candidatesTable.isRowSelected(candidatesTable.getSelectedRow())){
                //This is necessary with the Parties card, as there are no text fields to selected
                int answer = JOptionPane.showConfirmDialog( this.getRootPane(),
                        "Are you sure you want to delete this row?",
                        "Delete row", JOptionPane.YES_NO_OPTION );
                if (answer == JOptionPane.YES_OPTION) {
                    int idx = candidatesTable.getSelectedRow();
                    if (tableModel.getRowCount() > 1) {
                        int newIdx;
                        if (idx == tableModel.getRowCount() - 1)
                            newIdx = idx - 1;
                        else
                            newIdx = idx + 1;
                        candidatesTable.clearSelection();
                        candidatesTable
                                .addRowSelectionInterval( newIdx, newIdx );
                    }
                    tableModel.removeRow( idx );
                }
            }
        }

        /**
         * Updates the fields with the information for the new language
         */
        public void languageSelected(Language lang) {
            language = lang;
            updatePartyDropDown();
            validate();
            repaint();
        }

        /**
         * Moves the current candidate down one
         */
        public void moveDownCandidateButtonPressed() {
            if (candidatesTable.getEditingRow() == -1) {
                int idx = candidatesTable.getSelectedRow();
                if (idx < tableModel.getRowCount() - 1) {
                    int newIdx = idx + 1;
                    // card.moveCandidate(idx, newIdx);
                    tableModel.moveRow( idx, newIdx );
                    candidatesTable.clearSelection();
                    candidatesTable.addRowSelectionInterval( newIdx, newIdx );
                }
            }
        }

        /**
         * Moves the current candidate up one
         */
        public void moveUpCandidateButtonPressed() {
            if (candidatesTable.getEditingRow() == -1) {
                int idx = candidatesTable.getSelectedRow();
                if (idx > 0) {
                    int newIdx = idx - 1;
                    // card.moveCandidate(idx, newIdx);
                    tableModel.moveRow( idx, newIdx );
                    candidatesTable.clearSelection();
                    candidatesTable.addRowSelectionInterval( newIdx, newIdx );
                }
            }
        }

        /**
         * Returns whether or not the CandidatesModule that this view
         * corresponds to is missing any translations
         */
        public boolean needsTranslation(Language lang) {
            return CandidatesModule.this.needsTranslation( lang );
        }

        /**
         * Updates the list of parties in the drop down
         */
        public void updatePartyDropDown() {
            TableColumn column = candidatesTable.getColumnModel().getColumn(
                columns );
            final JComboBox dropDown = new JComboBox();
            dropDown.setRenderer( new PartyRenderer() );
            dropDown.addItem( Party.NO_PARTY );
            for (int i = 0; i < allParties.size(); i++) {
                dropDown.addItem( allParties.get( i ) );
            }
            dropDown.addItem( "Edit..." );
            dropDown.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (dropDown.getSelectedItem().equals( "Edit..." )) {
                        allParties = view.showPartiesDialog();
                        updatePartyDropDown();
                    }
                }
            } );

            column.setCellEditor( new DefaultCellEditor( dropDown ) );
            column.setCellRenderer( new PartyRenderer() );
        }

        /**
         * Called when the primary language has changed
         */
        public void updatePrimaryLanguage(Language lang) {
            primaryLanguage = lang;
            tableCopyFromItem.setText( "Copy selected candidate from "
                    + lang.getName() );
            tableCopyAllFromItem.setText( "Copy all candidates from "
                    + lang.getName() );
        }

        /**
         * Initializes the candidates table with the data from the card
         */
        private void initializeCandidatesTable() {
            tableModel = new CandidateTableModel();
            candidatesTable = new JTable( tableModel );
            candidatesTable.setDragEnabled( true );
            candidatesTable.setTransferHandler( new TableTransferHandler() );
            candidatesTable.getSelectionModel().setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION );
            candidatesTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        int idx = candidatesTable.getSelectedRow();
                        if (idx == -1) {
                            deleteCandidateButton.setEnabled( false );
                            moveUpCandidateButton.setEnabled( false );
                            moveDownCandidateButton.setEnabled( false );
                        }
                        else {
                            deleteCandidateButton.setEnabled( true );
                            moveUpCandidateButton.setEnabled( (idx > 0) );
                            moveDownCandidateButton
                                    .setEnabled( (idx < tableModel
                                            .getRowCount() - 1) );
                        }
                    }
                } );
            JPopupMenu tableContextMenu = new JPopupMenu();
            tableCopyFromItem = new JMenuItem();
            tableCopyFromItem.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (candidatesTable.getSelectedRow() != -1) {
                        int idx = candidatesTable.getSelectedRow();
                        data.get( idx ).copyFromPrimary( language,
                            primaryLanguage );
                        languageSelected( language );
                    }
                }
            } );
            tableContextMenu.add( tableCopyFromItem );
            tableCopyAllFromItem = new JMenuItem();
            tableCopyAllFromItem.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    for (int idx = 0; idx < data.size(); idx++)
                        data.get( idx ).copyFromPrimary( language,
                            primaryLanguage );
                    languageSelected( language );
                }
            } );
            tableContextMenu.add( tableCopyAllFromItem );
            candidatesTable.setComponentPopupMenu( tableContextMenu );

            candidatesScrollPane = new JScrollPane( candidatesTable );
        }

        /**
         * Initializes the candidates toolbar (the add, remove, etc buttons)
         */
        private void initializeCandidatesToolbar(boolean enableWriteIn) {
            candidatesToolbar = new JToolBar();
            candidatesToolbar.setFloatable( false );

            ImageIcon icon;
            String text;

            try {
                icon = new ImageIcon( ClassLoader.getSystemClassLoader()
                        .getResource( "images/list-add.png" ) );
                text = "";
            }
            catch (Exception e) {
                icon = null;
                text = "Add";
            }
            addCandidateButton = new JButton( new AbstractAction( text, icon ) {
                private static final long serialVersionUID = 1L;

                public void actionPerformed(ActionEvent e) {
                    addCandidateButtonPressed();
                }
            } );
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
                // Add the option to write-in a candidate, if the card is not a Party Card.
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
                    String candidateName = (String) tableModel.getSelectionName(Language.getLanguageForName("English"), currentIndex);
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
     * @param elt
     *            the Element
     * @return the CandidatesModule
     */
    public static CandidatesModule parseXML(Element elt) {
        assert elt.getTagName().equals( "Module" );
        assert elt.getAttribute( "type" ).equals( "CandidatesModule" );
        String name = elt.getAttribute( "name" );
        String writeInEnabledString = elt.getAttribute( "writein" );
        boolean enableWriteIn = writeInEnabledString.equals("true");
        ArrayList<String> columns = new ArrayList<String>();

        NodeList list = elt.getElementsByTagName( "Column" );
        for (int i = 0; i < list.getLength(); i++) {
            Element child = (Element) list.item( i );
            columns.add( child.getAttribute( "name" ) );
        }

        CandidatesModule module = new CandidatesModule( name, columns
                .toArray( new String[columns.size()] ) , enableWriteIn);

        list = elt.getElementsByTagName( "CardElement" );
        for (int i = 0; i < list.getLength(); i++) {
            Element child = (Element) list.item( i );
            module.data.add( CardElement.parseXML( child, columns.size() - 1 ) );
        }

        return module;
    }

    private ArrayList<CardElement> data;

    private int columns;

    private String[] colNames;

    private boolean writeInEnabled = false;

    /**
     * Constructs a new CandidatesModule with the given module name and names of
     * the columns. The number of candidates is assumed to be the number of
     * column names less one, as the last column should always be the Party.
     * 
     * @param name
     *            the module name
     * @param colNames
     *            column names
     */
    public CandidatesModule(String name, String[] colNames, boolean allowWriteIn) {
        super( name );
        data = new ArrayList<CardElement>();
        columns = colNames.length - 1;
        this.colNames = colNames;
        this.writeInEnabled = allowWriteIn;
    }

    /**
     * Generates and returns the table view for this module
     */
    @Override
    public AModuleView generateView(View view) {
        return new ModuleView( view , writeInEnabled );
    }

    /**
     * Returns the data as an array of CardElements
     */
    public ArrayList<CardElement> getData() {
        return data;
    }

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
     * @param doc
     *            the document
     * @return the Element
     */
    @Override
    public Element toSaveXML(Document doc) {
        Element moduleElt = doc.createElement( "Module" );
        moduleElt.setAttribute( "type", "CandidatesModule" );
        moduleElt.setAttribute( "name", getName() );
        moduleElt.setAttribute( "writein", writeInEnabled ? "true" : "false" );

        for (String col : colNames) {
            Element columnElt = doc.createElement( "Column" );
            columnElt.setAttribute( "name", col );
            moduleElt.appendChild( columnElt );
        }

        for (CardElement ce : data)
            moduleElt.appendChild( ce.toSaveXML( doc ) );

        return moduleElt;
    }

}
