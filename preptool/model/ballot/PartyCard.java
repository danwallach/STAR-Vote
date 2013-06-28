package preptool.model.ballot;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import preptool.model.XMLTools;
import preptool.model.ballot.module.AModule;
import preptool.model.ballot.module.CandidatesModule;
import preptool.model.ballot.module.TextFieldModule;
import preptool.model.ballot.module.TitleModule;
import preptool.model.language.Language;
import preptool.model.language.LiteralStrings;
import preptool.model.layout.manager.ALayoutManager;
import votebox.middle.Properties;

import java.util.ArrayList;
import java.util.List;

/**
 * This card will allow for straight party voting
 * It will present the voter with a card containing a CandidatesModule with party names instead of candidates and no
 * "party" field.
 *
 * @author Matt Bernhard
 * @version 0.0.1
 *          Date: 6/28/13
 */
public class PartyCard extends ACard {

    /**
     * Factory to create a PartyCard
     */
    public static final ICardFactory FACTORY = new ICardFactory() {

        public String getMenuString() {
            return "Add Party";
        }

        public ACard makeCard() {
            return new PartyCard();
        }

    };

    /**
     * Constructs a new Party card
     */
    public PartyCard() {
        super("Party");
        modules.add(new TitleModule("Label", "Straight Party Choices"));
        modules.add(new CandidatesModule("Party", new String[]{ "Party" }));
    }

    /**
     * Assigns the UIDs to this card
     *
     * @param manager the layout manager
     */
    public void assignUIDsToBallot(ALayoutManager manager) {
        setUID(manager.getNextBallotUID());
        CandidatesModule candidatesModule = (CandidatesModule) getModuleByName("Party");
        for (CardElement ce : candidatesModule.getData()) {
            ce.setUID(manager.getNextBallotUID());
        }
    }

    /**
     * Returns the text to show on the review screen if the user has not made a
     * selection on this card
     *
     * @param language the language
     */
    public String getReviewBlankText(Language language) {
        return LiteralStrings.Singleton.get("NONE", language);
    }

    /**
     * Returns the review title for this card
     *
     * @param language the language
     * @return the review title
     */
    public String getReviewTitle(Language language) {
        TitleModule titleModule = (TitleModule) getModuleByName("Label");
        return titleModule.getData(language) + ":";
    }

    /**
     * Lays out this card in the given ICardLayout
     *
     * @param manager    the layout manager
     * @param cardLayout the card layout object
     * @return the finished card layout object
     */
    public ALayoutManager.ICardLayout layoutCard(ALayoutManager manager, ALayoutManager.ICardLayout cardLayout) {
        Language lang = manager.getLanguage();
        TitleModule title = (TitleModule) getModuleByName("Label");
        CandidatesModule candidatesModule = (CandidatesModule) getModuleByName("Party");

        cardLayout.setTitle(title.getData(lang));
        for (CardElement ce : candidatesModule.getData()) {
            cardLayout.addCandidate(ce.getUID(), ce.getParty().getName(lang));
        }
        return cardLayout;
    }

    /**
     * Returns this card's title, by checking to see if there is a title module
     * and returning its data. If there is no title, returns the empty string
     * @param lang the language to get the title in
     * @return the title, if any
     */
    public String getTitle(Language lang) {
        return "Parties";
    }

    public Element toXML(Document doc) {
        Element cardElt = super.toXML(doc);

        List<String> ids = new ArrayList<String>();

        boolean first = true;
        int id = -233;

        CandidatesModule candidatesModule = (CandidatesModule) getModuleByName("Party");
        for (CardElement ce : candidatesModule.getData()) {
            if(first)    {
                first = false;
                id = Integer.parseInt(ce.getUID().substring(1)) - 1 ; //This will add the "none" id to the XML. I think.
                ids.add("B" + Integer.toString(id));
            }

            Element cardElementElt = ce.toXML(doc);
            cardElt.appendChild(cardElementElt);

            ids.add(ce.getUID());
        }



        //Need to carry the grouping of these candidates together for NIZK purposes.
        XMLTools.addListProperty(doc, cardElt, Properties.RACE_GROUP, "String", ids.toArray(new String[0]));

        XMLTools.addProperty(doc, cardElt, Properties.CARD_STRATEGY, "String", "StraightTicket".split("!"));

        return cardElt;
    }
}
