package supervisor.model.test;

import crypto.adder.AdderPrivateKeyShare;
import crypto.adder.AdderPublicKey;
import crypto.adder.AdderPublicKeyShare;
import junit.framework.TestCase;
import sexpression.ASEConverter;
import sexpression.ASExpression;
import sexpression.ListExpression;
import supervisor.model.AdderKeyManipulator;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by Matthew Kindy II on 6/19/2015.
 */
public class AdderKeyManipulatorTest extends TestCase {

    /**
     * Test case setup
     */
    protected void setUp() throws Exception {

        super.setUp();

    }

    /* Want to test PEK gen and also spew keyshares to files so that
    * we can use them for election run-throughs later */
    public void testProcedure() throws Exception {

        /* Load the seed key */
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Keys", "key");
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
            AdderPublicKeyShare seedKey = ASEConverter.convertFromASE((ListExpression) seedKeyASE);

            AdderKeyManipulator.setSeedKey(seedKey);
        }
        catch (Exception e) { throw new RuntimeException("Couldn't use the key file");}

        /* Have 3 logins to generate keypairs*/
        AdderPrivateKeyShare a1prks = AdderKeyManipulator.generateAuthorityKeySharePair("1");
        AdderPrivateKeyShare a2prks = AdderKeyManipulator.generateAuthorityKeySharePair("2");
        AdderPrivateKeyShare a3prks = AdderKeyManipulator.generateAuthorityKeySharePair("3");

        AdderKeyManipulator.generateAuthorityPolynomialValues("1");
        AdderKeyManipulator.generateAuthorityPolynomialValues("2");
        AdderKeyManipulator.generateAuthorityPolynomialValues("3");

        a1prks = AdderKeyManipulator.generateRealPrivateKeyShare("1");
        a2prks = AdderKeyManipulator.generateRealPrivateKeyShare("2");
        a3prks = AdderKeyManipulator.generateRealPrivateKeyShare("3");

        AdderPublicKey PEK = AdderKeyManipulator.generatePublicEncryptionKey();

        System.out.println("Public Encryption Key: " + PEK);





    }
}
