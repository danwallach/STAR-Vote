/**
 * @author Matt Bernhard
 * @version 0.1 5/21/13
 *
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

package printer;

import auditorium.Bugout;
import com.princexml.Prince;
import crypto.PlaintextRaceSelection;
import tap.BallotImageHelper;
import votebox.AuditoriumParams;

import javax.imageio.ImageIO;
import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.PrintQuality;
import javax.print.attribute.standard.PrinterName;
import javax.print.attribute.standard.PrinterResolution;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * This class handles all print calls made by Voteboxes, Supervisors and any future additions that will need to print
 */
@SuppressWarnings("WeakerAccess")
public class Printer{

    protected final AuditoriumParams _constants;
    private final AuditoriumParams _printerConstants;
    protected File _currentBallotFile;
    private List<List<String>> _races;
    private boolean test;

    private static int DPI_SCALE;

    /**
     * Alternate constructor
     *
     * @param ballotFile        the ballot file
     * @param races             the list of lists of all candidates in each race on a ballot
     * @param confFilePath      the filepath of the configuration file
     */
    public Printer(File ballotFile, List<List<String>> races, String confFilePath){
        this(ballotFile, races, confFilePath, false);
    }

    /**
     * Standard (well defined) constructor
     *
     * @param ballotFile        the ballot file
     * @param races             the list of lists of all candidates in each race on a ballot
     * @param confFilePath      the filepath of the configuration file
     * @param test              whether testing is enabled or not
     */
    public Printer(File ballotFile, List<List<String>> races, String confFilePath, boolean test) {
        _constants = new AuditoriumParams(confFilePath);
        _printerConstants = new AuditoriumParams("printer.conf");
        _currentBallotFile =  ballotFile;
        _races = races;
        this.test = test;
        DPI_SCALE = _constants.getPrinterDefaultDpi()/_constants.getJavaDefaultDpi();
    }

    /**
     * Alternate constructor
     *
     * @param ballotFile        the ballot file
     * @param races             the list of lists of all candidates in each race on a ballot
     */
    public Printer(File ballotFile,List<List<String>> races) {
        this(ballotFile, races, "vb.conf");
    }

    /**
     * Alternate constructor
     *
     * @param ballotFile        the ballot file
     * @param races             the list of lists of all candidates in each race on a ballot
     * @param test              whether testing is enabled or not
     */
    public Printer(File ballotFile,List<List<String>> races, boolean test){
        this(ballotFile, races, "vb.conf", test);
    }

    /**
     * Default constructor
     */
    public Printer() {
        _constants = new AuditoriumParams("supervisor.conf");
        _printerConstants = new AuditoriumParams("printer.conf");
    }

    /**
     * If a VVPAT is connected, print the voter's choices.
     *
     * @param ballot        the choices to print, in the form ((race-id choice) ...)
     * @return              true if the print executed successfully, false otherwise
     */
	@SuppressWarnings("StatementWithEmptyBody")
    public boolean printCommittedBallot(List<PlaintextRaceSelection> ballot, final String bid) {

		final Map<String, Image> choiceToImage = BallotImageHelper.loadImagesForVVPAT(_currentBallotFile);

        ArrayList<RaceTitlePair> actualRaceNameImagePairs = getRaceNameImagePairs(choiceToImage);

        final List<String> choices = new ArrayList<>();

        ArrayList<ChoicePair> reformedBallot = reformBallot(ballot);

        /* This for loop uses the corrected ballot, which accounts for No Selections. */
        for (ChoicePair currentItem : reformedBallot)
            if (currentItem.getStatus() == 1)
                choices.add(currentItem.getLabel());


        /* ---------------- This is where the HTML Printing occurs ---------------- */

        String fileChar = System.getProperty("file.separator");
        String ballotPath = _currentBallotFile.getAbsolutePath();
        String cleanFilePath = ballotPath.substring(0, ballotPath.lastIndexOf(".")) + fileChar;

        if(!test) cleanFilePath = ballotPath.substring(0, ballotPath.lastIndexOf(fileChar) + 1);

        /* Print to an HTML file. Parameters to be used: */
        String htmlFileName = cleanFilePath + "PrintableBallot.html";

        /* TODO actually read in these from somewhere */
        Boolean useTwoColumns = true;

        String path = cleanFilePath + "media" + fileChar;
        String altPath = cleanFilePath + "data" + fileChar + "media" + fileChar;
        String barcodeFileNameNoExtensionAlt = altPath + "Barcode";
        String barcodeFileNameNoExtension = path + "Barcode";
        String lineSeparatorFileNameAlt = altPath + "LineSeparator.png";
        String lineSeparatorFileName = path + "LineSeparator.png";


        /* Generate a barcode of the BID */
        /* Do it here so we can use height of the barcode for laying out other components on the printout */
        BufferedImage barcode = PrintImageUtils.getBarcode(bid);

        try
        {
            BufferedImage lineSeparator = new BufferedImage(10,10,BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = (Graphics2D) lineSeparator.getGraphics();
            g.setColor(Color.BLACK);
            g.fillRect(0,0,10,10);

            File lineSeparatorFile =  new File(lineSeparatorFileName);
            File altLineSeparatorFile = new File(lineSeparatorFileNameAlt);

            //noinspection ResultOfMethodCallIgnored
            lineSeparatorFile.mkdirs();
            ImageIO.write(lineSeparator, "png", lineSeparatorFile);
            ImageIO.write(barcode, "png", new File(barcodeFileNameNoExtension + ".png"));
            ImageIO.write(barcode, "png", new File(barcodeFileNameNoExtension + "_flipped.png"));

            ImageIO.write(lineSeparator, "png", altLineSeparatorFile);
            ImageIO.write(barcode, "png", new File(barcodeFileNameNoExtensionAlt + ".png"));
            ImageIO.write(barcode, "png", new File(barcodeFileNameNoExtensionAlt + "_flipped.png"));
        }
        catch (IOException e) { System.err.println("Could not write barcode image to a file."); }

        /* HTML Printing: Each column is an ArrayList of Strings. Each image is represented by its file name. */
        ArrayList<ArrayList<String>> columnsToPrint = new ArrayList<>();
        ArrayList<String> currentColumn = new ArrayList<>();

        /* Add at least 1 columns to print */
        columnsToPrint.add(currentColumn);

        int i = 0;

        /* For each of the selections */
        for (String selection : choices) {

            /* Add selection to 46 size columns */
            String title = actualRaceNameImagePairs.get(i).getLabel();
            currentColumn.add(title + fileChar + title + "_printable_en.png");
            currentColumn.add(selection + fileChar + selection + "_printable_en.png");
            i++;

            /* Add each column to columnsToPrint */
            if (i % 46 == 0) {

                /* Since the reference of the new column is added before things are added to it, columnsToPrint will always have a spot for the last column */
                currentColumn = new ArrayList<>();
                columnsToPrint.add(currentColumn);

                /* TODO this is for two columns stopping */
                if (i==92) break;
            }
        }

        /* Generate the HTML file with the properties set above. */
        HTMLPrinter.generateHTMLFile(htmlFileName, useTwoColumns, altPath, _constants, columnsToPrint);

        /* Get the file separator for this OS */
        String separator = File.separator;

        /* Get the OS. */
        String currentOS = _constants.getOS();

        String exePath = currentOS.equals("Windows") ? "C:\\Program Files (x86)\\Prince\\Engine\\bin\\prince.exe" :
                         currentOS.equals("Linux")   ? "/usr/local/bin/prince" : "";


        /* Attempt to convert HTML to PDF. */
        try {

            Prince prince = new Prince(exePath);

            System.out.println("Location: " + System.getProperty("user.dir") + separator + "PrintableBallot.pdf");
            prince.convert(htmlFileName, System.getProperty("user.dir") + separator + "PrintableBallot.pdf");

            /* Need to make the thread wait for the PDF to get created. */
            long start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < 1000) ;

            String foxitPath = currentOS.equals("Windows") ? "C:\\Program Files (x86)\\Foxit Software\\Foxit Reader\\FoxitReader.exe" :
                               currentOS.equals("Linux")   ? "/usr/local/bin/FoxitReader" : "";

            File testFile = new File(foxitPath);

            /* Fix so we don't have to worry about the type of install. hopefully */
            foxitPath = !testFile.exists() && currentOS.equals("Windows") ?  "C:\\Program Files\\Foxit Software\\Foxit Reader\\FoxitReader.exe" : foxitPath;

            String[] printPDFCommandArray = {foxitPath, "/t",
                                             System.getProperty("user.dir") + separator + "PrintableBallot.pdf",
                                             _printerConstants.getPrinterName() };

            new ProcessBuilder(printPDFCommandArray).start();
        }
        catch (IOException e) { System.err.println("Converting HTML to PDF failed."); e.printStackTrace(); }

        return true;
	}

    /**
     * Converts the mapping of (Race Names:Images) to a sorted ArrayList of RaceTitlePairs
     *
     * @param imageMap      a mapping of images of names to text names for all races
     * @return              a sorted ArrayList of RaceTitlePairs
     */
    protected ArrayList<RaceTitlePair> getRaceNameImagePairs(Map<String, Image> imageMap) {

        /* This ArrayList will hold all the numeric IDs that correspond to race labels. */
        ArrayList<Integer> raceNumericIDs = new ArrayList<>();

        /*
          Go through the image mapping and whenever a UID starts with "L", add the following number to raceNumericIDs.
          If a race label's image has UID L50, then this ArrayList will hold 50 to represent that race label.
        */
        for (String UID:imageMap.keySet())
            if (UID.contains("L"))
                raceNumericIDs.add(new Integer(UID.substring(1)));

        /* Now sort them by number */
        ArrayList<RaceTitlePair> sortedRaceNameImagePairs = new ArrayList<>();
        Integer[] sortedRaceNumIDArray = raceNumericIDs.toArray(new Integer[raceNumericIDs.size()]);
        Arrays.sort(sortedRaceNumIDArray);

        /* Go through each integer in the sorted array */
        for (Integer ID:sortedRaceNumIDArray) {

            /* Add the "L" back */
            String currentKey = "L" + ID.toString();

            /* Add them back to the sorted ArrayList as RaceTitlePairs (mapping of keys to images */
            sortedRaceNameImagePairs.add(new RaceTitlePair(currentKey, imageMap.get(currentKey)));
        }

        /* Returne the ArrayList */
        return sortedRaceNameImagePairs;
    }

    /**
     * Converts the raw ballot into an ArrayList of ChoicePairs for later printing
     *
     * @param rawBallot     the vote decisions as a ListExpression
     * @return              the vote decisions as an ArrayList of ChoicePairs
     */
    protected ArrayList<ChoicePair> reformBallot(List<PlaintextRaceSelection> rawBallot) {

        ArrayList<ChoicePair> reformedBallot = new ArrayList<>();

        /* Cycle through each race in the List of all races */
        for (List<String> currentRaceCandidateList : _races) {

            Boolean existingSelectedOption = false;

                /* Cycle through each PlaintextRaceSelection in the raw ballot until we find all of our candidate IDs */
                for (PlaintextRaceSelection currentRace : rawBallot) {

                    System.out.println("Current RaceSelection Title: " + currentRace.getTitle());
                    System.out.println("Current Initial Candidate: " + currentRaceCandidateList.get(0));

                    /* If the first candidate ID matches the title of the map */
                    if (currentRace.getTitle().equals(currentRaceCandidateList.get(0))) {

                        /* Now cycle through each of the candidates */
                        for (String currentCandidate : currentRaceCandidateList) {

                            System.out.println("Current candidate from RaceSelection: " + currentCandidate);
                            /* Extract the vote value for this candidate */
                            Integer currentVoteValue = currentRace.getRaceSelectionsMap().get(currentCandidate);

                            /* Write-in if they have a carat */
                            String candidate = currentCandidate.contains("^") ? currentCandidate.substring(currentCandidate.indexOf("^")+1) :
                                                                                currentCandidate;

                            /* Keep track if there has been at least one valid selection */
                            existingSelectedOption = (currentVoteValue == 1) || existingSelectedOption;

                            /* Add this ChoicePair to the reformedBallot */
                            reformedBallot.add(new ChoicePair(candidate, currentVoteValue));
                        }

                        /* If there is a valid option selected, do nothing. Otherwise, add the "No Selection" option and select that one. */
                        if (!existingSelectedOption)
                            reformedBallot.add(new ChoicePair(currentRaceCandidateList.get(0), 1));
                    }
                }
            }

        return reformedBallot;
    }

    /**
     * Renders a printed receipt for the committed ballot and sends to VVPAT for printing
     *
     * @param bid       the ballot ID
     */
    public void printedReceipt(final String bid){

        /*TODO is this supposed to be a hash? */
        Printable printedReceipt = new Printable(){

            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {

                /* Print is called by Java until NO_SUCH_PAGE is returned */
                if(pageIndex>0) return Printable.NO_SUCH_PAGE;

                /* Set up page constants */
                int pageWidth = _constants.getPaperWidthForVVPAT();
                int pageHeight = _constants.getPaperHeightForVVPAT();
                int printableWidth = _constants.getPrintableWidthForVVPAT();
                int printableHeight = _constants.getPrintableHeightForVVPAT();

                int xBound = _constants.getPrintableHeightForVVPAT()-111;

                /* Set the initial y position*/
                int y = 250;

                /* Print the first line of text after setting the font */
                graphics.setFont(new Font("Arial", 0, 16));
                printCenteredText("Thank you for voting!", xBound, y, graphics);

                /* Move down a line */
                y+=30;

                /* Print another line of text after changing the font size */
                graphics.setFont(new Font("Arial", 0, 12));
                printCenteredText("District: "+ AuditoriumParams.ELECTION_NAME, xBound, y, graphics);

                /* Move down a smaller line */
                y+=20;

                /* Print the date */
                DateFormat dateFormat = new SimpleDateFormat("MMMM d, y");
                Date date = new Date();
                printCenteredText("Date: "+ dateFormat.format(date), xBound, y, graphics);

                /* Move down a line */
                y+=30;

                /* Print a print message*/
                printCenteredText("Your ballot is currently printing", xBound, y, graphics);

                /* Move down a smaller line */
                y+=20;

                /* Print information about casting */
                printCenteredText("If you wish to cast your ballot, scan it in the scanner at this voting location", xBound, y, graphics);

                /* Move down a smaller line */
                y+=20;

                /* Print information about challenging */
                printCenteredText("If you wish to challenge your ballot, take it home and scan the QRCode below", xBound, y, graphics);

                /* Move down a smaller line*/
                y+=20;

                /* Housekeeping */
                String domain = "starvote.cs.rice.edu";
                String URL = "http://"+domain+"/ballot?ballotid="+bid;
                BufferedImage i = QRCodeGenerator.getImage(URL);

                /* Figure out the x position of the image */
                int imgStartX = xBound/2-i.getWidth()/2;

                /* Draw the image (centred) */
                graphics.drawImage(i,imgStartX,y,null);

                /* Move a line below the image */
                y+=i.getHeight()+20;

                /* Print information about view vote online */
                printCenteredText("To see your vote, please visit: "+URL , xBound, y, graphics);

                /* Move down a smaller line */
                y+=20;

                /* Print information about viewing vote online by entering BID */
                printCenteredText("Or you may go to " + domain + "/challenge and enter your ballot id: "+ bid, xBound, y, graphics);

                return Printable.PAGE_EXISTS;
            }

        };

        /* Send this to be printed */
        printOnVVPAT(printedReceipt);
    }

    /**
     * Given a string and graphic, along with horizontal margins and vertical printing location,
     * prints the string an graphic to the page (centred)
     *
     * @param s             the text to be printed to the page
     * @param xBound        the horizontal margins
     * @param y             the vertical positioning of the text/image
     * @param g             the graphic to be printed to the page
     */
    private void printCenteredText(String s, int xBound, int y, Graphics g) {

        /* Set up graphical info */
        FontMetrics fm = g.getFontMetrics();
        Rectangle2D rect = fm.getStringBounds(s, g);
        int sLength = (int) rect.getWidth();

        /* See if the side length of the rectangle is bigger than what the string can fit */
        if (sLength>fm.getStringBounds(s,g).getWidth()) {

            /* If it is, print the substring that fits and then print the rest under the graphic */
            printCenteredText(s.substring(0, sLength-1), xBound, y, g);
            printCenteredText(s.substring(sLength, s.length()), xBound, (int) (y+rect.getHeight()+5), g);
        }
        else {
            /* If it isn't, draw the string centered */
            int sXStart = xBound/2 - sLength/2;
            g.drawString(s, sXStart, y);
        }
    }

    /**
     * Prints the PIN for the user given measurements set in _constants
     *
     * @param userPin       the user PIN to be printed
     */
    public void printPin(String userPin) {

        final String pin = userPin;
        Printable printedPin = new Printable(){

            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {

                /* Print is called by Java until NO_SUCH_PAGE is returned */
                if(pageIndex>0) return Printable.NO_SUCH_PAGE;

                Image stateSeal = null;

                /* Try to read the stateSeal image from the file */
                try { stateSeal = ImageIO.read(new File("images//images//seal_tx.png")); }
                catch (IOException e) { System.out.print("Could not find TX state seal image"); e.printStackTrace(); }

                /* Measurements for the page: numbers used  are measurements for standard font */
                int pageWidth = _constants.getPaperWidthForVVPAT();
                int pageHeight = _constants.getPaperHeightForVVPAT();
                int printableWidth = _constants.getPrintableWidthForVVPAT();
                int printableHeight = _constants.getPrintableHeightForVVPAT();

                /* Settings for image/pin positioning and image scaling */
                int imgWidth = pageWidth*3/4;
                int imgHeight = pageWidth*3/4;
                int pinStartX = printableWidth/2 - 54;
                int pinStartY = printableHeight - 15;
                int imgStartX = printableWidth/2-imgWidth/2;
                int imgStartY = printableHeight-imgHeight-40;

                /* Draw the image and the string to the page*/
                graphics.drawImage(stateSeal, imgStartX, imgStartY, imgWidth, imgHeight, null);
                graphics.drawString("Your PIN is: "+ pin, pinStartX, pinStartY);

                return Printable.PAGE_EXISTS;
            }

        };

        /* Send the page to be printed on VVPAT */
        printOnVVPAT(printedPin);
    }

    /**
     * Prints onto the attached VVPAT printer, if possible.
     *
     * @param toPrint       the Printable to print.
     */
	public boolean printOnVVPAT(Printable toPrint){

		/* Check in case VVPAT not ready */
		if (_constants.getPrinterForVVPAT().equals("")) return false;

		PrintService[] printers = PrinterJob.lookupPrintServices();

		PrintService vvpat = null;

        /* Check the printer list */
		for (PrintService printer : printers) {

			PrinterName name = printer.getAttribute(PrinterName.class);

            /* When the correct printer name is found, set it as the printer */
			if(name.getValue().equals(_constants.getPrinterForVVPAT())) {
				vvpat = printer;
				break;
			}
		}

        /* Error if the printer could not be found/set */
		if(vvpat == null){
			Bugout.msg("VVPAT is configured, but not detected as ready.");
			return false;
		}

        /* Printer job housekeeping */
		PrinterJob job = PrinterJob.getPrinterJob();
        PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
        PrinterResolution pr = new PrinterResolution(300, 300, PrinterResolution.DPI);

        aset.add(pr);
        aset.add(PrintQuality.HIGH);

        /* Try to format and print the page */
		try {

            PageFormat pf = job.getPageFormat(aset);
            Paper paper = pf.getPaper();

            /* Set the printer for the job */
            job.setPrintService(vvpat) ;

            /* Send the paper the dimensions */
            paper.setSize(_constants.getPaperWidthForVVPAT(), _constants.getPaperHeightForVVPAT());

            /* Info for imageable area */
            int imageableWidth = _constants.getPrintableWidthForVVPAT();
            int imageableHeight = _constants.getPrintableHeightForVVPAT();

            int leftInset = (_constants.getPaperWidthForVVPAT() - _constants.getPrintableWidthForVVPAT()) / 2;
            int topInset = (_constants.getPaperHeightForVVPAT() - _constants.getPrintableHeightForVVPAT()) / 2;

            /* Set the imageable area beased on _constants */
            paper.setImageableArea(leftInset, topInset, imageableWidth, imageableHeight);

            /* Set the paper for the PageFormat*/
            pf.setPaper(paper);

            /* Send the printable to print to the paper */
            job.setPrintable(toPrint, pf);

            /* Print the page */
            job.print();
		}
        catch (PrinterException e) { Bugout.err("VVPAT printing failed: " + e.getMessage()); return false; }

        return true;
	}
}
