import com.avaje.ebean.Ebean;
import crypto.adder.AdderInteger;
import crypto.adder.AdderPublicKeyShare;
import play.Application;
import play.GlobalSettings;
import play.libs.Yaml;
import sexpression.ASEParser;
import sexpression.ASExpression;
import sexpression.ListExpression;
import utilities.AdderKeyManipulator;
import utilities.BallotLoader;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


/**
 * Created with IntelliJ IDEA.
 * User: mrdouglass95
 * Date: 7/23/13
 * Time: 2:24 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 * Implementation of a startup task for initializing ballot files
 */
public class Global extends GlobalSettings {

    /**
     * Play Start-up task. Calls method BallotLoader.init()
     */
    @Override
    public void onStart(Application app) {
               
       AdderPublicKeyShare pks = new AdderPublicKeyShare(AdderInteger.ONE,AdderInteger.ONE,AdderInteger.ONE);

        System.out.println(pks.getClass().getName());


        System.out.println("Initializing the Ballot Loader");
        BallotLoader.init();
        if (models.User.find.findRowCount() == 0) {
            Ebean.save((List) Yaml.load("initial-data.yml"));
        }

        /* Load the seed key */
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(".key files", "key");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(null);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            System.out.println("You chose to open this file: " +
                    chooser.getSelectedFile().getName());
        }

        File seedKeyFile = chooser.getSelectedFile();
        Path seedKeyPath = seedKeyFile.toPath();



        try {
            byte[] verbatimSeedKey = Files.readAllBytes(seedKeyPath);
            ASExpression seedKeyASE = ASExpression.makeVerbatim(verbatimSeedKey);
            System.out.println(seedKeyASE);
            AdderPublicKeyShare seedKey = ASEParser.convertFromASE((ListExpression)seedKeyASE);

            AdderKeyManipulator.setSeedKey(seedKey);
        }
        catch (Exception e) { e.printStackTrace(); throw new RuntimeException("Couldn't use the key file");}



    }
}
