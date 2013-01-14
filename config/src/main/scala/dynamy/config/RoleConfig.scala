package dynamy.config

import scala.reflect.BeanProperty

class RoleConfig {
  @BeanProperty var permissions: java.util.List[String] = _
}
