package meandre.webservices.realm

import org.mortbay.jetty.{Response, Request}
import org.mortbay.jetty.security.{Credential, SSORealm, UserRealm}
import meandre.kernel.Configuration
import meandre.kernel.Implicits._
import java.security.{MessageDigest, Principal}
import com.mongodb.util.JSON
import com.mongodb.{DBObject, BasicDBList, BasicDBObject, Mongo}

/**
 * A customized realm that is backended on a MongoDB store. The
 * code from this realm has been based on the basic functionality
 * implemented on the HashUserRealm
 *
 * @author Xavier Llora
 * @date Mar 1, 2010 at 10:51:15 PM
 * 
 */

class MongoDBRealm(cnf:Configuration, var realmName:String) extends UserRealm with SSORealm {

  val __SSO = "org.mortbay.http.SSO"
  
  private var _ssoRealm:SSORealm = null

  //--------------------------------------------------------------------------

  def computeHash ( s:String ) = {
    val sb = new StringBuffer
    val md = MessageDigest.getInstance("SHA")
    md.reset
    md update s.getBytes
    for ( b <- md.digest ) sb.append(Integer.toHexString(0xFF&b))
    sb.toString
  }

  def hashCredentials ( o:Object ) = computeHash(o.toString)
 
  implicit def basicMongoDBObject2Principal (bdbo:BasicDBObject ) : Principal = {
    val principal = new MongoDBPrincipal(bdbo.getString("_id"))
    principal.putAll(bdbo.asInstanceOf[DBObject])
    principal
  }
  //--------------------------------------------------------------------------

  class MongoDBPrincipal(val principalName:String) extends BasicDBObject with Principal {

    // Setup the principal name
    put("_id",principalName)

    /**
     * Compares this principal to the specified object.  Returns true
     * if the object passed in matches the principal represented by
     * the implementation of this interface.
     *
     * @param another principal to compare with.
     *
     * @return true if the principal passed in is the same as that
     * encapsulated by this principal, and false otherwise.

     */
    override def equals(another:Any):Boolean = {
      try {
        getString("_id")==((another.asInstanceOf[BasicDBObject]).get("_id"))
      }
      catch {
        case _ => false
      }
    }

    /**
     * Returns a string representation of this principal.
     *
     * @return a string representation of this principal.
     */
    override def toString = getString("_id")

    /**
     * Returns a hashcode for this principal.
     *
     * @return a hashcode for this principal.
     */
    override def hashCode = this.hashCode

    /**
     * Returns the name of this principal.
     *
     * @return the name of this principal.
     */
    def getName = principalName

  }


  //--------------------------------------------------------------------------

  /** The mongo db connection object */
  private val mongo = new Mongo(cnf.host,cnf.port)

  /** The Meandre database */
  private val db = mongo getDB cnf.MEANDRE_DB_NAME
  cnf.auth match {
    case Some((user,password)) => db.authenticate(user,password.toCharArray)
    case None =>
  }

  /** The user collection */
  protected val collection = db getCollection cnf.MEANDRE_REALM_COLLECTION

  /** Test connectivity and existence of admin. Will thrown an exception if
   * cannot connect */
  val adminCnd:BasicDBObject = """{"_id":"admin"}"""
  if (collection.find(adminCnd).count==0) {
    val bdbo:BasicDBObject = """{ "_id":"admin", "roles":["""+MongoDBRealm.AVAILABLE_ROLES.reduceLeft((a,b)=>'"'+a+"\",\""+b+'"')+"""] }"""
    bdbo.put("password",computeHash("admin"))
    collection.insert(bdbo)
  }

  //--------------------------------------------------------------------------

  /**Returns the real name
   *
   * @return The realm name
   */
  def getName:String = realmName

  /**Check it the principal exists
   *
   * @param The user to fetch the principal for
   * @return The principal
   *
   */
  def getPrincipal(user:String):Principal = collection.findOne("{_id:\""+user+"\"}") match {
    case null => null
    case u:BasicDBObject => u
  }

  /**Tries to authenticate a user.
   *
   * @param user The user to authenticate
   * @param credentials The credentials provided for the user
   * @param request The request object
   */
  def authenticate(user:String, credentials:Object, request:Request ) : Principal = {
    val q:BasicDBObject = "{\"_id\":\""+user+"\"}"
    val userInfo = collection.findOne(q)
    userInfo match {
      case null => null
      case u:BasicDBObject => val cred = Credential.getCredential(u.getString("password"))
                              cred.check(hashCredentials(credentials)) match {
                                case true  => u.put("authenticated","yes") ; u
                                case false => null
                              }
    }
  }

  /**Check if the principal has been authenticated before.
   *
   * @param principal The principal to check
   * @return True if authenticated before, false otherwise
   */
  def reauthenticate(principal:Principal):Boolean = principal match {
    case u:BasicDBObject => u.containsField("authenticated")
    case _ => false
  }

  /**Check if the user is in the role.
   *
   * @param principal The principal to check
   * @param role The role to check
   * @return True if the use is in the role
   */
  def isUserInRole(principal:Principal, role:String):Boolean = principal match {
    case u:BasicDBObject => val roles = u.get("roles").asInstanceOf[BasicDBList]
                            roles.contains(role)
    case _ => false
  }

  /**Disassociate the context for the principal. For this case *does nothing*.
   *
   * @param principal The principal to disassociate
   */
  def disassociate(principal:Principal):Unit = 1

  /**Logout the given principal. For this case *does nothing*.
   *
   * @param principal The principal to disassociate
   */
  def logout(principal:Principal):Unit = 1


  /** Pushes a role into a principal
   *
   * @param principal The principal to wrap
   * @param role The role to add
   * @return The wrapped principal
   */
  def pushRole(principal:Principal, role:String):Principal = {
    val newPrincipal = new MongoDBPrincipal(principal.getName)
    newPrincipal.put("principal",principal)
    newPrincipal.put("pushed_role",role)
    newPrincipal.putAll(principal.asInstanceOf[DBObject])
    newPrincipal
  }

  /**Unwraps the principal from the role.
   *
   * @param principal The principal to unwrap from the role
   */
  def popRole(principal:Principal):Principal =
    principal.asInstanceOf[MongoDBPrincipal].get("principal").asInstanceOf[Principal]


  //--------------------------------------------------------------------------

  /**
   * @return The SSORealm to delegate single sign on requests to.
   */
  def getSSORealm:SSORealm = _ssoRealm


  /** Set the SSORealm.
   * A SSORealm implementation may be set to enable support for SSO.
   * @param ssoRealm The SSORealm to delegate single sign on requests to.
   */
  def setSSORealm(ssoRealm:SSORealm) = _ssoRealm = ssoRealm;


  /* ------------------------------------------------------------ */

  def getSingleSignOn( request:Request , response:Response ):Credential = _ssoRealm match {
      case null => null
      case  _   => _ssoRealm.getSingleSignOn(request, response)
  }

  /* ------------------------------------------------------------ */

  def setSingleSignOn(request:Request, response:Response, principal:Principal , credential:Credential ) = _ssoRealm match {
    case null =>
    case _ => _ssoRealm.setSingleSignOn(request, response, principal, credential)
  }

  /* ------------------------------------------------------------ */
  def clearSingleSignOn(userName:String) = _ssoRealm match {
    case null =>
    case _ => _ssoRealm.clearSingleSignOn(userName)
  }

}

/**
 * The companion object for the customized realm that is backended on a MongoDB store
 *
 * @author Xavier Llora
 * @date Mar 1, 2010 at 10:51:15 PM
 *
 */
object MongoDBRealm {

  val AVAILABLE_ROLES = Array("admin","user")

  def apply (cnf:Configuration,realmName:String) = new MongoDBRealm(cnf,realmName)
}