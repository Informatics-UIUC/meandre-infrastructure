package meandre.webservices.specs

import org.specs.Specification
import crochet.CrochetServer
import meandre.kernel.Configuration
import meandre.webservices.client.MeandreInfrastructureAPIClient
import meandre.webservices.api.MeandreInfrastructurePrivateAPI
import com.mongodb.BasicDBObject
import meandre.kernel.Implicits._
import snare.Snare
import meandre.webservices.logger.Logger

/**
 * The basic specs for the Meandre Infrastructure APIs
 *
 * @author Xavier Llora
 * @date Mar 1, 2010 at 11:20:34 AM
 *
 */

class MeandreInfrastructurePrivateAPISpecs extends Specification("The Meandre Infrastructure specification") {
  val TEST_SERVER_PORT = 6969
  val snareMon = Snare(
    "test@localhost:" + TEST_SERVER_PORT,
    "test",
    """{"server": {
          "protocol":"http",
          "host":"localfost",
          "port":"""+TEST_SERVER_PORT+""",
          "prefix":"/",
          "static":"static",
          "resources":"resources"
        },
        "mongodb": {
          "port":"localhost",
          "host":"27017"
        }
    }""",
    "localhost",
    27017,
    ( msg:BasicDBObject ) => {println(msg) ; true} )
  val cnf = Configuration()
  val api = new MeandreInfrastructurePrivateAPI(cnf,snareMon,Logger(cnf,snareMon.uuid))
  val client = MeandreInfrastructureAPIClient("http", "localhost", TEST_SERVER_PORT)
  var server:CrochetServer = null


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

  // TODO: Add specification for add location
  // TODO: Add specification for list location
  // TODO: Add specification for remove location
  // TODO: Add specification for remove all locations

  // TODO: Add specification for list components
  // TODO: Add specification for list flows

  "Ping services " should {

    "return pong" in {
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