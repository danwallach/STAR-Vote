
package views.html

import play.templates._
import play.templates.TemplateMagic._

import play.api.templates._
import play.api.templates.PlayMagic._
import models._
import controllers._
import java.lang._
import java.util._
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import play.api.i18n._
import play.core.j.PlayMagicForJava._
import play.mvc._
import play.data._
import play.api.data.Field
import play.mvc.Http.Context.Implicit._
import views.html._
/**/
object adminlogin extends BaseScalaTemplate[play.api.templates.Html,Format[play.api.templates.Html]](play.api.templates.HtmlFormat) with play.api.templates.Template1[String,play.api.templates.Html] {

    /**/
    def apply/*1.2*/(message: String):play.api.templates.Html = {
        _display_ {

Seq[Any](format.raw/*1.19*/("""

"""),_display_(Seq[Any](/*3.2*/main("Administrator Login")/*3.29*/ {_display_(Seq[Any](format.raw/*3.31*/("""

    <center>
        <h1>Administrator Login</h1>
    </center>

    """),_display_(Seq[Any](/*9.6*/if(message != null)/*9.25*/  {_display_(Seq[Any](format.raw/*9.28*/("""
    <center>
        <div style="color:#FF0000;">
            <p>
                """),_display_(Seq[Any](/*13.18*/message)),format.raw/*13.25*/("""
            </p>
        </div>
    </center>
    """)))})),format.raw/*17.6*/("""
    <p>
        Enter administrative credentials below
    </p>

    <center>
        <form action="/admin/login" method="POST">
            Username: <input type="text" name="username"></br>
            Password: <input type="password" name="password"></br>
            <input type="submit" value="Login">
        </form>
    </center>

    <div id="wrapper"></div>
""")))})))}
    }
    
    def render(message:String): play.api.templates.Html = apply(message)
    
    def f:((String) => play.api.templates.Html) = (message) => apply(message)
    
    def ref: this.type = this

}
                /*
                    -- GENERATED --
                    DATE: Wed May 21 12:38:19 CDT 2014
                    SOURCE: /Users/matt/Workspace/STAR-Vote/web-server/app/views/adminlogin.scala.html
                    HASH: fbe9227af9e32dddfaeb605a4bf0f81e0656408b
                    MATRIX: 728->1|822->18|859->21|894->48|933->50|1039->122|1066->141|1106->144|1226->228|1255->235|1338->287
                    LINES: 26->1|29->1|31->3|31->3|31->3|37->9|37->9|37->9|41->13|41->13|45->17
                    -- GENERATED --
                */
            