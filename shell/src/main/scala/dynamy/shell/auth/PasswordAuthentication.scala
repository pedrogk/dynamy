package dynamy.shell.runtime.auth
import org.apache.sshd.server.PasswordAuthenticator
import org.apache.sshd.server.session.ServerSession
import org.apache.shiro.SecurityUtils
import dynamy.security.tokens.DynamyAuthenticationToken
import org.slf4j.LoggerFactory
import org.apache.shiro.subject.Subject

class PasswordAuthentication(f: (Subject) => Boolean) extends PasswordAuthenticator {

  private val logger = LoggerFactory.getLogger(classOf[PasswordAuthenticator])
  
  override def authenticate(username: String, password: String, session: ServerSession): Boolean = {
    val subject = SecurityUtils.getSubject()
    try {
      subject.login(new DynamyAuthenticationToken(username, password))
      subject.isAuthenticated() && f(subject)
    } catch {
      case e =>
        logger.debug("Exception occured, cannot authenticate", e)
        false
    }
  }
  
}
