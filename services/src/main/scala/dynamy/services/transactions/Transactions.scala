package dynamy.services.transactions

import dynamy.services.pool._

import java.sql.{Array => SQLArray, _}
import javax.transaction._

import scala.collection.JavaConversions._

import java.util.concurrent.locks._
import java.util.concurrent.atomic._
import java.util.concurrent._
import java.util.{Properties, Map}

import org.osgi.framework._

import com.jolbox.bonecp._

case class TransactionContext(currentConnection: Connection, transaction: Transaction)

class TransactionAwareDS extends BoneCPDataSource {

  private var transactionManager: TransactionManager = _
  private val transactionManagerLock = new ReentrantLock
  private var transactionManagerRef: ServiceReference[TransactionManager] = _
  private val transactionMemory = new ConcurrentHashMap[Transaction, TransactionContext]()
  private val connectionMemory = new ConcurrentHashMap[Connection, TransactionContext]()

  private def getTransactionManager() = {
    if(transactionManager == null) {
      transactionManagerLock.lock()
      try {
        val bc = FrameworkUtil.getBundle(getClass).getBundleContext
        transactionManagerRef = bc.getServiceReference(classOf[TransactionManager])
        transactionManager = bc.getService(transactionManagerRef)
      } finally {
        transactionManagerLock.unlock()
      }
    }
    transactionManager
  }

  override def close() = {
    if(transactionManagerRef != null) {
      transactionManagerLock.lock()
      try {
        val bc = FrameworkUtil.getBundle(getClass).getBundleContext
        bc.ungetService(transactionManagerRef) 
      } finally {
        transactionManagerLock.unlock()
      }
    }
    super.close()
  }

  def currentTransaction = {
    var transaction = getTransactionManager.getTransaction
    if(!isTransactionActive(transaction)) {
      transaction = null
    }
    transaction
  }
  
  private def isTransactionActive(t: Transaction) =
    t != null && (t.getStatus == Status.STATUS_ACTIVE || t.getStatus == Status.STATUS_MARKED_ROLLBACK)

  private def terminateTransaction(c: Connection, t: Transaction) = {
    transactionMemory.remove(t)
    connectionMemory.remove(c)
  }

  private def registerConnection(c: Connection, t: Transaction) = {
    val tc = TransactionContext(c, t)
    c match {
      case bc: ConnectionHandle => {
        bc.getInternalConnection match {
          case xa @ XAConnectionWrapper(conn) => {
            //enlist resource
            t.enlistResource(conn.getXAResource)
            t.registerSynchronization(new Synchronization {
              override def beforeCompletion() = {
              }
              override def afterCompletion(status: Int) = {
                //handle someway
                terminateTransaction(c, t)
              }
            })
          }
        }
      }
    }
    transactionMemory.put(t, tc)
    connectionMemory.put(c, tc)
  }
  
  private def isFreeConnection(c: Connection) = {
    if(connectionMemory.containsKey(c)) {
      val transaction = connectionMemory.get(c).transaction
      if(!isTransactionActive(transaction)) {
        terminateTransaction(c, transaction)
        true
      } else false
    } else true
  }

  private def findFreeConnection(username: String, password: String): Connection = {
    var conn = super.getConnection(username, password)
    while(!isFreeConnection(conn)) {
      //Release the last connection
      conn.close()
      //Get a new connection
      conn = super.getConnection(username, password)
    }
    conn
  }

  private def findFreeConnection(): Connection = {
    var conn = super.getConnection()
    while(!isFreeConnection(conn)) {
      //Release the last connection
      conn.close()
      //Get a new connection
      conn = super.getConnection()
    }
    conn
  }

  override def getConnection() = {
    val transaction = currentTransaction
    if(transaction == null) //There's no transaction just return any connection
      findFreeConnection()
    else {
      val currentContext = transactionMemory.get(transaction)
      //There's no transaction context for now
      if(currentContext == null) {
        transaction.synchronized {
          val conn = findFreeConnection()
          registerConnection(conn, transaction)
          new TransactionConnectionWrapper(conn, connectionMemory)
        }
      } else {
        //Return the same connection the transaction is using
        val TransactionContext(connection, _) = currentContext
        connection
      }
    }
  }

  override def getConnection(username: String, password: String) = {
    val transaction = currentTransaction
    if(transaction == null) //There's no transaction just return any connection
      findFreeConnection(username, password)
    else {
      val currentContext = transactionMemory.get(transaction)
      //There's no transaction context for now
      if(currentContext == null) {
        transaction.synchronized {
          val conn = findFreeConnection(username, password)
          registerConnection(conn, transaction)
          new TransactionConnectionWrapper(conn, connectionMemory)
        }
      } else {
        //Return the same connection the transaction is using
        val TransactionContext(connection, _) = currentContext
        connection
      }
    }
  }


}

class TransactionConnectionWrapper(plainConn: Connection, connectionMemory: ConcurrentHashMap[Connection, TransactionContext])
  extends Connection {
  
  private val closed = new AtomicBoolean(false)

  private def isTransactionActive(t: Transaction) =
    t != null && (t.getStatus == Status.STATUS_ACTIVE || t.getStatus == Status.STATUS_MARKED_ROLLBACK)

  def isInTransaction = {
    val tc = connectionMemory(plainConn)
    if(tc != null) {
      val TransactionContext(connection, transaction) = tc
      isTransactionActive(transaction)
    } else {
      false
    }
  }

  override def abort(executor: Executor) =
    plainConn.abort(executor)

  override def clearWarnings() = 
    plainConn.clearWarnings()

  override def close() = 
    if(isInTransaction) {
      try {
        val TransactionContext(_, transaction) = connectionMemory.get(plainConn)
        if(closed.compareAndSet(false, true)) {
          transaction.registerSynchronization(new Synchronization {
            override def beforeCompletion() = {
            }
            override def afterCompletion(status: Int) {
              plainConn.close()
            }
          })
        }
      } catch {
        case c: NullPointerException =>
          plainConn.close()
      }
    } else {
      plainConn.close()
    }

  override def commit() = 
    if(isInTransaction) throw new SQLException("Cannot commit while in transaction")
    else plainConn.commit()

  override def createArrayOf(typeName: String, elements: Array[Object]) = 
    plainConn.createArrayOf(typeName, elements)

  override def createBlob() =
    plainConn.createBlob()

  override def createClob() = 
    plainConn.createClob()

  override def createNClob() =
    plainConn.createNClob()

  override def createSQLXML() =
    plainConn.createSQLXML()

  override def createStatement() = 
    plainConn.createStatement()

  override def createStatement(resultSetType: Int, resultSetConcurrency: Int) =
    plainConn.createStatement(resultSetType, resultSetConcurrency)

  override def createStatement(resultSetType: Int, resultSetConcurrency: Int, resultSetHoldability: Int) =
    plainConn.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability)

  override def createStruct(typeName: String, attributes: Array[Object]) =
    plainConn.createStruct(typeName, attributes)

  override def getAutoCommit() =
    plainConn.getAutoCommit()

  override def getCatalog() =
    plainConn.getCatalog()

  override def getClientInfo() = 
    plainConn.getClientInfo()

  override def getClientInfo(property: String) = 
    plainConn.getClientInfo(property)

  override def getHoldability() =
    plainConn.getHoldability()

  override def getMetaData() = 
    plainConn.getMetaData()

  override def getTransactionIsolation() =
    plainConn.getTransactionIsolation()

  override def getTypeMap() =
    plainConn.getTypeMap()

  override def getWarnings() =
    plainConn.getWarnings()

  override def isClosed() =
    plainConn.isClosed()

  override def isReadOnly() =
    plainConn.isReadOnly()
  
  override def isValid(timeout: Int) =
    plainConn.isValid(timeout)
  
  override def nativeSQL(sql: String) =
    plainConn.nativeSQL(sql)
  
  override def prepareCall(sql: String) =
    plainConn.prepareCall(sql)

  override def prepareCall(sql: String, resultSetType: Int, resultSetConcurrency: Int) =
    plainConn.prepareCall(sql, resultSetType, resultSetConcurrency)
  
  override def prepareCall(sql: String, resultSetType: Int, resultSetConcurrency: Int, resultSetHoldability: Int) =
    plainConn.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability)

  override def prepareStatement(sql: String) =
    plainConn.prepareStatement(sql)
  
  override def prepareStatement(sql: String, autoGeneratedKeys: Int) =
    plainConn.prepareStatement(sql, autoGeneratedKeys)
  
  override def prepareStatement(sql: String, columnIndexes: Array[Int]) =
    plainConn.prepareStatement(sql, columnIndexes)

  override def prepareStatement(sql: String, resultSetType: Int, resultSetConcurrency: Int) =
    plainConn.prepareStatement(sql, resultSetType, resultSetConcurrency)
  
  override def prepareStatement(sql: String, resultSetType: Int, resultSetConcurrency: Int, resultSetHoldability: Int) =
    plainConn.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability: Int)

  override def prepareStatement(sql: String, columnNames: Array[String]) =
    plainConn.prepareStatement(sql, columnNames)
  
  override def releaseSavepoint(savepoint: Savepoint) =
    plainConn.releaseSavepoint(savepoint)

  override def rollback() =
    if(isInTransaction) throw new SQLException("Cannot rollback while in transaction")
    else plainConn.rollback()
  
  override def rollback(savepoint: Savepoint) =
    plainConn.rollback(savepoint)
  
  override def setAutoCommit(autoCommit: Boolean) =
    if(isInTransaction) throw new SQLException("Cannot set to autocommit while in transaction")
    else plainConn.setAutoCommit(autoCommit)
  
  override def setCatalog(catalog: String) =
    plainConn.setCatalog(catalog)
  
  override def setClientInfo(properties: Properties) =
    plainConn.setClientInfo(properties)
  
  override def setClientInfo(name: String, value: String) =
    plainConn.setClientInfo(name,value)
  
  override def setHoldability(holdability: Int) =
    plainConn.setHoldability(holdability)
  
  override def setReadOnly(readOnly: Boolean) =
    if(isInTransaction) throw new SQLException("Cannot set to redonly while in transaction")
    else plainConn.setReadOnly(readOnly)
  
  override def setSavepoint() =
    plainConn.setSavepoint()
  
  override def setSavepoint(name: String) =
    plainConn.setSavepoint(name)
  
  override def setTransactionIsolation(level: Int) =
    plainConn.setTransactionIsolation(level)
  
  override def setTypeMap(map: Map[String, Class[_]]) =
    plainConn.setTypeMap(map)

  override def isWrapperFor(iface: Class[_]) = 
    plainConn.isWrapperFor(iface)

  override def unwrap[T](iface: Class[T]): T = null.asInstanceOf[T]

  override def setNetworkTimeout(executor: Executor, milliseconds: Int) =
    plainConn.setNetworkTimeout(executor, milliseconds)

  override def getNetworkTimeout() =
    plainConn.getNetworkTimeout()

  override def getSchema() =
    plainConn.getSchema()

  override def setSchema(schema: String) =
    plainConn.setSchema(schema)
}

