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

package votebox.middle.driver;

import auditorium.IAuditoriumParams;
import crypto.PlaintextRaceSelection;
import sexpression.ListExpression;
import tap.BallotImageHelper;
import votebox.middle.IBallotVars;
import votebox.middle.ballot.*;
import votebox.middle.view.ViewManager;

import javax.print.PrintService;
import javax.print.attribute.standard.PrinterName;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.io.*;
import java.util.*;
import java.util.List;

/* TODO: Documentation by knowledgeable member */

public class Driver {

	private String _path;
	private ViewManager _view;
	private RuntimeBallot _ballot;

	private IBallotLookupAdapter _ballotLookupAdapter = new IBallotLookupAdapter() {

		public List<PlaintextRaceSelection> inRaceSelectionForm() {

			return _ballot.inRaceSelectionForm();
		}

		public int numSelections() {
			return _ballot.getNumSelections();
		}

        public List<List<String>> getRaceGroups() {
			return _ballot.getRaceGroups();
		}

        public List<String> getTitles() {
            return _ballot.getRaceTitles();
        }

	};

    /**
     * Constructor for the driver
     */
	public Driver() {}

	/**
	 * Loads the path of the ballot
	 * @param path path of ballot configuration and xml file
     */
	public void loadPath(String path){
		_path = path;
	}

	public void launchView(Observer reviewScreenObserver, Observer castBallotObserver) {
		/* Set up the view */
		_view = new ViewManager(_ballotLookupAdapter);

        /* Register for cast ballot in the view */
		if(castBallotObserver != null)
			_view.registerForCastBallot(castBallotObserver);

        /* Register for review screen in the view */
		if(reviewScreenObserver != null)
			_view.registerForReview(reviewScreenObserver);

		/* Execute */
		_view.run();
	}

	public void launchView() {
		launchView(null, null);
	}

    /**
     * Sets up to parse the ballot format and begin voting process
     */
	public void run() {

		IBallotVars vars;

        /* Load the ballot configuration file */
		try { vars = new GlobalVarsReader(_path).parse(); }
        catch (IOException e) {

			System.err.println("The ballot's configuration file could not be found.");
			e.printStackTrace();
			return;

		}

        /* Try to parse through the XML file */
		try { _ballot = new BallotParser().getBallot(vars); }
        catch (BallotParserException e) {

			System.err.println("The ballot's XML file was unable to be parsed.");
			e.printStackTrace();
			return;
		}
	}


    /**
     * Terminates the view.
     */
	public void kill() { _view.kill(); }
    
    /**
     * Gets this VoteBox instance's view.  Used to allow the caller to register for
     * the cast ballot event in the view manager.
     * @return the view manager
     */
    public ViewManager getView() { return _view; }
    
    /**
     * @return a reference to the current BallotLookupAdapter
     */
    public IBallotLookupAdapter getBallotAdapter(){
    	return _ballotLookupAdapter;
    }

    /**
     * @return the ballot this driver is working with
     */
    public RuntimeBallot getBallot(){
        return _ballot;
    }

    /**
     * Prints a ballot out on a VVPAT.
     *
     * @param constants - parameters to use for printing
     * @param ballot - ballot in the form ((race-id (race-id (... ))))
     * @param currentBallotFile - ballot file to extract images from
     */
    public static void printCommittedBallot(IAuditoriumParams constants, ListExpression ballot, File currentBallotFile) {

        /* Load the images as a Map(String:Image) for the ballot */
		final Map<String, Image> choiceToImage = BallotImageHelper.loadImagesForVVPAT(currentBallotFile);

        /* Check to make sure there were images loaded */
		if(choiceToImage == null){
			System.out.println("\tPrinting aborted, no VVPAT images");
			return;
		}

		final List<String> choices = new ArrayList<>();

        /* Get each of the ballots */
		for(int i = 0; i < ballot.size(); i++){

			ListExpression choice = (ListExpression)ballot.get(i);

            /* TODO check this but short-circuit should make this work */
			if(choice.size() != 2 || !(choice.get(1) instanceof ListExpression) || choice.get(1).size() < 1)
				choices.add(choice.get(0).toString());

			else {

                String check = ((ListExpression) choice.get(1)).get(0).toString();

                if (check.trim().length() > 0)
                    choices.add(check);
            }
		}

		int totalSize = 0;

        /* Get the total size of the ballot */
        for (String choice : choices)
            totalSize += choiceToImage.get(choice).getHeight(null);

		final int fTotalSize = totalSize;
		final List<String> printedChoices = new ArrayList<>();

        /* Set up the ballot for printing TODO comment this anonymous class */
		Printable printedBallot = (graphics, pageFormat, pageIndex) -> {

            int numPages = fTotalSize / (int)pageFormat.getImageableHeight();

            if(fTotalSize % (int)pageFormat.getImageableHeight() != 0)
                numPages++;

            if(printedChoices.size() == choices.size())
                return Printable.NO_SUCH_PAGE;

            int choiceIndex = 0;
            int totalSize1 = 0;

            while(pageIndex != 0){
                totalSize1 += choiceToImage.get(choices.get(choiceIndex)).getHeight(null);

                if(totalSize1 > pageFormat.getImageableHeight()){
                    totalSize1 = 0;
                    choiceIndex--;
                    pageIndex--;
                }

                choiceIndex++;
            }

            totalSize1 = 0;
            while(totalSize1 < pageFormat.getImageableHeight() && choiceIndex < choices.size()){

                BufferedImage img = (BufferedImage)choiceToImage.get(choices.get(choiceIndex));

                if(img.getHeight(null) + totalSize1 > pageFormat.getImageableHeight())
                    break;

                printedChoices.add(choices.get(choiceIndex));

                System.out.println("\t\t>>"+img);

                int x = (int)pageFormat.getImageableX();
                int y = (int)pageFormat.getImageableY() + totalSize1;

                graphics.drawImage(img, x, y, null);

                totalSize1 += img.getHeight(null);
                choiceIndex++;
            }

            return Printable.PAGE_EXISTS;
        };

		Driver.printOnVVPAT(constants, printedBallot);
	}

    /**
	 * Prints onto the attached VVPAT printer, if possible.
     *
     * @param constants - the constants the determine the printer, size, etc.
	 * @param toPrint - the Printable to print.
	 */
	public static void printOnVVPAT(final IAuditoriumParams constants, final Printable toPrint){

		/* Marshal printing to a new thread to keep from blocking on an Observer */
		Thread t = new Thread(){

			public void run(){

				/* VVPAT not ready */
				if(constants.getPrinterForVVPAT().equals("")) return;

                /* Get a list of printers */
				PrintService[] printers = PrinterJob.lookupPrintServices();

				PrintService vvpat = null;

                /* Cycle through the list of printers */
				for(PrintService printer : printers){

					PrinterName name = printer.getAttribute(PrinterName.class);

                    /* Find the printer to print to */
					if(name.getValue().equals(constants.getPrinterForVVPAT())){
						vvpat = printer;
						break;
					}
				}

                /* Could not find printer */
				if(vvpat == null) return;

				PrinterJob job = PrinterJob.getPrinterJob();

				try { job.setPrintService(vvpat); }
                catch (PrinterException e) { return; }

				Paper paper = new Paper();

                /* Set paper formatting */
				paper.setSize(constants.getPaperWidthForVVPAT(), constants.getPaperHeightForVVPAT());

				int imageableWidth  = constants.getPrintableWidthForVVPAT();
				int imageableHeight = constants.getPrintableHeightForVVPAT();

				int leftInset = (constants.getPaperWidthForVVPAT() - constants.getPrintableWidthForVVPAT()) / 2;
				int topInset  = (constants.getPaperHeightForVVPAT() - constants.getPrintableHeightForVVPAT()) / 2;

				paper.setImageableArea(leftInset, topInset, imageableWidth, imageableHeight);

				PageFormat pageFormat = new PageFormat();
				pageFormat.setPaper(paper);

				job.setPrintable(toPrint, pageFormat);

                /* Try to print */
				try { job.print(); }
                catch (PrinterException e) { e.printStackTrace(); }
			}

		};

		t.start();
	}

}

