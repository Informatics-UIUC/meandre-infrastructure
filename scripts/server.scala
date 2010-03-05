//
// A basic script to launch a server
//
import meandre.kernel.Configuration
import meandre.webservices.api.{MeandreServer}
//
// One server
//
val TEST_SERVER_PORT = 8080
val cnf = Configuration()
val server = MeandreServer(cnf, TEST_SERVER_PORT, "", "/Users/xavier/Documents/Workspace - IdeaProjects/Meandre-Infrastructure/styling", "/Users/xavier/Documents/Workspace - IdeaProjects/Meandre-Infrastructure/docs")
server.go


//
// Two servers
//
//import scala.concurrent.ops.spawn 
//
//val TEST_SERVER_PORT = 8080
//val cnf = Configuration()
//val server = MeandreServer(cnf, TEST_SERVER_PORT, "", "/Users/xavier/Documents/Workspace - IdeaProjects/Meandre-Infrastructure/styling", "/Users/xavier/Documents/Workspace - IdeaProjects/Meandre-Infrastructure/docs")
//val server2 = MeandreServer(cnf, TEST_SERVER_PORT+1, "", "/Users/xavier/Documents/Workspace - IdeaProjects/Meandre-Infrastructure/styling", "/Users/xavier/Documents/Workspace - IdeaProjects/Meandre-Infrastructure/docs")
//spawn { server.go  }
//server2.go