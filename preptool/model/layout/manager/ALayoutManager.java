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

package preptool.model.layout.manager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import preptool.model.ballot.ACard;
import preptool.model.ballot.Ballot;
import preptool.model.language.Language;
import preptool.model.layout.ALayoutComponent;
import preptool.model.layout.Background;
import preptool.model.layout.Button;
import preptool.model.layout.ILayoutComponentVisitor;
import preptool.model.layout.Label;
import preptool.model.layout.Layout;
import preptool.model.layout.Page;
import preptool.model.layout.ReviewButton;
import preptool.model.layout.PrintButton;
import preptool.model.layout.ReviewLabel;
import preptool.model.layout.ToggleButton;
import preptool.model.layout.ToggleButtonGroup;
import preptool.view.ProgressInfo;
import printer.PrintImageUtils;


/**
 * ALayoutManager is a more useful abstraction of the LayoutManager, providing
 * methods for different types of pages, and also implementing a card visitor to
 * layout the different types of cards.
 *
 * @author Corey Shaw, Dan Sandler
 */
public abstract class ALayoutManager implements ILayoutManager {

	/** 
	 * Use a thread pool when exporting images to speed up ballot export on
	 * multi-core machines. Experimental. [dsandler]
	 */
	private static final Boolean USE_THREADS = true;

    /**
     * Generate synthesized audio files to speak all of the text on this component
     */
    public static Boolean GENERATE_AUDIO = true;

	/**
     * @see preptool.model.layout.manager.ILayoutManager#makeLayout(preptool.model.ballot.Ballot)
     */
    public abstract Layout makeLayout(Ballot ballot);

    /**
     * @see preptool.model.layout.manager.ILayoutManager#makeCardPage(preptool.model.ballot.ACard)
     */
    public abstract ArrayList<JPanel> makeCardPage(ACard card);

    /**
     * Makes a review page that shows all of the cards on the screen and allows
     * the user to go back and change his response.
     *
     * @param ballot                the ballot, the collection of Cards
     * @param pageTargets           mapping of races (Cards) to review pages
     * @return                      the review page
     */
    protected abstract ArrayList<Page> makeReviewPage(Ballot ballot, HashMap<Integer, Integer> pageTargets);

    /**
     * Makes an introductory page with instructions on how to use VoteBox.
     *
     * @param hasLanguageSelect     whether the ballot will have a language selection page
     * @return                      the instructions page
     */
    protected abstract Page makeInstructionsPage(boolean hasLanguageSelect);

    /**
     * Makes a commit ballot page that asks the user for confirmation.
     *
     * @return                      the cast ballot page
     */
    protected abstract Page makeCommitPage();

    /**
     * @param languages             a list of the languages available
     * @return                      a language selection page that gives the user an option of different languages
     */
    protected abstract Page makeLanguageSelectPage(ArrayList<Language> languages);
    
    /**
     * @return                      a special page that is shown when an override-cancel message is received, and asks for confirmation
     */
    protected abstract Page makeOverrideCancelPage();
    
    /**
     * @return                      a special page that is shown when an override-cast message is received, and asks for confirmation
     */
    protected abstract Page makeOverrideCommitPage();

    /**
     * @return                      a success page that informs the user that the ballot was successfully committed.
     */
    protected abstract Page makeSuccessPage();

    /**
     * @return                      a size visitor that determines the size of a component specific to this layout configuration
     */
    public abstract ILayoutComponentVisitor<Object, Dimension> getSizeVisitor();

    /**
     * @return                      an image visitor that renders an image of a component specific to this layout configuration
     */
    public abstract ILayoutComponentVisitor<Boolean, BufferedImage> getImageVisitor();

    /**
     * The next unique ID to assign for a layout element
     */
    private int nextLUID = 1;

    /**
     * The next unique ID to assign for a selectable element
     */
    private int nextBUID = 1;

    /**
     * @return                      the next unique ID with an L in front (for Layout), and increments the counter
     */
    public String getNextLayoutUID() {
        return "L" + nextLUID++;
    }

    /**
     * @return                      the next unique ID with a B in front (for Ballot), and increments the counter
     */
    public String getNextBallotUID() {
        return "B" + nextBUID++;
    }

    /**
     * Sets the unique IDs of the entire ballot
     *
     * @param ballot                the ballot
     */
    public final void assignUIDsToBallot(Ballot ballot) {
        ballot.assignUIDsToBallot(this);
    }

    /**
     * Renders all images in a Layout to the disk, ignoring duplicates.
     *
     * @param layout                the layout holding images
     * @param location              the path to output the images to
     * @param progressInfo          used to indicate the status of the rendering
     */
    public void renderAllImagesToDisk(final Layout layout, final String location, ProgressInfo progressInfo) {

        /* Keeps tabs on which UIDs have been generated and written to the disk */
        final HashSet<String> uids = new HashSet<>();

        /* An easy reference for the shortname of the current language */
        final String langShortName = getLanguage().getShortName();

        /* Open a file for the destination of the images */
        File path = new File(location);
        if (!path.exists()) //noinspection ResultOfMethodCallIgnored
            path.mkdirs();

        /* A reference list of all supported languages */
        ArrayList<Language> langs = Language.getAllLanguages();

        /* A reference list for the names of all supported languages */
        final ArrayList<String> langNames = new ArrayList<>(langs.size());

        for(Language lang: langs)
           langNames.add(lang.getName());

        /* This is our visitor for the rendering, anonymously filled in here. */
        final ILayoutComponentVisitor<Object, Void> renderVisitor = new ILayoutComponentVisitor<Object, Void>() {

            private boolean first = true;

            /* TODO There must be a better way to check if we've seen this UID before... */


            /**
             * @see preptool.model.layout.ILayoutComponentVisitor#forBackground(preptool.model.layout.Background, Object[])
             */
            public Void forBackground(Background bg, Object... param) {

                /* This is how we avoid duplicates */
                if (!uids.contains(bg.getUID())) {

                    try {
                        /* Using our visitor, generate an image that we can write out */
                        BufferedImage img = bg.execute(getImageVisitor());

                        /* Create a subdirectory for this image */
                        File path = new File(location + File.separator + bg.getUID() + File.separator + bg.getUID() + "_" + langShortName + ".png");

                        /* Create the directory, if it isn't there */
                        //noinspection ResultOfMethodCallIgnored
                        path.mkdirs();

                        /* Write out the image in the specified format, e.g. /media/L71_1_en.png */
                        ImageIO.write(img, "png", path);

                    }
                    /* If we encounter an error, we need to stop since we shouldn't output incomplete ballots */
                    catch (IOException e) { throw new RuntimeException(e); }

                    uids.add(bg.getUID());
                }

                /* This is part of the weirdness of the visitor patter. TODO Rewrite the visitor? */
                return null;
            }

            /**
             * @see preptool.model.layout.ILayoutComponentVisitor#forButton(preptool.model.layout.Button, Object[])
             */
            public Void forButton(Button b, Object... param) {

                System.out.println("Button:" + b.getUID());

                if (!uids.contains(b.getUID())) {

                    try {

                        /* Using our visitor, generate an image that we can write out */
                        BufferedImage img = b.execute(getImageVisitor(), false);

                        /* Create a subdirectory for this image */
                        File path = new File(location + File.separator + b.getUID() + File.separator + b.getUID() + "_" + langShortName + ".png");

                        /* Create the directory, if it isn't there */
                        //noinspection ResultOfMethodCallIgnored
                        path.mkdirs();

                        /* Write out the image in the specified format, e.g. /media/B18/B18_en.png */
                        ImageIO.write(img, "png", path);

                        /* This will  return a focused image, i.e. one with an orange background */
                        BufferedImage focused = b.execute(getImageVisitor(), true);

                        path = new File(location + File.separator + b.getUID() + File.separator + b.getUID() + "_focused_" + langShortName + ".png");

                        //noinspection ResultOfMethodCallIgnored
                        path.mkdirs();

                        /* e.g. /media/B18/B18_focused_en.png */
                        ImageIO.write(focused, "png", path);

                    }
                    /* If we encounter an error, we need to stop since we shouldn't output incomplete ballots */
                    catch (IOException e) { throw new RuntimeException(e); }

                    uids.add(b.getUID());

                    /* If we are supposed to generate audio, do it here for this button*/
                    if(GENERATE_AUDIO)
                        forAudio(b.getUID(), b.getText());
                }

                return null;
            }

            /**
             * @see preptool.model.layout.ILayoutComponentVisitor#forLabel(preptool.model.layout.Label, Object[])
             */
            public Void forLabel(Label l, Object... param) {

                System.out.println("Label: " + l.getUID());

                if (!uids.contains(l.getUID())) {

                    try {

                        /* Using our visitor, generate an image that we can write out */
                        BufferedImage img = l.execute(getImageVisitor(), false);

                        /* Create a subdirectory for this image */
                        File path = new File(location + File.separator + l.getUID() + File.separator + l.getUID() + "_" + langShortName + ".png");

                        /* Create the directory, if it isn't there */
                        //noinspection ResultOfMethodCallIgnored
                        path.mkdirs();

                        /* Write out the image in the specified format, e.g. /media/L18/L18_en.png */
                        ImageIO.write(img, "png", path);

                        /* Create a focused version of this image */
                        BufferedImage focused = l.execute(getImageVisitor(), true);

                        /* Create a subdirectory for this image */
                        path = new File(location + File.separator + l.getUID() + File.separator + l.getUID() + "_focused_" + langShortName + ".png");

                        /* Create the directory, if it isn't there */
                        //noinspection ResultOfMethodCallIgnored
                        path.mkdirs();

                        /* e.g. /media/L18/L18_focused_en.png */
                        ImageIO.write(focused, "png", path);

                    }
                    /* If we encounter an error, we need to stop since we shouldn't output incomplete ballots */
                    catch (IOException e) { throw new RuntimeException(e); }

                    uids.add(l.getUID());

                    /*
                     * Since this a label, we have to ensure we get all of its info for sound,
                     * hence the comma separated text and description
                     */
                    if(GENERATE_AUDIO)
                        forAudio(l.getUID(), l.getText() + ", " + l.getDescription());
                }

                return null;
            }

            /**
             * @see preptool.model.layout.ILayoutComponentVisitor#forReviewButton(preptool.model.layout.ReviewButton, Object[])
             */
            @SuppressWarnings("ResultOfMethodCallIgnored")
            public Void forReviewButton(ReviewButton rb, Object... param) {

                System.out.println("Review button: " + rb.getUID());

                /* get the UID for the button */
                String uid = rb.getUID();

                /* If the UID had an underscore, get rid of it, since review buttons have uid "B1_review */
                String uuid = uid.contains("_") ? uid.substring(0, uid.indexOf('_')) : uid;

                /* Here we render the printable representation of the button */

                /* We only want to reformat candidates as no selection, not races, etc. */
                if (rb.getUID().contains("B")) {

                    /* Here is some logic for replacing the "NONE" of the voting review screen with a printable "NO SELECTION */
                    PrintButton pb = new PrintButton(rb.getUID(), (rb.getText().contains("None")) ? "NO SELECTION" : rb.getText(), getSizeVisitor());

                    /* Set the graphical elements of the pb render to mirror those of the review button */
                    pb.setBold(rb.isBold());
                    pb.setWidth(rb.getWidth());
                    pb.setIncreasedFontSize(rb.isIncreasedFontSize());

                    /* Execute the PrintButton's visitor */
                    pb.execute(this);
                }

                /* If the UID of the object begins with L, it cannot be a no selection, but it can contain the word "None" */
                if (rb.getUID().contains("L")) {

                    PrintButton pb = new PrintButton(rb.getUID(), rb.getText(), getSizeVisitor());

                    pb.setBold(rb.isBold());
                    pb.setWidth(rb.getWidth());
                    pb.setIncreasedFontSize(rb.isIncreasedFontSize());

                    /* TODO The following line may be unnecessary */
                    pb.setText(rb.getText());

                    /* Execute the PrintButton's visitor */
                    pb.execute(this);
                }


                try {

                    /* Render the ReviewButton */
                    BufferedImage image = rb.execute(getImageVisitor(), false);

                    /* If the card UID contains an L, it must be a party review button, so we need to generate a selection button as well */
                    if(rb.getUID().contains("L")) {
                        File path = new File(location + File.separator + uuid + File.separator + uuid  + "_" + langShortName + ".png");

                        /* Create the directory, if it isn't there */
                        //noinspection ResultOfMethodCallIgnored
                        path.mkdirs();

                        /* Trim the review screen image */
                        ImageIO.write(PrintImageUtils.trimImageHorizontally(image, true, 1000), "png", path);

                    }

                    /* Create a subdirectory for this image */
                    File path = new File(location + File.separator + uuid + File.separator + uuid  + "_review_" + langShortName + ".png");

                    /* Create the directory, if it isn't there */
                    //noinspection ResultOfMethodCallIgnored
                    path.mkdirs();

                    /* Trim the review screen image */
                    ImageIO.write(PrintImageUtils.trimImageHorizontally(image, true, 1000), "png", path);

                    /* Render the ReviewButton's focused version */
                    BufferedImage focusedReview = rb.execute(getImageVisitor(), true);

                    /* Create a subdirectory for this image */
                    path = new File(location + File.separator + uuid + File.separator + uid + "_focused_" + langShortName + ".png");

                    /* Create the directory, if it isn't there */
                    //noinspection ResultOfMethodCallIgnored
                    path.mkdirs();
                    ImageIO.write(focusedReview, "png", path);


                }
                catch (IOException ie){
                    throw new RuntimeException(ie);
                }

                /* Generate audio, if necessary */
                if(GENERATE_AUDIO){
                    /* Some buttons will have two names, as is the case for presidential races */
                    if(rb.getAuxText() != null)
                        forAudio(rb.getUID(), rb.getText() + "\n" + rb.getAuxText());
                    else
                        forAudio(rb.getUID(), rb.getText());
                }

                return null;
            }

            /**
             * @see preptool.model.layout.ILayoutComponentVisitor#forReviewLabel(preptool.model.layout.ReviewLabel, Object[])
             */
            public Void forReviewLabel(ReviewLabel rl, Object... param) {
                if (!uids.contains(rl.getUID())) {
                    try {

                        System.out.println("Review label: " + rl.getUID());

                        /* Generate the buffered image using the visitor and then write out the image */
                        BufferedImage img = rl.execute(getImageVisitor(), false);

                        /* Create a subdirectory for this image */
                        File path = new File(location + File.separator + rl.getUID() + File.separator + rl.getUID() + "_" + langShortName + ".png");

                        /* Create the directory, if it isn't there */
                        //noinspection ResultOfMethodCallIgnored
                        path.mkdirs();

                        /* e.g. /media/L74/L74_en.png */
                        ImageIO.write(img, "png", path);

                        /* Generate the focused version, and write it out */
                        BufferedImage focused = rl.execute(getImageVisitor(), true);

                        /* Create a subdirectory for this image */
                        path = new File(location + File.separator + rl.getUID() + File.separator + rl.getUID() + "_focused_" + langShortName + ".png");

                        /* Create the directory, if it isn't there */
                        //noinspection ResultOfMethodCallIgnored
                        path.mkdirs();

                        /* e.g. /media/L74_focused_en.png */
                        ImageIO.write(focused, "png", path);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    uids.add(rl.getUID());

                    /* Generate text-to-speech if necessary*/
                    if(GENERATE_AUDIO)
                        forAudio(rl.getUID(), rl.getText());

                }
                return null;
            }

            /**
             * @see preptool.model.layout.ILayoutComponentVisitor#forToggleButton(preptool.model.layout.ToggleButton, Object[])
             */
            @SuppressWarnings("ResultOfMethodCallIgnored")
            public Void forToggleButton(ToggleButton tb, Object... param) {
                if (!uids.contains(tb.getUID())) {
                    System.out.println("Toggle button: " + tb.getUID());

                    try {

                        /* Toggle buttons have four states:
                         *
                         *                  unselected, unfocused (empty checkbox, white bg)
                         *                  unselected, focused   (empty checkbox, orange bg)
                         *                  selected unfocused    (checked checkbox, white bg)
                         *                  selected, focused     (checked checkbox, orange bg)
                         *
                         *  Generate and write out images for each state
                         */
                        BufferedImage img = tb.execute(getImageVisitor(), false, false);
                        File path = new File(location + File.separator + tb.getUID() + File.separator + tb.getUID() + "_" + langShortName + ".png");
                        path.mkdirs();
                        ImageIO.write(img, "png", path);


                        BufferedImage focusedTb = tb.execute(getImageVisitor(), false, true);
                        path = new File(location + File.separator + tb.getUID() + File.separator + tb.getUID() + "_focused_" + langShortName + ".png");
                        path.mkdirs();
                        ImageIO.write(focusedTb, "png", path);


                        BufferedImage selectedTb = tb.execute(getImageVisitor(), true, false);
                        path = new File(location + File.separator + tb.getUID() + File.separator + tb.getUID() + "_selected_" + langShortName + ".png");
                        path.mkdirs();
                        ImageIO.write(selectedTb, "png", path);


                        BufferedImage focusedSelectedTb = tb.execute(getImageVisitor(), true, true);
                        path = new File(location + File.separator + tb.getUID() + File.separator + tb.getUID() + "_focusedSelected_" + langShortName + ".png");
                        path.mkdirs();
                        ImageIO.write(focusedSelectedTb, "png", path);


                        /* Construct a review screen version of this button */
                        ReviewButton review = new ReviewButton(tb.getUID() + "_review", tb.getBothLines(), "GoToPage", getSizeVisitor());

                        /* Added party info for ZH study here. [dsandler] */
						review.setAuxText(tb.getParty());

                        /* Set up the other rendering properties of the review button */
                        review.setBoxed(true);
                        review.setWidth(tb.getWidth());

                        /* Generate the image for this button, and handle it elsewhere in this visitor */
                        review.execute(this, param);

                        /* Create a printable version of this button */
                        PrintButton pb = new PrintButton(tb.getUID() + "_printable", tb.getText(), getSizeVisitor());

                        /* Set up the other rendering properties of the print button */
                        pb.setParty(tb.getParty());
                        pb.setSecondLine(tb.getSecondLine());
                        pb.setBold(tb.isBold());
                        pb.setWidth(tb.getWidth());
                        pb.setIncreasedFontSize(tb.isIncreasedFontSize());

                        /* Generate the image for this button, and handle it elsewhere in this visitor */
                        pb.execute(this, param);

                        /* Generate the t2s for this button*/
                        if(GENERATE_AUDIO)
                            forAudio(tb.getUID(), tb.getBothLines());


                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    uids.add(tb.getUID());
                }

                return null;
            }

            /**
             * @see preptool.model.layout.ILayoutComponentVisitor#forToggleButtonGroup(preptool.model.layout.ToggleButtonGroup, Object[])
             */
            public Void forToggleButtonGroup(ToggleButtonGroup tbg,
                    Object... param) {

                /* For a group of toggle buttons, just iterate through and visit each one */
                for (ToggleButton tb : tbg.getButtons())
                    tb.execute(this, param);

                return null;
            }

            /**
             * @see preptool.model.layout.ILayoutComponentVisitor#forPrintButton(preptool.model.layout.PrintButton, Object[])
             */
            public Void forPrintButton(PrintButton pb, Object... param) {
                System.out.println("Print button:"  + pb.getUID());

                /* Since not all uids are of equal length/don't have underscores, normalize them */
                String uid = pb.getUID();
                String uuid;

                if(uid.contains("_"))
                    uuid = uid.substring(0, uid.indexOf('_'));
                else
                    uuid = uid;

                if (!uids.contains(uid)) {
                    try {

                        /* Execute the renderer for this button */
                        BufferedImage img = pb.execute(getImageVisitor());

                        File path;

                        /* TODO I'm not sure what we're checking here, I think this code always executes... */
                        if(!langNames.contains(pb.getText())){
                            /* Create a file for the printable and then write it */
                            path = new File(location + File.separator + uuid + File.separator +uuid + "_printable_" + langShortName + ".png");

                            /* Create the directory, if it isn't there */
                            //noinspection ResultOfMethodCallIgnored
                            path.mkdirs();

                            ImageIO.write(img, "png", path);
                        }


                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    uids.add(uid);
                }
                return null;

			}

            /**
             * A private visitor which will ping google translate with a string and then recieve an mp3
             * text-to-speech of that string, in its designated language
             *
             * @param uid the UID, so the mp3 file can be saved appropriately
             * @param text the text to generate audio for
             */
            public void forAudio(String uid, String text){
                /* TODO This seems dumb, fix it*/
                if(first){
                    first = false;
                    forAudio("Selected", "Selected");
                    forAudio("Deselected", "Deselected");

                }

                /* This will hold a stream for each line of text of length 100 characters */
                ArrayList<InputStream> streams = new ArrayList<>();

                /* Google can only translate strings of less than 100 characters, se we need to break up as naturally as possible */
                String[] strings = text.split("\n");

                ArrayList<String> lines = new ArrayList<>();

                String line = "";

                /* We still need to break up strings further, since there are lines of length greater than 100*/
                for(String s : strings){
                    /* If the line is less than 100 characters long, add the next word */
                    if(line.length() + s.length()  < 99)
                        line += " " + s;

                    /* Otherwise, add the previous line to our array of lines and start a new line */
                    else {
                        lines.add(line);
                        line = s;
                    }

                }

                /* Make sure we add the last line, since it may not have been more than 100 characters long*/
                lines.add(line);

                try{
                    /* Now iterate over the lines, packaging each one in a http request */
                    for(String s : lines){
                        /* Since we can have international characters, we use UTF-8 encoding */
                        line = java.net.URLEncoder.encode(s, "UTF-8");

                        /* This is the prescribed google url */
                        URL url = new URL("http://translate.google.com/translate_tts?tl=" + langShortName + "&q="+line);

                        /* Open an HTTP connection to the specified URL */
                        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();

                        /* This fools the connection into thinking we're just a web-browser */
                        urlConn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.36 Safari/537.36");

                        /* Stream the mp3 return file into a buffer, and then stick the buffer in our list */
                        InputStream audioSrc = urlConn.getInputStream();
                        streams.add(audioSrc);
                    }

                    File path;

                    if(uid.contains("Selected") || uid.contains("Deselected"))
                        path = new File(location  + uid + "_" + langShortName + ".mp3");
                    else if (uid.contains("_"))
                        path = new File(location + uid.substring(0, uid.indexOf("_")) + File.separator+ uid + "_" + langShortName + ".mp3");
                    else
                        path = new File(location + uid + File.separator + uid + "_" + langShortName + ".mp3");

                    /* This is our output stream for our final mp3*/
                    OutputStream outstream = new FileOutputStream(path);

                    /* For each mp3, stream the data into the output. Note that by the mp3 protocol, we can just append one stream to another */
                    for(InputStream stream : streams){
                        DataInputStream read = new DataInputStream(stream);

                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = read.read(buffer)) > 0) {
                            outstream.write(buffer, 0, len);
                        }

                        /* Close the incoming stream*/
                        stream.close();
                    }

                    /* Close the output stream */
                    outstream.close();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        /* Calculate the total number of UIDs */
        final int totalIDs = nextBUID + nextLUID;

        /* Keep track of how many files we've drawn so far */
        int graphicsDrawn = 0;

        /* We're going to use a threadpool to render images and download sound, dynamically based on the local machine */
		int nProc = java.lang.Runtime.getRuntime().availableProcessors();

        /* If we're configured for multithreading, do it! */
		ExecutorService exc = Executors.newFixedThreadPool((USE_THREADS && (nProc > 1)) ? (nProc + 1): 1);

        /* This will show the progress as stuff gets rendered */
		final ProgressInfo _progressInfo = progressInfo;

        /* Iterate through and render each page */
        for (Page p : layout.getPages()) {

            /* Iterate through each page's components */
            for (ALayoutComponent c : p.getComponents()) {
				graphicsDrawn++;

                /* finalize this so it can be shown inside a different thread */
				final int graphicsDrawnF = graphicsDrawn;

                /* Finalize this so it can be used in a thread */
				final ALayoutComponent _c = c;

                /* Build our thread */
				exc.execute(new Runnable() {
					public void run() {
                        /* If the user cancels the rendering, simply do nothing */
                        /* TODO Shouldn't we kill or something? */
						if (_progressInfo.isCancelled()) return;

                        /* If we haven't already rendered this element, do so now */
						if (!uids.contains(_c.getUID())) {

                            /* Note that we're rendering*/
                            String progress = "Rendering Images";

                            /* And if we're generating audio note that too */
                            if(GENERATE_AUDIO)
                                progress += " and Generating Audio";

                            /* Update the progress bar */
							_progressInfo.setProgress(progress, 100	* graphicsDrawnF / totalIDs);

                            /* Finally, render the damn thing! */
							_c.execute(renderVisitor);
						}
					}
				});
            }
        }

        /* Tell the threadpool we're done creating new threads */
		exc.shutdown();

        /* Now wait for the threadpool ot finish*/
		while (true) {
			try {
                /* This process shouldn't take more than 2 minutes */
				exc.awaitTermination(120L, TimeUnit.SECONDS);
				break;
			} catch (java.lang.InterruptedException e) {
				/* pass */
			}
		}

    }



    /**
     * @return the language
     */
    public abstract Language getLanguage();

    /**
     * An interface for a card layout object. Cards can add information to the
     * card layout via these methods, and the specific implementation (usually
     * tied to the LayoutManager) knows what to do with it. Then the
     * makeIntoPanels method can be called and a list of JPanels is returned
     * with all information laid out properly.
     *
     * @author Corey Shaw
     */
    public interface ICardLayout {

        /**
         * Sets the title of this card
         *
         * @param title the title
         */
        public void setTitle(String title);

        /**
         * Sets a description on this card
         *
         * @param description the description
         */
        public void setDescription(String description);

        /**
         * Adds a candidate to this card layout
         *
         * @param uid the UID of the candidate
         * @param name the name of the candidate
         */
        public void addCandidate(String uid, String name);

        /**
         * Adds a candidate to this card layout
         *
         * @param uid the UID of the candidate
         * @param name the name of the candidate
         * @param party the candidate's party
         */
        public void addCandidate(String uid, String name, String party);

        /**
         * Adds a candidate to this card layout
         *
         * @param uid the UID of the candidate
         * @param name the name of the candidate
         * @param name2 the name of the running mate
         * @param party the candidate's party
         */
        public void addCandidate(String uid, String name, String name2,
                String party);

        /**
         * @return this card layout as a list of laid out JPanels
         */
        public ArrayList<JPanel> makeIntoPanels();

    }
}
