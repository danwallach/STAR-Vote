import com.avaje.ebean.Ebean;
import models.User;
import play.Application;
import play.GlobalSettings;
import play.libs.Yaml;
import sexpression.ASEConverter;
import sexpression.ASExpression;
import sexpression.ListExpression;
import supervisor.model.AuthorityManager;
import utilities.BallotLoader;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


/**
 * Created with IntelliJ IDEA.
 * User: mrdouglass95
 * Date: 7/23/13
 * Time: 2:24 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 * Implementation of a startup task for initializing ballot files
 */
public class Global extends GlobalSettings {

    /**
     * Play Start-up task. Calls method BallotLoader.init()
     */
    @Override
    public void onStart(Application app) {

        System.out.println("Initializing the Ballot Loader");
        BallotLoader.init();

        try {

            System.out.println("Opening and reading the authority information file...");
            File authorityFile = new File("conf", "authority-data.inf");
            Path authorityPath = authorityFile.toPath();

            System.out.println("Setting up the session...");
            byte[] verbatimAuthorityInfo = Files.readAllBytes(authorityPath);
            ASExpression authorityInfo = ASExpression.makeVerbatim(verbatimAuthorityInfo);
            AuthorityManager.SESSION = ASEConverter.convertFromASE((ListExpression) authorityInfo);
        }
        catch (Exception e) {
            System.err.println("Could not load the authority information!");

        }

        try {

            System.out.println("Opening and reading the user information file...");
            Object o = Yaml.load("user-data.yml");
            System.out.println(o);

            for (User u : ((List<User>)o)) {

                System.out.println("Saving the user information... ");
                System.out.println(u);

                if (User.find.where().eq("username", u.getIdentifier()) == null)
                    Ebean.save(u);
            }

        }
        catch (Exception e) {
            e.printStackTrace();
            System.err.println("Could not load the user information!");

        }

        if (models.User.find.findRowCount() == 0) {
            System.out.println("Initial data was loaded.");
            Ebean.save((List) Yaml.load("initial-data.yml"));
        }

    }
}
