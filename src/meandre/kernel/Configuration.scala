package meandre.kernel

/**
 * The basic configuration container for the Meandre Infrastructure
 *
 * @author Xavier Llora
 * @date Feb 9, 2010 at 6:07:18 PM
 *
 */
class Configuration (val protocol:String, val server:String, val serverPort:Int, val host:String, val port:Int, val auth:Option[(String,String)]) {

  val MEANDRE_REALM_COLLECTION = "meandre.realm"
  
  val MEANDRE_USER_COLLECTION_PREFIX = "meandre.user."

  val MEANDRE_PUBLIC_COLLECTION = "meandre.public"

  val MEANDRE_GLOBAL_LOGGER = "meandre.log"

  val MEANDRE_JOBS = "meandre.jobs"

  val MEANDRE_JOBS_QUEUE = MEANDRE_JOBS+".queue"

  val MEANDRE_CONTEXT_FS_COLLECTION = "meandre.context.fs"

  val MEANDRE_QUEUE_ATTEMPTS = 5

  var EXECUTION_SCALA = "scala"

  var EXECUTION_SCRIPT = "execution.scala"

  var EXECUTION_CLASSPATH = "."
  
  protected var MDBN = "Meandre"

  def MEANDRE_DB_NAME = MDBN
  def MEANDRE_DB_NAME_= (name:String) = MDBN=name

  protected var MLL = 2

  def MEANDRE_LOG_LEVEL = MLL
  def MEANDRE_LOG_LEVEL_= (level:Int) = MLL = if ( level<0 ) 0 else if ( level>5 ) 5 else level

  val MEANDRE_CLUSTER_POOL = "MeandreInf"

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
  val INFRASTRUCTURE_VERSION = "2.0.0-vcli-alpha"

  /**Returns the default infrastructure object
   *
   * @return The Configuration object
   */
  def apply() = new Configuration("http","localhost",1714,"localhost",27017,None)

  /**Create a new configuration object for the given information.
   *
   * @param server The server name
   * @param host The host running the MongoDB server
   * @param port The port where MongoDB server is running
   * @param auth The authorization required to access the Meandre database
   * @return A new configuration object for the given information
   */
  def apply(protocol:String,server:String,serverPort:Int,host:String, port:Int, auth:Option[(String,String)]) = new Configuration(protocol,server,serverPort,host,port,auth)

}

