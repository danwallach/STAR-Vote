package crypto;

import sexpression.ASExpression;
import sexpression.ListExpression;

import java.util.Map;

/**
 * Created by Matthew Kindy II on 11/19/2014.
 */
public class PlaintextRaceSelection extends ARaceSelection {

    private Map<String, Integer> voteMap;

    public PlaintextRaceSelection(Map<String, Integer> voteMap, String title, int size) {
        super(title,size);
        this.voteMap = voteMap;
    }

    public Map<String, Integer> getRaceSelectionsMap(){
        return voteMap;
    }

    public String getTitle(){
        return title;
    }

    public ASExpression toASE(){
        return new ListExpression("");
    }

    public String toString() {
        return "";
    }
}
