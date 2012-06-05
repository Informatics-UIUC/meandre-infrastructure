package meandre.kernel.execution

import actors.Actor
import actors.Actor._
import actors.Futures._
import com.mongodb.BasicDBObject
import meandre.kernel.Configuration
import java.util.UUID
import meandre.webservices.logger.Logger
import meandre.kernel.state.PersistentTextBuffer

/** The basic notification messages send to the execution actor */
sealed trait ExecutionMessages
case class InitQueue()  extends ExecutionMessages
case class CheckQueue() extends ExecutionMessages

/** The killing actor messages */
sealed trait KillingActorMessages
case class RegisterJob(server:String,jobID:String,proc:Process) extends KillingActorMessages
case class UnregisterCurrentJob() extends KillingActorMessages
case class KillJob(server:String,jobID:String) extends  KillingActorMessages

/**A simple actor who's job is to kill processes.
 *
 * @author Xavier Llora
 * @date May 13, 2010 at 11:21:45 AM
 */
class KillerJobActor() extends Actor {
  var server: Option[String] = None
  var jobID: Option[String] = None
  var proc: Option[Process] = None

  def act() {
    loop {
      react {
        case RegisterJob(s, j, p) =>
          server = Some(s)
          jobID = Some(j)
          proc = Some(p)

        case UnregisterCurrentJob() =>
          server = None
          jobID = None
          proc = None

        case KillJob(s, j) =>
          proc match {
            case Some(p) if (s == server.getOrElse("") && j == jobID.getOrElse("")) =>
              p.destroy
              this ! UnregisterCurrentJob()
            case _ =>
          }
      }
    }
  }
}

/**
 * This class implements the actor that is in charge of execution jobs
 * that have been queued on the global execution queue
 *
 * @author Xavier Llora
 * @date May 10, 2010 at 9:36:17 AM
 *
 */
class QueuedJobExecutionActor(cnf:Configuration,uuid:UUID) extends Actor {

  /** The global logger */
  protected val log = Logger(cnf,uuid)

  /** The global job queue */
  protected val queue = JobQueue(cnf)

  /** The killing actor */
  protected val killerActor = new KillerJobActor()
  killerActor.start

  /** The main actor reactive loop
   *
   */
  def act() {
    loop {
      react {
        case InitQueue() =>
          log.info("Cleaning the queue and requesting to check the queue")
          queue.updateStartingServer(uuid.toString)
          this ! CheckQueue()

        case CheckQueue() =>
          log.info("CheckQueue message received")
          //
          // Try to grab a job if available (basic ongoing thoughts)
          //  - Run if able to grab
          //  - If fail and queued jobs send notification to self
          //  - If fail and no queued jobs, do nothing
          //
          queue.grabQueuedJob(uuid.toString) match {
            case None =>
              if (queue.sizeQueued>0) this ! CheckQueue()

            case Some(job) =>
              val jobID = job getString "jobID"
              queue.transitionJob(jobID, Preparing(), Running(), uuid.toString) match {
                case None =>
                  log.warning("Failed to transition job %s to %" format (jobID,Preparing()))

                case Some(preJob) =>
                  queue.jobRepository(jobID) match {
                    case None => // Failed to retrieve the repository
                      queue.transitionJob(jobID, Running(), Failed(), uuid.toString)
                      log.warning("Job %s failed because it has no associated repository" format jobID)

                    case Some(repo) => // Ready to fire execution
                      try {
                        log.info("Job %s starting execution" format jobID)

                        val ex = ExecutionWrapper(cnf, preJob getString "wrapper")
                        val (process,console,joblog) = ex.fireWrapper(repo)
                        killerActor ! RegisterJob(uuid.toString,jobID,process)
                        // Capture the console
                        val consoleDone = future {
                          try {
                            val cptb = new PersistentTextBuffer(cnf,"jobs.%s.console" format jobID)
                            cptb append console
                          }
                          catch {
                            case s =>
                              println(s)
                              log.warning("Console reading for job %s failed because of %s".format(jobID,s.getMessage))
                          }
                          1
                        }
                        // Capture the logs
                        val jobLogDone = future {
                          try {
                            val lptb = new PersistentTextBuffer(cnf,"jobs.%s.log" format jobID)
                            lptb append joblog
                          }
                          catch {
                            case s =>
                              println(s)
                              log.warning("Log reading for job %s failed because of %s".format(jobID,s.getMessage))
                          }
                          1
                        }
                        // Wait for graceful ending
                        process.waitFor
                        consoleDone() ; jobLogDone()
                        // Clean up after the process
                        queue.setExitProcessForJob(jobID,process.exitValue)
                        (System getProperty  "os.name").split(" ")(0) match {
                          case "Mac" | "Linux" | "Solaris" =>
                            process.exitValue match {
                              case 0 => // Success
                                queue.transitionJob(jobID, Running(), Done(), uuid.toString)
                                log.info("Job %s finished execution successfully" format jobID)
                              case 143 => // Killed
                                queue.transitionJob(jobID, Running(), Killed(), uuid.toString)
                                log.info("Job %s was killed" format jobID)
                              case _ => // Failed
                                queue.transitionJob(jobID, Running(), Failed(), uuid.toString)
                                log.info("Job %s failed during execution" format jobID)
                            }

                          case "Windows" =>
                            process.exitValue match {
                              case 0 => // Success
                                queue.transitionJob(jobID, Running(), Done(), uuid.toString)
                                log.info("Job %s finished execution successfully" format jobID)
                              case 1 => // Killed
                                queue.transitionJob(jobID, Running(), Killed(), uuid.toString)
                                log.info("Job %s was killed" format jobID)
                              case _ => // Failed
                                queue.transitionJob(jobID, Running(), Failed(), uuid.toString)
                                log.info("Job %s failed during execution" format jobID)
                            }

                          case unknown =>
                            process.exitValue match {
                              case 0 => // Assume success
                                queue.transitionJob(jobID, Running(), Done(), uuid.toString)
                                log.info("Job %s finished execution successfully" format jobID)
                              case ev => // Assume failed
                                log.warning("Unknown OS: %s  - Do not know how to handle process exit code %d; assuming job %s has FAILED" format (unknown, ev, jobID))
                                queue.transitionJob(jobID, Running(), Failed(), uuid.toString)
                            }
                        }

                        killerActor ! UnregisterCurrentJob()
                        queue compactJobData jobID
                      }
                      catch {

                        case e =>
                          val failMsg = "Job %s failed during execution because %s".format(jobID,e.getMessage)
                          log warning failMsg
                          val ptbLog = new PersistentTextBuffer(cnf,"jobs.%s.log" format jobID)
                          ptbLog append failMsg
                          queue.transitionJob(jobID, Running(), Failed(), uuid.toString)
                          queue compactJobData jobID
                          killerActor ! UnregisterCurrentJob()
                      }
                  }
              }
              if (queue.sizeQueued>0) this ! CheckQueue()
          }

        case unknown =>
          log.warning("Unknow message %s passed to the %s" format (unknown.toString,this.getClass.getName))
      }
    }
  }

  /**Backdoor to kill a job
   *
   * @param jobID The jobID to kill (in case it is being run in this server
   */
  def killJob(jobID:String) = killerActor ! KillJob(uuid.toString,jobID)

  /** Get the actor running */
  start
}

/**
 * The companion object to the class that implements the actor that is in charge
 * of execution jobs that have been queued on the global execution queue
 *
 * @author Xavier Llora
 * @date May 10, 2010 at 9:36:17 AM
 *
 */
object QueuedJobExecutionActor {

  def apply(cnf:Configuration,uuid:UUID) = new QueuedJobExecutionActor(cnf,uuid)

}
