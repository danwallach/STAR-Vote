package security;

import controllers.routes;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;


public class Secured extends Security.Authenticator {

    @Override
    public String getUsername(Context ctx) {
        return ctx.session().get("username");
    }

    @Override
    public Result onUnauthorized(Context ctx) {

        if (ctx.request().path().contains("/admin")) {
            return Results.redirect(routes.AuditServer.adminverify());
        } else if (ctx.request().path().contains("/authority")) {
            return Results.redirect(routes.AuditServer.authorityverify());
        } else
            return Results.redirect(routes.AuditServer.index());
    }
}