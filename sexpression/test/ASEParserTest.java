package sexpression.test;

import crypto.PlaintextRaceSelection;
import junit.framework.TestCase;
import sexpression.ASEParser;
import sexpression.ASExpression;
import sexpression.ConversionException;
import sexpression.ListExpression;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Matthew Kindy II on 4/16/2015.
 */
public class ASEParserTest extends TestCase{

    private Map<String,Integer> rsMap = new HashMap<>();

    protected void setUp() throws Exception {

        int i = 2;
        PlaintextRaceSelection p = new PlaintextRaceSelection(rsMap,"myRaceSelection",1);

        rsMap.put("Matt K", 0);
        rsMap.put("Matt B", 1);
        rsMap.put("Clayton", 0);

    }

    public void testFromASE(){

            int i = 2;
            ASExpression iExp = ASEParser.convert(i);
            int j = ASEParser.convert((ListExpression) iExp);
            System.out.println(i + " " + iExp + " " + j);
            assertEquals(i, j);
    }

    public void testToASE(){

        String expected = "(object java.util.HashMap (object sexpression.KeyValue (key java.lang.String Matt B) " +
                          "(value java.lang.Integer 1)) (object sexpression.KeyValue (key java.lang.String Matt K) " +
                          "(value java.lang.Integer 0)) (object sexpression.KeyValue (key java.lang.String Clayton) " +
                          "(value java.lang.Integer 0)))";


        ListExpression rsExp = ASEParser.convert(rsMap);

        assertEquals(expected, rsExp.toString());

        System.out.println("Exoected: " + expected);
        System.out.println("Returned: " + rsExp);

        Map rs = ASEParser.convert(rsExp);

        assertEquals(rsMap, rs);

        System.out.println("Exoected: " + rsMap);
        System.out.println("Returned: " + rs);

    }
}
