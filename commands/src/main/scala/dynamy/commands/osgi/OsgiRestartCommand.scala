package dynamy.commands.osgi

import dynamy.shell.annotations.DynamyCommandMeta
import org.osgi.framework.Bundle

@DynamyCommandMeta(
  namespace = "osgi",
  name = "restart",
  perms = Array("dynamy:osgi:start", "dynamy:osgi:stop"),
  description = "Restart asked bundles")
class OsgiRestartCommand extends OsgiBundleCommand {

  def executeBundle(b: Bundle): String = {
    b.stop()
    b.start()
    "Bundle " + b.getBundleId() + " restarted"
  }

  def exceptionMessage(arg: String, e: Throwable): String =
    "Unable to restart bundle " + arg + ": " + e.getMessage()
}
