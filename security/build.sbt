name := "dynamy.security"

libraryDependencies += "org.osgi" % "org.osgi.core" % "4.3.0" % "provided"

libraryDependencies += "org.osgi" % "org.osgi.compendium" % "4.3.0" % "provided"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.2" % "provided"

libraryDependencies += "org.bouncycastle" % "bcprov-jdk15on" % "1.47" % "provided"

libraryDependencies += "org.bouncycastle" % "bcpkix-jdk15on" % "1.47" % "provided"

libraryDependencies += "org.apache.shiro" % "shiro-core" % "1.2.1" % "provided"

libraryDependencies += "com.jolbox" % "bonecp" % "0.8.0-rc1"

version := "1.0.0"

osgiSettings

OsgiKeys.importPackage := Seq("*")

OsgiKeys.exportPackage := Seq("dynamy.security.*")

OsgiKeys.bundleActivator := Option("dynamy.security.Activator")

publishTo := Some(Resolver.file("file",  new File("/data/repos/dynamy-bundles")))

