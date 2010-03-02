package meandre.kernel

/**
 * The basic configuration container for the Meandre Infrastructure
 *
 * @author Xavier Llora
 * @date Feb 9, 2010 at 6:07:18 PM
 *
 */
class Configuration (val host:String, val port:Int, val auth:Option[(String,String)]) {

  val MEANDRE_REALM_COLLECTION = "meandre.realm"
  
  val MEANDRE_USER_COLLECTION_PREFIX = "meandre.user."

  val MEANDRE_CONTEXT_FS_COLLECTION = "meandre.context.fs"
  
  protected var MDBN = "Meandre"

  def MEANDRE_DB_NAME = MDBN
  def MEANDRE_DB_NAME_= (name:String) = MDBN=name

}

/**
 * The basic companion object for the Meandre Infrastructure
 * configuration object
 *
 * @author Xavier Llora
 * @date Feb 9, 2010 at 6:07:18 PM
 *
 */
object Configuration {

  /** The infrastructure version */
  val INFRASTRUCTURE_VERSION = "1.5.0-vcli-alpha"

  /** A default configuration */
  private val cnf = new Configuration("localhost",27017,None)

  /**Returns the default infrastructure object
   *
   * @return The Configuration object
   */
  def apply() = cnf

  /**Create a new configuration object for the given information.
   *
   * @param host The host running the MongoDB server
   * @param port The port where MongoDB server is running
   * @param auth The authorization required to access the Meandre database
   * @return A new configuration object for the given information
   */
  def apply(host:String, port:Int, auth:Option[(String,String)]) = new Configuration(host,port,auth)

}

