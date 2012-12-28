package dynamy.transactions

import org.slf4j._

class Management {

  val logger = LoggerFactory.getLogger(classOf[Management])

  def init(): Unit = {
    try {
      import com.atomikos.icatch.admin.jmx._
      import java.lang.management._
      import javax.management._
      val service = new JmxTransactionService()
      val jmx = ManagementFactory.getPlatformMBeanServer()
      val mBeanName = new ObjectName ( "atomikos:type=Transactions" )
      jmx.registerMBean ( service , mBeanName )
    } catch {
      case e: Exception => logger.error("Cannot register JMX", e)
    }
  }
  def shutdown(): Unit = {
    logger.info("Should shutdown now")
  }
}

