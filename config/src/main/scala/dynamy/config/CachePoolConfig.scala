package dynamy.config

import scala.reflect.BeanProperty

class CachePoolConfig {
  @BeanProperty var name: String = _
  @BeanProperty var failureMode: Boolean = false
  @BeanProperty var connectTimeout: Long = 60 * 1000
  @BeanProperty var mergeFactor: Int = 150
  @BeanProperty var optimizeMergeBuffer: Boolean = true
  @BeanProperty var optimizeGet: Boolean = true
  @BeanProperty var connectionPoolSize: Int = 1
  @BeanProperty var addresses: String = _
}
