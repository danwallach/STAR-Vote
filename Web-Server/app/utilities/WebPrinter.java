package utilities;

import printer.*;
import sexpression.ListExpression;
import tap.BallotImageHelper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: mrdouglass95
 * Date: 6/27/13
 * Time: 11:51 AM
 * To change this template use File | Settings | File Templates.
 */

/**
 * An adaptation of STAR-Vote's printer class adapted to work with WebHTMLPrinter.
 */
public class WebPrinter extends Printer{

    public WebPrinter(File ballotFile,List<List<String>> races){
        super(ballotFile, races, "ws.conf");
    }

    /**
     * Prints a committed ballot through use of class Web-HTMLPrinter.
     *
     * @param ballot - the choices to print, in the form ((race-id choice) ...)
     * @param bid the ballot ID
     * @return success of print
     */
    public boolean printCommittedBallot(ListExpression ballot, String bid) {
        System.out.println("Current Ballot: " + _currentBallotFile.getAbsolutePath());
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

        ////////////////////////////////////////////////////////////////////////////////////////////////////
        // This is where the HTML Printing occurs.

        char sep = File.separatorChar; // seperator for this OS
        String cleanFilePath = _currentBallotFile.getAbsolutePath().substring(0, _currentBallotFile.getAbsolutePath().lastIndexOf(".zip")) + sep;
        // Print to an HTML file. Parameters to be used:
        String htmlFileName = cleanFilePath.substring(0, cleanFilePath.indexOf("public")) + "htmls" + sep + "ChallengedBallot_" + bid + ".html";
        Boolean useTwoColumns = true;
        Boolean printerFriendly = true;
        String pathToVVPATFolder = cleanFilePath + "data"+sep+"media"+sep+"vvpat"+sep;
        String barcodeFileNameNoExtension = pathToVVPATFolder + "Barcode";
        String lineSeparatorFileName = pathToVVPATFolder + "LineSeparator.png";
        //if((new File(cleanFilePath + "data")).exists())
          //  displayDir(new File(cleanFilePath + "data"), 0);

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
            ImageIO.write(barcode, "PNG", new File(barcodeFileNameNoExtension + "_flipped.png"));
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

            while ((currentColumn.size() < 46) && (counter < choices.size()))
            {
                String titleName = fActualRaceNamePairs.get(counter).getLabel();
                String selectionName = choices.get(counter);
                currentColumn.add(titleName + "_printable_en.png");
                currentColumn.add(selectionName + "_printable_en.png");
                counter++;
            }
            System.out.println(currentColumn.size());
            columnsToPrint.add(currentColumn);
        }

        // Generate the HTML file with the properties set above.
        System.out.println("Printing to HTML");

        String path = _currentBallotFile.getAbsolutePath();
        String webPathToVVPATFolder = sep + "assets" + path.substring(path.indexOf(sep + "ballots"), path.indexOf(".zip")) + sep + "data" + sep + "media" + sep + "vvpat" + sep;;
        String webBarcodeFileNameNoExtension = webPathToVVPATFolder + "Barcode";
        String webLineSeparatorFileName = webPathToVVPATFolder + "LineSeparator.png";

        WebHTMLPrinter.generateHTMLFile(htmlFileName, useTwoColumns, printerFriendly, webPathToVVPATFolder, _constants, fbid, webBarcodeFileNameNoExtension, webLineSeparatorFileName, columnsToPrint);
        return true;
    }
}
