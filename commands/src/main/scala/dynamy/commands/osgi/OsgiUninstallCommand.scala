package dynamy.commands.osgi

import dynamy.shell.annotations.DynamyCommandMeta
import org.osgi.framework.Bundle

@DynamyCommandMeta(
  namespace = "osgi",
  name = "uninstall",
  perms = Array("dynamy:osgi:uninstall"),
  description = "Uninstall asked bundles")
class OsgiUninstallCommand extends OsgiBundleCommand {

  def executeBundle(b: Bundle): String = {
    b.uninstall()
    "Bundle " + b.getBundleId() + " uninstalled"
  }

  def exceptionMessage(arg: String, e: Throwable): String =
    "Unable to uninstall bundle " + arg + ": " + e.getMessage()
}
