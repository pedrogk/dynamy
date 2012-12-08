package dynamy.commands.osgi

import dynamy.shell.annotations.DynamyCommandMeta
import org.osgi.framework.Bundle
import org.osgi.service.packageadmin.PackageAdmin

@DynamyCommandMeta(
  namespace = "osgi",
  name = "refresh",
  perms = Array("dynamy:osgi:refresh"),
  description = "Refresh asked bundles")
class OsgiRefreshCommand extends OsgiBundleCommand {

  def executeBundle(b: Bundle): String = {
    val bc = b.getBundleContext()
    val sr = bc.getServiceReference(classOf[PackageAdmin])
    val packageAdmin = bc.getService(sr)
    packageAdmin.refreshPackages(Array(b))
    "Bundle " + b.getBundleId() + " refreshed"
  }

  def exceptionMessage(arg: String, e: Throwable): String =
    "Unable to refresh bundle " + arg + ": " + e.getMessage()
}
