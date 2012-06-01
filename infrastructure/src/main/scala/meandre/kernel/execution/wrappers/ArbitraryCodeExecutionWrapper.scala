package meandre.kernel.execution.wrappers

import meandre.kernel.execution.ExecutionWrapper
import java.io.{ByteArrayInputStream, File, InputStream}
import com.mongodb.BasicDBObject
import snare.Snare

/**
 * A dummy wrapper that executes whatever command was passed to it
 *
 * @author Xavier Llora
 * @date May 4, 2010 at 9:43:07 AM
 * 
 */

class ArbitraryCodeExecutionWrapper(command:List[String]) extends ExecutionWrapper {

  /**Given a serialized repository fires the wrapper. Returns the process
   * object and the STDOUT and STDERR streams
   *
   * @param repo The serialized repository to push
   * @return A 3-tuple containing the process executing, the STDOUT, and
   *         the STDERR
   *
   */
  override def fireWrapper(repo: Array[Byte]) : (Process, InputStream, InputStream) =
    fireProcess(command,new File("."),repo)

  override def getJobExecutionMeta(jobID:String, snare:Snare) : BasicDBObject = { new BasicDBObject }
}