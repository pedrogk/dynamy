import sbt._
import sbt.Keys._
import xerial.sbt.Pack._

object Build extends sbt.Build {
  import Resolvers._
  import Dependencies._

  val commonDeps = Seq (
    osgiFramework,
    clojure,
    simpleJndi
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
  val equinoxVer    = "3.8.1.v20120830-144521"
  val clojureVer    = "1.4.0"

  val osgiFramework = "org.eclipse.tycho"         % "org.eclipse.osgi" % equinoxVer
  val clojure       = "org.clojure"               % "clojure"          % clojureVer
  val simpleJndi    = "simple-jndi"               % "simple-jndi"      % "0.11.4.1"
}
