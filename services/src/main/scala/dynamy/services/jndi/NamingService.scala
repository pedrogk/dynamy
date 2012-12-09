package dynamy.services.jndi

import com.atomikos.jdbc._
import com.atomikos.jdbc.nonxa._

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

    val dataSources = collection.mutable.Map[String, AbstractDataSourceBean]()
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
            for((id, name, serviceName, dsClass, testQuery, minPool, maxPool, idleTimeout, reapTimeout, isolation) <- pools.list) {
                try {
					val props = (for(p <- DataPoolProps if p.dsID is id) yield p.name ~ p.value).list
					val javaProps = new java.util.Properties
					for((name, value) <- props) javaProps.put(name, value)
					val ds = new AtomikosDataSourceBean
					ds.setUniqueResourceName(name)
					ds.setXaDataSourceClassName(dsClass)
					ds.setXaProperties(javaProps)
                    createDataSource(serviceName, ds)
                    logger.info("Registered datasource {}", serviceName)
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

    def buildLocalDs() = {
        val ds  = new AtomikosNonXADataSourceBean
        ds.setUniqueResourceName("dynamy/services" + System.currentTimeMillis())
        ds.setUrl("jdbc:h2:" + System.getProperty("prog.home") + "/storage/users/db;AUTO_SERVER=TRUE")
        ds.setUser("sa")
        ds.setPassword("")
        ds.setDriverClassName("org.h2.Driver")
        ds
    }

    def createDataSource(name: String, ds: AbstractDataSourceBean) = {
        val cds: javax.sql.DataSource = ds
        val props = new java.util.Hashtable[String, Object]()

		props.put("osgi.jndi.service.name", name)

        val bc = FrameworkUtil.getBundle(getClass).getBundleContext
        sr += bc.registerService(classOf[javax.sql.DataSource], cds, props)

    }

}

object DataPool extends Table[(Int, String, String, String, Option[String], Int, Int, Int, Int, Int)]("JDBC_DS") {
    def id = column[Int]("ID", O NotNull)
    def name = column[String]("NAME")
    def serviceName = column[String]("SERVICE_NAME")
    def dsClass     = column[String]("DS_CLASS")
    def testQuery   = column[Option[String]]("TEST_QUERY")
    def minPool     = column[Int]("MIN_POOL")
    def maxPool     = column[Int]("MAX_POOL")
    def idleTimeout = column[Int]("IDLE_TIMEOUT")
    def reapTimeout = column[Int]("REAP_TIMEOUT")
    def isolation   = column[Int]("ISOLATION")
    def * = id ~ name ~ serviceName ~ dsClass ~ testQuery ~ minPool ~ maxPool ~ idleTimeout ~ reapTimeout ~ isolation
}

object DataPoolProps extends Table[(Int, Int, String, String)]("JDBC_PROPS") {
    def id    = column[Int]("ID", O NotNull)
    def dsID  = column[Int]("ID_DS", O NotNull)
    def name  = column[String]("PROP_NAME")
    def value = column[String]("PROP_VALUE")
    def * = id ~ dsID ~ name ~ value
}
