package security;

import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import controllers.routes;
import models.User;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Results;
import play.mvc.SimpleResult;


public class AuthorisationHandler implements DeadboltHandler {

    public F.Promise<SimpleResult> beforeAuthCheck(Http.Context ctx) {
        return F.Promise.pure(null);
    }

    public F.Promise<SimpleResult> onAuthFailure(Http.Context ctx, String s) {

        if (ctx.request().path().contains("/admin")) {
            return F.Promise.pure(Results.redirect(routes.AuditServer.adminverify()));
        } else if (ctx.request().path().contains("/authority")) {
            return F.Promise.pure(Results.redirect(routes.AuditServer.authorityverify()));
        } else
            return F.Promise.pure(Results.redirect(routes.AuditServer.index()));

    }

    public Subject getSubject(Http.Context ctx) {
        return User.find.byId(ctx.session().get("username")); }

    public DynamicResourceHandler getDynamicResourceHandler(Http.Context ctx) { return null; }

}