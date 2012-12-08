package dynamy.commands.osgi

import scala.collection.JavaConversions._
import dynamy.shell.DynamyCommand
import org.osgi.framework.FrameworkUtil
import dynamy.shell.annotations.DynamyCommandMeta
import org.osgi.framework.Bundle
import joptsimple.OptionParser
import joptsimple.OptionSet
import java.io.StringWriter
import org.slf4j.LoggerFactory

@DynamyCommandMeta(
  namespace = "osgi",
  name = "list",
  alias = Array("ll", "lb"),
  perms = Array("dynamy:osgi:list"),
  description = "Print the list of bundles")
class OsgiListCommand extends DynamyCommand {

  lazy val logger = LoggerFactory.getLogger(classOf[OsgiListCommand])
  
  override def getCompleter() = null

  def bundles() = FrameworkUtil
    .getBundle(classOf[OsgiListCommand])
    .getBundleContext()
    .getBundles()

  def defaultFormatter = (b: Bundle, options: OptionSet) => {
    val bundleName = b.getHeaders().get("Bundle-Name")
    val printName =
      if (bundleName != null && !options.has("b")) bundleName
      else b.getSymbolicName()
    String.format("%s (%s)", printName, b.getVersion())
  }

  def listFormatter = (b: Bundle, options: OptionSet) => {
    val s = new StringBuffer
    val bundleName = b.getHeaders().get("Bundle-Name")
    val printName =
      if (bundleName != null && !options.has("b")) bundleName
      else b.getSymbolicName()
    val bundleId =
      b.getBundleId().toString()
    val state: String =
      b.getState() match {
        case Bundle.ACTIVE => "ACTIVE"
        case Bundle.INSTALLED => "INSTALLED"
        case Bundle.RESOLVED => "RESOLVED"
        case Bundle.STARTING => "STARTING"
        case Bundle.STOPPING => "STOPPING"
        case Bundle.UNINSTALLED => "UNINSTALLED"
      }
    String.format("%5s|%-65s|%-25s|%12s", bundleId, printName, b.getVersion(), state)
  }

  override def execute(args: Array[String]): Unit = {
    var formatter =
      defaultFormatter

    val parser = new OptionParser("lbh")
    parser.accepts("help")
    var argsOnly = args.tail.toList
    
    logger.trace("Got data {}", args)


    if (args.head.equals("osgi:ll")) {
      argsOnly = "-l" :: argsOnly
    }

    if (args.head.equals("osgi:lb")) {
      argsOnly = "-b" :: "-l" :: argsOnly
    }
    
    

    val options = parser.parse(argsOnly: _*)
    
    if (options.has("help") || options.has("h")) {
      val writer = new StringWriter
      parser.printHelpOn(writer)
      println(writer.toString())
      return
    }

    if (options.has("l")) {
      formatter = listFormatter
    }

    val list =
      for (bundle <- bundles) yield {
        formatter(bundle, options)
      }

    println(list.mkString("\n"))
  }

}
