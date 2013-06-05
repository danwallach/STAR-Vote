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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.AffineTransformOp;
import java.awt.AlphaComposite;
import java.awt.image.renderable.RenderableImage;
import java.awt.print.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;

import javax.imageio.ImageIO;
import javax.print.attribute.standard.PrinterName;
import javax.swing.Timer;

import edu.uconn.cse.adder.PublicKey;

import sexpression.*;
import tap.BallotImageHelper;
import votebox.AuditoriumParams;
import votebox.crypto.*;
import votebox.events.*;
import votebox.middle.*;
import votebox.middle.ballot.Ballot;
import votebox.middle.driver.*;
import votebox.middle.view.*;
import auditorium.*;
import auditorium.Event;


import javax.print.PrintService;

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
	public void printCommittedBallot(ListExpression ballot) {
		final Map<String, Image> choiceToImage = BallotImageHelper.loadImagesForVVPAT(_currentBallotFile);
        final Map<String, Image> raceTitles = BallotImageHelper.loadBallotTitles(_currentBallotFile);

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

        final ArrayList<RaceTitlePair> raceTitlePairs1 = raceTitlePairs;
        final int titlePairsSize = raceTitlePairs.size();

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

				while(totalSize < pageFormat.getImageableHeight() && choiceIndex < choices.size()){
                    //Image titleImg = (raceTitlePairs1.remove(0)).getImage();
					BufferedImage img = (BufferedImage)choiceToImage.get(choices.get(choiceIndex));


                    //Useful constants for image scaling and printing
                    int printWidth = _constants.getPrintableWidthForVVPAT();
                    if(_constants.getUseTwoColumns())
                        printWidth /= 2;

                    float scaledWidthFactor =     (1.0f*printWidth/img.getWidth(null));
                    int scaledHeight = Math.round(img.getHeight(null)*scaledWidthFactor);

                    //System.out.println("Now drawing " + choices.get(choiceIndex));

                    //Random scaling factor of 1/2
                    //Image outTitle = titleImg.getScaledInstance(_constants.getPrintableWidthForVVPAT(), _constants.getPrintableHeightForVVPAT()/(2*(choices.size()+titlePairsSize)), Image.SCALE_AREA_AVERAGING);
                    //Image outImage = img.getScaledInstance(_constants.getPrintableWidthForVVPAT(), _constants.getPrintableHeightForVVPAT()/(2*(choices.size()+titlePairsSize)), Image.SCALE_AREA_AVERAGING);




                    BufferedImage outImage = getScaledInstance(img, printWidth, scaledHeight, RenderingHints.VALUE_INTERPOLATION_BICUBIC, true);



                    //Want to scale the height with respect to the width only


                    //System.out.println(height);


                    //TODO This really doesn't work, need to fix...
                    // Useless comment that enables GIT pushing.
                    //BufferedImage outImage = new BufferedImage(printWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
//                    Graphics2D g = outImage.createGraphics();
//                    g.drawImage(img, 0, 0, printWidth, scaledHeight, null);
//                    g.dispose();
//                    g.setComposite(AlphaComposite.Src);
//                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
//                    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


//                    AffineTransform at = new AffineTransform();
//                    at.scale(scaledWidthFactor,scaledWidthFactor);
//                    AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
//                    outImage = scaleOp.filter(outImage, null);

                    //System.out.println("Image " + img + " | Height " + outImage.getHeight() + " | Width " + outImage.getWidth());

                    try{
                        ImageIO.write(outImage, "PNG", new File("BALLOT_IMAGE.png"));
                        //ImageIO.write(outImage, "PNG", new File("SCALED_BALLOT_IMAGE.png"));
                    }catch (IOException e){
                        new RuntimeException(e);
                    }

					graphics.drawImage(outImage,
                            printX,
                            totalSize,
                            null);

                    //totalSize += outTitle.getHeight(null);
					totalSize += outImage.getHeight(null);
					choiceIndex++;

                    //If we reach the end of a column and are printing in two columns, go back to the top with an offset of printwidth
                    if(totalSize > pageFormat.getImageableHeight() - outImage.getHeight(null) && _constants.getUseTwoColumns()){
                        totalSize = 0;
                        printX += printWidth;

                    }
				}

				return Printable.PAGE_EXISTS;
			}

		};

		printOnVVPAT(printedBallot);
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

    /**
     * Convenience method that returns a scaled instance of the
     * provided {@code BufferedImage}.
     *
     * @param img the original image to be scaled
     * @param targetWidth the desired width of the scaled instance,
     *    in pixels
     * @param targetHeight the desired height of the scaled instance,
     *    in pixels
     * @param hint one of the rendering hints that corresponds to
     *    {@code RenderingHints.KEY_INTERPOLATION} (e.g.
     *    {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
     * @param higherQuality if true, this method will use a multi-step
     *    scaling technique that provides higher quality than the usual
     *    one-step technique (only useful in downscaling cases, where
     *    {@code targetWidth} or {@code targetHeight} is
     *    smaller than the original dimensions, and generally only when
     *    the {@code BILINEAR} hint is specified)
     * @return a scaled version of the original {@code BufferedImage}
     *
     * Taken from  https://today.java.net/pub/a/today/2007/04/03/perils-of-image-getscaledinstance.html
     */
    public BufferedImage getScaledInstance(BufferedImage img,
                                           int targetWidth,
                                           int targetHeight,
                                           Object hint,
                                           boolean higherQuality)
    {
        int type = (img.getTransparency() == Transparency.OPAQUE) ?
                BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = (BufferedImage)img;
        int w, h;
        if (higherQuality) {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = img.getWidth();
            h = img.getHeight();
        } else {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = targetWidth;
            h = targetHeight;
        }

        do {
            if (higherQuality && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }

            if (higherQuality && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ret = tmp;
        } while (w != targetWidth || h != targetHeight);

        return ret;
    }
}
