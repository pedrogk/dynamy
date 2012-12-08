package dynamy.commands.osgi

import org.osgi.framework.Bundle
import dynamy.shell.annotations.DynamyCommandMeta
import org.slf4j.LoggerFactory

@DynamyCommandMeta(
  namespace = "osgi",
  name = "update",
  perms = Array("dynamy:osgi:update", "dynamy:osgi:install"),
  description = "Update asked bundles")
class OsgiUpdateCommand extends OsgiBundleCommand {
  
  def executeBundle(b: Bundle): String = {
    b.update()
    "Bundle " + b.getBundleId() + " updated"
  }

  def exceptionMessage(arg: String, e: Throwable): String = {
    "Unable to update bundle " + arg + ": " + e.getMessage()
  }

}
