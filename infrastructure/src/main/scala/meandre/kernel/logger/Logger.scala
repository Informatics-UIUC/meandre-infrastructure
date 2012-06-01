package meandre.webservices.logger

import meandre.kernel.Configuration
import scala.actors.Actor
import scala.actors.Actor._
import meandre.kernel.Implicits._
import java.util.{Date, UUID}
import com.mongodb.{BasicDBList, DBObject, BasicDBObject, Mongo}

/**
 * A basic logger persisted on the the MongoDB store
 *
 * @author Xavier Llora
 * @date Apr 26, 2010 at 3:11:37 PM
 *
 */
class Logger (cnf:Configuration,logCollection:String,uuid:UUID) {

  private val K_TS    = "ts"
  private val K_LEVEL = "lvl"
  private val K_MSG   = "msg"
  private val K_UUID  = "uuid"

  private val sUUID = uuid.toString

  val LVL_FINEST  = 5
  val LVL_FINER   = 4
  val LVL_FINE    = 3
  val LVL_INFO    = 2
  val LVL_WARNING = 1
  val LVL_SEVERE  = 0

  /** The mongo db connection object */
  private val mongo = cnf.mongo

  /** The Meandre database */
  private val db = mongo getDB cnf.MEANDRE_DB_NAME
  cnf.auth match {
    case Some((user,password)) => db.authenticate(user,password.toCharArray)
    case None =>
  }

  /** The logger collection */
  protected val collection = db getCollection logCollection

  /** Ensure index creation */
  collection.ensureIndex("{\""+K_TS+"\": 1}")
  collection.ensureIndex("{\""+K_LEVEL+"\": 1}")
  collection.ensureIndex("{\""+K_UUID+"\": 1}")

  var level = cnf.MEANDRE_LOG_LEVEL

  /**The logger actor in charge of pushing the updates into MongoDB.
   *
   */
  val logActor = actor {
    loop {
      react {
        case le:DBObject => collection insert le
      }
    }
  }

  /**Logs a severe message.
   *
   * @param lvl The hardcoded logger level
   * @param lvlName The loger level message
   * @param msg The message to log
   */
  protected def logMessage (lvl:Int,lvlName:String)( msg:String ) =
    if ( lvl<=level ) {
      val logObj = new BasicDBObject
      logObj.put(K_LEVEL,lvlName)
      logObj.put(K_UUID,sUUID)
      logObj.put(K_TS,new Date)
      logObj.put(K_MSG,msg)
      logActor ! logObj
    }

  /**Logs a severe message. */
  val severe = logMessage(LVL_SEVERE,"SEVERE") _

  /**Logs a warning message. */
  val warning = logMessage(LVL_WARNING,"WARNING") _

  /**Logs a info message. */
  val info = logMessage(LVL_INFO,"INFO") _

  /**Logs a fine message. */
  val fine = logMessage(LVL_FINE,"FINE") _

  /**Logs a finer message. */
   val finer = logMessage(LVL_FINER,"FINER") _

  /**Logs a finest message. */
   val finest = logMessage(LVL_FINEST,"FINEST") _


  /** The basic sorting condition by time stamps */
  val sortCndTS:BasicDBObject = """{"ts":-1}"""

  /**Returns the log entries that matches the provided criteria
   *
   * @param cnd The condition to query the log against
   * @param skip The number of entries to skip
   * @param limit The number of entries to retrieve
   */
  def getLogEntries(cnd:String,skip:Int,limit:Int) = {
    val les = new BasicDBList
    val q:BasicDBObject = cnd
    val cur = collection.find(q).sort(sortCndTS).skip(skip).limit(limit)
    while ( cur.hasNext ) {
      val d = cur.next.asInstanceOf[BasicDBObject]
      val r = new BasicDBObject
      r.put("meta","[%s] %s : %s".format(d.getString(K_LEVEL),d.getString(K_UUID),d.getString(K_TS)))
      r.put(K_MSG,d.getString(K_MSG))
      les add r
    }
    les
  }

}

/**
 * The companion object of the basic logger persisted on the the MongoDB store
 *
 * @author Xavier Llora
 * @date Apr 26, 2010 at 3:11:37 PM
 *
 */
object Logger {

  def apply(cnf:Configuration,uuid:UUID) = new Logger(cnf,cnf.MEANDRE_GLOBAL_LOGGER,uuid)

  def apply(cnf:Configuration,logCollection:String,uuid:UUID) = new Logger(cnf,logCollection,uuid)

}
