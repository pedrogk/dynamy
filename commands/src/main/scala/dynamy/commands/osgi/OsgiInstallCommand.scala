package dynamy.commands.osgi

import dynamy.shell.DynamyCommand
import java.io.StringWriter
import joptsimple.OptionParser
import org.slf4j.LoggerFactory
import org.osgi.framework.FrameworkUtil
import dynamy.shell.annotations.DynamyCommandMeta
import org.osgi.framework.startlevel._

@DynamyCommandMeta(
  namespace = "osgi",
  name = "install",
  perms = Array("dynamy:osgi:install"),
  description = "Install bundles")
class OsgiInstallCommand extends DynamyCommand {
  lazy val logger = LoggerFactory.getLogger(classOf[OsgiListCommand])

  override def getCompleter() = null

  override def execute(args: Array[String]) = {
    val argsOnly = args.toList.tail
    val b = FrameworkUtil.getBundle(classOf[OsgiInstallCommand])
    val bc = b.getBundleContext()
    val str = new StringBuffer

    for (arg <- argsOnly) {
      val Bundle  = """([^!]+)!?(start)?!?([1-9][0-9]*)?""".r
      try {
        arg match {
          case Bundle(url, start, runlevel) => {
            val installed = bc.installBundle(url)
            if("start".equals(start)) {
              b.start
              logger.info("Bundle %s started".format(b))
            }
            if(runlevel != null) {
              b.adapt(classOf[BundleStartLevel]).setStartLevel(runlevel.toInt)
            }
            str.append("Bundle ")
            str.append(installed)
            str.append(" installed from ")
            str.append(arg)
          }
        }
      } catch {
        case e => {
            str.append("Cannot install bundle ")
            str.append(arg)
            str.append(": ")
            str.append(e.getMessage())
            logger.error("Unable to install bundle", e)
        }
      }
      str.append("\n")
        }
        str.toString()
    }

  }
