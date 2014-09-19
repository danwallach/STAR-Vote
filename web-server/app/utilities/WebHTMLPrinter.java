package utilities;

import votebox.AuditoriumParams;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: mrdouglass95
 * Date: 7/3/13
 * Time: 11:20 AM
 * To change this template use File | Settings | File Templates.
 */

/**
 * An adaptation of STAR-Vote's html printer, modified to generate html files to be served on a browser
 */
public class WebHTMLPrinter {

    /* All sizes are in pixels. */
    public final static double scalar = 0.73;

    public final static int CONTAINER_WIDTH         = (int)(812*scalar);
    public final static int CONTAINER_HEIGHT        = (int)(940*scalar);
    public final static int LEFT_MARGIN_WIDTH       = (int)(72*scalar);
    public final static int RIGHT_MARGIN_WIDTH      = (int)(72*scalar);
    public final static int BARCODE_DIVIDER_HEIGHT  = (int)(80*scalar);

    public final static int TWO_COLUMNS_COLUMN_SIZE = (CONTAINER_WIDTH - LEFT_MARGIN_WIDTH - RIGHT_MARGIN_WIDTH) / 2;
    public final static int ONE_COLUMN_COLUMN_SIZE  = CONTAINER_WIDTH - LEFT_MARGIN_WIDTH - RIGHT_MARGIN_WIDTH;

    /* The ballot parameters. */
    public static AuditoriumParams BALLOT_CONSTANTS = null;

    /* The ballot ID. */
    public static String BALLOT_ID = "000000000";

    /* The path to the barcode image. */
    public static String BARCODE_IMAGE = "Barcode.png";

    /* The path to the line separator image. */
    public static String LINE_SEPARATOR_IMAGE = "LineSeparator.png";

    /**
     * Generates and saves a render of a ballot to html file
     *
     * @param filename                  file name of file to be created
     * @param pathToBallotVVPATFolder   file path to VVPAT folder for media
     * @param ballotID                  the ballot ID
     * @param barcodeFilePath           path to the barcode image
     * @param lineSeparatorFilePath     correct file separator for this OS
     * @param imageNames                names of ballot images
     */
    public static void generateHTMLFile (String filename, Boolean useTwoColumns, Boolean printFriendly, String pathToBallotVVPATFolder,
                                         AuditoriumParams ballotConstants, String ballotID, String barcodeFilePath,
                                         String lineSeparatorFilePath, List<ArrayList<String>> imageNames) {

        File file = new File(filename);

        /* If the file does not exist, then create it. */
        if (!file.exists())
            try { file.createNewFile(); }
            catch (IOException e) { System.err.println("HTML File Generator Error: Unable to create file '" + filename + "'"); }


        /* Create the writer. */
        BufferedWriter writer = null;

        /* Try to instantiate a new BufferedWriter based on the file */
        try { writer = new BufferedWriter(new FileWriter(file.getAbsoluteFile())); }
        catch (IOException e) { System.err.println("HTML File Generator Error: Unable to create BufferedWriter for file '" + filename + "'"); }

        /* Set the ballot constants object. */
        BALLOT_CONSTANTS = ballotConstants;

        /* Set the ballot ID. */
        BALLOT_ID = ballotID;

        /* Set the barcode. */
        BARCODE_IMAGE = barcodeFilePath;

        /* Set the line separator. */
        LINE_SEPARATOR_IMAGE = lineSeparatorFilePath;

        /* Actually write the HTML file. */
        if (writer != null) {

            try {
                /* Open div. */
                writer.write("<div id=\"ballotrender\" style=\"background-color:#FFFFFF;border:1px solid black;\">\n");

                /* Ballot printing uses one-column format so print that way if necessary */
                if (!useTwoColumns) generatorHelperForOneColumn(writer, printFriendly, pathToBallotVVPATFolder, imageNames);
                else generatorHelperForTwoColumns(writer, printFriendly, pathToBallotVVPATFolder, imageNames);

                /* Set the end of div. */
                writer.write("</div>\n");

                /* Housekeeping */
                writer.flush();
                writer.close();
            }
            catch (IOException e) { System.err.println("HTML File Generator Error: Unable to write to file '" + filename + "'"); }
        }
    }

     /**
     * Creates a "page" that contains two columns of images.
      *
     * @param writer                    the writer to the HTML file
     * @param printFriendly             whether or not to generate a printer-friendly HTML file. If true,
      *                                 then the HTML file will contain no colours and only black text on a white background.
     * @param pathToBallotVVPATFolder   the path to the vvpat folder in the ballot files
     * @param imageNames                ArrayLists of file names for images. One ArrayList per column.
     */
    private static void generatorHelperForTwoColumns (BufferedWriter writer, Boolean printFriendly, String pathToBallotVVPATFolder, List<ArrayList<String>> imageNames)
    {
        try {
            /* TODO fix this mess with a file probably */
            /* Writes the header of the page(s) to be printed. */
            writer.write("<p style = \"font-family:arial;color:black;font-size:20px;\">&nbsp;&nbsp;&nbsp;&nbsp;" + BALLOT_CONSTANTS.getElectionName() + "<br>\n");

            DateFormat dateFormat = new SimpleDateFormat("MMMM d, y hh:mm:ss");
            Date date = new Date();

            String currentDate = dateFormat.format(date);
            writer.write("&nbsp;&nbsp;&nbsp;&nbsp;" + currentDate + "</p>\n");

            if (printFriendly) {

                /* Creates the container for the columns of images. */
                writer.write("<div id = \"container\" style = \"background-color:#FFFFFF;width:" + CONTAINER_WIDTH + "px;height:" + CONTAINER_HEIGHT + "px;\">\n");

                /* Creates the left and right margins. */
                writer.write("<div id = \"left_margin\" style = \"background-color:#FFFFFF;width:" + LEFT_MARGIN_WIDTH + "px;height:" + CONTAINER_HEIGHT + "px;float:left;\"><br></div>\n");
                writer.write("<div id = \"right_margin\" style = \"background-color:#FFFFFF;width:" + RIGHT_MARGIN_WIDTH + "px;height:" + CONTAINER_HEIGHT + "px;float:right;\"><br></div>\n");

                /* Creates the divider for the content. */
                writer.write("<div id = \"content\" style = \"background-color:#FFFFFF;width:" + ONE_COLUMN_COLUMN_SIZE + "px;height:" + CONTAINER_HEIGHT + "px;float:left;\">\n");

                /* Add the barcode to the container (top). */
                writer.write("<div id = \"barcode_top\" style = \"background-color:#FFFFFF;width:" + ONE_COLUMN_COLUMN_SIZE + "px;height:" + BARCODE_DIVIDER_HEIGHT + "px;float:left;text-align:center;\">\n");
                writer.write("<center><img src = \"" + BARCODE_IMAGE + "_flipped.png\" alt = \"Image did not load properly\" width = \"" + TWO_COLUMNS_COLUMN_SIZE + "\"></center>\n");
                writer.write("</div>\n");
            }
            else {

                /* Creates the container for the columns of images. */
                writer.write("<div id = \"container\" style = \"background-color:#CCFF00;width:" + CONTAINER_WIDTH + "px;height:" + CONTAINER_HEIGHT + "px;\">\n");

                /* Creates the left and right margins. */
                writer.write("<div id = \"left_margin\" style = \"background-color:#000000;width:" + LEFT_MARGIN_WIDTH + "px;height:" + CONTAINER_HEIGHT + "px;float:left;\"><br></div>\n");
                writer.write("<div id = \"right_margin\" style = \"background-color:#000000;width:" + RIGHT_MARGIN_WIDTH + "px;height:" + CONTAINER_HEIGHT + "px;float:right;\"><br></div>\n");

                /* Creates the divider for the content. */
                writer.write("<div id = \"content\" style = \"background-color:#CCFFFF;width:" + ONE_COLUMN_COLUMN_SIZE + "px;height:" + CONTAINER_HEIGHT + "px;float:left;\">\n");

                /* Add the barcode to the container (top). */
                writer.write("<div id = \"barcode_top\" style = \"background-color:#FF0000;width:" + ONE_COLUMN_COLUMN_SIZE + "px;height:" + BARCODE_DIVIDER_HEIGHT + "px;float:left;text-align:center;\">\n");
                writer.write("<center><img src = \"" + BARCODE_IMAGE + "_flipped.png\" alt = \"Image did not load properly\" width = \"" + TWO_COLUMNS_COLUMN_SIZE + "\"></center>\n");
                writer.write("</div>\n");
            }


            /* ------------ Left Column ------------ */

            /* Create the left column. */
            if (printFriendly) writer.write("<div id = \"left_column\" style=\"background-color:#FFFFFF;width:" + TWO_COLUMNS_COLUMN_SIZE + "px;height:" + (CONTAINER_HEIGHT - 2 * BARCODE_DIVIDER_HEIGHT) + "px;float:left;\">\n");
            else writer.write("<div id = \"left_column\" style=\"background-color:#ABCDEF;width:" + TWO_COLUMNS_COLUMN_SIZE + "px;height:" + (CONTAINER_HEIGHT - 2 * BARCODE_DIVIDER_HEIGHT) + "px;float:left;\">\n");


            /* Put images in the left column. */
            ArrayList<String> left_column = imageNames.get(0);

            Boolean isSelectionImage = false; /* Used to leave an empty line after every selectionImage */
            int i = 0; /* Counts iterations allowing one to add margins to every first and second image accordingly */
            for (String imageName : left_column)
            {
                // Load in the image.
                writer.write("<img src = \"" + pathToBallotVVPATFolder + imageName + "\" alt = \"Image did not load properly\" width = \"" + TWO_COLUMNS_COLUMN_SIZE + "\"");
                if(i%3==1){
                    writer.write(" style=\"margin:0 0 2px 0;display:block;\">\n");
                }
                else if(i%3==0){
                    writer.write(" style=\"margin:0 0 0 0; display:block;\">\n");
                }
                // Leave an empty line after selection images.
                if (isSelectionImage)
                {
                    // Add selection separator.
                    // New separator:
                    writer.write("<img src = \"" + /*pathToBallotVVPATFolder*/ LINE_SEPARATOR_IMAGE + "\" alt = \"Image not found\" width = \"" + (TWO_COLUMNS_COLUMN_SIZE - 24) + "\" height = \"1\" align = \"right\" style=\"margin:0 0 8px 0;display:block;\">\n");
                    // Old separators:
                    // writer.write("<br>\n<br>\n");
                    // writer.write("<div id = \"bar\" style = \"background-color:#000000;width:" + TWO_COLUMNS_COLUMN_SIZE + "px;\"><hr></div>\n");
                    i++;
                }
                // Set the flag for selection images and reset it for label/title images.
                isSelectionImage = !isSelectionImage;
                i++;
            }

            // End of the left column.
            writer.write("</div>\n");
            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            // Right Column /////////////////////////////////////////////////////////////////////////////////////////////////////
            if (printFriendly)
            {
                // Create the right column.
                writer.write("<div id = \"right_column\" style=\"background-color:#FFFFFF;width:" + TWO_COLUMNS_COLUMN_SIZE + "px;height:" + (CONTAINER_HEIGHT - 2 * BARCODE_DIVIDER_HEIGHT) + "px;float:left;\">\n");
            }
            else
            {
                // Create the right column.
                writer.write("<div id = \"right_column\" style=\"background-color:#FEDCBA;width:" + TWO_COLUMNS_COLUMN_SIZE + "px;height:" + (CONTAINER_HEIGHT - 2 * BARCODE_DIVIDER_HEIGHT) + "px;float:right;\">\n");
            }

            // Put images in the right column.
            // It might be the case that there is an odd number of columns. Check if imageNames[1] throws an IndexOutOfBounds Exception.
            try
            {
                ArrayList<String> right_column = imageNames.get(1);
                isSelectionImage = false; // Used to leave an empty line after every selectionImage
                i = 0;  //counts iterations allowing one to add margins to every first and second image accordingly
                for (String imageName : right_column)
                {
                    // Load in the image.
                    writer.write("<img src = \"" + pathToBallotVVPATFolder + imageName + "\" alt = \"Image did not load properly\" width = \"" + TWO_COLUMNS_COLUMN_SIZE + "\"");

                    if(i%3==1){
                        writer.write(" style=\"margin:0 0 2px 0;display:block;\">\n");
                    }
                    else if(i%3==0){
                        writer.write(" style=\"margin:0 0 0 0;display:block;\">\n");
                    }

                    // Leave an empty line after selection images.
                    if (isSelectionImage)
                    {
                        // Add selection separator.
                        // New separator:
                        writer.write("<img id = \"line_separator\" src = \"" + /*pathToBallotVVPATFolder*/ LINE_SEPARATOR_IMAGE + "\" alt = \"Image not found\" width = \"" + (TWO_COLUMNS_COLUMN_SIZE - 24) + "\" height = \"1\" align = \"right\" style=\"margin:0 0 8px 0;display:block;\">\n");
                        // Old separators:
                        // writer.write("<br>\n<br>\n");
                        // writer.write("<div id = \"bar\" style = \"background-color:#000000;width:" + TWO_COLUMNS_COLUMN_SIZE + "px;\"><hr></div>\n");
                        i++;
                    }
                    // Set the flag for selection images and reset it for label/title images.
                    i++;
                    isSelectionImage = !isSelectionImage;
                }
            }
            catch (IndexOutOfBoundsException e)
            {
                System.out.println("HTML File Generator Exception Caught: IndexOutOfBoundsException: Found an odd number of columns when trying to print in two-columns format.");
            }

            // End of the right column.
            writer.write("</div>\n");
            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            // Add the barcode to the container (bottom).
            if (printFriendly)
            {
                writer.write("<div id = \"barcode_bottom\" style = \"background-color:#FFFFFF;width:" + ONE_COLUMN_COLUMN_SIZE + "px;height:" + BARCODE_DIVIDER_HEIGHT + "px;float:right;text-align:center;\">\n");
                writer.write("<center><img src = \"" + BARCODE_IMAGE + ".png\" alt = \"Image did not load properly\" width = \"" + TWO_COLUMNS_COLUMN_SIZE + "\"></center>\n");
                writer.write("</div>\n");
            }
            else
            {
                writer.write("<div id = \"barcode_bottom\" style = \"background-color:#FF0000;width:" + ONE_COLUMN_COLUMN_SIZE + "px;height:" + BARCODE_DIVIDER_HEIGHT + "px;float:right;text-align:center;\">\n");
                writer.write("<center><img src = \"" + BARCODE_IMAGE + ".png\" alt = \"Image did not load properly\" width = \"" + TWO_COLUMNS_COLUMN_SIZE + "\"></center>\n");
                writer.write("</div>\n");
            }

            // End of the container for the content.
            writer.write("</div>\n");

            // End of the container for the columns.
            writer.write("</div>\n");

            // Add a space between divs.
            writer.write("<br>\n");
        }
        catch (IOException e)
        {
            System.out.println("HTML File Generator Error: Unable to write to file.");
        }

        // If there are more columns to be printed, call this helper again on the rest of the columns.
        if (imageNames.size() > 2)
        {
            generatorHelperForTwoColumns(writer, printFriendly, pathToBallotVVPATFolder, imageNames.subList(2,imageNames.size()));
        }
    }

    /**
     * Creates a "page" that contains one column of images.
     * @param writer - The writer to the HTML file
     * @param printFriendly - Whether or not to generate a printer-friendly HTML file. If true, then the HTML file will contain no colours and only black text on a white background.
     * @param pathToBallotVVPATFolder - The path to the vvpat folder in the ballot files
     * @param imageNames - ArrayLists of file names for images. One ArrayList per column.
     */
    private static void generatorHelperForOneColumn (BufferedWriter writer, Boolean printFriendly, String pathToBallotVVPATFolder, List<ArrayList<String>> imageNames)
    {
        try
        {
            // Writes the header of the page(s) to be printed.
            // <p style="font-family:arial;color:red;font-size:20px;">A paragraph.</p>
            writer.write("<p style = \"font-family:arial;color:black;font-size:20px;\">&nbsp;&nbsp;&nbsp;&nbsp;" + BALLOT_CONSTANTS.getElectionName() + "<br>\n");

            if (printFriendly)
            {
                // Creates the container for the columns of images.
                writer.write("<div id = \"container\" style = \"background-color:#FFFFFF;width:" + CONTAINER_WIDTH + "px;height:" + CONTAINER_HEIGHT + "px;\">\n");

                // Creates the left and right margins.
                writer.write("<div id = \"left_margin\" style = \"background-color:#FFFFFF;width:" + LEFT_MARGIN_WIDTH + "px;height:" + CONTAINER_HEIGHT + "px;float:left;\"><br></div>\n");
                writer.write("<div id = \"right_margin\" style = \"background-color:#FFFFFF;width:" + RIGHT_MARGIN_WIDTH + "px;height:" + CONTAINER_HEIGHT + "px;float:right;\"><br></div>\n");

                // Creates the divider for the content.
                writer.write("<div id = \"content\" style = \"background-color:#FFFFFF;width:" + ONE_COLUMN_COLUMN_SIZE + "px;height:" + CONTAINER_HEIGHT + "px;float:left;\">\n");

                // Add the barcode to the container (top).
                writer.write("<div id = \"barcode_top\" style = \"background-color:#FFFFFF;width:" + ONE_COLUMN_COLUMN_SIZE + "px;height:" + BARCODE_DIVIDER_HEIGHT + "px;float:left;text-align:center;\">\n");
                writer.write("<center><img src = \"" + BARCODE_IMAGE + "_flipped.png\" alt = \"Image did not load properly\" width = \"" + TWO_COLUMNS_COLUMN_SIZE + "\"></center>\n");
                writer.write("</div>\n");
            }
            else
            {
                // Creates the container for the columns of images.
                writer.write("<div id = \"container\" style = \"background-color:#CCFF00;width:" + CONTAINER_WIDTH + "px;height:" + CONTAINER_HEIGHT + "px;\">\n");

                // Creates the left and right margins.
                writer.write("<div id = \"left_margin\" style = \"background-color:#000000;width:" + LEFT_MARGIN_WIDTH + "px;height:" + CONTAINER_HEIGHT + "px;float:left;\"><br></div>\n");
                writer.write("<div id = \"right_margin\" style = \"background-color:#000000;width:" + RIGHT_MARGIN_WIDTH + "px;height:" + CONTAINER_HEIGHT + "px;float:right;\"><br></div>\n");

                // Creates the divider for the content.
                writer.write("<div id = \"content\" style = \"background-color:#CCFFFF;width:" + ONE_COLUMN_COLUMN_SIZE + "px;height:" + CONTAINER_HEIGHT + "px;float:left;\">\n");

                // Add the barcode to the container (top).
                writer.write("<div id = \"barcode_top\" style = \"background-color:#FF0000;width:" + ONE_COLUMN_COLUMN_SIZE + "px;height:" + BARCODE_DIVIDER_HEIGHT + "px;float:left;text-align:center;\">\n");
                writer.write("<center><img src = \"" + BARCODE_IMAGE + "_flipped.png\" alt = \"Image did not load properly\" width = \"" + TWO_COLUMNS_COLUMN_SIZE + "\"></center>\n");
                writer.write("</div>\n");
            }


            // Column ///////////////////////////////////////////////////////////////////////////////////////////////////////////
            if (printFriendly)
            {
                // Create the column.
                writer.write("<div id = \"column\" style=\"background-color:#FFFFFF;width:" + ONE_COLUMN_COLUMN_SIZE + "px;height:" + (CONTAINER_HEIGHT - 2 * BARCODE_DIVIDER_HEIGHT) + "px;float:left;\">\n");
            }
            else
            {
                // Create the column.
                writer.write("<div id = \"column\" style=\"background-color:#ABCDEF;width:" + ONE_COLUMN_COLUMN_SIZE + "px;height:" + (CONTAINER_HEIGHT - 2 * BARCODE_DIVIDER_HEIGHT) + "px;float:left;\">\n");
            }

            // Put images in the column.
            ArrayList<String> column = imageNames.get(0);
            Boolean isSelectionImage = false; // Used to leave an empty line after every selectionImage
            int i = 0;  //counts iterations allowing one to add margins to every first and second image accordingly
            for (String imageName : column)
            {
                // Load in the image.
                writer.write("<img src = \"" + pathToBallotVVPATFolder + imageName + "\" alt = \"Image did not load properly\" width = \"" + ONE_COLUMN_COLUMN_SIZE + "\">\n");

                if(i%3==1){
                    writer.write(" style=\"margin:0 0 2px 0;display:block;\">\n");
                }
                else if(i%3==0){
                    writer.write(" style=\"margin:0 0 0 0; display:block;\">\n");
                }
                // Leave an empty line after selection images.
                if (isSelectionImage)
                {
                    // Add selection separator.
                    // New separator:
                    writer.write("<img id = \"line_separator\" src = \"" + /*pathToBallotVVPATFolder*/ LINE_SEPARATOR_IMAGE + "\" alt = \"Image not found\" width = \"" + (ONE_COLUMN_COLUMN_SIZE - 24) + "\" height = \"1\" align = \"right\" style=\"margin:0 0 8px 0;display:block;\">\n");
                    // Old separators:
                    // writer.write("<br>\n<br>\n");
                    // writer.write("<div id = \"bar\" style = \"background-color:#000000;width:" + TWO_COLUMNS_COLUMN_SIZE + "px;\"><hr></div>\n");
                    i++;
                }
                // Set the flag for selection images and reset it for label/title images.
                isSelectionImage = !isSelectionImage;
                i++;
            }

            // End of the column.
            writer.write("</div>\n");
            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            // Add the barcode to the container (bottom).
            if (printFriendly)
            {
                writer.write("<div id = \"barcode_bottom\" style = \"background-color:#FFFFFF;width:" + ONE_COLUMN_COLUMN_SIZE + "px;height:" + BARCODE_DIVIDER_HEIGHT + "px;float:right;text-align:center;\">\n");
                writer.write("<center><img src = \"" + BARCODE_IMAGE + ".png\" alt = \"Image did not load properly\" width = \"" + TWO_COLUMNS_COLUMN_SIZE + "\"></center>\n");
                writer.write("</div>\n");
            }
            else
            {
                writer.write("<div id = \"barcode_bottom\" style = \"background-color:#FF0000;width:" + ONE_COLUMN_COLUMN_SIZE + "px;height:" + BARCODE_DIVIDER_HEIGHT + "px;float:right;text-align:center;\">\n");
                writer.write("<center><img src = \"" + BARCODE_IMAGE + ".png\" alt = \"Image did not load properly\" width = \"" + TWO_COLUMNS_COLUMN_SIZE + "\"></center>\n");
                writer.write("</div>\n");
            }

            // End of the container for the content.
            writer.write("</div>\n");

            // End of the container for the column.
            writer.write("</div>\n");

            // Add a space between divs.
            writer.write("<br>\n");
        }
        catch (IOException e)
        {
            System.out.println("HTML File Generator Error: Unable to write to file.");
        }

        // If there are more columns to be printed, call this helper again on the rest of the columns.
        if (imageNames.size() > 1)
        {
            generatorHelperForOneColumn(writer, printFriendly, pathToBallotVVPATFolder, imageNames.subList(1, imageNames.size()));
        }
    }

}
