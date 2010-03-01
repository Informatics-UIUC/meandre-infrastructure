package meandre.webservices.client

import crochet.net.utils.HttpClient
import meandre.kernel.Implicits._
import meandre.Tools._

/**
 * A basic customized client for the Meandre infrastructure services API
 *
 * @author Xavier Llora
 * @date Mar 1, 2010 at 9:41:13 AM
 */
class MeandreInfrastructureAPIClient(protocol: String, host: String, prefix:String, port: Int) {

  val http = HttpClient(protocol,host,port)

  /**Invokes a ping to the server and provides the return.
    *
   * @return Pong if succeeded
   */
  def ping  = safeOp { http.get(prefix+"/public/services/ping.json") }


}

/**The basic companion object for the meandre infrastructure api client.
 *
 * @author Xavier Llora
 * @date Mar 1, 2010 at 11:38:24 AM
 */
object MeandreInfrastructureAPIClient {

  def apply (protocol: String, host: String, port: Int)  = new MeandreInfrastructureAPIClient(protocol,host,"",port)

  def apply (protocol: String, host: String, prefix:String, port: Int)  = new MeandreInfrastructureAPIClient(protocol,host,prefix,port)

}