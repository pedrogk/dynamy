name := "dynamy.bonecp"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.2" % "provided"

version := "1.0.0"

osgiSettings

OsgiKeys.dynamicImportPackage := Seq("*")

OsgiKeys.privatePackage := Seq("dynamy.bonecp")

OsgiKeys.fragmentHost := Some("com.jolbox.bonecp")

publishTo := Some(Resolver.file("file",  new File("/home/iamedu/Development/just-cloud/dynamy-bundles")))

