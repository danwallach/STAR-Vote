package supervisor.model.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Matt Bernhard
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
        PINValidatorTest.class,
        PrecinctTest.class,
        HashChainTest.class
})

public class SupervisorModelTest {
}
