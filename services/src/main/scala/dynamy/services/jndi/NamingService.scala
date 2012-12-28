package dynamy.services.jndi

import org.apache.commons.dbcp._
import org.apache.commons.dbcp.managed._

import org.osgi.framework._

import scala.collection.JavaConversions._

import org.scalaquery.session._
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.ql.extended.H2Driver.Implicit._
import org.scalaquery.ql.extended.{ExtendedTable => Table}
import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql._

import org.slf4j._

class NamingService {

  val defaultName = "org.apache.commons.dbcp:ManagedBasicDataSource=ManagedBasicDataSource"
  val dataSources = collection.mutable.Map[String, BasicDataSource]()
  val sr = collection.mutable.ArrayBuffer[ServiceRegistration[_]]()

  val logger = LoggerFactory.getLogger(classOf[NamingService])

  def init(): Unit = {
    val ds = buildLocalDs()
    createDataSource("jdbc/dynamyServices", ds)
    loadDS(ds)
}

  def loadDS(ds: javax.sql.DataSource) = {
    import org.scalaquery.session._
    val db = Database.forDataSource(ds)
    db withSession {
      val pools = for(d <- DataPool) yield d
      val funcs = scala.collection.mutable.ArrayBuffer[javax.sql.DataSource]()
      for((id, name, serviceName, dsClass, testQuery, xaPool, minPool, maxPool, idleTimeout, reapTimeout, isolation) <- pools.list) {
        try {
          val props = (for(p <- DataPoolProps if p.dsID is id) yield p.name ~ p.value).list
          val ds = if(xaPool) {
            val tmp = new BasicManagedDataSource()
            val tm = findTransactionManager
            tmp.setXADataSource(dsClass)
            tmp.setTransactionManager(tm)
            tmp
          } else {
            val tmp = new BasicDataSource()
            tmp.setDriverClassName(dsClass)
            tmp
          }
          ds.setMinIdle(minPool)
          ds.setMaxActive(maxPool)
          ds.setInitialSize(minPool)
          ds.setValidationQuery(testQuery.getOrElse(null))
          ds.setRemoveAbandonedTimeout(idleTimeout)
          ds.setDefaultTransactionIsolation(isolation)
          for((name, value) <- props) {
            ds.addConnectionProperty(name, value)
            if(name.toLowerCase == "url") ds.setUrl(value)
            else if(name.toLowerCase == "user") ds.setUsername(value)
            else if(name.toLowerCase == "password") ds.setPassword(value)
  }
  funcs += createDataSource(serviceName, ds)
logger.info("Registered datasource {}", serviceName)
                } catch {
                  case e => logger.error("Cannot create pool", e)
        }
                }
                for(f <- funcs) {
                  try {
                    val c = f.getConnection
                    c.close
                  } catch {
                    case e => logger.error("Cannot create pool", e)
                  }
                  }
                }
    }

  def shutdown(): Unit = {
    for((_, s) <- dataSources) {
      s.close
  }
  sr.foreach(_.unregister)
    }

  def findTransactionManager() = {
    import javax.transaction._
    val bc = FrameworkUtil.getBundle(getClass).getBundleContext
    val sr = bc.getServiceReference(classOf[TransactionManager])
    bc.getService(sr)
  }

  def buildLocalDs() = {
    val ds  = new ManagedBasicDataSource(defaultName + "-dataSource_dynamyServices")
    ds.setUrl("jdbc:h2:" + System.getProperty("prog.home") + "/storage/users/db;AUTO_SERVER=TRUE")
    ds.setUsername("sa")
    ds.setPassword("")
    ds.setDriverClassName("org.h2.Driver")
    ds
  }

  def createDataSource(name: String, ds: BasicDataSource) = {
    val cds: javax.sql.DataSource = ds


    val props = new java.util.Hashtable[String, Object]()

    props.put("osgi.jndi.service.name", name)

    val bc = FrameworkUtil.getBundle(getClass).getBundleContext
    sr += bc.registerService(classOf[javax.sql.DataSource], cds, props)

    cds
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
