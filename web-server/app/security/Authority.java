package security;

import be.objectify.deadbolt.core.models.Role;

/**
 * Created by Matthew Kindy II on 6/29/2015.
 */
public class Authority implements Role {

    private static final String roleName = "authority";

    @Override
    public String getName() {
        return roleName;
    }
}
