// @SOURCE:/home/mdb12/Workspace/STAR-Vote/web-server/conf/routes
// @HASH:732bbe8e4c64d59f907c3fc9acb7cd88507a1ed8
// @DATE:Thu Jun 05 14:41:39 CDT 2014

import Routes.{prefix => _prefix, defaultPrefix => _defaultPrefix}
import play.core._
import play.core.Router._
import play.core.j._

import play.api.mvc._
import play.libs.F

import Router.queryString


// @LINE:23
// @LINE:22
// @LINE:19
// @LINE:18
// @LINE:17
// @LINE:15
// @LINE:13
// @LINE:12
// @LINE:11
// @LINE:10
// @LINE:9
// @LINE:8
// @LINE:7
// @LINE:6
package controllers {

// @LINE:22
// @LINE:19
// @LINE:18
// @LINE:17
// @LINE:15
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
                                                

// @LINE:17
def adminlogin(): Call = {
   Call("GET", _prefix + { _defaultPrefix } + "admin")
}
                                                

// @LINE:18
def adminverify(): Call = {
   Call("POST", _prefix + { _defaultPrefix } + "admin/login")
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
                                                

// @LINE:22
def getBallotHtmlFile(ballotid:String = "none"): Call = {
   Call("GET", _prefix + { _defaultPrefix } + "files" + queryString(List(if(ballotid == "none") None else Some(implicitly[QueryStringBindable[String]].unbind("ballotid", ballotid)))))
}
                                                

// @LINE:19
def adminclear(): Call = {
   Call("POST", _prefix + { _defaultPrefix } + "admin/cleardata")
}
                                                

// @LINE:8
def challenge(): Call = {
   Call("GET", _prefix + { _defaultPrefix } + "challenge")
}
                                                

// @LINE:15
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
                                                
    
}
                          

// @LINE:23
class ReverseAssets {
    

// @LINE:23
def at(file:String): Call = {
   Call("GET", _prefix + { _defaultPrefix } + "assets/" + implicitly[PathBindable[String]].unbind("file", file))
}
                                                
    
}
                          
}
                  


// @LINE:23
// @LINE:22
// @LINE:19
// @LINE:18
// @LINE:17
// @LINE:15
// @LINE:13
// @LINE:12
// @LINE:11
// @LINE:10
// @LINE:9
// @LINE:8
// @LINE:7
// @LINE:6
package controllers.javascript {

// @LINE:22
// @LINE:19
// @LINE:18
// @LINE:17
// @LINE:15
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
                        

// @LINE:17
def adminlogin : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.AuditServer.adminlogin",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "admin"})
      }
   """
)
                        

// @LINE:18
def adminverify : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.AuditServer.adminverify",
   """
      function() {
      return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "admin/login"})
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
                        

// @LINE:22
def getBallotHtmlFile : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.AuditServer.getBallotHtmlFile",
   """
      function(ballotid) {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "files" + _qS([(ballotid == null ? null : (""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("ballotid", ballotid))])})
      }
   """
)
                        

// @LINE:19
def adminclear : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.AuditServer.adminclear",
   """
      function() {
      return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "admin/cleardata"})
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
                        

// @LINE:15
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
                        
    
}
              

// @LINE:23
class ReverseAssets {
    

// @LINE:23
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
        


// @LINE:23
// @LINE:22
// @LINE:19
// @LINE:18
// @LINE:17
// @LINE:15
// @LINE:13
// @LINE:12
// @LINE:11
// @LINE:10
// @LINE:9
// @LINE:8
// @LINE:7
// @LINE:6
package controllers.ref {


// @LINE:22
// @LINE:19
// @LINE:18
// @LINE:17
// @LINE:15
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
                      

// @LINE:17
def adminlogin(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.AuditServer.adminlogin(), HandlerDef(this, "controllers.AuditServer", "adminlogin", Seq(), "GET", """""", _prefix + """admin""")
)
                      

// @LINE:18
def adminverify(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.AuditServer.adminverify(), HandlerDef(this, "controllers.AuditServer", "adminverify", Seq(), "POST", """""", _prefix + """admin/login""")
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
                      

// @LINE:22
def getBallotHtmlFile(ballotid:String): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.AuditServer.getBallotHtmlFile(ballotid), HandlerDef(this, "controllers.AuditServer", "getBallotHtmlFile", Seq(classOf[String]), "GET", """ Map static resources from the /public folder to the /assets URL path""", _prefix + """files""")
)
                      

// @LINE:19
def adminclear(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.AuditServer.adminclear(), HandlerDef(this, "controllers.AuditServer", "adminclear", Seq(), "POST", """""", _prefix + """admin/cleardata""")
)
                      

// @LINE:8
def challenge(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.AuditServer.challenge(), HandlerDef(this, "controllers.AuditServer", "challenge", Seq(), "GET", """""", _prefix + """challenge""")
)
                      

// @LINE:15
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
                      
    
}
                          

// @LINE:23
class ReverseAssets {
    

// @LINE:23
def at(path:String, file:String): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Assets.at(path, file), HandlerDef(this, "controllers.Assets", "at", Seq(classOf[String], classOf[String]), "GET", """""", _prefix + """assets/$file<.+>""")
)
                      
    
}
                          
}
        
    