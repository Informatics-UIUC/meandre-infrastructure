package meandre.kernel.state

import meandre.kernel.Configuration
import meandre.Tools.zip3
import java.io.InputStream
import meandre.state.Repository
import meandre.kernel.Implicits._
import meandre.kernel.rdf._
import java.net.URL
import com.hp.hpl.jena.rdf.model.ModelFactory
import meandre.Tools.getInputStreamForURL
import com.mongodb.{BasicDBObject, Mongo}

/**A base case class for all addable elements to the store */
abstract sealed case class Element()

/**The case class that describes a location element */
case class LocationElement(url: String, desc:String) extends Element()

/**The case class that describes a bundled element */
case class BundledElement(desc:Descriptor, names: List[String], mimeTypes: List[String], contexts: List[InputStream]) extends Element

/**The case class that describes a partially bundled element where the context already exist on the store */
case class PartiallyBundledElement(desc:Descriptor, names: List[String], mimeTypes: List[String], contexts: List[String]) extends Element


//---------------------------------------------------------------------------

/**
 * Implements the main stateless wrapper to available stored data
 *
 * @author Xavier Llora
 * @date Feb 15, 2010 at 3:39:05 PM
 *
 */

class Store(override val cnf: Configuration, override val userName: String, val overwrite:Boolean)
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

  /**Given a certain context://localhost url it uses the location to rebuild
   * the original url to fetch the context
   *
   * @param location The location where the context url came from
   * @param url The context url to rewrite
   * @return The rewritten url
   */
  private def rewriteContextURL( location:String, url:String ) = {
    val splitLocation = location.split("/")
    val base = splitLocation.take(splitLocation.size-1).reduceLeft((a,b)=>a+"/"+b)
    url.replaceAll("context://localhost",base)
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
      case LocationElement(url,description) => try {
        val descs = DescriptorsFactory.buildDescriptors(url)
        // Get the descriptors and the unique set of urls to pull
        val (comps:Iterable[ComponentDescriptor],flows) = descs partition (_.isInstanceOf[ComponentDescriptor])
        var uris:List[String] = Nil
        comps foreach (c => c.context foreach (_ match {
          case URIContext(uri) => uris ::= uri
          case _ =>
        }))

        // Process and filter uris
        val processedURIs = uris map ( _ match {
          case s if s.startsWith("http") => Some(s)
          case s if s.startsWith("context://localhost") => Some(rewriteContextURL(url,s))
          case _ => None
        })
        uris = processedURIs.filter(_.isDefined).map(_.get)

        // Pull and store the remote contexts
        var lstCtxMap:List[Pair[String,String]] = Nil
        Set(uris:_*) foreach ( uri => {
          val fileName = uri.split("/").reverse.first
          // TODO Will need to check if MD5 exist to avoid pulling existing jars
          val (contentType,contentEncoding,is) = getInputStreamForURL(uri)
          contextsPool.update(fileName,contentType,is) match {
              case Left(t)  => throw t
              case Right(s:Pair[String,String]) => lstCtxMap ::= s._2 -> uri
          }
        })
        val cntxsMap = Map(lstCtxMap:_*)
        // Add all the flows blindly
        val addedFlowURIs = flows map (addToRepository(_))
        // Add all the components with proper context rewriting
        val rewrittenComps = comps map ( c => {
          val nc = c.context map ( _ match {
            case URIContext(uri) if  cntxsMap.contains(uri) =>  URIContext(cntxsMap(uri))
            case URIContext(uri) if !cntxsMap.contains(uri) =>  URIContext(uri)
            case s => s
          })
          c.context = nc
          c
        })
        val addedComponentURIs = rewrittenComps map (addToRepository(_))
        val addedURIs = descs map (c => Some(c.uri))

        // Update all the descriptors's metadata adding the original location
        (addedFlowURIs++addedComponentURIs) foreach ( uriOption => uriOption match {
          case Some(uri) =>  val doc = collection.findOne(wrapURI(uri))
                             val li = new BasicDBObject
                             li.put(K_LOCATION,url)
                             li.put(K_DESCRIPTION,description)
                             doc.put(K_LOCATION,li)
                             collection.update(wrapURI(uri),doc,false,false)
          case None => 
        })

        // Return the list of uris added
        Right(addedURIs)
      }
      catch {
        case t => Left(t)
      }

      //
      // It is a bundled descriptor usually the result of and upload
      //
      case be@BundledElement(desc, names, mimes, contexts) => try {
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
                  val  modName = name.replaceAll("""\s+""","-")
                  // TODO network can be improved by using Futures and then collecting values
                  val fileName = contextsPool.update(modName,mime,is)
                  cnames ::= (fileName match {
                    case Left(_) => ""
                    case Right(Pair(_,fn:String)) => fn
                  })
                  fileName
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
        case t => Left(t)
      }
    }
  }

  /**Returns the list of locations in the Store
   *
   * @return The list of locations
   */
  def locations = {
    val cur = collection.find("""{"location": {"$exists":true}}""")
    var res:Map[String,BasicDBObject] = Map()
    while ( cur.hasNext ) {
      val loc = cur.next.get(K_LOCATION).asInstanceOf[BasicDBObject]
      res += loc.getString(K_LOCATION) -> loc
    }
    res
  }


  /** Attempts to remove a locations and all the components and flows that came
   *  from it.
   *
   * @param urls The urls to removeLocations
   */
  def removeLocations ( urls:Seq[String] ) = {
    urls foreach (
      url => {
           val cnd:BasicDBObject = """{ "location.location": "%s" }""".format(url)
           collection.remove(cnd)
      }
    )
  }

  /** Attempts to remove all locations and all the components and flows that came
   *  from it.
   *
   */
  def removeAllLocations = {
    val cnd:BasicDBObject = """{ "location.location": { "$exists":true } }"""
    collection.remove(cnd)
  }

  /**Remove everything from the store
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
   */
  def apply (cnf: Configuration, user: String, overwrite:Boolean) = new Store(cnf,user,overwrite)

}