package preptool.model.ballot;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import preptool.model.ballot.module.WriteInModule;
import preptool.model.language.Language;
import preptool.model.layout.manager.ALayoutManager;

import java.util.ArrayList;

/**
 * A class for creating the write-in card to be inserted into ballots
 *
 * Created by matt on 2/24/14.
 */
public class WriteInCard extends ACard{

    /**
     * Factory to create a WriteIn
     */
    public static final ICardFactory FACTORY = new ICardFactory() {

        public String getMenuString() {
            return "Enable Write-ins";
        }

        public ACard makeCard() {
            return new WriteInCard();
        }

    };

    /**
     * Constructs a blank ACard with an empty list of modules.
     *
     */
    public WriteInCard() {
        super("WriteIn");

        modules.add(new WriteInModule());
    }

    @Override
    public void assignUIDsToBallot(ALayoutManager manager) {
        setUID(manager.getNextBallotUID());
        WriteInModule WriteInModule = (WriteInModule) getModuleByName("WriteIn");
    }

    /**
     * NO-OPs
     */
    @Override
    public String getReviewBlankText(Language language) {
        return null;
    }

    @Override
    public String getReviewTitle(Language language) {
        return null;
    }

    @Override
    public ALayoutManager.ICardLayout layoutCard(ALayoutManager manager, ALayoutManager.ICardLayout cardLayout) {
        cardLayout.setTitle("Write-in Input");
        cardLayout.setDescription("Input the name of your desired candidate");

        return cardLayout;
    }

    @Override
    public ArrayList<String> getCardData(Language language) {
        return null;
    }

    @Override
    public Element toXML(Document doc) {
        Element cardElt = super.toXML(doc);

        WriteInModule writeInModuleModule = (WriteInModule) getModuleByName("WriteIn");
        cardElt.appendChild(writeInModuleModule.toSaveXML(doc));

        return cardElt;
    }
}
