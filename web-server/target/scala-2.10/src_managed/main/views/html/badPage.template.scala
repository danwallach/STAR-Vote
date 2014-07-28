package views.html
/**/
object badPage extends BaseScalaTemplate[play.api.templates.HtmlFormat.Appendable,Format[play.api.templates.HtmlFormat.Appendable]](play.api.templates.HtmlFormat) with play.api.templates.Template0[play.api.templates.HtmlFormat.Appendable] {

    /**/
    def apply():play.api.templates.HtmlFormat.Appendable = {
        _display_ {

Seq[Any](_display_(Seq[Any](/*1.2*/main("Whoops!")/*1.17*/ {_display_(Seq[Any](format.raw/*1.19*/("""

<center><h1> Whoops! </h1></center>

<p>
    <center>
       You might be lost! Click <a href="/" style="color:#000000"> here</a> to return to the home page
       <br>
        Your IP is :"""),_display_(Seq[Any](/*9.22*/request()/*9.31*/.remoteAddress())),format.raw/*9.47*/("""
        Get out now before we find you :)
    </center>
</p>

""")))})))}
    }
    
    def render(): play.api.templates.HtmlFormat.Appendable = apply()
    
    def f:(() => play.api.templates.HtmlFormat.Appendable) = () => apply()
    
    def ref: this.type = this

}
                /*
                    -- GENERATED --
                    DATE: Mon Jul 28 14:38:39 CDT 2014
                    SOURCE: C:/Users/Matthew Kindy II/Documents/GitHub/STAR-Vote/web-server/app/views/badPage.scala.html
                    HASH: 1392e12d31d3cbeeccf645a017d7f0d188a8c072
                    MATRIX: 866->1|889->16|928->18|1163->218|1180->227|1217->243
                    LINES: 29->1|29->1|29->1|37->9|37->9|37->9
                    -- GENERATED --
                */
            