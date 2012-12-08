package dynamy.shell.runtime.util

import scala.collection.JavaConversions._
import jline.console.completer.Completer
import jline.console.completer.AggregateCompleter
import jline.console.completer.StringsCompleter
import jline.console.completer.ArgumentCompleter
import java.util.{List => JList}

class DynamyCompleter extends Completer {

  lazy val delim = new ArgumentCompleter.WhitespaceArgumentDelimiter
  
  def commandCompleter = new AggregateCompleter(
		  new StringsCompleter(DynamyCommandOsgiHelper.findCommandStrings()),
		  new StringsCompleter(DynamyCommandOsgiHelper.findAliasStrings()),
		  new StringsCompleter("exit", "help", "clear"),
		  new StringsCompleter("")
      )
  
  override def complete(buffer: String, cursor: Int, candidates: JList[CharSequence]): Int = {
    val list = delim.delimit(buffer, cursor)
    
    val argIndex = list.getCursorArgumentIndex()
    
    if(argIndex < 0) {
      return -1
    }
    
    if(argIndex == 0) {
      if("".equals(buffer)) {
        for(command <- DynamyCommandOsgiHelper.findCommands()) {
          candidates.add(command.fullName)
        }
        return 0
      }
      return commandCompleter.complete(buffer, cursor, candidates)
    }
    
    val arguments = list.getArguments()
    
    if(arguments.length == 0) {
      return -1
    }
    
    var command = arguments(0)
    
    if(!DynamyCommandOsgiHelper.isCommand(command)) {
      if(!DynamyCommandOsgiHelper.isAlias(command)) {
        return -1
      } else {
        command = DynamyCommandOsgiHelper.getCommandForAlias(command)
      }
    }
    
    val paramCompleter = DynamyCommandOsgiHelper.getParamCompleter(command)
    
    if(paramCompleter != null) {
      return paramCompleter.complete(buffer, cursor, candidates)
    }
    
    -1
  }
}

