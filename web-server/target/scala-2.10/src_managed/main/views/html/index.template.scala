
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
object index extends BaseScalaTemplate[play.api.templates.HtmlFormat.Appendable,Format[play.api.templates.HtmlFormat.Appendable]](play.api.templates.HtmlFormat) with play.api.templates.Template0[play.api.templates.HtmlFormat.Appendable] {

    /**/
    def apply():play.api.templates.HtmlFormat.Appendable = {
        _display_ {

Seq[Any](_display_(Seq[Any](/*1.2*/main("STAR-Vote Audit Server")/*1.32*/ {_display_(Seq[Any](format.raw/*1.34*/("""

<center><h1>Welcome to the Star-Vote Ballot Verifying Site</h1></center>
<h2 style="test-align: center">How this website works</h2>
<p>
    Star-Vote is a secure voting system that is end-to-end verifiable. This means that anyone, a voter or non-voter, can verify the results of an election without jeopardizing his or her anonymity. Moreover, the voter can be certain that his vote is counted as intended by the voting system.
</p>
<p>
    When using the Star-Vote System, the voter has two options after completing his vote at the booth:
</p>
    <ol>
        <li>
            Scan and deposit his vote into the ballot box, effectively casting his or her vote to be tallied in the results of the election. A BID (Ballot Identification Number) is posted publicly to allow the voter to confirm his ballot was counted.
        </li>
        <li>
            Take the ballot home without scanning. Any ballot that is not scanned and cast will be considered challenged by the voting system, and the contents of all challenged ballots is revealed publicly.
        </li>
    </ol>
<p>
    On this website, you can confirm that your cast ballot was included in tallying the results of the election. You can also review a challenged ballot, which will include an image of your ballot to be cross referenced with the copy you took home. To confirm or challenge, simply scan the QR code or type in the accompanying URL from your ballot receipt. Alternatively, click the confirm or challenge links in the menu above to perform these tasks manually.
</p>
""")))})),format.raw/*22.2*/("""
"""))}
    }
    
    def render(): play.api.templates.HtmlFormat.Appendable = apply()
    
    def f:(() => play.api.templates.HtmlFormat.Appendable) = () => apply()
    
    def ref: this.type = this

}
                /*
                    -- GENERATED --
                    DATE: Mon Jul 28 17:08:41 CDT 2014
                    SOURCE: /home/mdb12/Workspace/STAR-Vote/web-server/app/views/index.scala.html
                    HASH: 96267d47ad13b514f2e119cf4b54aaac41155cb7
                    MATRIX: 864->1|902->31|941->33|2520->1581
                    LINES: 29->1|29->1|29->1|50->22
                    -- GENERATED --
                */
            