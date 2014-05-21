
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
object aboutUs extends BaseScalaTemplate[play.api.templates.HtmlFormat.Appendable,Format[play.api.templates.HtmlFormat.Appendable]](play.api.templates.HtmlFormat) with play.api.templates.Template0[play.api.templates.HtmlFormat.Appendable] {

    /**/
    def apply():play.api.templates.HtmlFormat.Appendable = {
        _display_ {

Seq[Any](_display_(Seq[Any](/*1.2*/main("About Us")/*1.18*/ {_display_(Seq[Any](format.raw/*1.20*/("""

<center><h1>About Us</h1></center>

<p>
    STAR-Vote is an academic exploration into various new developments in voting security research.
    Heavily emphasising paper trails and end to end cryptography the project hopes to provide a more secure
    Direct-Recording Electronic vote system.
</p>

<h2>STAR-Vote</h2>


<p>
    The project was started in 2007 by Dan Wallach PhD of Rice University as VoteBox, a temper-evident, verifiable electronic voting system.
    However, in the coming years the project evolved and today holds an entirely
    different form than the original VoteBox Project, hence the new name - STAR-Vote.
</p>

<p>
    For more information about our project, please visit the VoteBox project page <a target="_blank" href="http://votebox.cs.rice.edu/" style="color:#000000"> here</a>.
</p>

<p>
    The STAR-Vote project is on <a target="_blank" href="https://github.com/mdb92nc/STAR-Vote" style="..."> github </a>
</p>

<p> The projects developing api can be found <a href = "/api" style="..."> here </a> </p>

""")))})))}
    }
    
    def render(): play.api.templates.HtmlFormat.Appendable = apply()
    
    def f:(() => play.api.templates.HtmlFormat.Appendable) = () => apply()
    
    def ref: this.type = this

}
                /*
                    -- GENERATED --
                    DATE: Wed May 21 16:19:19 CDT 2014
                    SOURCE: /home/mdb12/STAR-Vote/web-server/app/views/aboutUs.scala.html
                    HASH: d1cf4f032c61e179562af59a208f3b10e4ee0e51
                    MATRIX: 866->1|890->17|929->19
                    LINES: 29->1|29->1|29->1
                    -- GENERATED --
                */
            