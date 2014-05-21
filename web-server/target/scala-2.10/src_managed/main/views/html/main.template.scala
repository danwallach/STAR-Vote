
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
object main extends BaseScalaTemplate[play.api.templates.Html,Format[play.api.templates.Html]](play.api.templates.HtmlFormat) with play.api.templates.Template2[String,Html,play.api.templates.Html] {

    /**/
    def apply/*1.2*/(title: String)(content: Html):play.api.templates.Html = {
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
    
    def render(title:String,content:Html): play.api.templates.Html = apply(title)(content)
    
    def f:((String) => (Html) => play.api.templates.Html) = (title) => (content) => apply(title)(content)
    
    def ref: this.type = this

}
                /*
                    -- GENERATED --
                    DATE: Wed May 21 12:38:19 CDT 2014
                    SOURCE: /Users/matt/Workspace/STAR-Vote/web-server/app/views/main.scala.html
                    HASH: 19ae5e3911da808b1994365d4638fbd4c6db589b
                    MATRIX: 727->1|834->31|930->92|956->97|1068->174|1082->180|1144->220|1250->291|1264->297|1320->331|1424->399|1439->405|1505->448|1598->505|1613->511|1667->543|2454->1294|2483->1301
                    LINES: 26->1|29->1|35->7|35->7|36->8|36->8|36->8|37->9|37->9|37->9|38->10|38->10|38->10|39->11|39->11|39->11|59->31|59->31
                    -- GENERATED --
                */
            