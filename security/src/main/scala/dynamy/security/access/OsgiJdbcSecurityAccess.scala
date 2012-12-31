package dynamy.security.access

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement
import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer
import org.apache.shiro.authz.permission.WildcardPermission
import org.apache.shiro.authz.Permission
import org.slf4j.LoggerFactory

import com.jolbox.bonecp._

object OsgiJdbcSecurityAccess {
  private lazy val dataSources = new ConcurrentHashMap[String, BoneCPDataSource]()

  def stop() = {
    for (value <- dataSources.values()) {
      value.close()
    }
  }

}

class OsgiJdbcSecurityAccess extends DynamySecurityAccess {

  private val logger = LoggerFactory.getLogger(classOf[OsgiJdbcSecurityAccess])

  private var jdbcUri: String = _
  private var driver: String = _
  private var username: String = _
  private var password: String = _

  private var permissionQuery: String = "SELECT DISTINCT PERMISSION FROM (SELECT PERMISSION, U.USERNAME FROM DYNAMY_ROLE_PERMISSION RP INNER JOIN DYNAMY_ROLES R ON RP.ROLE_ID = R.ID INNER JOIN DYNAMY_USER_ROLE UR ON R.ID = UR.ROLE_ID INNER JOIN DYNAMY_USERS U ON U.ID = UR.USER_ID UNION SELECT PERMISSION, U.USERNAME FROM DYNAMY_USER_PERMISSION UP INNER JOIN DYNAMY_USERS U ON UP.USER_ID = U.ID) WHERE USERNAME = ?"
  private var rolesQuery: String = "SELECT R.NAME FROM DYNAMY_ROLES R INNER JOIN DYNAMY_USER_ROLE UR ON R.ID = UR.ROLE_ID INNER JOIN DYNAMY_USERS U ON U.ID = UR.USER_ID WHERE USERNAME = ?"
  private var existsQuery: String = "SELECT COUNT(*) FROM DYNAMY_USERS WHERE USERNAME = ?"
  private var passwordQuery: String = "SELECT PASSWORD FROM DYNAMY_USERS WHERE USERNAME = ?"
  private var keysQuery: String = "SELECT KEY FROM DYNAMY_AUTHORIZED_KEYS AK INNER JOIN DYNAMY_USERS U ON AK.USER_ID = U.ID WHERE U.USERNAME = ?"

  private lazy val dataSource = buildDataSource()

  override def getPermissions(principal: String): List[Permission] = {
    var permissions = new ListBuffer[String]()
    executeQuery(permissionQuery, principal) { rs =>
      while (rs.next()) {
        permissions += rs.getString(1)
      }
    }
    for (permission <- permissions.toList) yield {
      new WildcardPermission(permission)
    }
  }

  override def getRoles(principal: String): List[String] = {
    var roles = new ListBuffer[String]()
    executeQuery(rolesQuery, principal) { rs =>
      while (rs.next()) {
        roles += rs.getString(1)
      }
    }
    roles.toList
  }

  override def userExists(principal: String): Boolean = {
    var exists = false

    executeQuery(existsQuery, principal) { rs =>
      while (rs.next()) {
        exists = rs.getInt(1) == 1
        if (rs.next()) {
          throw new RuntimeException("Got more than one result for query " + existsQuery)
        }
      }
    }

    exists
  }

  override def getPrincipalPassword(principal: String): String = {
    var password: String = null

    executeQuery(passwordQuery, principal) { rs =>
      while (rs.next()) {
        password = rs.getString(1)
        if (rs.next()) {
          throw new RuntimeException("Got more than one result for query " + existsQuery)
        }
      }
    }

    password
  }

  override def getPrincipalKeys(principal: String): List[String] = {
    var keys = ListBuffer[String]()

    executeQuery(keysQuery, principal) { rs =>
      while (rs.next()) {
        keys += rs.getString(1)
      }
    }

    keys.toList
  }

  def setJdbcUri(uri: String): Unit = {
    this.jdbcUri = uri
  }

  def setDriver(driver: String): Unit = {
    this.driver = driver;
  }

  def setUsername(username: String): Unit = {
    this.username = username
  }

  def setPassword(password: String): Unit = {
    this.password = password
  }

  def setPermissionQuery(permissionQuery: String): Unit = {
    this.permissionQuery = permissionQuery
  }

  def setRolesQuery(rolesQuery: String): Unit = {
    this.rolesQuery = rolesQuery
  }

  def setExistsQuery(existsQuery: String): Unit = {
    this.existsQuery = existsQuery
  }

  def setPasswordQuery(passwordQuery: String): Unit = {
    this.passwordQuery = passwordQuery
  }

  def setKeysQuery(keysQuery: String): Unit = {
    this.keysQuery = keysQuery
  }

  private def executeQuery(query: String, principal: String)(f: (ResultSet) => (Unit)) = {
    val conn = dataSource.getConnection()
    var stat: PreparedStatement = null
    try {
      stat = conn.prepareStatement(query)
      stat.setString(1, principal)
      val rs = stat.executeQuery()

      f(rs)

    } finally {
      if (stat != null) {
        stat.close()
      }
      if (conn != null) {
        conn.close()
      }
    }
  }

  private def buildDataSource() = {
    val ds = new BoneCPDataSource
    
    ds.setJdbcUrl(jdbcUri)
    ds.setUsername(username)
    ds.setPassword(password)
    ds.setDriverClass(driver)
    ds.setClassLoader(getClass.getClassLoader)

    OsgiJdbcSecurityAccess.dataSources.put("dynamy/security", ds)

    ds
  }

}
