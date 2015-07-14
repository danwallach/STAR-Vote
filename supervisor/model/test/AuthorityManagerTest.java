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
        AdderPrivateKeyShare a1prks = AuthorityManager.generateAuthorityKeySharePair("1");
        AdderPrivateKeyShare a2prks = AuthorityManager.generateAuthorityKeySharePair("2");
        AdderPrivateKeyShare a3prks = AuthorityManager.generateAuthorityKeySharePair("3");

        AuthorityManager.generateAuthorityPolynomialValues("1");
        AuthorityManager.generateAuthorityPolynomialValues("2");
        AuthorityManager.generateAuthorityPolynomialValues("3");

        a1prks = AuthorityManager.generateRealPrivateKeyShare("1");
        a2prks = AuthorityManager.generateRealPrivateKeyShare("2");
        a3prks = AuthorityManager.generateRealPrivateKeyShare("3");

        AdderPublicKey PEK = AuthorityManager.generatePublicEncryptionKey();

        System.out.println("Public Encryption Key: " + PEK);





    }
}
