package meandre.webservices

import java.io.PrintWriter
import xml.{Text, Elem}
import com.mongodb.BasicDBObject
import meandre.Implicits._
import java.util.{ArrayList, Date}

/**
 * This class wraps a basic db object and adds template oriented
 * functionality.
 *
 * @author Xavier Llora
 * @date Feb 24, 2010 at 2:10:34 PM
 *
 */

class RichBasicDBObject (val self:BasicDBObject) extends Proxy {

  /**Given a response document, it formats it according to the provided
   * format.
   *
   * @param format The target format
   * @param writer The basic object containing the response to format
   */
  def format ( format:String, writer:PrintWriter ) : RichBasicDBObject = format match {
    case "json" => writer.println(asJSON) ; this
    case "xml"  => writer.println(asXML)  ; this
    case "html" => writer.println("html") ; this
    case _      => writer.println(self)   ; this
  }

  /** Curried version of the format function */
  protected val cFormat = Function.curried(format _)

  /** Converts to json */
  val asJSONTo = cFormat("json")

  /** Converts to xml */
  val asXMLTo  = cFormat("xml")

  /** Converts to html */
  val asHTMLTo = cFormat("html")

  /** Converts the document into a JSON string
    *
    * @return The json of the given object
    */
  def asJSON  = self.toString

  /** Converts the document into XML
   *
   * @return The XML of the given object
   */
  def asXML : Elem = {
    def transformObject ( d:BasicDBObject ) : Elem = {
      <meandre_item>
        {
          for ( k <- d.keySet.iterator )
            yield Elem(null, k, scala.xml.Null, scala.xml.TopScope, d.get(k) match {
                     // The value is an object
                     case t:BasicDBObject => transformObject(t)
                     // The value is not a list
                     case a:ArrayList[Object] => <meandre_list>{transformArray(a)}</meandre_list>
                     // The value is just a terminal
                     case t => Text(t.toString)
                  })
        }
      </meandre_item>
    }

    def transformArray ( a:ArrayList[Object] ) : Any =
      for ( i<-List.range(0,a.size) )
        yield Elem(null, "meandre_list_item", scala.xml.Null, scala.xml.TopScope,a.get(i) match {
          // The element is an object
          case t:BasicDBObject => transformObject(t)
          // The element is another list
          case a:ArrayList[Object] => <meandre_list>{transformArray(a)}</meandre_list>
          // Basic type
          case t => Text(t.toString)
        })

    <meandre_response>{transformObject(self)}</meandre_response>
  }
  
}

/**
 * This class contains templates and basic mechanics for rendering request
 * responses. Main output formats are JSON, XML, and HTML.
 *
 * @author Xavier Llora
 * @date Feb 24, 2010 at 2:10:34 PM
 * 
 */

object Templating {

  /**Provides implicit conversion from a basic db object to a rich
   * basic db object
   *
   * @param bdbo The basic db object to enrich
   * @return The enriched basic db object
   */
  implicit def basicDBObject2RichBasicDBObject ( bdbo:BasicDBObject ) = new RichBasicDBObject(bdbo)


  /**Provides implicit conversion from a rich basic db object to a
   * basic db object
   *
   * @param rbdbo The enriched basic db object to enrich
   * @return The basic bd object
   */
  implicit def richBasicDBObject2BasicDBObject ( rbdbo:RichBasicDBObject ) = rbdbo.self


  def html ( title:String, pathPrefix:String, response:String ) =
    "<html>"+
     htmlHeader(title,pathPrefix)+
    "<body>"+
     htmlMenu +
    """<br/>
       <div class="response">"""+
       response +
    """</div>"""+
     htmlFooter +
    """</body>
     </html>"""

  /** The html header section */
  def htmlHeader ( title:String, pathPrefix:String ) = """
    <head>
      <title>MI - """+title+"""</title>
      <link rel="stylesheet" type="text/css" href="""+'"'+pathPrefix+"""style.css"> 
    </head>
  """

  /** The html page footer */
  def htmlFooter = """
    <div class="footer">
        <p class="footer">&copy; DITA, NCSA, and UofI, 2007-2010.</p>
        <p class="footer-date">"""+new Date+"""</p>
    </div>
  """

  /** The HTML menu */
  val htmlMenu = """
    <div class="header">
    <p class="header-info">anonymous@dev-demo.seasr.org:1714</p>

    <ul class="pureCssMenu pureCssMenum">
        <li class="pureCssMenui"><a class="pureCssMenui" href="http://seasr.org/meandre">Meandre</a></li>

        <li class="pureCssMenui"><a class="pureCssMenui" href="#"><span>Repository</span><![if gt IE 6]></a><![endif]>
            <!--[if lte IE 6]>
            <table>
                <tr>
                    <td><![endif]-->
            <ul class="pureCssMenum">
                <li class="pureCssMenui"><a class="pureCssMenui" href="#"><span>Locations</span><![if gt IE 6]></a><![endif]>
                    <!--[if lte IE 6]>
                    <table>
                        <tr>
                            <td><![endif]-->
                    <ul class="pureCssMenum">
                        <li class="pureCssMenui"><a class="pureCssMenui" href="#">Add</a></li>
                        <li class="pureCssMenui"><a class="pureCssMenui" href="#">List</a></li>
                        <li class="pureCssMenui"><a class="pureCssMenui" href="#">Remove all</a></li>
                    </ul>
                    <!--[if lte IE 6]></td></tr></table></a><![endif]--></li>
                <li class="pureCssMenui"><a class="pureCssMenui" href="#"><span>Components</span><![if gt IE 6]></a><![endif]>
                    <!--[if lte IE 6]>
                    <table>
                        <tr>
                            <td><![endif]-->
                    <ul class="pureCssMenum">
                        <li class="pureCssMenui"><a class="pureCssMenui" href="#">Sorted by name</a></li>
                        <li class="pureCssMenui"><a class="pureCssMenui" href="#">Sorted by date</a></li>
                    </ul>
                    <!--[if lte IE 6]></td></tr></table></a><![endif]--></li>
                <li class="pureCssMenui"><a class="pureCssMenui" href="#"><span>Flows</span><![if gt IE 6]></a><![endif]>
                    <!--[if lte IE 6]>
                    <table>
                        <tr>
                            <td><![endif]-->
                    <ul class="pureCssMenum">
                        <li class="pureCssMenui"><a class="pureCssMenui" href="#">Sorted by name</a></li>
                        <li class="pureCssMenui"><a class="pureCssMenui" href="#">Sorted by date</a></li>
                    </ul>
                    <!--[if lte IE 6]></td></tr></table></a><![endif]--></li>
                <li class="pureCssMenui"><a class="pureCssMenui" href="#"><span>Tags</span><![if gt IE 6]></a><![endif]>
                    <!--[if lte IE 6]>
                    <table>
                        <tr>
                            <td><![endif]-->
                    <ul class="pureCssMenum">
                        <li class="pureCssMenui"><a class="pureCssMenui" href="#">All</a></li>
                        <li class="pureCssMenui"><a class="pureCssMenui" href="#">Components</a></li>
                        <li class="pureCssMenui"><a class="pureCssMenui" href="#">Flows</a></li>
                    </ul>
                    <!--[if lte IE 6]></td></tr></table></a><![endif]--></li>
                <li class="pureCssMenui"><a class="pureCssMenui" href="#"><span>Public</span><![if gt IE 6]></a><![endif]>
                    <!--[if lte IE 6]>
                    <table>
                        <tr>
                            <td><![endif]-->
                    <ul class="pureCssMenum">
                        <li class="pureCssMenui"><a class="pureCssMenui" href="#">Published repository</a></li>
                        <li class="pureCssMenui"><a class="pureCssMenui" href="#">Demo repository</a></li>
                    </ul>
                    <!--[if lte IE 6]></td></tr></table></a><![endif]--></li>

                <li class="pureCssMenui"><a class="pureCssMenui" href="#">Clear</a></li>
                <li class="pureCssMenui"><a class="pureCssMenui" href="#">Regenerate</a></li>
            </ul>
            <!--[if lte IE 6]></td></tr></table></a><![endif]--></li>

        <li class="pureCssMenui"><a class="pureCssMenui" href="#"><span>Publish</span><![if gt IE 6]></a><![endif]>
            <!--[if lte IE 6]>
            <table>
                <tr>
                    <td><![endif]-->
            <ul class="pureCssMenum">
                <li class="pureCssMenui"><a class="pureCssMenui" href="#">List published</a></li>
                <li class="pureCssMenui"><a class="pureCssMenui" href="#">Publish all</a></li>
                <li class="pureCssMenui"><a class="pureCssMenui" href="#">Unpublish all</a></li>

                <li class="pureCssMenui"><a class="pureCssMenui" href="#"><span>Others</span><![if gt IE 6]></a><![endif]>
                    <!--[if lte IE 6]>
                    <table>
                        <tr>
                            <td><![endif]-->
                    <ul class="pureCssMenum">
                        <li class="pureCssMenui"><a class="pureCssMenui" href="#">Published repository</a></li>
                        <li class="pureCssMenui"><a class="pureCssMenui" href="#">Demo repository</a></li>
                    </ul>
                    <!--[if lte IE 6]></td></tr></table></a><![endif]--></li>
            </ul>


        <li class="pureCssMenui"><a class="pureCssMenui" href="#"><span>Infrastructure</span><![if gt IE 6]></a><![endif]>
            <!--[if lte IE 6]>
            <table>
                <tr>
                    <td><![endif]-->
            <ul class="pureCssMenum">
                <li class="pureCssMenui"><a class="pureCssMenui" href="#"><span>Execution</span><![if gt IE 6]></a><![endif]>
                    <!--[if lte IE 6]>
                    <table>
                        <tr>
                            <td><![endif]-->
                    <ul class="pureCssMenum">
                        <li class="pureCssMenui"><a class="pureCssMenui" href="#">Run a flow</a></li>
                        <li class="pureCssMenui"><a class="pureCssMenui" href="#">Run a repository</a></li>
                    </ul>
                    <!--[if lte IE 6]></td></tr></table></a><![endif]--></li>
                <li class="pureCssMenui"><a class="pureCssMenui" href="#"><span>Jobs</span><![if gt IE 6]></a><![endif]>
                    <!--[if lte IE 6]>
                    <table>
                        <tr>
                            <td><![endif]-->
                    <ul class="pureCssMenum">
                        <li class="pureCssMenui"><a class="pureCssMenui" href="#">Running flows</a></li>
                        <li class="pureCssMenui"><a class="pureCssMenui" href="#">Job statuses</a></li>
                        <li class="pureCssMenui"><a class="pureCssMenui" href="#">Job consoles</a></li>
                    </ul>
                    <!--[if lte IE 6]></td></tr></table></a><![endif]--></li>
                <li class="pureCssMenui"><a class="pureCssMenui" href="#"><span>Logs</span><![if gt IE 6]></a><![endif]>
                    <!--[if lte IE 6]>
                    <table>
                        <tr>
                            <td><![endif]-->
                    <ul class="pureCssMenum">
                        <li class="pureCssMenui"><a class="pureCssMenui" href="#">Summarized</a></li>
                        <li class="pureCssMenui"><a class="pureCssMenui" href="#">Detailed</a></li>
                    </ul>
                    <!--[if lte IE 6]></td></tr></table></a><![endif]--></li>
                <li class="pureCssMenui"><a class="pureCssMenui" href="#"><span>Cluster</span><![if gt IE 6]></a><![endif]>
                    <!--[if lte IE 6]>
                    <table>
                        <tr>
                            <td><![endif]-->
                    <ul class="pureCssMenum">
                        <li class="pureCssMenui"><a class="pureCssMenui" href="#">Log</a></li>
                        <li class="pureCssMenui"><a class="pureCssMenui" href="#">Status</a></li>
                        <li class="pureCssMenui"><a class="pureCssMenui" href="#">Information</a></li>
                        <li class="pureCssMenui"><a class="pureCssMenui" href="#">Properties</a></li>
                        <li class="pureCssMenui"><a class="pureCssMenui" href="#">Shutdown server</a></li>
                    </ul>
                    <!--[if lte IE 6]></td></tr></table></a><![endif]--></li>
            </ul>
            <!--[if lte IE 6]></td></tr></table></a><![endif]--></li>



        <li class="pureCssMenui"><a class="pureCssMenui" href="#"><span>Security</span><![if gt IE 6]></a><![endif]>
            <!--[if lte IE 6]>
            <table>
                <tr>
                    <td><![endif]-->
            <ul class="pureCssMenum">
                <li class="pureCssMenui"><a class="pureCssMenui" href="#">Create user</a></li>
                <li class="pureCssMenui"><a class="pureCssMenui" href="#">Delete user</a></li>
                <li class="pureCssMenui"><a class="pureCssMenui" href="#">List users</a></li>
                <li class="pureCssMenui"><a class="pureCssMenui" href="#"><span>Roles</span><![if gt IE 6]></a><![endif]>
                    <!--[if lte IE 6]>
                    <table>
                        <tr>
                            <td><![endif]-->
                    <ul class="pureCssMenum">
                        <li class="pureCssMenui"><a class="pureCssMenui" href="#">User roles</a></li>
                        <li class="pureCssMenui"><a class="pureCssMenui" href="#">Valid roles</a></li>
                    </ul>
                    <!--[if lte IE 6]></td></tr></table></a><![endif]--></li>

                <li class="pureCssMenui"><a class="pureCssMenui" href="#">Role map</a></li>
                <li class="pureCssMenui"><a class="pureCssMenui" href="#">Rovoke all user roles</a></li>

            </ul>




        <li class="pureCssMenui"><a class="pureCssMenui" href="#"><span>Help</span><![if gt IE 6]></a><![endif]>
            <!--[if lte IE 6]>
            <table>
                <tr>
                    <td><![endif]-->
            <ul class="pureCssMenum">
                <li class="pureCssMenui"><a class="pureCssMenui" href="http://seasr.org/meandre/documentation/">Documentation</a></li>
                <li class="pureCssMenui"><a class="pureCssMenui" href="http://dev-tools.seasr.org/confluence/display/MDR/Welcome">Development wiki</a></li>
                <li class="pureCssMenui"><a class="pureCssMenui" href="http://dev-tools.seasr.org/confluence/display/COOK/Quick+recipes+to+make+Meandre+Pies">Cookbook</a></li>
                <li class="pureCssMenui"><a class="pureCssMenui" href="http://dev-tools.seasr.org/jira/secure/Dashboard.jspa">Issue tracking</a></li>
                <li class="pureCssMenui"><a class="pureCssMenui" href="http://dev-tools.seasr.org/fisheye/browse/Meandre-Infrastructure">SVN browser</a></li>
            </ul>

        </ul>
  </div>
  """
}