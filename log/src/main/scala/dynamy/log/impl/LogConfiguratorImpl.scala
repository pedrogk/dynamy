package dynamy.log.impl

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.core.util.StatusPrinter

import dynamy.log.LogConfigurator

import org.slf4j.LoggerFactory

class LogConfiguratorImpl extends LogConfigurator {

  def configure(fileName: String): Unit = {
    val context = LoggerFactory.getILoggerFactory().asInstanceOf[LoggerContext]
    try {
	    
	  val configurator = new JoranConfigurator()
	  configurator.setContext(context)
	    
	  context.reset()
	  configurator.doConfigure(fileName)
	  
    } catch {
      case e =>
        StatusPrinter.printInCaseOfErrorsOrWarnings(context)
    }
  }
  
}
