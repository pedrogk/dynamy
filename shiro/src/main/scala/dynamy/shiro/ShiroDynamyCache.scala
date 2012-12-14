package dynamy.shiro

import org.apache.shiro.cache._

import dynamy.cache._

import org.slf4j._

import scala.collection.JavaConversions._

class ShiroDynamyCache[K, V](cache: DynamyCache) extends Cache[K, V] {
    val logger = LoggerFactory.getLogger(classOf[ShiroDynamyCache[K, V]])

    logger.info("Started shiro dynamy cache with {}", cache)

    override def clear() = {
      try {
        cache.clear()
      } catch {
        case e => throw new CacheException(e)
      }
    }

    override def size: Int = 0

    override def get(key: K): V = cache.get(key.toString)

    override def put(key: K, value: V) = {
      try {
        val prev: Object = cache.get(key.toString)
        cache.set(key.toString, value)
        prev.asInstanceOf[V]
      } catch {
        case e => logger.error("FATAL ERROR", e)
        throw e
      }
    }

    override def remove(key: K) = {
      val prev: Object = cache.get(key.toString)
      cache.remove(key.toString)
      prev.asInstanceOf[V]
    }

    override def keys() = Set[K]()

    override def values() = Set[V]()

    override def toString() = 
      "Memcache [" + cache.getName + "]"

}
