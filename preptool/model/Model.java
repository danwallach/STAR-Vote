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

package preptool.model;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import preptool.controller.exception.BallotExportException;
import preptool.controller.exception.BallotOpenException;
import preptool.controller.exception.BallotPreviewException;
import preptool.controller.exception.BallotSaveException;
import preptool.model.ballot.*;
import preptool.model.ballot.module.AModule;
import preptool.model.language.Language;
import preptool.model.layout.ALayoutComponent;
import preptool.model.layout.Layout;
import preptool.model.layout.Page;
import preptool.model.layout.manager.ILayoutManager;
import preptool.model.layout.manager.ILayoutManagerFactory;
import preptool.model.layout.manager.PsychLayoutManager;
import preptool.model.layout.manager.Spacer;
import preptool.view.ProgressInfo;
import preptool.view.View;
import preptool.view.dialog.ProgressDialog;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 * Implementation of the model of the preptool. Contains a Ballot and a
 * LayoutManager.
 * 
 * @author Corey Shaw
 */
public class Model {

    private Ballot ballot;

    private ILayoutManagerFactory managerFactory;

    private ICardFactory[] cardFactories;
    
    private int cardsPerReviewPage = 10;
    private int fontSize = 8;

    private boolean textToSpeech = false;

    /**
     * Creates a new Model with a blank Ballot and using the PsychLayoutManager.
     */
    public Model() {

        /* Create a blank ballot */
        newBallot();

        /* Create a new layout manager factory that uses PsychLayoutManager */
        managerFactory = new ILayoutManagerFactory() {

            public ILayoutManager makeLayoutManager(Language language, int numCardsPerReviewPage, int fontSize, boolean textToSpeech) {
                return new PsychLayoutManager(language, numCardsPerReviewPage, fontSize, textToSpeech);
            }
        };

        /* Create a new card factory */
        cardFactories = new ICardFactory[] { PartyCard.FACTORY, RaceCard.FACTORY, PresidentialRaceCard.FACTORY, PropositionCard.FACTORY };
    }

    /**
     * Adds the given card to the ballot
     * 
     * @param newCard       the card to add
     */
    public void addCard(ACard newCard) {
        getBallot().getCards().add( newCard );
    }

    /**
     * Adds the card to the front of the ballot
     *
     * @param newCard       the card to add
     */
    public void addCardAtFront(ACard newCard){
        getBallot().getCards().add(0, newCard);
    }

    /**
     * Recursively adds the directory to a ZIP file
     */
    private void addDirectoryToZip(ZipOutputStream out, File dir, String path) throws IOException {

        byte[] buf = new byte[1024];

        /* Get the list of files in the directory */
        File[] children = dir.listFiles();

        /* Go through all the files */
        for (File f : children) {

            /* Check if the file is a directory */
            if (f.isDirectory()) {

                /* Take the directory and enter it as a new ZipEntry */
                out.putNextEntry(new ZipEntry(path + f.getName() + '/'));
                out.closeEntry();

                /* Re-call the function with the path of the directory */
                addDirectoryToZip(out, f, path + f.getName() + '/');
            }

            /* When the file isn't a directory */
            else {

                /* Create a new input stream */
                FileInputStream in = new FileInputStream(f);

                /* Enter the file as a new ZipEntry */
                out.putNextEntry(new ZipEntry(path + f.getName()));

                int len;

                /* Transfer bytes from the file to the ZIP file */
                while ((len = in.read(buf)) > 0)
                    out.write(buf, 0, len);

                /* Complete the entry */
                out.closeEntry();
                in.close();
                f.deleteOnExit();
            }
        }
    }

    /**
     * Checks the ballot for any cards that are missing translations, and then
     * returns a list of Strings with their names so they can be displayed as a
     * list in a dialog
     * 
     * @return          list of names of cards missing translations
     */
    public String[] checkTranslations() {

        ArrayList<String> cardsNeeded = new ArrayList<>();
        Language primaryLanguage = getLanguages().get(0);

        /* Get the cards */
        ArrayList<ACard> cards = getBallot().getCards();

        /* Look through all the cards */
        for (ACard card : cards) {

            boolean res = false;

            /* See if any cards need translations */
            for (Language lang : getLanguages())
                res |= card.needsTranslation(lang);

            /* Get the name of the current card */
            String title = card.getTitle(primaryLanguage);

            /* If so, then add the card language to the cardsNeeded */
            if (res) cardsNeeded.add(title);
        }

        /* Return the cardsNeeded ArrayList<String> as a String[] */
        String[] asString = new String[cardsNeeded.size()];
        return cardsNeeded.toArray(asString);
    }

    /**
     * Deletes the card at index idx from the ballot
     * 
     * @param idx       the index
     */
    public void deleteCard(int idx) {

        /* Get the cards */
        ArrayList<ACard> cards = getBallot().getCards();

        /* Remove the card at the index */
        cards.remove(idx);
    }

    /**
     * Exports the ballot to VoteBox.
     * 
     * @param view      the main view of the program
     * @param path      the path to export to
     */
    public void export(View view, final String path) {
        export(view, path, null, false);
    }

    /**
     * Exports the ballot to VoteBox
     * 
     * @param view                  the main view of the program
     * @param path                  the path to export to
     * @param whenDone              Runnable to execute when the export is done
     * @param hideWhenFinished      whether to hide the progress dialog when finished exporting
     */
    public void export(View view, final String path, final Runnable whenDone, final boolean hideWhenFinished) {

        /* Open a progress dialogue */
        final ProgressDialog dialog = new ProgressDialog(view, "Exporting Ballot to VoteBox");

        new Thread() {

            @Override
            public void run() {

                try {

                    /* Refresh the progress dialogue */
                    ProgressInfo info = dialog.getProgressInfo();
                    dialog.showDialog();

                    int c = 0;

                    /* Set the number of tasks based on number of languages */
                    info.setNumTasks(getLanguages().size());

                    /* Go through each language */
                    for (Language lang : getLanguages()) {

                        final String taskName = "Exporting " + lang.getName() + " Ballot";

                        /* Set the current task */
                        info.setCurrentTask(taskName, c);
                        info.setProgress("Laying out Ballot", 0);

                        /* Create a new layout manager */
                        ILayoutManager manager = getManagerFactory().makeLayoutManager(lang, cardsPerReviewPage,fontSize, textToSpeech);

                        /* Create a new Layout for the Ballot */
                        Layout layout = manager.makeLayout(getBallot());

                        /* Write the Ballot XML */
                        info.setProgress("Writing Ballot XML", 0);
                        Document doc = XMLTools.createDocument();
                        XMLTools.writeXML(getBallot().toXML(doc), path + "/ballot.xml");

                        /* Write the Layout XML */
                        info.setProgress("Writing Layout XML", 0);
                        doc = XMLTools.createDocument();
                        XMLTools.writeXML(layout.toXML(doc), path + "/layout_1_" + lang.getShortName() + ".xml");

                        /* Render all the images */
                        manager.renderAllImagesToDisk(layout, path + "/media/", info);

                        /* Write the configuration file */
                        BufferedWriter out = new BufferedWriter(new FileWriter(path + "/ballotbox.cfg"));

                        /* XML Housekeeping */
                        out.write("/ballot.xml");
                        out.newLine();
                        out.write("/layout");
                        out.close();

                        /* Increment for the next task */
                        c++;
                    }

                    /* Tasks completed */
                    info.finished();

                    /* If supposed to hide when finished, then set the dialogue invisible */
                    if (hideWhenFinished) dialog.setVisible(false);

                    /* If there is a Runnable to execute when complete, run it now */
                    if (whenDone != null) whenDone.run();
                }
                catch (TransformerFactoryConfigurationError | IllegalArgumentException | ParserConfigurationException |
                       TransformerException | IOException e) { throw new BallotExportException(e); }
            }
        }.start();
    }

    /**
     * Exports the ballot to VoteBox as a ZIP file.
     * 
     * @param view      the main view of the program
     * @param path      the path to export to
     */
    public void exportAsZip(View view, final String path) {
        exportAsZip(view, path, null, false);
    }

    /**
     * Exports the ballot to VoteBox as a ZIP file
     * 
     * @param view                  the main view of the program
     * @param path                  the path to export to
     * @param whenDone              Runnable to execute when the export is done
     * @param hideWhenFinished      whether to hide the progress dialog when finished exporting
     */
    public void exportAsZip(View view, final String path, final Runnable whenDone, final boolean hideWhenFinished) {

        /* Open a progress dialogue */
        final ProgressDialog dialog = new ProgressDialog(view, "Exporting Ballot to VoteBox");

        new Thread() {

            @Override
            public void run() {

                try {

                    /* Create a temporary file */
                    File tempDir = File.createTempFile("votebox", "");

                    /* Delete the file and create a directory */
                    tempDir.delete();
                    tempDir.mkdir();

                    String zipFile = path;

                    /* Extract the path from the filepath */
                    String fileExtension = zipFile.substring(zipFile.length() - 4);

                    Boolean isZip = fileExtension.equals(".zip");

                    /* See if the file is a zip and, if not, make it one */
                    if (!isZip)
                        zipFile = zipFile + ".zip";

                    /* Update the progress dialogue */
                    ProgressInfo info = dialog.getProgressInfo();
                    dialog.showDialog();

                    int c = 0;

                    /* Set the number of tasks to complete based on the size of the set of languages */
                    info.setNumTasks(getLanguages().size());

                    /* Go through each language in the language set */
                    for (Language lang : getLanguages()) {

                        final String taskName = "Exporting " + lang.getName() + " Ballot";

                        /* Set the current task and progress */
                        info.setCurrentTask(taskName, c);
                        info.setProgress("Laying out Ballot", 0);

                        /* Create a new layout manager */
                        ILayoutManager manager = getManagerFactory().makeLayoutManager(lang, cardsPerReviewPage, fontSize, textToSpeech);

                        /* Create a new Layout for the Ballot */
                        Layout layout = manager.makeLayout(getBallot());
                        
                        /* Set the start page based on the number of langauges */
                        int curPage = getLanguages().size() > 1 ? 2 : 1;

                        /* Go through each card in the card collection */
                        for (ACard card : getBallot().getCards()){

                            /* Get the current page */
                            ArrayList<Page> pages = layout.getPages();
                            Page currentPage = pages.get(curPage);

                            /* Get the current page's title */
                            ALayoutComponent title = currentPage.getComponents().get(1);

                            /* Set the title ID for the current page */
                            card.setTitleID(title.getUID());

                            /* Move to the next page */
                            curPage++;
                        }

                        /* Write the Ballot XML */
                        info.setProgress("Writing Ballot XML", 0);
                        Document doc = XMLTools.createDocument();
                        XMLTools.writeXML(getBallot().toXML(doc), tempDir + "/ballot.xml");

                        /* Write the Layout XML */
                        info.setProgress("Writing Layout XML", 0);
                        doc = XMLTools.createDocument();
                        XMLTools.writeXML(layout.toXML(doc), tempDir + "/layout_1_" + lang.getShortName() + ".xml");

                        /* Render the images */
                        manager.renderAllImagesToDisk(layout, tempDir + "/media/", info);

                        /* Write the configuration file */
                        BufferedWriter out = new BufferedWriter(new FileWriter(tempDir + "/ballotbox.cfg"));

                        /* XML housekeeping */
                        out.write("/ballot.xml");
                        out.newLine();
                        out.write("/layout");
                        out.close();

                        /* Move to the next task */
                        c++;
                    }

                    /* Create the zip file */
                    info.setCurrentTask("Adding Files to ZIP Archive", c);
                    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
                    addDirectoryToZip(out, tempDir, "");
                    out.close();

                    /* Clean up temp files */
                    info.setCurrentTask("Cleaning up Temporary Files", c);
                    Stack<File> dirStack = new Stack<>();
                    dirStack.add(tempDir);

                    /* Check that there is still something on the stack */
                    while (!dirStack.isEmpty()) {

                        /* Pop the file off the stack */
                        File file = dirStack.pop();

                        /* Mark it for deletion */
                        file.deleteOnExit();

                        /* Get its children files */
                        File[] children = file.listFiles();

                        /* Go through each child */
                        for (File f : children) {

                            /* Check if the child is a directory and, if so, add them to the stack */
                            if (f.isDirectory()) dirStack.add(f);

                            /* Otherwise, go ahead and mark for deletion */
                            else f.deleteOnExit();
                        }

                        /* If the stack is too deep, get out of there */
                        if (dirStack.size() > 100)
                            return;
                    }

                    /* All tasks completed */
                    info.finished();

                    /* If the dialogue is to be hidden after completion, set to invisible */
                    if (hideWhenFinished) dialog.setVisible(false);

                    /* If there is a Runnable to execute after completion, go ahead and run it now */
                    if (whenDone != null) whenDone.run();
                }
                catch (IllegalArgumentException | ParserConfigurationException | TransformerFactoryConfigurationError |
                       TransformerException | IOException e) { throw new BallotExportException(e); }

            }
        }.start();
    }

    /**
     * @return the ballot
     */
    public Ballot getBallot() {
        return ballot;
    }

    /**
     * @return the factories available to create new cards
     */
    public ICardFactory[] getCardFactories() {
        return cardFactories;
    }

    /**
     * Returns the list of modules for the card at the given index
     * 
     * @param idx       the index
     * @return          the list of modules
     */
    public ArrayList<AModule> getCardModules(int idx) {
        return getBallot().getCards().get(idx).getModules();
    }

    /**
     * Returns the title of the card at the given index, in the primary language
     * 
     * @param idx       the index
     * @return          the title in the primary language
     */
    public String getCardTitle(int idx) {

        /* Get the cards */
        ArrayList<ACard> cards = getBallot().getCards();

        /* Get the current card */
        ACard currentCard = cards.get(idx);

        /* Get the primary language */
        Language primaryLanguage = getLanguages().get(0);

        /* Return the title of the card in the primary language */
        return currentCard.getTitle(primaryLanguage);
    }

    /**
     * Returns a type name of the card at index idx
     * 
     * @param idx       the index
     * @return          the type as a string
     */
    public String getCardType(int idx) {

        /* Get the cards */
        ArrayList<ACard> cards = getBallot().getCards();

        /* Get the current card */
        ACard currentCard = cards.get(idx);

        /* Return the type of the current card */
        return currentCard.getType();
    }

    /**
     * @return          the list of languages in the ballot
     */
    public ArrayList<Language> getLanguages() {
        return getBallot().getLanguages();
    }

    /**
     * @return          the layout manager
     */
    public ILayoutManagerFactory getManagerFactory() {
        return managerFactory;
    }

    /**
     * @return          the number of cards in the ballot
     */
    public int getNumCards() {

        /* Get the cards */
        ArrayList<ACard> cards = getBallot().getCards();

        return cards.size();
    }

    /**
     * @return          the list of parties in the ballot
     */
    public ArrayList<Party> getParties() {
        return getBallot().getParties();
    }

    /**
     * Moves a card from oldIdx to newIdx in the ballot
     * 
     * @param oldIdx    the old index
     * @param newIdx    the new index
     */
    public void moveCard(int oldIdx, int newIdx) {

        /* Get the cards */
        ArrayList<ACard> cards = getBallot().getCards();

        /* Get the removed card */
        ACard removedCard = cards.remove(oldIdx);

        /* Add the removed card to the preferred location */
        cards.add(newIdx, removedCard);
    }

    /**
     * Starts a new ballot
     */
    public void newBallot() {

        /* Create a new Ballot */
        ballot = new Ballot();

        /* Get the current languages of the ballot */
        ArrayList<Language> ballotLanguages = getBallot().getLanguages();

        /* Get the primary language */
        Language primaryLanguage = Language.getAllLanguages().get(0);

        /* Add the primary language to the ballot */
        ballotLanguages.add(primaryLanguage);
    }

    /**
     * Opens and loads a ballot from an XML file
     * 
     * @param filepath      the file to open from
     */
    public void open(String filepath) {

        /* Try to read and parse the XML file */
        try {
            Document doc = XMLTools.readXML(filepath);
            ballot = Ballot.parseXML(doc.getDocumentElement());
        }
        catch (ParserConfigurationException | SAXException | IOException e) { throw new BallotOpenException(e); }
    }

    /**
     * Previews the entire ballot in VoteBox, by exporting to a temporary
     * directory and then launching VoteBox
     * 
     * @param view      the main view
     */
    public void previewBallot(final View view) {

        try {

            /* Create a temporary directory */
            final File tempDir = File.createTempFile("votebox", "");

            /* Delete and then make a directory */
            tempDir.delete();
            tempDir.mkdir();

            /* Create a new Runnable */
            Runnable whenDone = new Runnable() {

                public void run() {

                    /* Run datalogger in VoteBox */
                    votebox.middle.datacollection.DataLogger.init(new File(tempDir, "log"));

                    new votebox.middle.driver.Driver(tempDir.getAbsolutePath(), new votebox.middle.view.AWTViewFactory(true, false), false).run();

                    /* Delete temporary directories */
                    Stack<File> dirStack = new Stack<>();
                    dirStack.add(tempDir);

                    /* While the stack still has something */
                    while (!dirStack.isEmpty()) {

                        /* Pop the file off the stack */
                        File file = dirStack.pop();

                        /* Mark for deletion */
                        file.deleteOnExit();

                        /* Get its children files */
                        File[] children = file.listFiles();

                        /* Go through each child */
                        for (File f : children) {

                            /* See if it's a directory and, if so, add it to the stack*/
                            if (f.isDirectory()) dirStack.add(f);

                            /* Otherwise, go ahead and mark for deletion */
                            else f.deleteOnExit();
                        }

                        /* If the stack is way too deep, get out of there */
                        if (dirStack.size() > 100) return;
                    }
                }
            };

            /* Export to votebox */
            export(view, tempDir.getAbsolutePath(), whenDone, true);

        }
        catch (IOException e) { throw new BallotPreviewException(e); }
    }

    /**
     * Renders one or more JPanels of the given card - if there is more than one
     * panel, it is because the card is on multiple pages.<br>
     * All rendering is done in a separate thread.
     * 
     * @param idx           the index of the card to preview
     * @param language      the language to preview it in
     * @return              a list of rendered panels that can be displayed
     */
    public ArrayList<JPanel> previewCard(int idx, Language language) {

        /* Create a new layout manager */
        final ILayoutManager manager = getManagerFactory().makeLayoutManager(language, cardsPerReviewPage, fontSize, false);

        ArrayList<ACard> cards = getBallot().getCards();
        ACard currentCard = cards.get(idx);

        /* Get the list of panels for the card page */
        final ArrayList<JPanel> panels = manager.makeCardPage(currentCard);

        new Thread() {

            public void run() {

                /* Go through each of the panels */
                for (JPanel panel : panels) {

                    /* Go through each of the components on the panel */
                    for (Component comp : panel.getComponents()) {

                        /* Check for a spacer */
                        if (comp instanceof Spacer) {

                            /* Cast it to a spacer and grab an image */
                            final Spacer spacer = (Spacer) comp;
                            final BufferedImage image = spacer.getComponent().execute(manager.getImageVisitor(), false, false);

                            if (image != null) {

                                /* Set up a new Runnable */
                                SwingUtilities.invokeLater(new Runnable() {

                                    public void run() {
                                        spacer.setIcon(new ImageIcon(image));
                                    }

                                } );
                            }
                        }
                    }
                }
            }
        }.start();

        return panels;
    }

    /**
     * Saves the ballot as an XML file
     * 
     * @param filepath      the file to save to
     */
    public void saveAs(String filepath) {

        try {

            /* Extract the file extension */
            String fileExtension = filepath.substring(filepath.length() - 4);
            Boolean isBallotFile = fileExtension.equals(".bal");

            /* See if the file is a ballot file and, if not, add the file extension */
            if (!isBallotFile)
                filepath += ".bal";

            /* Write the ballot to an XML file */
            Document doc = XMLTools.createDocument();
            XMLTools.writeXML(getBallot().toSaveXML(doc), filepath);
        }
        catch (IllegalArgumentException | ParserConfigurationException | TransformerException |
               TransformerFactoryConfigurationError e) { throw new BallotSaveException(e); }
    }

    /**
     * Sets the list of languages
     * 
     * @param languages         the new list of languages
     */
    public void setLanguages(ArrayList<Language> languages) {
        getBallot().setLanguages(languages);
    }
    
    /**
     * Sets the properties of the layout
     * 
     * @param numCardsPerReviewPage     the number of races shown on one review page
     */
    public void setCardsPerReviewPage(int numCardsPerReviewPage){
    	cardsPerReviewPage = numCardsPerReviewPage;
    }
    
    /**
     * Sets the properties of the layout
     * 
     * @param fontSizeMultiplier        the font size multiplier
     */
    public void setFontSize(int fontSizeMultiplier){
    	fontSize = fontSizeMultiplier;
    }

    /**
     * Sets text-to-speech status
     *
     * @param textToSpeech      whether text-to-speech is enabled
     */
    public void setTextToSpeech(boolean textToSpeech) {
        this.textToSpeech = textToSpeech;
    }

    /**
     * @return      Number of cards/items found per review page
     */
	public int getCardsPerReviewPage() {
		return cardsPerReviewPage;
	}

	/**
	 * @return      Current base font size.
	 */
	public int getBaseFontSize() {
		return fontSize;
	}

    /**
     * @return      whether text-to-speech is enabled
     */
    public boolean getTextToSpeech(){
        return textToSpeech;
    }

}
