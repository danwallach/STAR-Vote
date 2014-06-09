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


import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import preptool.model.XMLTools;
import preptool.model.ballot.module.CandidatesModule;
import preptool.model.ballot.module.TextFieldModule;
import preptool.model.language.Language;
import preptool.model.language.LiteralStrings;
import preptool.model.layout.manager.ALayoutManager;
import preptool.model.layout.manager.ALayoutManager.ICardLayout;
import votebox.middle.Properties;

/**
 * PresidentialRaceCard is the implementation of an ACard that constitutes a
 * race between candidates that have running mates.
 *
 * @author Corey Shaw, Mircea C. Berechet
 */
public class PresidentialRaceCard extends ACard {

    /**
     * Factory to create a PresidentialRaceCard
     */
    public static final ICardFactory FACTORY = new ICardFactory() {

        /**
         * @see ICardFactory#getMenuString()
         */
        public String getMenuString() {
            return "Add Presidential Race";
        }

        /**
         * @see ICardFactory#makeCard()
         */
        public ACard makeCard() {
            return new PresidentialRaceCard();
        }

    };

    /**
     * Constructs a new RaceCard
     */
    public PresidentialRaceCard() {
        super("Presidential Race");

        /* This card will have a title, two race title fields, and then a candidates module with 3 columns */
        modules.add(new TextFieldModule("Title", "Title"));
        modules.add(new TextFieldModule("TopOfTicket", "Top Of Ticket"));
        modules.add(new TextFieldModule("BottomOfTicket", "Bottom Of Ticket"));
        modules.add(new CandidatesModule("Candidates", new String[]{
                "Candidate's Name", "Running Mate's Name", "Party" }, true));
    }


    /**
     * @see preptool.model.ballot.ACard#assignUIDsToBallot(preptool.model.layout.manager.ALayoutManager)
     */
    @Override
    public void assignUIDsToBallot(ALayoutManager manager) {
        /* Set the top level UID for this card */
        setUID(manager.getNextBallotUID());

        /* Assign UID's too all of this card's candidatesModule components */
        CandidatesModule candidatesModule = (CandidatesModule) getModuleByName("Candidates");
        for (CardElement ce : candidatesModule.getData()) {
            ce.setUID(manager.getNextBallotUID());
        }
    }

    /**
     * The blank review text here will be:
     *              TopRaceName: NONE
     *              BotRaceName: NONE
     *
     * @see preptool.model.ballot.ACard#getReviewBlankText(preptool.model.language.Language)
     */
    @Override
    public String getReviewBlankText(Language language) {
        return LiteralStrings.Singleton.get("NONE", language) + " \n "
                + LiteralStrings.Singleton.get("NONE", language);
    }

    /**
     * The review text for the selected candidates will be:
     *              TopRaceName: TopCandidateName
     *              BotRaceName: BotCandidateName
     * @see preptool.model.ballot.ACard#getReviewTitle(preptool.model.language.Language)
     */
    @Override
    public String getReviewTitle(Language language) {
        TextFieldModule topOfTicket = (TextFieldModule) getModuleByName("TopOfTicket");
        TextFieldModule bottomOfTicket = (TextFieldModule) getModuleByName("BottomOfTicket");
        return topOfTicket.getData(language) + " \n "
                + bottomOfTicket.getData(language) + ":";
    }

    /**
     * @see preptool.model.ballot.ACard#layoutCard(preptool.model.layout.manager.ALayoutManager, preptool.model.layout.manager.ALayoutManager.ICardLayout)
     */
    @Override
    public ICardLayout layoutCard(ALayoutManager manager, ICardLayout cardLayout) {
        Language lang = manager.getLanguage();

        /* Layout the title info for this card */
        TextFieldModule titleModule = (TextFieldModule) getModuleByName("Title");
        cardLayout.setTitle(titleModule.getData(lang));

        /* Layout the candidates based on the candidatesModule's code*/
        CandidatesModule candidatesModule = (CandidatesModule) getModuleByName("Candidates");
        for (CardElement ce : candidatesModule.getData()) {
            cardLayout.addCandidate(ce.getUID(), ce.getName(lang, 0), ce
                    .getName(lang, 1), ce.getParty().getAbbrev(lang));
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

        /* We will build an array of XML entries recursively */
    	List<String> ids = new ArrayList<String>();
    	
        CandidatesModule candidatesModule = (CandidatesModule) getModuleByName("Candidates");

        /* We will artificially insert a no selection option, with the same UID as the card itself */
        int id = Integer.parseInt(this.getUID().substring(1)) ;
        ids.add("B" + Integer.toString(id));

        for (CardElement ce : candidatesModule.getData()) {

            /* Delegate to each element for what XML should be written */
            Element cardElementElt = ce.toXML(doc);
            cardElt.appendChild(cardElementElt);
            
            ids.add(ce.uniqueID);
        }

        /* Need to carry the grouping of these candidates together for NIZK purposes. */
        XMLTools.addListProperty(doc, cardElt, Properties.RACE_GROUP, "String", ids.toArray(new String[ids.size()]));

        return cardElt;
    }
}
