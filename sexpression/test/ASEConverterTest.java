package sexpression.test;

import crypto.PlaintextRaceSelection;
import crypto.adder.AdderPublicKeyShare;
import junit.framework.TestCase;
import sexpression.ASEConverter;
import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.StringWildcard;
import supervisor.model.AdderKeyManipulator;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Matthew Kindy II on 4/16/2015.
 */
public class ASEConverterTest extends TestCase{

    private Map<String,Integer> rsMap = new HashMap<>();
    private PlaintextRaceSelection p;
    private PlaintextRaceSelection pNew;

    protected void setUp() throws Exception {

        int i = 2;

        rsMap.put("Matt K", 0);
        rsMap.put("Matt B", 1);
        rsMap.put("Clayton", 0);

        p = new PlaintextRaceSelection(rsMap,"myRaceSelection",1);
        pNew = new PlaintextRaceSelection(null, "myNewRaceSelection",1);

    }

    public void testFromASE(){

        int i = 2;
        ASExpression iExp = ASEConverter.convertToASE(i);
        int j = ASEConverter.convertFromASE((ListExpression) iExp);
        System.out.println(i + " " + iExp + " " + j);
        assertEquals(i, j);


        StringWildcard s = StringWildcard.SINGLETON;

        ListExpression sExp = ASEConverter.convertToASE(s);
        StringWildcard s2 = ASEConverter.convertFromASE(sExp);
        assertEquals(s,s2);

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
            System.out.println(seedKeyASE);
            AdderPublicKeyShare seedKey = ASEConverter.convertFromASE((ListExpression) seedKeyASE);

            AdderKeyManipulator.setSeedKey(seedKey);
        }
        catch (Exception e) { throw new RuntimeException("Couldn't use the key file");}


    }

    public void testToASE(){

        String expected = "(object java.util.HashMap (object sexpression.KeyValuePair (key java.lang.String Matt B) " +
                          "(value java.lang.Integer 1)) (object sexpression.KeyValuePair (key java.lang.String Matt K) " +
                          "(value java.lang.Integer 0)) (object sexpression.KeyValuePair (key java.lang.String Clayton) " +
                          "(value java.lang.Integer 0)))";


        ListExpression rsExp = ASEConverter.convertToASE(rsMap);

        assertEquals(expected, rsExp.toString());

        System.out.println("Expected: " + expected);
        System.out.println("Returned: " + rsExp);

        Map rs = ASEConverter.convertFromASE(rsExp);

        assertEquals(rsMap, rs);

        System.out.println("Expected: " + rsMap);
        System.out.println("Returned: " + rs);

        expected = "(object crypto.PlaintextRaceSelection (voteMap java.util.HashMap (object sexpression.KeyValuePair " +
                "(key java.lang.String Matt B) (value java.lang.Integer 1)) (object sexpression.KeyValuePair " +
                "(key java.lang.String Matt K) (value java.lang.Integer 0)) (object sexpression.KeyValuePair " +
                "(key java.lang.String Clayton) (value java.lang.Integer 0))) (title java.lang.String myRaceSelection) " +
                "(size java.lang.Integer 1))";

        ListExpression prs = ASEConverter.convertToASE(p);

        assertEquals(expected, prs.toString());

        System.out.println("Expected: " + expected);
        System.out.println("Returned: " + prs);

        expected = "(object java.util.HashMap (object sexpression.KeyValuePair (key NULL) (value java.lang.Integer 0)) " +
                   "(object sexpression.KeyValuePair (key java.lang.String Dan) (value NULL)) " +
                   "(object sexpression.KeyValuePair (key java.lang.String Matt B) (value java.lang.Integer 1)) " +
                   "(object sexpression.KeyValuePair (key java.lang.String Matt K) (value java.lang.Integer 0)) " +
                   "(object sexpression.KeyValuePair (key java.lang.String Clayton) (value java.lang.Integer 0)))";

        rsMap.put(null,0);
        rsMap.put("Dan",null);

        rsExp = ASEConverter.convertToASE(rsMap);

        System.out.println("Expected: " + expected);
        System.out.println("Returned: " + rsExp);

        expected = "(object crypto.PlaintextRaceSelection (voteMap NULL) (title java.lang.String myNewRaceSelection) " +
                   "(size java.lang.Integer 1))";

        prs = ASEConverter.convertToASE(pNew);

        assertEquals(expected, prs.toString());



        System.out.println("Expected: " + expected);
        System.out.println("Returned: " + prs);

    }
}
