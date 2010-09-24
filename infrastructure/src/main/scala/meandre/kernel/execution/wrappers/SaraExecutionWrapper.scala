package meandre.kernel.execution.wrappers

import meandre.kernel.execution.ExecutionWrapper
import java.io.{File, InputStream}
import meandre.kernel.Configuration

/**
 * @author Boris Capitanu
 */

class SaraExecutionWrapper(cnf: Configuration) extends ExecutionWrapper {
  override def fireWrapper(repo: Array[Byte]): (Process, InputStream, InputStream) = {
    fireProcess(
      List(cnf.EXECUTION_JAVA, cnf.EXECUTION_JAVA_ARGS, "-jar", cnf.EXECUTOR_14X, (cnf.serverPort + 1).toString),
      new File("."),
      repo
      )
  }
}
