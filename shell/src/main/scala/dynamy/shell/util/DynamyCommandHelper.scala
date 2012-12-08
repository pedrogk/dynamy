package dynamy.shell.runtime.util

import dynamy.shell.DynamyCommand
import dynamy.shell.annotations.DynamyCommandMeta

class DynamyCommandHelper(command: DynamyCommand) {

  lazy val meta = getMeta()
  
  def execute(args: Array[String]) = command.execute(args)
  
  def fullName: String =
  	meta.namespace() + ":" + meta.name()
  	
  def fullAliases: List[String] = {
    val aliases = for(alias <- meta.alias()) yield {
      meta.namespace() + ":" + alias
    }
    aliases.toList
  }

  private def getMeta(): DynamyCommandMeta =
    command.getClass().getAnnotation(classOf[DynamyCommandMeta])

}
