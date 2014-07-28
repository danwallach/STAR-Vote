// @SOURCE:/home/mdb12/Workspace/STAR-Vote/web-server/conf/routes
// @HASH:6d4dbf992ffb3e6a238a90207f286e8dac83f093
// @DATE:Mon Jul 28 14:48:09 CDT 2014

import Routes.{prefix => _prefix, defaultPrefix => _defaultPrefix}
import play.core._
import play.core.Router._
import play.core.j._

import play.api.mvc._
import play.libs.F

import Router.queryString


// @LINE:26
// @LINE:25
// @LINE:22
// @LINE:21
// @LINE:20
// @LINE:19
// @LINE:18
// @LINE:16
// @LINE:14
// @LINE:13
// @LINE:12
// @LINE:11
// @LINE:10
// @LINE:9
// @LINE:8
// @LINE:7
// @LINE:6
package controllers {

// @LINE:25
// @LINE:22
// @LINE:21
// @LINE:20
// @LINE:19
// @LINE:18
// @LINE:16
// @LINE:14
// @LINE:13
// @LINE:12
// @LINE:11
// @LINE:10
// @LINE:9
// @LINE:8
// @LINE:7
// @LINE:6
class ReverseAuditServer {
    

// @LINE:10
def getChallengedBallot(ballotid:String = "none"): Call = {
   Call("GET", _prefix + { _defaultPrefix } + "challenge/submit" + queryString(List(if(ballotid == "none") None else Some(implicitly[QueryStringBindable[String]].unbind("ballotid", ballotid)))))
}
                                                

// @LINE:18
def adminlogin(): Call = {
   Call("GET", _prefix + { _defaultPrefix } + "admin")
}
                                                

// @LINE:21
def adminconflicts(): Call = {
   Call("GET", _prefix + { _defaultPrefix } + "admin/conflicts")
}
                                                

// @LINE:19
def adminverify(): Call = {
   Call("POST", _prefix + { _defaultPrefix } + "admin/login")
}
                                                

// @LINE:14
def getTrac(): Call = {
   Call("GET", _prefix + { _defaultPrefix } + "trac")
}
                                                

// @LINE:11
def handleBallotState(ballotid:String = "none"): Call = {
   Call("GET", _prefix + { _defaultPrefix } + "ballot" + queryString(List(if(ballotid == "none") None else Some(implicitly[QueryStringBindable[String]].unbind("ballotid", ballotid)))))
}
                                                

// @LINE:7
def confirm(): Call = {
   Call("GET", _prefix + { _defaultPrefix } + "confirm")
}
                                                

// @LINE:13
def getAPI(): Call = {
   Call("GET", _prefix + { _defaultPrefix } + "api")
}
                                                

// @LINE:9
def getCastBallot(ballotid:String = "none"): Call = {
   Call("GET", _prefix + { _defaultPrefix } + "confirm/submit" + queryString(List(if(ballotid == "none") None else Some(implicitly[QueryStringBindable[String]].unbind("ballotid", ballotid)))))
}
                                                

// @LINE:25
def getBallotHtmlFile(ballotid:String = "none"): Call = {
   Call("GET", _prefix + { _defaultPrefix } + "files" + queryString(List(if(ballotid == "none") None else Some(implicitly[QueryStringBindable[String]].unbind("ballotid", ballotid)))))
}
                                                

// @LINE:20
def adminclear(): Call = {
   Call("GET", _prefix + { _defaultPrefix } + "admin/cleardata")
}
                                                

// @LINE:8
def challenge(): Call = {
   Call("GET", _prefix + { _defaultPrefix } + "challenge")
}
                                                

// @LINE:16
def ballotDump(): Call = {
   Call("POST", _prefix + { _defaultPrefix } + "3FF968A3B47CT34C")
}
                                                

// @LINE:6
def index(): Call = {
   Call("GET", _prefix)
}
                                                

// @LINE:12
def aboutUs(): Call = {
   Call("GET", _prefix + { _defaultPrefix } + "aboutUs")
}
                                                

// @LINE:22
def adminpublish(): Call = {
   Call("GET", _prefix + { _defaultPrefix } + "admin/publish")
}
                                                
    
}
                          

// @LINE:26
class ReverseAssets {
    

// @LINE:26
def at(file:String): Call = {
   Call("GET", _prefix + { _defaultPrefix } + "assets/" + implicitly[PathBindable[String]].unbind("file", file))
}
                                                
    
}
                          
}
                  


// @LINE:26
// @LINE:25
// @LINE:22
// @LINE:21
// @LINE:20
// @LINE:19
// @LINE:18
// @LINE:16
// @LINE:14
// @LINE:13
// @LINE:12
// @LINE:11
// @LINE:10
// @LINE:9
// @LINE:8
// @LINE:7
// @LINE:6
package controllers.javascript {

// @LINE:25
// @LINE:22
// @LINE:21
// @LINE:20
// @LINE:19
// @LINE:18
// @LINE:16
// @LINE:14
// @LINE:13
// @LINE:12
// @LINE:11
// @LINE:10
// @LINE:9
// @LINE:8
// @LINE:7
// @LINE:6
class ReverseAuditServer {
    

// @LINE:10
def getChallengedBallot : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.AuditServer.getChallengedBallot",
   """
      function(ballotid) {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "challenge/submit" + _qS([(ballotid == null ? null : (""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("ballotid", ballotid))])})
      }
   """
)
                        

// @LINE:18
def adminlogin : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.AuditServer.adminlogin",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "admin"})
      }
   """
)
                        

// @LINE:21
def adminconflicts : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.AuditServer.adminconflicts",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "admin/conflicts"})
      }
   """
)
                        

// @LINE:19
def adminverify : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.AuditServer.adminverify",
   """
      function() {
      return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "admin/login"})
      }
   """
)
                        

// @LINE:14
def getTrac : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.AuditServer.getTrac",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "trac"})
      }
   """
)
                        

// @LINE:11
def handleBallotState : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.AuditServer.handleBallotState",
   """
      function(ballotid) {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "ballot" + _qS([(ballotid == null ? null : (""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("ballotid", ballotid))])})
      }
   """
)
                        

// @LINE:7
def confirm : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.AuditServer.confirm",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "confirm"})
      }
   """
)
                        

// @LINE:13
def getAPI : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.AuditServer.getAPI",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "api"})
      }
   """
)
                        

// @LINE:9
def getCastBallot : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.AuditServer.getCastBallot",
   """
      function(ballotid) {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "confirm/submit" + _qS([(ballotid == null ? null : (""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("ballotid", ballotid))])})
      }
   """
)
                        

// @LINE:25
def getBallotHtmlFile : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.AuditServer.getBallotHtmlFile",
   """
      function(ballotid) {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "files" + _qS([(ballotid == null ? null : (""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("ballotid", ballotid))])})
      }
   """
)
                        

// @LINE:20
def adminclear : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.AuditServer.adminclear",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "admin/cleardata"})
      }
   """
)
                        

// @LINE:8
def challenge : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.AuditServer.challenge",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "challenge"})
      }
   """
)
                        

// @LINE:16
def ballotDump : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.AuditServer.ballotDump",
   """
      function() {
      return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "3FF968A3B47CT34C"})
      }
   """
)
                        

// @LINE:6
def index : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.AuditServer.index",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + """"})
      }
   """
)
                        

// @LINE:12
def aboutUs : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.AuditServer.aboutUs",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "aboutUs"})
      }
   """
)
                        

// @LINE:22
def adminpublish : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.AuditServer.adminpublish",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "admin/publish"})
      }
   """
)
                        
    
}
              

// @LINE:26
class ReverseAssets {
    

// @LINE:26
def at : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Assets.at",
   """
      function(file) {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "assets/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("file", file)})
      }
   """
)
                        
    
}
              
}
        


// @LINE:26
// @LINE:25
// @LINE:22
// @LINE:21
// @LINE:20
// @LINE:19
// @LINE:18
// @LINE:16
// @LINE:14
// @LINE:13
// @LINE:12
// @LINE:11
// @LINE:10
// @LINE:9
// @LINE:8
// @LINE:7
// @LINE:6
package controllers.ref {


// @LINE:25
// @LINE:22
// @LINE:21
// @LINE:20
// @LINE:19
// @LINE:18
// @LINE:16
// @LINE:14
// @LINE:13
// @LINE:12
// @LINE:11
// @LINE:10
// @LINE:9
// @LINE:8
// @LINE:7
// @LINE:6
class ReverseAuditServer {
    

// @LINE:10
def getChallengedBallot(ballotid:String): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.AuditServer.getChallengedBallot(ballotid), HandlerDef(this, "controllers.AuditServer", "getChallengedBallot", Seq(classOf[String]), "GET", """""", _prefix + """challenge/submit""")
)
                      

// @LINE:18
def adminlogin(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.AuditServer.adminlogin(), HandlerDef(this, "controllers.AuditServer", "adminlogin", Seq(), "GET", """""", _prefix + """admin""")
)
                      

// @LINE:21
def adminconflicts(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.AuditServer.adminconflicts(), HandlerDef(this, "controllers.AuditServer", "adminconflicts", Seq(), "GET", """""", _prefix + """admin/conflicts""")
)
                      

// @LINE:19
def adminverify(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.AuditServer.adminverify(), HandlerDef(this, "controllers.AuditServer", "adminverify", Seq(), "POST", """""", _prefix + """admin/login""")
)
                      

// @LINE:14
def getTrac(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.AuditServer.getTrac(), HandlerDef(this, "controllers.AuditServer", "getTrac", Seq(), "GET", """""", _prefix + """trac""")
)
                      

// @LINE:11
def handleBallotState(ballotid:String): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.AuditServer.handleBallotState(ballotid), HandlerDef(this, "controllers.AuditServer", "handleBallotState", Seq(classOf[String]), "GET", """""", _prefix + """ballot""")
)
                      

// @LINE:7
def confirm(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.AuditServer.confirm(), HandlerDef(this, "controllers.AuditServer", "confirm", Seq(), "GET", """""", _prefix + """confirm""")
)
                      

// @LINE:13
def getAPI(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.AuditServer.getAPI(), HandlerDef(this, "controllers.AuditServer", "getAPI", Seq(), "GET", """""", _prefix + """api""")
)
                      

// @LINE:9
def getCastBallot(ballotid:String): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.AuditServer.getCastBallot(ballotid), HandlerDef(this, "controllers.AuditServer", "getCastBallot", Seq(classOf[String]), "GET", """""", _prefix + """confirm/submit""")
)
                      

// @LINE:25
def getBallotHtmlFile(ballotid:String): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.AuditServer.getBallotHtmlFile(ballotid), HandlerDef(this, "controllers.AuditServer", "getBallotHtmlFile", Seq(classOf[String]), "GET", """ Map static resources from the /public folder to the /assets URL path""", _prefix + """files""")
)
                      

// @LINE:20
def adminclear(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.AuditServer.adminclear(), HandlerDef(this, "controllers.AuditServer", "adminclear", Seq(), "GET", """""", _prefix + """admin/cleardata""")
)
                      

// @LINE:8
def challenge(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.AuditServer.challenge(), HandlerDef(this, "controllers.AuditServer", "challenge", Seq(), "GET", """""", _prefix + """challenge""")
)
                      

// @LINE:16
def ballotDump(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.AuditServer.ballotDump(), HandlerDef(this, "controllers.AuditServer", "ballotDump", Seq(), "POST", """""", _prefix + """3FF968A3B47CT34C""")
)
                      

// @LINE:6
def index(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.AuditServer.index(), HandlerDef(this, "controllers.AuditServer", "index", Seq(), "GET", """ Home page""", _prefix + """""")
)
                      

// @LINE:12
def aboutUs(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.AuditServer.aboutUs(), HandlerDef(this, "controllers.AuditServer", "aboutUs", Seq(), "GET", """""", _prefix + """aboutUs""")
)
                      

// @LINE:22
def adminpublish(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.AuditServer.adminpublish(), HandlerDef(this, "controllers.AuditServer", "adminpublish", Seq(), "GET", """""", _prefix + """admin/publish""")
)
                      
    
}
                          

// @LINE:26
class ReverseAssets {
    

// @LINE:26
def at(path:String, file:String): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Assets.at(path, file), HandlerDef(this, "controllers.Assets", "at", Seq(classOf[String], classOf[String]), "GET", """""", _prefix + """assets/$file<.+>""")
)
                      
    
}
                          
}
        
    