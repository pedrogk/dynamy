package dynamy.cache

trait DynamyCacheService {
  def getCache: DynamyCache
  def configure(): Unit
}

trait DynamyCache {
  def get[T](key: String): T
  def set[T](key: String, value: T): Unit
  def set[T](key: String, time: Long, value: T): Unit
}

