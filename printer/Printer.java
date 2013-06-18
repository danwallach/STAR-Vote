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
import javax.print.attribute.HashPrintJobAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttribute;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.PrintQuality;
import javax.print.attribute.standard.PrinterName;


import preptool.model.layout.manager.RenderingUtils;
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
public class Printer {

    private final AuditoriumParams _constants;
    private File _currentBallotFile;
    private List<List<String>> _races;

    public static int counter = 0;

    public static int DPI_SCALE;

    public Printer(File ballotFile,List<List<String>> races) {
        _constants = new AuditoriumParams("vb.conf");
        _currentBallotFile =  ballotFile;
        _races = races;
        DPI_SCALE = _constants.getPrinterDefaultDpi()/_constants.getJavaDefaultDpi();
    }

    public Printer(){
        _constants = new AuditoriumParams("supervisor.conf");
    }

    /**
     * If a VVPAT is connected,
     *   print the voter's choices.
     *
     * @param ballot - the choices to print, in the form ((race-id choice) ...)
     */
	public void printCommittedBallot(ListExpression ballot, String bid) {
        System.out.println("Current Ballot: " + _currentBallotFile.getAbsolutePath());
		final Map<String, Image> choiceToImage = BallotImageHelper.loadImagesForVVPAT(_currentBallotFile);
        final Map<String, Image> raceTitles = BallotImageHelper.loadBallotTitles(_currentBallotFile);
;

        final String fbid = bid;

        //System.out.println()
//        System.out.println("Number of races " + (choiceToImage).size());
//        for(String i : choiceToImage.keySet()){
//            System.out.println("Image title: " + i);
//        }

        ArrayList<RaceTitlePair> actualRaceNameImagePairs = getRaceNameImagePairs(choiceToImage);

        final List<String> choices = new ArrayList<String>();

        ArrayList<ChoicePair> correctedBallot = correctBallot(ballot);


        /* This for loop uses the corrected ballot, which accounts for No Selections. */
        for(int i = 0; i < correctedBallot.size(); i++)
        {
            ChoicePair currentItem = correctedBallot.get(i);
            if (currentItem.getStatus() == 1)
                choices.add(currentItem.getLabel());
        }
        /* Build an ArrayList of Race Titles. */
        ArrayList<RaceTitlePair> raceTitlePairs = new ArrayList<RaceTitlePair>();
        for (String raceTitleLabel:raceTitles.keySet())
        {
            raceTitlePairs.add(new RaceTitlePair(raceTitleLabel, raceTitles.get(raceTitleLabel)));
        }


		int totalSize = 0;
		for(int i = 0; i < choices.size(); i++) {
            String currentImageKey = choices.get(i);
            Image img = choiceToImage.get(currentImageKey);

			totalSize += img.getHeight(null);
        }

		final int fTotalSize = totalSize;
        final ArrayList<RaceTitlePair> fActualRaceNamePairs = actualRaceNameImagePairs;




        ////////////////////////////////////////////////////////////////////////////////////////////////////
        // This is where the HTML Printing occurs.

        String cleanFilePath = _currentBallotFile.getAbsolutePath().substring(0, _currentBallotFile.getAbsolutePath().lastIndexOf('\\') + 1);
        // Print to an HTML file. Parameters to be used:
        String htmlFileName = cleanFilePath + "PrintableBallot.html";
        Boolean useTwoColumns = true;
        Boolean printerFriendly = true;
        String pathToVVPATFolder = cleanFilePath + "data\\media\\vvpat\\";
        String barcodeFileNameNoExtension = pathToVVPATFolder + "Barcode";
        String lineSeparatorFileName = pathToVVPATFolder + "LineSeparator.png";

        //Generate a barcode of the bid
        //Do it here so we can use height of the barcode for laying out other components on the printout
        BufferedImage barcode = PrintImageUtils.getBarcode(fbid);
        try
        {
            BufferedImage lineSeparator = new BufferedImage(10,10,BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = (Graphics2D) lineSeparator.getGraphics();
            g.setColor(Color.BLACK);
            g.fillRect(0,0,10,10);

            ImageIO.write(lineSeparator, "PNG", new File(lineSeparatorFileName));
            ImageIO.write(barcode, "PNG", new File(barcodeFileNameNoExtension + ".png"));
            ImageIO.write(PrintImageUtils.flipImageHorizontally(PrintImageUtils.flipImageVertically(barcode)), "PNG", new File(barcodeFileNameNoExtension + "_flipped.png"));
        }
        catch (IOException e)
        {
            System.out.println("Could not write barcode image to a file.");
        }

        // HTML Printing: Each column is an ArrayList of Strings. Each image is represented by its file name.
        ArrayList<ArrayList<String>> columnsToPrint = new ArrayList<ArrayList<String>>();
        int counter = 0;
        while (counter < choices.size())
        {
            ArrayList<String> currentColumn = new ArrayList<String>();
            while ((currentColumn.size() < 26) && (counter < choices.size()))
            {
                String titleName = fActualRaceNamePairs.get(counter).getLabel();
                String selectionName = choices.get(counter);
                currentColumn.add(titleName + "_printable_en.png");
                currentColumn.add(selectionName + "_printable_en.png");
                counter++;
            }
            columnsToPrint.add(currentColumn);
        }

        // Generate the HTML file with the properties set above.
        HTMLPrinter.generateHTMLFile(htmlFileName, useTwoColumns, printerFriendly, pathToVVPATFolder, _constants, fbid, barcodeFileNameNoExtension, lineSeparatorFileName, columnsToPrint);

        // Get the file that is to be read for commands and its parameter separator string.
        String filename = "C:\\Users\\Mircea\\Desktop\\CommandFile.txt";
        String fileSeparator = "###";

        // Open the file.
        File file = new File (filename);

        // If the file does not exist, then print error.
        if (!file.exists())
        {
            System.err.println("The specified file could not be found: " + filename);
        }

        // Create holders for the two commands.
        String convertHTMLtoPDFCommandLine = "";
        String printPDFCommandLine = "";

        // Create the reader.
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new FileReader(file.getAbsoluteFile()));
            convertHTMLtoPDFCommandLine = reader.readLine();
            printPDFCommandLine = reader.readLine();
            reader.close();
        }
        catch (IOException e)
        {
            System.out.println("Unable to read from file " + filename);
            e.printStackTrace();
        }

        // Create arrays of the command and its parameters (to use with the exec method in JDK 7+
        String[] convertHTMLtoPDFCommandArray = convertHTMLtoPDFCommandLine.split(fileSeparator);
        String[] printPDFCommandArray = printPDFCommandLine.split(fileSeparator);

        // Attempt to convert HTML to PDF.
        try
        {
            Runtime.getRuntime().exec(convertHTMLtoPDFCommandArray);

        }
        catch (IOException e)
        {
            System.err.println("Converting HTML to PDF failed.");
            e.printStackTrace();
        }

        // Attempt to print PDF.
        try
        {
            Runtime.getRuntime().exec(printPDFCommandArray);

        }
        catch (IOException e)
        {
            System.err.println("Printing PDF failed.");
            e.printStackTrace();
        }


        ////////////////////////////////////////////////////////////////////////////////////////////////////




		Printable printedBallot = new Printable(){

			public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {

                int numPages = fTotalSize / (int)pageFormat.getImageableHeight();
				if(fTotalSize % (int)pageFormat.getImageableHeight() != 0)
					numPages++;

				if(pageIndex >= numPages)
					return Printable.NO_SUCH_PAGE;

				return Printable.PAGE_EXISTS;

			}

		};

		printOnVVPAT(printedBallot);


	}

    private ArrayList<RaceTitlePair> getRaceNameImagePairs(Map<String, Image> imageMap) {
        // This ArrayList holds all the numeric IDs that correspond to race labels.
        // If a race label's image has UID L50, then this ArrayList will hold 50 to represent that race label.
        ArrayList<Integer> raceNumericIDs = new ArrayList<Integer> ();
        for (String UID:imageMap.keySet())
        {
            if (UID.contains("L"))
            {
                raceNumericIDs.add(new Integer(UID.substring(1)));
            }
        }
        ArrayList<RaceTitlePair> sortedRaceNameImagePairs = new ArrayList<RaceTitlePair> ();
        Integer[] sortedRaceNumIDArray = raceNumericIDs.toArray(new Integer[0]);
        Arrays.sort(sortedRaceNumIDArray);

        for (Integer ID:sortedRaceNumIDArray)
        {
            String currentKey = "L" + ID.toString();
            sortedRaceNameImagePairs.add(new RaceTitlePair(currentKey, imageMap.get(currentKey)));
        }
        return sortedRaceNameImagePairs;
    }

    private ArrayList<ChoicePair> correctBallot(ListExpression rawBallot) {
        // List of races is called: _races
        ArrayList<ChoicePair> updatedBallot = new ArrayList<ChoicePair>();
        for (int raceIdx = 0; raceIdx < _races.size(); raceIdx++)
        {
            List<String> currentRace = _races.get(raceIdx);
            Boolean existingSelectedOption = false;

            for (int labelIdx = 0; labelIdx < currentRace.size(); labelIdx++)
            {
                String currentLabel = currentRace.get(labelIdx);
                for (int choiceIdx = 0; choiceIdx < rawBallot.size(); choiceIdx++)
                {
                    ListExpression currentChoice = (ListExpression)rawBallot.get(choiceIdx);
                    if (currentChoice.get(0).toString().equals(currentLabel))
                    {
                        if (currentChoice.get(1).toString().equals("1"))
                        {
                            // THIS option was selected.
                            existingSelectedOption = true;
                            updatedBallot.add(new ChoicePair(currentLabel,new Integer(1)));
                            break;
                        }
                        else if (currentChoice.get(1).toString().equals("0")) // The if statement checks for consistency, but is not required.
                        {
                            updatedBallot.add(new ChoicePair(currentLabel,new Integer(0)));
                            break;
                        }
                    }
                }
            }

            // If there is a valid option selected, do nothing. Otherwise, add the "No Selection" option and select that one.
            if (!existingSelectedOption)
            {
                updatedBallot.add(new ChoicePair(currentRace.get(0),new Integer(1)));
            }
        }

        // Print the updated ballot (for consistency checking).
        /*for (int i = 0; i < updatedBallot.size(); i++)
        {
            ChoicePair currentItem = updatedBallot.get(i);
        }*/
        return updatedBallot;
    }

    public void printedReceipt(String bID){

        final String bid = bID;
        Printable printedReceipt = new Printable(){

            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                //print is called by Java until NO_SUCH_PAGE is returned
                if(pageIndex>0)
                    return Printable.NO_SUCH_PAGE;


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
                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                Date date = new Date();
                printCenteredText("Date: "+ dateFormat.format(date), xBound, y, graphics);
                y+=30;
                printCenteredText("Your ballot is currently printing", xBound, y, graphics);
                y+=20;
                printCenteredText("If you wish to cast your ballot, scan it in the scanner at this voting location", xBound, y, graphics);
                y+=20;
                printCenteredText("If you wish to challenge your ballot, take it home and scan the QRCode below", xBound, y, graphics);
                y+=20;
                QRCodeGenerator qGen = new QRCodeGenerator();
                BufferedImage i = qGen.getImage(bid);
                int imgStartX = xBound/2-i.getWidth()/2;
                graphics.drawImage(i,imgStartX,y,null);

                return Printable.PAGE_EXISTS;
            }

        };

        printOnVVPAT(printedReceipt);
    }

    private void printCenteredText(String s, int xBound, int y, Graphics g){
        FontMetrics fm = g.getFontMetrics();
        Rectangle2D rect = fm.getStringBounds(s, g);
        int sLength = (int) rect.getWidth();
        if(sLength>fm.getStringBounds(s,g).getWidth()){
            printCenteredText(s.substring(0, sLength-1), xBound, (int) (y), g);
            printCenteredText(s.substring(sLength, s.length()), xBound, (int) (y+rect.getHeight()+5), g);
        }
        else{
            int sXStart = xBound/2 - sLength/2;
            g.drawString(s, sXStart, y);
        }
    }

    //this class prints the pin for the user given measurements for a POS terminal printer
    public void printPin(String userPin){

        final String pin = userPin;
        Printable printedPin = new Printable(){

            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                //print is called by Java until NO_SUCH_PAGE is returned
                if(pageIndex>0)
                    return Printable.NO_SUCH_PAGE;

                Image stateSeal = null;
                try {
                    stateSeal = ImageIO.read(new File("images//images//seal_tx.png"));
                } catch (IOException e) {
                    System.out.print("Could not find TX state seal image");
                    e.printStackTrace();
                }
                //numbers are measurements for standard font
                int pageWidth = _constants.getPaperWidthForVVPAT();
                int pageHeight = _constants.getPaperHeightForVVPAT();
                int printableWidth = _constants.getPrintableWidthForVVPAT();
                int printableHeight = _constants.getPrintableHeightForVVPAT();

                int imgWidth = pageWidth*3/4;
                int imgHeight = pageWidth*3/4;
                int pinStartX = printableWidth/2 - 54;
                int pinStartY = printableHeight - 15;
                int imgStartX = pageWidth/2-imgWidth/2;
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
     * @param toPrint - the Printable to print.
     */
	public void printOnVVPAT(Printable toPrint){
		//VVPAT not ready
		if(_constants.getPrinterForVVPAT().equals("")) return;

		PrintService[] printers = PrinterJob.lookupPrintServices();

        System.out.println("There are " + printers.length + " printers:");

        for(PrintService printer : printers){
            System.out.println(printer.getName());
        }

		PrintService vvpat = null;

		for(PrintService printer : printers){
			PrinterName name = printer.getAttribute(PrinterName.class);
			if(name.getValue().equals(_constants.getPrinterForVVPAT())){
				vvpat = printer;
				break;
			}//if
		}//for
        if(vvpat == null) System.out.println("No available printers");
        else System.out.println(vvpat.getName());

		if(vvpat == null){
			Bugout.msg("VVPAT is configured, but not detected as ready.");
			return;
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

		} catch (PrinterException e) {
			Bugout.err("VVPAT printing failed: "+e.getMessage());
			return;
		}


	}


}
