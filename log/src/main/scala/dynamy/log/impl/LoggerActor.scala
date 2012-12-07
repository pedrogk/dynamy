package dynamy.log.impl

import scala.actors.Actor
import scala.collection.mutable.ListBuffer

import org.osgi.service.log.LogListener
import org.slf4j.LoggerFactory
import org.osgi.service.log.LogService

case class RegisterListener(listener: LogListener)
case class RemoveListener(listener: LogListener)
case class ListListeners()
case class RemoveAllListeners()

class LoggerActor extends Actor {

  private val listeners = ListBuffer[LogListener]()

  def act() = {
    while (true) {
      receive {
        case LogEntryImpl(bundle, serviceReference, level, message, exception) =>
          val name =
            if(bundle.getSymbolicName() != null) bundle.getSymbolicName()
            else classOf[LoggerActor].getName()
          val logger = LoggerFactory.getLogger(name)
          val func =
            if (level == LogService.LOG_DEBUG)
              logger.debug(_: String, _: Throwable)
            else if (level == LogService.LOG_INFO)
              logger.info(_: String, _: Throwable)
            else if (level == LogService.LOG_WARNING)
              logger.warn(_: String, _: Throwable)
            else
              logger.error(_: String, _: Throwable)

          val m =
            if (serviceReference == null)
              message
            else
              String.format("(%s) %s", serviceReference, message)

          func(m, exception)
        case RegisterListener(listener) =>
          listeners += listener
        case ListListeners() =>
          reply(List(listeners))
        case RemoveAllListeners() =>
          listeners.clear()
          exit()
      }
    }
  }

}
