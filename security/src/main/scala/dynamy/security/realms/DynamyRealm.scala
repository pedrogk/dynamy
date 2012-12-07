package dynamy.security.realms

import scala.collection.JavaConversions._
import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.subject.PrincipalCollection
import org.apache.shiro.authz.AuthorizationInfo
import org.apache.shiro.authc.AuthenticationToken
import dynamy.security.tokens.DynamyAuthenticationToken
import dynamy.security.tokens.DynamyAuthenticationToken
import org.apache.shiro.authc.HostAuthenticationToken
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authz.SimpleAuthorizationInfo
import scala.collection.JavaConversions
import org.apache.shiro.authc.SimpleAuthenticationInfo
import dynamy.security.access.DynamySecurityAccess
import dynamy.security.matcher.DynamyCredentialsMatcher
import dynamy.security.info.DynamyAuthenticationInfo
import org.apache.shiro.authc.UsernamePasswordToken
import org.slf4j.LoggerFactory
import java.util.HashSet

class DynamyRealm extends AuthorizingRealm {	

  private var securityAccess: DynamySecurityAccess = _
  private val logger = LoggerFactory.getLogger(classOf[DynamyRealm])
    
  override def doGetAuthorizationInfo(principals: PrincipalCollection): AuthorizationInfo = {
    val permissionCollections = for(principal <- principals.asList().toSeq;
        stringPrincipal = principal.asInstanceOf[String]) yield {
      securityAccess.getPermissions(stringPrincipal)
    }
    val rolesCollections = for(principal <- principals.asList().toSeq;
        stringPrincipal = principal.asInstanceOf[String]) yield {
      securityAccess.getRoles(stringPrincipal)
    }
    
    val permissions = permissionCollections.toList.flatten
    val roles       = rolesCollections.toList.flatten
    val authorization = new SimpleAuthorizationInfo(new HashSet(JavaConversions.asJavaSet(roles.toSet)))
    
    authorization.setObjectPermissions(new HashSet(setAsJavaSet(permissions.toSet)))
    
    authorization
  }
  
  override def doGetAuthenticationInfo(token: AuthenticationToken): AuthenticationInfo = {
    val principal = token.getPrincipal().asInstanceOf[String]
    
    if(securityAccess.userExists(principal)) {
      val password = securityAccess.getPrincipalPassword(principal)
      val keys     = securityAccess.getPrincipalKeys(principal)
      new DynamyAuthenticationInfo(token.getPrincipal(), (password, keys), getName())
  	} else {
      null
  	}
  }
  
  override def supports(token: AuthenticationToken) = {
    token match {
      case _: DynamyAuthenticationToken => true
      case _: UsernamePasswordToken => true
      case _ => false
    }
  }
  
  def getSecurityAccess() = securityAccess
  
  def setSecurityAccess(securityAccess: DynamySecurityAccess): Unit = {
    this.securityAccess = securityAccess
  }
    
}
