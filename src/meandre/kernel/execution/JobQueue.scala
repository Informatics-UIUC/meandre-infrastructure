package meandre.kernel.execution

import meandre.kernel.Implicits._
import meandre.kernel.Configuration
import java.util.Date
import com.mongodb.{BasicDBList, BasicDBObject, Mongo}
import java.security.MessageDigest
import java.util.Random
import java.math.BigInteger
import com.mongodb.gridfs.GridFS
import meandre.kernel.state.PersistentTextBuffer
import java.io.{ByteArrayInputStream, OutputStream}

//
// Basic job statuses
//
sealed case class JobStatus()
//
// The possible jobe statuses
//
case class Queued()    extends JobStatus() { override def toString = "Queued" }
case class Preparing() extends JobStatus() { override def toString = "Preparing" }
case class Running()   extends JobStatus() { override def toString = "Running" }
case class Done()      extends JobStatus() { override def toString = "Done" }
case class Failed()    extends JobStatus() { override def toString = "Failed" }
case class Aborted()   extends JobStatus() { override def toString = "Aborted" }
case class Killed()    extends JobStatus() { override def toString = "Killed" }

/**
 * Provides the basic facilities to manage a unified job queue for 
 * a Meandre cluster.
 *
 * @author Xavier Llora
 * @date May 5, 2010 at 11:37:49 AM
 *
 */

class JobQueue(val cnf: Configuration) {
  //
  // Basic collection gathering
  //
  val mongo = new Mongo
  val db = mongo getDB cnf.MEANDRE_DB_NAME
  val queue = db getCollection cnf.MEANDRE_JOBS_QUEUE
  val consoles = new GridFS(db,cnf.MEANDRE_JOBS_CONSOLE)
  val logs = new GridFS(db,cnf.MEANDRE_JOBS_LOG)

  //
  // Ensure indexes are available
  //
  queue.ensureIDIndex
  queue.ensureIndex("""{"status":1}""")
  queue.ensureIndex("""{"status":1,"ts":-1}""")
  queue.ensureIndex("""{"ts":-1}""")
  queue.ensureIndex("""{"server":1,"status":1}""")

  //
  // The digest generator
  //
  val rnd = new Random
  val md = MessageDigest getInstance "SHA"

  /**Generate a unique md5 from a list of byte arrays.
   *
   * @param bytes The list of byte arrays
   * @return The text version of the MD5
   */
  protected def md5 ( ba:List[Array[Byte]] ) = {
    try {
      md.reset
      ba foreach (md.update _)
      val md5String = (new BigInteger(md.digest)).abs.toString(16)
      if (md5String.length%2 != 0) "0"+md5String
      else md5String
    }
    catch {
      case _ => "unknown"
    }

  }

  /**Pushes a new job into the queue. Returns the relevant
   * status information about the job.
   *
   * @param repo The repository describing the job
   * @param user The user submitting the job
   * @param server The server from where the job was submitted
   * @param wrapper The requested execution wrapper to use
   * @param meta The metadata related to the job
   * @param attempts The number of attempts allowed trying to insert the job
   * @return The job information
   */
  def push ( repo:Array[Byte], user:String, server:String, wrapper:String, meta:BasicDBObject, attempts:Int ) : Option[BasicDBObject] = {
    val ts = new Date
    val job:BasicDBObject =
      """
      {
        "owner" : "%s",
        "wrapper" : "%s",
        "status" : "%s"
      }
      """ format (user,wrapper,Queued())
    job.put("repo",repo)
    job.put("ts", ts)
    val log:BasicDBObject =
      """
        {
          "status" : "%s",
          "server" : "%s",
        }
      """ format (Queued(),server)
    log.put("ts",ts)
    val progress = new BasicDBList
    progress add  log
    job.put("progress",progress)
    job.put("meta",meta)
    try {
      job.put(
        "_id",md5(
            List(
              repo,
              user.getBytes,
              server.getBytes,
              wrapper.getBytes,
              rnd.nextFloat.toString.getBytes
            )))
      queue insert job
      job.put("jobID",job.get("_id"))
      job remove "_id"
      job remove "_ns"
      job remove "repo"
      Some(job)
    }
    catch {
      case _ =>
        attempts match {
          case 0 => None
          case _ => push(repo,user,server,wrapper,meta,attempts-1)
        }
    }
  }

  /**Given a starting server it updates all the jobs that may have been lefts
   * hanging after a crash.
   *
   * @param server The server to update
   */
  def updateStartingServer(server:String) = {
    //
    // TODO Compact outputs of dead jobs
    //
    val cnd : BasicDBObject =
        """
          {
            "server":"%s",
            "status" : { "$in" : ["Preparing","Running"] }
          }
        """ format server

    //
    // Compact left behind jobs
    //
    val fields:BasicDBObject = """{"_id":1}"""
    val cur = queue.find(cnd,fields)
    while (cur.hasNext) {
      val jobID = cur.next.get("_id").toString
      compactJobData(jobID)
    }

    //
    // Update the status to Killed
    //
    val updateCnd : BasicDBObject  =
        """
          {
            "$set": { "status" : "Killed", "server" : "%s" },
            "$push" : {
              "progress" : {
                "status" : "Killed",
                "server" : "%s"
              }
            }
          }
        """ format (server,server)
    queue.update(cnd,updateCnd,false,true)
  }

  /**Sets the exit process code for a given job.
   *
   * @param jobID The job ID status
   * @param status The exit code status to set
   */
  def setExitProcessForJob (jobID:String,status:Int) = {
    val cnd : BasicDBObject =
        """
          {
            "_id":"%s"
          }
        """ format jobID
    val updateCnd : BasicDBObject  =
        """
          {
            "$set": { "exit_code" : "%d" }
          }
        """ format status
    queue.update(cnd,updateCnd)
  }

  /**Grabs a queued jobs and atomically transitions it to preparing status or
   * or forces a fails.
   *
   * @param server The server requesting the modifications
   * @return The id if the status transitioned correctly
   */
  def grabQueuedJob(server:String):Option[BasicDBObject] = {
    val cmd : BasicDBObject =
        """
          {
            "findandmodify" : "%s",
            "query":
              {
                "status" : "%s"
              },
            "sort" :
              {
                "ts" : -1
              },
            "update" :
              {
                "$set": { "status" : "%s"},
                "$push" : {
                  "progress" : {
                    "status" : "%s",
                    "server" : "%s"
                  }
                }
              }
          }
        """ format (queue.getName,Queued(),Preparing(),Preparing(),server)
    val ts = new Date
    val update = cmd.get("update").asInstanceOf[BasicDBObject]
    val set = update.get("$set").asInstanceOf[BasicDBObject]
    set.put("ts",ts)
    set.put("server",server)
    val push = update.get("$push").asInstanceOf[BasicDBObject].get("progress").asInstanceOf[BasicDBObject]
    push.put("ts",ts)
    //println(cmd)
    val res = db command cmd
    if ( res.containsField("errmsg") ) None
    else {
      val j = res.get("value").asInstanceOf[BasicDBObject]
      j.put("jobID",j.get("_id"))
      j remove "_id"
      j remove "_ns"
      j remove "repo"
      Some(j)
    }
  }

  /**Given a job, it atomically transitions to the new status or
   * or forces a fails.
   *
   * @param id The id of the job to transition
   * @param currentStatus The current status of the job
   * @param newStatus The new status of the job
   * @param server The server requesting the modifications
   * @return The id if the status transitioned correctly
   */
  def transitionJob(id:String,currentStatus:JobStatus, newStatus:JobStatus, server:String) : Option[BasicDBObject] = {
    val cmd : BasicDBObject =
        """
          {
            "findandmodify" : "%s",
            "query":
              {
                "_id" : "%s",
                "status" : "%s"
              },
            "sort" :
              {
                "ts" : -1
              },
            "update" :
              {
                "$set": { "status" : "%s"},
                "$push" : {
                  "progress" : {
                    "status" : "%s",
                    "server" : "%s"
                  }
                }
              }
          }
        """ format (queue.getName,id,currentStatus,newStatus,newStatus,server)
    val ts = new Date
    val update = cmd.get("update").asInstanceOf[BasicDBObject]
    val set = update.get("$set").asInstanceOf[BasicDBObject]
    set.put("ts",ts)
    set.put("server",server)
    val push = update.get("$push").asInstanceOf[BasicDBObject].get("progress").asInstanceOf[BasicDBObject]
    push.put("ts",ts)            
    //println(cmd)
    val res = db command cmd
    if ( res.containsField("errmsg") ) None
    else {
      val j = res.get("value").asInstanceOf[BasicDBObject]
      j.put("jobID",j.get("_id"))
      j remove "_id"
      j remove "_ns"
      j remove "repo"
      Some(j)
    }
  }

  /**Given a job ID, returns all the information associated to the job.
   *
   * @param id The ID of the job to retrieve
   * @returns None if the job does not exist, or the document containing the Job information
   */
  def job ( id:String ) = {
    val jobID:BasicDBObject = """{"_id":"%s"}""" format id
    val cur = queue find jobID
    if ( cur.size!=1 ) None
    else {
      val j = cur.next.asInstanceOf[BasicDBObject]
      j.put("jobID",j.get("_id"))
      j remove "_id"
      j remove "_ns"
      j remove "repo"
      Some(j)
    }
  }

  /**Given a job ID, returns the repository associated to it.
   *
   * @param id The ID of the job to retrieve
   * @returns None if the job does not exist, or the byte array containing the repository
   */
  def jobRepository ( id:String ) = {
    val jobID:BasicDBObject  = """{"_id":"%s"}""" format id
    val fields:BasicDBObject = """{"repo":1}"""
    queue.findOne(jobID,fields) match {
      case null => None
      case job  => Some(job.get("repo").asInstanceOf[Array[Byte]]) 
    }
  }

  /**Returns the list of job ids.
   *
   * @param offset The number of jobs to skip
   * @param limit The maximun number of jobs to return
   * @return The list of IDS
   */
  def jobIDs(offset:Int,limit:Int) : List[String] = {
    var res:List[String] = Nil
    val cur = queue.find("{}","""{"_id":1}""").sort("""{"ts":1}""").skip(offset).limit(limit)
    while ( cur.hasNext ) res ::= cur.next.get("_id").toString
    res
  }

  /**Returns the list of all job ids.
    *
    * @param offset The number of jobs to skip
    * @param limit The maximun number of jobs to return
    * @return The list of IDS
    */
  def jobIDs : List[String] = jobIDs(0,Integer.MAX_VALUE)

  /**Returns the size of the global queue.
    *
   * @return The size of the total queue
   */
  def size = queue.find.count

  /**Returns the number of jobs that match a particular criteria.
   *
   * @param cnd The condition to match against
   * @return The number of jobs that satisfy such a condition
   */
  protected def count(cnd:BasicDBObject) = queue.find(cnd).count

  /**Counts the jobs on a given status.
   *
   * @param status The job status to count on
   * @return the number of counts
   */
  protected def countJobsOnStatus(status:JobStatus) = count("""{"status":"%s"}""" format status)

  /**Count the number of queued jobs.
   *
   * @return The count of queued jobs
   */
  def sizeQueued = countJobsOnStatus(Queued())

  /**Count the number of preparing to execute jobs.
   *
   * @return The count of preparing to execute jobs
   */
  def sizePreparing = countJobsOnStatus(Preparing())

  /**Count the number of running jobs.
   *
   * @return The count of running jobs
   */
  def sizeRunning = countJobsOnStatus(Running())

  /**Count the number of done jobs.
   *
   * @return The count of done jobs
   */
  def sizeDone = countJobsOnStatus(Done())

  /**Count the number of aborted jobs.
   *
   * @return The count of aborted jobs
   */
  def sizeAborted = countJobsOnStatus(Aborted())

  /**Count the number of killed jobs.
   *
   * @return The count of killed jobs
   */
  def sizeKilled = countJobsOnStatus(Killed())

  /**Query the jobs in the queue.
   *
   * @param cnd The condition to query against the queue
   * @param srt The sort condition
   * @param skip The number of entries to skip
   * @param limit The number of entries to return
   * @return The list of jobs retrieved
   */
  def queryQueue(query:BasicDBObject,srt:BasicDBObject,skip:Int,limit:Int) = {
    var res:List[BasicDBObject] = Nil
    val cur = queue.find(query).sort(srt).skip(skip).limit(limit)
    while ( cur.hasNext ) {
      val j = cur.next.asInstanceOf[BasicDBObject]
      j.put("jobID",j.get("_id"))
      j remove "_id"
      j remove "_ns"
      j remove "repo"
      res ::= j
    }
    res
  }

  /**Compact job related data, mostly the console and the logs, into a GridFS
   * system to allow storing large outputs.
   *
   * @param jobID The jobID of the job to compact
   */
  def compactJobData(jobID:String) = {
    val ptbCon = new PersistentTextBuffer(cnf,"jobs.%s.console" format jobID)
    val ptbLog = new PersistentTextBuffer(cnf,"jobs.%s.log" format jobID)

    val fc = consoles.createFile(new ByteArrayInputStream(ptbCon.toString.getBytes),jobID)
    fc.save

    val fl = logs.createFile(new ByteArrayInputStream(ptbLog.toString.getBytes),jobID)
    fl.save

    ptbCon.destroy
    ptbLog.destroy
  }

  /**Dumps the console for the given jobID
   *
   * @param jobID The jobID to use
   * @param os The output stream where to dump the console
   */
  def dumpConsoleFor(jobID:String,os:OutputStream) = {
    consoles.findOne(jobID) match {

      case null => // The console has not been compacted yet
        val ptbCon = new PersistentTextBuffer(cnf,"jobs.%s.console" format jobID)
        os.write(ptbCon.toString.getBytes)

      case file => // The console has already been compacted
        file writeTo os
    }
  }

  /**Dumps the logs for the given jobID
   *
   * @param jobID The jobID to use
   * @param os The output stream where to dump the logs
   */
  def dumpLogFor(jobID:String,os:OutputStream) = {
    logs.findOne(jobID) match {

      case null => // The console has not been compacted yet
        val ptbLog = new PersistentTextBuffer(cnf,"jobs.%s.log" format jobID)
        os.write(ptbLog.toString.getBytes)

      case file => // The console has already been compacted
        file writeTo os
    }
  }
}


/**
 * The companion object that provides basic mechanics to build basic
 * instances of the JobQueue, which provides the basic facilities to
 * managed the unified job queue for a Meandre cluster.
 *
 * @author Xavier Llora
 * @date May 5, 2010 at 11:37:49 AM
 *
 */

object JobQueue {
  def apply(cnf: Configuration) = new JobQueue(cnf)
}