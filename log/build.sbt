name := "dynamy.log"

version := "1.0.0"

libraryDependencies += "org.osgi" % "org.osgi.core" % "4.3.0" % "provided"

libraryDependencies += "org.osgi" % "org.osgi.compendium" % "4.3.0" % "provided"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.9" % "provided"

libraryDependencies += "ch.qos.logback" % "logback-core" % "1.0.9" % "provided"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.2" % "provided"

osgiSettings

OsgiKeys.bundleActivator := Option("dynamy.log.LogActivator")

OsgiKeys.privatePackage := Seq("dynamy.log.*")

publishTo := Some(Resolver.file("file",  new File("/data/repos/dynamy-bundles")))

OsgiKeys.importPackage := Seq("*")
