package controllers;

import play.mvc.*;
import play.mvc.Http.*;

public class Secured extends Security.Authenticator {

    @Override
    public String getUsername(Context ctx) {
        return ctx.session().get("username");
    }

    @Override
    public Result onUnauthorized(Context ctx) {
        return redirect(routes.AuditServer.adminverify());
    }
}