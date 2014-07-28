
package views.html
/**/
object admin extends BaseScalaTemplate[play.api.templates.HtmlFormat.Appendable,Format[play.api.templates.HtmlFormat.Appendable]](play.api.templates.HtmlFormat) with play.api.templates.Template1[String,play.api.templates.HtmlFormat.Appendable] {

    /**/
    def apply/*1.2*/(message: String):play.api.templates.HtmlFormat.Appendable = {
        _display_ {

Seq[Any](format.raw/*1.19*/("""

"""),_display_(Seq[Any](/*3.2*/main("Administrator Login")/*3.29*/ {_display_(Seq[Any](format.raw/*3.31*/("""
    <h1>Administrative Page</h1>
    <p>
        Clicking The button below will clear ballots and ballot ids stored on the Web Server, This action can not be undone.
    </p>
    """),_display_(Seq[Any](/*8.6*/if(message != null)/*8.25*/{_display_(Seq[Any](format.raw/*8.26*/("""
        <center>
            <div style="color:#008800;">
                <p>
                    """),_display_(Seq[Any](/*12.22*/message)),format.raw/*12.29*/("""
                </p>
            </div>
        </center>
    """)))})),format.raw/*16.6*/("""
    <center>
        <form action="/admin/cleardata" method="POST">
            <input type="submit" value="Clear Data">
        </form>
    </center>
""")))})),format.raw/*22.2*/("""
"""))}
    }
    
    def render(message:String): play.api.templates.HtmlFormat.Appendable = apply(message)
    
    def f:((String) => play.api.templates.HtmlFormat.Appendable) = (message) => apply(message)
    
    def ref: this.type = this

}
                /*
                    -- GENERATED --
                    DATE: Thu Jul 03 17:16:52 CDT 2014
                    SOURCE: /home/mdb12/Workspace/STAR-Vote/web-server/app/views/admin.scala.html
                    HASH: 95dc322941a3b54124f4e35968b55bbb5158c055
                    MATRIX: 774->1|885->18|922->21|957->48|996->50|1211->231|1238->250|1276->251|1412->351|1441->358|1536->422|1720->575
                    LINES: 26->1|29->1|31->3|31->3|31->3|36->8|36->8|36->8|40->12|40->12|44->16|50->22
                    -- GENERATED --
                */
            