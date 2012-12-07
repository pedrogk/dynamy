package dynamy.shell

import jline.console.completer.Completer

trait DynamyCommand {
  def getCompleter: Completer
  def execute(args: Array[String]): Unit
}
