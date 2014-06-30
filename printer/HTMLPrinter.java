package printer;

import votebox.AuditoriumParams;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A class which provides tools to write images to HTML files. Might be useful in printing higher quality images.
 * @author Arghya Chatterjee, Mircea C. Berechet
 */
public class HTMLPrinter {

     /**
     * Generates a HTML file that will be used to print a voter's selections.
     * @param filename                  the name of the HTML file to be written
     * @param useTwoColumns             whether or not to use two columns of images per page
     * @param pathToBallotVVPATFolder   the path to the vvpat folder in the ballot files
     * @param ballotConstants           the object that contains ballot parameters (such as election name)
     * @param imageNames                ArrayLists of file names for images. One ArrayList per column.
     */

    public static void generateHTMLFile(String filename, Boolean useTwoColumns,
                                        String pathToBallotVVPATFolder, AuditoriumParams ballotConstants,
                                        List<ArrayList<String>> imageNames) {

        File file = new File(filename);

        /* If the file does not exist, then create it. */
        if (!file.exists())
            try { file.createNewFile(); }
            catch (IOException e) { System.err.println("HTML File Generator Error: Unable to create file '" + filename + "'"); }

        /* Create the writer. */
        BufferedWriter writer = null;
        try { writer = new BufferedWriter(new FileWriter(file.getAbsoluteFile())); }
        catch (IOException e) { System.err.println("HTML File Generator Error: Unable to create BufferedWriter for file '" + filename + "'"); }

        /* Actually write the HTML file. */

        if (writer != null) {

            try {
                /* Beginning of the file. */
                writer.write("<html>\n");

                /* Beginning of head. */
                writer.write("<head>\n");

                /* End of head. */
                writer.write("</head>\n");

                /* Beginning of body. */
                writer.write("<body bgcolor = \"#FFFFFF\" text = \"#000000\">\n");

                /* Ballot printing uses one-column format */
                if (!useTwoColumns)
                    generatorHelperForOneColumn(writer, pathToBallotVVPATFolder, imageNames);

                /* Ballot printing uses two-column format */
                else
                    generatorHelperForTwoColumns(writer, pathToBallotVVPATFolder, imageNames);

                /* End of body. */
                writer.write("</body>\n");

                /* End of file. */
                writer.write("</html>\n");

                writer.flush();
                writer.close();
            }
            catch (IOException e) { System.err.println("HTML File Generator Error: Unable to write to file '" + filename + "'"); }
        }
    }

    /**
     * Creates a "page" that contains two columns of images.
     * @param writer - The writer to the HTML file
     * @param pathToMediaFolder - The path to the vvpat folder in the ballot files
     * @param imageNames - ArrayLists of file names for images. One ArrayList per column.
     */
    private static void generatorHelperForTwoColumns(BufferedWriter writer, String pathToMediaFolder, List<ArrayList<String>> imageNames) throws IOException {

        /* HTML stuff being read from the file. */
        BufferedReader reader;

        try
        {
            reader = new BufferedReader(new FileReader("printer/printFriendly.txt"));
            String currentLine;
            while (!(currentLine = reader.readLine()).equals("*")) {
                writer.write(currentLine + "\n");
            }

            /* Put images in the left column. */
            ArrayList<String> left_column = imageNames.get(0);
            Boolean isSelectionImage = false;

            for (String imageName : left_column)
            {

                /* Load in the image. */
                writer.write("<img src = \"" + pathToMediaFolder + imageName +  "\" alt = \"Image did not load properly\" width = \"334\">\n");

                /* Leave an empty line after selection images. */
                if (isSelectionImage)
                {
                    /* Add selection separator.*/
                    while ((currentLine = reader.readLine())!=null && (currentLine.startsWith("<img id = \"line_sep")))
                        writer.write(currentLine);

                }

                /* Set the flag for selection images and reset it for label/title images. */
                isSelectionImage = !isSelectionImage;
            }

            /* End of the left column. */
            writer.write("</div>\n");

            /* Right Column */

            writer.write("<div id = \"right_column\" style=\"background-color:#FFFFFF;width:334px;height:780px;float:left;\">\n");

            /* Put images in the right column. */
            if (imageNames.size() > 1) {
                ArrayList<String> right_column = imageNames.get(1);
                isSelectionImage = false;
                for (String imageName : right_column) {
                    /* Load in the image. */
                    writer.write("<img src = \"" + pathToMediaFolder + imageName + "\" alt = \"Image did not load properly\" width = \"334\">\n");

                    /*  Leave an empty line after selection images. */
                    if (isSelectionImage) {

                        /* Add selection separator. */
                        while ((currentLine = reader.readLine()) != null && (currentLine.startsWith("<img id = \"line_sep")))
                            writer.write(currentLine);

                    }
                /* Set the flag for selection images and reset it for label/title images. */
                    isSelectionImage = !isSelectionImage;
                }

            }

            /* End of the right column.*/
            writer.write("</div>\n");

            /* Add the barcode to the container (bottom).*/

            while ((currentLine = reader.readLine()) != null){
                if(currentLine.startsWith("<div id = \"barcode_bottom"))
                    writer.write(currentLine+"\n");
                if(currentLine.startsWith("<center><img src = \"Barcode.png\""))
                    writer.write(currentLine+"\n");
            }

            writer.write("</div>\n");

            /* End of the container for the content.*/
            writer.write("</div>\n");

            /* End of the container for the columns. */
            writer.write("</div>\n");

            /* Add a space between divs. */
            writer.write("<br>\n");
            reader.close();
        }
        catch (IOException e)
        {
            System.out.println("Unable to read from file printFriendly.txt");
            e.printStackTrace();
            return;
        }

        /* If there are more columns to be printed, call this helper again on the rest of the columns. */
        if (imageNames.size() > 2)
        {
            generatorHelperForTwoColumns(writer, pathToMediaFolder, imageNames.subList(2,imageNames.size()));
        }
    }

    /**
     * Creates a "page" that contains one column of images.
     * @param writer - The writer to the HTML file
     * @param pathToMediaFolder - The path to the vvpat folder in the ballot files
     * @param imageNames - ArrayLists of file names for images. One ArrayList per column.
     */
    private static void generatorHelperForOneColumn(BufferedWriter writer, String pathToMediaFolder, List<ArrayList<String>> imageNames)
    {
        /* Writes the header of the page(s) to be printed. */

        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader("printer/printFriendly.txt"));
            String currentLine;

            while (!(currentLine = reader.readLine()).equals("*")) {
                writer.write(currentLine + "\n");
            }

            /* Put images in the column. */
            ArrayList<String> column = imageNames.get(0);

            /* Used to leave an empty line after every selectionImage */
            Boolean isSelectionImage = false;

            for (String imageName : column) {
                /* Load the image. */
                writer.write("<img src = \"" + pathToMediaFolder + imageName + "\" alt = \"Image did not load properly\" width = \"310\">\n");

                /* Leave an empty line after selection images. */
                if (isSelectionImage) {
                    while ((currentLine = reader.readLine()) != null && (currentLine.startsWith("<img id = \"line_sep")))
                        writer.write(currentLine);
                }
                /*  Set the flag for selection images and reset it for label/title images.*/
                isSelectionImage = !isSelectionImage;
            }

            /* End of the column.*/
            writer.write("</div>\n");

            /* Add the barcode to the container (bottom). */
            while ((currentLine = reader.readLine()) != null){
                if(currentLine.startsWith("<div id = \"barcode_bottom"))
                    writer.write(currentLine+"\n");
                if(currentLine.startsWith("<center><img src = \"Barcode.png\""))
                    writer.write(currentLine+"\n");
            }

            writer.write("</div>\n");

            /* End of the container for the content. */
            writer.write("</div>\n");

            /* End of the container for the column. */
            writer.write("</div>\n");

            /* Add a space between divs. */
            writer.write("<br>\n");
        } catch (IOException e) {
            System.out.println("Unable to read from file printFriendly.txt");
            e.printStackTrace();
            return;
        }

        /* If there are more columns to be printed, call this helper again on the rest of the columns. */
        if (imageNames.size() > 1)
        {
            generatorHelperForOneColumn(writer, pathToMediaFolder, imageNames.subList(1, imageNames.size()));
        }
    }
}

