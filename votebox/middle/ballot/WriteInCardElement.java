package votebox.middle.ballot;

import votebox.middle.IncorrectTypeException;
import votebox.middle.Properties;
import votebox.middle.writein.WriteInCandidateGUI;


/**
 * A WriteInCardElement is a SelectableCardElement whose candidate name can be modified.
 *
 * This class extends SelectableCardElement, because the WriteInCardElement
 * is a special kind of selectable ballot element -- one that can start a
 * prompt and have its corresponding image be rewritten.
 *
 * Author: Mircea C. Berechet
 * Added to project: 08/06/2013
 */

public final class WriteInCardElement extends SelectableCardElement{

    public WriteInCardElement(String uid, Properties properties) {
        super(uid, properties);
    }

    /**
     * The outside calls this method if they wish to attempt to select this card
     * element. It overrides the SelectableCardElement's select method. In addition
     * to attempting to select this card element, the WriteInCardElement's select
     * method starts a GUI that prompts a voter to type in his preferred candidate's
     * name.
     *
     * @throws CardStrategyException
     *             This method throws if the strategy runs into a problem.
     */
    public boolean select() throws CardStrategyException {
        startWriteInCandidateGUI();
        return getParentCard().select(this);
    }

    /**
     * This method starts a WriteInCandidateGUI that prompts a voter to type in the
     * name of his preferred Write-In Candidate. It reads the type of write-in from
     * this CardElement's properties and opens an appropriate GUI (regular or presidential).
     */
    public void startWriteInCandidateGUI()
    {
        try
        {   String writeInType = getProperties().getString(Properties.WRITE_IN_TYPE);
            //System.out.println("Starting a GUI for a " + writeInType + " write-in candidate (" + getUniqueID() + ")!");
            WriteInCandidateGUI writeInGUI = new WriteInCandidateGUI(680, 384, getUniqueID(), writeInType);
            writeInGUI.start();
        }
        catch (IncorrectTypeException e)
        {
            System.out.println("WRONG PROPERTY TYPE! Expected: String");
        }
    }

}