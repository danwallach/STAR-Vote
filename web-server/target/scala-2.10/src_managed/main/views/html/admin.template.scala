
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
object admin extends BaseScalaTemplate[play.api.templates.HtmlFormat.Appendable,Format[play.api.templates.HtmlFormat.Appendable]](play.api.templates.HtmlFormat) with play.api.templates.Template1[String,play.api.templates.HtmlFormat.Appendable] {

    /**/
    def apply/*1.2*/(message: String):play.api.templates.HtmlFormat.Appendable = {
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
    <div align="center">
        <TABLE BORDER="0">
        <TR>
        
        <TD>
            <form action="/admin/cleardata" method="GET">
                <input type="submit" value="Clear Data">
            </form>
        </TD>
        
        <TD> 
            <form action="/admin/conflicts" method="GET">
                <input type="submit" value="View Conflicts">
            </form>
        </TD>
        
        <TD> 
            <form action="/admin/publish" method="GET">
                <input type="submit" value="View Results to Publish">
            </form>
        </TD>
        
        </TR>
        </TABLE>
     
       
    </div>
""")))})),format.raw/*44.2*/("""
"""))}
    }
    
    def render(message:String): play.api.templates.HtmlFormat.Appendable = apply(message)
    
    def f:((String) => play.api.templates.HtmlFormat.Appendable) = (message) => apply(message)
    
    def ref: this.type = this

}
                /*
                    -- GENERATED --
                    DATE: Mon Jul 28 15:39:17 CDT 2014
                    SOURCE: /home/mdb12/Workspace/STAR-Vote/web-server/app/views/admin.scala.html
                    HASH: a477d1812097737bbc67dd2fa9325f04b1de37fe
                    MATRIX: 774->1|885->18|922->21|957->48|996->50|1211->231|1238->250|1276->251|1412->351|1441->358|1536->422|2229->1084
                    LINES: 26->1|29->1|31->3|31->3|31->3|36->8|36->8|36->8|40->12|40->12|44->16|72->44
                    -- GENERATED --
                */
            