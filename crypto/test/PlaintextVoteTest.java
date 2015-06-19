package crypto.test;

import crypto.PlaintextVote;
import junit.framework.TestCase;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Matthew Kindy II on 11/9/2014.
 */
public class PlaintextVoteTest extends TestCase {

    protected void setUp() throws Exception {}

    public void testConstruction() {

        Map<String, Integer> voteMap = new TreeMap<>();

        for(int i=0; i<10;i++)
            voteMap.put("B"+i, i);

        PlaintextVote pv = new PlaintextVote(voteMap,"testVote");

        assertEquals("Title: [" + "testVote" + "]\n" + "VoteMap: [" + voteMap.toString() + "]", pv.toString());
    }

    public void testAccessors() {
        Map<String, Integer> voteMap = new TreeMap<>();

        for(int i=0; i<10;i++)
            voteMap.put("B"+i, i);

        PlaintextVote pv = new PlaintextVote(voteMap,"testVote");

        assertEquals("testVote", pv.getTitle());
        assertEquals(voteMap, pv.getVoteMap());
    }

}
