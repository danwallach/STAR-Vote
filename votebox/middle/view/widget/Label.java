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
 * ACCURACY, COMPLETENESS, AND NON-INFRINGEMENT.  THE SOFTWARE USER SHALL
 * INDEMNIFY, DEFEND AND HOLD HARMLESS RICE UNIVERSITY AND ITS FACULTY,
 * STAFF AND STUDENTS FROM ANY AND ALL CLAIMS, ACTIONS, DAMAGES, LOSSES,
 * LIABILITIES, COSTS AND EXPENSES, INCLUDING ATTORNEYS' FEES AND COURT
 * COSTS, DIRECTLY OR INDIRECTLY ARISING OUR OF OR IN CONNECTION WITH
 * ACCESS OR USE OF THE SOFTWARE.
 */

package votebox.middle.view.widget;

import votebox.middle.IBallotVars;
import votebox.middle.Properties;
import votebox.middle.ballot.IBallotLookupAdapter;
import votebox.middle.driver.IAdapter;
import votebox.middle.view.IDrawable;
import votebox.middle.view.IViewFactory;
import votebox.middle.view.IViewImage;
import votebox.middle.view.IViewManager;
import votebox.middle.view.MeaninglessMethodException;
import votebox.middle.view.RenderPage;

import java.io.File;

/**
 * This is the simplest implementation of an IDrawable. Labels are simply window
 * decoration. Labels cannot be interacted with by the voter.
 */

public class Label implements IDrawable {

    private final String _uniqueID;
    private final Properties _properties;

    private RenderPage _parent;
    private int _x;
    private int _y;

    private IViewImage _image;
    private IViewImage _reviewImage, _focusedReviewImage;

    protected IViewFactory _factory;
    protected IViewManager _viewManager;
    protected IBallotVars _vars;
    protected IBallotLookupAdapter _ballot;

    /**
     * This is the public constructor for Label.
     *
     * @param uid           the drawable's UID.
     * @param properties    the properties that were defined for this label.
     */
    public Label(String uid, Properties properties) {
        _uniqueID = uid;
        _properties = properties;
    }

    /**
     * Call this method to set the parent of this label.
     */
    public void setParent(RenderPage page) {
        _parent = page;
    }

    /**
     * Call this method to get the parent of this label.
     *
     * @return      the parent of this label.
     */
    protected RenderPage getParent() {
        return _parent;
    }

    /**
     * @see votebox.middle.view.IDrawable#getImage()
     */
    public IViewImage getImage() {

        if (_image == null) {

            String suffix = _uniqueID.equals("reviewtitle") ? Integer.toString(_ballot.numSelections()) :  "";
            String imgPath = imagePath(_vars, _uniqueID + suffix, _viewManager.getSize(), _viewManager.getLanguage());

            _image = _factory.makeImage(imgPath, false);
        }

        return _image;
    }

    /**
     * @see votebox.middle.view.IDrawable#getImage()
     */
    public IViewImage getReviewImage() {

        if (_reviewImage == null) {
            String imgPath = imagePath(_vars, _uniqueID + "_review", _viewManager.getSize(), _viewManager.getLanguage());
            _reviewImage = _factory.makeImage(imgPath, false);
        }

        return _reviewImage;
    }

    /**
     * Returns the focused review image of the label
     */
    public IViewImage getFocusedReviewImage(){

        if(_focusedReviewImage == null) {
            String imgPath = imagePath(_vars, _uniqueID + "_review_focused", _viewManager.getSize(), _viewManager.getLanguage());
            _focusedReviewImage = _factory.makeImage(imgPath, false);
        }

        return _focusedReviewImage;
    }

    /**
     * This is the getter for _unqiueID.
     *
     * @return      _uniqueID
     */
    public String getUniqueID() {
        return _uniqueID;
    }

    /**
     * This is the getter for _properties.
     *
     * @return      _properties
     */
    public Properties getProperties() {
        return _properties;
    }

    /**
     * Call this method to get the x-coordinate at which this drawable should be
     * drawn.
     *
     * @return      the x-coordinate at which this drawable should be drawn.
     */
    public int getX() {
        return _x;
    }

    /**
     * Call this method to set the x-coordinate at which this drawable should be
     * drawn.
     *
     * @param x     the x-coordinate at which this drawable should be drawn.
     */
    public void setX(int x) {
        _x = x;
    }

    /**
     * Call this method to get the y-coordinate at which this drawable should be
     * drawn.
     *
     * @return      the y-coordinate at which this drawable should be drawn.
     */
    public int getY() {
        return _y;
    }

    /**
     * Call this method to set the y-coordinate at which this drawable should be
     * drawn.
     *
     * @param y     the y-coordinate at which this drawable should be drawn.
     */
    public void setY(int y) {
        _y = y;
    }

    public ToggleButtonGroup getGroup() throws MeaninglessMethodException {
        throw new MeaninglessMethodException( "Labels do not define a group." );
    }

    /**
     * The location on disk that the image wrappers will use to do the loading
     * of the images depends on the uid, size, language. There is *one* special
     * case here: if the uid is "reviewtitle", append the number of selections
     * the ballot thinks have been made onto the uid. Therefore, if there have
     * been 4 selections made in the ballot, the uid of this element should be
     * interpreted as "reviewtitle4". Because we need a reference to the ballot
     * lookup adapter in order to check the number of selections, we must insert
     * this behavior here.
     * TODO someone knowledgeable comment params
     */
    public void initFromViewManager(IViewManager viewManagerAdapter, IBallotLookupAdapter ballotLookupAdapter,
                                    IAdapter ballotAdapter, IViewFactory factory, IBallotVars ballotVars) {
        _factory = factory;
        _vars = ballotVars;
        _ballot = ballotLookupAdapter;
        _viewManager = viewManagerAdapter;
    }

    /**
     * Construct the full path to an image given several parameters.
     *
     * @param vars      the vars object that has the ballot bath.
     * @param uid       the image's unique id
     * @param size      the image's size index
     * @param lang      the image's language abbreviation.
     * @return          the path to the image.
     */
    protected String imagePath(IBallotVars vars, String uid, int size, String lang) {
        String folder;

        if(uid.contains("_"))
            folder = uid.substring(0, uid.indexOf("_"));
        else
            folder = uid;

        return vars.getBallotPath() + File.separator + "media" + File.separator + folder + File.separator + uid + "_" + lang + ".png";
    }

    /**
     * Construct the full path to an image given several paramters.
     * It loads an image to represent the selected options (except No Selection)
     * for the review page.
     *
     * @param vars      the vars object that has the ballot bath.
     * @param uid       the image's unique id
     * @param lang      the image's language abbreviation.
     * @return          the path to the image.
     */
    protected String imageToggleButtonPath (IBallotVars vars, String uid, String lang) {
        String folder;

        if(uid.contains("_"))
            folder = uid.substring(0, uid.indexOf("_"));
        else
            folder = uid;

        return vars.getBallotPath() + File.separator + "media" + File.separator + folder + File.separator + uid + "_" + lang + ".png";
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return _uniqueID;
    }
}