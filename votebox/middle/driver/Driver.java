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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.print.PrintService;
import javax.print.attribute.standard.PrinterName;

import auditorium.IAuditoriumParams;

import sexpression.ASExpression;
import sexpression.ListExpression;

import tap.BallotImageHelper;

import votebox.middle.IBallotVars;
import votebox.middle.Properties;
import votebox.middle.ballot.Ballot;
import votebox.middle.ballot.BallotParser;
import votebox.middle.ballot.BallotParserException;
import votebox.middle.ballot.CardException;
import votebox.middle.ballot.IBallotLookupAdapter;
import votebox.middle.ballot.NonCardException;
import votebox.middle.view.IView;
import votebox.middle.view.IViewFactory;
import votebox.middle.view.ViewManager;

/* TODO: Documentation by knowledgeable member */

public class Driver {

	private final String _path;
	private final IViewFactory _factory;

	private ViewManager _view;
	private Ballot _ballot;
	private boolean _encryptionEnabled;

	private IViewAdapter _viewAdapter = new IViewAdapter() {

        @Override
        public void setView(IView view) {
            _view.setView(view);
        }

        public boolean select(String uid) throws UnknownUIDException, SelectionException {
            return _view.select(uid);
        }

        public boolean deselect(String uid, boolean playSound) throws UnknownUIDException,
				DeselectionException {
			return _view.deselect(uid, playSound);
		}

		public Properties getProperties() {
			return _view.getCurrentLayout().getProperties();
		}

        public IView getView() {
            return _view.getView();
        }

	};

	private IAdapter _ballotAdapter = new IAdapter() {

		public boolean deselect(String uid, boolean playSound) throws UnknownUIDException,
				DeselectionException {
			return _ballot.deselect(uid);
		}

		public Properties getProperties() {
			return _ballot.getProperties();
		}

        public boolean select(String uid) throws UnknownUIDException,
				SelectionException {
			return _ballot.select(uid);
		}

	};

	private IBallotLookupAdapter _ballotLookupAdapter = new IBallotLookupAdapter() {

		public boolean isCard(String UID) throws UnknownUIDException {
			return _ballot.isCard(UID);
		}

        public String selectedElement(String UID) throws NonCardException,
				UnknownUIDException, CardException {
			return _ballot.selectedElement(UID);
		}

        public boolean exists(String UID) {
			return _ballot.exists(UID);
		}

        public boolean isSelected(String uid) throws UnknownUIDException {
			return _ballot.isSelected(uid);
		}

		public ASExpression getCastBallot() {
			if(!_encryptionEnabled)
				return _ballot.toASExpression();
			
			return _ballot.getCastBallot();
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

		public Map<String, List<ASExpression>> getAffectedRaces(List<String> affectedUIDs) {
			/* TODO: Implement remainder of piecemeal */
			throw new RuntimeException("Not implemented");
		}

		public List<String> getRaceGroupContaining(List<ASExpression> uids) {
			/* TODO: Implement remainder of piecemeal */
			throw new RuntimeException("Not implemented");
		}
	};

    /**
     * Constructor for the driver
     * @param path file path for ballot files
     * @param factory factory for view construction
     * @param encryptionEnabled whether or not encryption is enabled
     */
	public Driver(String path, IViewFactory factory, boolean encryptionEnabled) {
		_path = path;
		_factory = factory;
		_encryptionEnabled = encryptionEnabled;
	}

    /**
     * Sets up to parse the ballot format and begin voting process
     * @param reviewScreenObserver observer for the review screen
     * @param castBallotObserver observer for the castBallotEvent
     */
	public void run(Observer reviewScreenObserver, Observer castBallotObserver) {

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

        /* Set up the view */
		_ballot.setViewAdapter(_viewAdapter);
		_view = new ViewManager(_ballotAdapter, _ballotLookupAdapter, vars, _factory);

        /* Register for cast ballot in the view */
		if(castBallotObserver != null)
			_view.registerForCastBallot(castBallotObserver);

        /* Register for review screen in the view */
		if(reviewScreenObserver != null)
			_view.registerForReview(reviewScreenObserver);

		/* Execute */
		_view.run();
	}

    /**
     * Default implementation of run with no observer variables. This will not register
     * for cast ballot or review screen.
     */
	public void run() { run(null, null); }

    /**
     * Terminates the view.
     */
	public void kill() { _view.dispose(); }
    
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
    public Ballot getBallot(){
        return _ballot;
    }

    /**
     * Prints a statement that the ballot has been accepted by the voter on a VVPAT.
     * 
     * @param constants - parameters to use for printing
     * @param currentBallotFile - ballot file to extract images from
     */
    public static void printBallotAccepted(IAuditoriumParams constants, File currentBallotFile){

        /* Load the images as a Map(String:Image) for the ballot */
    	Map<String, Image> choices = BallotImageHelper.loadImagesForVVPAT(currentBallotFile);
    	
    	final Image accept = choices.get("accept");

    	Printable toPrint = new Printable(){

			public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {

                if(pageIndex != 0)
					return Printable.NO_SUCH_PAGE;

                /* Draw the accept image */
				graphics.drawImage(accept, (int)pageFormat.getImageableX(), (int)pageFormat.getImageableY(), null);
				return Printable.PAGE_EXISTS;
			}
        };

        /* Print acceptance */
    	printOnVVPAT(constants, toPrint);
    }
    
    /**
     * Prints a statement that the ballot has been rejected by the voter on a VVPAT.
     * 
     * @param constants - parameters to use for printing
     * @param currentBallotFile - ballot file to extract images from
     */
    public static void printBallotRejected(IAuditoriumParams constants, File currentBallotFile){

        /* Load the images as a Map(String:Image) for the ballot */
        Map<String, Image> choices = BallotImageHelper.loadImagesForVVPAT(currentBallotFile);
    	
    	final Image spoil = choices.get("spoil");

    	Printable toPrint = new Printable(){

			public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {

				if(pageIndex != 0)
					return Printable.NO_SUCH_PAGE;

                /* Draw the spoil image */
				graphics.drawImage(spoil, (int)pageFormat.getImageableX(), (int)pageFormat.getImageableY(), null);
				return Printable.PAGE_EXISTS;
			}
    	};

        /* Print rejection */
    	printOnVVPAT(constants, toPrint);
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
		Printable printedBallot = new Printable(){

			public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {

				int numPages = fTotalSize / (int)pageFormat.getImageableHeight();

				if(fTotalSize % (int)pageFormat.getImageableHeight() != 0)
					numPages++;
				
				if(printedChoices.size() == choices.size())
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
				
				totalSize = 0;
				while(totalSize < pageFormat.getImageableHeight() && choiceIndex < choices.size()){

					BufferedImage img = (BufferedImage)choiceToImage.get(choices.get(choiceIndex));

					if(img.getHeight(null) + totalSize > pageFormat.getImageableHeight())
						break;
					
					printedChoices.add(choices.get(choiceIndex));
					
					System.out.println("\t\t>>"+img);
					
					int x = (int)pageFormat.getImageableX();
					int y = (int)pageFormat.getImageableY() + totalSize;
					
					graphics.drawImage(img, x, y, null);

					totalSize += img.getHeight(null);
					choiceIndex++;
				}
				
				return Printable.PAGE_EXISTS;
			}
			
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

    /**
     * Unzips the source file to the destination directory
     * @param src the source zip file
     * @param dest the destination directory into which the unzipped files will be placed
     * @throws IOException
     */
    public static void unzip(String src, String dest) throws IOException {

        if(!(new File(dest)).exists()) (new File(dest)).mkdirs();

        ZipFile zipFile = new ZipFile(src);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        byte[] buf = new byte[1024];
        int len;

        /* Make all the directories first */
        while(entries.hasMoreElements()){

            ZipEntry entry = entries.nextElement();

            if (entry.isDirectory()) {

                /* Create the directory using the proper separator for this platform */
                File newDir = new File(dest, entry.getName().replace('/', File.separatorChar));
                newDir.mkdirs();
            }
        }

        entries = zipFile.entries();

        /* Now copy all the data files */
        while (entries.hasMoreElements()) {

            ZipEntry entry = entries.nextElement();

            if (!entry.isDirectory())
            {
                InputStream in = zipFile.getInputStream(entry);

                /* Create the file path, using the proper seperator char */
                File outFile = new File(dest, entry.getName().replace('/', File.separatorChar));

                OutputStream out = new BufferedOutputStream(new FileOutputStream(outFile));

                while((len = in.read(buf)) >= 0) out.write(buf, 0, len);

                in.close();

                out.flush();
                out.close();
            }
        }

        zipFile.close();
    }

    /**
     * Deletes all files in a directory recursively
     * @param dir the directory to be cleared
     */
	public static void deleteRecursivelyOnExit(String dir) {

		Stack<File> dirStack = new Stack<>();
        dirStack.add( new File(dir) );

        /* While there's still something on the directory stack... */
        while (!dirStack.isEmpty()) {

            /* Pop a file off the stack, mark for deletion, and get children */
            File file = dirStack.pop();
            file.deleteOnExit();
            File[] children = file.listFiles();

            if (children == null)
                    children = new File[0];

            /* If a directory, add to the stack -- if not, mark for deletion */
            for (File f : children) {
                if (f.isDirectory()) dirStack.add( f );
                else f.deleteOnExit();
            }

            /* If the directory is way too nested, just get out of there */
            if (dirStack.size() > 100)
                return;
        }
	}
}
