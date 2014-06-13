
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
object adminlogin extends BaseScalaTemplate[play.api.templates.HtmlFormat.Appendable,Format[play.api.templates.HtmlFormat.Appendable]](play.api.templates.HtmlFormat) with play.api.templates.Template1[String,play.api.templates.HtmlFormat.Appendable] {

    /**/
    def apply/*1.2*/(message: String):play.api.templates.HtmlFormat.Appendable = {
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
    
    def render(message:String): play.api.templates.HtmlFormat.Appendable = apply(message)
    
    def f:((String) => play.api.templates.HtmlFormat.Appendable) = (message) => apply(message)
    
    def ref: this.type = this

}
                /*
                    -- GENERATED --
                    DATE: Thu Jun 05 14:41:40 CDT 2014
                    SOURCE: /home/mdb12/Workspace/STAR-Vote/web-server/app/views/adminlogin.scala.html
                    HASH: 95955b3430c88f456bbd5bba4c7b37f5a47d01ea
                    MATRIX: 779->1|890->18|927->21|962->48|1001->50|1107->122|1134->141|1174->144|1294->228|1323->235|1406->287
                    LINES: 26->1|29->1|31->3|31->3|31->3|37->9|37->9|37->9|41->13|41->13|45->17
                    -- GENERATED --
                */
            