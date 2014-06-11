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
 *
 * @author Corey Shaw, Ted Torous, Matt Bernhard, Mircea Berechet
 */
public class RenderingUtils {

	/** Max width for buttons. Longer text will be clipped. */
	public static final int MAX_BUTTON_WIDTH = 600;

    /** Max height for buttons. Taller text will be clipped. */
	public static final int MAX_BUTTON_HEIGHT = 100;

    /**
     * Scaling factor for high dpi printed images
     * Choose this value based on a 300 dpi printer and the fact that java's default is 72 dpi
     */
    public static final int DPI_SCALE_FACTOR = Math.round(1.0f*300/72);

    /** The width of the selection box */
    public static final int SELECTION_BOX_WIDTH = 15*DPI_SCALE_FACTOR;

    /** The height of the selection box */
    public static final int SELECTION_BOX_HEIGHT = 10*DPI_SCALE_FACTOR;

	/** The standard font to use */
	public static final String FONT_NAME = "Arial Unicode";

	/** A dummy 1x1 image used for getting the sizes of components */
	private static final BufferedImage DUMMY_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

	/**
	 * Copies a buffered Image. Borrowed 100% from
	 * http://cui.unige.ch/~deriazm/javasources/ImgTools.java I really can't
	 * think of a better way of writing this code - so i just used theirs
	 */
	public static BufferedImage copy(BufferedImage bImage) {
		int w = bImage.getWidth(null);
		int h = bImage.getHeight(null);
		BufferedImage bImage2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = bImage2.createGraphics();
		g2.drawImage(bImage, 0, 0, null);
		return bImage2;
	}

	/**
	 * Calculates the size of a button.<br>
	 * Note: Buttons do not automatically wrap - must be wrapped with a \n in the
	 * text of the button
     *
	 * @param text          the text of the button
	 * @param fontsize      the size of the font to use
	 * @param bold          whether the button has bold text
	 * @return              the size of the Button
	 */
	public static Dimension getButtonSize(String text, int fontsize, boolean bold) {

        /* This is the standard font we use, so we can size the button with respect to the text */
		Font font = new Font(FONT_NAME, (bold) ? Font.BOLD : Font.PLAIN, fontsize);

        /* This is a dummy image that we will extract graphics from to size the button */
		BufferedImage wrappedImage = DUMMY_IMAGE;

        /* Get the graphics off our dummy image and set is font and aliasing information */
		Graphics2D graphs = wrappedImage.createGraphics();
		graphs.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphs.setFont(font);

        /* Find the baseline for the text that will be drawn on the button */
		int baseline = graphs.getFontMetrics().getAscent();

        /* Split the input string into words, so we can keep consistent spacing and wrap text as necessary */
		String[] words = text.split(" ");

        /* This is how much whitespace there will be between the text and the edge of the button */
		int padding = 10;

        /* This is the location of the top side of the bounding box for the text */
		int heightPos = padding + baseline;

        /* This is the starting horizontal coordinate of the text's bounding box */
		int lineWidth = padding;

        /* The width of the widest line */
		int maxWidth = 0;

        /* For each word try placing it on the line. If it won't fit, jump down a line and then write it */
		for (String word : words) {

            /* Get the bounding box for this word */
			Rectangle2D measurement = font.getStringBounds(word + " ", new FontRenderContext(null, true, true));

            /* Extract size information from the box */
			int wordWidth = (int) measurement.getWidth();
			int wordHeight = (int) measurement.getHeight();

            /* Increment the width of the line with this word */
			lineWidth += wordWidth;

            /* If the text contains a newline, wrap to the next line */
			if (word.equals("\n")) {

                /* Since we've hit the end of a line, update the max line width */
				maxWidth = Math.max(lineWidth, maxWidth);

                /* Update the position information */
				heightPos += wordHeight;
				lineWidth = padding;
			}
		}

        /* Update the max line width so we know how wide the button is */
		maxWidth = Math.max(lineWidth, maxWidth);

		return new Dimension(maxWidth, heightPos + padding);
	}

	/**
	 * Calculates the size of a label.
     *
	 * @param title                 is the text to be outputted
	 * @param instructions          are any instructions to be italicized, such as '(please select one)'
	 * @param description           are any descriptions (such as those used on propositions)
	 * @param fontsize              the size of the font
	 * @param wrappingWidth         is the width at which the label should wrap
	 * @param bold                  whether the label is bold
	 * @param titleCentered         is a boolean flag as to whether or not the text should be centered
	 * @return                      the size of the
	 */
	public static Dimension getLabelSize(String title,      String instructions, String description, int fontsize,
                                         int wrappingWidth, boolean bold,        boolean titleCentered) {

        /* This is the standard font we use, so we can size the button with respect to the text */
        Font font = new Font(FONT_NAME, (bold) ? Font.BOLD : Font.PLAIN, fontsize);

        /* Also get the italicized and bold versions of our font, since labels can have these properties */
		Font italicFont = new Font(FONT_NAME, Font.ITALIC, fontsize);
		Font bigBoldFont = new Font(FONT_NAME, Font.BOLD, fontsize + 4);

        /* This is a dummy image that we will extract graphics from to size the button */
		BufferedImage wrappedImage = DUMMY_IMAGE;

        /* Get the graphics off our dummy image and set is font and aliasing information */
		Graphics2D graphs = wrappedImage.createGraphics();
		graphs.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphs.setFont(font);

        /* Find the baseline for the text that will be drawn on the button */
        int baseline = graphs.getFontMetrics().getAscent();

        /* Split the input string into words, so we can keep consistent spacing and wrap text as necessary */
        String[] titleWords = title.split(" ");

        /* This is how much whitespace there will be between the text and the edge of the button */
        int padding = 10;

        /* This is the upper-right hand corner of the bounding box for the text */
        int heightPos = padding + baseline;

        /* This is the starting horizontal coordinate of the text's bounding box */
        int lineWidth = padding;

        /* The width of the widest line */
        int maxWidth = 0;

        /* If the title is centered, it will handle line splits differently */
		if (titleCentered) {

            /* Centered titles will have bold font */
			graphs.setFont(bigBoldFont);

            /* Split each line into its own array, grouping words by line */
			String[][] splitText = spliteOnNewLineAndSpace(title);

            /* Iterated through each line and increment the height of the label by each line's height */
            for (String[] ignored : splitText)
                heightPos += lineHeight("line", bigBoldFont);

		}

        /* If the title isn't centered, we can just calculate its size based on bounding boxes */
        else {

            /* For each word try placing it on the line. If it won't fit, jump down a line and then write it */
            for (String word : titleWords) {

                /* Get the bounding box for this word */
                Rectangle2D measurement = font.getStringBounds(word + " ", new FontRenderContext(new AffineTransform(), true, true));

				/* Extract size information from the box */
                int wordWidth = (int) measurement.getWidth();
                int wordHeight = (int) measurement.getHeight();

                /* Increment the width of the line with this word */
                lineWidth += wordWidth;

                /* If the text contains a newline, wrap to the next line */
                if (word.equals("\n") || word.equals("<newline>")) {

                    /* Since we've hit the end of a line, update the max line width */
                    maxWidth = Math.max(lineWidth, maxWidth);

                    /* Update the position information */
                    heightPos += wordHeight;
                    lineWidth = padding;
                }
			}

		}

        /* If instructions are included in the label, find out how large they are */
		if (!instructions.equals("")) {

            /* Split each line into its own array, grouping words by line */
			String[][] splitText = spliteOnNewLineAndSpace(instructions);

            for (String[] ignored : splitText)
                heightPos += lineHeight("line", italicFont);
		}

        /* If a description is included in the label, find out how large it is */
		if (!description.equals("")) {

            /* Add a new line before the description and account for its size */
			heightPos += lineHeight("text", font);
			description = addInNewLines(description, font, wrappingWidth, padding);

			/* Split each line into its own array, grouping words by line */
            String[][] splitText = spliteOnNewLineAndSpace(description);

            for (String[] ignored : splitText)
                heightPos += lineHeight("line", italicFont);
		}

         /* Update the max line width so we know how wide the button is */
		maxWidth = Math.max(lineWidth, maxWidth);

        /* Wrap at the wrappingWidth or maxWidth*/
		if (wrappingWidth != 0)
			maxWidth = Math.max(maxWidth, wrappingWidth);

		return new Dimension(maxWidth, heightPos + padding);

	}

	/**
	 * Calculates the size of a ToggleButton.<br>
	 * ToggleButton does not wrap unless indicated to do so with \n. Also if two
	 * names are used the second name appears at an offset. And since this is a
	 * togglebutton a box and possible check mark in the box are added
     *
	 * @param text                  the text of the togglebutton
	 * @param text2                 the second text of the toggle button - added on a second line and indented
	 * @param party                 the party of the candidate in the toggle button - right aligned on first line of button
	 * @param fontsize              the size of the font
	 * @param wrappingWidth         width of the button
	 * @param bold                  whether the button is bold
	 * @return                      the size of the ToggleButton
	 */
	public static Dimension getToggleButtonSize(String text, String text2, String party, int fontsize, int wrappingWidth, boolean bold) {

        /* This is the standard font we use, so we can size the button with respect to the text */
        Font font = new Font(FONT_NAME, (bold) ? Font.BOLD : Font.PLAIN, fontsize);

        /* This is a dummy image that we will extract graphics from to size the button */
        BufferedImage wrappedImage = DUMMY_IMAGE;

        /* Get the graphics off our dummy image and set is font and aliasing information */
        Graphics2D graphs = wrappedImage.createGraphics();
        graphs.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphs.setFont(font);

        /* Find the baseline for the text that will be drawn on the button */
        int baseline = graphs.getFontMetrics().getAscent();

	    /* This is how much whitespace there will be between the text and the edge of the button */
        int padding = 10;

        /* This is the upper-right hand corner of the bounding box for the text */
        int heightPos = padding + baseline;

        /* This is the starting horizontal coordinate of the text's bounding box */
        int lineWidth = padding;

        /* The width of the widest line */
        int maxWidth = 0;

        /* If this button has secondary text (i.e. it's a presidential race) account for it */
        if (!text2.equals(""))
			heightPos += lineHeight(text, font);

        /* Update the max line width so we know how wide the button is */
        maxWidth = Math.max(lineWidth, maxWidth);

        return new Dimension(maxWidth, heightPos + padding);
	}

	/**
	 * Renders a Button and returns it as a BufferedImage.<br>
	 * Buttons do not automatically wrap - must be wrapped with a \n in the text
	 * of the button
     *
	 * @param text                  is the text of the button
	 * @param fontsize              is the size of the font
	 * @param bold                  is whether the button is bold
	 * @param boxed                 whether the button is boxed
	 * @param backGroundColor       is the background color of the button
	 * @param preferredWidth        desired width of the button, honored if possible
	 * @return                      the rendered Button
	 */
	public static BufferedImage renderButton(String text, int fontsize, boolean bold, boolean boxed, int preferredWidth, Color backGroundColor, boolean focused) {

        /* This is the standard font we use, so we can size the button with respect to the text */
        Font font = new Font(FONT_NAME, (bold) ? Font.BOLD : Font.PLAIN, fontsize);

        /* This is the image that we will draw the button on to */
		BufferedImage wrappedImage = new BufferedImage(MAX_BUTTON_WIDTH, MAX_BUTTON_HEIGHT, BufferedImage.TYPE_INT_RGB);

        /* Get the graphics off our image and set is font and aliasing information */
        Graphics2D graphs = wrappedImage.createGraphics();
		graphs.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphs.setFont(font);

        /* Set the background color based on whether the button is focused or not */
        if(focused) graphs.setPaint(Color.ORANGE);
        else  graphs.setPaint(backGroundColor);

        /* Draw in the background */
        graphs.fillRect(0, 0, 330, MAX_BUTTON_HEIGHT);

        /* Set the color to black for drawing the button's text */
		graphs.setColor(Color.BLACK);

        /* Find the baseline for the text that will be drawn on the button */
		int baseline = graphs.getFontMetrics().getAscent();

	    /* Split the input string into words, so we can keep consistent spacing and wrap text as necessary */
        String[] words = text.split(" ");

        /* This is how much whitespace there will be between the text and the edge of the button */
        int padding = 10;

        /* This is used for vertical spacing between lines  */
		int leading = 1;

        /* This is the location of the top side of the bounding box for the text */
		int heightPos = padding + baseline;

        /* This is where the writing of the string will start */
		int writePos;

        /* This is a dummy width to initialize the linewidth */
		int lineWidth = padding;

		/* The width of the widest line */
        int maxWidth = 0;

        /* For each word try placing it on the line. If it won't fit, jump down a line and then write it */
        for (String word : words) {

            /* Account for the spaces between the words */
            String spacedWord = word + " ";

            /* Calculate the width of the string. Note we split it to put it in a one element array */
            int wordWidth = lineWidth(spacedWord.split("$"), font);

            /* Update the position of the write head */
			writePos = lineWidth;

            /* Udate the line width with each new word */
			lineWidth += wordWidth;

			/* If the text contains a newline, wrap to the next line */
            if (word.equals("\n")) {

                /* Since we've hit the end of a line, update the max line width */
                maxWidth = Math.max(lineWidth, maxWidth);

                /* Update the position information */
				heightPos += baseline + leading;
				writePos = padding;
				lineWidth = padding;
			}

            /* Draw the string, with a space, on the button */
			graphs.drawString(word + " ", writePos, heightPos);

		}

        /* Update the max line width so we know how wide the button is */
		maxWidth = Math.max(lineWidth, maxWidth);

        /* Set a preferred width to override the max width */
		if (preferredWidth > 0) maxWidth = preferredWidth;

        /* If the button should have a box drawn around it, do so */
		if (boxed) {
			graphs.setColor(Color.BLACK);
			graphs.setStroke(new BasicStroke(padding / 2));
			graphs.drawRect(0, 0, maxWidth - 1, heightPos + padding - 1);
		}

		/* Cut the image down to the correct size */
		wrappedImage = wrappedImage.getSubimage(0, 0, maxWidth, heightPos + padding);

        /* I think this copies to avoid aliasing or something. TODO Figure this out */
		return copy(wrappedImage);
	}

	/**
	 * Renders a label and returns it as a BufferedImage.
     *
	 * @param title                 is the text to be outputted
	 * @param instructions          are any instructions to be italicized, such as '(please select one)'
	 * @param description           are any descriptions (such as those used on propositions)
	 * @param fontsize              the size of the font
	 * @param wrappingWidth         is the width at which the label should wrap
	 * @param bold                  whether the label is bold
	 * @param color                 is the color of the text
	 * @param boxed                 is a boolean flag to determine whether or a box should be placed around the label
	 * @param titleCentered         is a boolean flag as to whether or not the text should be centered
	 * @return                      the rendered
	 */
	public static BufferedImage renderLabel(String title, String instructions, String description, int fontsize,          int wrappingWidth,
                                            Color color,  boolean bold,        boolean boxed,      boolean titleCentered, boolean focused) {

        /* This is the standard font we use, so we can size the button with respect to the text */
        Font font = new Font(FONT_NAME, (bold) ? Font.BOLD : Font.PLAIN, fontsize);

        /* Also get the italicized and bold versions of our font, since labels can have these properties */
        Font italicFont = new Font(FONT_NAME, Font.ITALIC, fontsize);
        Font bigBoldFont = new Font(FONT_NAME, Font.BOLD, fontsize + 4);

		BufferedImage wrappedImage = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);

	    /* Get the graphics off our image and set its aliasing information */
        Graphics2D graphs = wrappedImage.createGraphics();
        graphs.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        /* Set and draw the background color if the button is focused */
        if(focused){
            graphs.setColor(Color.ORANGE);
            graphs.fillRect(0, 0, wrappedImage.getWidth(), wrappedImage.getHeight());
        }

        /* Set the font and specified color for the label */
        graphs.setFont(font);
        graphs.setColor(color);

        /* Find the baseline for the text that will be drawn on the button */
        int baseline = graphs.getFontMetrics().getAscent();

	    /* Split the input string into words, so we can keep consistent spacing and wrap text as necessary */
        String[] titleWords = title.split(" ");

        /* This is how much whitespace there will be between the text and the edge of the button */
        int padding = 10;

        /* This is the location of the top side of the bounding box for the text */
		int heightPos = padding + baseline;

        /* This is where the writing of the string will start */
		int writePos = padding;

        /* This is a dummy width to initialize the linewidth */
        int lineWidth = padding;

		/* The width of the widest line */
        int maxWidth = 0;


	    /* If the title is centered, it will handle line splits differently */
        if (titleCentered) {

            /* Centered titles will have bold font */
            graphs.setFont(bigBoldFont);

            /* Split each line into its own array, grouping words by line */
            String[][] splitText = spliteOnNewLineAndSpace(title);

            /* Iterated through each line and draw it */
            for (String[] text : splitText) {

                /* Calculate the margin */
				int margin = (wrappingWidth - 2 * padding - lineWidth(text, bigBoldFont)) / 2;

                /* Draw the string, accounting for the margin */
				graphs.drawString(stringArrayToString(text), writePos + margin, heightPos);

                /* Update the height of the label */
				heightPos += lineHeight("line", bigBoldFont);
			}
		}

        /* If the title isn't centered, we can just draw it based on bounding boxes */
        else {

		    /* For each word try placing it on the line. If it won't fit, jump down a line and then write it */
            for (String word : titleWords) {

                /* Get the bounding box for this word */
                Rectangle2D measurement = font.getStringBounds(word + " ", new FontRenderContext(new AffineTransform(), true, true));

                /* Extract size information from the box */
                int wordWidth = (int) measurement.getWidth();
                int wordHeight = (int) measurement.getHeight();

                /* Update the write head position and the width of the line */
				writePos = lineWidth;
				lineWidth += wordWidth;

                /* If the width of our word is longer than the entire line space, break it up. */
                if(wordWidth > wrappedImage.getWidth()) {

                    /* This will hold the left overs from splitting the lines */
                    String remainingStr = word;

                    /* As long as the string is too long, draw out wrappingWidth increments of it */
                    while(remainingStr.length() > wrappedImage.getWidth()) {
                        graphs.drawString(remainingStr.substring(0, wrappedImage.getWidth()),  writePos, heightPos);

                        /* Compute the leftovers */
                        remainingStr = remainingStr.substring(wrappedImage.getWidth());

                        /* We've just written one whole line. put our position variables back at the beginning */
                        heightPos+= wordHeight;
                        writePos= padding;
                        lineWidth= padding;
                    }

                    /* TODO Test this */
                    /* Set up the leftovers to be drawn out below */
                    word = remainingStr;

                }

                /* If the text contains a newline, wrap to the next line */
                else if (word.equals("\n") || word.equals("<newline>")) {
                    /* Since we've hit the end of a line, update the max line width */
					maxWidth = Math.max(lineWidth, maxWidth);

                    /* Update the position information */
					heightPos += wordHeight;
					writePos = padding;
					lineWidth = padding;
				}

                /* Draw the the string */
				graphs.drawString(word + " ", writePos, heightPos);
			}

		}

        /* If instructions are included in the label, draw them  */
		if (!instructions.equals("")) {

		    /* Split each line into its own array, grouping words by line */
            String[][] splitText = spliteOnNewLineAndSpace(instructions);

              /* Iterated through each line and draw it */
            for (String[] text : splitText) {

                /* Calculate the margin */
                int margin = (wrappingWidth - 2 * padding - lineWidth(text, bigBoldFont)) / 2;

                /* Instructions are italicized */
                graphs.setFont(italicFont);

				/* Draw the string, accounting for the margin */
                graphs.drawString(stringArrayToString(text), writePos + margin, heightPos);

                /* Update the height of the label */
                heightPos += lineHeight("line", italicFont);
			}
		}

		     /* If a description is included in the label, draw it */
        if (!description.equals("")) {

		    /* Add a new line before the description and account for its size */
            heightPos += lineHeight("text", font);
            description = addInNewLines(description, font, wrappingWidth, padding);

            /* Split each line into its own array, grouping words by line */
            String[][] splitText = spliteOnNewLineAndSpace(description);

            /* Set the font to normal */
            graphs.setFont(font);

            /* Now iterate over the text and draw each line, updating the height */
            for (String[] text : splitText) {
				graphs.drawString(stringArrayToString(text), writePos,	heightPos);
				heightPos += lineHeight("line", font);
			}
		}

        /* Update the max line width so we know how wide the button is */
        maxWidth = Math.max(lineWidth, maxWidth);

        /* Wrap at the wrappingWidth or maxWidth */
		maxWidth = Math.max(maxWidth, wrappingWidth);

        /* If the label  should have a box drawn around it, do so */
		if (boxed) {
			graphs.setColor(Color.BLACK);
			graphs.setStroke(new BasicStroke(padding / 2));
			graphs.drawRect(0, 0, maxWidth - 1, heightPos + padding - 1);
		}

        /* Cut the image down to the correct size */
        int wrapWidth = Math.min(maxWidth, wrappedImage.getWidth());
	    wrappedImage = wrappedImage.getSubimage(0, 0, wrapWidth, heightPos + padding);

        /* I think this copies to avoid aliasing or something. TODO Figure this out */
		return copy(wrappedImage);

	}

	/**
	 * Renders a ToggleButton and returns it as a BufferedImage. ToggleButton
	 * does not wrap unless indicated to do so with \n. Also if two names are used
	 * the second name appears at an offset. And since this is a ToggleButton a
	 * box and possible check mark in the box are added
     *
	 * @param text                  is the text of the ToggleButton
	 * @param text2                 is the second text of the toggle button added on a second line and indented
	 * @param party                 is the party of the candidate in the toggle button, right aligned on first line of button
	 * @param fontsize              the size of the font
	 * @param wrappingWidth         is not used
	 * @param bold                  whether the button is bold
	 * @param selected              is whether or not the toggleButton should have a check mark in its box
	 * @return                      the rendered ToggleButton
	 */
	public static BufferedImage renderToggleButton(String text, String text2, String party, int fontsize, int wrappingWidth, boolean bold, boolean selected, boolean focused) {

		/* This is the standard font we use, so we can size the button with respect to the text */
        Font font = new Font(FONT_NAME, (bold) ? Font.BOLD : Font.PLAIN, fontsize);

        /* box character */
        String box = "\u25a1";

        /* check mark character */
		String filledSelection = "\u2713";

        /* This is the image that we will draw the button on to */
		BufferedImage wrappedImage = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);

        /* Get the graphics off our image and set is font and aliasing information */
		Graphics2D graphs = wrappedImage.createGraphics();
		graphs.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphs.setFont(font);

         /* Find the baseline for the text that will be drawn on the button */
		int baseline = graphs.getFontMetrics().getAscent();

        /* This is how much whitespace there will be between the text and the edge of the button */
        int padding = 10;

        /* This is the location of the top side of the bounding box for the text */
		int heightPos = padding + baseline;

        /* This is where the writing of the string will start */
		int writePos = padding;

        /* The positioning of the check box on every button, right justified */
        int boxPos = wrappingWidth - 30;

        /* The length of the party to be displayed for this button */
        int partyLength = lineWidth(party.split(" "), font);

        /* The position of the party text, right justified next to the checkbox */
        int partyPos = boxPos - 10 - partyLength;

        /* Set and draw the background color if the button is focused */
        if(focused){
            graphs.setColor(Color.ORANGE);
            graphs.fillRect(0, 0, wrappedImage.getWidth(), wrappedImage.getHeight());
        }

        /* Set the font color to black for drawing text */
        graphs.setColor(Color.BLACK);

        /* Draw the text for the button */
		graphs.drawString(text, writePos, heightPos);

        /* Draw the checkbox on the button */
        graphs.drawString(box, boxPos, heightPos);

        /* If the button is selected, fill the checkbox with a green check */
		if (selected) {

            /* Set the check's font */
            Font checkFont = new Font(FONT_NAME, Font.PLAIN, (int) (fontsize - 4 + ((fontsize - 4) * 1.1)));
			graphs.setFont(checkFont);

            /* Set the color of the check to green */
            graphs.setColor(new Color(0, 165, 80));

            /* Draw the box and reset the font and draw colors */
			graphs.drawString(filledSelection, boxPos, heightPos);
			graphs.setFont(font);
			graphs.setColor(Color.BLACK);
		}

        /* If there is a party, draw it on */
		if (!party.equals(""))
			graphs.drawString(party, partyPos, heightPos);

        /* If there is secondary text, i.e. this is a presidential race, draw it */
		if (!text2.equals("")) {
            /* Update the height to account for the second name */
			heightPos += lineHeight(text, font);

            /* Tab over the second candidate's name */
			graphs.drawString("        " + text2, writePos, heightPos);
		}

        /* Ensure the font color is black, and then set a thicker stroke to border the button */
		graphs.setColor(Color.BLACK);
		graphs.setStroke(new BasicStroke(padding / 2));

		/* start this rectangle off the top of our visible area so we don't see the top border */
		graphs.drawRect(0, -padding, wrappingWidth - 1, heightPos + 2*padding - 1);

        /* Wrap the image to the correct size */
		wrappedImage = wrappedImage.getSubimage(0, 0, wrappingWidth, heightPos + padding);

		/* I think this copies to avoid aliasing or something. TODO Figure this out */
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
        wrappingWidth += 500; //arbitrary sizes!




        Font nf = new Font("OCR A Extended", Font.PLAIN, 12*DPI_SCALE_FACTOR);


        String box = "\u25a1"; // box character
        String filledSelection = "\u25a8"; // filled box character


        int selectionLength = lineWidth(text.split(""), nf);

        int imageLength = Math.max(1000, selectionLength);



        BufferedImage wrappedImage = new BufferedImage(1000*DPI_SCALE_FACTOR, 1000*DPI_SCALE_FACTOR,
                BufferedImage.TYPE_INT_ARGB);




        Graphics2D graphs = wrappedImage.createGraphics();

        graphs.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);


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
            wrappingWidth = 250*DPI_SCALE_FACTOR + 750; //Arbitrary size!
            Font temp = new Font(font.getFontName(), Font.BOLD, 12*DPI_SCALE_FACTOR); //Changed from: font.deriveFont(12.0f*DPI_SCALE_FACTOR);

            String[] split = selection.split("\n");
            text = split[0];

            if(split.length > 1) //if there is a newline character, there are two titles
                text2 = split[1];

            /**
             * Since the selection is drawn using the 'temp' font, it would make sense to use the
             * 'temp' font when determining selectionLength. The font 'font' was being used before.
             */
            selectionLength = lineWidth(selection.split("$"), temp);

            graphs.setFont(temp);



            if (!text2.equals("")) {   // If the selection represents a Presidential election.


                graphs.drawString(text + ":", padding, heightPos);
                heightPos += lineHeight(text, font);
                selection = text2;

            }

            graphs.drawString(selection, padding, heightPos); //height based on an appropriate spacing of up to a 3 digit number
            graphs.setFont(font);

            /**
             * The formula for the total width of the resulting image should contain padding because the selection text gets drawn
             * at a horizontal displacement of 'padding' from the left margin.
             */
            wrappedImage = wrappedImage.getSubimage(0, 0, Math.max(wrappingWidth,selectionLength) + padding, 2 * heightPos);



            //Trim the image.
            wrappedImage = PrintImageUtils.trimImageVertically(wrappedImage, false, Integer.MAX_VALUE); // Above
            wrappedImage = PrintImageUtils.trimImageVertically(wrappedImage, true, Integer.MAX_VALUE); // Below
            // No Left/Right trimming, because it is done in the Printer class.
            //System.err.println("Image with UID " + uid + ":\n\tWrapping width: " + wrappingWidth + "\n\tText width: " + lineWidth(selection.split("$"), temp));

            return copy(wrappedImage);

        }


        //Get rid of all underscores and letters in UID's
        if(uid.contains("_"))
            uid = uid.substring(1, uid.indexOf("_"));
        else
            uid = uid.substring(1);

        graphs.drawString(uid, writePos, heightPos);



        /* This is where the box is being drawn. */
        drawBox(graphs, boxPos, (heightPos - SELECTION_BOX_HEIGHT), SELECTION_BOX_WIDTH, SELECTION_BOX_HEIGHT, selected, padding/8);


        graphs.setFont(nf);
        drawPosition = Math.max(0,  candidateNameEndPos - lineWidth(selection.split("$"), nf));
        graphs.drawString(selection, drawPosition, heightPos); //height based on an appropriate spacing of up to a 3 digit number


        /*// This bottom line will be instead drawn directly in the HTML file.
        graphs.setColor(Color.BLACK);
        graphs.setStroke(new BasicStroke(padding / 4));

        //split "1" because it gives a nice line width. It's sort of a hack...
        graphs.drawLine(writePos + lineWidth(("1").split(""), nf), heightPos + fontsize/2, wrappingWidth*//*Math.max(wrappingWidth,selectionLength)*//*, heightPos + fontsize/2);*/

        //This should automatically trim the image, sadly, it doesn't
        wrappedImage = wrappedImage.getSubimage(0, 0, wrappingWidth/*Math.max(wrappingWidth,selectionLength)*/, heightPos + padding);



        //Need to also remove whitespace above and below the image.
        wrappedImage = PrintImageUtils.trimImageVertically(wrappedImage, false, Integer.MAX_VALUE);
        wrappedImage = PrintImageUtils.trimImageVertically(wrappedImage, true, Integer.MAX_VALUE); // Below

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
	public static int lineHeight(String line, Font font) {
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
	public static int lineWidth(String[] line, Font font) {
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
     *
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
	 * Transforms an array of Strings to one string with appropriate spacing added
	 * in between. Also trims the string to remove useless white space
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
     * @param thickness - the line thickness of the box
     */
    public static void drawBox(Graphics2D graphicsObject, int upperLeftX, int upperLeftY, int width, int height, Boolean selected, int thickness)
    {

        graphicsObject.setStroke(new BasicStroke(thickness));
        // Drawing the empty box.
        graphicsObject.drawRect(upperLeftX, upperLeftY, width, height);

        if (selected)
        {
            //determines how many lines get drawn in the box
            int denominator = 3;
            float slope = (1.0f*height)/width;

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
                offsetX += width/denominator;
            }
            while (offsetY < height)
            {
                startXs.add(new Integer(upperLeftX+offsetX));
                startYs.add(new Integer(upperLeftY+offsetY));
                offsetY += height/denominator;
            }

            // Building the list of end positions for the fill lines.
            offsetX = 0;
            offsetY = 0;
            while (offsetY < height)
            {
                endXs.add(new Integer(upperLeftX+offsetX));
                endYs.add(new Integer(upperLeftY+offsetY));
                offsetY += height/denominator;
            }
            while (offsetX < width)
            {
                int endX = new Integer(upperLeftX+offsetX);
                int endY = new Integer(upperLeftY+offsetY);

                endXs.add(endX);
                endYs.add(endY);
                offsetX += width/denominator;
            }


            // Drawing the fill lines.
            for (int i = 0; i < startXs.size(); i++)
            {
                int startX = startXs.get(i);
                int startY = startYs.get(i);
                int endX = endXs.get(i);
                int endY = endYs.get(i);

                //Correct for overflow vertically
                if(endY > upperLeftY + height){
                    int newY = upperLeftY + height;
                    int newX = Math.round((endY - newY)/slope) + endX;

                    endX = newX;
                    endY = newY;

                }
                //Correct for overflow horizontally (note that there should never be overflow in both cases)
                else if(endX > upperLeftX + width){
                    int newX = upperLeftX + width;
                    int newY = Math.round((endX - newX)/slope) + endY;

                    endX = newX;
                    endY = newY;
                }

                graphicsObject.drawLine(startX, startY, endX, endY);
            }
        }
    }

    /**
     * Saves images to files, to be used for write-in candidates' names.
     */
    public static void writeLettersToFile (String location, int fontsize)
    {
        // All the characters that are used in English names.
        String ALL_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ -'";
        // The size of the images to be created.
        int CHARACTER_IMAGE_WIDTH = 28;
        int CHARACTER_IMAGE_HEIGHT = 28;

        // Set the font for the character.
        Font font = new Font("Monospaced", Font.PLAIN, fontsize);

        for (int idx = 0; idx < ALL_LETTERS.length(); idx++)
        {
            // Get a character.
            String currentCharacter = ALL_LETTERS.substring(idx, idx+1);
            // Create a new BufferedImage, on which to draw that character.
            BufferedImage currentImage = new BufferedImage(CHARACTER_IMAGE_WIDTH, CHARACTER_IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            // Create a Graphics object for the BufferedImage, to actually draw the String on the image.
            Graphics g = currentImage.getGraphics();
            // Draw a white background.
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, CHARACTER_IMAGE_WIDTH, CHARACTER_IMAGE_HEIGHT);
            // Set the colour to be used in drawing the character.
            g.setColor(Color.BLACK);
            // Set the font for the character.
            g.setFont(font);
            // Draw the character on the image's canvas.
            g.drawString(currentCharacter, 5, CHARACTER_IMAGE_HEIGHT - 5);
            // Create a new file, to write the image to a file.
            // All the file names start with 'W_'.
            // All the file names contain the character name, ending in the extension '.png'.
            // Create the writein directory, if it does not already exist.
            File file = new File(location, "writein");
            if(!file.exists())
            {
                file.mkdirs();
            }
            file = new File(file, "W_" + currentCharacter + ".png");
            // Try to write the image to the file.
            try
            {
                //System.out.println("Attempting to create an image for '" + currentCharacter + "' at: " + file.getAbsolutePath());
                ImageIO.write(currentImage, "png", file);
            }
            catch (IOException e)
            {
                System.out.println("Letter image creation failed!");
                e.printStackTrace();
            }
        }
    }
}
