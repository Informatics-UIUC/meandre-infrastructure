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

    println(System.getenv)
    println(new File(".").getAbsolutePath)
    val TEST_SERVER_PORT = 1714
    val cnf = Configuration()
    cnf.EXECUTION_SCALA = "/usr/local/bin/scala"
    cnf.EXECUTION_SCRIPT = "/Users/xavier/Documents/Workspace - IdeaProjects/Meandre-Infrastructure/scripts/execution_%s.scala"
    cnf.EXECUTION_CLASSPATH = "/Users/xavier/Documents/Workspace - IdeaProjects/Meandre-Infrastructure/out/production/Meandre-Infrastructure"
    val server = MeandreServer(cnf, "/", "./infrastructure/src/main/resources/styling", "./docs")
    server.go

  }
}