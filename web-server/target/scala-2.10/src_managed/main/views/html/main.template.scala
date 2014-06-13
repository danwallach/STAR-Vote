
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
                    DATE: Thu Jun 05 14:41:40 CDT 2014
                    SOURCE: /home/mdb12/Workspace/STAR-Vote/web-server/app/views/main.scala.html
                    HASH: ed8b5228c7e97ec7c26fabb7251762b5e82df564
                    MATRIX: 778->1|902->31|998->92|1024->97|1136->174|1150->180|1212->220|1318->291|1332->297|1388->331|1492->399|1507->405|1573->448|1666->505|1681->511|1735->543|2522->1294|2551->1301
                    LINES: 26->1|29->1|35->7|35->7|36->8|36->8|36->8|37->9|37->9|37->9|38->10|38->10|38->10|39->11|39->11|39->11|59->31|59->31
                    -- GENERATED --
                */
            