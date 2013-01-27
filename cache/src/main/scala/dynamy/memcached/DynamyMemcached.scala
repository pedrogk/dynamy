package dynamy.cache.xmemcached

import dynamy.cache._
import dynamy.config._
import com.google.gson._

import scala.collection.JavaConversions._

import net.rubyeye.xmemcached._
import net.rubyeye.xmemcached.command._
import net.rubyeye.xmemcached.utils._

class DynamyMemcache(val client: MemcachedClient) extends DynamyCache {
  override def get[T](keys: Set[String]): Map[String, T] = {
    val colls: java.util.Collection[String] = keys
    val vals: java.util.Map[String, T] = client.get(colls)
    vals.toMap
  }
  override def get[T](key: String): T = client.get(key)
  override def get[T](key: String, timeout: Long): T = client.get(key, timeout)
  override def set[T](key: String, value: T): Unit = client.set(key, 0, value)
  override def set[T](key: String, exp: Int, value: T): Unit = client.set(key, exp, value)
  override def set[T](key: String, exp: Int, value: T, timeout: Long): Unit = client.set(key, exp, value, timeout)
  override def shutdown() = client.shutdown

  override def getName = client.getName

  override def clear() = client.flushAllWithNoReply()

  override def remove(key: String) = client.delete(key)

}

class DynamyMemcacheManager(val configurationFile: String) extends DynamyCacheService {
  lazy val configService = getConfigService
  def getConfigService = {
    import org.osgi.framework._
    val bc = FrameworkUtil.getBundle(getClass).getBundleContext
    var sr = bc.getServiceReference(classOf[DynamyConfigService])
    while(sr == null) sr = bc.getServiceReference(classOf[DynamyConfigService])
    bc.getService(sr)
  }
  override def build(name: String): DynamyCache = {
    val serverConfig = configService.getConfig
    val cacheConfig = if(serverConfig.getCache().contains(name))
                        serverConfig.getCache.get(name)
                      else
                        serverConfig.getCache.get("default")
    val builder = new XMemcachedClientBuilder(AddrUtil.getAddresses(cacheConfig.addresses))
    builder.setCommandFactory(new BinaryCommandFactory())
    val client = builder.build()
    client.setConnectionSize(32)
    client.setName(name)
    new DynamyMemcache(client) with LoaderWrapper
  }
}
