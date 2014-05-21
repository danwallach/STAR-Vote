
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
object badPage extends BaseScalaTemplate[play.api.templates.Html,Format[play.api.templates.Html]](play.api.templates.HtmlFormat) with play.api.templates.Template0[play.api.templates.Html] {

    /**/
    def apply():play.api.templates.Html = {
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
    
    def render(): play.api.templates.Html = apply()
    
    def f:(() => play.api.templates.Html) = () => apply()
    
    def ref: this.type = this

}
                /*
                    -- GENERATED --
                    DATE: Wed May 21 12:38:19 CDT 2014
                    SOURCE: /Users/matt/Workspace/STAR-Vote/web-server/app/views/badPage.scala.html
                    HASH: 9ac17800535221abb9869c90e188e5885bc14b9e
                    MATRIX: 798->1|821->16|860->18|1087->210|1104->219|1141->235
                    LINES: 29->1|29->1|29->1|37->9|37->9|37->9
                    -- GENERATED --
                */
            