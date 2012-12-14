package dynamy.shiro

import org.apache.shiro.cache._
import org.apache.shiro.util._

import org.osgi.framework._

import dynamy.cache._

import org.slf4j._

class ShiroDynamyCacheManager() extends CacheManager with Initializable with Destroyable {
  val logger = LoggerFactory.getLogger(classOf[ShiroDynamyCacheManager])

  override def getCache[K, V](name: String): Cache[K, V] = {
    logger.info("Tried to get cache {}", name)
    val sr = context.getServiceReference(classOf[DynamyCacheService])
    logger.info("Got a sr {}", sr)
    val service = context.getService(sr)
    logger.info("Got a service {}", service)
    new ShiroDynamyCache(service.build(name))
  }

  def context = FrameworkUtil.getBundle(classOf[ShiroDynamyCacheManager]).getBundleContext
  
  override def init() = logger.info("Started shiro cache manager")

  override def destroy() = logger.info("Stopped shiro cache manager")
}

