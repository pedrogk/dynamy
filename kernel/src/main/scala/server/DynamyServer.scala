package dynamy.server

import java.io.FileInputStream

import java.lang.management._
import javax.management.remote._
import java.util.{Map => _, _}
import java.util.logging._

import scala.collection.JavaConversions._

import org.osgi.framework.launch._

import org.osgi.framework.startlevel._

import java.nio.file._

class DynamyServer(serverHome: String) {

  val logger = Logger.getLogger("dynamy.server.DynamyServer")

  val frameworkFactory = ServiceLoader.load(classOf[FrameworkFactory]).iterator.next
  val configuration = setup()
  val framework = frameworkFactory.newFramework(configuration)

  def setup() = {
    var configuration = Map[String, String]()
    val kernelProperties = Paths.get(serverHome, "conf", "kernel.properties")
    val props = new Properties()
    props.load(new FileInputStream(kernelProperties.toFile))
    for((k, v) <- props) {
      configuration += (k.toString -> v.toString)
      if(k.toString.startsWith("dynamy") || k.toString.startsWith("com.atomikos"))
        System.setProperty(k.toString, v.toString)
    }
    configuration
  }

  def loadJMX() = {
    try {
      val host = configuration.get("dynamy.jmxmp.host").get
      val port = configuration.get("dynamy.jmxmp.port").get.toInt
      val conn = JMXConnectorServerFactory.newJMXConnectorServer(
        new JMXServiceURL("jmxmp", host, port),
        null,
        ManagementFactory.getPlatformMBeanServer())
      conn.start
    } catch {
      case e: Throwable => logger.log(Level.SEVERE, "Cannot start JMX", e)
    }
  }

  def loadDrivers() = {
    import java.util.ServiceLoader
    val drivers = ServiceLoader.load(classOf[java.sql.Driver])
    for(d <- drivers) {
      logger.info("Loading driver " + d)
      java.sql.DriverManager.registerDriver(d)
    }
  }

  def installBundles() = {
    val bundles = io.Source.fromInputStream(getClass.getResourceAsStream("/initial.list")).getLines
    val Comment = """#(.*)""".r
    val Bundle  = """([^!]+)!?(start)?!?([1-9][0-9]*)?""".r
    for(b <- bundles) {
      b match {
        case Comment(c) => {
          logger.fine("Got a comment %s".format(c))
  }
        case Bundle(url, start, runlevel) => {
          try {
            val b = framework.getBundleContext.installBundle(url)
            logger.info("Bundle %s installed".format(b))
            if("start".equals(start)) {
              b.start
              logger.info("Bundle %s started".format(b))
        }
        if(runlevel != null) {
          b.adapt(classOf[BundleStartLevel]).setStartLevel(runlevel.toInt)
            }
          } catch {
            case e => logger.log(Level.SEVERE, "Cannot download bundle " + b, e)
          }
          }
            case _ => {}
            }
      }
    }

  def start = {
    setup()
    framework.start()
    logger.info("Started osgi framework")
    installBundles()
    loadDrivers()
    loadJMX()
  }

  def install() = {
    setup()
    framework.start()
    installBundles()
    stop
  }

  def stop  = if(framework != null) framework.stop
  
  def waitForStop = framework.waitForStop(60 * 1000)
}
