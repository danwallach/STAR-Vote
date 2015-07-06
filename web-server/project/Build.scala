import play.Project._
import sbt.Keys._
import sbt._

object ApplicationBuild extends Build {

  val appName         = "web-server"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    javaCore,
    javaJdbc,
    javaEbean,
    "mysql" % "mysql-connector-java" % "5.1.18"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    libraryDependencies ++= Seq(
      "org.avaje.ebeanorm" % "avaje-ebeanorm" % "3.3.3",
      "com.typesafe.play" % "play-ebean-33-compat" % "1.0.0",
      "be.objectify" %% "deadbolt-java" % "2.2.1-RC2"
    )
  )

  resolvers += "itext repository" at "http://jasperreports.sourceforge.net/maven2/"
}
