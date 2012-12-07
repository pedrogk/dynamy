import sbt._
import sbt.Keys._
import xerial.sbt.Pack._

object Build extends sbt.Build {

  import Dependencies._

  val commonDeps = Seq (
    felixFramework
  )

  lazy val root = Project(
    id = "dynamy",
    base = file("."),
    settings = Defaults.defaultSettings ++ packSettings  ++ Seq (libraryDependencies ++= commonDeps) ++
      Seq(
        packMain := Map("dynamy" -> "dynamy.Main")
      )
  )
  
}

object Dependencies {
  val felixVer = "4.0.3"

  val felixFramework = "org.apache.felix" % "org.apache.felix.framework" % felixVer

}
