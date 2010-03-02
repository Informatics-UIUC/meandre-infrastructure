package meandre.webservices.api

import crochet.CrochetServlet



/**
 * The Meandre infrastructure implementation of the services API
 *
 * @author Xavier Llora
 * @date Mar 1, 2010 at 9:26:33 AM
 * 
 */


class MeandreInfrastructureAbstractAPI extends CrochetServlet {

  /**Sets the response type for the given response
   *
   * @param format The format to set up on the content type response
   */
  protected def canonicalResponseType = elements(0) match {
    case "json" => "application/json"
    case "xml"  => "application/xml"
    case "html" => "text/html"
    case _      => "text/plain"
  }

  /** The guard is set to always return true.
   *
   * @return true always
   */
  protected def tautologyGuard = true

  /**The method is designed as public, since authentication always returns true.
   *
   * @param path The path to authorize
   * @param user The user to authenticate
   * @return true always
   */
  protected def public (path:String,user:Option[String]):Boolean = true

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