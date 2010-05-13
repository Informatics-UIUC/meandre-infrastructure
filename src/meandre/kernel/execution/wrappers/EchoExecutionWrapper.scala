package meandre.kernel.execution.wrappers

import meandre.kernel.execution.ExecutionWrapper
import java.io.{ByteArrayInputStream, File, InputStream}
import meandre.kernel.Configuration

/**
 * A dummy wrapper that echoes the repository that is being passed to it
 *
 * @author Xavier Llora
 * @date May 4, 2010 at 9:43:07 AM
 * 
 */

class EchoExecutionWrapper(cnf:Configuration) extends ExecutionWrapper {

  /**Given a serialized repository fires the wrapper. Returns the process
   * object and the STDOUT and STDERR streams
   *
   * @param repo The serialized repository to push
   * @return A 3-tuple containing the process executing, the STDOUT, and
   *         the STDERR
   *
   */
  override def fireWrapper(repo: Array[Byte]) : (Process, InputStream, InputStream) = {
    fireProcess(
      List(cnf.EXECUTION_SCALA,"-classpath",cnf.EXECUTION_CLASSPATH,cnf.EXECUTION_SCRIPT.format("echo"),(cnf.serverPort+1).toString),
      new File("."),
      repo
    )
  }

}