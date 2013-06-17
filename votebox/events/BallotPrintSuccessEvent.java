package votebox.events;

import sexpression.*;

import java.math.BigInteger;

/**
 * Created with IntelliJ IDEA.
 * User: martinnikol
 * Date: 6/12/13
 * An event to signify that a print has been successful
 */
public class BallotPrintSuccessEvent implements IAnnounceEvent{

        private int serial;

        private String bID;

        private byte[] nonce;

        /**
         * Matcher for the pinEntered message
         */
        private static MatcherRule MATCHER = new MatcherRule() {
            private ASExpression pattern = new ListExpression( StringExpression
                    .makeString("ballot-print-success"), StringWildcard.SINGLETON, StringWildcard.SINGLETON );

            public IAnnounceEvent match(int serial, ASExpression sexp) {
                ASExpression res = pattern.match( sexp );
                if (res != NoMatch.SINGLETON) {
                    String bID = ((ListExpression) res).get(0).toString();
                    byte[] nonce = new BigInteger(((StringExpression) ((ListExpression) res)
                            .get( 1 )).toString()).toByteArray();
                    return new BallotPrintSuccessEvent( serial, bID, nonce );
                }

                return null;
            }
        };

        public int getSerial() {
            return serial;
        }

        public byte[] getNonce() {
            return nonce;
        }

        /**
         *
         * @return a MatcherRule for parsing this event type.
         */
        public static MatcherRule getMatcher(){
            return MATCHER;
        }

        public String getBID() {
            return bID;
        }

        public BallotPrintSuccessEvent(int serial, String bID, byte[] nonce) {
            this.serial = serial;
            this.bID = bID;
            this.nonce = nonce;
        }

        public void fire(VoteBoxEventListener l) {
            l.ballotPrintSuccess(this);
        }

        public ASExpression toSExp() {
            return new ListExpression( StringExpression.makeString("ballot-print-success"),
                    StringExpression.makeString( bID ),
                    StringExpression.makeString( new BigInteger(nonce).toString()));
        }

    }