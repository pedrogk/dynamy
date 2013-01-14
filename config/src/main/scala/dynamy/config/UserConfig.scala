package dynamy.config

import scala.reflect.BeanProperty

class UserConfig {
  @BeanProperty var username: String = _
  @BeanProperty var password: String = _
  @BeanProperty var keys: java.util.List[String] = _
  @BeanProperty var permissions: java.util.List[String] = _
  @BeanProperty var roles: java.util.List[String] = _
}
