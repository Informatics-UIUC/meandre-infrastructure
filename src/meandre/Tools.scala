package meandre

import kernel.rdf.Descriptor
import meandre.kernel.Implicits._
import java.util.Date
import java.net.URL

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

  //----------------------------------------------------------------------

  /** Performs a a safe operation against a piece of code that can throw
   *  exceptions.
   *
   * @param op The operation to perform
   * @return The either construct containing the result
   */
  def safeOp[A](op: => A): Either[Throwable, A] = {
    try {
      Right(op)
    }
    catch {
      case t => Left(t)
    }
  }


  //----------------------------------------------------------------------

  /** Zips three lists into one.
   *
   * @param la The first list
   * @param lb The second list
   * @param lc The third list
   * @return The zipped list
   */
  def zip3 [A,B,C] ( la:List[A], lb:List[B], lc:List[C] ) :List[(A,B,C)] = {
    def zip3$ ( la:List[A], lb:List[B], lc:List[C], res:List[(A,B,C)] ) : List[(A,B,C)] = (la,lb,lc) match {
      case (Nil,_,_) => res.reverse
      case (_,Nil,_) => res.reverse
      case (_,_,Nil) => res.reverse
      case (as,bs,cs)   => zip3$(as.tail,bs.tail,cs.tail,(as.head,bs.head,cs.head)::res)
    }
    zip3$(la,lb,lc,Nil)
  }


  /** Zips four lists into one.
   *
   * @param la The first list
   * @param lb The second list
   * @param lc The third list
   * @return The zipped list
   */
  def zip4 [A,B,C] ( la:List[A], lb:List[B], lc:List[C] ) :List[(A,B,C)] = {
    def zip4$ ( la:List[A], lb:List[B], lc:List[C], res:List[(A,B,C)] ) : List[(A,B,C)] = (la,lb,lc) match {
      case (Nil,_,_) => res.reverse
      case (_,Nil,_) => res.reverse
      case (_,_,Nil) => res.reverse
      case (as,bs,cs)   => zip4$(as.tail,bs.tail,cs.tail,(as.head,bs.head,cs.head)::res)
    }
    zip4$(la,lb,lc,Nil)
  }


  //----------------------------------------------------------------------

  /**Opens an input stream for the given URL. The connection is opened
   *  with timeouts for connection and reading.
   *
   * @param url The url to open
   * @return The opened input stream
   */
  def getInputStreamForURL(url: String) = {
    val httpConnection = (new URL(url)).openConnection
    httpConnection.setConnectTimeout(10000)
    httpConnection.setReadTimeout(10000)
    (httpConnection.getContentType,httpConnection.getContentEncoding,httpConnection.getInputStream)
  }


}