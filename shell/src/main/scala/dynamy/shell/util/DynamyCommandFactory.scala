package dynamy.shell.runtime.util

import scala.collection.JavaConversions._

import org.apache.shiro.SecurityUtils

import org.apache.sshd.server.{CommandFactory, Command}
import org.apache.sshd.server.command._

class DynamyCommandFactory extends CommandFactory {
  override def createCommand(command: String): Command = {
    val completeCommand = command
    var commandString   = command
    val args = commandString.split("\\s+")
    commandString = args(0)
    if (DynamyCommandOsgiHelper.isAlias(commandString)) {
      commandString = DynamyCommandOsgiHelper.getCommandForAlias(commandString)
    }

    if (commandString != null && DynamyCommandOsgiHelper.isCommand(commandString)) {
      val command = new DynamyCommandHelper(DynamyCommandOsgiHelper.getCommand(commandString))
      val permittedCollection = for (p <- command.meta.perms()) yield {
        SecurityUtils.getSubject().isPermitted(p)
      }
      val permitted: Boolean =
        permittedCollection.forall(_ == true) &&
          SecurityUtils.getSubject().hasAllRoles(command.meta.roles().toSeq)
      if (permitted) {
        import org.apache.sshd.server.{Environment, ExitCallback}
        import java.io._
        return new Command {
          var in: InputStream = _
          var out: PrintWriter = _
          var err: PrintWriter = _
          var exitCallback: ExitCallback = _

          override def start(env: Environment) = {
            try {
              out.println(command.execute(args))
              if(exitCallback != null)
                exitCallback.onExit(0)
            } catch {
              case e => {
                e.printStackTrace(err)
                if(exitCallback != null)
                  exitCallback.onExit(-1, e.getMessage)
              }
            }
          }
          override def destroy() = {
          }
          override def setExitCallback(ec: ExitCallback) = 
            this.exitCallback = ec
          override def setInputStream(in: InputStream) = 
            this.in = in
          override def setOutputStream(out: OutputStream) = 
            this.out = new PrintWriter(out)
          override def setErrorStream(err: OutputStream) = 
            this.err = new PrintWriter(err)
        }
      } else {
        return new UnknownCommand(completeCommand)
      }
    }
    return new UnknownCommand(completeCommand)

  }
}
