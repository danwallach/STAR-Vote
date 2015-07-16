package supervisor.model.test;

import crypto.adder.AdderPrivateKeyShare;
import crypto.adder.AdderPublicKey;
import junit.framework.TestCase;
import supervisor.model.AuthorityManager;

/**
 * Created by Matthew Kindy II on 6/19/2015.
 */
public class AuthorityManagerTest extends TestCase {

    /**
     * Test case setup
     */
    protected void setUp() throws Exception {

        super.setUp();

    }

    /* Want to test PEK gen and also spew keyshares to files so that
    * we can use them for election run-throughs later */
    public void testProcedure() throws Exception {

        /* Have 3 logins to generate keypairs*/
        AdderPrivateKeyShare a1prks = AuthorityManager.SESSION.generateAuthorityKeySharePair("1");
        AdderPrivateKeyShare a2prks = AuthorityManager.SESSION.generateAuthorityKeySharePair("2");
        AdderPrivateKeyShare a3prks = AuthorityManager.SESSION.generateAuthorityKeySharePair("3");

        AuthorityManager.SESSION.generateAuthorityPolynomialValues("1");
        AuthorityManager.SESSION.generateAuthorityPolynomialValues("2");
        AuthorityManager.SESSION.generateAuthorityPolynomialValues("3");

        a1prks = AuthorityManager.SESSION.generateRealPrivateKeyShare("1");
        a2prks = AuthorityManager.SESSION.generateRealPrivateKeyShare("2");
        a3prks = AuthorityManager.SESSION.generateRealPrivateKeyShare("3");

        AdderPublicKey PEK = AuthorityManager.SESSION.generatePublicEncryptionKey();

        System.out.println("Public Encryption Key: " + PEK);

    }
}
