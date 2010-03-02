//
// A basic script to launch a server
//
import crochet.CrochetServer
import meandre.kernel.Configuration
import meandre.webservices.api.{MeandreServer, MeandreInfrastructurePublicAPI}
import meandre.webservices.client.MeandreInfrastructureAPIClient

val TEST_SERVER_PORT = 8080
val cnf = Configuration()
val server = MeandreServer(cnf, TEST_SERVER_PORT, "", "/Users/xavier/Documents/Workspace - IdeaProjects/Meandre-Infrastructure/styling", "/Users/xavier/Documents/Workspace - IdeaProjects/Meandre-Infrastructure/docs")
server.go