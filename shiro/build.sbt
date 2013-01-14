name := "dynamy.shiro"

version := "1.0.0"

libraryDependencies += "org.osgi" % "org.osgi.core" % "4.3.0" % "provided"

libraryDependencies += "org.osgi" % "org.osgi.compendium" % "4.3.0" % "provided"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.9" % "provided"

libraryDependencies += "ch.qos.logback" % "logback-core" % "1.0.9" % "provided"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.2" % "provided"

libraryDependencies += "org.apache.shiro" % "shiro-core" % "1.2.1" % "provided"

libraryDependencies += "dynamy.util" %% "dynamy.util" % "1.0.0"

osgiSettings

OsgiKeys.exportPackage := Seq("dynamy.shiro")

publishTo := Some(Resolver.file("file",  new File("/home/iamedu/Development/just-cloud/dynamy-bundles")))

OsgiKeys.importPackage := Seq("*")

