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

    public void testUseSomePINs() {
        pv.clear();
        String pid = "003";

        Random rand = new Random();

        /* Run 10 trials to get a good sampling */
        for(int trial = 0; trial < 10; trial++) {

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

                pins.add(i, pin);

            }

            /* Use a random sample of the pins */
            for (int i = 0; i < 500; i++)
                used.add(pv.usePIN(pins.get(rand.nextInt(10000))));


            for (String pin : pins) {
                if (used.contains(pin))
                    assertFalse("Trial " + trial + ": PIN " + pin + " should no longer be valid!", (pv.validatePIN(pin)));

                else
                    assertTrue("Trial " + trial + ": PIN " + pin + " should be valid!", (pv.validatePIN(pin)));
            }

        }

    }
}
