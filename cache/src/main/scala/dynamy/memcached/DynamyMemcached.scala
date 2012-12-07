package dynamy.cache.xmemcached

import dynamy.cache._

import com.google.gson._

import net.rubyeye.xmemcached._
import net.rubyeye.xmemcached.command._
import net.rubyeye.xmemcached.utils._

class DynamyMemcache(val client: MemcachedClient) extends DynamyCache {
  override def get[T](key: String): T = client.get(key)
  override def get[T](key: String, timeout: Long): T = client.get(key, timeout)
  override def set[T](key: String, value: T): Unit = client.set(key, 0, value)
  override def set[T](key: String, exp: Int, value: T): Unit = client.set(key, exp, value)
}

class DynamyMemcacheManager(val configurationFile: String) extends DynamyCacheService {
  var client: MemcachedClient = _
  
  configure()
 
  override def getCache = new DynamyMemcache(client)
  override def configure() = {
	val parser = new JsonParser()
  	val configurationContents = io.Source.fromFile(configurationFile).mkString
	val configuration = parser.parse(configurationContents).getAsJsonObject
    val builder = new XMemcachedClientBuilder(AddrUtil.getAddresses(configuration.get("connection").getAsString))
    builder.setCommandFactory(new BinaryCommandFactory())
    client = builder.build()
  }
}
