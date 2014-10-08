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

package votebox.middle.ballot;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import sexpression.ASExpression;
import sexpression.StringExpression;
import sexpression.ListExpression;
import votebox.middle.IncorrectTypeException;
import votebox.middle.Properties;


/**
 * 
 * The Card class is the model's representation for a piece of the ballot.
 * Because, in the model, ballots can be logically divided into races, a Card
 * most generally represents one race. However, it can also represent other
 * pieces of the ballot, such as informational paragraphs about the election or
 * directions for using the voting machine.< para>
 * 
 * However, we mandate that a Card contains at most one race.
 * 
 */

public class Card {
    /**
     * These are the card elements that this card is parent to.
     */
    private ArrayList<SelectableCardElement> _elements = new ArrayList<SelectableCardElement>();

    /**
     * This is the card's unique id.
     */
    private String _uniqueID;

    /**
     * This is the ballot that this card belongs to.
     */
    private Ballot _parent;

    /**
     * This denotes whether this card has a write-in or not
     */
    private boolean _hasWriteIn;

    /**
     * The value of the write in, if it exists
     */
    private String writeInValue;

    /**
     * This is the card's strategy. It determines when to allow selections. The
     * default strategy is RadioButton.
     */
    private ACardStrategy _strategy = RadioButton.Singleton;

    /**
     * These are the properties that have been defined for this Card.
     */
    private Properties _properties = new Properties();

    /**
     * Creates a new card.
     *
     * @param uniqueid      the UID of the Card
     * @param properties    the Properties associated with the card
     * @param elements      the Elements contained within the card
     * @param hasWriteIn    whether the @Card has a write-in
     */
    public Card(String uniqueid, Properties properties, ArrayList<SelectableCardElement> elements, boolean hasWriteIn) {

        _properties = properties;
        _uniqueID = uniqueid;
        _elements = elements;
        _hasWriteIn = hasWriteIn;

        writeInValue = "null";

        /* Set each element's parent card to this instance. */
        for (SelectableCardElement ce : _elements)
            ce.setParent(this);
    }

    /**
     * Call this method to make this Card's strategy agree with what is defined
     * in this Card's properties.
     */
    public void setStrategy() {
        try {

            int k = 0;

            /* Get the value for K if we need it. */
            if (_properties.contains(Properties.K))
                k = _properties.getInteger(Properties.K);

            /* Set the strategy.*/
            if (_properties.contains(Properties.CARD_STRATEGY)) {

                String csString = _properties.getString(Properties.CARD_STRATEGY);

                /* Set strategy based on what String is in _properties */
                _strategy = ("RadioButton"   .equals(csString)) ? RadioButton.Singleton     :
                            ("Kofn"          .equals(csString)) ? new KofN(k)               :
                            ("StraightTicket".equals(csString)) ? new StraightTicket(this)  :
                            _strategy;

            }
            else System.err.println("Strategy not defined for element " + _uniqueID + ". Using radio button.");

        } catch (IncorrectTypeException e) {
            System.err.println("Strategy formatting error detected for element " + _uniqueID + ". Using radio button");
            System.err.println(e.getMessage());
        }
    }

    /**
     * A SelectableCardElement will call this method when it decides that it
     * needs to Select itself.
     * 
     * @param element                   This element wants to Select itself.
     * @throws CardStrategyException    This method throws if its strategy has a problem.
     */
    public boolean select(SelectableCardElement element) throws CardStrategyException {
        return _strategy.select(element);
    }

    /**
     * A SelectableCardElement will call this method when it decides that it
     * needs to Deselect itself.
     * 
     * @param element                   This element wants to Deselect itself.
     * @throws CardStrategyException    This method throws if the card's strategy runs into a problem.
     */
    public boolean deselect(SelectableCardElement element) throws CardStrategyException {
        return _strategy.deselect(element);
    }

    /**
     * @return the Elements on this Card.
     */
    public List<SelectableCardElement> getElements() {
        return _elements;
    }

    /**
     * @return the UID of this Card.
     */
    public String getUniqueID() {
        return _uniqueID;
    }

    /**
     * @return the Parent of this Card.
     */
    public Ballot getParent() {
        return _parent;
    }

    /**
     * @param value sets the new CardStrategy for this Card.
     */
    void setStrategy(ACardStrategy value) {
        _strategy = value;
    }

    /**
     * @return the Properties associated with this Card.
     */
    public Properties getProperties() {
        return _properties;
    }
    
    /**
     * @param card the new parent of this Card
     */
    public void setParent(Ballot card) {
        _parent = card;
    }

    /**
     * Call this method to ask a card which one of its elements is currently
     * selected. This option presumes only one element is selected. If more than
     * one is selected, this method will throw an exception. Call this method
     * knowing that it will lie, if the card property Properties.LIE is set.<br>
     * <br>
     * Possible values for Property.LIE are as follows:<br>
     * <br>
     * "non": If any element is selected, this method will respond that no
     * elements are selected. If no element is selected, this method will
     * respond that the second element is selected.<br>
     * <br>
     * "cand": If the possible selections are interpreted as being linked in a
     * circular list, this method will claim actual selections are, instead,
     * their successors in the list. (1 is 2, 2 is 3, 3 is 1 in a card that has 3
     * elements). If no selection is made, this element simply chooses the
     * second.
     * "last_non": Reports the last element on the page if it is not selected, otherwise
     * responds that the second element is selected (if possible)
     * 
     * @return This method returns the UID of the currently selected card
     *         element, or the correct lie of this if Property.LIE is set. If
     *         this method wishes to communicate that no element is selected in
     *         this card [whether or not this is actually the case (it may not
     *         be due to an intentional lie)] it will return the UID of the
     *         card.
     */
    public String getSelectedElement() throws CardException {

        if (numSelected() > 1)
            throw new CardException(this, "There has been more than one selection made on this card");

        if (_elements.size() == 0)
            return getUniqueID();

        return getSelectedElementNormal();
    }
    
    /**
     * This method implements getSelectedElement truthfully.
     * 
     * @return This method returns the element that really is (in actuality)
     *         selected that is a child of this card.
     */
    private String getSelectedElementNormal() {

        for (SelectableCardElement ce : _elements)
            if (ce.isSelected())
                return ce.getUniqueID();

        return _uniqueID;
    }

    /**
     * Get the number of elements (that are children of this card) that are
     * selected.
     * 
     * @return This method returns the number of selected elements in this card.
     */
    private int numSelected() {

        int numselected = 0;

        for (SelectableCardElement sce : _elements)
            if (sce.isSelected())
                numselected++;

        return numselected;
    }

    /**
     * @return an SExpression representation of this Card.
     */
    public ASExpression toASExpression() {
        ArrayList<ASExpression> elementList = new ArrayList<>();

        try{ elementList.add(StringExpression.makeString(this.getSelectedElement())); }
        catch(Exception e){ e.printStackTrace(); }

        return new ListExpression(StringExpression.makeString(_uniqueID), new ListExpression(elementList));
    }

    /**
     * @return This method returns a conceptual list of BigIntegers which
     *         represents this ballot. A "1" in the list represents a selection,
     *         while a "0" represents no selection. Each of these "counter"
     *         values is paired with the selectable's unique ID that it
     *         represents. These pairs are represented as s-expressions. To get
     *         the BigInteger value back out of the s-expression, use the
     *         BigInteger(String) constructor.
     */
    public List<ASExpression> getCastBallot() {

        ArrayList<ASExpression> lst = new ArrayList<>();

        /* A flag to determine if NO SELECTION was...er...selected. */
        boolean selected = false;

        for (SelectableCardElement sce : _elements) {

            ASExpression val;

            /* If one SCE is selected, set val to "1" StringExpression, otherwise "0" */
            if (sce.isSelected()){

                selected = true;
            	val = StringExpression.makeString(BigInteger.ONE.toString());

            }
            else val = StringExpression.makeString(BigInteger.ZERO.toString());

            /* Add this val as a ListExpression to lst */
            lst.add(new ListExpression(StringExpression.makeString(sce.getUniqueID()), val));
        }

        /*
          If there is a write-in candidate, the last element here will be the write-in field, which needs to
          be updated with the value that was written in, if any, or the default value if not.
        */
        if(_hasWriteIn){

            /* Get the last element as a ListExpression */
            ListExpression last = (ListExpression) lst.get(lst.size() - 1);

            /* Get the first two elements as ASE in the ListExpression */
            ASExpression first = last.get(0);
            ASExpression data = last.get(1);

            /* Create a String to hold the information */
            String modData = data.toString() + writeInValue;

            /* Write this to an ASE */
            ASExpression written = new ListExpression(first, StringExpression.make(modData));

            /* Set this into the lst */
            lst.set(lst.size()-1 , written);
        }

        /**
         * This code allows for a "No Selection" element to be added to the string representation of the ballot
         * This is so the tallier can include (and count) races where no one was selected
         * We do this by including the UID of the no selection candidate, which will always be the lowest number
         * UID for the race. Hence we take out the first candidate and subtract one from the UID to derive the
         * No selection UID
         **/
        String noSelectionLabel = _elements.get(0).getUniqueID();
        noSelectionLabel = noSelectionLabel.substring(0,1) + (Integer.parseInt(noSelectionLabel.substring(1)) - 1);

        lst.add(0, new ListExpression(StringExpression.makeString(noSelectionLabel),
                StringExpression.makeString((selected?BigInteger.ZERO:BigInteger.ONE).toString())));

        return lst;
    }

    /**
     * This method allows us to update the card's write-in value by passing in new values.
     *
     * @param text      the value that was written in for the candidate's name
     * @param secondary the value that was written in for the VP. If it is null, ignore it
     */
    public void setWriteInValue(String text, String secondary) {

        writeInValue = text;

        if(!secondary.equals(""))
            writeInValue += "^" + secondary;   /* Note that the carat here is a delimiter */
    }

    public String getTitleLabelUID() throws IncorrectTypeException {
        return _properties.getString("TitleLabelUID");
    }
}
