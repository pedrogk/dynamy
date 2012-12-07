package dynamy.log.impl

import org.osgi.framework.BundleListener
import org.osgi.framework.FrameworkListener
import org.osgi.framework.ServiceListener
import org.osgi.service.log.LogEntry
import org.osgi.framework.FrameworkEvent
import org.osgi.service.log.LogService
import org.osgi.framework.BundleEvent
import org.osgi.framework.ServiceEvent
import org.osgi.service.log.LogListener
import org.slf4j.LoggerFactory

class Log extends BundleListener with FrameworkListener with ServiceListener {

  val slf4jLogger = LoggerFactory.getLogger(classOf[BundleListener])
  val loggerActor = new LoggerActor
  
  loggerActor.start()
  
  val FRAMEWORK_EVENT_MESSAGES =
    Array(
      "FrameworkEvent STARTED",
      "FrameworkEvent ERROR",
      "FrameworkEvent PACKAGES REFRESHED",
      "FrameworkEvent STARTLEVEL CHANGED",
      "FrameworkEvent WARNING",
      "FrameworkEvent INFO")

  val BUNDLE_EVENT_MESSAGES =
    Array(
      "BundleEvent INSTALLED",
      "BundleEvent STARTED",
      "BundleEvent STOPPED",
      "BundleEvent UPDATED",
      "BundleEvent UNINSTALLED",
      "BundleEvent RESOLVED",
      "BundleEvent UNRESOLVED")

  val SERVICE_EVENT_MESSAGES =
    Array(
      "ServiceEvent REGISTERED",
      "ServiceEvent MODIFIED",
      "ServiceEvent UNREGISTERING")

  override def frameworkEvent(event: FrameworkEvent) = {
    val eventType = event.getType()
    var message: String = ""
    val t =
      if (eventType == FrameworkEvent.ERROR) LogService.LOG_ERROR
      else LogService.LOG_INFO

    for (i <- 0 until FRAMEWORK_EVENT_MESSAGES.size) {
      if (eventType >> i == 1) {
        message = FRAMEWORK_EVENT_MESSAGES(i)
      }
    }

    addEntry(LogEntryImpl(event.getBundle(), null, t, message, event.getThrowable()))
  }

  override def bundleChanged(event: BundleEvent) = {
    val eventType = event.getType()
    var message: String = ""
    val t = LogService.LOG_INFO

    for (i <- 0 until BUNDLE_EVENT_MESSAGES.size) {
      if (eventType >> i == 1) {
        message = BUNDLE_EVENT_MESSAGES(i)
      }
    }

    addEntry(LogEntryImpl(event.getBundle(), null, t, message, null))
  }

  override def serviceChanged(event: ServiceEvent) = {
    val eventType = event.getType()
    var message: String = ""
    val t =
      if (eventType == ServiceEvent.MODIFIED) LogService.LOG_DEBUG
      else LogService.LOG_INFO

    for (i <- 0 until SERVICE_EVENT_MESSAGES.size) {
      if (eventType >> i == 1) {
        message = SERVICE_EVENT_MESSAGES(i)
      }
    }

    addEntry(LogEntryImpl(event.getServiceReference().getBundle(), event.getServiceReference(), t, message, null))
  }
  
  def close() = {
    loggerActor ! RemoveAllListeners()
  }

  def addEntry(entry: LogEntry) =
    loggerActor ! entry
  
  def addServiceListener(logListener: LogListener) =
    loggerActor ! RegisterListener(logListener)
  
  def remoteServiceListener(logListener: LogListener) =
    loggerActor ! RemoveListener(logListener)
    
  def listListeners(): List[LogListener] = {
    val listeners = loggerActor !? ListListeners()
    listeners.asInstanceOf[List[LogListener]]
  }
    

}
