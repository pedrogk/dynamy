package dynamy.security.matcher

import org.apache.shiro.authc.credential.CredentialsMatcher
import org.apache.shiro.authc.credential.DefaultPasswordService
import org.apache.shiro.authc.credential.PasswordService
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.authc.SimpleAuthenticationInfo
import org.slf4j.LoggerFactory
import dynamy.security.info.DynamyAuthenticationInfo
import java.security.PublicKey
import dynamy.security.util.PublicKeyStringDecoder
import org.apache.shiro.authc.UsernamePasswordToken

class DynamyCredentialsMatcher extends CredentialsMatcher {

  private val logger = LoggerFactory.getLogger(classOf[DynamyCredentialsMatcher])
  
  var passwordService: PasswordService = new DefaultPasswordService()
  
  override def doCredentialsMatch(token: AuthenticationToken, info: AuthenticationInfo): Boolean = {
    info match {
      case s: DynamyAuthenticationInfo => {
        matchSimpleAuthenticationInfo(token, s)
      }
      case _ => false
    }
  }
  
  private def matchSimpleAuthenticationInfo(token: AuthenticationToken, info: DynamyAuthenticationInfo): Boolean = {
        
    val (tokenPassword, tokenKey) = token.getCredentials() match {
      case (null, key: PublicKey) => (null, key)
      case (password: String, null) => (password, null)
      case password: Array[Char] => (String.valueOf(password), null)
      case _ => {
        (null, null)
      }
    }
    val (infoPassword, infoTrusted: List[String]) = info.getCredentials() match {
      case (password: String, trusted: List[String]) => (password, trusted)
      case _ => (null, List[String]())
    }
    keysMatch(tokenKey, infoTrusted) || passwordsMatch(tokenPassword, infoPassword)
  }
  
  private def passwordsMatch(sentPassword: String, expectedPassword: String) = {
    passwordService.passwordsMatch(sentPassword, expectedPassword)
  }
  
  private def keysMatch(key: PublicKey, trustedList: List[String]): Boolean = {
    val decoder = new PublicKeyStringDecoder()
    if(trustedList == null || key == null) return false
    for(trusted <- trustedList) {
      val trustedKey = decoder.decodePublicKey(trusted)
      logger.debug("Found trusted key {}", trustedKey)
      if(key equals trustedKey) return true
    }
    false
  }
  
  def setPasswordService(passwordService: PasswordService): Unit = {
    this.passwordService = passwordService
  }
  
}
