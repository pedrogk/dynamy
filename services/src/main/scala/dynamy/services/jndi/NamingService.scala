package dynamy.services.jndi

import java.util.{List => _, _}
import javax.transaction.TransactionManager

import org.apache.commons.pool.impl._
import org.apache.commons.dbcp._
import org.apache.commons.dbcp.managed._

import org.apache.commons.beanutils.PropertyUtils

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

  val dataSources = collection.mutable.Map[String, BasicDataSource]()
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
      val funcs = scala.collection.mutable.ArrayBuffer[CommonDataSource]()
      for((id, name, serviceName, dsClass, testQuery, xaPool, minPool, maxPool, idleTimeout, reapTimeout, isolation) <- pools.list) {
        try {
          val props = (for(p <- DataPoolProps if p.dsID is id) yield p.name ~ p.value).list
          val ds = if(xaPool) {
            val rawDS = loadPool(dsClass, props)
            val rawConnectionFactory = new DataSourceXAConnectionFactory(findTM, rawDS)
            val tmp = new GenericObjectPool()
            tmp.setMinIdle(minPool)
            tmp.setMaxActive(maxPool)
            val factory = new PoolableConnectionFactory(rawConnectionFactory, tmp, null, null, false, true)
            factory.setValidationQuery(testQuery.getOrElse(null))
            factory.setDefaultTransactionIsolation(isolation)
            tmp.setFactory(factory)
            new ManagedDataSource(tmp, rawConnectionFactory.getTransactionRegistry())
          } else {
            val tmp = new BasicDataSource()
            tmp.setDriverClassName(dsClass)
            tmp.setMinIdle(minPool)
            tmp.setMaxActive(maxPool)
            tmp.setInitialSize(minPool)
            tmp.setValidationQuery(testQuery.getOrElse(null))
            tmp.setRemoveAbandonedTimeout(idleTimeout)
            tmp.setDefaultTransactionIsolation(isolation)
            for((name, value) <- props) {
              tmp.addConnectionProperty(name, value)
              if(name.toLowerCase == "url") tmp.setUrl(value)
              else if(name.toLowerCase == "user") tmp.setUsername(value)
              else if(name.toLowerCase == "password") tmp.setPassword(value)
            }
            tmp
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
          val c: { def close(): Unit } = f match {
            case fd: DataSource => fd.getConnection
            case fx: XADataSource => fx.getXAConnection
            case _ => throw new RuntimeException("Unknown data source")
          }
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
    for((name, value) <- props) {
      PropertyUtils.setProperty(ds, name, value)
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
    val ds  = new BasicDataSource()
    ds.setUrl("jdbc:h2:" + System.getProperty("prog.home") + "/storage/users/db;AUTO_SERVER=TRUE")
    ds.setUsername("sa")
    ds.setPassword("")
    ds.setDriverClassName("org.h2.Driver")
    ds
  }

  def createDataSource(name: String, ds: CommonDataSource): CommonDataSource = {
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
