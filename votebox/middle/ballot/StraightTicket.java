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

import votebox.middle.IncorrectTypeException;
import votebox.middle.Properties;
import votebox.middle.driver.DeselectionException;
import votebox.middle.driver.SelectionException;
import votebox.middle.driver.UnknownUIDException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 
 * This is the strategy implementation for straight ticket voting. On a ballot,
 * certain cards provide elements who, when selected, should enact a "special"
 * straight ticket behavior. Namely, the special behavior should be that every
 * card element in the ballot who defines Properties.PARTY to be the same as the
 * selected element should have their Select() method called. StraightTicket
 * decorates RadioButton.
 * 
 */
public class StraightTicket extends ACardStrategy {

    /**
     * Local interface for help in cleaning up some repeated code.
     * Still relatively hackish, but a lot better than copy/paste.
     */
	private interface IStub{
		public void invoke(SelectableCardElement sce) throws CardStrategyException;
	}
	
	/**
	 * For each party, there is a mapping here from the party name to a list of
	 * candidates who belong to that party.
	 */
	private HashMap<String, ArrayList<SelectableCardElement>> _partyAffiliations = null;

	/**
	 * This is a mapping of each party to the button who does the work of
	 * straight ticket for that party.
	 */
	private HashMap<String, SelectableCardElement> _buttons = new HashMap<String, SelectableCardElement>();

	/**
	 * This is the Card who is delegating to this strategy.
	 */
	private Card _card;

	public StraightTicket(Card card) {
		_card = card;
	}

	/**
	 * <summary> Iterate through the ballot and figure out who claims to be
	 * assigned to a party. For those people who do, add them to the correct
	 * list here so we know who to talk to later when the voter expresses intent
	 * for straight-ticket. Also, we need to make sure that everyone who claims
	 * that he belongs to a party, when deselected, also deselects the straight
	 * ticket button for his party. </summary>
	 */
	private void assignParties() throws CardStrategyException {

		_partyAffiliations = new HashMap<>();

		/*
		  For each CardElement in the ballot, we're going to check for a party
		  affiliation and then add it to our data structure.
		*/
		for (Card c : _card.getParent().getCards()) {

            for (SelectableCardElement ce : c.getElements()) {

				/*
				  If:
				   (1) We're not looking at the card who is delegating to this
				       strategy (That is the straight ticket card)
				   (2) The cardElement has set its value for "party"
				*/
				if ((c != _card) && (ce.getProperties().contains(Properties.PARTY))) {

                    String party;

                    try { party = ce.getProperties().getString(Properties.PARTY); }
                    catch (IncorrectTypeException e) {
						throw new CardStrategyException("There was an issue getting the party affiliation of candidate with UID: " + ce.getUniqueID(), e);
					}

					ArrayList<SelectableCardElement> list;

                    /*
                      -If we've encountered this party before, just add to its list.
					  -If we have not encountered this party before, create a new list,
					   Assign it to the party, and then add to that list.
                    */
					if (_partyAffiliations.containsKey(party))
						list = _partyAffiliations.get(party);
					else {
						list = new ArrayList<>();
						_partyAffiliations.put(party, list);
					}

					list.add(ce);
				}
				/*
				  If, instead, !(1), we -are- looking at the straight ticket
				  card, add the element to the straight ticket buttons mapping
				*/
				else if ((c == _card) && (ce != null) && (ce.getProperties().contains(Properties.PARTY))) {

                    try { _buttons.put(ce.getProperties().getString(Properties.PARTY),ce); }
                    catch (IncorrectTypeException e) {
                        throw new CardStrategyException("There was an issue getting the party affiliation of straight ticket button with UID: " + ce.getUniqueID(), e);
					}
				}
			}
		}
	}

    /**
     * Groups Card Elements with their party affiliations
     *
     * @return a Map from party-affiliation String to list of affiliated Card elements
     * @throws CardStrategyException
     */
	private HashMap<String, ArrayList<SelectableCardElement>> getPartyAffiliations() throws CardStrategyException {

		if (_partyAffiliations == null)
			assignParties();

		return _partyAffiliations;
	}

	/**
	 * When this element is selected, before delegating to the radio button
	 * strategy, we need to explicitly select all the elements who belong to the
	 * same party that this selected button belongs to.
     *
	 * @param element   This is the button who was selected. Check the party of
	 *                  this element and then select everyone who belongs to that party.
     *
     * @return          the result of RadioButton.Singleton.select on element
     *
     * @throws CardStrategyException if there isn't a UI element associated with the ballot
     *                               element or some other view error during .select()
	 */
	public boolean select(SelectableCardElement element) throws CardStrategyException {

		String party = getParty(element);

        iterateOverAnd(party, new IStub(){
			public void invoke(SelectableCardElement sce) throws CardStrategyException{
				try { sce.getParentCard().getParent().getViewAdapter().select(sce.getUniqueID()); }
                catch (UnknownUIDException e) {
					throw new CardStrategyException("When executing straight ticket behavior, an error occurred: " + e.getMessage()
									                + " This probably means that there is no ToggleButton defined that corresponds"
                                                    + " with a particular ballot element.", e);
				} catch (SelectionException e) {
					throw new CardStrategyException("When executing straight ticket behavior, an error occurred in the view: " + e.getMessage(), e);
				}
            }
		});
		
		return RadioButton.Singleton.select(element);
	}

	/**
	 * Extracts the party associated with the given element.
	 * 
	 * @param element   the element selected from which to get the party
	 * @return          the associated party
     *
	 * @throws CardStrategyException if the straight ticket party is not declared
     *                               as a String
	 */
	protected String getParty(SelectableCardElement element) throws CardStrategyException{

		try { return element.getProperties().getString(Properties.PARTY); }
        catch (IncorrectTypeException e) {
			throw new CardStrategyException("When extracting the straight ticket party of an element in a straight ticket card, "
                                            + "we found that the property was declared to be the wrong type. Declare this property"
                                            + " as a string.", e);
		}
	}
	
	/**
	 * Traverses elements associated with the given party, and invokes
	 * the provided stub on them.
     *
	 * @param party     party in question
	 * @param stub      stub to invoke
     *
	 * @throws CardStrategyException
	 */
	protected void iterateOverAnd(String party, IStub stub) throws CardStrategyException{

        List<SelectableCardElement> parties;
		
		if ((parties = getPartyAffiliations().get(party)) != null) {

			/* Select all the candidates who belong to this party. */
			for (SelectableCardElement sce : parties)
				stub.invoke(sce);
		}
	}
	
	/**
	 * When this element is deselected, before delegating to the radio button
	 * strategy, we need to explicitly deselect all the elements who belong to
	 * the same party that this selected button belongs to.
     *
	 * @param element   the button who was deselected.
     *
     * @throws CardStrategyException
	 */
	public boolean deselect(SelectableCardElement element) throws CardStrategyException {
		
		String party = getParty(element);

		iterateOverAnd(party, new IStub(){

			public void invoke(SelectableCardElement sce) throws CardStrategyException {

				try { sce.getParentCard().getParent().getViewAdapter().deselect(sce.getUniqueID(), false); }
                catch (UnknownUIDException e) {
					throw new CardStrategyException("When executing straight ticket behavior, could not find an element " +
                                                    "in the view corresponding to a particular ballot ID." + e.getMessage(), e);
				} catch (DeselectionException e) {
					throw new CardStrategyException("When executing straight ticket behavior, an error occurred in the view: " + e.getMessage(), e);
				}
			}
		});
		
		return RadioButton.Singleton.deselect(element);
	}
}