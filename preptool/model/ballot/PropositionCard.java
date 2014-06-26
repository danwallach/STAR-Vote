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

package preptool.model.ballot;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import preptool.model.XMLTools;
import preptool.model.ballot.module.PropositionModule;
import preptool.model.ballot.module.TextAreaModule;
import preptool.model.ballot.module.TextFieldModule;
import preptool.model.language.Language;
import preptool.model.language.LiteralStrings;
import preptool.model.layout.manager.ALayoutManager;
import preptool.model.layout.manager.ALayoutManager.ICardLayout;
import votebox.middle.Properties;

import java.util.ArrayList;
import java.util.List;

/**
 * PropositionCard is the implementation of an ACard that constitutes a
 * proposition.
 *
 * @author Corey Shaw
 */
public class PropositionCard extends ACard {

    /**
     * Factory to create a PropositionCard
     */
    public static final ICardFactory FACTORY = new ICardFactory() {


        /**
         * @see ICardFactory#getMenuString()
         */
        public String getMenuString() {
            return "Add Proposition";
        }

        /**
         * @see ICardFactory#makeCard()
         */
        public ACard makeCard() {
            return new PropositionCard();
        }

    };

    /**
     * Constructs a new PropositionCard
     */
    public PropositionCard() {
        super("Proposition");

        /* This card will have a title field, and a description field, along with yes/no/abstain choices*/
        modules.add(new TextFieldModule("Title", "Title"));
        modules.add(new TextAreaModule("Description", "Description"));
        modules.add(new PropositionModule("PropOpts"));
    }

    /**
     * @see preptool.model.ballot.ACard#assignUIDsToBallot(preptool.model.layout.manager.ALayoutManager)
     */
    @Override
    public void assignUIDsToBallot(ALayoutManager manager) {

        /* Assign a topmost UID to this card */
        setUID(manager.getNextBallotUID());

        /* Iterate through all the elements of the PropositionModule to assign UIDs*/
        PropositionModule optionsModule = (PropositionModule) getModuleByName("PropOpts");
        for (CardElement ce : optionsModule.getData()) {
            ce.setUID(manager.getNextBallotUID());
        }
    }

    /**
     * The no selection review text for this card will look like:
     *
     *              Proposition title text: NONE
     *
     * @see preptool.model.ballot.ACard#getReviewBlankText(preptool.model.language.Language)
     */
    @Override
    public String getReviewBlankText(Language language) {
        return LiteralStrings.Singleton.get("NONE", language);
    }

    /**
     * The selected review text for this card will look like one of the following:
     *
     *              Proposition title text: Yes
     *              Proposition title text: No
     *              Proposition title text: None of the Above
     *
     * @see preptool.model.ballot.ACard#getReviewTitle(preptool.model.language.Language)
     */
    @Override
    public String getReviewTitle(Language language) {
        TextFieldModule titleModule = (TextFieldModule) getModuleByName("Title");
        return titleModule.getData(language) + ":";
    }

    /**
     * @see preptool.model.ballot.ACard#layoutCard(preptool.model.layout.manager.ALayoutManager, preptool.model.layout.manager.ALayoutManager.ICardLayout)
     */
    @Override
    public ICardLayout layoutCard(ALayoutManager manager, ICardLayout cardLayout) {
        Language lang = manager.getLanguage();

        /* Layout the textual information for the title */
        TextFieldModule titleModule = (TextFieldModule) getModuleByName("Title");
        cardLayout.setTitle(titleModule.getData(lang));

        /* Layout the text for the description, which further elaborates on the title (the proposition) */
        TextAreaModule descriptionModule = (TextAreaModule) getModuleByName("Description");
        cardLayout.setDescription(descriptionModule.getData(lang));

        /* Now layout each option */
        PropositionModule optionsModule = (PropositionModule) getModuleByName("PropOpts");
        for (CardElement ce : optionsModule.getData()) {
            cardLayout.addCandidate(ce.getUID(), ce.getName(lang, 0));
        }

        return cardLayout;
    }

    /**
     * @see preptool.model.ballot.ACard#getCardData(preptool.model.language.Language)
     */ /* TODO Is this okay? */
    public ArrayList<String> getCardData(Language language) {
        return null;
    }

    /**
     * @see preptool.model.ballot.ACard#toXML(org.w3c.dom.Document)
     */
    @Override
    public Element toXML(Document doc) {
    	Element cardElt = super.toXML(doc);

    	List<String> ids = new ArrayList<>();

        /* We will artificially insert a no selection option, with the same UID as the card itself */
        int id = Integer.parseInt(this.getUID().substring(1)) ;
        ids.add("B" + Integer.toString(id));

        /* XML each of the options */
        PropositionModule optionsModule = (PropositionModule) getModuleByName("PropOpts");
        for (CardElement ce : optionsModule.getData()) {
            Element cardElementElt = ce.toXML(doc);
            cardElt.appendChild(cardElementElt);
            
            ids.add(ce.uniqueID);
        }

        /* Need to carry the grouping of these candidates together for NIZK purposes. */
        XMLTools.addListProperty(doc, cardElt, Properties.RACE_GROUP, "String", ids.toArray(new String[ids.size()]));
        
        return cardElt;
    }
}
