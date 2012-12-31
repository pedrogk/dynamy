package dynamy.services.jndi

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
    import org.scalaquery.session._
    val db = Database.forDataSource(ds)
    db withSession {
      val pools = for(d <- DataPool) yield d
      val funcs = scala.collection.mutable.ArrayBuffer[DataSource]()
      for((id, name, serviceName, dsClass, testQuery, xaPool, minPool, maxPool, idleTimeout, reapTimeout, isolation) <- pools.list) {
        try {
          val props = (for(p <- DataPoolProps if p.dsID is id) yield p.name ~ p.value).list
          val ds = if(xaPool) {
            val rawDS = loadPool(dsClass, props)
            val dsWrapper = new XADataSourceWrapper(rawDS)
            val boneDS = new TransactionAwareDS()
            boneDS.setDatasourceBean(dsWrapper)
            boneDS.setMaxConnectionsPerPartition(maxPool)
            boneDS.setMinConnectionsPerPartition(minPool)
            boneDS.setConnectionTimeoutInMs(idleTimeout * 1000)
            boneDS
          } else {
            val ds = new BoneCPDataSource()
            ds.setDriverClass(dsClass)
            ds.setClassLoader(getClass.getClassLoader)
            ds.setMaxConnectionsPerPartition(maxPool)
            ds.setMinConnectionsPerPartition(minPool)
            ds.setConnectionTimeoutInMs(idleTimeout * 1000)
            for((name, value) <- props) {
              if(name.toLowerCase == "url") ds.setJdbcUrl(value)
              else if(name.toLowerCase == "user") ds.setUsername(value)
              else if(name.toLowerCase == "password") ds.setPassword(value)
            }
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
  }

  def loadPool(dsClass: String, props: List[Tuple2[String, String]]) = {
    val clazz = Class.forName(dsClass, true, getClass.getClassLoader)
    val ds = clazz.newInstance.asInstanceOf[XADataSource]
    val wrapped = new ConvertingWrapDynaBean(ds)
    for((name, value) <- props) {
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

object DataPool extends Table[(Int, String, String, String, Option[String], Boolean, Int, Int, Int, Int, Int)]("JDBC_DS") {
  def id = column[Int]("ID", O NotNull)
  def name = column[String]("NAME")
  def serviceName = column[String]("SERVICE_NAME")
  def dsClass     = column[String]("DS_CLASS")
  def testQuery   = column[Option[String]]("TEST_QUERY")
  def xaPool      = column[Boolean]("XA_POOL")
  def minPool     = column[Int]("MIN_POOL")
  def maxPool     = column[Int]("MAX_POOL")
  def idleTimeout = column[Int]("IDLE_TIMEOUT")
  def reapTimeout = column[Int]("REAP_TIMEOUT")
  def isolation   = column[Int]("ISOLATION")
  def * = id ~ name ~ serviceName ~ dsClass ~ testQuery ~ xaPool ~ minPool ~ maxPool ~ idleTimeout ~ reapTimeout ~ isolation
}

object DataPoolProps extends Table[(Int, Int, String, String)]("JDBC_PROPS") {
  def id    = column[Int]("ID", O NotNull)
  def dsID  = column[Int]("ID_DS", O NotNull)
  def name  = column[String]("PROP_NAME")
  def value = column[String]("PROP_VALUE")
  def * = id ~ dsID ~ name ~ value
}
