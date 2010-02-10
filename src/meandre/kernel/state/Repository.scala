package meandre.state

import meandre.Implicits._
import meandre.kernel.Configuration
import java.io.{ObjectOutputStream, ByteArrayOutputStream}
import com.mongodb.{BasicDBList, BasicDBObject, Mongo}
import com.hp.hpl.jena.rdf.model.Model
import meandre.kernel.rdf._

/**
 * Provides access to the user repository.
 *
 * @author Xavier Llora
 * @date Feb 9, 2010 at 3:38:36 PM
 * 
 */
class Repository ( val cnf:Configuration, val userName:String ) {

  private val K_ID      = "_id"
  private val K_TOKENS  = "_tokens"
  private val K_TYPE    = "_type"
  private val K_MODE    = "_mode"
  private val K_BIN     = "_bin"
  private val K_TTL     = "_ttl"
  private val K_RDF     = "_rdf"
  private val K_NT      = "_nt"
  private val K_NAME    = "name"
  private val K_DESC    = "desc"
  private val K_CREATOR = "creator"
  private val K_DATE    = "ts"
  private val K_RIGHTS  = "rights"
  private val K_TAGS    = "tags"

  private val V_COMPONENT = "component"
  private val V_FLOW      = "flow"
  private val V_COMPUTE   = "compute"
  private val V_WEBUI     = "webui"

  //---------------------------------------------------------------------------

  /** Implicit conversion of a descriptor to a Basic DB Object
   *
   * @param flow The flow descriptor to convert
   * @return The encapsulating BasicDBObject
   */
  implicit private def descriptor2BasicDBObject ( desc:Descriptor ) : BasicDBObject = {
    // Serializing the descriptor
    val baosSer = new ByteArrayOutputStream
    new ObjectOutputStream(baosSer) writeObject desc
    // Creating the tokens
    // TODO Text should be HTML unparsed
    val tkns = new BasicDBList
    for ( t <- (desc.description.name + " " +
                desc.description.creator.getOrElse("") + " " +
                desc.description.rights.getOrElse("") +  " " +
                desc.description.description.getOrElse("")).split("""\s+""") ) tkns add t
    // Creating the BasicDBObject
    val bdbo = new BasicDBObject
    bdbo.put(K_ID,desc.uri)
    bdbo.put(K_NAME,desc.description.name)
    bdbo.put(K_DATE,desc.description.creationDate)
    bdbo.put(K_CREATOR,desc.description.creator.getOrElse("Unknown!"))
    bdbo.put(K_RIGHTS,desc.description.rights.getOrElse("Unknown!"))
    bdbo.put(K_DESC,desc.description.description.getOrElse("Unknown!"))
    bdbo.put(K_BIN,baosSer.toByteArray)
    bdbo.put(K_TOKENS,tkns)
    // Serializing the RDF
    val model:Model = desc match {
      case d:FlowDescriptor => d
      case c:ComponentDescriptor => c
    }
    val baosRDF = new ByteArrayOutputStream()
    val baosTTL = new ByteArrayOutputStream()
    val baosNT  = new ByteArrayOutputStream()
    model.write(baosRDF,"RDF/XML-ABBREV")
    model.write(baosTTL,"TTL")
    model.write(baosNT, "N-TRIPLE")
    bdbo.put(K_RDF,baosRDF.toByteArray)
    bdbo.put(K_TTL,baosTTL.toByteArray)
    bdbo.put(K_NT, baosNT.toByteArray)
    // Return the object
    bdbo
  }

  /** Implicit conversion of a flow descriptor to a Basic DB Object
   *
   * @param flow The flow descriptor to convert
   * @return The encapsulating BasicDBObject
   */
  private implicit def flowDescriptor2BasicDBObject ( flow:FlowDescriptor ) : BasicDBObject = {
    val bdbo = descriptor2BasicDBObject(flow)
    bdbo.put(K_TYPE,V_FLOW)
    bdbo
  }

  /** Implicit conversion of a component descriptor to a Basic DB Object
   *
   * @param comp The component descriptor to convert
   * @return The encapsulating BasicDBObject
   */
  private implicit def componentDescriptor2BasicDBObject ( comp:ComponentDescriptor ) : BasicDBObject = {
    val bdbo = descriptor2BasicDBObject(comp)
    bdbo.put(K_TYPE,V_COMPONENT)
    bdbo.put(K_MODE,comp.mode match {
      case m:ComputeMode    => V_COMPUTE
      case m:FlowDescriptor => V_WEBUI
    })
    bdbo
  }

  /**Given descriptor returns a Basic DB Object with the _id field set
   *
   * @param desc The descriptor to wrap
   * @return The BasicDBObject with the _id field set
   */
  private def wrapURI ( desc:Descriptor ) = {
    val bdbo = new BasicDBObject
    bdbo.put(K_ID,desc.uri)
    bdbo
  }

  //---------------------------------------------------------------------------

  /** The name of the repository collection for the given user */
  private val USER_COLLECTION_NAME = cnf.MEANDRE_USER_COLLECTION_PREFIX+userName.replaceAll("""\s""","_")

  /** The mongo db connection object */
  private val mongo = new Mongo(cnf.host,cnf.port)

  /** The Meandre database */
  private val db = mongo getDB cnf.MEANDRE_DB_NAME
  cnf.auth match {
    case Some((user,password)) => db.authenticate(user,password.toCharArray)
    case None => 
  }

  /** The user collection */
  private val collection = db getCollection USER_COLLECTION_NAME

  /** Test connectivity. Will thrown an exception if cannot connect */
  collection.find.count

  /** Ensure index creation */
  collection.ensureIndex("{\""+K_TYPE+"\": 1}")
  collection.ensureIndex("{\""+K_MODE+"\": 1}")
  collection.ensureIndex("{\""+K_TOKENS+"\": 1}")

  //---------------------------------------------------------------------------

  /** Returns the number of components and flows in a repository
   *
   * @return The number of components and flows in the user repository
   */
  def size = collection.find.count

  /** Returns the number of flows in a repository
   *
   * @return The number of flows in the user repository
   */
  def sizeFlows = collection.find("{\""+K_TYPE+"\": \""+V_FLOW+"\"}").count

  /** Returns the number of components in a repository
   *
   * @return The number of components in the user repository
   */
  def sizeComponents = collection.find("{\""+K_TYPE+"\": \""+V_COMPONENT+"\"}").count

  /** Remove everything from the repository
   *
   */
  def removeAll = collection remove """{}"""

  /** Remove all the flows from the repository
   *
   */
  def removeAllComponents = collection remove "{\"" + K_TYPE + "\": \"" + V_COMPONENT + "\"}"


  /** Remove all the flows from the repository
   *
   */
  def removeAllFlows = collection remove "{\"" + K_TYPE + "\": \"" + V_FLOW + "\"}"

  /** Add the given descriptors to the repository. If the descriptor exists it gets
   *  replaced by the new provided instance
   *
   * @param flows The list of flows to add
   */
  def add ( flows:List[Descriptor] ) : List[Option[String]] = flows.map (
      _ match {
        case flow:FlowDescriptor => collection.update(wrapURI(flow),flow,true,false)
                                    Some(flow.uri)
        case component:ComponentDescriptor => collection.update(wrapURI(component),component,true,false)
                                                      Some(component.uri)
        case _ =>  None
      }
    )

  /** Add the given flows to the repository. If the flow exists it gets
   *  replaced by the new provided instance
   *
   * @param flows The list of flows to add
   */
  def addFlows ( flows:List[Descriptor] ) : List[Option[String]] = flows.map (
      _ match {
        case flow:FlowDescriptor => collection.update(wrapURI(flow),flow,true,false)
                                    Some(flow.uri)
        case _ =>  None
      }
    )

  /** Add the given components to the repository. If the component exist it gets
   *  replaced by the new provided instance
   *
   * @param components The list of flows to add
   */
  def addComponents ( components:List[Descriptor] ) : List[Option[String]] = components.map (
      _ match {
        case component:ComponentDescriptor => collection.update(wrapURI(component),component,true,false)
                                              Some(component.uri)
        case _ =>  None
      }
    )

}

/**The companion object for the Repository class.
 *
 */
object Repository {

  /**Create a new repository for the given configuration and user name.
   *
   * @param cnf The configuration object to use
   * @param userName The user name
   * @return  The repository object created
   */
  def apply ( cnf:Configuration, userName:String ) = new Repository(cnf,userName)

  /** Extract the repository configuration values
   *
   * @param repo the repository object
   * @return The mongo object and the user name
   *
   */
  def unapply ( repo:Repository ) : Option[(Configuration,String)] = Some((repo.cnf,repo.userName))
  
}