package meandre.webservices.api

import meandre.kernel.Configuration
import com.mongodb.BasicDBObject
import meandre.kernel.Implicits._
import meandre.webservices.api.Templating._
/**
 * The Meandre infrastructure implementation of the services API
 *
 * @author Xavier Llora
 * @date Mar 1, 2010 at 9:26:33 AM
 *
 */


class MeandreInfrastructureRootAPI(cnf: Configuration) extends MeandreInfrastructureAbstractAPI {


    //
    // The basic welcome message
    //
    get("/") {
      val res:BasicDBObject = """{
          "status":"OK",
          "success":{"Welcome":"You have reached the HTML interface for the Meandre Infrastructure"}
      }"""
      res serializeTo "html"
    }

}