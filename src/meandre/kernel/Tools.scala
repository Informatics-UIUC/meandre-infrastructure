package meandre.kernel

import rdf.Descriptor
import meandre.Implicits._
import java.util.Date

/**
 * Provides a simple collection of commodity tools for sorting descriptors,
 * descriptors metadata, and the like
 *
 * @author Xavier Llora
 * @date Feb 11, 2010 at 5:18:42 PM
 *
 */

object Tools {

  //----------------------------------------------------------------------

  /**Sorts a list of descriptors by URI
   *
   * @param l The list of descriptors to sort.
   * @return The sorted list
   */
  def sortByURI[A <: Descriptor](l: List[A]) = l.sort(_.uri < _.uri)


  /**Sorts a list of descriptors by name
   *
   * @param l The list of descriptors to sort.
   * @return The sorted list
   */
  def sortByName[A <: Descriptor](l: List[A]) = l.sort(_.description.name < _.description.name)


  /**Sorts a list of descriptors by creator
   *
   * @param l The list of descriptors to sort.
   * @return The sorted list
   */
  def sortByCreator[A <: Descriptor](l: List[A]) = l.sort(_.description.creator.getOrElse("") < _.description.creator.getOrElse(""))


  /**Sorts a list of descriptors by rights
   *
   * @param l The list of descriptors to sort.
   * @return The sorted list
   */
  def sortByRights[A <: Descriptor](l: List[A]) = l.sort(_.description.rights.getOrElse("") < _.description.rights.getOrElse(""))


  /**Sorts a list of descriptors by date
   *
   * @param l The list of descriptors to sort.
   * @return The sorted list
   */
  def sortByDate[A <: Descriptor](l: List[A]) = l.sort((a, b) => a.description.creationDate.before(b.description.creationDate))


  //----------------------------------------------------------------------

  /**Sorts a list of descriptors metadata by URI
   *
   * @param l The list of descriptors to sort.
   * @return The sorted list
   */
  def sortMetadataByURI[A <: Map[String,Any]](l: List[A]) = l.sort( _(K_ID).toString < _(K_ID).toString)


  /**Sorts a list of descriptors by name
   *
   * @param l The list of descriptors to sort.
   * @return The sorted list
   */
  def sortMetadataByName[A <: Map[String,Any]](l: List[A]) = l.sort( _(K_NAME).toString < _(K_NAME).toString)

  /**Sorts a list of descriptors by creator
   *
   * @param l The list of descriptors to sort.
   * @return The sorted list
   */
  def sortMetadataByCreator[A <: Map[String,Any]](l: List[A]) = l.sort( _(K_CREATOR).toString < _(K_CREATOR).toString)


  /**Sorts a list of descriptors by rights
   *
   * @param l The list of descriptors to sort.
   * @return The sorted list
   */
  def sortMetadataByRights[A <: Map[String,Any]](l: List[A]) = l.sort( _(K_RIGHTS).toString < _(K_RIGHTS).toString)

  /**Sorts a list of descriptors by date
   *
   * @param l The list of descriptors to sort.
   * @return The sorted list
   */
  def sortMetadataByDate[A <: Map[String,Any]](l: List[A]) =
    l.sort((a, b) => a(K_DATE).asInstanceOf[Date].before(a(K_DATE).asInstanceOf[Date]))

}