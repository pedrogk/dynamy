package dynamy.security.info
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.subject.PrincipalCollection
import org.apache.shiro.subject.SimplePrincipalCollection

class DynamyAuthenticationInfo(principal: Object, credentials: Object, realmName: String) extends AuthenticationInfo {

  val principals = new SimplePrincipalCollection(principal, realmName)
  
  override def getPrincipals(): PrincipalCollection = principals
  
  override def getCredentials(): Object = credentials
  
}
