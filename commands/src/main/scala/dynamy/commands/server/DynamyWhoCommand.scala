package dynamy.commands.server

import dynamy.shell.DynamyCommand
import dynamy.shell.annotations.DynamyCommandMeta
import org.apache.shiro.SecurityUtils

@DynamyCommandMeta(
  namespace = "dynamy",
  name = "who",
  perms = Array("dynamy:who"),
  description = "Prints the logged user")
class DynamyWhoCommand  extends DynamyCommand {

  override def getCompleter() = null
  
  override def execute(args: Array[String]) = {
    println(SecurityUtils.getSubject().getPrincipal())
  }
  
}
