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


import sexpression.*;
import tap.BallotImageHelper;
import votebox.AuditoriumParams;
import auditorium.*;

import javax.print.PrintService;
import javax.print.attribute.standard.PrinterResolution;

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
		final Map<String, Image> choiceToImage = BallotImageHelper.loadImagesForVVPAT(_currentBallotFile);
        final Map<String, Image> raceTitles = BallotImageHelper.loadBallotTitles(_currentBallotFile);

        final String fbid = bid;

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

                System.out.println(counter++);

                int numPages = fTotalSize / (int)pageFormat.getImageableHeight();
				if(fTotalSize % (int)pageFormat.getImageableHeight() != 0)
					numPages++;

				if(pageIndex >= numPages)
					return Printable.NO_SUCH_PAGE;


                int totalSize = _constants.getPrintableVerticalMargin();
                int printX = (int)pageFormat.getImageableX();

                int printWidth = _constants.getPrintableWidthForVVPAT();


                //Print the date and title of the election at the top of the page
                Font font = new Font("ARIAL Unicode", Font.PLAIN, 10);
                graphics.setFont(font);
                graphics.drawString(_constants.getElectionName(), printX, totalSize+graphics.getFont().getSize());
                totalSize += graphics.getFont().getSize();

                DateFormat dateFormat = new SimpleDateFormat("MMMM d, y");
                Date date = new Date();
                graphics.drawString(dateFormat.format(date), printX, totalSize+graphics.getFont().getSize());
                totalSize += graphics.getFont().getSize();

                //Generate a barcode of the bid
                //Do it here so we can use height of the barcode for laying out other components on the printout
                BufferedImage barcode = PrintImageUtils.getBarcode(fbid);

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

                printWidth  *= DPI_SCALE;

                int initialHeight = totalSize;
                int column = 1;


                // Scaling down the graphics object, to improve print quality. The factor is 72/300 on both x and y dimensions.
                Graphics2D g = (Graphics2D) graphics;
                double xScale = .2;
                double yScale = .2;
                double xMargin = (pageFormat.getImageableWidth() - ((BufferedImage)choiceToImage.get(choices.get(1))).getWidth()*xScale)/2;
                double yMargin = (pageFormat.getImageableHeight() - ((BufferedImage)choiceToImage.get(choices.get(1))).getHeight()*yScale)/2;
                g.translate(pageFormat.getImageableX(), pageFormat.getImageableY() + totalSize);
                g.scale(xScale , yScale);



                int counter = 0;
				while(totalSize < _constants.getPrintableHeightForVVPAT()*DPI_SCALE && counter < choices.size()){

					BufferedImage img = (BufferedImage)choiceToImage.get(choices.get(counter));
                    BufferedImage titleImg = (BufferedImage)fActualRaceNamePairs.get(counter).getImage();


                    //Remove trailing whitespace to allow for better scaling
                    //Only the title image will have trailing whitespace due to rendering
                    titleImg = PrintImageUtils.trimImageHorizontally(titleImg, true, maxToTrimTitleHorizontally);
                    titleImg = PrintImageUtils.trimImageVertically(titleImg, true, maxToTrimTitleVertically);

                    //Remove whitespace above the selection image.
                    img = PrintImageUtils.trimImageVertically(img, false, maxToTrimSelectionVertically);

//                    System.out.println("Now scaling " + choices.get(counter) + "'s outImage.");
//                    BufferedImage outImage = PrintImageUtils.getScaledInstance(img,(printWidth*2)/3, (titleImg.getHeight()*2)/3, RenderingHints.VALUE_INTERPOLATION_BICUBIC, true );
//                    System.out.println("Now scaling " + fActualRaceNamePairs.get(counter).getLabel() + "'s outImage.");
//                    BufferedImage outTitle = PrintImageUtils.getScaledInstance(titleImg, (printWidth*2)/3, (titleImg.getHeight()*2)/3, RenderingHints.VALUE_INTERPOLATION_BICUBIC, true);



                    g.drawImage(titleImg,
                            printX,
                            totalSize,
                            null);


					g.drawImage(img,
                            printX,
                            totalSize + Math.round(titleImg.getHeight(null)),
                            null);


					totalSize += img.getHeight(null) + titleImg.getHeight(null);
                    counter++;

                    //If we reach the end of a column and are printing in two columns, go back to the top with an offset of printwidth
                    if(totalSize + img.getHeight(null) + titleImg.getHeight(null) >= (_constants.getPrintableHeightForVVPAT() - barcode.getHeight(null)) * DPI_SCALE
                            && _constants.getUseTwoColumns() && column == 1){
                        totalSize = initialHeight;
                        printX += printWidth;
                        column = 2;

                    } else if (totalSize + img.getHeight(null) + titleImg.getHeight(null) >= (_constants.getPrintableHeightForVVPAT() - barcode.getHeight(null)) * DPI_SCALE
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

        System.out.println(vvpat.getName());

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
