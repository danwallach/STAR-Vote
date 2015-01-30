package crypto.adder.test;

import crypto.adder.*;
import junit.framework.TestCase;
import junit.textui.TestRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * Private key test.
 *
 * @version $LastChangedRevision$ $LastChangedDate$
 * @since 0.0.1
 * @author David Walluck
 */
public class AdderPrivateKeyShareTest extends TestCase {
    /**
     * Constructs a new private key test.
     *
     * @param name the name of the test
     */
    public AdderPrivateKeyShareTest(String name) {
        super(name);
    }

    /**
     * The test.
     */
    public void test() {
        try {
            AdderPrivateKeyShare privateKey = AdderPrivateKeyShare.fromString("p123g135x246f234");

            assertEquals(new AdderInteger("123"), privateKey.getP());
            assertEquals(new AdderInteger("135", privateKey.getP()),
                         privateKey.getG());
            assertEquals(new AdderInteger("246", privateKey.getQ()),
                         privateKey.getX());
            assertEquals(new AdderInteger("234", privateKey.getP()),
                         privateKey.getF());
        } catch (InvalidPrivateKeyException ipke) {
            fail();
        }

        AdderPrivateKeyShare privateKey1 = new AdderPrivateKeyShare(new AdderInteger("123"),
                                                new AdderInteger("135"),
                                                new AdderInteger("246"),
                                                new AdderInteger("234"));

        assertEquals(new AdderInteger("123"), privateKey1.getP());
        assertEquals(new AdderInteger("135"), privateKey1.getG());
        assertEquals(new AdderInteger("246"), privateKey1.getX());
        assertEquals(new AdderInteger("234"), privateKey1.getF());

        AdderPrivateKeyShare privateKey2 = new AdderPrivateKeyShare(new AdderInteger("123"),
                                                new AdderInteger("135"),
                                                new AdderInteger("246"),
                                                new AdderInteger("234"));

        assertEquals("p123g135x246f234", privateKey2.toString());

        try {
            AdderPrivateKeyShare.fromString("pgxf");
            fail();
        } catch (InvalidPrivateKeyException ignored) {

        }

        try {
            AdderPrivateKeyShare.fromString("x123g123x123f123");
            fail();
        } catch (InvalidPrivateKeyException ignored) {

        }

        try {
            AdderPrivateKeyShare.fromString("p123x123x123f123");
            fail();
        } catch (InvalidPrivateKeyException ignored) {

        }

        try {
            AdderPrivateKeyShare.fromString("p123g123g123f123");
            fail();
        } catch (InvalidPrivateKeyException ignored) {

        }

        try {
            AdderPrivateKeyShare.fromString("p123g123x123x123");
            fail();
        } catch (InvalidPrivateKeyException ignored) {

        }

        try {
            AdderPrivateKeyShare.fromString("p123g123x123f123p123");
            fail();
        } catch (InvalidPrivateKeyException ignored) {

        }

        try {
            AdderPrivateKeyShare.fromString("p123g123x123f12a");
            fail();
        } catch (InvalidPrivateKeyException ignored) {

        }

        try {
            AdderPrivateKeyShare.fromString("p123g123x123p123");
            fail();
        } catch (InvalidPrivateKeyException ignored) {

        }

        try {
            AdderPrivateKeyShare privateKey3
                = AdderPrivateKeyShare.
                  fromString("p1045854189839g696796413029x125538416498f74554249"
                          + "804");
            AdderVote vote1
                = AdderVote.fromString("p1045854189839G733227695096H407210523871");
            List/*<AdderInteger>*/ list1 = new ArrayList/*<AdderInteger>*/(1);
            list1.add(new AdderInteger("696993318894"));
            assertEquals(list1, privateKey3.partialDecrypt(vote1));
            AdderPrivateKeyShare privateKey4
                = AdderPrivateKeyShare.
                  fromString(
                          "p1045854189839g696796413029x17670762055f74554249804");
            AdderVote vote2
                = AdderVote.fromString("p1045854189839G733227695096H407210523871");
            List/*<AdderInteger>*/ list2 = new ArrayList/*<AdderInteger>*/(1);
            list2.add(new  AdderInteger("695327169426"));
            assertEquals(list2, privateKey4.partialDecrypt(vote2));
        } catch (InvalidPrivateKeyException | InvalidVoteException ipke) {
            fail();
        }

        try {
            List/*<ElgamalCiphertext>*/ poly1
                = new ArrayList/*<ElgamalCiphertext>*/(2);

            ElgamalCiphertext poly11 =
                ElgamalCiphertext.
                fromString("p553417232063G493554648720H419663070136");
            ElgamalCiphertext poly21 =
                ElgamalCiphertext.
                fromString("p553417232063G472527834841H115611499483");

            poly1.add(poly11);
            poly1.add(poly21);

            List/*<ElgamalCiphertext>*/ poly2
                = new ArrayList/*<ElgamalCiphertext>*/(2);

            ElgamalCiphertext poly12 =
                ElgamalCiphertext
                .fromString("p553417232063G523509046398H219311764844");
            ElgamalCiphertext poly22 =
                ElgamalCiphertext
                .fromString("p553417232063G92746876741H526564771384");

            poly2.add(poly12);
            poly2.add(poly22);

            AdderPrivateKeyShare authPrivKey1 =
                AdderPrivateKeyShare.
                fromString(
                        "p553417232063g15044079079x187988315695f419757826339");

            AdderPrivateKeyShare authPrivKey2 =
                AdderPrivateKeyShare.
                fromString(
                        "p553417232063g15044079079x227043662924f419757826339");

            AdderPrivateKeyShare authFinPrivKey1 = authPrivKey1.getFinalPrivKey(poly1);
            AdderPrivateKeyShare authFinPrivKey2 = authPrivKey2.getFinalPrivKey(poly2);

            assertEquals("p553417232063g15044079079x143494327621f419757826339",
                          authFinPrivKey1.toString());
            assertEquals("p553417232063g15044079079x182873233170f419757826339",
                         authFinPrivKey2.toString());
        } catch (InvalidPrivateKeyException | InvalidVoteException ipke) {
            fail();
        }
    }

    /**
     * The main method.
     *
     * @param args the main parameters
     */
    public static void main(String[] args) {
        TestRunner.run(AdderPrivateKeyShareTest.class);
    }
}
