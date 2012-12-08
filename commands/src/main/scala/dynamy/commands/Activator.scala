package dynamy.commands

import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext
import dynamy.shell.DynamyCommand
import org.osgi.framework.FrameworkUtil
import java.util.Dictionary
import java.util.Hashtable
import dynamy.commands.osgi._
import dynamy.commands.server._
import dynamy.shell.annotations.DynamyCommandMeta

class Activator extends BundleActivator {

  lazy val commands = Array(
      new OsgiListCommand,
      new OsgiUpdateCommand,
      new OsgiStartCommand,
      new OsgiStopCommand,
      new OsgiRefreshCommand,
      new OsgiResolveCommand,
      new OsgiInstallCommand,
      new OsgiUninstallCommand,
      new OsgiRestartCommand,
      new DynamyWhoCommand)

  override def start(context: BundleContext) = {
	  for(command <- commands) {
	    val meta = command.getClass().getAnnotation(classOf[DynamyCommandMeta])
	    registerCommand(command, meta.namespace(), meta.name(), context)
	  }
  }

  override def stop(context: BundleContext) = {

  }

  def registerCommand(command: DynamyCommand, namespace: String, name: String, context: BundleContext) = {
    val dictionary = new Hashtable[String, String]()
    dictionary.put("dynamy.command.namespace", namespace)
    dictionary.put("dynamy.command.name", name)
    context.registerService(classOf[DynamyCommand], command, dictionary)
  }

}
