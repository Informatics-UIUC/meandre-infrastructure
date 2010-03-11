package meandre.webservices.api

import crochet.CrochetServlet
import com.mongodb.BasicDBObject
import meandre.kernel.Implicits._
import meandre.webservices.api.Templating._
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import java.io.{Writer, PrintWriter, StringWriter}
import org.mortbay.jetty.{MimeTypes, HttpConnection}

/**
 * The Meandre infrastructure implementation of the services API
 *
 * @author Xavier Llora
 * @date Mar 1, 2010 at 9:26:33 AM
 * 
 */


class MeandreInfrastructureAbstractAPI extends CrochetServlet {

  protected val REQUEST_FAIL = "FAIL"
  protected val REQUEST_OK = "OK"
  protected val REQUEST_INCOMPLETE = "INCOMPLETE"

  /**Tries to convert a String into an integer. Returns 0 if fails.
   *
   * @param s The string to convert
   * @return The converted integer or 0 if it fails
   */
  protected def safeParseInt(s: String): Int = {

    try {
      Integer.parseInt(params("count"))
    }
    catch {
      case _ => 0
    }
  }

  /**Assembles an OK response.
   *
   * @param payload The response payload
   * @return The assembled response
   */
  protected def OKResponse(payload: Any) = {
    val res = new BasicDBObject
    res.put("status", REQUEST_OK)
    res.put("success", payload)
    res
  }

  /**Assembles a failure response.
   *
   * @param msg The failure message
   * @param payload The failure payload
   * @return The assembled response
   */
  protected def FailResponse(msg: String, payload:BasicDBObject): BasicDBObject = {
    val res:BasicDBObject = """{
          "status":"%s",
          "message":"%s"
    }""".format(REQUEST_FAIL, msg)
    res.put("failure",payload)
    res
  }

  /**Assembles a partial failure response.
   *
   * @param msg The partial failure message
   * @param successPayload The response payload
   * @param failurePayload The failure payload
   * @return The assembled response
   */
  protected def PartialFailResponse(msg:String, successPayload:BasicDBObject,failurePayload:BasicDBObject): BasicDBObject = {
    val res: BasicDBObject = """{
            "status":"%s",
            "message":"%s"
      }""".format(REQUEST_FAIL, msg)
    res.put("success", successPayload)
    res.put("failure", failurePayload)
    res
  }


  /**Sets the response type for the given response
   *
   * @param format The format to set up on the content type response
   */
  protected def canonicalResponseType = elements match {
    case Nil       => "text/plain"
    case "json"::_ => "application/json"
    case  "xml"::_ => "application/xml"
    case "html"::_ => "text/html"
    case _         => "text/plain"
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


  /**Get the formatted stack trace output.
   *
   * @param request The request that generated the error
   */
  def formatStackTrace(thOrig:Throwable) = {
    val sw = new StringWriter()
    val pw = new PrintWriter(sw)
    var t = thOrig

    while (t != null) {
      t printStackTrace pw
      t = t.getCause
    }
    sw.getBuffer.toString.replaceAll("\"", "\\\"")
  }


  /**Generate the error page itself.
   *
   * @param request The request that failed
   * @param writer The writer to use
   * @param code The error code
   * @param message The error message
   */
  def handleErrorPage(request: HttpServletRequest, writer: Writer, code: Int, message: String, content:String) = {
    val revampMessage = message.replaceAll("\"", "\\\"")
    val response: BasicDBObject = """{
         "status":"FAIL",
         "message":""" +'"'+ revampMessage +'"'+ """,
         "failure": { "code":""" + code + """, "reason":"""+'"'+content+'"'+"""}
     }"""
    response asHTMLTo new PrintWriter(writer)
  }


  /**Implements the handlers. The code from this functions has been
   * based on http://www.java2s.com/Open-Source/Java-Document/Sevlet-Container/jetty-modules/org/mortbay/jetty/handler/ErrorHandler.java.htm
   *
   * @param target The requested target
   * @param request The request object
   * @param response The response object
   * @param dispatch The dispatch value
   */
  def handle(target: String,
             request: HttpServletRequest,
             response: HttpServletResponse,
             code: Int, message: String, content:String ) = {
    val connection = HttpConnection.getCurrentConnection
    connection.getRequest.setHandled(true)
    response.setContentType(MimeTypes.TEXT_HTML_UTF_8)
    val writer = response.getWriter
    handleErrorPage(request, writer, code, message, content)
    writer.flush
  }


  //
  // Internal server error thrown while processing the request
  //
  override def internalServerError(path: String, request: HttpServletRequest,
                                   response: HttpServletResponse, e: Throwable) = {
    handle(path, request, response,
      HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
      "Internal server error processing " + path, formatStackTrace(e))
  }


  //
  // The requested was not authorized
  //
  override def unauthorizedAccess(path: String, user: Option[String],
                                  request: HttpServletRequest, response: HttpServletResponse) = {
    handle(path, request, response,
      HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
      "Unauthorized access for user "+user.getOrElse("unknown")+" for " + path,
      "Unauthorized access on "+path+" by user "+user.getOrElse("unknown") )
  }


  //
  // The requested path could not be found
  //
  override def requestNotFound(path: String, request: HttpServletRequest, response: HttpServletResponse) = {
    handle(path, request, response,
      HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
      "Request not found " + path,
      "Path does not align with Meandre API "+path )
  }


}