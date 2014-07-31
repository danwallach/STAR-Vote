import play.*;
import play.libs.*;
import models.User;
import java.util.List;
import utilities.BallotLoader;
import com.avaje.ebean.Ebean;

/**
 * Created with IntelliJ IDEA.
 * User: mrdouglass95
 * Date: 7/23/13
 * Time: 2:24 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 * Implementation of a starup task for initializing ballot files
 */
public class Global extends GlobalSettings {

    /**
     * Play Start-up task. Calls method BallotLoader.init()
     */
    @Override
    public void onStart(Application app) {
        System.out.println("Initializing the Ballot Loader");
        BallotLoader.init();
        if (models.User.find.findRowCount() == 0) {
            Ebean.save((List) Yaml.load("initial-data.yml"));
        }
    }
}
