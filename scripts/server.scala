//
// A basic script to launch a server
//
import meandre.kernel.Configuration
import meandre.webservices.api.{MeandreServer}
//
// One server
//

val TEST_SERVER_PORT = 1714
val cnf = Configuration()
cnf.EXECUTION_SCALA = "/usr/local/bin/scala"
cnf.EXECUTION_SCRIPT = "/Users/xavier/Documents/Workspace - IdeaProjects/Meandre-Infrastructure/scripts/execution.scala"
cnf.EXECUTION_CLASSPATH = "/Users/xavier/Documents/Workspace - IdeaProjects/Meandre-Infrastructure/out/production/Meandre-Infrastructure"
val server = MeandreServer(cnf, "/", "/Users/xavier/Documents/Workspace - IdeaProjects/Meandre-Infrastructure/styling", "/Users/xavier/Documents/Workspace - IdeaProjects/Meandre-Infrastructure/docs")
server.go


//
// Three servers
//

//import scala.concurrent.ops.spawn
//
//val TEST_SERVER_PORT = 1714
//
//val (cnf1,cnf2,cnf3) = (
//        Configuration(),
//        Configuration("http","localhost",TEST_SERVER_PORT+1000,"localhost",27017,None),
//        Configuration("http","localhost",TEST_SERVER_PORT+2000,"localhost",27017,None)
//    )
//
//val server  = MeandreServer(cnf1, "/", "/Users/xavier/Documents/Workspace - IdeaProjects/Meandre-Infrastructure/styling", "/Users/xavier/Documents/Workspace - IdeaProjects/Meandre-Infrastructure/docs")
//val server2 = MeandreServer(cnf2, "/", "/Users/xavier/Documents/Workspace - IdeaProjects/Meandre-Infrastructure/styling", "/Users/xavier/Documents/Workspace - IdeaProjects/Meandre-Infrastructure/docs")
//val server3 = MeandreServer(cnf3, "/", "/Users/xavier/Documents/Workspace - IdeaProjects/Meandre-Infrastructure/styling", "/Users/xavier/Documents/Workspace - IdeaProjects/Meandre-Infrastructure/docs")
//
//spawn { server.start  }
//spawn { server2.start  }
//server3.start
//
//server.join
//server2.join
//server3.join