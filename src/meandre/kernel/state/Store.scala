package meandre.kernel.state

import meandre.kernel.Configuration
import meandre.kernel.Tools.zip3
import com.mongodb.Mongo
import java.io.InputStream
import meandre.state.Repository
import meandre.Implicits._
import meandre.kernel.rdf._

/**A base case class for all addable elements to the store */
abstract sealed case class Element()

/**The case class that describes a location element */
case class LocationElement(url: String) extends Element()

/**The case class that describes a bundled element */
case class BundledElement(desc:Descriptor, names: List[String], mimeTypes: List[String], contexts: List[InputStream]) extends Element

//---------------------------------------------------------------------------

/**
 * Implements the main stateless wrapper to available stored data
 *
 * @author Xavier Llora
 * @date Feb 15, 2010 at 3:39:05 PM
 *
 */

class Store(override val cnf: Configuration, override val userName: String, val overwrite:Boolean, val embed:Boolean)
extends Repository(cnf,userName) {

  //---------------------------------------------------------------------------

  /** The contexts pool to use */
  private val contextsPool = ContextsPool(cnf)


  //---------------------------------------------------------------------------

  /** Adds or overwrite a component as appropriate.
   *
   * @param d The descriptor to add to the repository
   * @return The uri if succeeded, None otherwise
   */
  private def addToRepository ( d:Descriptor ) = overwrite match {
    case false if exist(d.uri) => None
    case _ => add(d).head
  }

  //---------------------------------------------------------------------------

  /**Attempts to add all the provided elements to the userName repository.
   *
   * @param elements The list of elements to add to teh userName repository
   * @return The list containing the rdf uri of the added components/flows or None
   *         if it could not be added (failed or overwrite was not set)
   */
  def addElements[A <: Element](elements: List[A]): List[Either[Throwable, List[Option[RDFURI]]]] = elements map {
    _ match {
      //
      // It is a location that needs to be added
      //
      case LocationElement(url) => Left(new Exception("Not implemented yet"))

      //
      // It is a bundled descriptor usually the result of and upload
      //
      case BundledElement(desc, names, mimes, contexts) => try {
        val namingOK = names.length == contexts.length
        val res:Option[RDFURI] = desc match {
            // Adding a flow to the repository
            case f: FlowDescriptor => addToRepository(f)
            // Adding a component to the repository if naming is OK
            case c: ComponentDescriptor if !namingOK =>  None
            // Adding a component to the repository if naming is OK
            case c: ComponentDescriptor if  namingOK => {
              // Safe the files to the context pool
              var cnames:List[String] = List(if (c.uri.endsWith("/") ) c.uri+"implementation/"
                                             else c.uri+"/implementation/")
              val addedContexts = zip3(names,mimes,contexts).map( _ match {
                case (name,mime,is) => {
                  val modName = "context://localhost/"+name.replaceAll("""\s+""","-")
                  cnames ::= modName
                  contextsPool.update(modName,mime,is)
                }}
              ).foldLeft(true)((a,b)=>a&&b.isRight)
              addedContexts match {
                case false => None
                case true => c.context = cnames.map(URIContext(_)) ; addToRepository(c)
              }
            }
            case _ => None
          }
        Right(res)
      }
      catch {
        case t => println(t.toString+"!!!!!!!!!!!!!!!");Left(t)
      }
    }
  }

  /** Remove everything from the store
   *
   */
  override def removeAll = {
    super.removeAll
    contextsPool.removeAll
  }

}

/**The companion object for the store.
 *
 *
 * @author Xavier Llora
 * @date Feb 15, 2010 at 3:39:05 PM
 *
 */
object Store {

  /**Creates a new instance of the store.
   *
   * @param cnf The configuration object
   * @param user The user store to build
   * @param overwrite Should component/flows be overwrote
   * @param embed Should component context be embedded?
   */
  def apply (cnf: Configuration, user: String, overwrite:Boolean, embed:Boolean) = new Store(cnf,user,overwrite,embed)

}