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
     * @param ballot        the choices to print, in the form ((race-id choice) ...)
     * @param bid           the ballot ID
     * @return              success of print
     */
    public boolean printCommittedBallot(ListExpression ballot, final String bid) {

        final Map<String, Image> choiceToImage = BallotImageHelper.loadImagesForVVPAT(_currentBallotFile);
        final ArrayList<RaceTitlePair> actualRaceNameImagePairs = getRaceNameImagePairs(choiceToImage);
        final List<String> choices = new ArrayList<String>();

        ArrayList<ChoicePair> correctedBallot = reformBallot(ballot);

        /* This for loop uses the corrected ballot, which accounts for No Selections. */
        for (ChoicePair currentItem : correctedBallot)
            if (currentItem.getStatus() == 1)
                choices.add(currentItem.getLabel());

        /* This is where the HTML Printing occurs. */
        char sep = File.separatorChar; /* separator for this OS */
        String cleanFilePath = _currentBallotFile.getAbsolutePath().substring(0, _currentBallotFile.getAbsolutePath().lastIndexOf(".zip")) + sep;

        /* Print to an HTML file. Parameters to be used: */
        String htmlFileName = cleanFilePath.substring(0, cleanFilePath.indexOf("public")) + "htmls" + sep + "ChallengedBallot_" + bid + ".html";

        /* TODO should read in useTwoColumns and printerFriendly from somewhere */
        Boolean useTwoColumns = true;
        Boolean printerFriendly = true;

        String pathToVVPATFolder = cleanFilePath + "data" + sep + "media" + sep + "vvpat" + sep;
        String barcodeFileNameNoExtension = pathToVVPATFolder + "Barcode";
        String lineSeparatorFileName = pathToVVPATFolder + "LineSeparator.png";

        /* Generate a barcode of the bid */
        /* Do it here so we can use height of the barcode for laying out other components on the printout */
        BufferedImage barcode = PrintImageUtils.getBarcode(bid);

        /* Draw the barcode and other separators onto the ballot */
        try {
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

        /* Generate the HTML file with the properties set above. */
        String path = _currentBallotFile.getAbsolutePath();
        String webPathToVVPATFolder = sep + "assets" + path.substring(path.indexOf(sep + "ballots"), path.indexOf(".zip")) + sep + "data" + sep + "media" + sep + "vvpat" + sep;
        String webBarcodeFileNameNoExtension = webPathToVVPATFolder + "Barcode";
        String webLineSeparatorFileName = webPathToVVPATFolder + "LineSeparator.png";

        WebHTMLPrinter.generateHTMLFile(htmlFileName, useTwoColumns, printerFriendly, webPathToVVPATFolder, _constants, bid, webBarcodeFileNameNoExtension, webLineSeparatorFileName, columnsToPrint);
        return true;
    }
}
