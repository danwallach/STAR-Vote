package votebox.middle.ballot;

import votebox.middle.IncorrectTypeException;
import votebox.middle.Properties;


/**
 * A WriteInCardElement is a CardElement that can be toggled and focused on.
 * For the purposes of the voting machine runtime, toggling can be thought of as
 * the voter's expression of preference. Focusing is strictly a gui capability.
 * The "focused" element simply is the element which the user is currently
 * looking at. The only reason this state is supported model side is because the
 * model needs to define an image to hand to the view for when the element is
 * focused. This is important, since the voting machine should do no rendering
 * work during runtime. The focused, state, however unlike the selected state,
 * has nothing to do with the model.< para>
 *
 * In order to gain the focusing capability, this class must implement the
 * IFocusable interface. This class also extends CardElement, because the
 * WriteInCardElement is a special kind of ballot element -- one that can be
 * selected and have its corresponding image be rewritten.< para>
 *
 * @author Mircea C. Berechet
 * Date added to project: 08/06/2013
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
        {
        System.out.println("Starting a GUI for a " + getProperties().getString(Properties.WRITE_IN_TYPE) + " write-in candidate!");
        }
        catch (IncorrectTypeException e)
        {
            System.out.println("WRONG PROPERTY TYPE! Expected: String");
        }
    }

}