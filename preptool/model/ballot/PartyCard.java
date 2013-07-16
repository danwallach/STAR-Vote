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

    private TitleModule title;

    /**
     * Factory to create a PartyCard
     */
    public static final ICardFactory FACTORY = new ICardFactory() {

        public String getMenuString() {
            return "Add Straight Party Support";
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
        title = new TitleModule("Title", "Title");
        modules.add(title);
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
        title.setData(language, LiteralStrings.Singleton.get("STRAIGHT_PARTY").get(language));
        return LiteralStrings.Singleton.get("NONE", language);
    }

    /**
     * Returns the review title for this card
     *
     * @param language the language
     * @return the review title
     */
    public String getReviewTitle(Language language) {
        title.setData(language, LiteralStrings.Singleton.get("STRAIGHT_PARTY").get(language));
        TitleModule titleModule = (TitleModule) getModuleByName("Title");
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
        title.setData(lang, LiteralStrings.Singleton.get("STRAIGHT_PARTY").get(lang));
        TitleModule title = (TitleModule) getModuleByName("Title");
        CandidatesModule candidatesModule = (CandidatesModule) getModuleByName("Party");

        cardLayout.setTitle(title.getData(lang));
        for (CardElement ce : candidatesModule.getData()) {
            //Note that we put the party where the candidate's name would normally be
            cardLayout.addCandidate(ce.getUID(), ce.getParty().getName(lang));
        }
        return cardLayout;
    }

    @Override
    public ArrayList<String> getCardData(Language language) {

        CandidatesModule candidatesModule = (CandidatesModule) getModuleByName("Party");
        ArrayList<CardElement> cardElements = candidatesModule.getData();

        ArrayList<String> dataStrings = new ArrayList<String>();

        for(CardElement ce : cardElements){
            dataStrings.add(ce.getParty().getAbbrev(language));
        }

        return dataStrings;
    }

    /**
     * Returns this card's title, by checking to see if there is a title module
     * and returning its data. If there is no title, returns the empty string
     * @param lang the language to get the title in
     * @return the title, if any
     */
    public String getTitle(Language lang) {
        //Since there will only be one of these, we can just return the string literal
        // of it for a given language
        return LiteralStrings.Singleton.get("STRAIGHT_PARTY").get(lang);

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

        XMLTools.addProperty(doc, cardElt, Properties.CARD_STRATEGY, "String", "StraightTicket");

        return cardElt;
    }
}
