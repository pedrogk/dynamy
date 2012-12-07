package dynamy.log.impl

import org.osgi.framework.ServiceFactory
import org.osgi.service.log.LogService
import org.osgi.framework.Bundle
import org.osgi.framework.ServiceRegistration

class OsgiLogFactory(log: Log) extends ServiceFactory[LogService] {

  override def getService(bundle: Bundle, registration: ServiceRegistration[LogService]): LogService = {
    return new OsgiLogService(log, bundle)
  }

  override def ungetService(bundle: Bundle, registration: ServiceRegistration[LogService], service: LogService): Unit = {
    
  }

}
