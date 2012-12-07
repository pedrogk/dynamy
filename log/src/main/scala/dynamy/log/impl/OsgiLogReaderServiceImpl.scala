package dynamy.log.impl

import scala.collection.JavaConversions._

import org.osgi.service.log.LogReaderService
import org.osgi.service.log.LogListener

class OsgiLogReaderServiceImpl(log: Log) extends LogReaderService {

  override def addLogListener(listener: LogListener) =
	log.addServiceListener(listener)
  

  override def removeLogListener(listener: LogListener) =
    log.remoteServiceListener(listener)

  override def getLog(): java.util.Enumeration[_] =
    log.listListeners.iterator
    

}
