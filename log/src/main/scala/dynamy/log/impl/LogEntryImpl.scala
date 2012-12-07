package dynamy.log.impl
import org.osgi.service.log.LogEntry
import org.osgi.framework.Bundle
import org.osgi.framework.ServiceReference

case class LogEntryImpl(
    bundle: Bundle,
    sr: ServiceReference[_],
    level: Int,
    message: String,
    exception: Throwable) extends LogEntry {
  
  val time = System.currentTimeMillis()

  override def getBundle(): Bundle = bundle

  override def getServiceReference(): ServiceReference[_] = sr

  override def getLevel(): Int = level

  override def getMessage(): String = message

  override def getException(): Throwable = exception

  override def getTime(): Long = time

}
