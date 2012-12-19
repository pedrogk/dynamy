name := "dynamy.util"

version := "1.0.0"

libraryDependencies += "jline" % "jline" % "2.9" % "provided"

osgiSettings

publishTo := Some(Resolver.file("file",  new File("/data/repos/dynamy-bundles")))

OsgiKeys.exportPackage := Seq("dynamy.cache", "dynamy.shell", "dynamy.shell.annotations")

