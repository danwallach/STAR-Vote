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

package votebox.middle.view.widget;

import java.util.ArrayList;

import votebox.middle.IncorrectTypeException;
import votebox.middle.Properties;
import votebox.middle.driver.IAdapter;
import votebox.middle.view.IViewManager;

public class ToggleButtonGroup {

	/**
	 * These are the buttons that claim to be a member of this
	 * ToggleButtonGroup.
	 */
	private ArrayList<ToggleButton> _buttons = new ArrayList<ToggleButton>();

	/**
	 * This is the strategy that this ToggleButtonGroup is delegating to.
	 */
	private AToggleButtonGroupStrategy _strategy = null;

	/**
	 * These are the properties for the group that were parsed from the XML.
	 */
	private Properties _properties;

	/**
	 * This is the public constructor for ToggleButtonGroup.
	 * 
	 * @param properties        the properties that were parsed from the layout XML.
	 */
	public ToggleButtonGroup(Properties properties) {
		_properties = properties;
	}

	/**
	 * Call this method to set the strategy that this button group delegates to
	 * based on what is set in this button group's properties.
	 * 
	 * @param viewManagerAdapter    a reference to the viewManager
	 */
	public void setStrategy(IViewManager viewManagerAdapter, IAdapter ballotAdapter) throws UnknownStrategyException {

        /* Make sure the strategy is currently null */
		if(_strategy == null) {

            /* Make sure there's a strategy in properties */
            if (_properties.contains(Properties.TOGGLE_BUTTON_GROUP_STRATEGY)) {

                try {

                    /* Set the strategy based on the strategy in properties */
                    String strategy = _properties.getString(Properties.TOGGLE_BUTTON_GROUP_STRATEGY);

                    switch (strategy) {

                        case "Race":
                            _strategy = new Race(ballotAdapter);
                            break;

                        case "LanguageSelect":
                            _strategy = new LanguageSelect(viewManagerAdapter);
                            break;

                        default:
                            throw new UnknownStrategyException(strategy);
                    }
                }
                catch (IncorrectTypeException e) {
                    throw new UnknownStrategyException(_properties.getObject(Properties.TOGGLE_BUTTON_GROUP_STRATEGY) + "(which is not of type string)");
                }

            } /* If there isn't a strategy in properties, throw an exception */
            else { throw new UnknownStrategyException("undefined"); }
        }
	}

	/**
	 * Toggle buttons should invoke this method when they want to select
	 * themselves. The group should delegate to its strategy here.
	 * 
	 * @param context       the button that would like to select itself.
	 */
	public void select(ToggleButton context){
		_strategy.select(context);
	}

	/**
	 * Toggle buttons should invoke this method when they want to deselect
	 * themselves. The group should delegate to its strategy here.
	 *
     * @param context       the button that would like to deselect itself.
     * @param playSound     whether or not a sound should be played
     */
	public void deselect(ToggleButton context, boolean playSound){
		_strategy.deselect(context, playSound);
	}

	/**
	 * This is the getter for _buttons
	 * 
	 * @return      _buttons.
	 */
	public ArrayList<ToggleButton> getButtons() {
		return _buttons;
	}

	/**
	 * This is the getter for _properties
	 * 
	 * @return      _properties.
	 */
	public Properties getProperties() {
		return _properties;
	}
}
