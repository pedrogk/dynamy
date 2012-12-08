package dynamy.shell.runtime.key

import org.apache.sshd.server.keyprovider.AbstractGeneratorHostKeyProvider
import java.security.KeyPair
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.InputStream
import org.bouncycastle.openssl.PEMWriter
import org.bouncycastle.openssl.PEMReader
import java.io.InputStreamReader
import org.slf4j.LoggerFactory

class KeyGenerator(path: String) extends AbstractGeneratorHostKeyProvider(path, "RSA") {

  private val logger = LoggerFactory.getLogger(classOf[KeyGenerator])

  override def doWriteKeyPair(keyPair: KeyPair, os: OutputStream): Unit = {
    val writer = new PEMWriter(new OutputStreamWriter(os))
    writer.writeObject(keyPair)
    writer.flush()
  }

  override def doReadKeyPair(is: InputStream): KeyPair = {
    val reader = new PEMReader(new InputStreamReader(is))
    reader.readObject().asInstanceOf[KeyPair]
  }

}
