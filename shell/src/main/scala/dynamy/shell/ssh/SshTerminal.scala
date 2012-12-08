package dynamy.shell.runtime.ssh

import jline.TerminalSupport
import org.apache.sshd.server.Environment

class SshTerminal(env: Environment) extends TerminalSupport(true) {
  
  setAnsiSupported(true)
  
  override def init(): Unit = {}
  
  override def restore(): Unit = {}
 
  override def getWidth() = {
    var width = 0
    
    try {
      width = env.getEnv().get(Environment.ENV_COLUMNS).toInt
    } catch {
      case _ => width = 0
    }
    
    width
  }
  
  override def getHeight() = {
    var height = 0
    
    try {
      height = env.getEnv().get(Environment.ENV_LINES).toInt
    } catch {
      case _ => height = 0
    }
    
    height
  }
  
}
