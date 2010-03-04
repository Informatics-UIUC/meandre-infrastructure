package meandre.webservices.client

import crochet.net.utils.HttpClient
import meandre.kernel.Implicits._
import meandre.Tools._
import java.net.URLEncoder

/**
 * A basic customized client for the Meandre infrastructure services API
 *
 * @author Xavier Llora
 * @date Mar 1, 2010 at 9:41:13 AM
 */
class MeandreInfrastructureAPIClient(protocol: String, host: String, prefix:String, port: Int, credentials:Option[Pair[String,String]]) {

  val http = HttpClient(protocol,host,port,credentials)

  // ---------------------- Public -------------------------------------------

  /**Invokes a ping to the server and provides the return.
    *
   * @return Pong if succeeded
   */
  def ping  = safeOp { http.get(prefix+"/public/services/ping.json") }


  // ---------------------- Authenticated ------------------------------------

  /** Given an array of locations and descriptors request to get them added
   *  to the user's repository.
   *
   * @param locations The array of locations to add
   * @param descriptions The descriptions of each of the locations to add
   * @return The list of added uri and locations
   */
  def addLocations ( locations:Array[String],descriptions:Array[String] ) = safeOp {
    val params = (locations zip descriptions).foldLeft("")(
       (a,pair) => a+"location="+URLEncoder.encode(pair._1,"UTF8")+"&"
                    +"description="+URLEncoder.encode(pair._2,"UTF8")+"&"
    )
    http.get(prefix+"/services/locations/add.json?"+params)
  }

  /**Returns the list of locations added to the user's repository
   *
   * @return The list of locations
   */
  def listLocations = safeOp { http.get(prefix+"/services/locations/list.json") }

}

/** The basic companion object for the meandre infrastructure api client.
 *
 * @author Xavier Llora
 * @date Mar 1, 2010 at 11:38:24 AM
 */
object MeandreInfrastructureAPIClient {

  def apply (protocol: String, host: String, port: Int)  = new MeandreInfrastructureAPIClient(protocol,host,"",port,None)

  def apply (protocol: String, host: String, prefix:String, port: Int)  = new MeandreInfrastructureAPIClient(protocol,host,prefix,port,None)

  def apply (protocol: String, host: String, prefix:String, port: Int, userName:String, password:String)  = new MeandreInfrastructureAPIClient(protocol,host,prefix,port,Some((userName,password)))

}