package views.html

import java.util._
/**/
object challengeballot extends BaseScalaTemplate[play.api.templates.HtmlFormat.Appendable,Format[play.api.templates.HtmlFormat.Appendable]](play.api.templates.HtmlFormat) with play.api.templates.Template3[List[ChallengedBallot],Form[ChallengedBallot],String,play.api.templates.HtmlFormat.Appendable] {

    /**/
    def apply/*1.2*/(ballots: List[ChallengedBallot], confirmForm: Form[ChallengedBallot], message: String):play.api.templates.HtmlFormat.Appendable = {
        _display_ {


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
                    DATE: Mon Jul 28 14:38:40 CDT 2014
                    SOURCE: C:/Users/Matthew Kindy II/Documents/GitHub/STAR-Vote/web-server/app/views/challengeballot.scala.html
                    HASH: 20b8bf62e5aa113cf0077b17289e7709f438c453
                    MATRIX: 830->1|1061->88|1091->143|1128->146|1160->170|1199->172|1319->257|1347->276|1388->279|1528->383|1557->390|1656->458|2548->1314|2564->1321|2593->1328|2645->1344|2674->1364|2713->1365|2941->1557|2989->1589|3029->1591|3096->1622|3116->1633|3155->1634|3261->1708|3328->1739|3370->1772|3417->1781|3532->1860|3559->1865|3625->1894|3651->1897|3727->1941|3786->1968|3905->2055|4008->2130|4037->2131|4195->2262|4224->2263
                    LINES: 26->1|32->1|34->5|35->6|35->6|35->6|41->12|41->12|41->12|45->16|45->16|49->20|67->38|67->38|67->38|68->39|68->39|68->39|73->44|73->44|73->44|74->45|74->45|74->45|76->47|77->48|77->48|77->48|78->49|78->49|78->49|78->49|79->50|80->51|84->55|89->60|89->60|91->62|91->62
                    -- GENERATED --
                */
            