package meandre.kernel.execution

import scala.actors.Actor
import scala.actors.Actor._
import com.mongodb.BasicDBObject
import meandre.kernel.Configuration
import java.util.UUID
import meandre.webservices.logger.Logger

/** The basic notification messages send to the execution actor */
case class CheckQueue()


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


  /** The main actor reactive loop
   *
   */
  def act() = {
    loop {
      react {
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
              
              queue.transitionJob(job getString "jobID", Preparing(), Running(), uuid.toString) match {
                case None =>
                  log.warning("Failed to transition job %s to %" format (job getString "jobID",Preparing()))

                case Some(preJob) =>
                  queue.jobRepository(preJob getString "jobID") match {
                    case None => // Failed to retrieve the repository
                      queue.transitionJob(job getString "jobID", Running(), Failed(), uuid.toString)
                      log.warning("Job %s failed because it has no associated repository" format (job getString "jobID"))
                      
                    case Some(repo) => // Ready to fire execution
                      val ex = ExecutionWrapper(cnf, preJob getString "wrapper")
                      val (process,console,joblog) = ex.fireWrapper(repo)

                      println("CONSOLE\n--------------")
                      var c = console.read
                      while (c>=0) {
                        System.out.write(c)
                        c = console.read
                      }

                      println("LOG\n--------------")
                      c = joblog.read
                      while (c>=0) {
                        System.err.write(c)
                        c = joblog.read
                      }
                      process.waitFor
                      println("Process exit status: "+process.exitValue)
                  }
              }


          }
        
        case unknown =>
          log.warning("Unknow message %s passed to the %s" format (unknown.toString,this.getClass.getName))
      }
    }
  }

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
