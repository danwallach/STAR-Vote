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

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import javax.imageio.ImageIO;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.PrintQuality;
import javax.print.attribute.standard.PrinterName;

import com.princexml.Prince;
import sexpression.*;
import tap.BallotImageHelper;
import votebox.AuditoriumParams;
import auditorium.*;

import javax.print.PrintService;
import javax.print.attribute.standard.PrinterResolution;

import printer.HTMLPrinter;

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
    public Printer(){
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
    public boolean printCommittedBallot(ListExpression ballot, final String bid) {

		final Map<String, Image> choiceToImage = BallotImageHelper.loadImagesForVVPAT(_currentBallotFile);

        ArrayList<RaceTitlePair> actualRaceNameImagePairs = getRaceNameImagePairs(choiceToImage);

        final List<String> choices = new ArrayList<String>();

        ArrayList<ChoicePair> reformedBallot = reformBallot(ballot);


        /* This for loop uses the corrected ballot, which accounts for No Selections. */
        for (ChoicePair currentItem : reformedBallot)
            if (currentItem.getStatus() == 1)
                choices.add(currentItem.getLabel());


        ////////////////////////////////////////////////////////////////////////////////////////////////////
        // This is where the HTML Printing occurs.

        String fileChar = System.getProperty("file.separator");
        String ballotPath = _currentBallotFile.getAbsolutePath();
        String cleanFilePath = ballotPath.substring(0, ballotPath.lastIndexOf(".")) + fileChar;

        if(!test) cleanFilePath = ballotPath.substring(0, ballotPath.lastIndexOf(fileChar) + 1);

        // Print to an HTML file. Parameters to be used:
        String htmlFileName = cleanFilePath + "PrintableBallot.html";

        /* TODO actually read in these from somewhere */
        Boolean useTwoColumns = true;
        Boolean printerFriendly = true;

        String pathToVVPATFolder = cleanFilePath +  "data" + fileChar + "media" + fileChar + "vvpat" + fileChar;
        String barcodeFileNameNoExtension = pathToVVPATFolder + "Barcode";
        String lineSeparatorFileName = pathToVVPATFolder + "LineSeparator.png";

        //Generate a barcode of the bid
        //Do it here so we can use height of the barcode for laying out other components on the printout
        BufferedImage barcode = PrintImageUtils.getBarcode(bid);

        try
        {
            BufferedImage lineSeparator = new BufferedImage(10,10,BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = (Graphics2D) lineSeparator.getGraphics();
            g.setColor(Color.BLACK);
            g.fillRect(0,0,10,10);

            ImageIO.write(lineSeparator, "PNG", new File(lineSeparatorFileName));
            ImageIO.write(barcode, "PNG", new File(barcodeFileNameNoExtension + ".png"));
            ImageIO.write(barcode, "PNG", new File(barcodeFileNameNoExtension + "_flipped.png"));
        }
        catch (IOException e) { System.err.println("Could not write barcode image to a file."); }

        /* HTML Printing: Each column is an ArrayList of Strings. Each image is represented by its file name. */
        ArrayList<ArrayList<String>> columnsToPrint = new ArrayList<ArrayList<String>>();
        ArrayList<String> currentColumn = new ArrayList<String>();

        int i = 0;

        /* For each of the selections */
        for (String selection : choices) {

            /* Add selection to 46 size columns */
            String title = actualRaceNameImagePairs.get(i).getLabel();
            currentColumn.add(title + "_printable_en.png");
            currentColumn.add(selection + "_printable_en.png");
            i++;

            /* Add each column to columnsToPrint */
            if (i % 46 == 0) {

                columnsToPrint.add(currentColumn);
                currentColumn = new ArrayList<String>();

                /* TODO this is for two columns stopping */
                if (i==92) break;
            }
        }

        // Generate the HTML file with the properties set above.
        HTMLPrinter.generateHTMLFile(htmlFileName, useTwoColumns, printerFriendly, pathToVVPATFolder, _constants, bid, barcodeFileNameNoExtension, lineSeparatorFileName, columnsToPrint);

        // Get the file that is to be read for commands and its parameter separator string.
        String filename = _printerConstants.getCommandsFileFilename();
        String fileSeparator = _printerConstants.getCommandsFileParameterSeparator();

        // Open the file.
        File file = new File (filename);

        // If the file does not exist, then print error.
        if (!file.exists()) System.err.println("The specified file could not be found: " + filename);

        // Create holders for the two commands.
        String convertHTMLtoPDFCommandLine = "";
        String printPDFCommandLine = "";

        // Create the reader.
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(file.getAbsoluteFile()));
            convertHTMLtoPDFCommandLine = reader.readLine();
            printPDFCommandLine = reader.readLine();
            reader.close();
            System.out.println(convertHTMLtoPDFCommandLine);
        }
        catch (IOException e) { System.err.println("Unable to read from file " + filename); e.printStackTrace(); }

        // Get the OS.
        String currentOS = _constants.getOS();

        if (currentOS.equals("Windows")) {

            // Create arrays of the command and its parameters (to use with the exec method in JDK 7+
            String[] convertHTMLtoPDFCommandArray = convertHTMLtoPDFCommandLine.split(fileSeparator);
            String[] printPDFCommandArray = printPDFCommandLine.split(fileSeparator);

            // Attempt to convert HTML to PDF.
            try { Runtime.getRuntime().exec(convertHTMLtoPDFCommandArray); }
            catch (IOException e) { System.err.println("Converting HTML to PDF failed."); e.printStackTrace(); }

            // Need to make the thread wait for the PDF to get created.
            long start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < 1000);

            // Attempt to print PDF.
            try { Runtime.getRuntime().exec(printPDFCommandArray); }
            catch (IOException e) { System.err.println("Printing PDF failed."); e.printStackTrace(); }
        }
        else {

            if (currentOS.equals("Linux")) {

                // Attempt to convert HTML to PDF.
                try {
                    Prince prince = new Prince("/usr/local/bin/prince");
                    System.out.println("Location: " + System.getProperty("user.dir") + "/ballot_printout.pdf");
                    prince.convert(htmlFileName, System.getProperty("user.dir") + "/ballot_printout.pdf");
                }
                catch (IOException e) { System.err.println("Converting HTML to PDF failed."); e.printStackTrace(); }

                // Need to make the thread wait for the PDF to get created.
                long start = System.currentTimeMillis();
                while (System.currentTimeMillis() - start < 1000) ;

            }
        }
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
        ArrayList<Integer> raceNumericIDs = new ArrayList<Integer> ();

        /*
          Go through the image mapping and whenever a UID starts with "L", add the following number to raceNumericIDs.
          If a race label's image has UID L50, then this ArrayList will hold 50 to represent that race label.
        */
        for (String UID:imageMap.keySet())
            if (UID.contains("L"))
                raceNumericIDs.add(new Integer(UID.substring(1)));

        /* Now sort them by number */
        ArrayList<RaceTitlePair> sortedRaceNameImagePairs = new ArrayList<RaceTitlePair> ();
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
    protected ArrayList<ChoicePair> reformBallot(ListExpression rawBallot) {

        ArrayList<ChoicePair> reformedBallot = new ArrayList<ChoicePair>();

        /* Cycle through each race in the List of all races */
        for (List<String> currentRace : _races) {

            Boolean existingSelectedOption = false;

            /* Cycle through each label in each race */
            for (String currentLabel : currentRace) {

                /* Cycle through each ASExpression in the raw ballot until we find our currentLabel */
                for (ASExpression curChoice : rawBallot) {

                    ListExpression currentChoice = (ListExpression) curChoice;

                    /* If the first element of the ListExpression is the current String... */
                    if (currentChoice.get(0).toString().equals(currentLabel)) {

                        ASExpression voteChoice = currentChoice.get(1);

                        /* See if the option was selected */
                        if (voteChoice.toString().equals("1")) {


                            /* Reflect that a valid option was selected */
                            existingSelectedOption = true;

                            /* Add the choice-decision pairing to the ArrayList */
                            reformedBallot.add(new ChoicePair(currentLabel, 1));
                            break;
                        }

                        /* See if it was not selected */
                        else if (voteChoice.toString().equals("0")) {

                            /* Add the choice-decision pairing to the ArrayList */
                            reformedBallot.add(new ChoicePair(currentLabel, 0));
                            break;
                        }

                        /* Write-in */
                        else {

                            /* Check if the write-in was selected */
                            if (voteChoice.toString().charAt(0) == '1') {

                                /* Reflect that a valid option was selected */
                                existingSelectedOption = true;

                                /* Add the choice-decision pairing to the ArrayList */
                                reformedBallot.add(new ChoicePair(currentLabel, 1));
                                break;
                            }

                            /* Otherwise, just show it as not selected */
                            else {

                                /* Add the choice-decision pairing to the ArrayList */
                                reformedBallot.add(new ChoicePair(currentLabel, 0));
                                break;
                            }
                        }
                    }
                }
            }

            /* If there is a valid option selected, do nothing. Otherwise, add the "No Selection" option and select that one. */
            if (!existingSelectedOption)
                reformedBallot.add(new ChoicePair(currentRace.get(0), 1));
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

                //print is called by Java until NO_SUCH_PAGE is returned
                if(pageIndex>0) return Printable.NO_SUCH_PAGE;


                int pageWidth = _constants.getPaperWidthForVVPAT();
                int pageHeight = _constants.getPaperHeightForVVPAT();
                int printableWidth = _constants.getPrintableWidthForVVPAT();
                int printableHeight = _constants.getPrintableHeightForVVPAT();

                int xBound = _constants.getPrintableHeightForVVPAT()-111;

                int y = 250;

                graphics.setFont(new Font("Arial", 0, 16));
                printCenteredText("Thank you for voting!", xBound, y, graphics);

                y+=30;

                graphics.setFont(new Font("Arial", 0, 12));
                printCenteredText("District: "+ AuditoriumParams.ELECTION_NAME, xBound, y, graphics);

                y+=20;

                DateFormat dateFormat = new SimpleDateFormat("MMMM d, y");
                Date date = new Date();
                printCenteredText("Date: "+ dateFormat.format(date), xBound, y, graphics);

                y+=30;

                printCenteredText("Your ballot is currently printing", xBound, y, graphics);

                y+=20;

                printCenteredText("If you wish to cast your ballot, scan it in the scanner at this voting location", xBound, y, graphics);

                y+=20;

                printCenteredText("If you wish to challenge your ballot, take it home and scan the QRCode below", xBound, y, graphics);

                y+=20;

                String domain = "starvote.cs.rice.edu";
                String URL = "http://"+domain+"/ballot?ballotid="+bid;
                BufferedImage i = QRCodeGenerator.getImage(URL);

                int imgStartX = xBound/2-i.getWidth()/2;

                graphics.drawImage(i,imgStartX,y,null);

                y+=i.getHeight()+20;

                printCenteredText("To see your vote, please visit: "+URL , xBound, y, graphics);

                y+=20;

                printCenteredText("Or you may go to " + domain + "/challenge and enter your ballot id: "+ bid, xBound, y, graphics);

                return Printable.PAGE_EXISTS;
            }

        };

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

        FontMetrics fm = g.getFontMetrics();
        Rectangle2D rect = fm.getStringBounds(s, g);
        int sLength = (int) rect.getWidth();

        if (sLength>fm.getStringBounds(s,g).getWidth()) {
            printCenteredText(s.substring(0, sLength-1), xBound, y, g);
            printCenteredText(s.substring(sLength, s.length()), xBound, (int) (y+rect.getHeight()+5), g);
        }
        else {
            int sXStart = xBound/2 - sLength/2;
            g.drawString(s, sXStart, y);
        }
    }

    //this class prints the pin for the user given measurements for a POS terminal printer

    /**
     * Prints the PIN for the user given measurements set in _constants
     *
     * @param userPin       the user PIN to be printed
     */
    public void printPin(String userPin) {

        final String pin = userPin;
        Printable printedPin = new Printable(){

            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {

                //print is called by Java until NO_SUCH_PAGE is returned
                if(pageIndex>0) return Printable.NO_SUCH_PAGE;

                Image stateSeal = null;
                try { stateSeal = ImageIO.read(new File("images//images//seal_tx.png")); }
                catch (IOException e) { System.out.print("Could not find TX state seal image"); e.printStackTrace(); }

                //numbers are measurements for standard font
                int pageWidth = _constants.getPaperWidthForVVPAT();
                int pageHeight = _constants.getPaperHeightForVVPAT();
                int printableWidth = _constants.getPrintableWidthForVVPAT();
                int printableHeight = _constants.getPrintableHeightForVVPAT();

                int imgWidth = pageWidth*3/4;
                int imgHeight = pageWidth*3/4;
                int pinStartX = printableWidth/2 - 54;
                int pinStartY = printableHeight - 15;
                int imgStartX = printableWidth/2-imgWidth/2;
                int imgStartY = printableHeight-imgHeight-40;

                graphics.drawImage(stateSeal, imgStartX, imgStartY, imgWidth, imgHeight, null);
                graphics.drawString("Your PIN is: "+ pin, pinStartX, pinStartY);

                return Printable.PAGE_EXISTS;
            }

        };

        printOnVVPAT(printedPin);
    }

    /**
     * Prints onto the attached VVPAT printer, if possible.
     *
     * @param toPrint       the Printable to print.
     */
	public boolean printOnVVPAT(Printable toPrint){

		//VVPAT not ready
		if (_constants.getPrinterForVVPAT().equals("")) return false;

		PrintService[] printers = PrinterJob.lookupPrintServices();

		PrintService vvpat = null;

		for (PrintService printer : printers) {

			PrinterName name = printer.getAttribute(PrinterName.class);

			if(name.getValue().equals(_constants.getPrinterForVVPAT())) {
				vvpat = printer;
				break;
			}
		}

        if(vvpat == null) System.out.println("No available printers");
        else System.out.println("Printing on " + vvpat.getName());

		if(vvpat == null){
			Bugout.msg("VVPAT is configured, but not detected as ready.");
			return false;
		}

		PrinterJob job = PrinterJob.getPrinterJob();

        PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
        PrinterResolution pr = new PrinterResolution(300, 300, PrinterResolution.DPI);

        aset.add(pr);
        aset.add(PrintQuality.HIGH);

		try {

            PageFormat pf = job.getPageFormat(aset);
            Paper paper = pf.getPaper();

            job.setPrintService(vvpat) ;

            paper.setSize(_constants.getPaperWidthForVVPAT(), _constants.getPaperHeightForVVPAT());

            int imageableWidth = _constants.getPrintableWidthForVVPAT();
            int imageableHeight = _constants.getPrintableHeightForVVPAT();

            int leftInset = (_constants.getPaperWidthForVVPAT() - _constants.getPrintableWidthForVVPAT()) / 2;
            int topInset = (_constants.getPaperHeightForVVPAT() - _constants.getPrintableHeightForVVPAT()) / 2;

            paper.setImageableArea(leftInset, topInset, imageableWidth, imageableHeight);

            pf.setPaper(paper);

            job.setPrintable(toPrint, pf);

            job.print();
		}
        catch (PrinterException e) { Bugout.err("VVPAT printing failed: " + e.getMessage()); return false; }

        return true;
	}
}
