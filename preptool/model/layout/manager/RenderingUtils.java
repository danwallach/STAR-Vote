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

package preptool.model.layout.manager;

import printer.PrintImageUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import printer.PrintImageUtils;


/**
 * A set of static functions useful for rendering different types of layout
 * components. These methods are independent of implementation as they take many
 * customization parameters.
 * @author cshaw, ttorous
 */
public class RenderingUtils {

	/**
	 * Max dimensions for buttons. Longer text will be clipped.
	 */
	public static final int MAX_BUTTON_WIDTH = 600;
	public static final int MAX_BUTTON_HEIGHT = 100;

    /**
     * Scaling factor for high dpi printed images
     * Choose this value based on a 360 dpi printer and the fact that java's default is 72 dpi and 360/72 is a whole number
     */
    public static final int DPI_SCALE_FACTOR = 360/72;

    /**
     * The dimensions of the selection box.
     */
    public static final int SELECTION_BOX_WIDTH = 15*DPI_SCALE_FACTOR;
    public static final int SELECTION_BOX_HEIGHT = 10*DPI_SCALE_FACTOR;

	/**
	 * The standard font to use
	 */
	public static final String FONT_NAME = "Arial Unicode";

	/**
	 * A dummy 1x1 image used for getting the sizes of components
	 */
	private static final BufferedImage DUMMY_IMAGE = new BufferedImage(1, 1,
			BufferedImage.TYPE_INT_ARGB);

	/**
	 * Copies a buffered Image. Borrowed 100% from
	 * http://cui.unige.ch/~deriazm/javasources/ImgTools.java I really can't
	 * think of a better way of writing this code - so i just used theirs
	 */
	public static BufferedImage copy(BufferedImage bImage) {
		int w = bImage.getWidth(null);
		int h = bImage.getHeight(null);
		BufferedImage bImage2 = new BufferedImage(w, h,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = bImage2.createGraphics();
		g2.drawImage(bImage, 0, 0, null);
		return bImage2;
	}

	/**
	 * Calculates the size of a button.<br>
	 * Note: Buttons do not automatically wrap - must be wrapped w/ a \n in the
	 * text of the button
	 * @param text the text of the button
	 * @param fontsize the size of the font to use
	 * @param bold whether the button has bold text
	 * @return the size of the Button
	 */
	public static Dimension getButtonSize(String text, int fontsize,
			boolean bold) {

		Font font = new Font(FONT_NAME, (bold) ? Font.BOLD : Font.PLAIN,
				fontsize);

		BufferedImage wrappedImage = DUMMY_IMAGE;

		Graphics2D graphs = wrappedImage.createGraphics();
		graphs.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		graphs.setFont(font);

		int baseline = graphs.getFontMetrics().getAscent();

		String[] words = text.split(" ");
		int padding = 10;
		int heightPos = padding + baseline;
		int lineWidth = padding;

		int maxWidth = 0; // the max width of any line
		for (String word : words) // For each word try placing it on the line,
		// if not jump down a line and then write it
		{
			Rectangle2D measurement = font.getStringBounds(word + " ",
					new FontRenderContext(null, true, true));
			int wordWidth = (int) measurement.getWidth();
			int wordHeight = (int) measurement.getHeight();
			lineWidth += wordWidth;

			if (word.equals("\n")) {
				maxWidth = Math.max(lineWidth, maxWidth);
				heightPos += wordHeight;
				lineWidth = padding;
			}
		}
		maxWidth = Math.max(lineWidth, maxWidth);

		return new Dimension(maxWidth, heightPos + padding);
	}

	/**
	 * Calculates the size of a Label.
	 * @param title is the text to be outputted
	 * @param instructions are any instructions to be italized, such as '(please
	 *            select one)'
	 * @param description are any descriptions (such as those used on
	 *            propositions)
	 * @param fontsize the size of the font
	 * @param wrappingWidth is the width at which the label should wrap
	 * @param bold whether the label is bold
	 * @param titleCentered is a boolean flag as to whether or not the text
	 *            should be centered
	 * @return the size of the Label
	 */
	public static Dimension getLabelSize(String title, String instructions,
			String description, int fontsize, int wrappingWidth, boolean bold,
			boolean titleCentered) {

		Font font = new Font(FONT_NAME, (bold) ? Font.BOLD : Font.PLAIN,
				fontsize);
		Font italicFont = new Font(FONT_NAME, Font.ITALIC, fontsize);
		Font bigBoldFont = new Font(FONT_NAME, Font.BOLD, fontsize + 4);

		BufferedImage wrappedImage = DUMMY_IMAGE;

		Graphics2D graphs = wrappedImage.createGraphics();
		graphs.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		graphs.setFont(font);

		int baseline = graphs.getFontMetrics().getAscent();

		String[] titleWords = title.split(" ");
		int padding = 10;
		int heightPos = padding + baseline;
		int lineWidth = padding;

		int maxWidth = 0; // the max width of any line
		if (titleCentered) {

			graphs.setFont(bigBoldFont);
			String[][] splitText = spliteOnNewLineAndSpace(title);
			for (int y = 0; y < splitText.length; y++) {
				heightPos += lineHeight("line", bigBoldFont);
			}

		} else {
			for (String word : titleWords) // For each word try placing it on
			// the line,
			// if not jump down a line and then write it
			{
				Rectangle2D measurement = font
						.getStringBounds(word + " ", new FontRenderContext(
								new AffineTransform(), true, true));
				int wordWidth = (int) measurement.getWidth();
				int wordHeight = (int) measurement.getHeight();
				lineWidth += wordWidth;

				if (word.equals("\n") || word.equals("<newline>")) {
					maxWidth = Math.max(lineWidth, maxWidth);
					heightPos += wordHeight;
					lineWidth = padding;
				}
			}

		}

		if (!instructions.equals("")) // write instructions on how to use
		{
			String[][] splitText = spliteOnNewLineAndSpace(instructions);

			for (int y = 0; y < splitText.length; y++) {
				heightPos += lineHeight("line", italicFont);
			}
		}

		if (!description.equals("")) // write description on how to use
		{
			heightPos += lineHeight("text", font);
			description = addInNewLines(description, font, wrappingWidth,
					padding);
			String[][] splitText = spliteOnNewLineAndSpace(description);

			for (int y = 0; y < splitText.length; y++) {
				heightPos += lineHeight("line", font);
			}
		}

		maxWidth = Math.max(lineWidth, maxWidth);
		if (wrappingWidth != 0) // then wrap at the wrappingWidth or maxWidth
			maxWidth = Math.max(maxWidth, wrappingWidth);

		return new Dimension(maxWidth, heightPos + padding);

	}

	/**
	 * Calculates the size of a ToggleButton.<br>
	 * ToggleButton does not wrap unless indicated to do so w/ \n. Also if two
	 * names are used the second name appears at an offset. And since this is a
	 * togglebutton a box and possible check mark in the box are added
	 * @param text is the text of the togglebutton
	 * @param text2 is the second text of the toggle button - added on a
	 *            secondline and indented
	 * @param party is the party of the candidate in the toggle button - right
	 *            aligned on first line of button
	 * @param fontsize the size of the font
	 * @param wrappingWidth width of the button
	 * @param bold whether the button is bold
	 * @return the size of the ToggleButton
	 */
	public static Dimension getToggleButtonSize(String text, String text2,
			String party, int fontsize, int wrappingWidth, boolean bold) {

		Font font = new Font(FONT_NAME, (bold) ? Font.BOLD : Font.PLAIN,
				fontsize);
		BufferedImage wrappedImage = DUMMY_IMAGE;

		Graphics2D graphs = wrappedImage.createGraphics();
		graphs.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		graphs.setFont(font);

		int baseline = graphs.getFontMetrics().getAscent();

		int padding = 10;
		int heightPos = padding + baseline;
		int lineWidth = padding;

		int maxWidth = 0; // the max width of any line

		if (!text2.equals("")) {
			heightPos += lineHeight(text, font);
		}

		maxWidth = Math.max(lineWidth, maxWidth);

		return new Dimension(wrappingWidth, heightPos + padding);
	}

	/**
	 * Renders a Button and returns it as a BufferedImage.<br>
	 * Buttons do not automatically wrap - must be wrapped w/ a \n in the text
	 * of the button
	 * @param text is the text of the button
	 * @param fontsize is the size of the font
	 * @param bold is whether the button is bold
	 * @param boxed whether the button is boxed
	 * @param backGroundColor is the background color of the button
	 * @param preferredWidth desired width of the button, honored if possible
	 * @return the rendered Button
	 */
	public static BufferedImage renderButton(String text, int fontsize,
			boolean bold, boolean boxed, int preferredWidth,
			Color backGroundColor) {

		Font font = new Font(FONT_NAME, (bold) ? Font.BOLD : Font.PLAIN,
				fontsize);

		BufferedImage wrappedImage = new BufferedImage(
				MAX_BUTTON_WIDTH, MAX_BUTTON_HEIGHT,
				BufferedImage.TYPE_INT_RGB);

		Graphics2D graphs = wrappedImage.createGraphics();
		graphs.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		graphs.setFont(font);

		graphs.setPaint(backGroundColor);
		graphs.fillRect(0, 0, MAX_BUTTON_WIDTH, MAX_BUTTON_HEIGHT);

		graphs.setColor(Color.BLACK); // Could make this a variable

		int baseline = graphs.getFontMetrics().getAscent();

		String[] words = text.split(" ");
		int padding = 10;
		int leading = 1;
		int heightPos = padding + baseline;
		int writePos = padding;

		int lineWidth = padding;

		int maxWidth = 0; // the max width of any line
		for (String word : words) // For each word try placing it on the line,
		// if not jump down a line and then write it
		{
            //Rectangle2D measurement = font.getStringBounds(word + "  ", new FontRenderContext(new AffineTransform(), true, true));
            String spacedWord = word + " ";
            int wordWidth = lineWidth(spacedWord.split("$"), font);
			writePos = lineWidth;
			lineWidth += wordWidth;

			if (word.equals("\n")) {
				maxWidth = Math.max(lineWidth, maxWidth);
				heightPos += baseline + leading;
				writePos = padding;
				lineWidth = padding;
			}
			graphs.drawString(word + " ", writePos, heightPos);
		}

		maxWidth = Math.max(lineWidth, maxWidth);

		if (preferredWidth > 0) maxWidth = preferredWidth;

		if (boxed) {
			graphs.setColor(Color.BLACK);
			graphs.setStroke(new BasicStroke(padding / 2));
			graphs.drawRect(0, 0, maxWidth - 1, heightPos + padding - 1);
		}

		// Cut the image down to the correct size
		wrappedImage = wrappedImage.getSubimage(0, 0, maxWidth, heightPos
				+ padding);

		return copy(wrappedImage);

	}

	/**
	 * Renders a Label and returns it as a BufferedImage.
	 * @param title is the text to be outputted
	 * @param instructions are any instructions to be italicized, such as '(please
	 *            select one)'
	 * @param description are any descriptions (such as those used on
	 *            propositions)
	 * @param fontsize the size of the font
	 * @param wrappingWidth is the width at which the label should wrap
	 * @param bold whether the label is bold
	 * @param color is the color of the text
	 * @param boxed is a boolean flag to determine whether or a box should be
	 *            placed around the label
	 * @param titleCentered is a boolean flag as to whether or not the text
	 *            should be centered
	 * @return the rendered Label
	 */
	public static BufferedImage renderLabel(String title, String instructions,
			String description, int fontsize, int wrappingWidth, Color color,
			boolean bold, boolean boxed, boolean titleCentered) {

		Font font = new Font(FONT_NAME, (bold) ? Font.BOLD : Font.PLAIN,
				fontsize);
		
		Font italicFont = new Font(FONT_NAME, Font.ITALIC, fontsize);
		Font bigBoldFont = new Font(FONT_NAME, Font.BOLD, fontsize + 4);

		BufferedImage wrappedImage = new BufferedImage(1000, 1000,
				BufferedImage.TYPE_INT_ARGB);

		Graphics2D graphs = wrappedImage.createGraphics();
		graphs.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		graphs.setFont(font);
		graphs.setColor(color);

		int baseline = graphs.getFontMetrics().getAscent();

		String[] titleWords = title.split(" ");
		int padding = 10;
		int heightPos = padding + baseline;
		int writePos = padding;

		int lineWidth = padding;

		int maxWidth = 0; // the max width of any line
		if (titleCentered) {

			graphs.setFont(bigBoldFont);
			String[][] splitText = spliteOnNewLineAndSpace(title);
			for (int y = 0; y < splitText.length; y++) {
				String[] line = splitText[y];
				int margin = (wrappingWidth - 2 * padding - lineWidth(line,
						bigBoldFont)) / 2;
				graphs.drawString(stringArrayToString(line), writePos + margin,
						heightPos);
				heightPos += lineHeight("line", bigBoldFont);
			}

		} else {
			for (String word : titleWords) // For each word try placing it on
			// the line,
			// if not jump down a line and then write it
			{
				Rectangle2D measurement = font
						.getStringBounds(word + " ", new FontRenderContext(
								new AffineTransform(), true, true));
				int wordWidth = (int) measurement.getWidth();
				int wordHeight = (int) measurement.getHeight();
				writePos = lineWidth;
				lineWidth += wordWidth;

                // TODO: this code does do what the comment above says it does!
                // It doesn't jump down a line on it's own! add this!

                // if the width of our word is longer than the entire line space,
                // break it up.
                if(wordWidth > wrappedImage.getWidth()) {
                    String remainingStr = word;
                    while(remainingStr.length() > wrappedImage.getWidth()) {
                        graphs.drawString(remainingStr.substring(0, wrappedImage.getWidth()),
                            writePos, heightPos);
                        remainingStr= remainingStr.substring(wrappedImage.getWidth());
                        // we've just written one whole line. put our position variables back
                        // at the beginning
                        heightPos+= wordHeight;
                        writePos= padding;
                        lineWidth= padding;
                    }

                } else if (word.equals("\n") || word.equals("<newline>")) {
					maxWidth = Math.max(lineWidth, maxWidth);
					heightPos += wordHeight;
					writePos = padding;
					lineWidth = padding;
				}
				graphs.drawString(word + " ", writePos, heightPos);
			}

		}

		if (!instructions.equals("")) // write instructions on how to use
		{
			// heightPos+= lineHeight(instructions.split(" "), italicFont);
			String[][] splitText = spliteOnNewLineAndSpace(instructions);

			for (int y = 0; y < splitText.length; y++) {
				String[] line = splitText[y];
				int margin = (wrappingWidth - 2 * padding - lineWidth(line,
						italicFont)) / 2;
				graphs.setFont(italicFont);
				graphs.drawString(stringArrayToString(line), writePos + margin,
						heightPos);
				heightPos += lineHeight("line", italicFont);
			}
		}

		if (!description.equals("")) // write description on how to use
		{
			heightPos += lineHeight("text", font);
			description = addInNewLines(description, font, wrappingWidth,
					padding);
			String[][] splitText = spliteOnNewLineAndSpace(description);
			graphs.setFont(font);

			for (int y = 0; y < splitText.length; y++) {
				String[] line = splitText[y];
				graphs.drawString(stringArrayToString(line), writePos,
						heightPos);
				heightPos += lineHeight("line", font);
			}
		}

		maxWidth = Math.max(lineWidth, maxWidth);
		if (wrappingWidth != 0) // then wrap at the wrappingWidth or maxWidth
			maxWidth = Math.max(maxWidth, wrappingWidth);

		if (boxed) {
			graphs.setColor(Color.BLACK);
			graphs.setStroke(new BasicStroke(padding / 2));
			graphs.drawRect(0, 0, maxWidth - 1, heightPos + padding - 1);
		}

		if(maxWidth < wrappedImage.getWidth()) {
			wrappedImage = wrappedImage.getSubimage(0, 0, maxWidth, heightPos
					+ padding);
		}
		else {
			wrappedImage = wrappedImage.getSubimage(0, 0, wrappedImage.getWidth(), heightPos);
		}
		return copy(wrappedImage);

	}

	/**
	 * Renders a ToggleButton and returns it as a BufferedImage. ToggleButton
	 * does not wrap unless indicated to do so with \n. Also if two names are used
	 * the second name appears at an offset. And since this is a togglebutton a
	 * box and possible check mark in the box are added
	 * @param text is the text of the togglebutton
	 * @param text2 is the second text of the toggle button - added on a
	 *            secondline and indented
	 * @param party is the party of the candidate in the toggle button - right
	 *            aligned on first line of button
	 * @param fontsize the size of the font
	 * @param wrappingWidth is not used
	 * @param bold whether the button is bold
	 * @param selected is whether or not the toggleButton should have a check
	 *            mark in its box
	 * @return the rendered ToggleButton
	 */
	public static BufferedImage renderToggleButton(String text, String text2,
			String party, int fontsize, int wrappingWidth, boolean bold,
			boolean selected) {

		Font font = new Font(FONT_NAME, (bold) ? Font.BOLD : Font.PLAIN,
				fontsize);

		String box = "\u25a1"; // box character
		String filledSelection = "\u2713"; // check mark character

		BufferedImage wrappedImage = new BufferedImage(1000, 1000,
				BufferedImage.TYPE_INT_ARGB);

		Graphics2D graphs = wrappedImage.createGraphics();
		graphs.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		graphs.setFont(font);

		graphs.setColor(Color.BLACK); // Could make this a variable

		int baseline = graphs.getFontMetrics().getAscent();

		int padding = 10;
		int heightPos = padding + baseline;
		int writePos = padding;

		int lineWidth = padding;
        int boxPos = wrappingWidth - 30;
        int partyLength = lineWidth(party.split(" "), font);

        int partyPos = boxPos - 10 - partyLength;



		graphs.drawString(text, writePos, heightPos);
        graphs.drawString(box, boxPos, heightPos);
		if (selected) {
            Font checkFont = new Font(FONT_NAME, Font.PLAIN,
                    (int) (fontsize - 4 + ((fontsize - 4) * 1.1)));
			graphs.setColor(new Color(0, 165, 80));
			graphs.setFont(checkFont);

			graphs.drawString(filledSelection, boxPos, heightPos);
			graphs.setFont(font);
			graphs.setColor(Color.BLACK);
		}
		if (!party.equals("")) {

			graphs.drawString(party, partyPos, heightPos);
		}

		if (!text2.equals("")) {
			heightPos += lineHeight(text, font);
			graphs.drawString("        " + text2, writePos, heightPos);
		}

		graphs.setColor(Color.BLACK);
		graphs.setStroke(new BasicStroke(padding / 2));
		// start this rectangle off the top of our visible area so we don't see
		// the top border
		graphs.drawRect(0, -padding, wrappingWidth - 1, heightPos + 2*padding - 1);

		wrappedImage = wrappedImage.getSubimage(0, 0, wrappingWidth, heightPos
				+ padding);

		return copy(wrappedImage);
	}

    public static BufferedImage renderPrintButton(String uid, String text, String text2,
                                                  String party, int fontsize, int wrappingWidth, boolean bold,
                                                  boolean selected) {

        //System.out.println("Image with UID: "+ uid + " Text1: " + text + " has wrapping width " + wrappingWidth);
        //System.out.println("Wrapping Width:\tUID:\tText1:");
        //System.out.println(wrappingWidth + "\t" + uid + "\t" + text);
        /* This is never true.
        if (wrappingWidth == 600 && !uid.contains("_printable"))
            System.out.println(">>>>>>>> " + uid);
        */
        fontsize *= DPI_SCALE_FACTOR;
        Font font = new Font(FONT_NAME, (bold) ? Font.BOLD : Font.PLAIN,
                fontsize);

        if(text.equals("NO SELECTION")){
            selected = false;
        }

        //Scale the wrapping width
        wrappingWidth *= DPI_SCALE_FACTOR;




        Font nf = new Font("OCR A Extended", Font.PLAIN, 12*DPI_SCALE_FACTOR);


        String box = "\u25a1"; // box character
        String filledSelection = "\u25a8"; // filled box character

        BufferedImage wrappedImage = new BufferedImage(1000*DPI_SCALE_FACTOR, 1000*DPI_SCALE_FACTOR,
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphs = wrappedImage.createGraphics();

        graphs.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);


        graphs.scale(1.0/DPI_SCALE_FACTOR, 1.0/DPI_SCALE_FACTOR);


        graphs.setFont(nf);
        graphs.setColor(Color.BLACK); // Could make this a variable


        int baseline = graphs.getFontMetrics().getAscent()*DPI_SCALE_FACTOR;

        /* Useful data attributes. */
        int padding = 10*DPI_SCALE_FACTOR;

        int presidentNameLength = lineWidth(text.split("$"), nf);
        int vicePresidentNameLength = lineWidth(text2.split("$"), nf);

        int heightPos = padding + baseline;
        int writePos = padding;
        int boxPos = wrappingWidth - SELECTION_BOX_WIDTH - DPI_SCALE_FACTOR;
        int candidateNameEndPos = boxPos - 2*DPI_SCALE_FACTOR;


        String selection = "";
        int selectionLength = 0;
        int drawPosition = 0;

        if (!text2.equals("")) {   // If the selection represents a Presidential election.
            if (!party.equals("")) {
                selection = text2 + " - " + party;
                selectionLength = lineWidth(selection.split("$"), nf);
            }

            graphs.drawString(text, candidateNameEndPos - (selectionLength - vicePresidentNameLength) - presidentNameLength, heightPos);
            heightPos += lineHeight(text, nf);
        }
        else {
            if (!party.equals("")) {
                selection += text +  " - " + party;
            }
            else{
                selection = text;
            }

        }
        graphs.setFont(nf);

        //If this is a race name and not a candidate
        if (uid.contains("L"))
        {
            wrappingWidth = 400*DPI_SCALE_FACTOR;
            Font temp = font.deriveFont(12.0f*DPI_SCALE_FACTOR);
            String[] split = selection.split("\n");
            text = split[0];

            if(split.length > 1) //if there is a newline character, there are two titles
                text2 = split[1];

            selectionLength = lineWidth(selection.split("$"), font);

            graphs.setFont(temp);



            if (!text2.equals("")) {   // If the selection represents a Presidential election.

                graphs.setFont(temp);
                graphs.drawString(text + ":", padding, heightPos);
                heightPos += lineHeight(text, font)*DPI_SCALE_FACTOR;
                selection = text2;

            }

            graphs.drawString(selection, padding, heightPos); //height based on an appropriate spacing of up to a 3 digit number
            graphs.setFont(font);

            wrappedImage = wrappedImage.getSubimage(0, 0, Math.max(wrappingWidth,selectionLength), 2 * heightPos);



            //Trim the image.
            wrappedImage = PrintImageUtils.trimImageVertically(wrappedImage, false, Integer.MAX_VALUE); // Above
            wrappedImage = PrintImageUtils.trimImageVertically(wrappedImage, true, Integer.MAX_VALUE); // Below
            // No Left/Right trimming, because it is done in the Printer class.


//            wrappedImage = PrintImageUtils.getScaledInstance(wrappedImage, wrappedImage.getWidth()/DPI_SCALE_FACTOR,
//                    wrappedImage.getHeight()/DPI_SCALE_FACTOR, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
//
//            System.out.println("Scaling the trimmed image");
//            Graphics2D scaleG = (Graphics2D)wrappedImage.getGraphics();
//            scaleG.scale(1.0/DPI_SCALE_FACTOR, 1.0/DPI_SCALE_FACTOR);


            return copy(wrappedImage);

        }


        //Get rid of all underscores and letters in UID's
        if(uid.contains("_"))
            uid = uid.substring(1, uid.indexOf("_"));
        else
            uid = uid.substring(1);

        graphs.drawString(uid, writePos, heightPos);



        /* This is where the box is being drawn. */
        drawBox(graphs, boxPos, (heightPos - SELECTION_BOX_HEIGHT), SELECTION_BOX_WIDTH, SELECTION_BOX_HEIGHT, selected);


        /*Font boxFont = new Font(font.getName(), font.getStyle(), font.getSize() + 20);
        graphs.setFont(boxFont);
        graphs.drawString(box, boxPos, heightPos);

        if (selected) {

            graphs.drawString(filledSelection, boxPos, heightPos);

        }*/



        graphs.setFont(nf);
        drawPosition = Math.max(0,  candidateNameEndPos - lineWidth(selection.split("$"), nf));
        graphs.drawString(selection, drawPosition, heightPos); //height based on an appropriate spacing of up to a 3 digit number


        graphs.setColor(Color.BLACK);
        graphs.setStroke(new BasicStroke(padding / 4));

        //split "1" because it gives a nice line width. It's sort of a hack...
        graphs.drawLine(writePos + lineWidth(("1").split(""), nf), heightPos + fontsize/2, Math.max(wrappingWidth,selectionLength), heightPos + fontsize/2);

        //This should automatically trim the image, sadly, it doesn't
        wrappedImage = wrappedImage.getSubimage(0, 0, Math.max(wrappingWidth,selectionLength), heightPos + padding);



        //Need to also remove whitespace above and below the image.
        wrappedImage = PrintImageUtils.trimImageVertically(wrappedImage, false, Integer.MAX_VALUE);
        wrappedImage = PrintImageUtils.trimImageVertically(wrappedImage, true, Integer.MAX_VALUE); // Below


//
//        System.out.println("Scaling the trimmed image");
//        Graphics2D scaleG = (Graphics2D)wrappedImage.getGraphics();
//        scaleG.scale(1.0/DPI_SCALE_FACTOR, 1.0/DPI_SCALE_FACTOR);
//
//        wrappedImage = PrintImageUtils.getScaledInstance(wrappedImage, wrappedImage.getWidth()/DPI_SCALE_FACTOR,
//            wrappedImage.getHeight()/DPI_SCALE_FACTOR, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);

        return copy(wrappedImage);
    }

	/**
	 * A private helper to add in tags of where new lines should be added when a
	 * text is rendered with at given font with a set wrappingWidth and padding
	 * @param text the text to be rendered
	 * @param font the font to render with
	 * @param wrappingWidth the width at which to wrap
	 * @param padding the padding that should be on the text
	 * @return the text with appropriate <newline> tags added in
	 */
	private static String addInNewLines(String text, Font font,
			int wrappingWidth, int padding) {
		String copy = new String("");
		String[] splitText = text.split(" ");
		int currentLineWidth = padding;
		for (String word : splitText) {
			Rectangle2D measurement = font.getStringBounds(word + " ",
					new FontRenderContext(new AffineTransform(), true, true));
			currentLineWidth += measurement.getWidth();

			if (currentLineWidth + padding > wrappingWidth) {
				currentLineWidth = (int) measurement.getWidth() + padding;
				copy = copy.concat(" <newline>");
			}
			copy = copy.concat(word + " ");
		}
		return copy;
	}

	/**
	 * Calculates the line height at a given font, by looking at the height of
	 * the first word
	 * @param line is the line
	 * @param font is the font
	 * @return the height
	 */
	private static int lineHeight(String line, Font font) {
		Rectangle2D measurement = font.getStringBounds(line + " ",
				new FontRenderContext(new AffineTransform(), true, true));
		return (int) measurement.getHeight();

	}

	/**
	 * Calculates the line width at a given font
	 * @param line is the line
	 * @param font is the font
	 * @return the width
	 */
	private static int lineWidth(String[] line, Font font) {
		int width = 0;
		for (String word : line) {
			Rectangle2D measurement = font.getStringBounds(word + " ",
					new FontRenderContext(new AffineTransform(), true, true));
			width += measurement.getWidth();
		}

		return width;
	}

	/**
	 * Splits text on new line and then on white space
	 * @param text the text to be split
	 * @return the split text
	 */
	private static String[][] spliteOnNewLineAndSpace(String text) {

		String[] splitOnNewLine = text.split("<newline>");
		String[][] splitText = new String[splitOnNewLine.length][0];
		for (int x = 0; x < splitOnNewLine.length; x++) {
			splitText[x] = splitOnNewLine[x].split(" ");
		}
		return splitText;
	}

	/**
	 * Transforms an array of Strings to one string w/ appropriate spacing added
	 * in between. Also trims the string to remove useless white sapce
	 * @param array the array to be transformed
	 * @return the array as a string
	 */
	private static String stringArrayToString(String[] array) {
		String currentString = new String(" ");
		for (int x = 0; x < array.length; x++) {
			currentString = currentString.concat(array[x] + " ");
		}
		return currentString.trim();

	}

    /**
     * This method draws a box in a given context, with the upper left corner at the location given.
     * @param graphicsObject - the context (graphics object) on which to draw the box
     * @param upperLeftX - the X coordinate of the upper-left corner
     * @param upperLeftY - the Y coordinate of the upper-left corner
     * @param width - the width of the box
     * @param height - the height of the box
     * @param selected - whether or not the box should be filled in
     */
    public static void drawBox(Graphics2D graphicsObject, int upperLeftX, int upperLeftY, int width, int height, Boolean selected)
    {
        // Drawing the empty box.
        graphicsObject.drawRect(upperLeftX, upperLeftY, width, height);


        if (selected)
        {
            ArrayList<Integer> startXs = new ArrayList<Integer> ();
            ArrayList<Integer> startYs = new ArrayList<Integer> ();
            ArrayList<Integer> endXs = new ArrayList<Integer> ();
            ArrayList<Integer> endYs = new ArrayList<Integer> ();

            // Building the list of start positions for the fill lines.
            int offsetX = 0;
            int offsetY = 0;
            while (offsetX < width)
            {
                startXs.add(new Integer(upperLeftX+offsetX));
                startYs.add(new Integer(upperLeftY+offsetY));
                offsetX += 5;
            }
            while (offsetY < height)
            {
                startXs.add(new Integer(upperLeftX+offsetX));
                startYs.add(new Integer(upperLeftY+offsetY));
                offsetY += 5;
            }

            // Building the list of end positions for the fill lines.
            offsetX = 0;
            offsetY = 0;
            while (offsetY < height)
            {
                endXs.add(new Integer(upperLeftX+offsetX));
                endYs.add(new Integer(upperLeftY+offsetY));
                offsetY += 5;
            }
            while (offsetX < width)
            {
                endXs.add(new Integer(upperLeftX+offsetX));
                endYs.add(new Integer(upperLeftY+offsetY));
                offsetX += 5;
            }

            // Drawing the fill lines.
            for (int i = 0; i < startXs.size(); i++)
            {
                graphicsObject.drawLine(startXs.get(i), startYs.get(i), endXs.get(i), endYs.get(i));
            }
        }
    }

}
