package test;

import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;

import play.mvc.*;
import play.libs.*;
import play.test.*;
import static play.test.Helpers.*;
import com.avaje.ebean.Ebean;
import com.google.common.collect.ImmutableMap;

public class LoginTest extends WithApplication {
    @Before
    public void setUp() {
        start(fakeApplication(inMemoryDatabase(), fakeGlobal()));
        Ebean.save((List) Yaml.load("test-data.yml"));
    }
    
    @Test
    public void authenticateSuccess() {
        Result result = callAction(
            controllers.routes.ref.AuditServer.authenticate(),
            fakeRequest().withFormUrlEncodedBody(ImmutableMap.of(
                "username", "bob@example.com",
                "password", "secret"))
        );
        assertEquals(302, status(result));
        assertEquals("bob@example.com", session(result).get("username"));
    }

}