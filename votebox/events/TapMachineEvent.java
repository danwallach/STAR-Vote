package votebox.events;

import sexpression.*;

/**
 * @author Matt Bernhard
 * @version 0.0.1
 *          Date: 7/29/13
 */
public class TapMachineEvent implements IAnnounceEvent {

    /**
     * Matcher for the ballotScanner message
     */
    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = new ListExpression(StringExpression
                .makeString("tapmachine"),
                StringWildcard.SINGLETON, StringWildcard.SINGLETON,
                StringWildcard.SINGLETON, StringWildcard.SINGLETON );

        public IAnnounceEvent match(int serial, ASExpression sexp){
            ASExpression res = pattern.match(sexp);
            if (res != NoMatch.SINGLETON) {
                int label = Integer.parseInt( ((ListExpression) res).get( 0 )
                        .toString() );
                int battery = Integer.parseInt(((ListExpression)res).get(2).toString());
                int protectedCount = Integer.parseInt( ((ListExpression) res)
                        .get(3).toString() );
                int publicCount = Integer.parseInt( ((ListExpression) res)
                        .get(4).toString() );
                return new TapMachineEvent(serial, label, battery, protectedCount, publicCount);
            }

            return null;
        }


    };
    private int serial;
    private int battery;
    private int protectedCount;
    private int publicCount;
    private int label;

    public TapMachineEvent(int serial, int label,int battery,
                              int protectedCount, int publicCount) {
        this.serial = serial;
        this.label = label;
        this.battery = battery;
        this.protectedCount = protectedCount;
        this.publicCount = publicCount;
    }

    /**
     * @return the label
     */
    public int getLabel() {
        return label;
    }

    /**
     * @return a MatcherRule for parsing this event type.
     */
    public static MatcherRule getMatcher() {
        return MATCHER;
    }//getMatcher

    public int getSerial() {
        return serial;
    }


    /**
     * @return the battery level
     */
    public int getBattery() {
        return battery;
    }

    /**
     * @return the protected count
     */
    public int getProtectedCount() {
        return protectedCount;
    }

    /**
     * @return the public count
     */
    public int getPublicCount() {
        return publicCount;
    }

    public void fire(VoteBoxEventListener l) {
        l.tapMachine(this);
    }

    public ASExpression toSExp() {
        return new ListExpression(

                StringExpression.makeString("tapmachine"),
                StringExpression.makeString( Integer.toString( label ) ),
                StringExpression.makeString( Integer.toString( battery ) ),
                StringExpression.makeString( Integer.toString( protectedCount ) ),
                StringExpression.makeString( Integer.toString( publicCount ) ));
    }
}

