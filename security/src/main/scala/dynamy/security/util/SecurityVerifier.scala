package dynamy.security

import javax.crypto._
import javax.crypto.spec._

import java.util.UUID

import org.apache.shiro.codec._

object SecurityVerifier {
  def sign(s: String, secret: String, algo: String = "HMac-SHA1"): String = {
    val message = s.getBytes
    val key = new SecretKeySpec(s.getBytes, algo)
    val mac = Mac.getInstance(algo, "BC")
    mac.init(key)
    mac.reset()
    mac.update(message, 0, message.length)

    val bytesoutput = mac.doFinal
    Hex.encodeToString(bytesoutput)
  }
  def generateSessionId(secret: String, algo: String="HMac-SHA1"): String = {
    val u = UUID.randomUUID().toString
    val s = sign(u, secret, algo)
    u + s
  }
  def verify(s: String, secret: String, algo: String="HMac-SHA1", size: Int=36): Boolean = {
    val u   = s.substring(0, size)
    val ss  = s.substring(size)
    val enc = sign(u, secret, algo)
    ss == enc
  }
}
