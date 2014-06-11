package preptool.model.layout;

import java.awt.*;

/**
 * This class represents a button which will never be shown, but renders the appropriate text for printing
 * a Print button on a VVPAT
 *
 * @author Matt Bernhard
 */
public class PrintButton extends Button {

    /** The text of this PrintButton  */
    private String text;

    /** The second line of this PrintButton (used in Presidential Races)  */
    private String secondLine = "";

    /** The party text of this PrintButton (used for candidates) */
    private String party = "";

    /** Whether this PrintButton has bold text */
    private boolean bold;

    /** Whether this PrintButton has increased font size */
    private boolean increasedFontSize;

    /**
     * Constructs a PrintButton
     *
     * @param uid       the unique ID
     * @param text      the text
     */
    public PrintButton(String uid, String text) {
        super(uid, text);
        setBackgroundColor(Color.WHITE);

    }

    /**
     * Constructs a PrintButton with the given unique ID, text, strategy, and
     * size visitor that determines and sets the size.
     *
     * @param uid       the unique ID
     * @param text      the text
     * @param sizeVisitor       the size visitor
     */
    public PrintButton(String uid, String text, ILayoutComponentVisitor<Object,Dimension> sizeVisitor) {
        this(uid, text);
        this.text = text;
        setSize(execute(sizeVisitor));
    }
    
    /**
     * Calls the forPrintButton method in visitor
     *
     * @see preptool.model.layout.ALayoutComponent#execute(ILayoutComponentVisitor, Object[])
     */
    @Override
    public <P,R> R execute(ILayoutComponentVisitor<P,R> visitor, P... param) {
        return visitor.forPrintButton(this, param);
    }


    /**
     * @return the party
     */
    public String getParty() {
        return party;
    }

    /**
     * @return the secondLine
     */
    public String getSecondLine() {
        return secondLine;
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @return if the Print button is bold
     */
    public boolean isBold() {
        return bold;
    }

    /**
     * @return if the Print button has increasedFontSize
     */
    public boolean isIncreasedFontSize() {
        return increasedFontSize;
    }

    /**
     * @param bold the bold to set
     */
    public void setBold(boolean bold) {
        this.bold = bold;
    }

    /**
     * @param increasedFontSize the increasedFontSize to set
     */
    public void setIncreasedFontSize(boolean increasedFontSize) {
        this.increasedFontSize = increasedFontSize;
    }

    /**
     * @param party the party to set
     */
    public void setParty(String party) {
        this.party = party;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text){
        this.text = text;
    }

    /**
     * @param secondLine the secondLine to set
     */
    public void setSecondLine(String secondLine) {
        this.secondLine = secondLine;
    }

    /**
     * @return the String representation of this PrintButton
     */
    @Override
    public String toString() {
        return "PrintButton[text=" + text + ",x=" + xPos + ",y=" + yPos
                + ",width=" + width + ",height=" + height + "]";
    }
}
