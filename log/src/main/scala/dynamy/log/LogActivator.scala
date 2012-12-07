package dynamy.log

import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.core.util.StatusPrinter
import ch.qos.logback.classic.LoggerContext
import dynamy.log.impl.LogConfiguratorImpl
import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext
import org.slf4j.LoggerFactory
import java.nio.file.FileSystems
import dynamy.log.impl.Log
import org.osgi.service.log.LogService
import impl.OsgiLogFactory
import impl.OsgiLogFactory
import dynamy.log.impl.OsgiLogReaderFactory
import org.osgi.service.log.LogReaderService

class LogActivator extends BundleActivator {
  private lazy val configurator = new LogConfiguratorImpl()
  private var log: Log = _
  
  override def start(context: BundleContext) = {
    val serverHome = System.getProperty("prog.home")
    val dynamyPath = FileSystems.getDefault().getPath(serverHome, "conf", "logback.xml")
	
    configurator configure dynamyPath.toString()
    context.registerService(classOf[LogConfigurator], configurator, null)
    
    log = new Log()
     
    context.addBundleListener(log)
    context.addFrameworkListener(log)
    context.addServiceListener(log)
    
    context.registerService(classOf[LogService].getName(), new OsgiLogFactory(log), null)
    context.registerService(classOf[LogReaderService].getName(), new OsgiLogReaderFactory(log), null)
    
    val loggerReference = context.getServiceReference(classOf[LogService])
    val logger = context.getService(loggerReference)
    
    logger.log(LogService.LOG_INFO, "Welcome to dynamy logger")
  }
  
  override def stop(context: BundleContext) = {
    log.close()
    
    val loggerContext = LoggerFactory.getILoggerFactory().asInstanceOf[LoggerContext]
    try {
	    
	  val configurator = new JoranConfigurator()
	  configurator.setContext(loggerContext)
	    
	  loggerContext.reset()
	  
    } catch {
      case e =>
        StatusPrinter.printInCaseOfErrorsOrWarnings(loggerContext)
    }
  }
  
}
