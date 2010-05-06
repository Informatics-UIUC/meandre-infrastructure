package meandre.kernel.specs

import java.util.UUID
import meandre.kernel.Configuration
import org.specs.Specification
import com.mongodb.{BasicDBObject, Mongo}
import meandre.kernel.Implicits._
import meandre.kernel.execution._

/**
 * Implements a collection of specifications to test the functionality
 * provided by the Store class and companion object
 *
 * @author Xavier Llora
 * @date Feb 15, 2010 at 3:44:35 PM
 *
 */
object JobQueueSpecs extends Specification("The context pool specification") {
  val cnf = Configuration()
  cnf.MEANDRE_DB_NAME = "Meandre_Test"
  val REPS = 1000

  val repo = "This is a silly mock repository".getBytes
  val user = "test_user"
  val server = UUID.randomUUID.toString
  val wrapper = "Snowfield"
  val meta: BasicDBObject = """{"uri":"meandre://test.org/some/silly/flow","params":{}}"""


  "A job queue " should {

    doFirst {
      (new Mongo).dropDatabase(cnf.MEANDRE_DB_NAME)
    }

    "Be able to insert jobs at will" in {
      val queue = JobQueue(cnf)
      val size = queue.size

      val res = (1 to REPS).toList.map(i => queue.push(repo, user + "_" + i, server, wrapper, meta, 5))

      res.exists(_.isEmpty) must beFalse

      queue.size must beEqualTo(size + REPS)
      queue.sizeQueued must beEqualTo(size + REPS)
      queue.sizePreparing must beEqualTo(0)
      queue.sizeRunning must beEqualTo(0)
      queue.sizeDone must beEqualTo(0)
      queue.sizeAborted must beEqualTo(0)
      queue.sizeKilled must beEqualTo(0)
    }

  }

  "Be able to grab queued jobs, change its status to preparing, and transition to running" in {
    val queue = JobQueue(cnf)

    queue.size must beEqualTo(REPS)
    queue.sizeQueued must beEqualTo(REPS)

    (1 to REPS).toList.foreach (
      i => queue grabQueuedJob server  match {
        case None => None must beEqualTo("failure")
        case Some(obj:BasicDBObject) =>
          obj.getString("status") must beEqualTo("Queued") // Returns the object before modification
          val upObj = queue.transitionJob(obj.getString("jobID"),Preparing(),Running(),server).get
          upObj.getString("status") must beEqualTo("Preparing") // Returns the object before modification
          queue job upObj.getString("jobID") match {
            case None => None must beEqualTo("failure")
            case Some(finObj:BasicDBObject) => finObj.getString("status") must beEqualTo("Running")
          }
      }
    )

    queue.sizeQueued must beEqualTo(0)
    queue.sizeRunning must beEqualTo(REPS)
  }

}