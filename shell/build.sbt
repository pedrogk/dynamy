name := "dynamy.shell"

libraryDependencies += "org.osgi" % "org.osgi.core" % "4.3.0" % "provided"

libraryDependencies += "org.osgi" % "org.osgi.compendium" % "4.3.0" % "provided"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.9" % "provided"

libraryDependencies += "ch.qos.logback" % "logback-core" % "1.0.9" % "provided"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.2" % "provided"

libraryDependencies += "com.google.code.gson" % "gson" % "2.2.2"

libraryDependencies += "org.apache.shiro" % "shiro-core" % "1.2.1"

libraryDependencies += "org.apache.sshd" % "sshd-core" % "0.8.0"

libraryDependencies += "jline" % "jline" % "2.9"

libraryDependencies += "org.bouncycastle" % "bcprov-jdk15on" % "1.47" % "provided"

libraryDependencies += "org.bouncycastle" % "bcpkix-jdk15on" % "1.47" % "provided"

libraryDependencies += "dynamy.util" %% "dynamy.util" % "1.0.0"

libraryDependencies += "dynamy.security" %% "dynamy.security" % "1.0.0"

version := "1.0.0"

osgiSettings

OsgiKeys.importPackage := Seq("dynamy.security.access","dynamy.security.matcher","dynamy.security.realms","dynamy.security.tokens","*")

OsgiKeys.dynamicImportPackage := Seq("*")

OsgiKeys.privatePackage := Seq("dynamy.shell.runtime.*")

OsgiKeys.bundleActivator := Option("dynamy.shell.runtime.Activator")

publishTo := Some(Resolver.file("file",  new File("/home/iamedu/Development/just-cloud/dynamy-bundles")))


