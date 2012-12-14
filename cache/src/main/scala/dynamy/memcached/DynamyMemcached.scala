package dynamy.cache.xmemcached

import dynamy.cache._
import com.google.gson._

import scala.collection.JavaConversions._

import net.rubyeye.xmemcached._
import net.rubyeye.xmemcached.command._
import net.rubyeye.xmemcached.utils._

class DynamyMemcache(val client: MemcachedClient) extends DynamyCache with LoaderWrapper {
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
  override def build(name: String): DynamyCache = {
	val parser = new JsonParser()
  	val configurationContents = io.Source.fromFile(configurationFile).mkString
	val configuration = parser.parse(configurationContents).getAsJsonObject
        val builder = new XMemcachedClientBuilder(AddrUtil.getAddresses(configuration.get("connection").getAsString))
        builder.setCommandFactory(new BinaryCommandFactory())
        val client = builder.build()
        client.setName(name)
        new DynamyMemcache(client)
  }
}
