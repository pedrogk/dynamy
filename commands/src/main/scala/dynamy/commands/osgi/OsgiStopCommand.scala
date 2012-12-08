package dynamy.commands.osgi

import org.osgi.framework.Bundle
import dynamy.shell.annotations.DynamyCommandMeta

@DynamyCommandMeta(
  namespace = "osgi",
  name = "stop",
  perms = Array("dynamy:osgi:stop"),
  description = "Stop asked bundles")
class OsgiStopCommand extends OsgiBundleCommand {

  def executeBundle(b: Bundle): String = {
    b.stop()
    "Bundle " + b.getBundleId() + " stopped"
  }

  def exceptionMessage(arg: String, e: Throwable): String =
    "Unable to stop bundle " + arg + ": " + e.getMessage()
}
