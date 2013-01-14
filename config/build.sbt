name := "dynamy.config"

libraryDependencies += "org.osgi" % "org.osgi.core" % "4.3.0" % "provided"

libraryDependencies += "org.osgi" % "org.osgi.compendium" % "4.3.0" % "provided"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.2" % "provided"

libraryDependencies += "org.yaml" % "snakeyaml" % "1.11"

version := "1.0.0"

osgiSettings

OsgiKeys.dynamicImportPackage := Seq("*")

OsgiKeys.importPackage := Seq("*")

OsgiKeys.exportPackage := Seq("dynamy.config")

publishTo := Some(Resolver.file("file",  new File("/home/iamedu/Development/just-cloud/dynamy-bundles")))

