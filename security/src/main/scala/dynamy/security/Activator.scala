package dynamy.security

import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext
import org.apache.shiro.config.IniSecurityManagerFactory
import org.apache.shiro.SecurityUtils
import org.slf4j.LoggerFactory
import org.apache.shiro.subject.Subject
import dynamy.security.tokens.DynamyAuthenticationToken
import tokens.DynamyAuthenticationToken
import dynamy.security.access.OsgiJdbcSecurityAccess
import java.security.Security
import org.bouncycastle.jce.provider.BouncyCastleProvider

class Activator extends BundleActivator {

  private val logger = LoggerFactory.getLogger(classOf[Activator])
  
  override def start(context: BundleContext) = {
    Security.addProvider(new BouncyCastleProvider())
    val currentLoader = Thread.currentThread().getContextClassLoader()
    try {
    	Thread.currentThread().setContextClassLoader(classOf[Activator].getClassLoader())
	    val factory = new IniSecurityManagerFactory("conf/shiro.ini")
	    logger.info("Creating factory {}", factory)
	    val securityManager = factory.getInstance()
	    logger.info("Creating security manager {}", securityManager)
	    SecurityUtils.setSecurityManager(securityManager)
    } finally {
      Thread.currentThread().setContextClassLoader(currentLoader)
    }
    
  }

  override def stop(context: BundleContext) = {
    OsgiJdbcSecurityAccess.stop()
  }
  
}
