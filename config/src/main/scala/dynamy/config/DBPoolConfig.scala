package dynamy.config

import scala.reflect.BeanProperty

class DBPoolConfig {
  @BeanProperty 
  var xaPool = false
  @BeanProperty 
  var minConnectionsPerPartition = 1
  @BeanProperty 
  var maxConnectionsPerPartition = 2
  @BeanProperty 
  var acquireIncrement = 2
  @BeanProperty 
  var partitionCount = 1
  @BeanProperty 
  var jdbcUrl: String = _
  @BeanProperty 
  var driverClass: String = _
  @BeanProperty 
  var username: String = _
  @BeanProperty 
  var password: String = _
  @BeanProperty 
  var idleConnectionTestPeriodInSeconds: Long = 240 * 60
  @BeanProperty 
  var idleMaxAgeInSeconds: Long = 60 * 60
  @BeanProperty 
  var connectionTestStatement: String = _
  @BeanProperty 
  var statementsCacheSize = 0
  @BeanProperty 
  var statementsCachedPerConnection = 0
  @BeanProperty 
  var releaseHelperThreads = 0
  @BeanProperty 
  var statementReleaseHelperThreads = 0
  @BeanProperty 
  var initSql: String = _
  @BeanProperty 
  var closeConnectionWatch = false
  @BeanProperty 
  var logStatementsEnabled = false
  @BeanProperty 
  var acquireRetryDelayInMs: Long = 7000
  @BeanProperty 
  var acquireRetryAttempts = 5
  @BeanProperty 
  var lazyInit = false
  @BeanProperty 
  var transactionRecoveryEnabled = false
  @BeanProperty 
  var queryExecuteTimeLimitInMs: Long = 0
  @BeanProperty 
  var poolAvailabilityThreshold = 0
  @BeanProperty 
  var disableConnectionTracking = false
  @BeanProperty 
  var connectionTimeoutInMs: Long = 0
  @BeanProperty 
  var closeConnectionWatchTimeoutInMs: Long = 0
  @BeanProperty 
  var maxConnectionAgeInSeconds: Long = 0
  @BeanProperty 
  var statisticsEnabled: Boolean = false
  @BeanProperty 
  var defaultTransactionIsolation: String = _
  @BeanProperty 
  var defautCatalog: String = _
  @BeanProperty 
  var closeOpenStatements: Boolean = false
  @BeanProperty 
  var detectUnclosedStatements: Boolean = false
  @BeanProperty 
  var properties: java.util.Map[String, String] = _
}

