name := "dynamy.util"

version := "1.0.0"

libraryDependencies += "jline" % "jline" % "2.9" % "provided"

osgiSettings

publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))

OsgiKeys.exportPackage := Seq("dynamy.cache")

