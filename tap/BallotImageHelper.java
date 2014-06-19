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

package tap;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

import votebox.middle.IBallotVars;
import votebox.middle.IncorrectTypeException;
import votebox.middle.ballot.Ballot;
import votebox.middle.ballot.BallotParser;
import votebox.middle.ballot.BallotParserException;
import votebox.middle.ballot.Card;
import votebox.middle.ballot.SelectableCardElement;
import votebox.middle.driver.Driver;
import votebox.middle.driver.GlobalVarsReader;

public class BallotImageHelper {
	
	/**
	 * Loads a map of "race-id" to "title label" from a ballot.
	 * 
	 * @param ballotFile        the ballot file
	 * @return                  a map if it exists, or null otherwise
	 */
	public static Map<String, Image> loadBallotTitles(File ballotFile){
		return loadBallotTitles(ballotFile.getAbsolutePath());
	}
	
	/**
	 * Loads the VVPAT ready files from the ballot.
	 * 
	 * @param ballotFile        the ballot file
	 * @return                  a map of "image-id" (L**, B**, etc.) to vvpat ready images.
	 */
	public static Map<String, Image> loadImagesForVVPAT(File ballotFile){
		return loadImagesForVVPAT(ballotFile.getAbsolutePath());
	}
	
	/**
	 * Loads the VVPAT ready files from the ballot.
	 * 
	 * @param ballotPath        the path to the ballot file
	 * @return                  a map of "image-id" (L**, B**, etc.) to vvpat ready images.
	 */
	public static Map<String, Image> loadImagesForVVPAT(String ballotPath){

		Map<String, Image> vvpatMap = new HashMap<String, Image>();

		try {

			ZipFile file = new ZipFile(ballotPath);
			Enumeration<? extends ZipEntry> entries = file.entries();

            /* Cycle through all the entries */
			while (entries.hasMoreElements()) {

				ZipEntry entry = entries.nextElement();

                /* Make sure it's the type of file we want */
				if (entry.getName().endsWith(".png") && entry.getName().contains("_printable_")) {

                    /* Get the name and largest substring we care about */
					String id = entry.getName();

                    /* Strip off any path information */
                    id = id.substring(id.lastIndexOf("/") + 1);

                    /* This way we only look at the images generated for the printer */
					int sub = id.indexOf("_printable_");

                    /* Ballot names will always have underscores, so if it doesn't, skip it */
					if(sub == -1) continue;

                    /* Extract the substring we care about */
					id = id.substring(0, sub);

                    /* Map this to the image */
					vvpatMap.put(id, ImageIO.read(file.getInputStream(entry)));
				}
			}
		}
        catch(Exception e){ e.printStackTrace(); return null; }

		return vvpatMap;
	}
	
	/**
	 * Loads a map of "race-id" to "title label" from a ballot.
	 * 
	 * @param ballotPath        path to the ballot file
	 * @return                  a map if it exists, or null otherwise
	 */
	public static Map<String, Image> loadBallotTitles(String ballotPath) {

        Map<String, Image> titleMap = new HashMap<String, Image>();

		try {

			Ballot ballot = getBallot(ballotPath);

			List<Card> cards = ballot.getCards();

            /* Look through all the cards */
			for(Card card : cards){

				try {

					String label = card.getProperties().getString("TitleLabelUID");

					if (label != null) {

						Image labelImg = loadLabel(label, ballotPath);

                        /* Map the image to the UID */
						for (SelectableCardElement element : card.getElements())
							titleMap.put(element.getUniqueID(), labelImg);

						/* So a lookup on the CARD will get the title image */
						titleMap.put(card.getUniqueID(), labelImg);
					}
				}
                catch(IncorrectTypeException e) { /* Fail silently, and move on */ }
			}
		}
        catch (IOException | BallotParserException e) { e.printStackTrace(); return null; }

		/* If we didn't get ANY title images, act like we failed. */
		if(titleMap.size() == 0) return null;

		return titleMap;
	}

	/**
	 * Loads the image associated with the given label uid.
	 * 
	 * @param label         string in the form L??, as a valid uid.
	 * @param ballot        the path to the ballot file
	 * @return              the corresponding image.
     *
     * @throws RuntimeException if there's a problem reading the image from the file
	 */
	private static Image loadLabel(String label, String ballot) throws RuntimeException {

		try {

			ZipFile file = new ZipFile(ballot);

			Enumeration<? extends ZipEntry> entries = file.entries();

            /* Look through all the entries */
			while (entries.hasMoreElements()) {

				ZipEntry entry = entries.nextElement();
				String name = entry.getName();

                /* Force the this to only return English titles */
				boolean isLabel = name.startsWith("media/"+label+"_1_en") && name.endsWith(".png");

                /* Pull and trim the image if it's English */
				if (isLabel) {
					BufferedImage image = ImageIO.read(file.getInputStream(entry));
					return trimImage(image);
				}
			}
		}
        catch (IOException e) { throw new RuntimeException(e); }

        /* Throw a runtime exception if it can't find any English images at all */
		throw new RuntimeException("Couldn't load image associated with label <"+label+"> from <"+ballot+">");
	}

	/**
	 * Trims the given image so that there is no leading white/transparent block.
	 * 
	 * @param image         image to trim
	 * @return              trimmed image
	 */
	private static Image trimImage(BufferedImage image) {

		try {

            /* Create an int array to represent the picture */
			int[] pix = new int[image.getWidth() * image.getHeight()];

            /* Set up a PixelGrabber */
			PixelGrabber grab = new PixelGrabber(image, 0, 0, image.getWidth(), image.getHeight(), pix, 0, image.getWidth());

            /* If there's an error, return the image */
			if(!grab.grabPixels()) return image;

			int lastClearRow = 0;

            /* TODO fix this goto crap */
			out:

				for (int x = 1; x < image.getWidth(); x++){

					for (int y = 0; y < image.getHeight(); y++){

						int i = y*image.getWidth() + x;
						int pixel = pix[i];

						int alpha = (pixel >> 24) & 0xff;
						int red   = (pixel >> 16) & 0xff;
						int green = (pixel >>  8) & 0xff;
						int blue  = (pixel      ) & 0xff;

						if(alpha == 0) continue;
						if(red == 255 && green == 255 && blue == 255) continue;

						break out;
					}

					lastClearRow = x;
				}

				return image.getSubimage(lastClearRow, 0, image.getWidth() - lastClearRow, image.getHeight());
		}
        catch (InterruptedException e) { return image; }
	}

	/**
	 * Grabs the list of languages from the ballot if it exists
	 * 
	 * @param ballotPath        the path to the ballot
	 * @return                  the list of languages in string form, or null
     *                          if there was a problem (ex. only 1 language)
	 */
	public static List<String> getLanguages(String ballotPath){

        try {

            Ballot ballot = getBallot(ballotPath);

			List<String> lang;

            /* Pull the list of languages from the ballot and return */
            try { lang = ballot.getProperties().getStringList("Languages"); }
            catch (IncorrectTypeException e) { lang = null; }

			return lang;

		}
        catch (IOException | BallotParserException e) { return null; }

	}

	/**
	 * Takes the ballot path and returns the ballot in Ballot form
	 * 
	 * @param ballotPath        the path to the ballot zip file
	 * @return                  the Ballot
     *
	 * @throws IOException
	 * @throws BallotParserException
	 */
	@SuppressWarnings("ResultOfMethodCallIgnored")
    public static Ballot getBallot(String ballotPath) throws IOException, BallotParserException {

		/*
		  TODO ? GlobalVarsReader et. al. expect the ballot to be extracted. This is a very stupid assumption
		  TODO (why we aren't taking InputStreams is beyond me) but needs to hold for now.
		*/

        /* Create a new directory by using and deleting a temp file */
		File tempBallotPath = File.createTempFile("ballot", "path");
		tempBallotPath.delete();

        /* Create a new file there and a directory */
		tempBallotPath = new File(tempBallotPath,"data");
		tempBallotPath.mkdirs();

        /* Unzip into this directory */
		Driver.unzip(ballotPath, tempBallotPath.getAbsolutePath());

		IBallotVars vars    = new GlobalVarsReader(tempBallotPath.getAbsolutePath()).parse();
		BallotParser parser = new BallotParser();

        /* Pull the ballot out and return as a Ballot */
		return parser.getBallot(vars);
	}
}
