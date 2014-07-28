package views.html
/**/
object challengedballotfound extends BaseScalaTemplate[play.api.templates.HtmlFormat.Appendable,Format[play.api.templates.HtmlFormat.Appendable]](play.api.templates.HtmlFormat) with play.api.templates.Template2[ChallengedBallot,String,play.api.templates.HtmlFormat.Appendable] {

    /**/
    def apply/*1.2*/(ballot: ChallengedBallot)(ballotid: String):play.api.templates.HtmlFormat.Appendable = {
        _display_ {


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
                    DATE: Mon Jul 28 14:38:40 CDT 2014
                    SOURCE: C:/Users/Matthew Kindy II/Documents/GitHub/STAR-Vote/web-server/app/views/challengedballotfound.scala.html
                    HASH: 6e6b47844c7fffc01d71e0c975422c1d4ee6c66e
                    MATRIX: 807->1|979->45|1009->84|1050->91|1084->117|1123->119|1302->263|1331->271|2062->974|2091->975|2361->1217|2390->1218|2485->1277|2515->1285|2582->1321
                    LINES: 26->1|30->1|32->4|33->5|33->5|33->5|37->9|37->9|51->23|51->23|58->30|58->30|59->31|59->31|62->34
                    -- GENERATED --
                */
            