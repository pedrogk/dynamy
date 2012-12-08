package dynamy.commands.osgi

import org.osgi.framework.Bundle
import dynamy.shell.annotations.DynamyCommandMeta

@DynamyCommandMeta(
  namespace = "osgi",
  name = "start",
  perms = Array("dynamy:osgi:start"),
  description = "Start asked bundles")
class OsgiStartCommand extends OsgiBundleCommand {

  def executeBundle(b: Bundle): String = {
    b.start()
    "Bundle " + b.getBundleId() + " started"
  }

  def exceptionMessage(arg: String, e: Throwable): String =
    "Unable to start bundle " + arg + ": " + e.getMessage()
}
