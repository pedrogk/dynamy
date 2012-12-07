package dynamy.security.tokens

import java.security.PublicKey
import org.apache.shiro.authc.AuthenticationToken

case class DynamyAuthenticationToken(username: String, password: String, key: PublicKey) extends AuthenticationToken {

  def this(username: String, key: PublicKey) = this(username, null, key)
  
  def this(username: String, password: String) = this(username, password, null)
  
  
  override def getPrincipal(): Object = username
  override def getCredentials(): Object = (password, key)
  
  
}
