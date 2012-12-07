package dynamy.log.impl

import org.osgi.service.log.LogService
import org.osgi.framework.ServiceReference
import org.osgi.framework.Bundle

class OsgiLogService(log: Log, bundle: Bundle) extends LogService {

  override def log(level: Int, message: String): Unit = 
    log(null, level, message, null)

  override def log(level: Int, message: String, exception: Throwable): Unit = 
    log(null, level, message, null)

  override def log(sr: ServiceReference[_], level: Int, message: String): Unit = 
    log(sr, level, message, null)

  override def log(sr: ServiceReference[_], level: Int, message: String, exception: Throwable): Unit = {
    val b = if(sr != null) sr.getBundle() else bundle
    log.addEntry(LogEntryImpl(b, sr, level, message, exception))
  }
    

}
