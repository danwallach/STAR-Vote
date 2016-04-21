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

package votebox.middle.ballot;

import crypto.PlaintextRaceSelection;

import java.util.List;

/**
 * Use an instance of this interface to make queries on the ballot.
 * 
 * @author Kyle
 * 
 */
public interface IBallotLookupAdapter {


	/**
	 * Call this method to get the s-expression representation of the ballot.
	 * 
	 * @return This method returns an s-expression representation of the ballot.
	 */
	public List<PlaintextRaceSelection> inRaceSelectionForm();

	/**
	 * Call this method to get the number of selections that have currently been
	 * made in the target ballot. By "selections" here, we mean the number of
	 * card elements that belong to this ballot that have selections on them at
	 * the time of the method call (these selections cannot be "no selection").
	 * 
	 * @return This method returns the number of selections that have been made
	 *         on the ballot associated with this adapter.
	 */
	public int numSelections();
	
	/**
	 * Call this method to determine the structure and contents of the various races in this ballot.
	 * 
	 * @return a List of the groups of race-ids that make up each race.  Used to construct NIZKs.
	 */
	public List<List<String>> getRaceGroups();

    public List<String> getTitles();

}
