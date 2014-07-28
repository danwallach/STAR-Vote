package views.html

import controllers._
/**/
object main extends BaseScalaTemplate[play.api.templates.HtmlFormat.Appendable,Format[play.api.templates.HtmlFormat.Appendable]](play.api.templates.HtmlFormat) with play.api.templates.Template2[String,Html,play.api.templates.HtmlFormat.Appendable] {

    /**/
    def apply/*1.2*/(title: String)(content: Html):play.api.templates.HtmlFormat.Appendable = {
        _display_ {

Seq[Any](format.raw/*1.32*/("""

<!DOCTYPE html>

<html id = "mainHtml">
<head>
    <title>"""),_display_(Seq[Any](/*7.13*/title)),format.raw/*7.18*/("""</title>
    <link rel="stylesheet" media="(max-device-width: 690px)" href=""""),_display_(Seq[Any](/*8.69*/routes/*8.75*/.Assets.at("stylesheets/mobileMain.css"))),format.raw/*8.115*/("""">
    <link rel="stylesheet" media="(min-device-width: 690px)" href=""""),_display_(Seq[Any](/*9.69*/routes/*9.75*/.Assets.at("stylesheets/main.css"))),format.raw/*9.109*/("""">
    <link rel="stylesheet" type="text/css" media="screen" href=""""),_display_(Seq[Any](/*10.66*/routes/*10.72*/.Assets.at("stylesheets/bootstrap.min.css"))),format.raw/*10.115*/("""">
    <link rel="shortcut icon" type="image/png" href=""""),_display_(Seq[Any](/*11.55*/routes/*11.61*/.Assets.at("images/favicon.png"))),format.raw/*11.93*/("""">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <center>
        <div id = "header">
            <a href="/"><img src="/assets/images/logo.png" alt="STAR-Vote Icon" align="middle|top" width = 100% height = auto></a>
        </div>
        <div id = NBar>
            <ul id="navBar">
                <li><a href="/">Home</a></li>
                <li><a href="/challenge">challenge</a></li>
                <li><a href="/confirm">confirm</a></li>
                <li><a href="/aboutUs">About</a></li>
            </ul>
        </div>
    </center>
</head>
<body style="height:100%;">
    <div id="content" class="container">
        <div class = "vSpace"></div>
        <div id="inner" class="container">
            """),_display_(Seq[Any](/*31.14*/content)),format.raw/*31.21*/("""
        </div>
        <div class = "vSpace"></div>
    </div>
</body>
</html>"""))}
    }
    
    def render(title:String,content:Html): play.api.templates.HtmlFormat.Appendable = apply(title)(content)
    
    def f:((String) => (Html) => play.api.templates.HtmlFormat.Appendable) = (title) => (content) => apply(title)(content)
    
    def ref: this.type = this

}
                /*
                    -- GENERATED --
                    DATE: Mon Jul 28 14:38:40 CDT 2014
                    SOURCE: C:/Users/Matthew Kindy II/Documents/GitHub/STAR-Vote/web-server/app/views/main.scala.html
                    HASH: f63726a7a906bcdc0a9431b5d65b80d55ee7e5d4
                    MATRIX: 778->1|902->31|1004->98|1030->103|1143->181|1157->187|1219->227|1326->299|1340->305|1396->339|1501->408|1516->414|1582->457|1676->515|1691->521|1745->553|2552->1324|2581->1331
                    LINES: 26->1|29->1|35->7|35->7|36->8|36->8|36->8|37->9|37->9|37->9|38->10|38->10|38->10|39->11|39->11|39->11|59->31|59->31
                    -- GENERATED --
                */
            