package dynamy.commands.osgi

import dynamy.shell.annotations.DynamyCommandMeta
import org.osgi.framework.Bundle
import org.osgi.service.packageadmin.PackageAdmin

@DynamyCommandMeta(
  namespace = "osgi",
  name = "resolve",
  perms = Array("dynamy:osgi:resolve"),
  description = "Resolve asked bundles")
class OsgiResolveCommand extends OsgiBundleCommand {

  def executeBundle(b: Bundle): String = {
    val bc = b.getBundleContext()
    val sr = bc.getServiceReference(classOf[PackageAdmin])
    val packageAdmin = bc.getService(sr)
    packageAdmin.resolveBundles(Array(b))
    "Bundle " + b.getBundleId() + " resolved"
  }

  def exceptionMessage(arg: String, e: Throwable): String =
    "Unable to resolve bundle " + arg + ": " + e.getMessage()
}
