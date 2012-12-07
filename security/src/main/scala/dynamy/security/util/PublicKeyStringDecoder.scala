package dynamy.security.util
import java.math.BigInteger
import java.security.spec.DSAPublicKeySpec
import java.security.spec.RSAPublicKeySpec
import java.security.KeyFactory
import java.security.PublicKey

import org.apache.shiro.codec.Base64

class PublicKeyStringDecoder {
  var bytes: Array[Byte] = _
  var pos: Int = _

  def decodePublicKey(keyLine: String): PublicKey = {
    bytes = null
    pos = 0
    bytes =
      Base64.decode(
        keyLine
          .split(" ")
          .filter(_.startsWith("AAAA"))
          .head)
    if (bytes == null) {
      throw new IllegalArgumentException("no Base64 part to decode")
    }
    val typeName = decodeType()
    typeName match {
      case "ssh-rsa" => {
        val e = decodeBigInt()
        val m = decodeBigInt()
        val spec = new RSAPublicKeySpec(m.bigInteger, e.bigInteger)
        KeyFactory.getInstance("RSA", "BC").generatePublic(spec)
      }
      case "ssh-dss" => {
        val p = decodeBigInt()
        val q = decodeBigInt()
        val g = decodeBigInt()
        val y = decodeBigInt()
        val spec = new DSAPublicKeySpec(y.bigInteger, p.bigInteger, q.bigInteger, g.bigInteger)
        KeyFactory.getInstance("DSA", "BC").generatePublic(spec)
      }
      case _ => {
        throw new IllegalArgumentException("Unknown type " + typeName)
      }
    }

  }

  private def getAndIncPos(): Int = {
    val currPos = pos
    pos += 1
    currPos
  }

  private def decodeType(): String = {
    val len = decodeInt()
    val typeName: String = new String(bytes, pos, len)
    pos += len
    typeName
  }

  private def decodeInt(): Int = {
    ((bytes(getAndIncPos()) & 0xFF) << 24) |
      ((bytes(getAndIncPos()) & 0xFF) << 16) |
      ((bytes(getAndIncPos()) & 0xFF) << 8) |
      ((bytes(getAndIncPos()) & 0xFF))
  }

  def decodeBigInt(): BigInt = {
    val len = decodeInt()
    val bigIntBytes = new Array[Byte](len)
    Array.copy(bytes, pos, bigIntBytes, 0, len)
    pos += len
    new BigInt(new BigInteger(bigIntBytes))
  }

}
