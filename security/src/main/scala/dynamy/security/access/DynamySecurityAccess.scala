package dynamy.security.access

import org.apache.shiro.authz.Permission

trait DynamySecurityAccess {

  def getPermissions(principal: String): List[Permission]
  
  def getRoles(principal: String):  List[String]
  
  def userExists(principal: String): Boolean
  
  def getPrincipalPassword(principal: String): String
  
  def getPrincipalKeys(principal: String): List[String]
  
}
