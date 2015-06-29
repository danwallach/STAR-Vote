package security;

import be.objectify.deadbolt.core.models.Role;

/**
 * Created by Matthew Kindy II on 6/29/2015.
 */
public class Admin implements Role {

    private static final String roleName = "admin";

    @Override
    public String getName() {
        return roleName;
    }
}
