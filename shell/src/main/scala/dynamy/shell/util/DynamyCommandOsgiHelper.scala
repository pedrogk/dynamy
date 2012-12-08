package dynamy.shell.runtime.util

import scala.collection.JavaConversions._
import dynamy.shell.DynamyCommand
import org.osgi.framework.BundleContext
import org.osgi.framework.FrameworkUtil
import jline.console.completer.Completer

object DynamyCommandOsgiHelper {
  
  def isCommand(command: String): Boolean = {
    getCommand(command) != null
  }
  
  def isAlias(fullAlias: String): Boolean = {
    val splitted = fullAlias.split(":")
    if(splitted.size != 2) return false
    val (namespace, alias) = (splitted(0), splitted(1))
    for(c <- getCommands()) {
      if(c.meta.namespace().equals(namespace) && c.meta.alias().contains(alias))
        return true
    }
    false
  }
  
  def getCommandForAlias(fullAlias: String): String = {
    val splitted = fullAlias.split(":")
    if(splitted.size != 2) return null
    val (namespace, alias) = (splitted(0), splitted(1))
    for(c <- getCommands()) {
      if(c.meta.namespace().equals(namespace) && c.meta.alias().contains(alias))
        return c.fullName
    }
    null
  }
  
  def findCommands(): List[DynamyCommandHelper] = getCommands()
  
  def findCommandStrings(): List[String] = 
    for(c <- findCommands()) yield c.fullName
    
  def findAliasStrings(): List[String] = {
    val aliases = for(c <- findCommands()) yield c.fullAliases
    aliases.flatten
  }
    
  
  def getParamCompleter(fullCommand: String): Completer = {
    getCommand(fullCommand).getCompleter
  }
  
  def getCommand(fullName: String): DynamyCommand = {
    val splitted = fullName.split(":")
    if(splitted.size != 2) return null
    val (namespace, name) = (splitted(0), splitted(1))
    val filter = String.format(
    		"(&(dynamy.command.namespace=%s)(dynamy.command.name=%s))",
    		namespace,
    		name
        )
    val bc = getBundleContext()
    val refs = bc.getServiceReferences(classOf[DynamyCommand], filter)
    if(refs.size() > 1) throw new RuntimeException("Found more than 1 command for " + filter)
    else if(refs.size() == 0) null
    else bc.getService(refs.iterator().next())
  }
  
  def getCommands(): List[DynamyCommandHelper] = {
    val bc = getBundleContext()
    val refs = bc.getServiceReferences(classOf[DynamyCommand], null)
    val list = for(ref <- refs) yield {
      new DynamyCommandHelper(bc.getService(ref))
    }
    list.toList
  }
  
  def getBundleContext(): BundleContext =
    FrameworkUtil.getBundle(classOf[DynamyCommandHelper]).getBundleContext()
  
}
