package views.html

import java.util._
/**/
object confirmballot extends BaseScalaTemplate[play.api.templates.HtmlFormat.Appendable,Format[play.api.templates.HtmlFormat.Appendable]](play.api.templates.HtmlFormat) with play.api.templates.Template3[List[CastBallot],Form[CastBallot],String,play.api.templates.HtmlFormat.Appendable] {

    /**/
    def apply/*1.2*/(ballots: List[CastBallot], confirmForm: Form[CastBallot], message: String):play.api.templates.HtmlFormat.Appendable = {
        _display_ {


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
    
    def render(ballots:List[CastBallot],confirmForm:Form[CastBallot],message:String): play.api.templates.HtmlFormat.Appendable = apply(ballots,confirmForm,message)
    
    def f:((List[CastBallot],Form[CastBallot],String) => play.api.templates.HtmlFormat.Appendable) = (ballots,confirmForm,message) => apply(ballots,confirmForm,message)
    
    def ref: this.type = this

}
                /*
                    -- GENERATED --
                    DATE: Mon Jul 28 14:38:40 CDT 2014
                    SOURCE: C:/Users/Matthew Kindy II/Documents/GitHub/STAR-Vote/web-server/app/views/confirmballot.scala.html
                    HASH: 0397b300ab1c1bfac01645abf24a671eda82cc4f
                    MATRIX: 816->1|1019->76|1049->115|1086->118|1113->137|1152->139|1258->211|1285->230|1325->233|1465->337|1494->344|1593->412|2417->1200|2433->1207|2462->1214|2661->1377|2709->1409|2749->1411|2804->1430|2837->1454|2887->1466|2980->1523|2995->1529|3026->1538|3098->1574|3113->1580|3140->1585|3223->1636|3270->1651|3372->1725|3401->1726|3555->1853|3584->1854
                    LINES: 26->1|30->1|32->4|33->5|33->5|33->5|37->9|37->9|37->9|41->13|41->13|45->17|63->35|63->35|63->35|70->42|70->42|70->42|71->43|71->43|71->43|73->45|73->45|73->45|74->46|74->46|74->46|76->48|77->49|82->54|82->54|84->56|84->56
                    -- GENERATED --
                */
            