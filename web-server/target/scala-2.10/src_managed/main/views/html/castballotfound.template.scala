
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
object castballotfound extends BaseScalaTemplate[play.api.templates.Html,Format[play.api.templates.Html]](play.api.templates.HtmlFormat) with play.api.templates.Template2[CastBallot,String,play.api.templates.Html] {

    /**/
    def apply/*1.2*/(ballot: CastBallot)(ballotid: String):play.api.templates.Html = {
        _display_ {import helper.twitterBootstrap._


Seq[Any](format.raw/*1.40*/("""

"""),format.raw/*4.1*/("""

"""),_display_(Seq[Any](/*6.2*/main("Cast Ballot Confirmed")/*6.31*/ {_display_(Seq[Any](format.raw/*6.33*/("""

<center><h1 style="text-align: center">STAR-Vote Cast Ballot Confirmation</h1></center>
<p>
    The BID of your cast ballot is """),_display_(Seq[Any](/*10.37*/ballotid)),format.raw/*10.45*/(""". Our records show that this ballot has been successfully cast and will be correctly tallied in the results of this election.
</p>
<p>
    Thank you for voting!
</p>
""")))})))}
    }
    
    def render(ballot:CastBallot,ballotid:String): play.api.templates.Html = apply(ballot)(ballotid)
    
    def f:((CastBallot) => (String) => play.api.templates.Html) = (ballot) => (ballotid) => apply(ballot)(ballotid)
    
    def ref: this.type = this

}
                /*
                    -- GENERATED --
                    DATE: Wed May 21 12:38:19 CDT 2014
                    SOURCE: /Users/matt/Workspace/STAR-Vote/web-server/app/views/castballotfound.scala.html
                    HASH: 741e680652aa2ed07a4bd2cbded568003234ffb6
                    MATRIX: 744->1|892->39|920->75|957->78|994->107|1033->109|1199->239|1229->247
                    LINES: 26->1|30->1|32->4|34->6|34->6|34->6|38->10|38->10
                    -- GENERATED --
                */
            