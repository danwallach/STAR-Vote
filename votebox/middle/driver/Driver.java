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

import votebox.middle.IBallotVars;
import java.io.*;
import java.util.*;
import java.util.List;

/* TODO: Documentation by knowledgeable member */

public class Driver {

	private String _path;
	private ViewManager _view;

    /**
     * Constructor for the driver
     */
	public Driver() {}

	/**
	 * Loads the path of the ballot
	 * @param path path of ballot configuration and xml file
     */
	public void loadPath(String path){
		_path = path;
	}

	public void launchView(Observer reviewScreenObserver, Observer castBallotObserver) {
		/* Set up the view */
		_view = new ViewManager();

        /* Register for cast ballot in the view */
		if(castBallotObserver != null)
			_view.registerForCastBallot(castBallotObserver);

        /* Register for review screen in the view */
		if(reviewScreenObserver != null)
			_view.registerForReview(reviewScreenObserver);

		/* Execute */
		_view.run();
	}

	public void launchView() {
		launchView(null, null);
	}

    /**
     * Sets up to parse the ballot format and begin voting process
     */
	public void run() {

		IBallotVars vars;

        /* Load the ballot configuration file */
		try { vars = new GlobalVarsReader(_path).parse(); }
        catch (IOException e) {

			System.err.println("The ballot's configuration file could not be found.");
			e.printStackTrace();
			return;

		}

	}


    /**
     * Terminates the view.
     */
	public void kill() { _view.kill(); }
    
    /**
     * Gets this VoteBox instance's view.  Used to allow the caller to register for
     * the cast ballot event in the view manager.
     * @return the view manager
     */
    public ViewManager getView() { return _view; }

}

