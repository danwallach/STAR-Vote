package votebox.middle.ballot;

import votebox.middle.IncorrectTypeException;
import votebox.middle.Properties;
import votebox.middle.view.AView;
import votebox.middle.view.AWTView;
import votebox.middle.view.IView;
import votebox.middle.writein.WriteInCardGUI;

import javax.swing.*;
import java.awt.*;


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
        String writeInType = "Regular";
        try
        {
            writeInType = getProperties().getString(Properties.WRITE_IN_TYPE);
        }
        catch (IncorrectTypeException e)
        {
            System.out.println("WRONG PROPERTY TYPE! Expected: String");
        }

        final String fWriteInType = writeInType;

        SwingWorker worker = new SwingWorker<Void, Void>() {
            public Void doInBackground() {
                WriteInCardGUI writeInCardGUI = new WriteInCardGUI(680, 384, getUniqueID(), fWriteInType, getParentCard());
                writeInCardGUI.start();
                return null;
            }
        };

        worker.execute();

    }
}