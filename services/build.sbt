name := "dynamy.services"

libraryDependencies += "org.osgi" % "org.osgi.core" % "4.3.0" % "provided"

libraryDependencies += "org.osgi" % "org.osgi.compendium" % "4.3.0" % "provided"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.2" % "provided"

libraryDependencies += "com.atomikos" % "transactions-osgi" % "3.8.0" % "provided"

libraryDependencies += "com.h2database" % "h2" % "1.2.127"

libraryDependencies += "org.scalaquery" % "scalaquery_2.9.1-1" % "0.10.0-M1"

version := "1.0.0"

osgiSettings

OsgiKeys.importPackage := Seq("*")

OsgiKeys.privatePackage := Seq("dynamy.services.jndi")

publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))

