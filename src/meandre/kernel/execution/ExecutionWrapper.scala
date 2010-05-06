package meandre.kernel.execution

import java.io.{File, OutputStream, InputStream}

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
   * @param is The input stream to use
   * @return A 3-tuple containing the process executing, the STDOUT, and
   *         the STDERR
   *
   */
  protected def fireProcess(command: List[String], dir: File, is: InputStream) = {
    val pb = new ProcessBuilder(java.util.Arrays.asList(command.toArray: _*))
    //
    // TODO Environment manipulation
    //
    pb directory dir
    val p = pb.start
    val pos = p.getOutputStream
    var b = is.read
    while (b >= 0) {
      pos write b
      b = is.read
    }
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
  def fireWrapper(repo: String) : (Process, InputStream, InputStream)

}


/**
 * The basic trait that all wrappers have to implement
 *
 * @author Xavier Llora
 * @date May 4, 2010 at 9:41:22 AM
 *
 */

object ExecutionWrapper {

  val validExecutionWrapper = Set("1.4.x","Snowfield")

  val defaultExecutionWrapper = "Snowfield"
}
