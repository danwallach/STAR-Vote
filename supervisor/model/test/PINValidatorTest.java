package supervisor.model.test;

import junit.framework.TestCase;
import supervisor.model.PINValidator;

import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;

/**
 * A test suite for the PINValidator class
 *
 * @author Matt Bernhard
 */
public class PINValidatorTest extends TestCase {

    private Pattern pattern;

    PINValidator pv;

    protected void setUp() throws Exception {
        super.setUp();

        pv = PINValidator.SINGLETON;

        pattern = Pattern.compile("\\d\\d\\d\\d\\d");
    }

    /**
     * Ensure that PINs get generated without collisions, and are formatted properly.
     */
    public void testPINGeneration() {
        pv.clear();
        String pid = "003";

        ArrayList<String> pins = new ArrayList<>();

        /* This gives us a 10% chance of collision */
        for(int i = 0; i < 10000; i++) {
            String pin = pv.generatePIN(pid);

            /* Ensure the generated pin is of the correct format */
            if(!pattern.matcher(pin).matches())
                fail("Generated PIN " + pin + " failed to match the pattern!");

            if(pins.contains(pin))
                fail("PIN collision!");

            pins.add(i, pin);

        }
    }

    /**
     * Ensure that PINs expire like they are supposed to, and are no longer valid.
     */
    public void testPINExpiration() {
        pv.clear();
        String pid = "003";

        ArrayList<String> pins = new ArrayList<>();

        /* This gives us a 10% chance of collision */
        for(int i = 0; i < 10000; i++) {
            String pin = pv.generatePIN(pid, 1);

            pins.add(i, pin);
        }

        /* Wait for all of the PINs to expire */
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /* Verify that they have expired */
        for(String pin : pins)
            assertFalse("PIN " + pin + " should no longer be valid!", pv.validatePIN(pin));


    }

    /**
     * Ensure that PINs that get used are invalidated.
     */
    public void testUsePIN() {
        pv.clear();
        String pid = "003";

        ArrayList<String> pins = new ArrayList<>();

        /* This gives us a 10% chance of collision */
        for(int i = 0; i < 10000; i++) {
            String pin = pv.generatePIN(pid);

            /* Ensure the generated pin is of the correct format */
            if(!pattern.matcher(pin).matches())
                fail("Generated PIN " + pin + " failed to match the pattern!");

            if(pins.contains(pin))
                fail("PIN collision!");

            pins.add(i, pin);

        }

        for(String pin : pins)
            pv.usePIN(pin);

        for(String pin : pins)
            assertFalse("PIN " + pin + " should no longer be valid!", pv.validatePIN(pin));

    }

    /**
     * Further testing the invalidation of PINs, this time using random sampling
     * instead of using every PIN.
     */
    public void testUseSomePINs() {

        String pid = "003";

        Random rand = new Random();

        /* Run 10 trials to get a good sampling */
        for(int trial = 0; trial < 10; trial++) {
            System.out.println("testUseSomePINs trial " + trial);
            pv.clear();

            ArrayList<String> pins = new ArrayList<>();
            ArrayList<String> used = new ArrayList<>();

            /* This gives us a 10% chance of collision */
            for (int i = 0; i < 10000; i++) {
                String pin = pv.generatePIN(pid);

                /* Ensure the generated pin is of the correct format */
                if (!pattern.matcher(pin).matches())
                    fail("Generated PIN " + pin + " failed to match the pattern!");

                if (pins.contains(pin))
                    fail("PIN collision!");

                pins.add(pin);

            }

            /* Use a random sample of 500 pins */
            for (int i = 0; i < 500; i++) {
                String removeMe = pins.get(rand.nextInt(10000));
                pv.usePIN(removeMe);
                used.add(removeMe);
            }

            for (String pin : pins) {
                boolean valid = pv.validatePIN(pin);

                if (used.contains(pin)) {
                    assertFalse("Trial " + trial + ": PIN " + pin + " should no longer be valid!", valid);
                    used.remove(pin);
                } else {
                    assertTrue("Trial " + trial + ": PIN " + pin + " should be valid!", valid);
                }

            }

        }

    }
}
