package meandre.webservices.api

import com.mongodb.BasicDBObject
import meandre.kernel.Configuration
import meandre.kernel.Implicits._
import meandre.webservices.api.Templating._
import meandre.kernel.state.ContextsPool
import javax.servlet.http.HttpServletResponse._

import javax.servlet.http.HttpServletResponse.SC_NOT_FOUND
import util.DynamicVariable
import javax.servlet.http.{HttpServletRequest, HttpServlet, HttpServletResponse}
import java.net.URI

/**
 * The Meandre infrastructure implementation of the services API
 *
 * @author Xavier Llora
 * @date Mar 1, 2010 at 9:26:33 AM
 *
 */


class MeandreInfrastructureResourcesAPI(cnf: Configuration) extends HttpServlet {


  private val contextsPool = ContextsPool(cnf)


  // ---------------------------------------------------------------------------

  /**
    * Response to a get request.
    *
    * @param req  The request object
    * @param resp The response object
    */
  override def doGet(request: HttpServletRequest, response: HttpServletResponse) {
    val fileName = "context://localhost" + new URI(request.getRequestURI).getPath
    contextsPool.containsContext(fileName) match {

      case Right(true) =>
        contextsPool.write(fileName, response) match {
          case Right(_) =>
          case e =>
            println(e)
            response.sendError(SC_INTERNAL_SERVER_ERROR, "Failed to retrieve %s not found" format fileName)
        }

      case _ =>
        response.sendError(SC_NOT_FOUND, "Context %s not found" format fileName)

    }
  }

  // ---------------------------------------------------------------------------

}