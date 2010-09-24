package meandre.apps

import meandre.kernel.Configuration
import meandre.webservices.api.MeandreServer
import java.io.File

/**
 * XXX
 *
 * @author Xavier Llora
 * @date Jul 1, 2010 at 4:44:09 PM
 *
 */

object DefaultServerApp {
  def main(args: Array[String]) = {

    //
    // TODO This needs to be fix to make it Windows friendly and do a better job with the scala home
    //
    val TEST_SERVER_PORT = 1714
    val cnf = Configuration()
    cnf.EXECUTION_SCALA = "scala"
    cnf.EXECUTION_SCRIPT = "scripts/execution_%s.scala"
    cnf.EXECUTION_CLASSPATH = "infrastructure/target/scala_2.7.7/classes"
    val server = MeandreServer(cnf, "/", "infrastructure/src/main/resources/styling", "docs")
    server.go

  }
}
