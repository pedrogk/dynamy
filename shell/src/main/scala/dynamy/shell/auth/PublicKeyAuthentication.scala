package dynamy.shell.runtime.auth

import org.apache.sshd.server.PublickeyAuthenticator
import java.security.PublicKey
import org.apache.sshd.server.session.ServerSession
import org.slf4j.LoggerFactory
import org.apache.shiro.SecurityUtils
import dynamy.security.tokens.DynamyAuthenticationToken
import org.apache.shiro.subject.Subject

class PublicKeyAuthentication(f: (Subject) => Boolean) extends PublickeyAuthenticator {

  private val logger = LoggerFactory.getLogger(classOf[PublicKeyAuthentication])
  
  override def authenticate(username: String, key: PublicKey, session: ServerSession): Boolean = {
    val subject = SecurityUtils.getSubject()
    try {
      subject.login(new DynamyAuthenticationToken(username, key))
      subject.isAuthenticated() && f(subject)
    } catch {
      case e =>
        logger.debug("Exception occured, cannot authenticate", e)
        false
    }
  }
  
  
  
}
