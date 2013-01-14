name := "dynamy.services"

libraryDependencies += "org.osgi" % "org.osgi.core" % "4.3.0" % "provided"

libraryDependencies += "org.osgi" % "org.osgi.compendium" % "4.3.0" % "provided"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.2" % "provided"

libraryDependencies += "com.jolbox" % "bonecp" % "0.8.0-rc1"

libraryDependencies += "com.h2database" % "h2" % "1.2.127"

libraryDependencies += "org.scalaquery" % "scalaquery_2.9.1-1" % "0.10.0-M1"

libraryDependencies += "javax.transaction" % "jta" % "1.1"

libraryDependencies += "org.apache.servicemix.bundles" % "org.apache.servicemix.bundles.commons-beanutils" % "1.8.3_1"

libraryDependencies += "dynamy.config" %% "dynamy.config" % "1.0.0"

version := "1.0.0"

osgiSettings

OsgiKeys.dynamicImportPackage := Seq("*")

OsgiKeys.importPackage := Seq("*")

OsgiKeys.privatePackage := Seq("dynamy.services.jndi", "dynamy.services.pool", "dynamy.services.transactions")

publishTo := Some(Resolver.file("file",  new File("/home/iamedu/Development/just-cloud/dynamy-bundles")))

