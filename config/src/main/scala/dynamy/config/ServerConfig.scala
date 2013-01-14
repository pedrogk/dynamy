package dynamy.config

import scala.reflect.BeanProperty
import java.nio.file.Paths
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import org.slf4j._

class ServerConfig {
  @BeanProperty var db: java.util.Map[String, DBPoolConfig] = _
  @BeanProperty var cache: java.util.Map[String, CachePoolConfig] = _
  @BeanProperty var user: java.util.Map[String, UserConfig] = _
  @BeanProperty var role: java.util.Map[String, RoleConfig] = _
}

trait DynamyConfigService {
  def loadConfig(): Unit
  def writeConfig(config: ServerConfig): Unit
  def getConfig(): ServerConfig
}

class ServerConfigService extends DynamyConfigService {
  var serverConfig: ServerConfig = _
  val logger = LoggerFactory.getLogger(classOf[ServerConfigService])
  override def loadConfig(): Unit = {
    logger.info("Attempt to start service")
    val path =  Paths.get(System.getProperty("prog.home"), "conf", "server.yaml")
    val yaml = new Yaml(new Constructor(classOf[ServerConfig]))
    val contents = io.Source.fromFile(path.toString).mkString
    serverConfig = yaml.load(contents).asInstanceOf[ServerConfig]
  }
  def writeConfig(config: ServerConfig): Unit = {
    val path =  Paths.get(System.getProperty("prog.home"), "conf", "server.yaml")
  }
  def getConfig(): ServerConfig = serverConfig
}
