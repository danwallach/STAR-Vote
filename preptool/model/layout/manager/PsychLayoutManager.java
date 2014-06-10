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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import preptool.model.ballot.*;
import preptool.model.language.Language;
import preptool.model.language.LiteralStrings;
import preptool.model.layout.*;


/**
 * PsychLayoutManager is a concrete implementation of a LayoutManager, as
 * specified by the Psychology department.<br>
 * See the wiki for more details about this layout
 *
 * @author Corey Shaw, Ted Torous, Kyle Derr
 */
public class PsychLayoutManager extends ALayoutManager {

    /** Constant used to indicate how wide the text boxes describing a race are to be drawn */
    private static final int RACE_DESCRIPTION_WIDTH = 600;

    /** Constant to indicate how high presidential labels should be */
    private static final int PRESIDENTIAL_RACE_LABEL_COMPONENT_HEIGHT = 40;

	/** Width of each candidate or contest on the review screen (RenderButton) */
	private static final int REVIEW_SCREEN_WIDTH = 330;

    /** Allows the review screen to show party information */
	private static final Boolean REVIEW_SCREEN_SHOW_PARTY = true;

    /** Allows the review screen to put parentheses around the party info */
	private static final Boolean REVIEW_SCREEN_PARENTHESIZE_PARTY = true;

    /** Dictates the number of columns the review screen will have */
	private static final int REVIEW_SCREEN_NUM_COLUMNS = 1;

    /** Dictates the number of races that can be shown on a review screen */
	private static int CARDS_PER_REVIEW_PAGE = 10;

    /** Constant for the width of the language selection page box */
    private static final int LANG_SELECT_WIDTH = 600;

    /**
     * Extension of the ICardLayout for use by this manager
     */
    public class PsychCardLayout implements ICardLayout {

        /** The title of the card this layout represents */
        private String titleText = "";

        /** The description of the card being laid out */
        private String descriptionText = "";

        /** A list of the candidates on this card */
        private ArrayList<ToggleButton> candidates;

        /**
         * Constructor, simply initializes the list of candidates
         */
        public PsychCardLayout() {
            candidates = new ArrayList<>();
        }

        /**
         * @see preptool.model.layout.manager.ALayoutManager.ICardLayout#addCandidate(String, String)
         */
        public void addCandidate(String uid, String name) {
            ToggleButton tb = new ToggleButton(uid, name);
            candidates.add(tb);
        }

        /**
         * @see preptool.model.layout.manager.ALayoutManager.ICardLayout#addCandidate(String, String, String)
         */
        public void addCandidate(String uid, String name, String party) {
            ToggleButton tb = new ToggleButton(uid, name);
            tb.setParty(party);

            candidates.add(tb);
        }

        /**
         * @see preptool.model.layout.manager.ALayoutManager.ICardLayout#addCandidate(String, String, String, String)
         */
        public void addCandidate(String uid, String name, String name2, String party) {
            ToggleButton tb = new ToggleButton(uid, name);
            tb.setSecondLine(name2);
            tb.setParty(party);

            candidates.add(tb);
        }

        /**
         * @see preptool.model.layout.manager.ALayoutManager.ICardLayout#makeIntoPanels()
         */
        public ArrayList<JPanel> makeIntoPanels() {

            /* Keep track of how many candidates we've added */
            int cnt = 0;

            /* A list of the panels we make, to be returned */
            ArrayList<JPanel> panels = new ArrayList<>();

            /* Create new JPanels for each candidate on this card */
            while (cnt < candidates.size()) {

                /* Create a new panel, and set its layout to GridBag*/
                JPanel panel = new JPanel();
                panel.setLayout(new GridBagLayout());

                /* Layout constraints for the elements on this panel*/
                GridBagConstraints panelConstraints = new GridBagConstraints();

                /* The panel will be anchored to the bottom of the screen and filled vertically*/
                panelConstraints.anchor = GridBagConstraints.SOUTH;
                panelConstraints.fill = GridBagConstraints.VERTICAL;

                /* The coordinate dictating where a candidate will be put in the gridbag (in this case, starting at the top) */
                int ycoord = 0;

                /* Initialize the grid part of gridbag */
                panelConstraints.gridy = ycoord;
                panelConstraints.gridx = 0;

                /* Position the first element 1 unit off the bottom of the panel */
                ycoord++;

                /* Build the title label*/
                Label title = new Label(getNextLayoutUID(), titleText);

                /* Add a description, set the width, box, and center the label */
                title.setDescription(descriptionText);
                title.setWidth(RACE_DESCRIPTION_WIDTH); 
                title.setBoxed(true);
                title.setCentered(true);

                /* If there are more candidates than one page can accommodate, add a label indicating this in the title */
                if (candidates.size() > MAX_CANDIDATES)
                    title.setInstructions("("
                            + LiteralStrings.Singleton.get("PAGE", language)
                            + " "
                            + (cnt / MAX_CANDIDATES + 1)
                            + " "
                            + LiteralStrings.Singleton.get("OF", language)
                            + " "
                            + (int) Math.ceil((double) candidates.size()
                                    / MAX_CANDIDATES) + ")");

                /* Set the size of the title box with its visitor */
                title.setSize(title.execute(sizeVisitor));

                /* Put the title in a spacer, then add the spacer to the panel */
                Spacer PTitle = new Spacer(title, panel);
                panel.add(PTitle, panelConstraints);

                /* Now build a toggle button group for the candidates */
                ToggleButtonGroup tbg = new ToggleButtonGroup("Race");

                /* For every candidate, add a button to the group */
                for (int i = 0; i < MAX_CANDIDATES && cnt < candidates.size(); ++i, ++cnt) {
                    /* Create the button */
                    ToggleButton button = candidates.get(cnt);

                    /* Set up its rendering properties */
                    button.setWidth(RACE_DESCRIPTION_WIDTH);
                    button.setIncreasedFontSize(true);
                    button.setSize(button.execute(sizeVisitor));

                    /* Account for it in the layout */
                    panelConstraints.gridy = ycoord++;
                    panelConstraints.gridx = 0;

                    /* Put the button on a spacer, add the spacer to the panel and add the button to the group */
                    Spacer PDrawable = new Spacer(button, panel);
                    panel.add(PDrawable, panelConstraints);
                    tbg.getButtons().add(button);
                }

                /* Add the group to the panel */
                panel.add(new Spacer(tbg, panel));

                /* Stick our panel in the list of panels */
                panels.add(panel);
            }
            return panels;
        }

        /**
         * @see preptool.model.layout.manager.ALayoutManager.ICardLayout#setDescription(String)
         */
        public void setDescription(String description) {
            this.descriptionText = description;
        }

        /**
         * @see preptool.model.layout.manager.ALayoutManager.ICardLayout#setTitle(String)
         */
        public void setTitle(String title) {
            this.titleText = title;
        }
    }

    /**
     * PsychLayoutPanel is a subclass of JFrame and is used to temporarily hold
     * layout components so that GridBagLayout can be used to get the locations
     * of all of the components.
     *
     * @author Corey Shaw
     */
    public class PsychLayoutPanel extends JFrame {

        /**
         * North panel (for the title)
         */
        public JPanel north;

        /**
         * South panel (for the navigation buttons)
         */
        public JPanel south;

        /**
         * East panel (for the main content of the page)
         */
        public JPanel east;

        /**
         * West panel (for the sidebar - current step)
         */
        public JPanel west;

        /**
         * Constructs a new PsychLayoutPanel. Initializes the frame, and the
         * four internal panes.
         */
        PsychLayoutPanel() {
            /* Compute size of window */
            int width = WINDOW_WIDTH;
            int height = WINDOW_HEIGHT;

            /* Construct a frame so we can get an Insets object from it */
            JFrame sampleFrame = new JFrame();
            sampleFrame.setSize(width, height);
            sampleFrame.pack();

            /* Get the insets and use them to determine the visible width and height of the frame */
            Insets insets = sampleFrame.getInsets();
            int insetWidth = insets.left + insets.right;
            int insetHeight = insets.top + insets.bottom;
            height = height + insetHeight - 2;
            width = width + insetWidth - 2;
            setSize(width, height);

            /* Initialize this frame and its layout with the previously computed sizes */
            pack();
            setPreferredSize(new Dimension(width, height));
            setMinimumSize(new Dimension(width, height));
            setResizable(false);
            setLayout(new GridBagLayout());

            /* Initialize east pane, which will contain the actual ballot card */
            east = new JPanel();

            /* Initialize west pane */
            /* This pane will hold progress information about the voting session */
            west = new JPanel();
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.BOTH;
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.gridheight = 3;
            constraints.gridwidth = 1;
            constraints.weighty = 1;
            constraints.weightx = 0;
            /* Set the background to STAR-blue */
            west.setBackground(new Color(48, 149, 242));
            add(west, constraints);
            west.setLayout(new GridBagLayout());

            /* Initialize north pane */
            /* This pane will hold information about the current election and voting session */
            north = new JPanel();
            constraints.gridx = 1;
            constraints.gridy = 0;
            constraints.gridheight = 1;
            constraints.gridwidth = 1;
            constraints.weighty = 0;
            constraints.weightx = 0;
            north.setBackground(Color.pink);
            add(north, constraints);
            north.setLayout(new GridBagLayout());

            /* Initialize south pane, which holds navigation buttons */
            south = new JPanel();
            constraints.gridx = 1;
            constraints.gridy = 2;
            constraints.gridheight = 1;
            constraints.gridwidth = 1;
            constraints.weighty = 0;
            constraints.weightx = 0;
            constraints.anchor = GridBagConstraints.SOUTH;
            south.setBackground(Color.pink);
            add(south, constraints);
            south.setLayout(new GridBagLayout());
        }

        /**
         * @return an array of all components in the four panes, in order: north, south, west, east
         */ /* TODO Vet this revision */
        public Component[] getAllComponents() {
            List<Component> comps = new ArrayList<>(Arrays.asList(north.getComponents()));
            comps.addAll(Arrays.asList(south.getComponents()));
            comps.addAll(Arrays.asList(west.getComponents()));
            comps.addAll(Arrays.asList(east.getComponents()));

            return comps.toArray(north.getComponents());
        }

        /**
         * Adds a JPanel to the frame as the east panel
         *
         * @param panel the panel to add
         */
        protected void addAsEastPanel(JPanel panel) {
            /* Remove east pane if already exists */
            if (east != null) remove(east);

            /* Set constraints and add east pane */
            east = panel;
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = 1;
            constraints.gridy = 1;
            constraints.gridheight = 1;
            constraints.gridwidth = 1;
            constraints.weighty = 1;
            constraints.weightx = 1;
            east.setBackground(Color.white);
            add(east, constraints);
        }

        /**
         * Adds a Commit Ballot button to the frame, with the given label as instructions
         *
         * @param l the label that tells the user where they're going
         */
        protected void addCommitButton(Label l) {
            Spacer PCastInfo = new Spacer(l, south);
            
            Spacer PCastButton = new Spacer(commitButton, south);

            /* Setup constraints  */
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.weightx = .5;
            constraints.weighty = .5;
            constraints.insets = new Insets(0, 0, 0, 0);
            constraints.gridx = 1;
            constraints.gridy = 0;
            constraints.anchor = GridBagConstraints.LINE_END;

            /* Add the label */
            south.add(PCastInfo, constraints);

            constraints.gridx = 1;
            constraints.gridy = 1;
            constraints.insets = new Insets(0, 0, 32, 50);

            /* Add the button */
            south.add(PCastButton, constraints);
        }

        /**
         * Adds a Next button to the frame, with the given label as instructions
         *
         * @param l the label that tells the user where they're going
         */
        protected void addNextButton(Label l) {
            /* Create a spacer for the label */
            Spacer PNextInfo = new Spacer(l, south);

            /* Create the next, er, next button and set its size constraint*/
            nextButton = new Button(getNextLayoutUID(), LiteralStrings.Singleton.get("NEXT_PAGE_BUTTON", language), "NextPage");
            nextButton.setIncreasedFontSize(true);
            nextButton.setSize(nextButton.execute(sizeVisitor));

            /* Add the button to a spacer */
            Spacer PNextButton = new Spacer(nextButton, south);


            /* Setup constraints and add label and button spacers */
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.weightx = .5;
            constraints.weighty = .5;
            constraints.insets = new Insets(0, 0, 0, 0);
            constraints.gridx = 1;
            constraints.gridy = 0;
            constraints.anchor = GridBagConstraints.LINE_END;
            south.add(PNextInfo, constraints);
            constraints.gridx = 1;
            constraints.gridy = 1;
            constraints.insets = new Insets(0, 0, 32, 50);
            south.add(PNextButton, constraints);
        }


        /**
         * Adds a Previous button to the frame, with the given label as instructions
         *
         * @param l the label that tells the user where they're going
         */
        protected void addPreviousButton(Label l) {
            /* Create a spacer for the label */
            Spacer PPreviousInfo = new Spacer(l, south);

            /* Create a new previous button and set its size information */
            previousButton = new Button(getNextLayoutUID(), LiteralStrings.Singleton.get("PREVIOUS_PAGE_BUTTON", language), "PreviousPage");
            previousButton.setIncreasedFontSize(true);
            previousButton.setSize(previousButton.execute(sizeVisitor));

            /* Add the button to a spacer*/
            Spacer PPreviousButton = new Spacer(previousButton, south);

            /* Setup constraints and add label and button spacers */
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.weightx = .5;
            constraints.weighty = .5;
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.anchor = GridBagConstraints.LINE_START;
            south.add(PPreviousInfo, constraints);
            constraints.gridx = 0;
            constraints.gridy = 1;
            constraints.insets = new Insets(0, 50, 32, 0);
            south.add(PPreviousButton, constraints);
        }

        /**
         * Adds a Return button to the frame, with the given label as instructions
         *
         * @param l the label that tells the user where they're going
         * @param target page number of the target
         */
        protected void addReturnButton(Label l, int target) {
            /* Create the return button, loading its text and setting its font size, etc. */
        	returnButton = new Button(getNextLayoutUID(), LiteralStrings.Singleton.get("RETURN_BUTTON", language), "GoToPage");
            returnButton.setIncreasedFontSize(true);
            returnButton.setSize(returnButton.execute(sizeVisitor));
            returnButton.setPageNum(target);

            /* Put the label and button on spacers */
            Spacer PReturnInfo = new Spacer(l, south);
            Spacer PReturnButton = new Spacer(returnButton, south);

            /* Setup constraints and add label and button spacers */
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = 0;
            constraints.gridy = 0;
            south.add(PReturnInfo, constraints);
            constraints.gridx = 0;
            constraints.gridy = 1;
            constraints.insets = new Insets(0, 0, 32, 0);
            south.add(PReturnButton, constraints);
        }

        /**
         * Adds the sidebar to the west pane, with the given step highlighted
         *
         * @param step the current step
         */
        protected void addSideBar(int step) {
            /* Create special constraints for teh side bar */
            GridBagConstraints constraints = new GridBagConstraints();

            Spacer PYouAreNowOn       = new Spacer(instructions, west);
            Spacer PMakeYourChoice    = new Spacer(makeYourChoices, west);
            Spacer PReviewYourChoices = new Spacer(reviewYourChoices, west);
            Spacer PRecordYourVote    = new Spacer(recordYourVote, west);

            /* Select correct highlighted label for current step */

            switch (step) {

                case 1:

                    PYouAreNowOn       = new Spacer(instructionsBold, west);
                    break;

                case 2:

                    PMakeYourChoice    = new Spacer(makeYourChoicesBold, west);
                    break;

                case 3:

                    PReviewYourChoices = new Spacer(reviewYourChoicesBold, west);
                    break;

                case 4:

                    PRecordYourVote    = new Spacer(recordYourVoteBold, west);
                    break;

                default:

                    throw new IllegalStateException("Not on any current valid step in the voting process!");

            }

            /* Add the labels to west pane */
            constraints.gridy = 0;
            constraints.gridx = 0;
            constraints.weighty = 1;
            constraints.fill = GridBagConstraints.VERTICAL;
            constraints.gridwidth = 1;
            west.add(PYouAreNowOn, constraints);

            constraints.gridx = 0;
            constraints.gridy = 1;
            west.add(PMakeYourChoice, constraints);

            constraints.gridx = 0;
            constraints.gridy = 2;
            west.add(PReviewYourChoices, constraints);

            constraints.gridx = 0;
            constraints.gridy = 3;
            west.add(PRecordYourVote, constraints);
        }

        /**
         * Adds a title to this frame
         *
         *
         * @param title the title to add
         */
        protected void addTitle(Label title) {
            /* Setup constraints and add title to north pane */
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridwidth = 1;
            constraints.weightx = 1;
            constraints.weighty = 1;
            constraints.anchor = GridBagConstraints.NORTH;
            title.setCentered(true);

            /* We reserve this space for the side bar */
            title.setWidth(WINDOW_WIDTH - 225);

            title.setSize(title.execute(sizeVisitor));
            Spacer label = new Spacer(title, north);
            north.add(label, constraints);
        }

        /**
         * Adds a title String to this frame
         *
         * @param titleText the String title
         */
        protected void addTitle(String titleText) {
            Label title = new Label(getNextLayoutUID(), titleText, sizeVisitor);
            addTitle(title);
            
        }

    }

    /**
     * Maximum number of candidates on a page
     */
    private static final int MAX_CANDIDATES = 6;

    /**
     * Constant used when determining the font size
     */
    private static int FONT_SIZE_MULTIPLE = 8;

    /**
     * Width of the VoteBox window
     */
    private static final int WINDOW_WIDTH = 1600;

    /**
     * Height of the VoteBox window
     */
    private static final int WINDOW_HEIGHT = 900;


    /**
     * Visitor that renders a component and returns an image
     */
    private static ILayoutComponentVisitor<Boolean, BufferedImage> imageVisitor = new ILayoutComponentVisitor<Boolean, BufferedImage>() {
    	
        /**
         * Gets the image from the Background
         *
         * @see preptool.model.layout.ILayoutComponentVisitor#forBackground(preptool.model.layout.Background, Object[])
         */
        public BufferedImage forBackground(Background bg, Boolean... param) {
            return bg.getImage();
        }

        /**
         * Renders a Button
         *
         * @see preptool.model.layout.ILayoutComponentVisitor#forButton(preptool.model.layout.Button, Object[])
         */
        public BufferedImage forButton(Button button, Boolean... param) {

            /* Buttons will be scaled to twice the normal font size */
            int fontsize = 2 * FONT_SIZE_MULTIPLE;

            /* If the button is supposed to be of increased size, increment the font size by 4 */
            if (button.isIncreasedFontSize()) {
                fontsize += 4;
            }

            /* Now call the rendering utility to render the button */
            return RenderingUtils.renderButton(button.getText(), fontsize, button.isBold(), button.isBoxed(), -1, button.getBackgroundColor(), param[0]);
        }

        /**
         * Renders a label
         *
         * @see preptool.model.layout.ILayoutComponentVisitor#forLabel(preptool.model.layout.Label, Object[])
         */
        public BufferedImage forLabel(Label l, Boolean... param) {

            /* Labels will be scaled to twice the normal font size */
            int fontsize = 2 * FONT_SIZE_MULTIPLE;

             /* If the label is supposed to be of increased size, increment the font size by 4 */
            if (l.isIncreasedFontSize()) {
                fontsize += 4;
            }

            /* Now call the rendering utility to render the label */
            return RenderingUtils.renderLabel(l.getText(), l.getInstructions(), l.getDescription(), fontsize, l.getWidth(),
                                              l.getColor(), l.isBold(), l.isBoxed(), l.isCentered(), param[0]);
        }

        /**
         * Renders a ReviewButton
         *
         *  @see preptool.model.layout.ILayoutComponentVisitor#forReviewButton(preptool.model.layout.ReviewButton, Object[])
         */
        public BufferedImage forReviewButton(ReviewButton rb, Boolean... param) {

            /* Review buttons will be scaled to twice less 4 the normal font size */
            int fontsize = 2 * (FONT_SIZE_MULTIPLE - 1) - 2;

            /* Render the button using the rendering utility */
            BufferedImage buttonImg = RenderingUtils.renderButton(rb.getText(), fontsize, rb.isBold(), rb.isBoxed(),
                                                                  REVIEW_SCREEN_WIDTH, rb.getBackgroundColor(), param[0]);
            
			/* render party information [dsandler] */
			String aux = rb.getAuxText();

            /* Check that we are showing the party and that there is party data to show */
			if (REVIEW_SCREEN_SHOW_PARTY && aux != null && !aux.equals("")) {

                /* If the party information is supposed to be parenthesized, do it here [ e.g. (DEM) ]*/
				if (REVIEW_SCREEN_PARENTHESIZE_PARTY) 
					aux = "(" + aux + ")";

                /* Now render the String. TODO Perhaps this should be in RenderingUtils? */
				Graphics2D g = buttonImg.createGraphics();
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                /* Load the preselected font */
				Font font = new Font(RenderingUtils.FONT_NAME, Font.PLAIN, fontsize);
				g.setFont(font);
				g.setColor(Color.BLACK);

                /* Draw a bounding box around the party */
				Rectangle2D partyTextBounds = font.getStringBounds(aux, new FontRenderContext(null, true, true));

				/* draw the party on the far right side (Right-aligned) */
				g.drawString(aux, (int) (buttonImg.getWidth() - partyTextBounds.getWidth() - 10), (int) (partyTextBounds.getHeight() + 8));
			}

			return buttonImg;
        }

        /**
         * Renders a ReviewLabel
         *
         *  @see preptool.model.layout.ILayoutComponentVisitor#forReviewLabel(preptool.model.layout.ReviewLabel, Object[])
         */
        public BufferedImage forReviewLabel(ReviewLabel rl, Boolean... param) {

             /* Review labels will be scaled to twice less 2 the normal font size */
            int fontsize = 2 * (FONT_SIZE_MULTIPLE - 1);

            /* Return the result of the rendering utility's render of the label */
            return RenderingUtils.renderLabel(rl.getText(), "", "", fontsize, 100, rl.getColor(), rl.isBold(), rl.isBoxed(), rl.isCentered(), param[0]);
        }

        /**
         * Renders a ToggleButton
         *
         *  @see preptool.model.layout.ILayoutComponentVisitor#forToggleButton(preptool.model.layout.ToggleButton, Object[])
         */
        public BufferedImage forToggleButton(ToggleButton tb, Boolean... param) {

            /* The toggle button font size will be twice the normal font size */
            int fontsize = 2 * FONT_SIZE_MULTIPLE;

            /* If the button is supposed to have larger font size, make it 4 larger than twice the normal size */
            if (tb.isIncreasedFontSize()) {
                fontsize += 4;
            }

            /* Return the rendering utility's render of the button */
            return RenderingUtils.renderToggleButton(tb.getText(), tb.getSecondLine(), tb.getParty(), fontsize,	tb.getWidth(), tb.isBold(), param[0], param[1]);
        }

        /**
         * @see preptool.model.layout.ILayoutComponentVisitor#forToggleButtonGroup(preptool.model.layout.ToggleButtonGroup, Object[])
         *
         * @return null
         */
        public BufferedImage forToggleButtonGroup(ToggleButtonGroup tbg, Boolean... param) {
            return null;
        }


        /**
         * @see preptool.model.layout.ILayoutComponentVisitor#forPrintButton(preptool.model.layout.PrintButton, Object[])
         */
        public BufferedImage forPrintButton(PrintButton pb, Boolean... param) {

            /* Make the font size for print buttons the normal font size */
            int fontsize = FONT_SIZE_MULTIPLE;

            /* Increase the font size if necessary */
            if (pb.isIncreasedFontSize()) {
                fontsize += 4;
            }

            /* Render using the rendering utility. Chose 281 for the width because it suits our printing well */
            return RenderingUtils.renderPrintButton(pb.getUID(), pb.getText(), pb.getSecondLine(), pb.getParty(), fontsize, 281, false, true);
		}
    };

    /**
     * Visitor that calculates the size of a component
     */
    private static ILayoutComponentVisitor<Object, Dimension> sizeVisitor = new ILayoutComponentVisitor<Object, Dimension>() {

        /**
         * Gets the size of the Background
         *
         * @see preptool.model.layout.ILayoutComponentVisitor#forBackground(preptool.model.layout.Background, Object[])
         */
        public Dimension forBackground(Background bg, Object... param) {
            return new Dimension(bg.getWidth(), bg.getHeight());
        }

        /**
         * Calculates the size of the Button
         *
         * @see preptool.model.layout.ILayoutComponentVisitor#forButton(preptool.model.layout.Button, Object[])
         */
        public Dimension forButton(Button button, Object... param) {
            /* The button font size will be twice the normal font size */
            int fontsize = 2 * FONT_SIZE_MULTIPLE;

            /* Increase the font size if necessary */
            if (button.isIncreasedFontSize()) {
                fontsize += 4;
            }

             /* Return the rendering utility's calculated size of the button */
            return RenderingUtils.getButtonSize(button.getText(), fontsize, button.isBold());
        }

        /**
         * Calculates the size of the
         *
         * @see preptool.model.layout.ILayoutComponentVisitor#forLabel(preptool.model.layout.Label, Object[])
         */
        public Dimension forLabel(Label l, Object... param) {

             /* The button font size will be twice the normal font size */
            int fontsize = 2 * FONT_SIZE_MULTIPLE;

            /* Increase the font size if necessary */
            if (l.isIncreasedFontSize()) {
                fontsize += 4;
            }

            /* Return the rendering utility's calculated size of the label */
            return RenderingUtils.getLabelSize(l.getText(), l.getInstructions(), l.getDescription(), fontsize, l.getWidth(), l.isBold(), l.isCentered());
        }

        /**
         * Calculates the size of the ReviewButton
         *
         * @see preptool.model.layout.ILayoutComponentVisitor#forReviewButton(preptool.model.layout.ReviewButton, Object[])
         */
        public Dimension forReviewButton(ReviewButton rb, Object... param) {

            /* The button font size will be twice the normal font size */
            int fontsize = (int) (1.5 * (FONT_SIZE_MULTIPLE - 2));

            /* Increase the font size if necessary */
            if (rb.isIncreasedFontSize()) {
                fontsize += 4;
            }

            /* Return the rendering utility's calculated size of the button */
            return RenderingUtils.getButtonSize(rb.getText(), fontsize, rb.isBold());
        }

        /**
         * Calculates the size of the ReviewLabel
         *
         * @see preptool.model.layout.ILayoutComponentVisitor#forReviewLabel(preptool.model.layout.ReviewLabel, Object[])
         */
        public Dimension forReviewLabel(ReviewLabel rl, Object... param) {

            /* The button font size will be twice the normal font size */
            int fontsize = (int) (1.5 * (FONT_SIZE_MULTIPLE - 2));

            /* Return the rendering utility's calculated size of the label */
            return RenderingUtils.getLabelSize(rl.getText(), "", "", fontsize, rl.getWidth(), rl.isBold(), rl.isCentered());
        }

        /**
         * Calculates the size of the ToggleButton
         *
         * @see preptool.model.layout.ILayoutComponentVisitor#forToggleButton(preptool.model.layout.ToggleButton, Object[])
         */
        public Dimension forToggleButton(ToggleButton tb, Object... param) {

            /* The button font size will be twice the normal font size */
            int fontsize = 2 * (FONT_SIZE_MULTIPLE);

            /* Increase the font size if necessary */
            if (tb.isIncreasedFontSize()) {
                fontsize += 4;
            }

            /* Return the rendering utility's calculated size of the button */
            return RenderingUtils.getToggleButtonSize(tb.getText(), tb.getSecondLine(), tb.getParty(), fontsize, RACE_DESCRIPTION_WIDTH, tb.isBold());
        }

        /**
         * @see preptool.model.layout.ILayoutComponentVisitor#forToggleButtonGroup(preptool.model.layout.ToggleButtonGroup, Object[])
         *
         * @return null
         */
        public Dimension forToggleButtonGroup(ToggleButtonGroup tbg, Object... param) {
            return null;
        }


        /**
         * @see preptool.model.layout.ILayoutComponentVisitor#forPrintButton(preptool.model.layout.PrintButton, Object[])
         */
        public Dimension forPrintButton(PrintButton pb, Object... param) {
            /* The button font size will be twice the normal font size */
            int fontsize = 2 * (FONT_SIZE_MULTIPLE);

            /* Increase the font size if necessary */
            if (pb.isIncreasedFontSize()) {
                fontsize += 4;
            }

            /* Return the rendering utility's calculated size of the button */
            return RenderingUtils.getToggleButtonSize(pb.getText(), pb.getSecondLine(), pb.getParty(), fontsize, RACE_DESCRIPTION_WIDTH, pb.isBold());
		}
    };

    /**
     * The language this LayoutManager is responsible for
     */
    private Language language;

    /**
     * A Common Commit Button
     */
    protected Button commitButton;

    /**
     * A Common NextButton
     */
    protected Button nextButton;

    /**
     * A Common PreviousButton
     */
    protected Button previousButton;

    /**
     * A Common Return Button
     */
    protected Button returnButton;

    /**
     * Instructions label for the sidebar
     */
    protected Label instructions;

    /**
     * Make your choices label for the sidebar
     */
    protected Label makeYourChoices;

    /**
     * Review your choices label for the sidebar
     */
    protected Label reviewYourChoices;

    /**
     * Record your vote label for the sidebar
     */
    protected Label recordYourVote;

    /**
     * Bold Instructions label for the sidebar
     */
    protected Label instructionsBold;

    /**
     * Bold Make your choices label for the sidebar
     */
    protected Label makeYourChoicesBold;

    /**
     * Bold Review your choices label for the sidebar
     */
    protected Label reviewYourChoicesBold;

    /**
     * Bold Record your vote label for the sidebar
     */
    protected Label recordYourVoteBold;

    /**
     * Next race label
     */
    protected Label nextInfo;

    /**
     * Previous race label
     */
    protected Label previousInfo;

    /**
     * Return label
     */
    protected Label returnInfo;

    /**
     * More candidates label
     */
    protected Label moreCandidatesInfo;

    /**
     * The background for this layout
     */
    protected Background background;

    /**
     * Background for this layout, without the sidebar
     */
    protected Background simpleBackground;

    /**
     * Creates a new PsychLayoutManager and initializes many of the "common"
     * components, such as the next button.
     */
    public PsychLayoutManager(Language language, int numCardsPerReviewPage, int fontSize, boolean textToSpeech) {

        this.language = language;

        GENERATE_AUDIO = textToSpeech;
        
        CARDS_PER_REVIEW_PAGE = numCardsPerReviewPage;

    	FONT_SIZE_MULTIPLE = fontSize;

        /* Initialize the instructions label, which is the first element on the sidebar of the GUI */
        instructions = new Label(getNextLayoutUID(), LiteralStrings.Singleton.get("SIDEBAR_INSTRUCTIONS", language));
        instructions.setWidth(225);
        instructions.setIncreasedFontSize(true);
        instructions.setColor(new Color(72, 72, 72));
        instructions.setSize(instructions.execute(sizeVisitor));

        /* This is the bold version of the instructions label */
        instructionsBold = new Label(getNextLayoutUID(),LiteralStrings.Singleton.get("SIDEBAR_INSTRUCTIONS_HIGHLIGHTED", language));
        instructionsBold.setWidth(225);
        instructionsBold.setIncreasedFontSize(true);
        instructionsBold.setColor(Color.WHITE);
        instructionsBold.setBold(false);
        instructionsBold.setSize(instructionsBold.execute(sizeVisitor));

        /* Initialize the "Make your choices" label, step 2 on the side bar */
        makeYourChoices = new Label(getNextLayoutUID(), LiteralStrings.Singleton.get("SIDEBAR_MAKE_CHOICES", language));
        makeYourChoices.setWidth(225);
        makeYourChoices.setIncreasedFontSize(true);
        makeYourChoices.setColor(new Color(72, 72, 72));
        makeYourChoices.setSize(makeYourChoices.execute(sizeVisitor));

        /* This is the bold version of the choices label */
        makeYourChoicesBold = new Label(getNextLayoutUID(),LiteralStrings.Singleton.get("SIDEBAR_MAKE_CHOICES_HIGHLIGHTED", language));
        makeYourChoicesBold.setWidth(225);
        makeYourChoicesBold.setIncreasedFontSize(true);
        makeYourChoicesBold.setColor(Color.WHITE);
        makeYourChoicesBold.setBold(false);
        makeYourChoicesBold.setSize(makeYourChoicesBold.execute(sizeVisitor));

        /* Initialize the "Review your choices" label, step 3 on the sidebar */
        reviewYourChoices = new Label(getNextLayoutUID(), LiteralStrings.Singleton.get("SIDEBAR_REVIEW_CHOICES", language));
        reviewYourChoices.setWidth(225);
        reviewYourChoices.setIncreasedFontSize(true);
        reviewYourChoices.setColor(new Color(72, 72, 72));
        reviewYourChoices.setSize(reviewYourChoices.execute(sizeVisitor));

        /* This is the bold version of the review label */
        reviewYourChoicesBold = new Label(getNextLayoutUID(), LiteralStrings.Singleton.get("SIDEBAR_REVIEW_CHOICES_HIGHLIGHTED", language));
        reviewYourChoicesBold.setWidth(225);
        reviewYourChoicesBold.setIncreasedFontSize(true);
        reviewYourChoicesBold.setColor(Color.WHITE);
        reviewYourChoicesBold.setBold(false);
        reviewYourChoicesBold.setSize(reviewYourChoicesBold.execute(sizeVisitor));

        /* Initialize the "Record your vote" label, step 4 on the sidebar */
        recordYourVote = new Label(getNextLayoutUID(), LiteralStrings.Singleton.get("SIDEBAR_RECORD_VOTE", language));
        recordYourVote.setWidth(225);
        recordYourVote.setIncreasedFontSize(true);
        recordYourVote.setColor(new Color(72, 72, 72));
        recordYourVote.setSize(recordYourVote.execute(sizeVisitor));

        /* This is the bold version of the record label */
        recordYourVoteBold = new Label(getNextLayoutUID(), LiteralStrings.Singleton.get("SIDEBAR_RECORD_VOTE_HIGHLIGHTED", language));
        recordYourVoteBold.setWidth(225);
        recordYourVoteBold.setIncreasedFontSize(true);
        recordYourVoteBold.setColor(Color.WHITE);
        recordYourVoteBold.setBold(false);
        recordYourVoteBold.setSize(recordYourVoteBold.execute(sizeVisitor));

        /* Initialize a next button */
        nextButton = new Button(getNextLayoutUID(), LiteralStrings.Singleton.get("NEXT_PAGE_BUTTON", language), "NextPage");
        nextButton.setIncreasedFontSize(true);
        nextButton.setSize(nextButton.execute(sizeVisitor));

        /* Initialize a previous button */
        previousButton = new Button(getNextLayoutUID(), LiteralStrings.Singleton.get("PREVIOUS_PAGE_BUTTON", language), "PreviousPage");
        previousButton.setIncreasedFontSize(true);
        previousButton.setSize(previousButton.execute(sizeVisitor));

        /* Initialize the review screen return button */
        returnButton = new Button(getNextLayoutUID(), LiteralStrings.Singleton.get("RETURN_BUTTON", language), "GoToPage");
        returnButton.setIncreasedFontSize(true);
        returnButton.setSize(returnButton.execute(sizeVisitor));

        /* Initialize the commit button */
        commitButton = new Button(getNextLayoutUID(), LiteralStrings.Singleton.get("COMMIT_BUTTON", language), "CommitBallot");
        commitButton.setIncreasedFontSize(true);
        commitButton.setSize(commitButton.execute(sizeVisitor));

        /* Initialize the instructions label accompanying the next page button */
        nextInfo = new Label(getNextLayoutUID(), LiteralStrings.Singleton.get("FORWARD_NEXT_RACE", language), sizeVisitor);

        /* Initialize the instructions label accompanying the previous page button */
        previousInfo = new Label(getNextLayoutUID(), LiteralStrings.Singleton.get("BACK_PREVIOUS_RACE", language), sizeVisitor);

        /* Initialize the instructions label accompanying the review screen return button */
        returnInfo = new Label(getNextLayoutUID(), LiteralStrings.Singleton.get("RETURN_REVIEW_SCREEN", language), sizeVisitor);

        /* Initialize the instructions label accompanying the more candidates button */
        moreCandidatesInfo = new Label(getNextLayoutUID(), LiteralStrings.Singleton.get("MORE_CANDIDATES", language), sizeVisitor);

        /* Initialize the backgrounds */
        background = makeBackground();
        simpleBackground = makeSimpleBackground();
    }


    /**
     * @return the instructions label for the straight-party screen
     */
    public Label getPartyInstructions() {
        return new Label(getNextLayoutUID(), LiteralStrings.Singleton.get("PARTY_INSTRUCTIONS", language), sizeVisitor);
    }

    /**
     * @return the instruction label for a proposition
     */
    public Label getPropInstructions() {
        return new Label(getNextLayoutUID(), LiteralStrings.Singleton.get("PROPOSITION_INSTRUCTIONS", language), sizeVisitor);
    }

    /**
     * @return the instruction label for a race, presidential or otherwise
     */
    public Label getRaceInstructions() {
        return new Label(getNextLayoutUID(), LiteralStrings.Singleton.get("RACE_INSTRUCTIONS", language), sizeVisitor);
    }

    /**
     * @return the image rendering visitor
     */
    @Override
    public ILayoutComponentVisitor<Boolean, BufferedImage> getImageVisitor() {
        return imageVisitor;
    }

    /**
     * @return the language
     */
    @Override
    public Language getLanguage() {
        return language;
    }

    /**
     * @return the size calculating visitor
     */
    @Override
    public ILayoutComponentVisitor<Object, Dimension> getSizeVisitor() {
        return sizeVisitor;
    }

    /**
     * @see preptool.model.layout.manager.ALayoutManager#makeCardPage(preptool.model.ballot.ACard)
     */
    @Override
    public ArrayList<JPanel> makeCardPage(ACard card) {
        return card.layoutCard(this, new PsychCardLayout()).makeIntoPanels();
    }

    /**
     * Makes the layout from the given ballot, as specified by the Rice University Psychology department.
     *
     * @see preptool.model.layout.manager.ALayoutManager#makeLayout(preptool.model.ballot.Ballot)
     */
    @Override
    public Layout makeLayout(Ballot ballot) {

        /* First assign UIDs */
        assignUIDsToBallot(ballot);

        /* Initialize a layout */
        Layout layout = new Layout();

         /* If there is more than one language, add in a language selection page */
        boolean multiLang = ballot.getLanguages().size() > 1;

        if (multiLang)
            layout.getPages().add(makeLanguageSelectPage(ballot.getLanguages()));

        layout.getPages().add(makeInstructionsPage(multiLang));


        /* Create a new page for each card in the ballot and add it to the layout */
        int cnt = 1;
        for (ACard card : ballot.getCards()) {
            layout.getPages().addAll(makeCardLayoutPage(card, false, 0, cnt, ballot.getCards().size()));
            cnt++;
        }

        /* This will be where we insert the review pages*/
        int reviewPageNum = layout.getPages().size();

        /* This will map each page with a race to a corresponding review page*/
        HashMap<Integer, Integer> pageTargets = new HashMap<>();

        /* Iterate through all of the races and create review pages */
        for (int raceN = 0; raceN < ballot.getCards().size(); raceN++) {

            /* Get the race information */
        	ACard card = ballot.getCards().get(raceN);

        	/*
        	 * Calculate how many review pages this card will need, and correctly deal
        	 * with boundary conditions: size = 1, size = CARDS_PER_REVIEW_PAGE or a multiple thereof
        	 */
        	int additionalReviewPages = (ballot.getCards().size() - 1) / CARDS_PER_REVIEW_PAGE;


        	/* there are 2 pages after the last review screen: Commit and Success, plus one for this card */
        	int reviewCardNumber = raceN + 3;

            /* Map the numerical identifier for this race to its last review page */
            pageTargets.put(raceN, reviewPageNum + additionalReviewPages + reviewCardNumber);

            /* Calculate the first page that corresponds to this card */
            int currentReviewPage = raceN / CARDS_PER_REVIEW_PAGE;

            /* Generate and add this review page to the layout */
            layout.getPages().addAll(makeCardLayoutPage(card, true, reviewPageNum + currentReviewPage, 0, 0));
        }

        /* Create the rest of the review pages using the mapping of pages to review pages */
        List<Page> reviewPages = makeReviewPage(ballot, pageTargets);

        /* Add the review pages to the layout */
        layout.getPages().addAll(reviewPageNum, reviewPages);

        /* Note which pages are review pages */
        for(Page reviewPage : reviewPages)
        	reviewPage.markAsReviewPage();

        /* Add the commit page */
        layout.getPages().add(reviewPageNum + (ballot.getCards().size() / CARDS_PER_REVIEW_PAGE) + 1, makeCommitPage());

        /* Add the success/print page */
        layout.getPages().add(reviewPageNum + (ballot.getCards().size() / CARDS_PER_REVIEW_PAGE) + 2, makeSuccessPage());

        /* Add in an override cancel page*/
        layout.getPages().add(makeOverrideCancelPage());
        layout.setOverrideCancelPage(layout.getPages().size()-1);

        /* Add in an override commit page */
        layout.getPages().add(makeOverrideCommitPage());
        layout.setOverrideCommitPage(layout.getPages().size() - 1);

        /* Add a success/print page for provisional voting */
        layout.getPages().add(makeProvisionalSuccessPage());

        /* Set the provisional success page in the layout */
        layout.setProvisionalPage(layout.getPages().size() - 1);

        /* Our layout is now complete */
        return layout;
    }

    /**
     * Makes a Background for this LayoutManager.
     *
     * @return the Background
     */
    protected Background makeBackground() {

        /* Initialize our panel */
        PsychLayoutPanel frame = new PsychLayoutPanel();

        /* Initialize the instructions label and add it to the panel */
        Label instructionsTitle = new Label("L0", LiteralStrings.Singleton.get("INSTRUCTIONS_TITLE", language));
        instructionsTitle.setCentered(true);
        instructionsTitle.setSize(instructionsTitle.execute(sizeVisitor));
        frame.addTitle(instructionsTitle);

        /* Add a side bar */
        frame.addSideBar(1);

        /* Add a next button along with its label */
        frame.addNextButton(nextInfo);

        /* Create and add the panel where the actual selections will be made */
        JPanel east = new JPanel();
        east.setLayout(new GridBagLayout());
        Label instrLabel = new Label("L0", LiteralStrings.Singleton.get("INSTRUCTIONS", language), sizeVisitor);
        Spacer sp = new Spacer(instrLabel, east);
        east.add(sp);
        frame.addAsEastPanel(east);

        /* Draw and align the frame  */
        frame.validate();
        frame.pack();

        /* Draw the background */
        BufferedImage image = new BufferedImage(WINDOW_WIDTH, WINDOW_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphs = (Graphics2D) image.getGraphics();

        /* First color the whole image white*/
        graphs.setColor(Color.WHITE);
        graphs.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

        /* Now color the sidebar blue */
        graphs.setColor(new Color(48, 149, 242));
        graphs.fillRect(0, 0, frame.west.getWidth(), WINDOW_HEIGHT);

        /* Color the top and bottom borders pink */
        graphs.setColor(Color.PINK);
        graphs.fillRect(frame.west.getWidth(), 0, frame.north.getWidth(), frame.north.getHeight());
        graphs.fillRect(frame.west.getWidth(), WINDOW_HEIGHT - frame.south.getHeight(), frame.south.getWidth(), frame.south.getHeight());

        return new Background(getNextLayoutUID(), image);
    }

    /**
     * Makes a Page that contains a Card, by creating the page as normal and
     * then calling into the visitor to handle the Card-specific components
     *
     * @param card the card
     * @param jump whether this page is a jump page from the review screen
     * @param target page number of the review screen
     * @param idx the index of the card in the ballot
     * @param total total number of cards
     * @return the completed Page
     */
    protected ArrayList<Page> makeCardLayoutPage(ACard card, boolean jump, int target, int idx, int total) {

        /* TODO Maybe move this functionality into ACard, since it should be able to deal with it */

        /* Get the card panels */
        ArrayList<JPanel> cardPanels = makeCardPage(card);

        /* This will hold the pages as they're generated */
        ArrayList<Page> pages = new ArrayList<>();

        /* For each card, create a panel */
        for (int i = 0; i < cardPanels.size(); i++) {

            /* Setup card frame */
            PsychLayoutPanel cardFrame = new PsychLayoutPanel();
            cardFrame.addTitle(card.getTitle(language));
            cardFrame.addSideBar(2);

            /* If this page wasn't jumped to, i.e. isn't an override or review page */
            if (!jump) {

                /* If this is not the first page of the card and we haven't jumped, add a "go back to see more candidates" button  */
                if (i > 0)
                    cardFrame.addPreviousButton((Label) moreCandidatesInfo.clone());

                /* If this is the first selection page of the first card in the ballot, add a button to go back to the instructions */
                else if (idx == 1)
                    cardFrame.addPreviousButton(new Label(getNextLayoutUID(), LiteralStrings.Singleton.get("BACK_INSTRUCTIONS", language), sizeVisitor));

                /* Otherwise we're on the first page of a card that isn't the first card, so add a normal previous card button */
                else cardFrame.addPreviousButton(previousInfo);

                /* If this is not the last selection page in the ballot, add a view more candidates button */
                if (i < cardPanels.size() - 1)
                    cardFrame.addNextButton((Label) moreCandidatesInfo.clone());

                /* If this is the last selection page in the ballot, add a forward to review button */
                else if (idx == total)
                    cardFrame.addNextButton(new Label(getNextLayoutUID(), LiteralStrings.Singleton.get("FORWARD_REVIEW", language), sizeVisitor));

                /* Otherwise, add a normal next page button*/
                else cardFrame.addNextButton(nextInfo);
            }

            /* If we were jumped to this page, add only a return button and more candidates options*/
            else {
                cardFrame.addReturnButton(returnInfo, target);

                /* If this isn't the first page in the review section, add a "go back to see more candidates" */
                if (i > 0)
                    cardFrame.addPreviousButton((Label) moreCandidatesInfo.clone());

                /* If this isn't the last page in the review section, add a "go forward to see more candidates" button */
                if (i < cardPanels.size() - 1)
                    cardFrame.addNextButton((Label) moreCandidatesInfo.clone());
            }

            /* Add card's content as east pane */
            cardFrame.addAsEastPanel(cardPanels.get(i));

            /* Add instructions to the top of the panel */
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.anchor = GridBagConstraints.NORTH;
            constraints.fill = GridBagConstraints.VERTICAL;
            constraints.gridy = 1;
            constraints.gridx = 0;
            constraints.weightx = 1;
            constraints.weighty = 1;
            Spacer instspacer;

            /* Determine what kind of card we have and get the appropriate instructions */
            boolean isPartyCard = card instanceof PartyCard;
            boolean isPropositionCard = card instanceof PropositionCard;

            Label instructions = isPartyCard       ? getPartyInstructions() :
                                 isPropositionCard ? getPropInstructions()  : getRaceInstructions();

            /* Put the instructions on a spacer and add them to the top of the card panel */
            instspacer = new Spacer(instructions, cardFrame.north);
            cardFrame.north.add(instspacer, constraints);
            cardFrame.validate();
            cardFrame.pack();

            /* Add all components to a Page, with their updated positions */
            Page cardPage = new Page();
            cardPage.getComponents().add(background);
            cardPage.setBackgroundLabel(background.getUID());

            /* Denote whether the component on the card should be titled */
            boolean titled = false;

            /* This button will allow us set up intermediary keyboard navigation until all the buttons are added */
            ALayoutComponent tempButton = null;

            /* Iterate over all the components and add them to the page */
            for (Component c : cardFrame.getAllComponents()) {

                /* We will pretend the component is actually a spacer to handle its positioning */
                Spacer s = (Spacer) c;
                s.updatePosition();

                /* TODO This could probably use some redoing... */
                /* If this isn't a toggle button, we can directly add the component to the layout */
                if (!(s.getComponent() instanceof ToggleButton))
                    cardPage.getComponents().add(s.getComponent());

                /* Get the component out for manipulation */
                ALayoutComponent button = s.getComponent();

                /* A flag that will determine if a label component is focusable and needs navigation setup */
                boolean focusable = false;

                /* If we have a label */
                if(button instanceof Label){

                    /* If this is the title of the component group */
                    if(((Label) button).getText().equals(card.getTitle(language))) {

                        /* The title should be able to be navigated to */
                        if(titled) focusable = true;

                        /* If there hasn't previously been a title, then it shouldn't be navigated
                         *  to but this component group is titled
                         */
                        else titled = true;

                    }
                }

                /* At this point, we either have buttons or otherwise focusable components */
                if(button instanceof ToggleButton || focusable){

                    /*
                     * Review cards have less-involved navigation, effectively they are only one column:
                     *
                     *                                 INSTRUCTIONS
                     *                                    TITLE
                     *                                  SELECTION 1
                     *                                  SELECTION 2
                     *                                  SELECTION 3
                     *                                      .
                     *                                      .
                     *                                      .
                     *                                  SELECTION N
                     *
                     *                                    RETURN
                     *
                     * Cyclic navigation (that is, two-keyed, next element and previous element selection) will start
                     * at the top and work its way down, and then circle back on getting to the bottom.
                     *
                     * 4-directional navigation will work much the same way, with the up and down keys doing the same
                     * as cyclic navigation, and the left and right keys toggling between the RETURN button and the
                     * bottom-most selection, in this case SELECTION N
                     */
                    if(jump){
                        /* TODO Make this work with lots of candidates (i > n) */

                        /* If this is the first button we've seen, we can set the instruction's navigation*/
                        if(tempButton == null){
                            button.setPrevious(instructions);
                            button.setUp(instructions);
                            instructions.setDown(button);
                            instructions.setNext(button);

                        }

                        /*
                         * If this isn't the first button, the previously seen button is stored in tmpButton and we can
                         * set up its navigation
                         */
                        else{
                            button.setPrevious(tempButton);
                            button.setUp(tempButton);
                            tempButton.setNext(button);
                            tempButton.setDown(button);
                        }

                        /* Ensure that horizontal navigation is short, only between the return button and the column of instructions/selections*/
                        button.setLeft(returnButton);
                        button.setRight(returnButton);


                    }

                    /*
                     * If we're not a review page, navigation is a little complicated
                     *
                     * Here we effectively have 3 columns, one with the previous button, one with the selections, and
                     * one with the next button, as follows:
                     *
                     *                                  INSTRUCTIONS
                     *
                     *                                    TITLE
                     *                                  SELECTION 1
                     *                                  SELECTION 2
                     *                                      .
                     *                                      .
                     *                                      .
                     *                                  SELECTION N
                     *
                     *                   PREVIOUS PAGE                   NEXT PAGE
                     *
                     * Cyclic navigation will start on the PREVIOUS PAGE button, and the "next" link will be INSTRUCTIONS,
                     * after which it will navigate down the center column. On getting to the bottom of the center column,
                     * the next option will go to the NEXT PAGE button. Reverse this process for the "back" links.
                     *
                     * 4-directional navigation works as one would expect, starting at the PREVIOUS PAGE button. UP will
                     * result in the focusing of SELECTION N, DOWN and RIGHT will go to the INSTRUCTIONS, and LEFT will
                     * go to the NEXT PAGE button.
                     *
                     * In the center column, down and up work as expected, where UP at the top (INSTRUCTIONS) will go to
                     * the PREVIOUS PAGE button, and DOWN at the bottom of the column (SELECTION N) will go to NEXT PAGE.
                     * Otherwise, in the center column, UP and DOWN will go to the elements above and below, respectively.
                     * E.g., if SELECTION 1 is focused, UP will go to TITLE, DOWN will go to SELECTION 2, LEFT will go to
                     * PREVIOUS PAGE, and RIGHT will go to NEXT PAGE.
                     *
                     * For the right-most column, navigation is simply the inverse of the left-most column. RIGHT will
                     * focus PREVIOUS PAGE, LEFT will go to SELECTION N, and UP and DOWN function just as they do for
                     * PREVIOUS PAGE
                     */
                    else{

                        /* If this is the first button, set up the instruction's navigation */
                        if(tempButton == null){
                            button.setPrevious(instructions);
                            button.setUp(instructions);
                            previousButton.setNext(instructions);
                            instructions.setNext(button);
                            instructions.setPrevious(previousButton);
                            instructions.setLeft(previousButton);
                            instructions.setDown(button);
                            instructions.setRight(button);
                            nextButton.setDown(button);

                        }

                        /* If we are not the first button, use the last button to set up navigation */
                        else{
                            button.setPrevious(tempButton);
                            button.setUp(tempButton);
                            tempButton.setNext(button);
                            tempButton.setDown(button);
                        }

                        button.setLeft(previousButton);
                        button.setRight(nextButton);

                    }

                    /* Update so the next button can get back to this button */
                    tempButton = button;

                }
            }

            /* Now that we've set up all of the components, tie up the loose ends */

            /* If we are a review screen, tie the return button to the last seen button */
            if(jump){
                assert tempButton != null;
                tempButton.setNext(returnButton);
                tempButton.setDown(returnButton);
                returnButton.setPrevious(tempButton);
                returnButton.setUp(tempButton);
            }

            /* Otherwise tie the last selection button to the previous and next buttons */
            else{
                assert tempButton != null;
                tempButton.setNext(nextButton);
                tempButton.setDown(nextButton);
                nextButton.setPrevious(tempButton);
                nextButton.setUp(tempButton);
                nextButton.setLeft(tempButton);
                previousButton.setUp(tempButton);
            }

            /* Add the new page to the list of pages */
            pages.add(cardPage);
        }
        return pages;
    }

    /**
     * @return a page that will inform the voter that they are about to commit and print their vote
     */
    @Override
    protected Page makeCommitPage() {

        /* This frame will hold the components */
        PsychLayoutPanel frame = new PsychLayoutPanel();

        /* Title for this screen */
        Label recordTitle = new Label(getNextLayoutUID(), LiteralStrings.Singleton.get("RECORD_TITLE", language));
        recordTitle.setBold(true);
        recordTitle.setCentered(true);
        recordTitle.setSize(recordTitle.execute(sizeVisitor));
        frame.addTitle(recordTitle);

        /* Add the sidebar in the correct step */
        frame.addSideBar(4);

        /* Add a back button to go back to the review screen */
        frame.addPreviousButton(new Label(getNextLayoutUID(), LiteralStrings.Singleton.get("BACK_REVIEW", language), sizeVisitor));

        /* Add a commit/print button to go to the print screen and print the ballot */
        frame.addCommitButton(new Label(getNextLayoutUID(), LiteralStrings.Singleton.get("NEXT_PAGE_BUTTON", language), sizeVisitor));

        /* On the selection panel, display instruction about what is about to happen, the printing, etc. */
        JPanel east = new JPanel();
        east.setLayout(new GridBagLayout());
        Label instrLabel = new Label(getNextLayoutUID(), LiteralStrings.Singleton.get("RECORD_INSTRUCTIONS", language), sizeVisitor);
        Spacer sp = new Spacer(instrLabel, east);

        /* Set up the keyboard navigation. 3 columns, from PREVIOUS to INSTRUCTIONS to PRINT */
        previousButton.setNext(instrLabel);
        previousButton.setRight(instrLabel);
        instrLabel.setLeft(previousButton);
        instrLabel.setPrevious(previousButton);
        instrLabel.setRight(commitButton);
        instrLabel.setNext(commitButton);
        commitButton.setPrevious(instrLabel);
        commitButton.setLeft(instrLabel);

        /* Add the panel */
        east.add(sp);
        frame.addAsEastPanel(east);

        /* Update the frame */
        frame.validate();
        frame.pack();

        /* Create and initialize a new page */
        Page page = new Page();
        page.getComponents().add(background);
        page.setBackgroundLabel(background.getUID());

        /* Populate the page with the previously added components on the frame */
        for (Component c : frame.getAllComponents()) {

            /* Pretend the components are spacers for positioning */
            Spacer s = (Spacer) c;
            s.updatePosition();

            page.getComponents().add(s.getComponent());
        }
        return page;
    }

    /**
     * @param hasLanguageSelect whether the ballot will enable users to select a language
     * @return a page containing all of the instructions for the election, to be shown at the beginning of the voting process
     */
    @Override
    protected Page makeInstructionsPage(boolean hasLanguageSelect) {

        /* This frame will hold the components */
        PsychLayoutPanel frame = new PsychLayoutPanel();

        /* Create and add a label containing the title of this page */
        Label instructionsTitle = new Label(getNextLayoutUID(), LiteralStrings.Singleton.get("INSTRUCTIONS_TITLE", language));
        instructionsTitle.setCentered(true);
        instructionsTitle.setSize(instructionsTitle.execute(sizeVisitor));
        frame.addTitle(instructionsTitle);
        frame.addSideBar(1);

        /* Create and populate a panel containing all of the election instructions */
        JPanel east = new JPanel();
        east.setLayout(new GridBagLayout());
        Label instrLabel = new Label(getNextLayoutUID(), LiteralStrings.Singleton.get("INSTRUCTIONS", language), sizeVisitor);

        /* If there is a language option, then this page will have different buttons */
        if (hasLanguageSelect){

            /* Add a button to go back ot language selection */
            frame.addPreviousButton(new Label(getNextLayoutUID(), LiteralStrings.Singleton.get("BACK_LANGUAGE_SELECT", language), sizeVisitor));

            /* Add a button to go to the first race*/
            frame.addNextButton(new Label(getNextLayoutUID(), LiteralStrings.Singleton.get("FORWARD_FIRST_RACE", language), sizeVisitor));

            /* Navigation will be the 3 column model described above, only without selections */
            nextButton.setPrevious(instrLabel);
            nextButton.setLeft(instrLabel);
            instrLabel.setNext(nextButton);
            instrLabel.setRight(nextButton);
            instrLabel.setPrevious(previousButton);
            instrLabel.setLeft(previousButton);
            previousButton.setNext(instrLabel);
            previousButton.setRight(instrLabel);
        }

        /* If there is no language selection, there is only one button */
        else{

            /* Add a button to go to the first race*/
            frame.addNextButton(new Label(getNextLayoutUID(), LiteralStrings.Singleton.get("FORWARD_FIRST_RACE", language), sizeVisitor));

            /* Since there are only two elements here, any navigation will just toggle between them*/
            instrLabel.setNext(nextButton);
            instrLabel.setRight(nextButton);
            nextButton.setPrevious(instrLabel);
            nextButton.setLeft(instrLabel);


        }

        /* Add the instructions to a label and put them on a spacer on the panel */
        Spacer sp = new Spacer(instrLabel, east);
        east.add(sp);
        frame.addAsEastPanel(east);

        /* Update the frame */
        frame.validate();
        frame.pack();

        /* Create the page*/
        Page page = new Page();

        /* If there is a background, add it to the page*/
        if (background != null) {
            page.getComponents().add(background);
            page.setBackgroundLabel(background.getUID());
        }

        /* Add all the components to the page */
        for (Component c : frame.getAllComponents()) {
            Spacer s = (Spacer) c;
            s.updatePosition();
            page.getComponents().add(s.getComponent());
        }

        return page;
    }

    /**
     * @param languages a list of the languages available
     * @return a page that will allow the user to select a language
     */
    @Override
    protected Page makeLanguageSelectPage(ArrayList<Language> languages) {

        /* A frame for positioning the components */
        PsychLayoutPanel frame = new PsychLayoutPanel();

        /* Add a title to the page*/
        frame.addTitle(LiteralStrings.Singleton.get("LANGUAGE_SELECT_TITLE", language));

        /* Update the sidebar to reflect the current step */
        frame.addSideBar(1);

        /* Add the forwarding instructions to the page */
        frame.addNextButton(new Label(getNextLayoutUID(), LiteralStrings.Singleton.get("FORWARD_INSTRUCTIONS", language), sizeVisitor));

        /* This selections panel will hold the languages. This is effectively cribbed from CandidatesModule */
        JPanel east = new JPanel();
        east.setLayout(new GridBagLayout());
        GridBagConstraints eastConstraints = new GridBagConstraints();

        eastConstraints.anchor = GridBagConstraints.SOUTH;
        eastConstraints.fill = GridBagConstraints.VERTICAL;
        int ycoord = 0;
        eastConstraints.gridy = ycoord;
        eastConstraints.gridx = 0;

        ycoord++;

        /* Title the language selection module */
        Label title = new Label(getNextLayoutUID(), LiteralStrings.Singleton.get("LANGUAGE_SELECT_TITLE", language));
        title.setWidth(LANG_SELECT_WIDTH);
        title.setBoxed(true);
        title.setCentered(true);
        title.setSize(title.execute(sizeVisitor));

        /* Put the module on a spacer and add it */
        Spacer PTitle = new Spacer(title, east);
        east.add(PTitle, eastConstraints);

        /* A label for instructing the voter on how to select a language */
        Label instLabel = new Label(getNextLayoutUID(), LiteralStrings.Singleton.get("LANGUAGE_SELECT_INSTRUCTIONS", language), sizeVisitor);

        /* link the instruction and title into navigation */
        instLabel.setNext(title);
        title.setPrevious(instLabel);

        /* Link the next button */
        instLabel.setRight(nextButton);

        /* Create a toggle button group for the languages */
        ToggleButtonGroup tbg = new ToggleButtonGroup("LanguageSelect");

        /* This will hold temporary information about a language button for navigation and placement */
        ALayoutComponent tempButton = null;

        /* Now add a button for every language passed in */
        for (Language lang : languages) {

            /* First create a new button */
            LanguageButton button = new LanguageButton(getNextLayoutUID(), lang.getName());

            /* Here we set up 3 column navigation just like selection pages */
            if(tempButton == null){
                title.setNext(button);
                title.setDown(button);
                button.setUp(title);
                button.setPrevious(title);

            } else{
                button.setPrevious(tempButton);
                button.setUp(tempButton);
                tempButton.setNext(button);
                tempButton.setDown(button);
            }

            button.setLeft(nextButton);
            button.setRight(nextButton);

            /* Set language, position, and size information for the button */
            button.setLanguage(lang);
            button.setWidth(LANG_SELECT_WIDTH);
            button.setIncreasedFontSize(true);
            button.setSize(button.execute(sizeVisitor));
            eastConstraints.gridy = ycoord++;
            eastConstraints.gridx = 0;

            /* Add the button to a spacer, the panel, and the button group */
            Spacer PDrawable = new Spacer(button, east);
            east.add(PDrawable, eastConstraints);
            tbg.getButtons().add(button);

            /* Update the placeholder button */
            tempButton = button;
        }

        /* Tie up the loose ends with navigation */
        assert tempButton != null;
        tempButton.setNext(nextButton);
        tempButton.setDown(nextButton);
        nextButton.setPrevious(tempButton);
        nextButton.setUp(tempButton);
        nextButton.setLeft(tempButton);

        /* Add the button group to the panel, the panel to the frame */
        east.add(new Spacer(tbg, east));
        frame.addAsEastPanel(east);

        /* reset the layout constraints */
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.fill = GridBagConstraints.VERTICAL;
        constraints.gridy = 1;
        constraints.gridx = 0;
        constraints.weightx = 1;
        constraints.weighty = 1;

        /* add the instructions */
        Spacer instspacer = new Spacer(instLabel, frame.north);
        frame.north.add(instspacer, constraints);

        /* Update the frame */
        frame.validate();
        frame.pack();

        /* Now create the page */
        Page page = new Page();

        /* Add the background information, if there is any */
        if (background != null) {
            page.getComponents().add(background);
            page.setBackgroundLabel(background.getUID());
        }

        /* Add the components to the frame */
        for (Component c : frame.getAllComponents()) {
            /* Position the component */
            Spacer s = (Spacer) c;
            s.updatePosition();

            /* Since we're adding the toggle button group of languages, we don't need to add the buttons themselves */
            if (!(s.getComponent() instanceof ToggleButton))
                page.getComponents().add(s.getComponent());
        }

        return page;
    }

    /**
     * @return A page to be returned when the supervisor attempts to cancel the voting session
     */
    @Override
    protected Page makeOverrideCancelPage() {

        /* The frame all the components will be laid out on*/
        PsychLayoutPanel frame = new PsychLayoutPanel();

        /* The title for the override cancel page */
        Label successTitle = new Label(getNextLayoutUID(), LiteralStrings.Singleton.get("OVERRIDE_CANCEL_TITLE", language));
        successTitle.setBold(true);
        successTitle.setCentered(true);
        successTitle.setSize(successTitle.execute(sizeVisitor));
        frame.addTitle(successTitle);

        /* Remove the left-portion of the frame */
        frame.remove(frame.west);

          /* Create a layout for the panel */
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.VERTICAL;
        c.gridy = 1;
        c.gridx = 0;
        c.weightx = 1;
        c.weighty = 1;

        /* The label containing the instructions for the override */
        Label reviewInstructions = new Label(getNextLayoutUID(), LiteralStrings.Singleton.get("OVERRIDE_CANCEL_INSTRUCTIONS", language), sizeVisitor);
        Spacer instspacer = new Spacer(reviewInstructions, frame.north);
        frame.north.add(instspacer, c);

        /* This panel will hold the buttons and information for the override selection */
        JPanel east = new JPanel();
        east.setLayout(new GridBagLayout());
        c = new GridBagConstraints();

        /* This button will allow the user to confirm the override and cancel the voting session */
        Button confirmBtn = new Button(getNextLayoutUID(), LiteralStrings.Singleton.get("OVERRIDE_CANCEL_CONFIRM", language), "OverrideCancelConfirm");
        confirmBtn.setIncreasedFontSize(true);
        confirmBtn.setSize(confirmBtn.execute(sizeVisitor));
        Spacer sp = new Spacer(confirmBtn, east);
        east.add(sp, c);

        /* This button will allow the user to reject the override and continue voting */
        Button denyBtn = new Button(getNextLayoutUID(), LiteralStrings.Singleton.get("OVERRIDE_DENY", language), "OverrideCancelDeny");
        denyBtn.setIncreasedFontSize(true);
        denyBtn.setSize(denyBtn.execute(sizeVisitor));
        sp = new Spacer(denyBtn, east);
        c.gridy = 1;
        c.insets = new Insets(50, 0, 0, 0);
        east.add(sp, c);

        /* Set keyboard navigation. Since there is only one column, there will be no left or right navigation. */
        reviewInstructions.setNext(confirmBtn);
        reviewInstructions.setDown(confirmBtn);
        confirmBtn.setPrevious(reviewInstructions);
        confirmBtn.setUp(reviewInstructions);
        confirmBtn.setNext(denyBtn);
        confirmBtn.setDown(denyBtn);
        denyBtn.setPrevious(confirmBtn);
        denyBtn.setUp(confirmBtn);

        /* Add the button panel */
        frame.addAsEastPanel(east);
        frame.validate();
        frame.pack();

        /* Create a new page with the simply background */
        Page page = new Page();

        /* TODO Do we need a null check here like we have with the regular background? */
        page.getComponents().add(simpleBackground);
        page.setBackgroundLabel(simpleBackground.getUID());

        /* Add the components to the page */
        for (Component co : frame.getAllComponents()) {
            Spacer s = (Spacer) co;
            s.updatePosition();
            page.getComponents().add(s.getComponent());
        }

        return page;
    }

    /**
     * @return a page indicating the voting session is being overridden by the supervisor with intent to commit and print the ballot
     */
    @Override
    protected Page makeOverrideCommitPage() {
        /* The frame that the components will be laid out on */
        PsychLayoutPanel frame = new PsychLayoutPanel();

        /* The title for this page */
        Label successTitle = new Label(getNextLayoutUID(), LiteralStrings.Singleton.get("OVERRIDE_COMMIT_TITLE", language));
        successTitle.setBold(true);
        successTitle.setCentered(true);
        successTitle.setSize(successTitle.execute(sizeVisitor));
        frame.addTitle(successTitle);

        /* Remove the left side of this panel */
        frame.remove(frame.west);

        /* Create a new layout for this page */
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.VERTICAL;
        c.gridy = 1;
        c.gridx = 0;
        c.weightx = 1;
        c.weighty = 1;

        /* The instructions for what to do on this page */
        Label reviewInstructions = new Label(getNextLayoutUID(), LiteralStrings.Singleton.get("OVERRIDE_COMMIT_INSTRUCTIONS", language), sizeVisitor);
        Spacer instspacer = new Spacer(reviewInstructions, frame.north);
        frame.north.add(instspacer, c);

        /* This will be the panel that holds the buttons for this screen */
        JPanel east = new JPanel();
        east.setLayout(new GridBagLayout());
        c = new GridBagConstraints();

        /* A button to confirm the override and commit and print the ballot */
        Button confirmBtn = new Button(getNextLayoutUID(), LiteralStrings.Singleton.get("OVERRIDE_COMMIT_CONFIRM", language), "OverrideCommitConfirm");
        confirmBtn.setIncreasedFontSize(true);
        confirmBtn.setSize(confirmBtn.execute(sizeVisitor));
        Spacer sp = new Spacer(confirmBtn, east);
        east.add(sp, c);

        /* A button to cancel the override process and continue voting */
        Button denyBtn = new Button(getNextLayoutUID(), LiteralStrings.Singleton.get("OVERRIDE_DENY", language), "OverrideCommitDeny");
        denyBtn.setIncreasedFontSize(true);
        denyBtn.setSize(denyBtn.execute(sizeVisitor));
        sp = new Spacer(denyBtn, east);
        c.gridy = 1;
        c.insets = new Insets(50, 0, 0, 0);
        east.add(sp, c);

        /* Since there are only two buttons, toggle between them, ignoring LEFT and RIGHT */
        reviewInstructions.setNext(confirmBtn);
        reviewInstructions.setDown(confirmBtn);
        confirmBtn.setPrevious(reviewInstructions);
        confirmBtn.setUp(reviewInstructions);
        confirmBtn.setNext(denyBtn);
        confirmBtn.setDown(denyBtn);
        denyBtn.setPrevious(confirmBtn);
        denyBtn.setUp(confirmBtn);

        /* Add the button panel */
        frame.addAsEastPanel(east);
        frame.validate();
        frame.pack();

        /* Create a new page with the simply background */
        Page page = new Page();

        /* TODO Do we need a null check here like we have with the regular background? */
        page.getComponents().add(simpleBackground);
        page.setBackgroundLabel(simpleBackground.getUID());

         /* Add the components to the page */
        for (Component co : frame.getAllComponents()) {
            Spacer s = (Spacer) co;
            s.updatePosition();
            page.getComponents().add(s.getComponent());
        }
        return page;
    }

    /**
     * @param ballot      the ballot, the collection of Cards
     * @param pageTargets mapping of races (Cards) to review pages
     * @return a review page, containing the all of the races and the voter's selection for each race
     */
    @Override
    protected ArrayList<Page> makeReviewPage(Ballot ballot, HashMap<Integer, Integer> pageTargets) {

        /* Since we could go on to several pages, we will keep track of them here */
    	ArrayList<Page> reviewPages = new ArrayList<>();

        /* the current position in the list of race review things */
    	int position = 0;

        /* starting value since there is always at least one review page. Increases if there is a need. */
    	int numReviewPages = 1;

        /* Build all of the review pages. */
    	for ( int reviewPageNum = 0; reviewPageNum < numReviewPages; reviewPageNum++) {
    		/* set up review frame */
    		PsychLayoutPanel frame = new PsychLayoutPanel();

            /* The title for each page */
    		Label reviewTitle = new Label(getNextLayoutUID(), LiteralStrings.Singleton.get("REVIEW_TITLE", language));
    		reviewTitle.setBold(true);
    		reviewTitle.setCentered(true);
    		reviewTitle.setSize(reviewTitle.execute(sizeVisitor));
    		frame.addTitle(reviewTitle);

            /* Update the sidebar to the appropriate step */
    		frame.addSideBar(3);

            /* If this is the first review page, create a button that goes back to the last race */
    		if (reviewPageNum == 0)
    			frame.addPreviousButton(new Label(getNextLayoutUID(), LiteralStrings.Singleton.get("BACK_LAST_RACE", language), sizeVisitor));

            /* Otherwise create a button that goes to the previous review page */
    		else
    			frame.addPreviousButton(new Label(getNextLayoutUID(), LiteralStrings.Singleton.get("PREVIOUS_PAGE_BUTTON", language), sizeVisitor));


            /* If this is the last page, add a button that goes to the commit/print confirmation page */
    		if (position == ballot.getCards().size() - 1) //last page, see bottom conditional
    			frame.addNextButton(new Label(getNextLayoutUID(), LiteralStrings.Singleton.get("FORWARD_RECORD", language), sizeVisitor));

            /* If its not, add a button that goes to the next review page */
    		else
    			frame.addNextButton(new Label(getNextLayoutUID(), LiteralStrings.Singleton.get("NEXT_PAGE_BUTTON", language), sizeVisitor));

            /* This is the instructions for how the review screen works */
            Label reviewInstructions = new Label(getNextLayoutUID(), LiteralStrings.Singleton.get("REVIEW_INSTRUCTIONS", language), sizeVisitor);

            /* Add the selection panel as the east panel */
    		JPanel east = new JPanel();
    		east.setLayout(new GridBagLayout());
    		GridBagConstraints c = new GridBagConstraints();
    		east.setLayout(new GridBagLayout());
    		c.gridx = 0;
    		c.gridy = 0;
    		c.anchor = GridBagConstraints.FIRST_LINE_START;
    		c.fill = GridBagConstraints.HORIZONTAL;
    		int align = 0;

            /* A counter to make sure we don't get columns that are too long */
    		int buttonsInColumn = 0;

            /* The upper bound on how many entries a column can have*/
    		int columnLength = (int)Math.ceil(ballot.getCards().size() / REVIEW_SCREEN_NUM_COLUMNS);

            /* Represents the button to the left of this one */
            ALayoutComponent tempLeftButton = null;

            /* Represents the button above this one */
            ALayoutComponent tempUpButton = null;

            /* Go through all of the cards and add a review selection for all of them */
    		for (int i = position; i < ballot.getCards().size(); i++) {

                /* Get the card for the current race we're creating a review entry for */
    			ACard card = ballot.getCards().get(i);

                /*
                 * The navigation here is somewhat tricky. We have a four-column setup, with instructions and
                 * titles in a sort of meta-column:
                 *
                 *                                          INSTRUCTIONS
                 *                                             TITLE
                 *                                RACENAME 1:    \    SELECTION 1
                 *                                RACENAME 2:    \    SELECTION 2
                 *                                RACENAME 3:    \    SELECTION 3
                 *                                               .
                 *                                               .
                 *                                               .
                 *                                RACENAME N:    \    SELECTION N
                 *
                 *                   PREVIOUS PAGE                                     NEXT PAGE
                 *
                 * In the cyclic model, we start with PREVIOUS PAGE, then go up to INSTRUCTIONS, and then to TITLE and
                 * next to RACENAME 1. From here, the next focused item is SELECTION 1, then RACENAME 1, and so on and
                 * so forth, snaking down to SELECTION N, then to NEXT PAGE and back to PREVIOUS PAGE. For the "back" direction,
                 * everything is reversed.
                 *
                 * In the 4-direction model, the LEFT-RIGHT navigation works in a cycle. Starting at PREVIOUS PAGE, RIGHT
                 * would go to RACENAME N then to SELECTION N then to NEXT PAGE. Going RIGHT or LEFT from the INSTRUCTIONS
                 * or TITLE items will go to NEXT PAGE and PREVIOUS PAGE, respectively. UP-DOWN navigation works like
                 * two parallel rings. Starting at RACENAME 1, UP will go to TITLE, then TITLE and INSTRUCTIONS, then to
                 * RACENAME N, and so on and so forth. To get to the other ring, LEFT or RIGHT must be selected. So, as
                 * per our example, from RACENAME 1, RIGHT would go to SELECTION 1 (and LEFT from SELECTION 1 would go
                 * back to RACENAME 1). From here, UP and DOWN would work like the did for RACENAME 1, only NEXT PAGE would
                 * be eventually reached. PREVIOUS PAGE cannot be reached through UP-DOWN navigation on the right-hand
                 * ring, and likewise, NEXT PAGE can't be reached on the left-hand ring.
                 */


                /* The name of the race (or proposition) that this is a review for */
    			ReviewButton raceNameButton = new ReviewButton(getNextLayoutUID(), card.getReviewTitle(language), "GoToPage", sizeVisitor);
    			raceNameButton.setBold(true);
    			raceNameButton.setBoxed(true);
    			raceNameButton.setWidth(REVIEW_SCREEN_WIDTH);
    			raceNameButton.setPageNum(pageTargets.get(position));

                /* The name of the selection the voter made and is currently reviewing */
    			ReviewButton selectionNameButton = new ReviewButton(card.getUID(), card.getReviewBlankText(language), "GoToPage", sizeVisitor);
    			selectionNameButton.setBoxed(true);
    			selectionNameButton.setWidth(REVIEW_SCREEN_WIDTH);
    			selectionNameButton.setPageNum(pageTargets.get(position));

                /* If this is the first button we're setting navigation up for, we have to add the instructions */
                if (tempUpButton == null) {
                    reviewInstructions.setNext(raceNameButton);
                    reviewInstructions.setRight(raceNameButton);
                    reviewInstructions.setDown(raceNameButton);
                    raceNameButton.setPrevious(reviewInstructions);
                    raceNameButton.setUp(reviewInstructions);
                    raceNameButton.setLeft(reviewInstructions);
                    selectionNameButton.setUp(reviewInstructions);
                }

                /* Otherwise tie in the previous button to this button's navigation*/
                else {
                    raceNameButton.setPrevious(tempUpButton);
                    raceNameButton.setUp(tempLeftButton);
                    selectionNameButton.setUp(tempUpButton);
                    tempLeftButton.setDown(raceNameButton);
                    tempUpButton.setDown(selectionNameButton);
                    tempUpButton.setNext(raceNameButton);
                }

                /* Tie up the loose ends for these two buttons*/
                raceNameButton.setNext(selectionNameButton);
                raceNameButton.setRight(selectionNameButton);
                raceNameButton.setLeft(previousButton);
                selectionNameButton.setLeft(previousButton);
                selectionNameButton.setPrevious(raceNameButton);
                selectionNameButton.setLeft(raceNameButton);
                selectionNameButton.setRight(nextButton);

                /* Add both new buttons to spacers, and add them to the frame */
    			Spacer rlSpacer = new Spacer(raceNameButton, east);
    			c.gridx = align;
    			east.add(rlSpacer, c);

    			Spacer rbSpacer = new Spacer(selectionNameButton, east);
    			c.gridx = c.gridx + 1;
    			east.add(rbSpacer, c);

                /* Increment the number of column entries (note, only by one) */
    			buttonsInColumn++;
    			c.gridy++;

                /* If the column is too large, create a new one and start the counter over */
    			if (buttonsInColumn > columnLength) {
    				buttonsInColumn = 0;
    				align++;
    				c.gridy = 0;
    				c.anchor = GridBagConstraints.FIRST_LINE_END;
    			}

                /* Update our position in the ballot */
    			position++;

                /* If we have exceeded the number of races allowable on a card, stop */
                /* TODO This seems bad. */
    			if (i % CARDS_PER_REVIEW_PAGE >= CARDS_PER_REVIEW_PAGE - 1)	break;

                /* Update the temporary buttons */
                tempLeftButton = raceNameButton;
                tempUpButton = selectionNameButton;

    		}

            /* Tie up the loose ends on button navigation */
            previousButton.setUp(reviewInstructions);
            previousButton.setNext(reviewInstructions);
            reviewInstructions.setPrevious(previousButton);
            reviewInstructions.setLeft(previousButton);
            reviewInstructions.setRight(nextButton);
            nextButton.setPrevious(tempUpButton);
            nextButton.setLeft(tempUpButton);
            nextButton.setUp(tempUpButton);

            assert tempUpButton != null;
            tempUpButton.setNext(nextButton);
            tempUpButton.setDown(nextButton);

            //noinspection ConstantConditions
            assert tempLeftButton != null;
            tempLeftButton.setDown(nextButton);

            /* Add the button panel */
    		frame.addAsEastPanel(east);

    		/* Add instructions to the page */
    		GridBagConstraints constraints = new GridBagConstraints();
    		constraints.anchor = GridBagConstraints.NORTH;
    		constraints.fill = GridBagConstraints.VERTICAL;
    		constraints.gridy = 1;
    		constraints.gridx = 0;
    		constraints.weightx = 1;
    		constraints.weighty = 1;
    		Spacer instspacer = new Spacer(reviewInstructions, frame.north);
    		frame.north.add(instspacer, constraints);

            /* Refresh the frame */
    		frame.validate();
    		frame.pack();

    		/* Create a new page */
    		Page cardPage = new Page();
    		cardPage.getComponents().add(background);
    		cardPage.setBackgroundLabel(background.getUID());

            /* Add the components to the page */
            Component[] componentsArray = frame.getAllComponents();
    		for (Component cmp : componentsArray) {
                /* Position the components */
    			int componentHeight = cmp.getHeight();
                Spacer s = (Spacer) cmp;
                s.updatePosition();

                /* If the component is a presidential race label or a presidential race selection it needs to be shifted vertically */
                if (componentHeight == PRESIDENTIAL_RACE_LABEL_COMPONENT_HEIGHT) {
                    s.getComponent().setYPos(s.getComponent().getYPos());
                    cardPage.getComponents().add(s.getComponent());
                    continue;
                }

                /*
                 * Shift everything down except the button labels.
                 * They are descriptions of the buttons on the current page and they should remain where they are.
                 * Normally, the review page card would contain components that have UIDs that alternate between L and B.
                 * The only two exceptions are the button labels:
                 *      The first button label comes after a B but before an L.
                 *      The second button label comes after an L but before an L.
                 *      No other component meets these conditions.
                 */

                cardPage.getComponents().add(s.getComponent());
            }

            /* Add the page to the pages list */
    		reviewPages.add(cardPage);

            /* Increment the count of pages */
    		if (position < ballot.getCards().size())
    			numReviewPages++;
    	}
    	
    	return reviewPages;
    }

    /**
     * Makes a simple background (without the sidebar). Used on override pages
     * and the success page.
     * @return the success page background
     */
    protected Background makeSimpleBackground() {
        PsychLayoutPanel frame = new PsychLayoutPanel();

        Label instructionsTitle = new Label("L0", LiteralStrings.Singleton.get(
                "INSTRUCTIONS_TITLE", language));
        instructionsTitle.setCentered(true);
        instructionsTitle.setSize(instructionsTitle.execute(sizeVisitor));
        frame.addTitle(instructionsTitle);
        frame.addSideBar(1);
        frame.addNextButton(nextInfo);

        JPanel east = new JPanel();
        east.setLayout(new GridBagLayout());
        Label instrLabel = new Label(getNextLayoutUID(), LiteralStrings.Singleton.get(
                "INSTRUCTIONS", language), sizeVisitor);

        nextButton.setPrevious(instrLabel);
        nextButton.setLeft(instrLabel);
        instrLabel.setNext(nextButton);
        instrLabel.setRight(nextButton);

        Spacer sp = new Spacer(instrLabel, east);
        east.add(sp);
        frame.addAsEastPanel(east);

        frame.validate();
        frame.pack();

        BufferedImage image = new BufferedImage(WINDOW_WIDTH, WINDOW_HEIGHT,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D graphs = (Graphics2D) image.getGraphics();
        graphs.setColor(Color.WHITE);
        graphs.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        graphs.setColor(Color.PINK);
        graphs.fillRect(0, 0, WINDOW_WIDTH, frame.north.getHeight());
        return new Background(getNextLayoutUID(), image);
    }

    //#ifdef NONE_OF_ABOVE
//    protected Page makeNoSelectionPage(int target) {
//        PsychLayoutPanel frame = new PsychLayoutPanel();
//          successTitle = new  (getNextLayoutUID(),
//                LiteralStrings.Singleton.get("NO_SELECTION_TITLE", language));
//        successTitle.setBold(true);
//        successTitle.setCentered(true);
//        successTitle.setSize(successTitle.execute(sizeVisitor));
//        frame.addTitle(successTitle);
//        frame.remove(frame.west);
//
//        JPanel east = new JPanel();
//        east.setLayout(new GridBagLayout());
//          instrLabel = new  (getNextLayoutUID(),
//                LiteralStrings.Singleton.get("NO_SELECTION", language), sizeVisitor);
//        Spacer sp = new Spacer(instrLabel, east);
//        east.add(sp);
//        frame.addAsEastPanel(east);
//
//          returnLbl = new  (getNextLayoutUID(), LiteralStrings.Singleton
//                .get("RETURN_RACE", language), sizeVisitor);
//        frame.addReturnButton(returnLbl, target);
//
//        frame.validate();
//        frame.pack();
//
//        Page page = new Page();
//        page.getComponents().add(simpleBackground);
//        page.setBackgroundLabel(simpleBackground.getUID());
//
//        ALayoutComponent button = null;
//        ALayoutComponent tempButton = null;
//
//        for (Component c : frame.getAllComponents()) {
//            Spacer s = (Spacer) c;
//            s.updatePosition();
//            page.getComponents().add(s.getComponent());
//            button = s.getComponent();
//
//            if(button instanceof ToggleButton){
//                if(tempButton == null){
//                    button.setPrevious(previousButton);
//                    previousButton.setNext(button);
//
//                }else{
//                    button.setPrevious(tempButton);
//                    tempButton.setNext(button);
//                }
//
//                tempButton = button;
//
//            }
//        }
//
//        //If the temporary button is still null at this point that means the
//        //page contains no ToggleButtons
//        if(tempButton != null){
//            tempButton.setNext(nextButton);
//            nextButton.setPrevious(tempButton);
//        }
//
//        return page;
//    }
    //#endif

    @Override
    protected Page makeSuccessPage() {
        PsychLayoutPanel frame = new PsychLayoutPanel();
        Label successTitle = new Label(getNextLayoutUID(),
                LiteralStrings.Singleton.get("SUCCESS_TITLE", language));
        successTitle.setBold(true);
        successTitle.setCentered(true);
        successTitle.setSize(successTitle.execute(sizeVisitor));
        frame.addTitle(successTitle);
        frame.remove(frame.west);

        JPanel east = new JPanel();
        east.setLayout(new GridBagLayout());
        Label instrLabel = new Label(getNextLayoutUID(),
                LiteralStrings.Singleton.get("SUCCESS", language), sizeVisitor);
        Spacer sp = new Spacer(instrLabel, east);
        east.add(sp);
        frame.addAsEastPanel(east);

        frame.validate();
        frame.pack();

        Page page = new Page();
        page.getComponents().add(simpleBackground);
        page.setBackgroundLabel(simpleBackground.getUID());

        for (Component c : frame.getAllComponents()) {
            Spacer s = (Spacer) c;
            s.updatePosition();
            page.getComponents().add(s.getComponent());
        }

        return page;
    }

    protected Page makeProvisionalSuccessPage() {
        PsychLayoutPanel frame = new PsychLayoutPanel();
        Label successTitle = new Label(getNextLayoutUID(),
                LiteralStrings.Singleton.get("SUCCESS_TITLE", language));
        successTitle.setBold(true);
        successTitle.setCentered(true);
        successTitle.setSize(successTitle.execute(sizeVisitor));
        frame.addTitle(successTitle);
        frame.remove(frame.west);

        JPanel east = new JPanel();
        east.setLayout(new GridBagLayout());
        Label instrLabel = new Label(getNextLayoutUID(),
                LiteralStrings.Singleton.get("PROVISIONAL", language), sizeVisitor);
        Spacer sp = new Spacer(instrLabel, east);
        east.add(sp);
        frame.addAsEastPanel(east);

        frame.validate();
        frame.pack();

        Page page = new Page();
        page.getComponents().add(simpleBackground);
        page.setBackgroundLabel(simpleBackground.getUID());

//        ALayoutComponent button = null;
//        ALayoutComponent tempButton = null;

//        previousButton.setNext(instrLabel);
//        instrLabel.setPrevious(previousButton);

        for (Component c : frame.getAllComponents()) {
            Spacer s = (Spacer) c;
            s.updatePosition();
            page.getComponents().add(s.getComponent());
//            button = s.getComponent();

//            if(button instanceof ToggleButton){
//                if(tempButton == null){
//                    button.setPrevious(instrLabel);
//                    instrLabel.setNext(button);
//
//                }else{
//                    button.setPrevious(tempButton);
//                    tempButton.setNext(button);
//                }
//
//                tempButton = button;
//
//            }
        }

        //If the temporary button is still null at this point that means the
        //page contains no ToggleButtons
//        if(tempButton != null){
//            tempButton.setNext(nextButton);
//            nextButton.setPrevious(tempButton);
//        }

        return page;
    }

}
