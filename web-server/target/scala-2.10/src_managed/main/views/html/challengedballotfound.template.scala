
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
object challengedballotfound extends BaseScalaTemplate[play.api.templates.HtmlFormat.Appendable,Format[play.api.templates.HtmlFormat.Appendable]](play.api.templates.HtmlFormat) with play.api.templates.Template2[ChallengedBallot,String,play.api.templates.HtmlFormat.Appendable] {

    /**/
    def apply/*1.2*/(ballot: ChallengedBallot)(ballotid: String):play.api.templates.HtmlFormat.Appendable = {
        _display_ {import helper.twitterBootstrap._


Seq[Any](format.raw/*1.46*/("""

"""),format.raw/*4.1*/("""
    """),_display_(Seq[Any](/*5.6*/main("Challenging Ballot")/*5.32*/ {_display_(Seq[Any](format.raw/*5.34*/("""

    <center><h1 style="text-align: center">STAR-Vote Ballot Challenge</h1></center>
    <p>
        The BID of your challenged ballot is """),_display_(Seq[Any](/*9.47*/ballotid)),format.raw/*9.55*/(""".
    </p>
    <p>
        Below is a visual representation of your ballot as cast by you in the voting booth. You can cross reference the selections shown below with those on your printed ballot.
    </p>
    <p>
        Please note that the visual appearance of the ballot below may be different than your printed copy, however the selections represented below should completely match those on your printed copy.
    </p>
    <p>
        If you question the integrity of your vote due to information shown on this web page, please contact your local election office.
    </p>
    <div id="render" style = "width:600px;">
        <script>
            function httpGet(theUrl)
            """),format.raw/*23.13*/("""{"""),format.raw/*23.14*/("""
                var xmlHttp = null;

                xmlHttp = new XMLHttpRequest();
                xmlHttp.open( "GET", theUrl, false );
                xmlHttp.send( null );
                return xmlHttp.responseText;
            """),format.raw/*30.13*/("""}"""),format.raw/*30.14*/("""
            document.write(httpGet("/files?ballotid=" + """),_display_(Seq[Any](/*31.58*/ballotid)),format.raw/*31.66*/("""))
        </script>
    </div>
""")))})),format.raw/*34.2*/("""
"""))}
    }
    
    def render(ballot:ChallengedBallot,ballotid:String): play.api.templates.HtmlFormat.Appendable = apply(ballot)(ballotid)
    
    def f:((ChallengedBallot) => (String) => play.api.templates.HtmlFormat.Appendable) = (ballot) => (ballotid) => apply(ballot)(ballotid)
    
    def ref: this.type = this

}
                /*
                    -- GENERATED --
                    DATE: Mon Jul 28 15:39:17 CDT 2014
                    SOURCE: /home/mdb12/Workspace/STAR-Vote/web-server/app/views/challengedballotfound.scala.html
                    HASH: 89451e880f3f8c622a3381835d9b3d2a3686df4c
                    MATRIX: 807->1|978->45|1006->81|1046->87|1080->113|1119->115|1294->255|1323->263|2040->952|2069->953|2332->1188|2361->1189|2455->1247|2485->1255|2549->1288
                    LINES: 26->1|30->1|32->4|33->5|33->5|33->5|37->9|37->9|51->23|51->23|58->30|58->30|59->31|59->31|62->34
                    -- GENERATED --
                */
            