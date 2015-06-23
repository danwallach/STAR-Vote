package preptool.converter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Tool for scaling compiled ballots to arbitrary resolutions,
 * useful for low powered & low resolution systems.
 *
 * @author Kevin Montrose
 */
public class BallotScaler {

    /** a constant that serves as the width dimension */
    private static final int WINDOW_WIDTH = 1600;

    /** A constant for the height dimension */
    private static final int WINDOW_HEIGHT = 900;

    /**
     * Copies the zip input stream to a zip output stream, used for rewriting the ballot
     * to the zip format after scaling.
     *
     * @param in the input file (where the ballot was loaded from)
     * @param name the name of the entry to be written into the new file
     * @param out the destination zip file for the component
     *
     * @throws IOException if the file writing or reading process errors
     */
	protected static void copy(InputStream in, String name, ZipOutputStream out) throws IOException {
        /* make a new entry in the destination for the object being copied */
		out.putNextEntry(new ZipEntry(name));

        /* now write the object, using error checking, to the output file */
		int i;
		while((i = in.read()) != -1)
			out.write(i);
	}

    /**
     * Updates xml components in layout.xml to reflect the scaling of the ballot.
     *
     * @param in the original layout.xml
     * @param name the name of the component that has been reformatted
     * @param out the destination for the fixed layout.xml
     * @param scaleX the amount that the object was scaled horizontally
     * @param scaleY the amount that the object was scaled vertically
     * @throws IOException if any of the file reads or writes go awry
     */ /* TODO Rewrite this method to be a lot less stupid */
	protected static void scaleLayout(InputStream in, String name, ZipOutputStream out, double scaleX, double scaleY) throws IOException {

        /* Output stream that will buffer data to be changed and then written out */
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

        /* Read in data from the source, and put it in the buffer */
		int i;
		while((i = in.read()) != -1) baos.write(i);

        /* The data that was read in was technically a string (due to the input file being in xml format */
		String str = new String(baos.toByteArray());

        /* Make a place in the outbound zip file for the newly re-scaled component */
		out.putNextEntry(new ZipEntry(name));


        /* Reset the file read count */
		i = 0;

        /* Create a new counter to keep track of our place in the given input string from the buffer */
		int oldI = i;

        /* Create a temporary string buffer for keeping track of our place in the input string */
		String buf = "";

        /* Find the horizontal components in the xml and rewrite them to reflect the scaling */
        /* TODO Shouldn't this be an if since it only happens once? */
		while((i = str.indexOf("x=\"", i)) != -1) {
            /*
             * Since we know the string preceding the old dimensions are of the form 'x="0"', we look past the
             * first three characters in it to get to the numerical values
             */
			i += 3;

            /* grab all of the information for this component's horizontal sizing */
			buf+=str.substring(oldI, i);

            /* Grab the index of the numerical value from the xml string */
			int j = str.indexOf("\"", i);

            /* Cast the value to a double so it will scale with proper rounding */
			double newX = Integer.parseInt(str.substring(i,j));

            /* Now scale the value */
			newX *= scaleX;

            /* Package it back into a string, casting it back to an integer to lose any mantissa */
			buf += (""+(int)newX);

            /* Update the old index with the index of the last found numerical value */
			oldI = j;
		}

        /* Since the y-components always follow the x-components, set up the buffer to point just after the last x */
		buf += str.substring(oldI);

        /* reset the indices for the y values */
		i = 0;
		oldI = i;

        /* Save and then reset the buffer, for semantic clarity */
		str = buf;
		buf = "";

        /* Now look through the xml substring for instances of, e.g., "y="1"'*/
        /* TODO Theoretically this can be done immediately after the x's, reusing all of the same variables and values */
		while((i = str.indexOf("y=\"", i)) != -1) {

            /* Does the same as the x-values, refer to those comments*/
			i+=3;
			buf+=str.substring(oldI, i);
			
			int j = str.indexOf("\"", i);
			double newY = Integer.parseInt(str.substring(i,j));

			newY *= scaleY;

			buf += (""+(int)newY);
			
			oldI = j;
		}

        /* Now let the buffer contain the wholly updated string */
		buf += str.substring(oldI);

        /* Write out the new string with formatted values */
		out.write(buf.getBytes());
	}

    /**
     * Scales the various image files inside the ballot.zip
     *
     * @param in the file stream for a given image
     * @param name the name of the image, so it can be written out
     * @param out the desired destination of the newly scaled image
     * @param scaleX the amount that the object should be scaled horizontally
     * @param scaleY the amount that the object should be scaled vertically
     * @throws IOException if any of the file reads or writes go awry
     */
	protected static void scaleImage(InputStream in, String name, ZipOutputStream out, double scaleX, double scaleY) throws IOException {

        /* First read in the image data to be scaled */
		BufferedImage img = ImageIO.read(in);

        /* Now use Java's provided scaling methods to scale the image to the desired size */
		Image scaled = img.getScaledInstance((int)(img.getWidth()*scaleX), (int)(img.getHeight()*scaleY), Image.SCALE_SMOOTH);

        /* Now build a buffered image whose graphics we can draw our newly scaled image on */
		BufferedImage bScaled = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);

        /* Now write the scaled image data on to the new BufferedImage */
		bScaled.getGraphics().drawImage(scaled, 0, 0,null);

        /* Write out the newly scaled image to a properly names zip entry */
		out.putNextEntry(new ZipEntry(name));
		ImageIO.write(bScaled, "PNG", out);
	}

    /**
     * Scales a ballot, given a valid input ballot file and a valid destination
     *
     * @param in the ballot.zip file to be scaled
     * @param out a new file (e.g. scaled_ballot.zip) to write the newly scaled ballot to
     * @param scaleX the amount that the ballot should be scaled horizontally
     * @param scaleY the amount that the ballot should be scaled vertically
     * @throws IOException if any of the file reads or writes go awry
     */
	protected static void scaleBallot(File in, File out, double scaleX, double scaleY) throws IOException {

        /* Set up the ballot file to be read in */
		ZipFile ballot = new ZipFile(in);

        /* Create an output stream to write the newly scaled ballot components to */
		ZipOutputStream scaledBallot = new ZipOutputStream(new FileOutputStream(out));

        /* Put all of the entries in the zip input file into an enumeration to be iterated through */
		Enumeration<? extends ZipEntry> entries = ballot.entries();

        /* Iterate over all of the elements in the zip file */
		while (entries.hasMoreElements()) {

            /* Get out the next element */
			ZipEntry entry = entries.nextElement();

            /* If the found object was an image, scale it accordingly */
			if (entry.getName().endsWith(".png")) {
				scaleImage(ballot.getInputStream(entry), entry.getName(), scaledBallot, scaleX, scaleY);
				continue;
			}

            /* If the found object was an xml file, and was not the ballot.xml file, scale it accordingly */
			if (entry.getName().endsWith(".xml") && !entry.getName().endsWith("ballot.xml")) {
				scaleLayout(ballot.getInputStream(entry), entry.getName(), scaledBallot, scaleX, scaleY);
				continue;
			}

			/* Now copy the scaled results into the output file */
			copy(ballot.getInputStream(entry), entry.getName(), scaledBallot);
		}

        /* Flush and close the output stream to finish up the scaling */
		scaledBallot.flush();
		scaledBallot.close();
	}

    /**
     * Can be run in the command line to scale any input ballot file
     *
     * @param args input string with four necessary inputs:
     *                  1 - The name of the existing ballot file to scale
     *                  2 - The name of the new ballot file to write the scaled data to
     *                  3 - The new width the ballot should be scaled to
     *                  4 - the new height the ballot should be scaled to
     *
     * @throws IOException if any of the file reads or writes go awry
     */
	public static void main(String[] args) throws IOException {

		if (args.length != 4) System.exit(-1);

		File ballot = new File(args[0]);
		if (!ballot.exists()) System.exit(-1);

		File outBallot = new File(args[1]);
		if (outBallot.exists()) System.exit(-1);

		int width = Integer.parseInt(args[2]);
		int height = Integer.parseInt(args[3]);
		
		double scaleX = ((double)width)  / ((double) WINDOW_WIDTH);
		double scaleY = ((double)height) / ((double) WINDOW_HEIGHT);
		
		scaleBallot(ballot, outBallot, scaleX, scaleY);
	}

}
