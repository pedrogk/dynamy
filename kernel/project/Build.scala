import sbt._
import sbt.Keys._
import xerial.sbt.Pack._

object Build extends sbt.Build {
  import Resolvers._
  import Dependencies._

  val commonDeps = Seq (
    felixFramework,
    clojure
  )

  lazy val root = Project(
    id = "dynamy",
    base = file("."),
    settings = Defaults.defaultSettings ++ packSettings  ++ Seq (resolvers := dynamyResolvers,
      libraryDependencies ++= commonDeps) ++
      Seq(
        packMain := Map("dynamy" -> "dynamy.Main")
      )
  )
  
}

object Resolvers {
  val sunrepo      = "Sun Maven2 Repo" at "http://download.java.net/maven/2"
  val oraclerepo   = "Oracle Maven2 Repo" at "http://download.oracle.com/maven"
  val typesaferepo = "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"

  val dynamyResolvers = Seq (sunrepo, typesaferepo, oraclerepo)
}

object Dependencies {
  val felixVer   = "4.0.3"
  val akkaVer    = "2.0.4"
  val clojureVer = "1.4.0"

  val felixFramework = "org.apache.felix" % "org.apache.felix.framework" % felixVer
  val clojure        = "org.clojure"      % "clojure"                    % clojureVer
}
