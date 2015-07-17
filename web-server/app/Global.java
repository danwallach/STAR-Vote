import com.avaje.ebean.Ebean;
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
        if (models.User.find.findRowCount() == 0) {
            Ebean.save((List) Yaml.load("initial-data.yml"));
        }


        try {

            File authorityFile = new File("conf", "authority-data.inf");
            Path authorityPath = authorityFile.toPath();

            byte[] verbatimAuthorityInfo = Files.readAllBytes(authorityPath);
            ASExpression authorityInfo = ASExpression.makeVerbatim(verbatimAuthorityInfo);
            AuthorityManager.SESSION = ASEConverter.convertFromASE((ListExpression) authorityInfo);
        }
        catch (Exception e) {
            System.err.println("Could not load the authority information");

        }


    }
}
