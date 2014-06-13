
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
object badPage extends BaseScalaTemplate[play.api.templates.HtmlFormat.Appendable,Format[play.api.templates.HtmlFormat.Appendable]](play.api.templates.HtmlFormat) with play.api.templates.Template0[play.api.templates.HtmlFormat.Appendable] {

    /**/
    def apply():play.api.templates.HtmlFormat.Appendable = {
        _display_ {

Seq[Any](_display_(Seq[Any](/*1.2*/main("Whoops!")/*1.17*/ {_display_(Seq[Any](format.raw/*1.19*/("""

<center><h1> Whoops! </h1></center>

<p>
    <center>
       You might be lost! Click <a href="/" style="color:#000000"> here</a> to return to the home page
       <br>
        Your IP is :"""),_display_(Seq[Any](/*9.22*/request()/*9.31*/.remoteAddress())),format.raw/*9.47*/("""
        Get out now before we find you :)
    </center>
</p>

""")))})))}
    }
    
    def render(): play.api.templates.HtmlFormat.Appendable = apply()
    
    def f:(() => play.api.templates.HtmlFormat.Appendable) = () => apply()
    
    def ref: this.type = this

}
                /*
                    -- GENERATED --
                    DATE: Thu Jun 05 14:41:40 CDT 2014
                    SOURCE: /home/mdb12/Workspace/STAR-Vote/web-server/app/views/badPage.scala.html
                    HASH: 660c46fbafa58e3a60db6d86400143cc8639818c
                    MATRIX: 866->1|889->16|928->18|1155->210|1172->219|1209->235
                    LINES: 29->1|29->1|29->1|37->9|37->9|37->9
                    -- GENERATED --
                */
            