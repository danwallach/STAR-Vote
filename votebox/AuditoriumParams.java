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

package votebox;

import auditorium.IAuditoriumParams;
import votebox.middle.IVoteboxConstants;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class reads constants from a configuration file specified by client software. It reads in configuration
 * constants from a file with the following format: </br></br>
 *
 * #comment description of constant</br>
 * CONSTANT_NAME</br>
 * constantValue</br>
 * ... more ...</br>
 * .............</br>
 *
 * @author kyle
 * 
 */
public class AuditoriumParams implements IAuditoriumParams,
        IVoteboxConstants {

    public static final AuditoriumParams Singleton = new AuditoriumParams();

    public static final int DISCOVER_TIMEOUT = 4000;
    public static int DISCOVER_PORT = 9782;
    public static final int DISCOVER_REPLY_TIMEOUT = 4000;
    public static final int DISCOVER_REPLY_PORT = 9783;
    public static final int LISTEN_PORT = 9700;
    public static final int JOIN_TIMEOUT = 1000;
    public static final String BROADCAST_ADDRESS = "255.255.255.255";
    public static final String LOG_LOCATION = "log/log.out";
    public static final String KEYS_DIRECTORY = "keys/";

    public static final String VIEW_IMPLEMENTATION = "AWT"; //Changed from SDL
    public static final String RULE_FILE = "rules/STARVoting.rules";
    public static final String INCREMENTAL_RULE_FILE = "rules/STARVotingIncremental.rules";
    
    /* Default for cast_ballot_encryption_enabled. */
    public static final boolean CAST_BALLOT_ENCRYPTION_ENABLED = true;

    /* If true will attempt to use SDL to enable the touchscreen.  Should not be set if not using SDL view. */
    public static final boolean USE_ELO_TOUCH_SCREEN = false;

    /* The path to use to the Elo touchscreen if USE_ELO_TOUCH_SCREEN is true */
    public static final String ELO_TOUCH_SCREEN_DEVICE = null;

    /* Amount of time before restart */
    public static final int VIEW_RESTART_TIMEOUT = 5000;


    /* Default for default serial number, -1 indicates that it MUST be specified explicitly somehow */
    public static final int DEFAULT_SERIAL_NUMBER = -1;

    /* Default report address, used exclusively by tap.  if "", must be specified explicitly somehow. */
    public static final String DEFAULT_REPORT_ADDRESS = "";

    /* Default server port, used by web server and tap.  If -1, must be specified explicitly somehow. */
    public static final int DEFAULT_PORT = 9700;

    /* Default http port, used by web server. */
    public static final int DEFAULT_HTTP_PORT = 8080;

    /* Default ballot file.  If "", must be specified explicitly somehow. */
    public static final String DEFAULT_BALLOT_FILE = System.getProperty("user.dir") + "/tmp/ballot";

    /* Default printer for VVPAT. If "", do not use VVPAT. */
    public static final String PRINTER_FOR_VVPAT = "";

    /* Default page size for VVPAT.  Based off of Star TPS800 model printer. */
    public static final int PAPER_WIDTH_FOR_VVPAT = 249;
    public static final int PAPER_HEIGHT_FOR_VVPAT = 322;

    /* Default imageable area for VVPAT.  Based off of Star TPS800 model printer. */
    public static final int PRINTABLE_WIDTH_FOR_VVPAT = 239;
    public static final int PRINTABLE_HEIGHT_FOR_VVPAT = 311;

    /* Configurable margins, may not be needed? */
    public static final int PRINTABLE_VERTICAL_MARGIN  = 25;
    public static final int PRINTABLE_HORIZONTAL_MARGIN  = 25;

    /* Configurable DPI */
    public static final int PRINTER_DEFAULT_DPI = 300;
    public static final int JAVA_DEFAULT_DPI = 72;

    /* By default, we don't enable NIZKs. */
    public static final boolean ENABLE_NIZKS = true;

    /* By default, we don't enable Piecemeal Encryption */
    public static final boolean USE_PIECEMEAL_ENCRYPTION = false;

    /* By default, we use the "fanciest" tally view possible */
    public static final boolean USE_SIMPLE_TALLY_VIEW = false;
    public static final boolean USE_TABLE_TALLY_VIEW = false;

    /* AWT view is windowed by default (as it may break if fullscreen */
    public static final boolean DEFAULT_USE_WINDOWED_VIEW = false;

    public static final boolean DEFAULT_ALLOW_UI_SCALING = true;

    public static final String ELECTION_NAME = "Rice University General Election";

    /* Setting which determines whether ballots will be printed using two columns */
    public static final boolean PRINT_USE_TWO_COLUMNS = true;

    /* Settings for the ballotScanner */
    public static final boolean USE_SCAN_CONFIRMATION_SOUND = false;
    public static final String SCAN_CONFIRMATION_SOUND_PATH = "sound/test.mp3";

    private final HashMap<String, String> _config;

    /* Settings for printing */
    public static final String PRINT_COMMANDS_FILE_FILENAME = "CommandsFile.txt";
    public static final String PRINT_COMMANDS_FILE_PARAMETER_SEPARATOR = "!!!";

    public static final String DEFAULT_OPERATING_SYSTEM = "Windows";

    public static final boolean DEFAULT_SHUFFLE_CANDIDATE_ORDER = false;

    /* Position of the center of the screen (used for GUIs) */
    public static final int DEFAULT_SCREEN_CENTER_X = 680;
    public static final int DEFAULT_SCREEN_CENTER_Y = 384;

    /**
     * Reads configuration from the path and converts it into a HashMap(String:String).
     *
     * @param path      the path from which the configuration is read
     */
    public AuditoriumParams(String path) {

        _config = new HashMap<>();
        read(path);
    }

    /**
     * Default constructor simply constructs an empty HashMap(String:String)
     */
    private AuditoriumParams() {
        _config = new HashMap<>();
    }

    /**
     * Checks the HashMap to see if it contains an entry for the broadcast address
     * and, if so, returns it.
     *
     * @return      the address to which to broadcast
     */
    public String getBroadcastAddress() {

        if (_config.containsKey("BROADCAST_ADDRESS"))
            return _config.get("BROADCAST_ADDRESS");

        return BROADCAST_ADDRESS;
    }

    /**
     * Checks the HashMap to see if it contains an entry for the used port
     * and, if so, returns it.
     *
     * @return      the identifier of the port to be used
     */
    public int getDiscoverPort() {

        if (_config.containsKey("DISCOVER_PORT"))
            return Integer.parseInt(_config.get("DISCOVER_PORT"));

        return DISCOVER_PORT;
    }

    /**
     * Checks the HashMap to see if it contains an entry for the reply port
     * to be used and, if so, returns it.
     *
     * @return      the identifier of the reply port to be used
     */
    public int getDiscoverReplyPort() {

        if (_config.containsKey("DISCOVER_REPLY_PORT"))
            return Integer.parseInt(_config.get("DISCOVER_REPLY_PORT"));

        return DISCOVER_REPLY_PORT;
    }

    /**
     * Checks the HashMap to see if it contains an entry for the time to
     * wait for a reply before timing out and, if so, returns it.
     *
     * @return      the amount of time to wait for a reply before timing out
     */
    public int getDiscoverReplyTimeout() {

        if (_config.containsKey("DISCOVER_REPLY_TIMEOUT"))
            return Integer.parseInt(_config.get("DISCOVER_REPLY_TIMEOUT"));

        return DISCOVER_REPLY_TIMEOUT;
    }

    /**
     * Checks the HashMap to see if it contains an entry for the time to wait
     * before timing out and, if so, returns it.
     *
     * @return      the amount of time to wait before timing out
     */
    public int getDiscoverTimeout() {

        if (_config.containsKey("DISCOVER_TIMEOUT"))
            return Integer.parseInt(_config.get("DISCOVER_TIMEOUT"));

        return DISCOVER_TIMEOUT;
    }

    /**
     * Checks the HashMap to see if it contains an entry for the time to wait
     * during joining before timing out and, if so, returns it.
     *
     * @return      the amount of time to wait during joining before timing out
     */
    public int getJoinTimeout() {

        if (_config.containsKey("JOIN_TIMEOUT"))
            return Integer.parseInt(_config.get("JOIN_TIMEOUT"));

        return JOIN_TIMEOUT;
    }

    /**
     * Checks the HashMap to see if it contains an entry for the listening port
     * and, if so, returns it.
     *
     * @return      the identifier of the listening port
     */
    public int getListenPort() {

        if (_config.containsKey("LISTEN_PORT"))
            return Integer.parseInt(_config.get("LISTEN_PORT"));

        return LISTEN_PORT;
    }

    /**
     * Checks the HashMap to see if it contains an entry for the filename of the
     * print commands file and, if so, returns it.
     *
     * @return      the filename of the print commands file
     */
    public String getCommandsFileFilename() {

        if (_config.containsKey( "PRINT_COMMANDS_FILE_FILENAME"))
            return _config.get( "PRINT_COMMANDS_FILE_FILENAME");

        return PRINT_COMMANDS_FILE_FILENAME;
    }

    /**
     * Checks the HashMap to see if it contains an entry for the parameter separator
     * of the print commands file and, if so, returns it.
     *
     * @return      the parameter separator
     */
    public String getCommandsFileParameterSeparator() {

        if (_config.containsKey("PRINT_COMMANDS_FILE_PARAMETER_SEPARATOR"))
            return _config.get("PRINT_COMMANDS_FILE_PARAMETER_SEPARATOR");

        return PRINT_COMMANDS_FILE_PARAMETER_SEPARATOR;
    }

    /**
     * Checks the HashMap to see if it contains an entry for the log location
     * and, if so, returns it.
     *
     * @return      the location of the log
     */
    public String getLogLocation() {

        if (_config.containsKey("LOG_LOCATION"))
            return _config.get("LOG_LOCATION");

        return LOG_LOCATION;
    }

    /**
     * Checks the HashMap to see if it contains an entry for the view implementation
     * and, if so, returns it.
     *
     * @return      the view implementation
     */
    public String getViewImplementation() {

        if (_config.containsKey("VIEW_IMPLEMENTATION"))
            return _config.get("VIEW_IMPLEMENTATION");

        return VIEW_IMPLEMENTATION;
    }

    /**
     * Checks the HashMap to see if it contains an entry for the keys directory
     * and, if so, queries auditorium with that entry and returns the result as
     * an @IKeyStore.
     *
     * @return      the simple key store in auditorium that corresponds to the keys
     *              directory entry in the HashMap
     */
    public auditorium.IKeyStore getKeyStore() {

        String kd = KEYS_DIRECTORY;

        if (_config.containsKey("KEYS_DIRECTORY"))
            kd =_config.get("KEYS_DIRECTORY");

        return new auditorium.SimpleKeyStore(kd);
    }

    /**
     * Checks the HashMap to see if it contains an entry for the rule file
     * and, if so, returns it.
     *
     * @return      the rule file
     */
    public String getRuleFile() {

        if (_config.containsKey("RULE_FILE"))
            return _config.get("RULE_FILE");

        return RULE_FILE;
    }

    /**
     * Checks the HashMap to see if it contains an entry for whether the cast
     * ballot encryption is enabled and, if so, returns it.
     *
     * @return      whether the cast ballot encryption is enabled
     */
	public boolean getCastBallotEncryptionEnabled() {

		if (_config.containsKey("CAST_BALLOT_ENCRYPTION_ENABLED"))
			return Boolean.parseBoolean(_config.get("CAST_BALLOT_ENCRYPTION_ENABLED"));

		return CAST_BALLOT_ENCRYPTION_ENABLED;
	}

    /**
     * Checks the HashMap to see if it contains an entry for whether the elo touch
     * screen is to be used and, if so, returns it.
     *
     * @return      whether the elo touch screen is to be used
     */
	public boolean getUseEloTouchScreen() {

		if (_config.containsKey("USE_ELO_TOUCH_SCREEN"))
			return Boolean.parseBoolean(_config.get("USE_ELO_TOUCH_SCREEN"));

		return USE_ELO_TOUCH_SCREEN;
	}

    /**
     * Checks the HashMap to see if it contains an entry for the elo touch screen
     * device and, if so, returns it.
     *
     * @return      the identifier of the elo touch screen device
     */
	public String getEloTouchScreenDevice() {

        if (_config.containsKey("ELO_TOUCH_SCREEN_DEVICE"))
            return _config.get("ELO_TOUCH_SCREEN_DEVICE");

        return ELO_TOUCH_SCREEN_DEVICE;
    }

    /**
     * Checks the HashMap to see if it contains an entry for the time before
     * view restart times out and, if so, returns it.
     *
     * @return      the time before view restart times out
     */
	public int getViewRestartTimeout() {

		if (_config.containsKey("VIEW_RESTART_TIMEOUT"))
			return Integer.parseInt(_config.get("VIEW_RESTART_TIMEOUT"));

		return VIEW_RESTART_TIMEOUT;
	}

    /**
     * Checks the HashMap to see if it contains an entry for the default serial
     * number and, if so, returns it.
     *
     * @return      the default serial number
     */
	public int getDefaultSerialNumber() {

		if (_config.containsKey("DEFAULT_SERIAL_NUMBER"))
			return Integer.parseInt(_config.get("DEFAULT_SERIAL_NUMBER"));

		return DEFAULT_SERIAL_NUMBER;
	}

    /**
     * Checks the HashMap to see if it contains an entry for the default report
     * address and, if so, returns it.
     *
     * @return      the default report address
     */
	public String getReportAddress() {

		if (_config.containsKey("DEFAULT_REPORT_ADDRESS"))
			return _config.get("DEFAULT_REPORT_ADDRESS");

		return DEFAULT_REPORT_ADDRESS;
	}

    /**
     * Checks the HashMap to see if it contains an entry for the printer for
     * VVPAT and, if so, returns it.
     *
     * @return      the identifier of the printer for VVPAT
     */
    public String getPrinterForVVPAT() {

		if (_config.containsKey("PRINTER_FOR_VVPAT"))
			return _config.get("PRINTER_FOR_VVPAT");

		return PRINTER_FOR_VVPAT;
	}

    /**
     * Checks the HashMap to see if it contains an entry for the paper height
     * setting for VVPAT and, if so, returns it.
     *
     * @return      the paper height setting for VVPAT
     */
	public int getPaperHeightForVVPAT() {

		try {
			if (_config.containsKey("PAPER_HEIGHT_FOR_VVPAT"))
				return Integer.parseInt(_config.get("PAPER_HEIGHT_FOR_VVPAT"));
		}
        catch (NumberFormatException e) { e.printStackTrace(); }

		return PAPER_HEIGHT_FOR_VVPAT;
	}

    /**
     * Checks the HashMap to see if it contains an entry for the paper width
     * setting for VVPAT and, if so, returns it.
     *
     * @return      the paper width setting for VVPAT
     */
	public int getPaperWidthForVVPAT() {

		try {
			if (_config.containsKey("PAPER_WIDTH_FOR_VVPAT"))
				return Integer.parseInt(_config.get("PAPER_WIDTH_FOR_VVPAT"));
		}
        catch (NumberFormatException e) { e.printStackTrace(); }

		return PAPER_WIDTH_FOR_VVPAT;
	}

    /**
     * Checks the HashMap to see if it contains an entry for the printable height
     * setting for VVPAT and, if so, returns it.
     *
     * @return      the printable height setting for VVPAT
     */
	public int getPrintableHeightForVVPAT() {

		try {
			if (_config.containsKey("PRINTABLE_HEIGHT_FOR_VVPAT"))
				return Integer.parseInt(_config.get("PRINTABLE_HEIGHT_FOR_VVPAT"));
		}
        catch (NumberFormatException e) { e.printStackTrace(); }

		return PRINTABLE_HEIGHT_FOR_VVPAT;
	}

    /**
     * Checks the HashMap to see if it contains an entry for the printable width
     * setting for VVPAT and, if so, returns it.
     *
     * @return      the printable width setting for VVPAT
     */
	public int getPrintableWidthForVVPAT() {

		try {
			if (_config.containsKey("PRINTABLE_WIDTH_FOR_VVPAT"))
				return Integer.parseInt(_config.get("PRINTABLE_WIDTH_FOR_VVPAT"));
		}
        catch (NumberFormatException e) { e.printStackTrace(); }

		return PRINTABLE_WIDTH_FOR_VVPAT;
	}

    /**
     * Checks the HashMap to see if it contains an entry for the printable vertical
     * margin setting and, if so, returns it.
     *
     * @return      the printable vertical margin setting
     */
    public int getPrintableVerticalMargin() {

        try {
            if (_config.containsKey("PRINTABLE_VERTICAL_MARGIN"))
                return Integer.parseInt(_config.get("PRINTABLE_VERTICAL_MARGIN"));
        }
        catch (NumberFormatException e) { e.printStackTrace(); }

        return PRINTABLE_VERTICAL_MARGIN;
    }

    /**
     * Checks the HashMap to see if it contains an entry for the printable horizontal
     * margin setting and, if so, returns it.
     *
     * @return      the printable horizontal margin setting
     */
    public int getPrintableHorizontalMargin() {

        try {
            if (_config.containsKey("PRINTABLE_HORIZONTAL_MARGIN"))
                return Integer.parseInt(_config.get("PRINTABLE_HORIZONTAL_MARGIN"));
        }
        catch (NumberFormatException e) { e.printStackTrace(); }

        return PRINTABLE_HORIZONTAL_MARGIN;
    }

    /**
     * Checks the HashMap to see if it contains an entry for the printer default DPI
     * setting and, if so, returns it.
     *
     * @return      the printer default DPI setting
     */
    public int getPrinterDefaultDpi() {

        try {
            if (_config.containsKey("PRINTER_DEFAULT_DPI"))
                return Integer.parseInt(_config.get("PRINTER_DEFAULT_DPI"));
        }
        catch (NumberFormatException e) { e.printStackTrace(); }

        return PRINTER_DEFAULT_DPI;
    }

    /**
     * Checks the HashMap to see if it contains an entry for the java default DPI
     * setting and, if so, returns it.
     *
     * @return      the java default DPI setting
     */
    public int getJavaDefaultDpi() {

        try {
            if (_config.containsKey("JAVA_DEFAULT_DPI"))
                return Integer.parseInt(_config.get("JAVA_DEFAULT_DPI"));
        }
        catch (NumberFormatException e) { e.printStackTrace(); }

        return JAVA_DEFAULT_DPI;
    }

    /**
     * Checks the HashMap to see if it contains an entry for whether NIZKs are
     * enabled and, if so, returns it.
     *
     * @return      whether NIZKs are enabled
     */
    public boolean getEnableNIZKs() {

		if (_config.containsKey("ENABLE_NIZKS"))
			return Boolean.parseBoolean(_config.get("ENABLE_NIZKS"));

		return ENABLE_NIZKS;
	}

    /**
     * Checks the HashMap to see if it contains an entry for whether piecemeal
     * encryption is to be used and, if so, returns it.
     *
     * @return      whether piecemeal encryption is to be used
     */
	public boolean getUsePiecemealEncryption() {

		if (_config.containsKey("USE_PIECEMEAL_ENCRYPTION"))
			return Boolean.parseBoolean(_config.get("USE_PIECEMEAL_ENCRYPTION"));

		return USE_PIECEMEAL_ENCRYPTION;
	}

    /**
     * Checks the HashMap to see if it contains an entry for whether simple tally
     * view is to be used and, if so, returns it.
     *
     * @return      whether simple tally view is to be used
     */
	public boolean getUseSimpleTallyView() {

		if (_config.containsKey("USE_SIMPLE_TALLY_VIEW"))
			return Boolean.parseBoolean(_config.get("USE_SIMPLE_TALLY_VIEW"));

		return USE_SIMPLE_TALLY_VIEW;
	}

    /**
     * Checks the HashMap to see if it contains an entry for whether table tally
     * view is to be used and, if so, returns it.
     *
     * @return      whether table tally view is to be used
     */
    public boolean getUseTableTallyView() {

    	if (_config.containsKey("USE_TABLE_TALLY_VIEW"))
			return Boolean.parseBoolean(_config.get("USE_TABLE_TALLY_VIEW"));

		return USE_TABLE_TALLY_VIEW;
    }

    /**
     * Checks the HashMap to see if it contains an entry for whether windowed view
     * is to be used and, if so, returns it.
     *
     * @return      whether windowed view is to be used
     */
    public boolean getUseWindowedView() {

    	if (_config.containsKey("USE_WINDOWED_VIEW"))
    		return Boolean.parseBoolean(_config.get("USE_WINDOWED_VIEW"));

    	return DEFAULT_USE_WINDOWED_VIEW;
    }

    /**
     * Checks the HashMap to see if it contains an entry for whether UI scaling is
     * to be allowed and, if so, returns it.
     *
     * @return      whether UI scaling is to be allowed
     */
    public boolean getAllowUIScaling() {

    	if (_config.containsKey("ALLOW_UI_SCALING"))
    		return Boolean.parseBoolean(_config.get("ALLOW_UI_SCALING"));

    	return DEFAULT_ALLOW_UI_SCALING;
    }

    /**
     * Checks the HashMap to see if it contains an entry for the election name
     * and, if so, returns it.
     *
     * @return      the election name
     */
    public String getElectionName() {

        if (_config.containsKey("ELECTION_NAME"))
            return _config.get("ELECTION_NAME");

        return ELECTION_NAME;
    }

    /**
     * Checks the HashMap to see if it contains an entry for the server port
     * and, if so, returns it.
     *
     * @return      the identifier of the server port
     */
    @Override
    public int getPort() {

        if (_config.containsKey("SERVER_PORT"))
            return Integer.parseInt(_config.get("SERVER_PORT"));

        return DEFAULT_PORT;
    }

    @Override
    public String getIncrementalRuleFile() {
        return INCREMENTAL_RULE_FILE;
    }

    /**
     * Checks the HashMap to see if it contains an entry for whether the order in
     * which the candidates are shown is to be shuffled and, if so, returns it.
     *
     * @return      whether the order in which the candidates are shown is to be shuffled
     */
    public boolean shuffleCandidates() {

        if (_config.containsKey("SHUFFLE_CANDIDATE_ORDER"))
            return Boolean.parseBoolean(_config.get("SHUFFLE_CANDIDATE_ORDER"));

        return DEFAULT_SHUFFLE_CANDIDATE_ORDER;
    }

    /**
     * Checks the HashMap to see if it contains an entry for whether two columns are
     * to be used during printing and, if so, returns it.
     *
     * @return      whether two columns are to be used during printing
     */
    public boolean getUseTwoColumns() {

        if (_config.containsKey("PRINT_USE_TWO_COLUMNS"))
            return Boolean.parseBoolean(_config.get("PRINT_USE_TWO_COLUMNS"));

        return PRINT_USE_TWO_COLUMNS;
    }

    /**
     * Checks the HashMap to see if it contains an entry for whether a scan confirmation
     * sound is to be used and, if so, returns it.
     *
     * @return      whether a scan confirmation sound is to be used
     */
    public boolean useScanConfirmationSound() {

        if (_config.containsKey("USE_SCAN_CONFIRMATION_SOUND"))
            return Boolean.parseBoolean(_config.get("USE_SCAN_CONFIRMATION_SOUND"));

        return USE_SCAN_CONFIRMATION_SOUND;
    }

    /**
     * Checks the HashMap to see if it contains an entry for the filepath of the scan
     * confirmation sound and, if so, returns it.
     *
     * @return      the filepath of the scan confirmation sound
     */
    public String getConfirmationSoundPath() {

        if (_config.containsKey("SCAN_CONFIRMATION_SOUND_PATH"))
            return _config.get("SCAN_CONFIRMATION_SOUND_PATH");

        return SCAN_CONFIRMATION_SOUND_PATH;
    }

    /**
     * Checks the HashMap to see if it contains an entry for the operating system to be
     * used and, if so, returns it.
     *
     * @return      the operating system to be used
     */
    public String getOS() {

        if (_config.containsKey("OPERATING_SYSTEM"))
            return _config.get("OPERATING_SYSTEM");

        return DEFAULT_OPERATING_SYSTEM;
    }

    /**
     * Checks the HashMap to see if it contains an entry for the ballot file
     * and, if so, returns it.
     *
     * @return      the ballot file
     */
    public String getBallotFile() {

        if (_config.containsKey("BALLOT_FILE"))
            return _config.get("BALLOT_FILE");

        return DEFAULT_BALLOT_FILE;
    }

    /**
     * Checks the HashMap to see if it contains an entry for the screen centre
     * x-coordinate and, if so, returns it.
     *
     * @return      the x-coordinate of the centre of the screen
     */
    public int getScreenCenterX() {

        if (_config.containsKey("SCREEN_CENTER_X"))
            return Integer.parseInt(_config.get("SCREEN_CENTER_X"));

        return DEFAULT_SCREEN_CENTER_X;
    }

    /**
     * Checks the HashMap to see if it contains an entry for the screen centre
     * y-coordinate and, if so, returns it.
     *
     * @return      the y-coordinate of the centre of the screen
     */
    public int getScreenCenterY() {

        if (_config.containsKey("SCREEN_CENTER_Y"))
            return Integer.parseInt(_config.get("SCREEN_CENTER_Y"));

        return DEFAULT_SCREEN_CENTER_Y;
    }


    /**
     * Reads the configuration file from the path given and converts
     * each section into an entry in the HashMap.
     *
     * @param path      the file path of the configuration file to be read
     */
    private void read(String path) {

        try {

            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path))));

            ArrayList<String> content = new ArrayList<>();
            String temp;

            /* Read until the end of the file is reached */
            while ((temp = reader.readLine()) != null) {

                /* Trim off extra whitespace */
            	temp = temp.trim();

            	/* Read lines that start with "#" as comments, and thus ignore */
            	if (temp.startsWith("#")) continue;

            	/* Ignore the rest of the line if it is a comment */
            	if (temp.indexOf('#') > -1) temp = temp.substring(0, temp.indexOf('#'));

                /* Trim again to deal with any whitespace caused by comments */
            	if(temp.length() > 0)
            		content.add(temp.trim());
            }

            /* Throw a runtime exception if the file is not well constructed */
            if (content.size() % 2 == 1)
            	throw new RuntimeException("Couldn't parse the configuration file.", null);

            /* Associate together adjacent entries in content and put into the _config HashMap
               as a mapped pairing */
            for (int i = 0; i < content.size(); i += 2)
            	_config.put(content.get(i), content.get(i + 1));
        }
        catch (IOException e) { System.err.println("Couldn't parse the configuration file, using defaults: "+ e.getMessage()); }
    }
}
