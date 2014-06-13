
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
object ballotnotfound extends BaseScalaTemplate[play.api.templates.HtmlFormat.Appendable,Format[play.api.templates.HtmlFormat.Appendable]](play.api.templates.HtmlFormat) with play.api.templates.Template1[String,play.api.templates.HtmlFormat.Appendable] {

    /**/
    def apply/*1.2*/(ballotid: String):play.api.templates.HtmlFormat.Appendable = {
        _display_ {

Seq[Any](format.raw/*1.20*/("""

"""),_display_(Seq[Any](/*3.2*/main("Ballot Not Found")/*3.26*/ {_display_(Seq[Any](format.raw/*3.28*/("""
    <h2>
        Hmm....
    </h2>
    <p>
    It appears that your ballot is not present in our records. Please note that your ballot will not be found here while the election is still in progress. Once an elecion is closed, it may take up to 24 hours for your ballot to be uploaded to this location by election officials.
    </p>
    <p>
    If you scanned a QR code to reach this page, you can try manually entering the URL on your reciept.
    </p>
    <p>
    If problems persist and a full 24-hour period has passed after the closing of election polls, please contact your local election office to pursue a solution.
    </p>
""")))})))}
    }
    
    def render(ballotid:String): play.api.templates.HtmlFormat.Appendable = apply(ballotid)
    
    def f:((String) => play.api.templates.HtmlFormat.Appendable) = (ballotid) => apply(ballotid)
    
    def ref: this.type = this

}
                /*
                    -- GENERATED --
                    DATE: Thu Jun 05 14:41:40 CDT 2014
                    SOURCE: /home/mdb12/Workspace/STAR-Vote/web-server/app/views/ballotnotfound.scala.html
                    HASH: 2227274cb17eea44528bb0b16e71200b5f3c6d01
                    MATRIX: 783->1|895->19|932->22|964->46|1003->48
                    LINES: 26->1|29->1|31->3|31->3|31->3
                    -- GENERATED --
                */
            