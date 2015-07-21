package crypto.adder.test;

import crypto.adder.AdderPrivateKeyShare;
import junit.framework.TestCase;
import org.apache.commons.codec.binary.Base64;
import supervisor.model.AuthorityManager;

import java.io.*;

/**
 * Created by Matthew Kindy II on 7/21/2015.
 */
public class AdderPrivateKeyShareTest extends TestCase {


    /**
     * Test case setup
     */
    protected void setUp() throws Exception {

        super.setUp();

    }

    public void testSerialize() throws Exception {

        AuthorityManager.SESSION.newSession(2,1,3);
        AdderPrivateKeyShare prks = AuthorityManager.SESSION.generateAuthorityKeySharePair("1");

        assertEquals(prks, stringToKey(keyToString(prks)));
    }

    private String keyToString(AdderPrivateKeyShare key){
        try {

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(key);
            objectOutputStream.close();

            String stringKey = new String(Base64.encodeBase64(byteArrayOutputStream.toByteArray()));
            byteArrayOutputStream.close();
            return stringKey;
        }
        catch (IOException e) { e.printStackTrace(); }

        return null;
    }

    private AdderPrivateKeyShare stringToKey(String stringKey) {

        if (stringKey == null) return null;

        try {
            byte[] bytes = Base64.decodeBase64(stringKey.getBytes());

            ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));

            AdderPrivateKeyShare privateKeyShare = (AdderPrivateKeyShare)objectInputStream.readObject();
            objectInputStream.close();

            return privateKeyShare;
        }
        catch (IOException | ClassNotFoundException | ClassCastException e) { e.printStackTrace(); }

        return null;
    }

}
