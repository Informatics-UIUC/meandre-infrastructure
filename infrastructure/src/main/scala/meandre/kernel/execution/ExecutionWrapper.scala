package meandre.kernel.execution

import meandre.kernel.Configuration
import wrappers.{SaraExecutionWrapper, EchoExecutionWrapper}
import java.io._
import com.mongodb.BasicDBObject
import snare.Snare

/**
 * The basic class that all wrappers have to implement
 *
 * @author Xavier Llora
 * @date May 4, 2010 at 9:41:22 AM
 *
 */

abstract class ExecutionWrapper {

  /**Starts a new process. Given a command, a directory to use as based,
   * and an input stream containing the serialized RDF repository to run,
   * spawns a process and returns the process object and the STDOUT and
   * STDERR streams
   *
   * @param command The command to run
   * @param dir The directory to run it from
   * @param repo The serialized repository
   * @return A 3-tuple containing the process executing, the STDOUT, and
   *         the STDERR
   *
   */
  protected def fireProcess(command: List[String], dir: File, repo:Array[Byte]) = {
    val pb = new ProcessBuilder(java.util.Arrays.asList(command.toArray: _*))
    //
    // TODO Environment manipulation
    //
    pb directory dir
    val p = pb.start
    val pos = p.getOutputStream
    pos write repo
    pos.flush
    pos.close
    (p, p.getInputStream, p.getErrorStream)
  }

  /**Given a serialized repository fires the wrapper. Returns the process
   * object and the STDOUT and STDERR streams
   *
   * @param repo The serialized repository to push
   * @return A 3-tuple containing the process executing, the STDOUT, and
   *         the STDERR
   *
   */
  def fireWrapper(repo:Array[Byte]) : (Process, InputStream, InputStream)

  /**
   * Get any metadata additional metadata for a particular job given the executor used
   *
   * @param jobID The job ID
   * @param snare The snare instance to use for communication
   * @return The BasicDBObject describing the metadata
   */
  def getJobExecutionMeta(jobID:String, snare:Snare): BasicDBObject
}


/**
 * The basic trait that all wrappers have to implement
 *
 * @author Xavier Llora
 * @date May 4, 2010 at 9:41:22 AM
 *
 */

object ExecutionWrapper {

  val validExecutionWrapper = Set("1.4.x","Snowfield","custom","echo")


  val defaultExecutionWrapper = "1.4.x"

  def apply(cnf:Configuration,engine:String) =
    //
    // TODO Add the missing 2 execution engines
    //
    engine match {
      case "1.4.x" => new SaraExecutionWrapper(cnf)
      case "echo" => new EchoExecutionWrapper(cnf)
    }
}
