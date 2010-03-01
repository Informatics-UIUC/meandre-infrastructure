//
// A basic script to launch a server
//
import meandre.webservices.MeandreInfrastructureAPI
import crochet.CrochetServer
import meandre.webservices.client.MeandreInfrastructureAPIClient

val TEST_SERVER_PORT = 8080
val api = new MeandreInfrastructureAPI()
val client = MeandreInfrastructureAPIClient("http", "localhost", TEST_SERVER_PORT)
var server = CrochetServer(TEST_SERVER_PORT, api,"/Users/xavier/Documents/Workspace - IdeaProjects/Meandre-Infrastructure/styling","/static")
server.go