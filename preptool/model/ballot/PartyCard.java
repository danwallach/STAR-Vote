package preptool.model.ballot;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import preptool.model.XMLTools;
import preptool.model.ballot.module.CandidatesModule;
import preptool.model.ballot.module.TextFieldModule;
import preptool.model.language.Language;
import preptool.model.language.LiteralStrings;
import preptool.model.layout.manager.ALayoutManager;
import votebox.middle.Properties;

import java.util.ArrayList;
import java.util.List;

/**
 * This card will allow for straight party voting. It will present the voter with a card containing
 * a CandidatesModule with party names instead of candidates and no "party" field.
 *
 * @author Matt Bernhard, Mircea C. Berechet
 */
public class PartyCard extends ACard {

    /** A text module to show that this is a straight party card */
    private TextFieldModule title;

    /**
     * Factory to create a PartyCard
     */
    public static final ICardFactory FACTORY = new ICardFactory() {


        /**
         * @see ICardFactory#getMenuString()
         */
        public String getMenuString() {
            return "Add Straight Party Support";
        }

        /**
         * @see preptool.model.ballot.ICardFactory#makeCard()
         */
        public ACard makeCard() {
            return new PartyCard();
        }

    };

    /**
     * Constructs a new Party card
     */
    public PartyCard() {
        super("Party");

        /* Initialize and add the text module */
        title = new TextFieldModule("Title", "Title");
        modules.add(title);

        /* Add a new candidates module with 1 column */
        modules.add(new CandidatesModule("Party", new String[]{ "Party" }, false));
    }

    /**
     * @see preptool.model.ballot.ACard#assignUIDsToBallot(preptool.model.layout.manager.ALayoutManager)
     */
    public void assignUIDsToBallot(ALayoutManager manager) {
        setUID(manager.getNextBallotUID());
        CandidatesModule candidatesModule = (CandidatesModule) getModuleByName("Party");

        /* Also assign UIDs to every element on this card */
        for (CardElement ce : candidatesModule.getData()) {
            ce.setUID(manager.getNextBallotUID());
        }
    }

    /**
     * Returns the text to show on the review screen if the user has not made a
     * selection on this card
     *
     * @param language      the language in which the review screen text should be
     * @return              "No Selection" in the specified language
     */
    public String getReviewBlankText(Language language) {
        title.setData(language, LiteralStrings.Singleton.get("STRAIGHT_PARTY").get(language));
        return LiteralStrings.Singleton.get("NONE", language);
    }

    /**
     * Returns the review title for this card
     *
     * @param language      the language in which the review screen text should be
     * @return              the review title in the specified language
     */
    public String getReviewTitle(Language language) {
        title.setData(language, LiteralStrings.Singleton.get("STRAIGHT_PARTY").get(language));
        TextFieldModule titleModule = (TextFieldModule) getModuleByName("Title");
        return titleModule.getData(language) + ":";
    }

    /**
     * Lays out this card in the given ICardLayout
     *
     * @param manager           the layout manager that will layout this card
     * @param cardLayout        the card layout object specifying how the card should be laid out
     * @return                  the finished card layout object, with the proper information about this card
     */
    public ALayoutManager.ICardLayout layoutCard(ALayoutManager manager, ALayoutManager.ICardLayout cardLayout) {
        /* Get the language and set this card as Straight Party*/
        Language lang = manager.getLanguage();
        title.setData(lang, LiteralStrings.Singleton.get("STRAIGHT_PARTY").get(lang));

        /* Add title information to the layout */
        TextFieldModule title = (TextFieldModule) getModuleByName("Title");
        cardLayout.setTitle(title.getData(lang));

        /* Layout the candidates module this card contains */
        CandidatesModule candidatesModule = (CandidatesModule) getModuleByName("Party");
        for (CardElement ce : candidatesModule.getData()) {
            /* Note that we put the party where the candidate's name would normally be */
            cardLayout.addCandidate(ce.getUID(), ce.getParty().getName(lang));
        }

        return cardLayout;
    }

    /**
     * @param language      the language in which to get the data
     * @return              a list of all of the parties contained on this card in the given language
     */
    @Override
    public ArrayList<String> getCardData(Language language) {

        /* Get the module containing all of the party data */
        CandidatesModule candidatesModule = (CandidatesModule) getModuleByName("Party");
        ArrayList<CardElement> cardElements = candidatesModule.getData();

        /* Initialize a new list of strings */
        ArrayList<String> dataStrings = new ArrayList<String>();

        /* Build the list of party names from the module data */
        for(CardElement ce : cardElements){
            dataStrings.add(ce.getParty().getAbbrev(language));
        }

        return dataStrings;
    }

    /**
     * Returns this card's title, by checking to see if there is a title module
     * and returning its data. If there is no title, returns the empty string.
     * Since there will only be one of these, we can just return the string literal
     * of it for a given language
     *
     * @param lang      the language to get the title in
     * @return          the title, if any
     */
    public String getTitle(Language lang) {
        return LiteralStrings.Singleton.get("STRAIGHT_PARTY").get(lang);

    }

    /**
     * @param doc       the document that this card is a part of
     * @return          an XML representation of this card
     */
    public Element toXML(Document doc) {
        /* First do all the bookkeeping for ACard*/
        Element cardElt = super.toXML(doc);

        /* We will build a list of UIDs */
        List<String> ids = new ArrayList<String>();

        CandidatesModule candidatesModule = (CandidatesModule) getModuleByName("Party");

        /* We will artificially insert a no selection option, with the same UID as the card itself */
        int id = Integer.parseInt(this.getUID().substring(1)) ;
        ids.add("B" + Integer.toString(id));

        /* Now add all of the children's XML representations to our CE and add their ID's to our list */
        for (CardElement ce : candidatesModule.getData()) {

            Element cardElementElt = ce.toXML(doc);
            cardElt.appendChild(cardElementElt);

            ids.add(ce.getUID());
        }

        /* Need to carry the grouping of these candidates together for NIZK purposes. */
        XMLTools.addListProperty(doc, cardElt, Properties.RACE_GROUP, "String", ids.toArray(new String[ids.size()]));

        /* Note that this is a straight ticket card in the XML */
        XMLTools.addProperty(doc, cardElt, Properties.CARD_STRATEGY, "String", "StraightTicket");

        return cardElt;
    }
}

