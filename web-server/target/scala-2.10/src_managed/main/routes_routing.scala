// @SOURCE:/home/mdb12/Workspace/STAR-Vote/web-server/conf/routes
// @HASH:6d4dbf992ffb3e6a238a90207f286e8dac83f093
// @DATE:Mon Jul 28 14:48:09 CDT 2014


import play.core._
import play.core.Router._
import play.core.j._

import play.api.mvc._
import play.libs.F

import Router.queryString

object Routes extends Router.Routes {

private var _prefix = "/"

def setPrefix(prefix: String) {
  _prefix = prefix
  List[(String,Routes)]().foreach {
    case (p, router) => router.setPrefix(prefix + (if(prefix.endsWith("/")) "" else "/") + p)
  }
}

def prefix = _prefix

lazy val defaultPrefix = { if(Routes.prefix.endsWith("/")) "" else "/" }


// @LINE:6
private[this] lazy val controllers_AuditServer_index0 = Route("GET", PathPattern(List(StaticPart(Routes.prefix))))
        

// @LINE:7
private[this] lazy val controllers_AuditServer_confirm1 = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("confirm"))))
        

// @LINE:8
private[this] lazy val controllers_AuditServer_challenge2 = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("challenge"))))
        

// @LINE:9
private[this] lazy val controllers_AuditServer_getCastBallot3 = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("confirm/submit"))))
        

// @LINE:10
private[this] lazy val controllers_AuditServer_getChallengedBallot4 = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("challenge/submit"))))
        

// @LINE:11
private[this] lazy val controllers_AuditServer_handleBallotState5 = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("ballot"))))
        

// @LINE:12
private[this] lazy val controllers_AuditServer_aboutUs6 = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("aboutUs"))))
        

// @LINE:13
private[this] lazy val controllers_AuditServer_getAPI7 = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("api"))))
        

// @LINE:14
private[this] lazy val controllers_AuditServer_getTrac8 = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("trac"))))
        

// @LINE:16
private[this] lazy val controllers_AuditServer_ballotDump9 = Route("POST", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("3FF968A3B47CT34C"))))
        

// @LINE:18
private[this] lazy val controllers_AuditServer_adminlogin10 = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("admin"))))
        

// @LINE:19
private[this] lazy val controllers_AuditServer_adminverify11 = Route("POST", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("admin/login"))))
        

// @LINE:20
private[this] lazy val controllers_AuditServer_adminclear12 = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("admin/cleardata"))))
        

// @LINE:21
private[this] lazy val controllers_AuditServer_adminconflicts13 = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("admin/conflicts"))))
        

// @LINE:22
private[this] lazy val controllers_AuditServer_adminpublish14 = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("admin/publish"))))
        

// @LINE:25
private[this] lazy val controllers_AuditServer_getBallotHtmlFile15 = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("files"))))
        

// @LINE:26
private[this] lazy val controllers_Assets_at16 = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("assets/"),DynamicPart("file", """.+""",false))))
        
def documentation = List(("""GET""", prefix,"""controllers.AuditServer.index()"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """confirm""","""controllers.AuditServer.confirm()"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """challenge""","""controllers.AuditServer.challenge()"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """confirm/submit""","""controllers.AuditServer.getCastBallot(ballotid:String ?= "none")"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """challenge/submit""","""controllers.AuditServer.getChallengedBallot(ballotid:String ?= "none")"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """ballot""","""controllers.AuditServer.handleBallotState(ballotid:String ?= "none")"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """aboutUs""","""controllers.AuditServer.aboutUs"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """api""","""controllers.AuditServer.getAPI()"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """trac""","""controllers.AuditServer.getTrac()"""),("""POST""", prefix + (if(prefix.endsWith("/")) "" else "/") + """3FF968A3B47CT34C""","""controllers.AuditServer.ballotDump()"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """admin""","""controllers.AuditServer.adminlogin()"""),("""POST""", prefix + (if(prefix.endsWith("/")) "" else "/") + """admin/login""","""controllers.AuditServer.adminverify()"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """admin/cleardata""","""controllers.AuditServer.adminclear()"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """admin/conflicts""","""controllers.AuditServer.adminconflicts()"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """admin/publish""","""controllers.AuditServer.adminpublish()"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """files""","""controllers.AuditServer.getBallotHtmlFile(ballotid:String ?= "none")"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """assets/$file<.+>""","""controllers.Assets.at(path:String = "/public", file:String)""")).foldLeft(List.empty[(String,String,String)]) { (s,e) => e.asInstanceOf[Any] match {
  case r @ (_,_,_) => s :+ r.asInstanceOf[(String,String,String)]
  case l => s ++ l.asInstanceOf[List[(String,String,String)]] 
}}
      

def routes:PartialFunction[RequestHeader,Handler] = {

// @LINE:6
case controllers_AuditServer_index0(params) => {
   call { 
        invokeHandler(controllers.AuditServer.index(), HandlerDef(this, "controllers.AuditServer", "index", Nil,"GET", """ Home page""", Routes.prefix + """"""))
   }
}
        

// @LINE:7
case controllers_AuditServer_confirm1(params) => {
   call { 
        invokeHandler(controllers.AuditServer.confirm(), HandlerDef(this, "controllers.AuditServer", "confirm", Nil,"GET", """""", Routes.prefix + """confirm"""))
   }
}
        

// @LINE:8
case controllers_AuditServer_challenge2(params) => {
   call { 
        invokeHandler(controllers.AuditServer.challenge(), HandlerDef(this, "controllers.AuditServer", "challenge", Nil,"GET", """""", Routes.prefix + """challenge"""))
   }
}
        

// @LINE:9
case controllers_AuditServer_getCastBallot3(params) => {
   call(params.fromQuery[String]("ballotid", Some("none"))) { (ballotid) =>
        invokeHandler(controllers.AuditServer.getCastBallot(ballotid), HandlerDef(this, "controllers.AuditServer", "getCastBallot", Seq(classOf[String]),"GET", """""", Routes.prefix + """confirm/submit"""))
   }
}
        

// @LINE:10
case controllers_AuditServer_getChallengedBallot4(params) => {
   call(params.fromQuery[String]("ballotid", Some("none"))) { (ballotid) =>
        invokeHandler(controllers.AuditServer.getChallengedBallot(ballotid), HandlerDef(this, "controllers.AuditServer", "getChallengedBallot", Seq(classOf[String]),"GET", """""", Routes.prefix + """challenge/submit"""))
   }
}
        

// @LINE:11
case controllers_AuditServer_handleBallotState5(params) => {
   call(params.fromQuery[String]("ballotid", Some("none"))) { (ballotid) =>
        invokeHandler(controllers.AuditServer.handleBallotState(ballotid), HandlerDef(this, "controllers.AuditServer", "handleBallotState", Seq(classOf[String]),"GET", """""", Routes.prefix + """ballot"""))
   }
}
        

// @LINE:12
case controllers_AuditServer_aboutUs6(params) => {
   call { 
        invokeHandler(controllers.AuditServer.aboutUs, HandlerDef(this, "controllers.AuditServer", "aboutUs", Nil,"GET", """""", Routes.prefix + """aboutUs"""))
   }
}
        

// @LINE:13
case controllers_AuditServer_getAPI7(params) => {
   call { 
        invokeHandler(controllers.AuditServer.getAPI(), HandlerDef(this, "controllers.AuditServer", "getAPI", Nil,"GET", """""", Routes.prefix + """api"""))
   }
}
        

// @LINE:14
case controllers_AuditServer_getTrac8(params) => {
   call { 
        invokeHandler(controllers.AuditServer.getTrac(), HandlerDef(this, "controllers.AuditServer", "getTrac", Nil,"GET", """""", Routes.prefix + """trac"""))
   }
}
        

// @LINE:16
case controllers_AuditServer_ballotDump9(params) => {
   call { 
        invokeHandler(controllers.AuditServer.ballotDump(), HandlerDef(this, "controllers.AuditServer", "ballotDump", Nil,"POST", """""", Routes.prefix + """3FF968A3B47CT34C"""))
   }
}
        

// @LINE:18
case controllers_AuditServer_adminlogin10(params) => {
   call { 
        invokeHandler(controllers.AuditServer.adminlogin(), HandlerDef(this, "controllers.AuditServer", "adminlogin", Nil,"GET", """""", Routes.prefix + """admin"""))
   }
}
        

// @LINE:19
case controllers_AuditServer_adminverify11(params) => {
   call { 
        invokeHandler(controllers.AuditServer.adminverify(), HandlerDef(this, "controllers.AuditServer", "adminverify", Nil,"POST", """""", Routes.prefix + """admin/login"""))
   }
}
        

// @LINE:20
case controllers_AuditServer_adminclear12(params) => {
   call { 
        invokeHandler(controllers.AuditServer.adminclear(), HandlerDef(this, "controllers.AuditServer", "adminclear", Nil,"GET", """""", Routes.prefix + """admin/cleardata"""))
   }
}
        

// @LINE:21
case controllers_AuditServer_adminconflicts13(params) => {
   call { 
        invokeHandler(controllers.AuditServer.adminconflicts(), HandlerDef(this, "controllers.AuditServer", "adminconflicts", Nil,"GET", """""", Routes.prefix + """admin/conflicts"""))
   }
}
        

// @LINE:22
case controllers_AuditServer_adminpublish14(params) => {
   call { 
        invokeHandler(controllers.AuditServer.adminpublish(), HandlerDef(this, "controllers.AuditServer", "adminpublish", Nil,"GET", """""", Routes.prefix + """admin/publish"""))
   }
}
        

// @LINE:25
case controllers_AuditServer_getBallotHtmlFile15(params) => {
   call(params.fromQuery[String]("ballotid", Some("none"))) { (ballotid) =>
        invokeHandler(controllers.AuditServer.getBallotHtmlFile(ballotid), HandlerDef(this, "controllers.AuditServer", "getBallotHtmlFile", Seq(classOf[String]),"GET", """ Map static resources from the /public folder to the /assets URL path""", Routes.prefix + """files"""))
   }
}
        

// @LINE:26
case controllers_Assets_at16(params) => {
   call(Param[String]("path", Right("/public")), params.fromPath[String]("file", None)) { (path, file) =>
        invokeHandler(controllers.Assets.at(path, file), HandlerDef(this, "controllers.Assets", "at", Seq(classOf[String], classOf[String]),"GET", """""", Routes.prefix + """assets/$file<.+>"""))
   }
}
        
}

}
     