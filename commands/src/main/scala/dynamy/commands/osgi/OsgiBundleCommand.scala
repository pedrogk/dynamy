package dynamy.commands.osgi

import scala.collection.JavaConversions._
import dynamy.shell.DynamyCommand
import org.osgi.framework.FrameworkUtil
import org.osgi.framework.Bundle
import jline.console.completer.Completer
import java.util.List
import java.nio.CharBuffer
import jline.console.completer.StringsCompleter
import org.slf4j.LoggerFactory

abstract class OsgiBundleCommand extends DynamyCommand {
  
  lazy val logger = LoggerFactory.getLogger(classOf[OsgiBundleCommand])
  
  override def getCompleter() = new Completer {
    def complete(buffer: String, cursor: Int, candidates: List[CharSequence]) = {
      val usedIds = buffer.trim.split("\\s+")
      val bundleIds = for(b <- bundles) yield {
        b.getBundleId().toString()
      }
      val completer = new StringsCompleter(bundleIds.toSeq)
      completer.complete(buffer, cursor, candidates)
    }
  }

  def bundles() = FrameworkUtil
    .getBundle(classOf[OsgiListCommand])
    .getBundleContext()
    .getBundles()
    
  def notFoundMessage(bundleId: Long): String = 
    "Bundle " + bundleId + " not found"
  
  def executeBundle(b: Bundle): String
  
  def exceptionMessage(arg: String, e: Throwable): String

  override def execute(args: Array[String]) = {
    val argsOnly = args.toList.tail
    val results = for (arg <- argsOnly) yield {
      try {
        val bundleId = arg.toLong
        val b = bundles().filter(_.getBundleId() == bundleId)
        if (b.size > 0) {
          executeBundle(b(0))
        } else {
          notFoundMessage(bundleId)
        }
      } catch {
        case e => {
          logger.error("Cannot execute bundle operation", e)
          exceptionMessage(arg, e)
        }
      }
    }
    println(results.mkString("\n"))
  }

}
