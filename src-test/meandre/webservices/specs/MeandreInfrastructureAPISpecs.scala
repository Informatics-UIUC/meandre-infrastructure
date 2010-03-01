package meandre.webservices.specs

import org.specs.Specification
import meandre.webservices.MeandreInfrastructureAPI
import crochet.CrochetServer
import meandre.webservices.client.MeandreInfrastructureAPIClient
import meandre.kernel.Implicits._
import com.mongodb.BasicDBObject

/**
 * The basic specs for the Meandre Infrastructure APIs
 *
 * @author Xavier Llora
 * @date Mar 1, 2010 at 11:20:34 AM
 *
 */

class MeandreInfrastructureAPISpecs extends Specification("The Meandre Infrastructure specification") {
  val TEST_SERVER_PORT = 6969
  val api = new MeandreInfrastructureAPI()
  val client = MeandreInfrastructureAPIClient("http", "localhost", TEST_SERVER_PORT)
  var server: CrochetServer = null


  //
  // Create an new test server for the specs
  //
  doBeforeSpec {
    server = CrochetServer(TEST_SERVER_PORT, api)
    server.start
  }

  //
  // Return stop and dispose the server
  //
  doAfterSpec {
    server.stop
    server = null
  }

  "Ping services " should {

    "return pong in" in {
       client.ping match {
         case Right((200,pong)) => val json:BasicDBObject = pong
                                   pong.getString("status")  must beEqualTo("OK")
                                   pong.getString("message") must beEqualTo("pong")
         
         case Left(t) => fail(t.toString)
         case unknown => fail(unknown.toString)
       }
    }


  }

}