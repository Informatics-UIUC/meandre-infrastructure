package meandre.webservices

import meandre.kernel.Implicits._
import crochet.CrochetServlet
import crochet.net.utils.HttpClient
import com.mongodb.BasicDBObject
import meandre.webservices.Templating._
import javax.servlet.http.HttpServletResponse
import util.parsing.json.JSON
import meandre.Tools.safeOp

/**
 * The Meandre infrastructure implementation of the services API
 *
 * @author Xavier Llora
 * @date Mar 1, 2010 at 9:26:33 AM
 * 
 */

class MeandreInfrastructureAPI extends CrochetServlet {

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

  //
  // The basic welcome message
  //
  get("/") {
    val res:BasicDBObject = """
      {
        "status":"OK",
        "success":{"Welcome":"You have reach the HTML interface for the Meandre Infrastructure"}
      }"""
    res serializeTo "html"
  }

  //
  // The well known ping
  //
  get("""/public/services/ping.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    val res:BasicDBObject = """{"status":"OK","success":{"message":"pong"}}"""
    res serializeTo elements(0)
  }


}
