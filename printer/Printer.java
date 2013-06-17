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
import java.io.File;
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

		Printable printedBallot = new Printable(){

			public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {

                int numPages = fTotalSize / (int)pageFormat.getImageableHeight();
				if(fTotalSize % (int)pageFormat.getImageableHeight() != 0)
					numPages++;

				if(pageIndex >= numPages)
					return Printable.NO_SUCH_PAGE;


                int totalSize = _constants.getPrintableVerticalMargin();
                int printX = (int)pageFormat.getImageableX();

                int printWidth = _constants.getPrintableWidthForVVPAT();

                ArrayList<String> column1 = new ArrayList<String>();
                column1.add("L168_printable_en.png");
                column1.add("B1_printable_en.png");
                column1.add("L169_printable_en.png");
                column1.add("B2_printable_en.png");
                column1.add("L170_printable_en.png");
                column1.add("B3_printable_en.png");
                column1.add("L171_printable_en.png");
                column1.add("B4_printable_en.png");
                column1.add("L172_printable_en.png");
                column1.add("B5_printable_en.png");
                column1.add("L173_printable_en.png");
                column1.add("B6_printable_en.png");
                column1.add("L174_printable_en.png");
                column1.add("B7_printable_en.png");
                column1.add("L175_printable_en.png");
                column1.add("B8_printable_en.png");
                column1.add("L176_printable_en.png");
                column1.add("B9_printable_en.png");
                column1.add("L177_printable_en.png");
                column1.add("B10_printable_en.png");
                column1.add("L182_printable_en.png");
                column1.add("B11_printable_en.png");
                column1.add("L183_printable_en.png");
                column1.add("B12_printable_en.png");
                column1.add("L184_printable_en.png");
                column1.add("B13_printable_en.png");
                ArrayList<String> column2 = new ArrayList<String>();
                column2.add("L187_printable_en.png");
                column2.add("B16_printable_en.png");
                column2.add("L188_printable_en.png");
                column2.add("B17_printable_en.png");
                column2.add("L189_printable_en.png");
                column2.add("B18_printable_en.png");
                column2.add("L190_printable_en.png");
                column2.add("B19_printable_en.png");
                column2.add("L191_printable_en.png");
                column2.add("B20_printable_en.png");
                column2.add("L196_printable_en.png");
                column2.add("B21_printable_en.png");
                column2.add("L197_printable_en.png");
                column2.add("B22_printable_en.png");
                column2.add("L198_printable_en.png");
                column2.add("B23_printable_en.png");
                column2.add("L168_printable_en.png");
                column2.add("B1_printable_en.png");
                column2.add("L169_printable_en.png");
                column2.add("B2_printable_en.png");
                column2.add("L170_printable_en.png");
                column2.add("B3_printable_en.png");
                column2.add("L171_printable_en.png");
                column2.add("B4_printable_en.png");
                column2.add("L172_printable_en.png");
                column2.add("B5_printable_en.png");
                ArrayList<String> column3 = new ArrayList<String>();
                column3.add("L175_printable_en.png");
                column3.add("B8_printable_en.png");
                column3.add("L176_printable_en.png");
                column3.add("B9_printable_en.png");
                column3.add("L177_printable_en.png");
                column3.add("B10_printable_en.png");
                column3.add("L182_printable_en.png");
                column3.add("B11_printable_en.png");
                column3.add("L183_printable_en.png");
                column3.add("B12_printable_en.png");
                column3.add("L184_printable_en.png");
                column3.add("B13_printable_en.png");
                column3.add("L185_printable_en.png");
                column3.add("B14_printable_en.png");
                column3.add("L186_printable_en.png");
                column3.add("B15_printable_en.png");
                column3.add("L187_printable_en.png");
                column3.add("B16_printable_en.png");
                column3.add("L188_printable_en.png");
                column3.add("B17_printable_en.png");
                column3.add("L189_printable_en.png");
                column3.add("B18_printable_en.png");
                column3.add("L190_printable_en.png");
                column3.add("B19_printable_en.png");
                column3.add("L191_printable_en.png");
                column3.add("B20_printable_en.png");
                HTMLPrinter.generateHTMLFile("C:\\Users\\Mircea\\Desktop\\b.html", true, true, "C:\\Users\\Mircea\\Desktop\\long_ballot_correct\\media\\vvpat\\",
                        _constants, fbid, "C:\\Users\\Mircea\\Desktop\\long_ballot_correct\\media\\vvpat\\Barcode",
                        "C:\\Users\\Mircea\\Desktop\\long_ballot_correct\\media\\vvpat\\LineSeparator.png", column1, column2);

                //Print the date and title of the election at the top of the page
                Font font = new Font("ARIAL Unicode", Font.PLAIN, 10);
                graphics.setFont(font);
                graphics.drawString(_constants.getElectionName(), printX, totalSize+graphics.getFont().getSize());
                totalSize += graphics.getFont().getSize();

                DateFormat dateFormat = new SimpleDateFormat("MMMM d, y");
                Date date = new Date();
                graphics.drawString(dateFormat.format(date), printX, totalSize+graphics.getFont().getSize());
                totalSize += graphics.getFont().getSize() + 5; //Add a little space between the date and title

                //Generate a barcode of the bid
                //Do it here so we can use height of the barcode for laying out other components on the printout
                BufferedImage barcode = PrintImageUtils.getBarcode(fbid);

                try
                {
                    ImageIO.write(barcode, "PNG", new File("C:\\Users\\Mircea\\Desktop\\long_ballot_correct\\media\\vvpat\\Barcode.png"));
                    ImageIO.write(PrintImageUtils.flipImageHorizontally(PrintImageUtils.flipImageVertically(barcode)), "PNG", new File("C:\\Users\\Mircea\\Desktop\\long_ballot_correct\\media\\vvpat\\Barcode_flipped.png"));
                }
                catch (IOException e)
                {
                    System.out.println("Could not write barcode image to a file.");
                }

                Font ocra = new Font("OCR A Extended", Font.PLAIN, 16);

                // Draw the barcode and the ballot ID.
                graphics.setFont(ocra);
                graphics.drawString(fbid, (int)pageFormat.getImageableX(), _constants.getPrintableHeightForVVPAT()-ocra.getSize());
                graphics.drawImage(barcode, printWidth/2, _constants.getPrintableHeightForVVPAT()-barcode.getHeight(null), null);




                //Find the minimum amount of whitespace to be trimmed off title images
                int maxToTrimTitleHorizontally = Integer.MAX_VALUE;
                int maxToTrimTitleVertically = Integer.MAX_VALUE;
                for(RaceTitlePair rtp : fActualRaceNamePairs){
                    BufferedImage title = (BufferedImage)rtp.getImage();

                    maxToTrimTitleHorizontally = Math.min(PrintImageUtils.getHorizontalImageTrim(title, true), maxToTrimTitleHorizontally);
                    maxToTrimTitleVertically = Math.min(PrintImageUtils.getVerticalImageTrim(title, true), maxToTrimTitleVertically);


                }

                //Find the minimum amount of whitespace to be trimmed off selection images
                int maxToTrimSelectionHorizontally = Integer.MAX_VALUE;
                int maxToTrimSelectionVertically = Integer.MAX_VALUE;
                for(String choice :choices)
                {
                    BufferedImage selection = (BufferedImage) choiceToImage.get(choice);

                    maxToTrimSelectionHorizontally = Math.min(PrintImageUtils.getHorizontalImageTrim(selection, false), maxToTrimSelectionHorizontally);
                    maxToTrimSelectionVertically = Math.min(PrintImageUtils.getVerticalImageTrim(selection, false), maxToTrimSelectionVertically);
                }

                if(_constants.getUseTwoColumns())
                    printWidth = _constants.getPrintableWidthForVVPAT()/2;



                int initialHeight = totalSize;
                int column = 1;


                // Scaling down the graphics object, to improve print quality. The factor is 72/300 on both x and y dimensions.
                Graphics2D g = (Graphics2D) graphics;
                double xScale = .2;
                double yScale = .2;
                double xMargin = (pageFormat.getImageableWidth() - ((BufferedImage)choiceToImage.get(choices.get(1))).getWidth()*xScale)/2;
                double yMargin = (pageFormat.getImageableHeight() - ((BufferedImage)choiceToImage.get(choices.get(1))).getHeight()*yScale)/2;
//                g.translate(pageFormat.getImageableX(), pageFormat.getImageableY() + totalSize);
//                g.scale(xScale , yScale);

                int columnPrintableWidth = printWidth - printX;

                int counter = 0;
				while(totalSize < _constants.getPrintableHeightForVVPAT() && counter < choices.size()){
                    System.out.println("Counter: " +  counter);
                    System.out.println("Size of Choices: " + choices.size());
                    System.out.println("Size of fActualRaceNamePairs: " + fActualRaceNamePairs.size());
                    System.out.println(fActualRaceNamePairs.get(counter).getImage());


					BufferedImage img = (BufferedImage)choiceToImage.get(choices.get(counter));
                    BufferedImage titleImg = (BufferedImage)fActualRaceNamePairs.get(counter).getImage();




                    //Remove trailing whitespace to allow for better scaling
                    //Only the title image will have trailing whitespace due to rendering
                    titleImg = PrintImageUtils.trimImageHorizontally(titleImg, true, maxToTrimTitleHorizontally);
                    titleImg = PrintImageUtils.trimImageVertically(titleImg, true, maxToTrimTitleVertically);

                    //Remove whitespace above the selection image.
                    img = PrintImageUtils.trimImageVertically(img, false, maxToTrimSelectionVertically);

                    float percentageScaling = (1.0f * columnPrintableWidth) / printWidth;
                    int targetTitleHeight = Math.round(titleImg.getHeight() * percentageScaling);
                    int targetSelectionHeight = Math.round(img.getHeight() * percentageScaling);

//                    System.out.println("Now scaling " + choices.get(counter) + "'s outImage.");
//                    BufferedImage outImage = PrintImageUtils.getScaledInstance(img,columnPrintableWidth, targetSelectionHeight, RenderingHints.VALUE_INTERPOLATION_BICUBIC, true );
////                    System.out.println("Now scaling " + fActualRaceNamePairs.get(counter).getLabel() + "'s outImage.");
                    BufferedImage outTitle = PrintImageUtils.getScaledInstance(titleImg, columnPrintableWidth, targetTitleHeight, RenderingHints.VALUE_INTERPOLATION_BICUBIC, true);



                    g.drawImage(outTitle,
                            printX,
                            totalSize,
                            null);


					g.drawImage(img,
                            printX,
                            totalSize + Math.round(outTitle.getHeight(null)),
                            null);


					totalSize += img.getHeight(null) + outTitle.getHeight(null) + 5;
                    counter++;

                    //If we reach the end of a column and are printing in two columns, go back to the top with an offset of printwidth
                    if(totalSize + img.getHeight(null) + outTitle.getHeight(null) >= (_constants.getPrintableHeightForVVPAT() - barcode.getHeight(null))
                            && _constants.getUseTwoColumns() && column == 1){
                        totalSize = initialHeight;
                        printX += printWidth;
                        column = 2;

                    } else if (totalSize + img.getHeight(null) + outTitle.getHeight(null) >= (_constants.getPrintableHeightForVVPAT() - barcode.getHeight(null))
                            && _constants.getUseTwoColumns() && column == 2){
                        totalSize = initialHeight;
                        printX =  (int) pageFormat.getImageableX();
                        column = 1;

                    }





				}

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
        for (int i = 0; i < updatedBallot.size(); i++)
        {
            ChoicePair currentItem = updatedBallot.get(i);
        }
        return updatedBallot;
    }
    public void printedReciept(String bID){

        final String bid = bID;
        Printable printedReciept = new Printable(){

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

        printOnVVPAT(printedReciept);
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
