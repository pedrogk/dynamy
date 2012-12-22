package dynamy

import java.nio.file._

import java.io._

class ServerSetup(val serverPath: String) {

  val confPath            = Paths.get(serverPath, "conf")
  val logsPath            = Paths.get(serverPath, "logs")
  val storagePath         = Paths.get(serverPath, "storage")
  val usersPath           = Paths.get(serverPath, "storage", "users")
  val jndiPath            = Paths.get(serverPath, "jndi")
  val kernelProperties    = Paths.get(serverPath, "conf", "kernel.properties")
  val memcachedProperties = Paths.get(serverPath, "conf", "memcached.json")
  val shiroIni            = Paths.get(serverPath, "conf", "shiro.ini")
  val jetty               = Paths.get(serverPath, "conf", "jetty.xml")
  val users               = Map("/users/db.h2.db" -> Paths.get(serverPath, "storage", "users", "db.h2.db"))
  val logbackConf         = Paths.get(serverPath, "conf", "logback.xml")
  val initializedPath     = Paths.get(serverPath, ".initialized")

  def isInitialized = Files.exists(initializedPath)

  def process(cpath: String): InputStream = {
    val lines = scala.io.Source.fromInputStream(getClass.getResourceAsStream(cpath)).mkString
    val string = lines.toString.replaceAll("\\$\\{prog.home\\}", System.getProperty("prog.home"))
    new ByteArrayInputStream(string.getBytes())
  }

  def initialize() = if(!isInitialized) {
    Files.createDirectories(confPath)
    Files.createDirectories(logsPath)
    Files.createDirectories(storagePath)
    Files.createDirectories(usersPath)
    Files.createDirectories(jndiPath)
    Files.copy(process("/kernel.properties"), kernelProperties)
    Files.copy(process("/shiro.ini"), shiroIni)
    Files.copy(process("/jetty.xml"), jetty)
    Files.copy(getClass.getResourceAsStream("/logback.xml"), logbackConf)
    Files.copy(getClass.getResourceAsStream("/memcached.json"), memcachedProperties)
    for((k, v) <- users) Files.copy(getClass.getResourceAsStream(k), v)
    Files.createFile(initializedPath)
  }
	
}
