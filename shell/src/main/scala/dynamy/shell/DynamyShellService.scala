package dynamy.shell.runtime
import org.apache.sshd.SshServer
import org.apache.shiro.subject.Subject
import dynamy.shell.runtime.auth.PublicKeyAuthentication
import dynamy.shell.runtime.auth.PasswordAuthentication
import dynamy.shell.runtime.key.KeyGenerator
import dynamy.shell.runtime.util.{DynamyShellFactory, DynamyCommandFactory}

class DynamyShellService(var port: Int, var keysPath: String) extends ShellService {

  val sshd = SshServer.setUpDefaultServer()

  configure(port, keysPath)

  sshd.setShellFactory(new DynamyShellFactory())
  sshd.setCommandFactory(new DynamyCommandFactory())
  
  def checkPermission = (s: Subject) => {
    s.isPermitted("dynamy:osgi:access")
  }

  sshd.setPublickeyAuthenticator(new PublicKeyAuthentication(checkPermission))
  sshd.setPasswordAuthenticator(new PasswordAuthentication(checkPermission))

  def configure(port: Int, keysPath: String) = {
    this.port = port
    this.keysPath = keysPath
    sshd.setPort(port)
    sshd.setKeyPairProvider(new KeyGenerator(keysPath))
    sshd.getProperties().put("idle-timeout", -1)
  }

  def start() = sshd.start()

  def shutdown() = sshd.stop(true)

}
