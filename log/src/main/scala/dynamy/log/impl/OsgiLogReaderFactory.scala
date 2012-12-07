package dynamy.log.impl

import org.osgi.framework.ServiceFactory
import org.osgi.service.log.LogReaderService
import org.osgi.framework.Bundle
import org.osgi.framework.ServiceRegistration

class OsgiLogReaderFactory(log: Log) extends ServiceFactory[LogReaderService] {

  override def getService(bundle: Bundle, registration: ServiceRegistration[LogReaderService]): LogReaderService = {
    return new OsgiLogReaderServiceImpl(log)
  }

  override def ungetService(bundle: Bundle, registration: ServiceRegistration[LogReaderService], service: LogReaderService): Unit = {
    
  }
  
}
