package dynamy.shell.runtime

trait ShellService {

  def configure(port: Int, keysPath: String)

  def start()
  
  def restart(port: Int, keysPath: String) = {
    configure(port, keysPath)
    shutdown()
    start()
  }
  
  def restart() = {
    shutdown()
    start()
  }
  
  def shutdown()

}
