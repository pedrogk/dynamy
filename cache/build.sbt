name := "dynamy.cache"

libraryDependencies += "org.osgi" % "org.osgi.core" % "4.3.0" % "provided"

libraryDependencies += "org.osgi" % "org.osgi.compendium" % "4.3.0" % "provided"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.9" % "provided"

libraryDependencies += "ch.qos.logback" % "logback-core" % "1.0.9" % "provided"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.2" % "provided"

libraryDependencies += "com.googlecode.xmemcached" % "xmemcached" % "1.3.8"

libraryDependencies += "com.google.code.gson" % "gson" % "2.2.2"

libraryDependencies += "dynamy.util" %% "dynamy.util" % "1.0.0"

version := "1.0.0"

osgiSettings

OsgiKeys.importPackage := Seq("dynamy.cache", "*")

OsgiKeys.bundleActivator := Option("dynamy.cache.xmemcached.Activator")

OsgiKeys.privatePackage := Seq("dynamy.cache.xmemcached.*")

publishTo := Some(Resolver.file("file",  new File("/home/iamedu/Development/just-cloud/dynamy-bundles")))


