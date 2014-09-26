package votebox.events;

import org.apache.commons.codec.binary.Base64;
import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.NamedNoMatch;
import sexpression.StringExpression;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

/**
 * @author Matt Bernhard
 */
public class BallotUploadEvent extends AAnnounceEvent {

    private static MatcherRule MATCHER = new MatcherRule() {
        private ASExpression pattern = ASExpression
                .make("(ballot-upload %map:#any)");

        public IAnnounceEvent match(int serial, ASExpression sexp) {
            HashMap<String, ASExpression> result = pattern.namedMatch(sexp);
            if (result != NamedNoMatch.SINGLETON) {
                return new BallotUploadEvent(serial, result.get("map"));
            }

            return null;
        }
    };


    private Serializable map;

    public BallotUploadEvent(int serial, Serializable map) {
        super(serial);

        this.map = map;
    }

    public Serializable getMap() {
        return map;
    }


    /**
     * @return a MatcherRule for parsing this event type.
     */
    public static MatcherRule getMatcher(){
        return MATCHER;
    }//getMatcher

    @Override
    public ASExpression toSExp()  {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(map);
            objectOutputStream.close();


        } catch (IOException e) {
            throw new RuntimeException("Couldn't serialize the message!");
        }

        String encoded = new String(Base64.encodeBase64(byteArrayOutputStream.toByteArray()));

        return new ListExpression(StringExpression.makeString("ballot-upload"), StringExpression.make(encoded));
    }

    @Override
    public void fire(VoteBoxEventListener l) {
        l.uploadBallots(this);
    }
}
