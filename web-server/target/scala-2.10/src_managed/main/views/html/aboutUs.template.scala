package views.html
/**/
object aboutUs extends BaseScalaTemplate[play.api.templates.HtmlFormat.Appendable,Format[play.api.templates.HtmlFormat.Appendable]](play.api.templates.HtmlFormat) with play.api.templates.Template0[play.api.templates.HtmlFormat.Appendable] {

    /**/
    def apply():play.api.templates.HtmlFormat.Appendable = {
        _display_ {

Seq[Any](_display_(Seq[Any](/*1.2*/main("About Us")/*1.18*/ {_display_(Seq[Any](format.raw/*1.20*/("""

<center><h1>About Us</h1></center>

<p>
    STAR-Vote is an academic exploration into various new developments in voting security research.
    Heavily emphasising paper trails and end to end cryptography the project hopes to provide a more secure
    Direct-Recording Electronic vote system.
</p>

<h2>STAR-Vote</h2>


<p>
    The project was started in 2007 by Dan Wallach PhD of Rice University as VoteBox, a temper-evident, verifiable electronic voting system.
    However, in the coming years the project evolved and today holds an entirely
    different form than the original VoteBox Project, hence the new name - STAR-Vote.
</p>

<p>
    For more information about our project, please visit the VoteBox project page <a target="_blank" href="http://votebox.cs.rice.edu/" style="color:#000000"> here</a>.
</p>

<p>
    The STAR-Vote project is on <a target="_blank" href="https://github.com/mdb92nc/STAR-Vote" style="..."> github </a>
</p>

<p> The projects developing api can be found <a href = "/api" style="..."> here </a> </p>

""")))})))}
    }
    
    def render(): play.api.templates.HtmlFormat.Appendable = apply()
    
    def f:(() => play.api.templates.HtmlFormat.Appendable) = () => apply()
    
    def ref: this.type = this

}
                /*
                    -- GENERATED --
                    DATE: Mon Jul 28 14:38:39 CDT 2014
                    SOURCE: C:/Users/Matthew Kindy II/Documents/GitHub/STAR-Vote/web-server/app/views/aboutUs.scala.html
                    HASH: 719cfb0e88d1afdaa631d66eb4bac4d6a7e30372
                    MATRIX: 866->1|890->17|929->19
                    LINES: 29->1|29->1|29->1
                    -- GENERATED --
                */
            