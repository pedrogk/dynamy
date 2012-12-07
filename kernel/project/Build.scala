import sbt._
import sbt.Keys._
import xerial.sbt.Pack._

object Build extends sbt.Build {
  import Resolvers._
  import Dependencies._

  val commonDeps = Seq (
    osgiFramework,
    xmemcached,
    slf4j,
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
  val equinoxVer    = "3.8.1.v20120830-144521"
  val clojureVer    = "1.4.0"
  val xmemcachedVer = "1.3.8"
  val slf4Ver       = "1.7.2"

  val osgiFramework = "org.eclipse.tycho"         % "org.eclipse.osgi" % equinoxVer
  val clojure       = "org.clojure"               % "clojure"          % clojureVer
  val xmemcached    = "com.googlecode.xmemcached" % "xmemcached"       % xmemcachedVer
  val slf4j         = "org.slf4j"                 % "slf4j-simple"     % slf4Ver
}
