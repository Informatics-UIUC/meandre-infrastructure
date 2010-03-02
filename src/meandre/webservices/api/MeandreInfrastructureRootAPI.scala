package meandre.webservices.api

import meandre.kernel.Configuration
import com.mongodb.BasicDBObject
import meandre.kernel.Implicits._
import meandre.webservices.api._
import meandre.webservices.api.Templating._
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

/**
 * The Meandre infrastructure implementation of the services API
 *
 * @author Xavier Llora
 * @date Mar 1, 2010 at 9:26:33 AM
 * 
 */


class MeandreInfrastructureRootAPI(cnf:Configuration) extends MeandreInfrastructureAbstractAPI {


  //
  // Internal server error thrown while processing the request
  //
  override def internalServerError(path: String, request: HttpServletRequest, response: HttpServletResponse, e: Throwable) = {

  }


  //
  // The requested was not authorized
  //
  override def unauthorizedAccess(path: String, user:Option[String],request: HttpServletRequest, response: HttpServletResponse) = {

  }


  //
  // The requested path could not be found
  //
  override def requestNotFound(path: String, request: HttpServletRequest, response: HttpServletResponse) = {

  }

//  //
//  // The basic welcome message
//  //
//  get("/") {
//    val res:BasicDBObject = """{
//        "status":"OK",
//        "success":{"Welcome":"You have reach the HTML interface for the Meandre Infrastructure"}
//    }"""
//    res serializeTo "html"
//  }

}