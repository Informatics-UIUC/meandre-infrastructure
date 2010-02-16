package meandre.kernel.state

import meandre.kernel.Configuration
import com.mongodb.Mongo
import java.io.InputStream
import meandre.state.Repository
import meandre.kernel.rdf.{FlowDescriptor, ComponentDescriptor, DescriptorsFactory}
import meandre.Implicits._

/**
 * Implements the main stateless wrapper to available stored data
 *
 * @author Xavier Llora
 * @date Feb 15, 2010 at 3:39:05 PM
 *
 */

class Store(val cnf: Configuration, val user: String, val overwrite:Boolean, val embed:Boolean) {

  //---------------------------------------------------------------------------

  type RDFURI = String

  //---------------------------------------------------------------------------

  /**A base case class for all addable elements to the store */
  abstract sealed case class Element()

  /**The case class that describes a location element */
  case class LocationElement(url: RDFURI) extends Element()

  /**The case class that describes a bundled element */
  case class BundledElement(rdf: String, names: List[Option[String]], contexts: List[InputStream]) extends Element

  //---------------------------------------------------------------------------

  /**The name of the Meandre store repository collection */
  private val MEANDRE_COLLECTION = "meandre"

  /**The mongo db connection object */
  private val mongo = new Mongo(cnf.host, cnf.port)

  /**The Meandre database */
  private val db = mongo getDB cnf.MEANDRE_DB_NAME
  cnf.auth match {
    case Some((user, password)) => db.authenticate(user, password.toCharArray)
    case None =>
  }

  /**The user collection */
  private val collection = db getCollection MEANDRE_COLLECTION

  /**Test connectivity. Will thrown an exception if cannot connect */
  collection.find.count

  /**Create the repository wrapper for the given user*/
  private val repository = Repository(cnf,user)

  //---------------------------------------------------------------------------

  /**Attempts to add all the provided elements to the user repository.
   *
   * @param elements The list of elements to add to teh user repository
   * @return The list containing the rdf uri of the added components/flows or None
   *         if it could not be added (failed or overwrite was not set)
   */
  def addElements[A <: Element](elements: List[A]): List[Either[Throwable, List[RDFURI]]] = elements map {
    _ match {
      //
      // It is a location that needs to be added
      //
      case LocationElement(url) => Left(new Exception("Not implemented yet"))

      //
      // It is a bundled descriptor usually the result of and upload
      //
      case BundledElement(rdf, names, contexts) => try {
        val res:List[Option[String]] = DescriptorsFactory(rdf) map {
          _ match {
            // Adding a flow to the repository
            case f: FlowDescriptor => repository.add(f).head match {
              case Some(uri) => repository.updateRDFFor(uri,rdf)
                                Some(uri)
              case None => None
            }
            // Adding a component to the repository
            case c: ComponentDescriptor => None
          }
        }
        Right(res filter {_.isDefined} map {_.get})
      }
      catch {
        case t => Left(t)
      }
    }
  }


}