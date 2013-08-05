package votebox.events;

import sexpression.*;

/**
 * Event class for when a ballot is scanned
 */
public class BallotScannedEvent implements IAnnounceEvent {

  private int serial;

  private String BID;

  /**
   * Matcher for the ballotscanned message
   */
  private static MatcherRule MATCHER = new MatcherRule() {
    private ASExpression pattern = new ListExpression( StringExpression
        .makeString( "ballot-scanned" ), StringWildcard.SINGLETON);

    public IAnnounceEvent match(int serial, ASExpression sexp) {
      ASExpression res = pattern.match( sexp );
      if (res != NoMatch.SINGLETON) {
        String BID = ((ListExpression) res).get( 0 ).toString();
        return new BallotScannedEvent( serial, BID );
      }

      return null;
    }
  };

  /**
   *
   * @return a MatcherRule for parsing this event type.
   */
  public static MatcherRule getMatcher(){
    return MATCHER;
  }

  public int getSerial() {
    return serial;
  }

  /**
   * @return the ballot ID of accepted ballot
   */
  public String getBID() {
    return BID;
  }

    /**
     * Constructor of ballot
     *
     * @param serial
     * @param BID
     */
  public BallotScannedEvent(int serial, String BID) {
    this.serial = serial;
    this.BID = BID;
  }

  public void fire(VoteBoxEventListener l) {
    l.ballotScanned(this);
  }

  public ASExpression toSExp() {
    return new ListExpression( StringExpression.makeString("ballot-scanned"),
        StringExpression.makeString( BID ) );
  }

}
