package dynamy.cache

trait DynamyCacheService {
  def build(name: String): DynamyCache
}

trait DynamyCache {
  def get[T](keys: Set[String]): Map[String, T]
  def get[T](key: String): T
  def get[T](key: String, timeout: Long): T
  def set[T](key: String, value: T): Unit
  def set[T](key: String, exp: Int, value: T): Unit
  def set[T](key: String, exp: Int, value: T, timeout: Long): Unit

  def remove(key: String): Unit
  def clear(): Unit
  def getName: String

  def shutdown()
}

