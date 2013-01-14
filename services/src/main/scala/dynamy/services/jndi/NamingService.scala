package dynamy.services.jndi

import dynamy.config._
import dynamy.services.pool._
import dynamy.services.transactions._
import  com.jolbox.bonecp._

import java.util.{List => _, _}
import javax.transaction.TransactionManager

import org.apache.commons.beanutils._

import org.osgi.framework._

import scala.collection.JavaConversions._

import org.scalaquery.session._
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.ql.extended.H2Driver.Implicit._
import org.scalaquery.ql.extended.{ExtendedTable => Table}
import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql._

import org.slf4j._

import javax.sql._

class NamingService {

  val dataSources = collection.mutable.Map[String, BoneCPDataSource]()
  val sr = collection.mutable.ArrayBuffer[ServiceRegistration[_]]()

  val logger = LoggerFactory.getLogger(classOf[NamingService])

  def init(): Unit = {
    val ds = buildLocalDs()
    createDataSource("jdbc/dynamyServices", ds)
    loadDS(ds)
  }

  def loadDS(ds: DataSource) = {
    val data = getConfigService.getConfig.getDb
    val funcs = scala.collection.mutable.ArrayBuffer[DataSource]()
    for(e <- data.entrySet) {
      val serviceName = e.getKey
      val pool = e.getValue
      try {
        val xaPool = pool.xaPool
        val ds     = if(xaPool) {
          val rawDS = loadPool(pool)
          val dsWrapper = new XADataSourceWrapper(rawDS)
          val boneDS = new TransactionAwareDS()
          boneDS.setDatasourceBean(dsWrapper)
          boneDS.setMaxConnectionsPerPartition(pool.maxConnectionsPerPartition)
          boneDS.setMinConnectionsPerPartition(pool.minConnectionsPerPartition)
          boneDS
        } else {
          val ds = new BoneCPDataSource()
          ds.setDriverClass(pool.driverClass)
          ds.setClassLoader(getClass.getClassLoader)
          ds.setMaxConnectionsPerPartition(pool.maxConnectionsPerPartition)
          ds.setMinConnectionsPerPartition(pool.minConnectionsPerPartition)
          ds.setConnectionTimeoutInMs(pool.connectionTimeoutInMs)
          ds.setJdbcUrl(pool.jdbcUrl)
          ds.setUsername(pool.username)
          ds.setPassword(pool.password)
          ds
        }
        funcs += createDataSource(serviceName, ds)
        logger.info("Registered datasource {}", serviceName)
      } catch {
        case e => logger.error("Cannot create pool", e)
      }
    }
    for(f <- funcs) {
      val cl = Thread.currentThread.getContextClassLoader
      try {
        Thread.currentThread.setContextClassLoader(getClass.getClassLoader)
        val c = f.getConnection
        c.close
      } catch {
        case e => logger.error("Cannot create pool", e)
      } finally {
        Thread.currentThread.setContextClassLoader(cl)
      }
    }
  }

  def loadPool(pool: DBPoolConfig) = {
    val clazz = Class.forName(pool.driverClass, true, getClass.getClassLoader)
    val ds = clazz.newInstance.asInstanceOf[XADataSource]
    val wrapped = new ConvertingWrapDynaBean(ds)
    for((name, value) <- pool.getProperties) {
      wrapped.set(name, value)
    }
    ds
  }

  def shutdown(): Unit = {
    for((_, s) <- dataSources) {
      s.close
    }
    sr.foreach(_.unregister)
  }

  def findTM = {
    val bc = FrameworkUtil.getBundle(getClass).getBundleContext
    val sr = bc.getServiceReference(classOf[TransactionManager])
    bc.getService(sr)
  }

  def getConfigService = { 
    import org.osgi.framework._
    val bc = FrameworkUtil.getBundle(getClass).getBundleContext
    var sr = bc.getServiceReference(classOf[DynamyConfigService])
    while(sr == null) sr = bc.getServiceReference(classOf[DynamyConfigService])
    bc.getService(sr)
  }

  def buildLocalDs() = {
    val ds = new BoneCPDataSource()
    ds.setDriverClass("org.h2.Driver")
    ds.setJdbcUrl("jdbc:h2:" + System.getProperty("prog.home") + "/storage/users/db;AUTO_SERVER=TRUE")
    ds.setUsername("sa")
    ds.setPassword("")
    ds.setClassLoader(getClass.getClassLoader)
    ds
  }

  def createDataSource(name: String, ds: BoneCPDataSource): DataSource = {
    val props = new java.util.Hashtable[String, Object]()

    props.put("osgi.jndi.service.name", name)

    val bc = FrameworkUtil.getBundle(getClass).getBundleContext
    ds match {
      case tmp: XADataSource => {
        sr += bc.registerService(classOf[XADataSource], tmp, props)
      }
      case tmp: DataSource => {
        sr += bc.registerService(classOf[DataSource], tmp, props)
      }
    }

    ds
  }

}
