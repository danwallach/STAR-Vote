package preptool.model.ballot;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import preptool.model.XMLTools;
import preptool.model.ballot.ACard;
import preptool.model.ballot.module.CandidatesModule;
import preptool.model.ballot.module.TextFieldModule;
import preptool.model.language.Language;
import preptool.model.language.LiteralStrings;
import preptool.model.layout.manager.ALayoutManager;
import votebox.middle.Properties;

import java.util.ArrayList;
import java.util.List;

/**
 * A class which represents a card in which a user can select all candidates of this party in a voting session.
 * This will be the first card seen after the start screen in Votebox and will have the ability to set candidates
 * of a certain party to "selected" in all subsequent cards.
 *
 * @author Matt Bernhard
 * @version 0.0.1
 * Date: 6/27/13
 */
public class StraightPartyCard extends ACard {
    /**
     * Factory to create a RaceCard
     */
    public static final ICardFactory FACTORY = new ICardFactory() {

        public String getMenuString() {
            return "Add Race";
        }

        public ACard makeCard() {
            return new RaceCard();
        }

    };

    /**
     * Constructs a new RaceCard
     */
    public StraightPartyCard() {
        super("StraightParty");
        modules.add(new TextFieldModule("Title", "Title"));
        modules.add(new CandidatesModule("Candidates", new String[]{
                "Candidate's Name", "Party" }));
    }

    @Override
    public void assignUIDsToBallot(ALayoutManager manager) {
        setUID(manager.getNextBallotUID());
        CandidatesModule candidatesModule = (CandidatesModule) getModuleByName("Candidates");
        for (CardElement ce : candidatesModule.getData()) {
            ce.setUID(manager.getNextBallotUID());
        }
    }

    @Override
    public String getReviewBlankText(Language language) {
        return LiteralStrings.Singleton.get("NONE", language);
    }

    @Override
    public String getReviewTitle(Language language) {
        TextFieldModule titleModule = (TextFieldModule) getModuleByName("Title");
        return titleModule.getData(language) + ":";
    }

    @Override
    public ALayoutManager.ICardLayout layoutCard(ALayoutManager manager, ALayoutManager.ICardLayout cardLayout) {
        Language lang = manager.getLanguage();
        TextFieldModule titleModule = (TextFieldModule) getModuleByName("Title");
        CandidatesModule candidatesModule = (CandidatesModule) getModuleByName("Candidates");

        cardLayout.setTitle(titleModule.getData(lang));
        for (CardElement ce : candidatesModule.getData()) {
            cardLayout.addCandidate(ce.getUID(), ce.getName(lang, 0), ce
                    .getParty().getAbbrev(lang));
        }
        return cardLayout;
    }

    @Override
    public Element toXML(Document doc) {
        Element cardElt = super.toXML(doc);

        List<String> ids = new ArrayList<String>();

        boolean first = true;
        int id = -233;

        CandidatesModule candidatesModule = (CandidatesModule) getModuleByName("Candidates");
        for (CardElement ce : candidatesModule.getData()) {
            if(first)    {
                first = false;
                id = Integer.parseInt(ce.uniqueID.substring(1)) - 1 ; //This will add the "none" id to the XML. I think.
                ids.add("B" + Integer.toString(id));
            }

            Element cardElementElt = ce.toXML(doc);
            cardElt.appendChild(cardElementElt);

            ids.add(ce.uniqueID);
        }



        //Need to carry the grouping of these candidates together for NIZK purposes.
        XMLTools.addListProperty(doc, cardElt, Properties.RACE_GROUP, "String", ids.toArray(new String[0]));
        return cardElt;
    }
}
