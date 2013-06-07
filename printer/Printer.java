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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import javax.print.attribute.standard.PrinterName;


import sexpression.*;
import tap.BallotImageHelper;
import votebox.AuditoriumParams;
import auditorium.*;

import javax.print.PrintService;

import net.sourceforge.barbecue.Barcode;

/**
 * This class handles all print calls made by Voteboxes, Supervisors and any future additions that will need to print
 */
public class Printer {

    private final AuditoriumParams _constants;
    private File _currentBallotFile;
    private List<List<String>> _races;

    public Printer(File ballotFile,List<List<String>> races) {
        _constants = new AuditoriumParams("vb.conf");
        _currentBallotFile =  ballotFile;
        _races = races;
    }

    /**
     * If a VVPAT is connected,
     *   print a message indicating that this ballot is "spoiled" and will not be counted."
     */
    /*protected void printBallotSpoiled() {

    	//TODO: Change this to use prerendered images (pulled from ballot, probably) rather than bringing Java font rendering code into
    	//      VoteBox
		Printable spoiled = new Printable(){
			public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
				if(pageIndex != 0) return Printable.NO_SUCH_PAGE;

				String text = "BALLOT SPOILED";
				FontRenderContext context = new FontRenderContext(new AffineTransform(), false, true);

				Rectangle2D bounds = graphics.getFont().getStringBounds(text, context);

				while(bounds.getWidth() < pageFormat.getImageableWidth()){
					text = "*" + text+ "*";
					bounds = graphics.getFont().getStringBounds(text, context);
				}

				graphics.drawString(text, (int)pageFormat.getImageableX(), (int)bounds.getHeight());

				return Printable.PAGE_EXISTS;
			}
		};

		printOnVVPAT(spoiled);
	}*/

    /**
     * If a VVPAT is connected,
     *   print a "confirmation" of the ballot being counted.
     */
    /*protected void printBallotCastConfirmation() {
    	//TODO: Make this use prerendered elements instead of Font
    	Printable confirmed = new Printable(){
			public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
				if(pageIndex != 0) return Printable.NO_SUCH_PAGE;

				String text = "--BALLOT CAST--";
				FontRenderContext context = new FontRenderContext(new AffineTransform(), false, true);

				Rectangle2D bounds = graphics.getFont().getStringBounds(text, context);

				int x = (int)(pageFormat.getImageableWidth()/2  - bounds.getWidth() / 2);

				graphics.drawString(text, x + (int)pageFormat.getImageableX(), (int)bounds.getHeight());

				return Printable.PAGE_EXISTS;
			}
		};

		printOnVVPAT(confirmed);
	}*/

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
        /*
        System.out.println("The races are:");
        for (String label:raceTitles.keySet())
        {
            System.out.println("Race: " + label + " corresponds to image: " + raceTitles.get(label).toString());
        }
        */

        //System.out.println("There are " + choiceToImage.keySet().size() + " entries in the mapping right after loadImagesForVVPAT is called.");
		final List<String> choices = new ArrayList<String>();

        ArrayList<ChoicePair> correctedBallot = correctBallot(ballot);

        /* This for loop uses the original ballot, which does not account for No Selections. */
        /*
        System.out.println("Choices in old ballot:");
		for(int i = 0; i < ballot.size(); i++){
			ListExpression choice = (ListExpression)ballot.get(i);
            System.out.println("Choice: " + choice.get(0).toString() + ":" + choice.get(1).toString());
			if(choice.get(1).toString().equals("1"))
				choices.add(choice.get(0).toString());
		}
		*/

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
            /* NEW CODE. */
            Image currentRaceTitleImage = raceTitlePairs.get(i).getImage();
            //System.out.println("Attempting to get mapping for key \"" + currentImageKey + "\"");
            Image img = choiceToImage.get(currentImageKey);

			totalSize += img.getHeight(null);
            /* NEW CODE. */
            //totalSize += currentRaceTitleImage.getHeight(null);
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

				int choiceIndex = 0;
				int totalSize = 0;
				while(pageIndex != 0){
					totalSize += choiceToImage.get(choices.get(choiceIndex)).getHeight(null);

					if(totalSize > pageFormat.getImageableHeight()){
						totalSize = 0;
						choiceIndex--;
						pageIndex--;
					}

					choiceIndex++;
				}

                totalSize = 25;
                int printX = (int)pageFormat.getImageableX();
                int printHeight = _constants.getPrintableHeightForVVPAT();

                int counter = 0;

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
                BufferedImage barcode = PrintImageUtils.getBarcode(fbid);

                Font ocra = new Font("OCR A Extended", Font.PLAIN, 16);
                printHeight = printHeight - ocra.getSize();
                int printWidth = _constants.getPrintableWidthForVVPAT();


                //Find the minimum amount of whitespace to be trimmed off title images
                int maxToTrim = Integer.MAX_VALUE;
                for(RaceTitlePair rtp : fActualRaceNamePairs){
                    BufferedImage title = (BufferedImage)rtp.getImage();

                    maxToTrim = Math.min(PrintImageUtils.getImageTrim(title, true), maxToTrim);


                }

                if(_constants.getUseTwoColumns())
                    printWidth /= 2;

                int initialHeight = totalSize;

				while(totalSize < _constants.getPrintableHeightForVVPAT() && choiceIndex < choices.size()){
                    //Image titleImg = (raceTitlePairs1.remove(0)).getImage();
					BufferedImage img = (BufferedImage)choiceToImage.get(choices.get(choiceIndex));
                    BufferedImage titleImg = (BufferedImage)fActualRaceNamePairs.get(counter).getImage();
                    counter++;

                    //Useful constants for image scaling and printing



                    float scaledWidthFactor =     (1.0f*printWidth/img.getWidth(null));
                    int scaledHeight = Math.round(img.getHeight(null)*scaledWidthFactor);


                    //Remove trailing whitespace to allow for better scaling
                    //Only the title image will have trailing whitespace due to rendering
                    titleImg = PrintImageUtils.trimImage(titleImg, true, maxToTrim);

//                    BufferedImage outImage = PrintImageUtils.getScaledInstance(img, printWidth, scaledHeight, RenderingHints.VALUE_INTERPOLATION_BICUBIC, true);
//                    System.out.println("Images:\n\tSize: " + outImage.getWidth() + "x" + outImage.getHeight());

//                    try{
//                        ImageIO.write(outImage, "PNG", new File("SCALED_IMAGE.png"));
//                    }
//                    catch (IOException e){
//                        new RuntimeException(e);
//                    }

                    BufferedImage outTitle = PrintImageUtils.getScaledInstance(titleImg, (printWidth*2)/3, (titleImg.getHeight()*2)/3, RenderingHints.VALUE_INTERPOLATION_BICUBIC, false);
                    System.out.println("Titles:\n\tSize: " + img.getWidth() + "x" + img.getHeight());
                    graphics.drawImage(outTitle,
                            printX,
                            totalSize,
                            null);

					graphics.drawImage(img,
                            printX,
                            totalSize + outTitle.getHeight(null),
                            null);



                    //totalSize += outTitle.getHeight(null);
					totalSize += img.getHeight(null) + outTitle.getHeight(null);
					choiceIndex++;

                    //If we reach the end of a column and are printing in two columns, go back to the top with an offset of printwidth
                    if(totalSize + img.getHeight(null) + outTitle.getHeight(null) >= _constants.getPrintableHeightForVVPAT() - barcode.getHeight(null)
                            && _constants.getUseTwoColumns()){
                        totalSize = initialHeight;
                        printX += printWidth;

                    }
				}
                graphics.setFont(ocra);
                graphics.drawString(fbid, (int)pageFormat.getImageableX(), _constants.getPrintableHeightForVVPAT()-ocra.getSize());

                graphics.drawImage(barcode, printWidth, _constants.getPrintableHeightForVVPAT()-barcode.getHeight(null), null);

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
                //System.out.println("Adding numeric ID " + UID.substring(1) + " to the list of numeric IDs.");
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
            //System.out.println("Labels in current race: ");

            for (int labelIdx = 0; labelIdx < currentRace.size(); labelIdx++)
            {
                String currentLabel = currentRace.get(labelIdx);
                //System.out.println(currentLabel);
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
        //System.out.println("Corrected ballot:");
        for (int i = 0; i < updatedBallot.size(); i++)
        {
            ChoicePair currentItem = updatedBallot.get(i);
            //System.out.println(currentItem.getLabel() + ":" + currentItem.getStatus());
        }
        return updatedBallot;
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

		try {
			job.setPrintService(vvpat);
		} catch (PrinterException e) {
			Bugout.err("VVPAT printing failed: "+e.getMessage());
			return;
		}

		Paper paper = new Paper();
		paper.setSize(_constants.getPaperWidthForVVPAT(), _constants.getPaperHeightForVVPAT());

		int imageableWidth = _constants.getPrintableWidthForVVPAT();
		int imageableHeight = _constants.getPrintableHeightForVVPAT();

		int leftInset = (_constants.getPaperWidthForVVPAT() - _constants.getPrintableWidthForVVPAT()) / 2;
		int topInset = (_constants.getPaperHeightForVVPAT() - _constants.getPrintableHeightForVVPAT()) / 2;

		paper.setImageableArea(leftInset, topInset, imageableWidth, imageableHeight);
		PageFormat pageFormat = new PageFormat();
		pageFormat.setPaper(paper);

		job.setPrintable(toPrint, pageFormat);

		try {
			job.print();
		} catch (PrinterException e) {
			Bugout.err("VVPAT printing failed: "+e.getMessage());
			return;
		}
	}


}
