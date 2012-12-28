name := "dynamy.transactions"

libraryDependencies += "org.osgi" % "org.osgi.core" % "4.3.0" % "provided"

libraryDependencies += "org.osgi" % "org.osgi.compendium" % "4.3.0" % "provided"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.2" % "provided"

libraryDependencies += "com.atomikos" % "transactions-osgi" % "3.8.0"

version := "1.0.0"

osgiSettings

OsgiKeys.dynamicImportPackage := Seq("*")

OsgiKeys.importPackage := Seq("javax.management", "org.slf4j", "scala", "scala.*")

OsgiKeys.privatePackage := Seq("dynamy.transactions")

OsgiKeys.fragmentHost := Some("com.atomikos.transactions-osgi")

publishTo := Some(Resolver.file("file",  new File("/data/repos/dynamy-bundles")))

