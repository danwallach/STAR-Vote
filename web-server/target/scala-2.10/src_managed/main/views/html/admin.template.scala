
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
object admin extends BaseScalaTemplate[play.api.templates.Html,Format[play.api.templates.Html]](play.api.templates.HtmlFormat) with play.api.templates.Template1[String,play.api.templates.Html] {

    /**/
    def apply/*1.2*/(message: String):play.api.templates.Html = {
        _display_ {

Seq[Any](format.raw/*1.19*/("""

"""),_display_(Seq[Any](/*3.2*/main("Administrator Login")/*3.29*/ {_display_(Seq[Any](format.raw/*3.31*/("""
    <h1>Administrative Page</h1>
    <p>
        Clicking The button below will clear ballots and ballot ids stored on the Web Server, This action can not be undone.
    </p>
    """),_display_(Seq[Any](/*8.6*/if(message != null)/*8.25*/{_display_(Seq[Any](format.raw/*8.26*/("""
        <center>
            <div style="color:#008800;">
                <p>
                    """),_display_(Seq[Any](/*12.22*/message)),format.raw/*12.29*/("""
                </p>
            </div>
        </center>
    """)))})),format.raw/*16.6*/("""
    <center>
        <form action="/admin/cleardata" method="POST">
            <input type="submit" value="Clear Data">
        </form>
    </center>
""")))})),format.raw/*22.2*/("""
"""))}
    }
    
    def render(message:String): play.api.templates.Html = apply(message)
    
    def f:((String) => play.api.templates.Html) = (message) => apply(message)
    
    def ref: this.type = this

}
                /*
                    -- GENERATED --
                    DATE: Wed May 21 12:38:19 CDT 2014
                    SOURCE: /Users/matt/Workspace/STAR-Vote/web-server/app/views/admin.scala.html
                    HASH: 41547043c17f854edea0a8e20639d05567e672dc
                    MATRIX: 723->1|817->18|854->21|889->48|928->50|1143->231|1170->250|1208->251|1344->351|1373->358|1468->422|1652->575
                    LINES: 26->1|29->1|31->3|31->3|31->3|36->8|36->8|36->8|40->12|40->12|44->16|50->22
                    -- GENERATED --
                */
            