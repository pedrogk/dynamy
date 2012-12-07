package dynamy.cache

trait DynamyCacheService {
  def getCache: DynamyCache
  def configure(): Unit
}

trait DynamyCache {
  def get[T](key: String): T
  def get[T](key: String, timeout: Long): T
  def set[T](key: String, value: T): Unit
  def set[T](key: String, exp: Int, value: T): Unit
}

