package preptool.model.ballot;

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
     * Constructs a blank ACard with an empty list of modules.
     *
     * @param type
     */
    public WriteInCard(String type) {
        super(type);
    }

    @Override
    public void assignUIDsToBallot(ALayoutManager manager) {

    }

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
        return null;
    }

    @Override
    public ArrayList<String> getCardData(Language language) {
        return null;
    }
}
