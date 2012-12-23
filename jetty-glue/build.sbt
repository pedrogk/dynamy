name := "dynamy.jetty"

version := "1.0.0"

libraryDependencies += "org.osgi" % "org.osgi.core" % "4.3.0" % "provided"

libraryDependencies += "org.osgi" % "org.osgi.compendium" % "4.3.0" % "provided"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.9" % "provided"

libraryDependencies += "ch.qos.logback" % "logback-core" % "1.0.9" % "provided"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.2" % "provided"

libraryDependencies += "ch.qos.logback" % "logback-access" % "1.0.9"

osgiSettings

OsgiKeys.importPackage := Seq("ch.qos.logback.access.jetty")

OsgiKeys.fragmentHost := Some("org.eclipse.jetty.aggregate.jetty-all-server")

publishTo := Some(Resolver.file("file",  new File("/data/repos/dynamy-bundles")))

