
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
object confirmballot extends BaseScalaTemplate[play.api.templates.Html,Format[play.api.templates.Html]](play.api.templates.HtmlFormat) with play.api.templates.Template3[List[CastBallot],Form[CastBallot],String,play.api.templates.Html] {

    /**/
    def apply/*1.2*/(ballots: List[CastBallot], confirmForm: Form[CastBallot], message: String):play.api.templates.Html = {
        _display_ {import helper.twitterBootstrap._


Seq[Any](format.raw/*1.77*/("""

"""),format.raw/*4.1*/("""
"""),_display_(Seq[Any](/*5.2*/main("Cast Ballot")/*5.21*/ {_display_(Seq[Any](format.raw/*5.23*/("""

    <center><h1>STAR-Vote Ballot Confirmation</h1></center>

    """),_display_(Seq[Any](/*9.6*/if(message != null)/*9.25*/  {_display_(Seq[Any](format.raw/*9.28*/("""
        <center>
            <div style="color:#FF0000;">
                <p>
                    """),_display_(Seq[Any](/*13.22*/message)),format.raw/*13.29*/("""
                </p>
            </div>
        </center>
    """)))})),format.raw/*17.6*/("""

    <p>
        Here, you may confirm that your ballot was tallied after you inserted your printed paper ballot into the ballot box.
        If you did not cast your ballot by doing so, then you may verify it on the challenge page.
        To confirm that your ballot was tallied you may either scan the QR code on your ballot receipt,
        or alternatively enter the ballot ID number on the receipt into the form below.
    </p>
    <p>
        Confirm your ballot by entering its BID in the space below.
    </p>

    <center>
        Enter your BID:
        <input id="confirmidfield" type="text"/>
        <input id="confirmidbutton" type="submit" value="Confirm" onClick="handleForm();"/>
    </center>

    <h2 style="text-align: center">Total Ballots Cast: """),_display_(Seq[Any](/*35.57*/ballots/*35.64*/.size())),format.raw/*35.71*/("""</h2>

    <!--
        <table id="confirmtable" class="table table-striped">
            <tr>
                <th>hash</th>
            </tr>
            """),_display_(Seq[Any](/*42.14*/for(i <- 0 until ballots.size()) yield /*42.46*/ {_display_(Seq[Any](format.raw/*42.48*/("""
                """),_display_(Seq[Any](/*43.18*/defining(ballots.get(i))/*43.42*/ { ballot =>_display_(Seq[Any](format.raw/*43.54*/("""
                    <tr>
                        <td>"""),_display_(Seq[Any](/*45.30*/ballot/*45.36*/.ballotid)),format.raw/*45.45*/("""</td>
                        <td>"""),_display_(Seq[Any](/*46.30*/ballot/*46.36*/.hash)),format.raw/*46.41*/("""</td>
                    </tr>
                """)))})),format.raw/*48.18*/("""
            """)))})),format.raw/*49.14*/("""
        </table>
    -->

    <script>
        function handleForm()"""),format.raw/*54.30*/("""{"""),format.raw/*54.31*/("""
            document.location.href = "/confirm/submit?ballotid=" + document.getElementById("confirmidfield").value;
        """),format.raw/*56.9*/("""}"""),format.raw/*56.10*/("""
    </script>
""")))})))}
    }
    
    def render(ballots:List[CastBallot],confirmForm:Form[CastBallot],message:String): play.api.templates.Html = apply(ballots,confirmForm,message)
    
    def f:((List[CastBallot],Form[CastBallot],String) => play.api.templates.Html) = (ballots,confirmForm,message) => apply(ballots,confirmForm,message)
    
    def ref: this.type = this

}
                /*
                    -- GENERATED --
                    DATE: Wed May 21 12:38:19 CDT 2014
                    SOURCE: /Users/matt/Workspace/STAR-Vote/web-server/app/views/confirmballot.scala.html
                    HASH: e65d1aa6010e7882e20b1573ffa85eb4793792fb
                    MATRIX: 765->1|950->76|978->112|1014->114|1041->133|1080->135|1182->203|1209->222|1249->225|1385->325|1414->332|1509->396|2315->1166|2331->1173|2360->1180|2552->1336|2600->1368|2640->1370|2694->1388|2727->1412|2777->1424|2868->1479|2883->1485|2914->1494|2985->1529|3000->1535|3027->1540|3108->1589|3154->1603|3251->1672|3280->1673|3432->1798|3461->1799
                    LINES: 26->1|30->1|32->4|33->5|33->5|33->5|37->9|37->9|37->9|41->13|41->13|45->17|63->35|63->35|63->35|70->42|70->42|70->42|71->43|71->43|71->43|73->45|73->45|73->45|74->46|74->46|74->46|76->48|77->49|82->54|82->54|84->56|84->56
                    -- GENERATED --
                */
            