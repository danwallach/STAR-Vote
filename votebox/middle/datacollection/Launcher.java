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

package votebox.middle.datacollection;

import auditorium.IAuditoriumParams;
import auditorium.IKeyStore;
import printer.Printer;
import sexpression.ListExpression;
import votebox.middle.driver.Driver;
import votebox.middle.view.AWTViewFactory;

import java.io.*;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

/**
 * This is a launcher for the vote system. Use this launcher in order to do
 * human factors type testing.
 * @author Kyle
 */
public class Launcher {

    private static final File SettingsFile = new File("settings");

    /**
     * This is the gui for this launcher.
     */
    private LauncherView _view = null;

    /**
     * This is the votebox that is currently running.
     */
    private Driver _voteBox = null;

    private Printer printer = null;

    private static File dest;
    private static File tempDir;

    /**
     * Launch the votebox software after doing some brief sanity checking. These
     * checks won't catch everything but they will catch enough problems caused
     * by simple accidents.
     *
     * @param ballotLocation    the location of the ballot. (zip)
     * @param logDir            the directory that log files should be written out
     *                          to. (dir)
     * @param logFilename       This is the desired filename for the log file.
     * @param debug             parameter passed to AWTViewFactory to determine
     *                          windowed/full screen mode.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void launch(final String ballotLocation, String logDir, String logFilename,
                       boolean debug, final String vvpat, final int vvpatWidth,
			           final int vvpatHeight, final int printableWidth, final int printableHeight){

        File baldir;

        /* Unzip the ballot to a temporary directory and delete recursively on exit */
        try {

            /* Set up the ballot directory */
            baldir = new File(ballotLocation.substring(0, ballotLocation.lastIndexOf(".")));
            dest = new File(System.getProperty("user.dir") + "/tmp/ballots/ballot");
            dest.delete();
            baldir.delete();
            baldir.mkdirs();
            dest.mkdirs();

            /* Unzip the ballot to the ballot directory */
            Driver.unzip(ballotLocation, baldir.getAbsolutePath());

            /* TODO filepath construction? */
            /* Copy the directory to dest and new ballot file to a dest zip  */
            copyFolder(baldir, dest);
            copyFolder(new File(ballotLocation), new File(dest.getAbsolutePath() + ".zip"));

            /* Delete recursively on exit */
            Driver.deleteRecursivelyOnExit(baldir.getAbsolutePath());
            Driver.deleteRecursivelyOnExit(dest.getAbsolutePath());

        } catch (IOException e) { e.printStackTrace(); return; }

		File logdir = new File(logDir);
		File logfile = new File(logdir, logFilename);

        /* Check that ballot location is a directory. */
		if (!baldir.isDirectory()) {
			_view.statusMessage("Supplied 'ballot location' is not a directory.", "Please make sure that you select a" +
                                " directory which contains a ballot configuration file and media directory. Do not select a file.");
			return;
		}

		/* Check that it has the cfg file. */
		if (!Arrays.asList(baldir.list()).contains("ballotbox.cfg")) {
			_view.statusMessage("Supplied 'ballot location' does not contain the file 'ballotbox.cfg'", "Please specify" +
                                " a valid ballot.zip or ballot directory.");
			return;
		}

		/* Check that the log directory is actually a directory */
		if (!logdir.isDirectory()) {
			_view.statusMessage("Supplied 'log directory' is not a directory.", "Please make sure that you select a" +
                                " directory\nfor 'log directory' field. Do not select a file.");
			return;
		}

		/* Check that the user actually specified a log filename. */
		if (logFilename.equals("")) {
			_view.statusMessage("Log Filename blank.", "Please specify a log filename.");
			return;
		}

		/* Check that the log file does not already exist. If it exists, notify the user that stuff will be appended to the end. */
		if (logfile.exists()) {

            boolean overwrite = _view.askQuestion("Supplied 'log file' exists", "If you choose to continue, event data" +
                                                  " will be overwritten in file: " + logfile.getName());
			if (!overwrite) return;
		}

		/* Set the data logger and launch. */
		DataLogger.init(logfile);

		save(ballotLocation, logDir, logFilename);

        /* TODO test if necessary */
        _voteBox = null;
        System.gc();
        /* ---------------------- */

		_voteBox = new Driver(System.getProperty("user.dir") + "/tmp/ballots/ballot", new AWTViewFactory(debug, false), true);

		final Driver vbcopy = _voteBox;

        tempDir = baldir;

        _view.setRunning(true);
		new Thread(new Runnable() {

			public void run() {

				final IAuditoriumParams constants = new IAuditoriumParams() {

                    public String       getReportAddress()               { return null;  }
                    public String       getRuleFile()                    { return null;  }
                    public String       getElectionName()                { return null;  }
					public String       getBroadcastAddress()            { return null;  }
                    public String       getEloTouchScreenDevice()        { return null;  }
                    public String       getLogLocation()                 { return null;  }
                    public String       getPrinterForVVPAT()             { return vvpat; }

                    public boolean      getAllowUIScaling()              { return true;  }
                    public boolean      getUseWindowedView()             { return true;  }
                    public boolean      getCastBallotEncryptionEnabled() { return false; }
					public boolean      getUseEloTouchScreen()           { return false; }
					public boolean      getEnableNIZKs()                 { return false; }
					public boolean      getUsePiecemealEncryption()      { return false; }
					public boolean      getUseSimpleTallyView()          { return false; }
					public boolean      getUseTableTallyView()           { return false; }

                    public int          getDefaultSerialNumber()         { return 0; }
                    public int          getDiscoverPort()                { return 0; }
                    public int          getDiscoverReplyPort()           { return 0; }
                    public int          getDiscoverReplyTimeout()        { return 0; }
                    public int          getDiscoverTimeout()             { return 0; }
                    public int          getPort()                        { return 0; }

                    @Override
                    public String getIncrementalRuleFile() {
                        return null;
                    }

                    public int          getListenPort()                  { return 0; }
                    public int          getJoinTimeout()                 { return 0; }
                    public int          getViewRestartTimeout()          { return 1; }
                    public int          getPaperHeightForVVPAT()         { return vvpatHeight;     }
                    public int          getPaperWidthForVVPAT()          { return vvpatWidth;      }
                    public int          getPrintableHeightForVVPAT()     { return printableHeight; }
                    public int          getPrintableWidthForVVPAT()      { return printableWidth;  }

                    public IKeyStore    getKeyStore()                    { return null; }
				};


		        /* Register for the cast ballot event, and "review page encountered" event */
				vbcopy.run(new Observer(){

                    ListExpression _lastSeenBallot;

					public void update(Observable o, Object arg){

						Object[] obj = (Object[])arg;

						/* Do nothing if this is before rendering the screen... */
						if(!((Boolean)obj[0]))
							return;

						ListExpression ballot = (ListExpression) obj[1];

                        /* Set up the printer */
                        printer = new Printer(new File(dest.getAbsolutePath() + ".zip"), _voteBox.getBallotAdapter().getRaceGroups(), true);

                        /* Print a bogus ballot if not the last seen ballot? TODO check what this does / is supposed to do */
                        if(ballot != _lastSeenBallot)
                            printer.printCommittedBallot(ballot, "9999999999");

                        printer.printedReceipt("9999999999");

                        _lastSeenBallot = ballot;

					}
				},

				new Observer() {

					public void update(Observable o, Object arg) {

                        Object[] obj = (Object[])arg;
                        ListExpression ballot = (ListExpression)obj[1];

						vbcopy.getView().nextPage();
					}
				});

				_view.setRunning(true);

            }

        }).start();
	}

    public void kill() {
        _voteBox.kill();
        _view.setRunning(false);
    }

    /**
     * Call this method to run the launcher.
     */
    public void run() {
        _view = new LauncherView(this);
        load();
        _view.setRunning(false);
        _view.setVisible(true);
    }

    /**
     * Load the state of the fields from disk.
     */
    private void load() {

        String ballot, logdir, logfile;

        if (SettingsFile.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(SettingsFile));
                logdir = reader.readLine();
                ballot = reader.readLine();
                logfile = reader.readLine();
            }
            catch (Exception e) { return; }
            _view.setFields(logdir, ballot, logfile);
        }
    }

    /**
     * Save the state of the fields to disk.
     */
    private void save(String ballot, String logdir, String logfile) {

        try {

            PrintWriter writer = new PrintWriter(new FileWriter(SettingsFile));
            writer.write(logdir + "\n");
            writer.write(ballot + "\n");
            writer.write(logfile + "\n");
            writer.close();

        } catch (Exception e) { e.printStackTrace(); }

    }

    /**
     * Useful methods for getting printing in the launcher to work, from http://www.mkyong.com/java/how-to-copy-directory-in-java/
     * @param src   the source folder
     * @param dest  the destination folder
     *
     * @throws IOException
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void copyFolder(File src, File dest) throws IOException{

        /* Check if src is a folder */
        if(src.isDirectory()){

            /* If dest doesn't exist, create it */
            if(!dest.exists()) dest.mkdir();

            /* List all the directory contents */
            String files[] = src.list();

            /* Deal with file structure for all the files */
            for (String file : files) {

                /* Construct the src and dest file structure */
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);

                /* Recursive copy */
                copyFolder(srcFile,destFile);
            }

        }
        else {

            /* If file, then copy it - Use bytes stream to support all file types */
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dest);

            byte[] buffer = new byte[1024];

            int length;

            /* Copy the file content in bytes */
            while ((length = in.read(buffer)) > 0) out.write(buffer, 0, length);

            /* Close the streams */
            in.close();
            out.close();
        }
    }


    public static void main(String[] args) {
        new Launcher().run();
    }
}
