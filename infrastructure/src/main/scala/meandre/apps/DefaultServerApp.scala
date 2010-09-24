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
    cnf.EXECUTION_SCRIPT =  "scripts%1$sexecution_%%s.scala" format File.separator
    cnf.EXECUTION_CLASSPATH = "infrastructure%1$starget%1$sscala_2.7.7%1sclasses" format File.separator
    val server = MeandreServer(cnf, File.separator, "infrastructure%1$ssrc%1$smain%1$sresources%1$sstyling" format File.separator, "docs")
    server.go

  }
}
