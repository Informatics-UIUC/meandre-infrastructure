package meandre.webservices.api

import meandre.kernel.Implicits._
import crochet.CrochetServlet
import crochet.net.utils.HttpClient
import com.mongodb.BasicDBObject
import meandre.webservices.api.Templating._
import javax.servlet.http.HttpServletResponse
import util.parsing.json.JSON
import meandre.Tools.safeOp
import meandre.kernel.Configuration


/**
 * The Meandre infrastructure implementation of the services API
 *
 * @author Xavier Llora
 * @date Mar 1, 2010 at 9:26:33 AM
 * 
 */


class MeandreInfrastructurePrivateAPI(cnf:Configuration) extends MeandreInfrastructureAbstractAPI {


  //
  // The well known ping
  //
  get("""/services/test/ping.(json|xml|html)""".r, canonicalResponseType, tautologyGuard, public _) {
    val res:BasicDBObject = """{
         "status":"OK",
         "success":{"message":"test pong!!!"}
    }"""
    res serializeTo elements(0)
  }


}