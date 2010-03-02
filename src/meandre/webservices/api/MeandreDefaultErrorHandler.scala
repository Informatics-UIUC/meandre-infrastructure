package meandre.webservices.api

import org.mortbay.jetty.{MimeTypes, HttpConnection}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import com.mongodb.BasicDBObject
import meandre.kernel.Implicits._
import meandre.webservices.api.Templating._
import org.mortbay.util.{StringUtil, ByteArrayISO8859Writer}
import java.io.{PrintWriter, StringWriter, Writer}
import org.mortbay.jetty.handler.{ErrorHandler}

/**
 * The default error handler for the HTML rendering
 *
 * @author Xavier Llora
 * @date Mar 2, 2010 at 2:51:03 PM
 *
 */

class MeandreDefaultErrorHandler extends ErrorHandler {

  /** Formats a string to avoid getting it broke.
   *
   */
  protected def format(message: String) = {
    var res = StringUtil.replace(message, "&", "&amp;")
    res = StringUtil.replace(res, "<", "&lt;")
    res = StringUtil.replace(res, ">", "&gt;")
    res = StringUtil.replace(res, "\"", "\\\"")
    res
  }

  /**Implements the handlers. The code from this functions has been
    * based on http://www.java2s.com/Open-Source/Java-Document/Sevlet-Container/jetty-modules/org/mortbay/jetty/handler/ErrorHandler.java.htm
   *
   * @param target The requested target
   * @param request The request object
   * @param response The response object
   * @param dispatch The dispatch value
   */
  override def handle(target: String,
             request: HttpServletRequest,
             response: HttpServletResponse,
             dispatch: Int) = {
    val connection = HttpConnection.getCurrentConnection
    connection.getRequest.setHandled(true)
    response.setContentType(MimeTypes.TEXT_HTML_8859_1)
    val writer = new ByteArrayISO8859Writer(4096)
    handleErrorPage(request, writer, connection.getResponse.getStatus, connection.getResponse.getReason);
    writer.flush;
    response.setContentLength(writer.size());
    writer.writeTo(response.getOutputStream());
    writer.destroy();
  }

  /**Get the formatted stack trace output.
   *
   * @param request The request that generated the error
   */
  def formatStackTrace(request:HttpServletRequest) = {
    val sw = new StringWriter()
    val pw = new PrintWriter(sw)
    var t = request.getAttribute("javax.servlet.error.exception").asInstanceOf[Throwable]

    while (t!=null) {
      t printStackTrace pw
      t = t.getCause
    }
    "<pre>"+format(sw.getBuffer.toString)+"</pre>"
  }


  /**Generate the error page itself.
   *
   * @param request The request that failed
   * @param writer The writer to use
   * @param code The error code
   * @param message The error message
   */
  override def handleErrorPage(request:HttpServletRequest, writer:Writer, code:Int, message:String) = {
    val revampMessage = format(message)
    val response:BasicDBObject = """{
        "status":"FAIL",
        "message":"""+revampMessage+""",
        "failure": { "code":"""+code+""",
                     "reason":"""+formatStackTrace(request)+"""}
    }"""
    response asHTMLTo new PrintWriter(writer)
  }

}