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

package votebox.middle.driver;

import crypto.PlaintextRaceSelection;
import votebox.middle.ballot.IBallotLookupAdapter;

import java.util.List;

/**
 * Other classes that make use of the ViewManager should program to this
 * abstraction, and not to the concrete ViewManager class. This allows for much
 * easier testing.
 * 
 * @author Kyle
 * 
 */
public interface IViewManager {

    /**
     * Commit the ballot.
     */
    void commitBallot(List<PlaintextRaceSelection> ballot,
                      List<List<String>> raceGroups,
                      List<String> raceTitles);

    /**
     * Cast the ballot.
     */
    void castCommittedBallot(List<PlaintextRaceSelection> ballot,
                             List<List<String>> raceGroups);

    /**
     * Fired when the override-cancel operation is confirmed on the booth.
     */
    public void overrideCancelConfirm();
    
    /**
     * Fired when the override-cancel operation is denied from the booth.
     */
    public void overrideCancelDeny();
    
    /**
     * Fired when the override-cast operation is confirmed on the booth.
     */
    public void overrideCommitConfirm(List<PlaintextRaceSelection> ballot,
                                      List<List<String>> raceGroups,
                                      List<String> raceTitles);
    
    /**
     * Fired when the override-cast operation is confirmed on the booth.
     */
    public void overrideCommitDeny();

}
