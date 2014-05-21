
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
object challengeballot extends BaseScalaTemplate[play.api.templates.HtmlFormat.Appendable,Format[play.api.templates.HtmlFormat.Appendable]](play.api.templates.HtmlFormat) with play.api.templates.Template3[List[ChallengedBallot],Form[ChallengedBallot],String,play.api.templates.HtmlFormat.Appendable] {

    /**/
    def apply/*1.2*/(ballots: List[ChallengedBallot], confirmForm: Form[ChallengedBallot], message: String):play.api.templates.HtmlFormat.Appendable = {
        _display_ {import helper._

import java.text.DecimalFormat


Seq[Any](format.raw/*1.89*/("""

"""),format.raw/*5.1*/("""
"""),_display_(Seq[Any](/*6.2*/main("Challenge Ballot")/*6.26*/ {_display_(Seq[Any](format.raw/*6.28*/("""

    <center>
        <h1>STAR-Vote Ballot Challenge</h1>
    </center>

    """),_display_(Seq[Any](/*12.6*/if(message != null)/*12.25*/  {_display_(Seq[Any](format.raw/*12.28*/("""
        <center>
            <div style="color:#FF0000;">
                <p>
                    """),_display_(Seq[Any](/*16.22*/message)),format.raw/*16.29*/("""
                </p>
            </div>
        </center>
    """)))})),format.raw/*20.6*/("""

    <p>
        This page allows you to verify that your ballot was properly handled if you chose to challenge it.
        If your ballot was placed in the ballot box and consequently cast then you may go to the confirm ballots page to confirm that it was tallied.
        Otherwise, by either scanning the QR code on your receipt or entering the ballot ID printed below the QR code on the receipt into the form below,
        you can review the ballot and confirm its integrity.
    </p>
    <p>
        Challenge your ballot by entering its BID in the space below.
    </p>

    <center>
        Enter your BID:
        <input id="challengeidfield" type="text"/>
        <input id="challengeidbutton" type="submit" value="Confirm" onClick="handleForm();"/>
    </center>

    <h2 style="text-align: center">Total Ballots Challenged: """),_display_(Seq[Any](/*38.63*/ballots/*38.70*/.size())),format.raw/*38.77*/("""</h2>
        """),_display_(Seq[Any](/*39.10*/if(ballots.size()>0)/*39.30*/{_display_(Seq[Any](format.raw/*39.31*/("""
            <center>
                <h3> Ballot IDs </h3>
                <table id="challengetable" class="table-striped" border="1">
                    <tr>
                        """),_display_(Seq[Any](/*44.26*/for(i <- 0 until ballots.size()) yield /*44.58*/ {_display_(Seq[Any](format.raw/*44.60*/("""
                            """),_display_(Seq[Any](/*45.30*/if(i%6 ==0)/*45.41*/{_display_(Seq[Any](format.raw/*45.42*/("""
                                </tr><tr>
                            """)))})),format.raw/*47.30*/("""
                            """),_display_(Seq[Any](/*48.30*/defining(ballots.get(i).ballotid)/*48.63*/ { bid =>_display_(Seq[Any](format.raw/*48.72*/("""
                                <td></b><a href="/challenge/submit?ballotid="""),_display_(Seq[Any](/*49.78*/(bid))),format.raw/*49.83*/("""" style="color:#0000FF"> <b>"""),_display_(Seq[Any](/*49.112*/bid)),format.raw/*49.115*/("""</b></a></td>
                            """)))})),format.raw/*50.30*/("""
                        """)))})),format.raw/*51.26*/("""
                    </tr>
                </table>
            </center>
        """)))})),format.raw/*55.10*/("""

<div id="wrapper"></div>

    <script>
        function handleForm()"""),format.raw/*60.30*/("""{"""),format.raw/*60.31*/("""
            document.location.href = "/challenge/submit?ballotid=" + document.getElementById("challengeidfield").value;
        """),format.raw/*62.9*/("""}"""),format.raw/*62.10*/("""
    </script>

""")))})))}
    }
    
    def render(ballots:List[ChallengedBallot],confirmForm:Form[ChallengedBallot],message:String): play.api.templates.HtmlFormat.Appendable = apply(ballots,confirmForm,message)
    
    def f:((List[ChallengedBallot],Form[ChallengedBallot],String) => play.api.templates.HtmlFormat.Appendable) = (ballots,confirmForm,message) => apply(ballots,confirmForm,message)
    
    def ref: this.type = this

}
                /*
                    -- GENERATED --
                    DATE: Wed May 21 16:19:19 CDT 2014
                    SOURCE: /home/mdb12/STAR-Vote/web-server/app/views/challengeballot.scala.html
                    HASH: 1d34b8c29827e6f4fb98c8dff884975ee0ee9dfd
                    MATRIX: 830->1|1059->88|1087->139|1123->141|1155->165|1194->167|1308->246|1336->265|1377->268|1513->368|1542->375|1637->439|2511->1277|2527->1284|2556->1291|2607->1306|2636->1326|2675->1327|2898->1514|2946->1546|2986->1548|3052->1578|3072->1589|3111->1590|3215->1662|3281->1692|3323->1725|3370->1734|3484->1812|3511->1817|3577->1846|3603->1849|3678->1892|3736->1918|3851->2001|3949->2071|3978->2072|4134->2201|4163->2202
                    LINES: 26->1|32->1|34->5|35->6|35->6|35->6|41->12|41->12|41->12|45->16|45->16|49->20|67->38|67->38|67->38|68->39|68->39|68->39|73->44|73->44|73->44|74->45|74->45|74->45|76->47|77->48|77->48|77->48|78->49|78->49|78->49|78->49|79->50|80->51|84->55|89->60|89->60|91->62|91->62
                    -- GENERATED --
                */
            