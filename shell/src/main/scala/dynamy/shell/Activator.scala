package dynamy.shell.runtime

import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext
import org.bouncycastle.jce.provider.BouncyCastleProvider

import java.util.concurrent._
import java.security.Security

class Activator extends BundleActivator {
  
  val systemPort = Integer.parseInt(System.getProperty("dynamy.shell.system.port"))
  val systemKeys = System.getProperty("dynamy.shell.system.keys")
  
  val dynamyPort = Integer.parseInt(System.getProperty("dynamy.shell.dynamy.port"))
  val dynamyKeys = System.getProperty("dynamy.shell.dynamy.keys")
  
  val scriptingPort = Integer.parseInt(System.getProperty("dynamy.shell.scripting.port"))
  val scriptingKeys = System.getProperty("dynamy.shell.scripting.keys")
  
  private lazy val systemShell = new SystemShellService(systemPort, systemKeys)
  private lazy val dynamyShell = new DynamyShellService(dynamyPort, dynamyKeys)
  private lazy val scriptShell = new ScriptingShellService(scriptingPort, scriptingKeys)
  
  private lazy val shells = List(systemShell, dynamyShell, scriptShell) 
  private lazy val executor = Executors.newFixedThreadPool(1)

  override def start(context: BundleContext) = {
    Security.addProvider(new BouncyCastleProvider())
    executor.submit(new Runnable {
      def run() = {
        for(shell <- shells) {
          shell.start()
        }
      }
    })
  }

  override def stop(context: BundleContext) = {
    for(shell <- shells) shell.shutdown()
  }

}
