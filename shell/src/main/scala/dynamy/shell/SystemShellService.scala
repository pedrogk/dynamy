package dynamy.shell.runtime

import org.apache.sshd.SshServer
import dynamy.shell.runtime.key.KeyGenerator
import org.apache.sshd.server.shell.ProcessShellFactory
import dynamy.shell.runtime.auth.PublicKeyAuthentication
import dynamy.shell.runtime.auth.PasswordAuthentication
import java.util.EnumSet
import org.apache.sshd.common.util.OsUtils
import org.apache.shiro.subject.Subject

class SystemShellService(var port: Int, var keysPath: String) extends ShellService {

  val sshd = SshServer.setUpDefaultServer()

  configure(port, keysPath)

  if (OsUtils.isUNIX()) {
    sshd.setShellFactory(new ProcessShellFactory(Array("/bin/sh", "-i"),
        EnumSet.of(
            ProcessShellFactory.TtyOptions.ONlCr)))
  } else {
    sshd.setShellFactory(new ProcessShellFactory(Array("cmd.exe"),
        EnumSet.of(
            ProcessShellFactory.TtyOptions.Echo,
            ProcessShellFactory.TtyOptions.ICrNl,
            ProcessShellFactory.TtyOptions.ONlCr)))
  }
  
  def checkPermission = (s: Subject) => {
    s.isPermitted("dynamy:host:access")
  }
  
  sshd.setPublickeyAuthenticator(new PublicKeyAuthentication(checkPermission))
  sshd.setPasswordAuthenticator(new PasswordAuthentication(checkPermission))

  def configure(port: Int, keysPath: String) = {
    this.port = port
    this.keysPath = keysPath
    sshd.setPort(port)
    sshd.setKeyPairProvider(new KeyGenerator(keysPath))
    sshd.gerProperties().put("idle-timeout", -1)
  }

  def start() = sshd.start()

  def shutdown() = sshd.stop(true)

}
