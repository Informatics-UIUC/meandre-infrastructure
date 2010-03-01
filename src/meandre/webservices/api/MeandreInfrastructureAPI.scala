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
   * @param response The reponse object to set up
   */
  private def setResponseType (format:String, response:HttpServletResponse) = format match {
    case "json" => response setContentType "application/json"
    case "xml"  => response setContentType "application/xml"
    case "html" => response setContentType "text/html"
  }

  get("""/public/services/ping.(json|xml|html)""".r) {
    val format = elements(0)
    val res:BasicDBObject = """{"status":"OK","message":"pong"}"""
    res.formatToResponse(format,response)
  }


}
