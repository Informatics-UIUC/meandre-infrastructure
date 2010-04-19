package meandre.state

import meandre.kernel.Implicits._
import meandre.kernel.Configuration
import meandre.kernel.rdf._
import com.mongodb.{DBObject, BasicDBList, BasicDBObject, Mongo}
import java.io.{ByteArrayInputStream, ObjectInputStream, ObjectOutputStream, ByteArrayOutputStream}
import com.hp.hpl.jena.rdf.model.{ModelFactory, Model}

/**
 * Provides access to the user repository.
 *
 * @author Xavier Llora
 * @date Feb 9, 2010 at 3:38:36 PM
 * 
 */
class Repository ( val cnf:Configuration, val userName:String ) {

  type RDFURI = String

  //---------------------------------------------------------------------------

  /** Implicit conversion of a descriptor to a Basic DB Object
   *
   * @param flow The flow descriptor to convert
   * @return The encapsulating BasicDBObject
   */
   protected implicit def descriptor2BasicDBObject ( desc:Descriptor ) : BasicDBObject = {
    // Serializing the descriptor
    val baosSer = new ByteArrayOutputStream
    new ObjectOutputStream(baosSer) writeObject desc
    val tagList:BasicDBList = desc.description.tags
    // Creating the tokens
    // TODO Text should be HTML unparsed
    val tkns = new BasicDBList
    for ( t <- (desc.description.name + " " +
                desc.description.creator.getOrElse("") + " " +
                desc.description.rights.getOrElse("") +  " " +
                desc.description.tags.foldLeft("")((a,t)=>a+" "+t+" ") +
                desc.description.description.getOrElse("")).split("""\s+""") ) tkns add t
    // Creating the BasicDBObject
    val bdbo = new BasicDBObject
    bdbo.put(K_ID,desc.uri)
    bdbo.put(K_NAME,desc.description.name)
    bdbo.put(K_DATE,desc.description.creationDate)
    bdbo.put(K_TAGS,tagList)
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

  //---------------------------------------------------------------------------

  /**Update the serialized descriptor.
   *
   * @param desc The descriptor to reserialize to the store
   */
  protected def updateSerializedDescriptor ( desc:Descriptor ) = {
    // Serializing the descriptor
    val baosSer = new ByteArrayOutputStream
    new ObjectOutputStream(baosSer) writeObject desc
    val bdbo = new BasicDBObject
    bdbo.put(K_BIN,baosSer.toByteArray)
    collection.update(wrapURI(desc),bdbo,true,false)
  }


  /**Update the rdf for the given descriptor.
   *
   * @param desc Updates the raw rdf for the given descriptor
   */
  def updateRDFFor ( uri:String, rdf:String ) : Option[String] = {
    // Serializing the RDF
    val model:Model = DescriptorsFactory readModelFromText rdf
    if ( model.size>0 ) {
      try {
        val bdboCnd = wrapURI(uri)

        val bdboUpdate = new BasicDBObject
        val baosRDF = new ByteArrayOutputStream()
        val baosTTL = new ByteArrayOutputStream()
        val baosNT  = new ByteArrayOutputStream()
        model.write(baosRDF,"RDF/XML-ABBREV")
        model.write(baosTTL,"TTL")
        model.write(baosNT, "N-TRIPLE")
        bdboUpdate.put(K_RDF,baosRDF.toByteArray)
        bdboUpdate.put(K_TTL,baosTTL.toByteArray)
        bdboUpdate.put(K_NT, baosNT.toByteArray)

        collection.update(bdboCnd,bdboUpdate,true,false)
        Some(uri)
      }
      catch {
        case _ => None
      }
    }
    else None
  }

  /** Given an arbitrary DBObject this method tries to regenerate the component
   *  description of the component.
   *
   *  @param dbObj The object to process
   *  @return The component descriptor if it could be regenerated
   */
  protected def regenerateComponentDescriptorFromDBObject ( dbObj:DBObject ) : Option[ComponentDescriptor] = {
    var res:Option[ComponentDescriptor] = None

    try {
      val ois = new ObjectInputStream(new ByteArrayInputStream((dbObj get K_BIN).asInstanceOf[Array[Byte]]))
      val dsc = ois.readObject.asInstanceOf[ComponentDescriptor]
      res = Some(dsc)
    }
    catch {
      case _ => try {
                  val mod = ModelFactory.createDefaultModel
                  val bais = new ByteArrayInputStream((dbObj get K_TTL).asInstanceOf[Array[Byte]])
                  res = DescriptorsFactory.buildComponentDescriptors(mod.read(bais,null,"TTL")) find {_.uri==dbObj.get(K_ID).toString}
                  updateSerializedDescriptor(res.get)
                }
                catch {
                  case _ => None
                }

    }
    res
  }

  /** Given an arbitrary DBObject this method tries to regenerate the flow
   *  description of the component.
   *
   *  @param dbObj The object to process
   *  @return The flow descriptor if it could be regenerated
   */
  protected def regenerateFlowDescriptorFromDBObject ( dbObj:DBObject ) : Option[FlowDescriptor] = {
    var res:Option[FlowDescriptor] = None

    try {
      val ois = new ObjectInputStream(new ByteArrayInputStream((dbObj get K_BIN).asInstanceOf[Array[Byte]]))
      val dsc = ois.readObject.asInstanceOf[FlowDescriptor]
      res = Some(dsc)
    }
    catch {
      case _ => try {
                  val mod = ModelFactory.createDefaultModel
                  val bais = new ByteArrayInputStream((dbObj get K_TTL).asInstanceOf[Array[Byte]])
                  res = DescriptorsFactory.buildFlowDescriptors(mod.read(bais,null,"TTL")) find {_.uri==dbObj.get(K_ID).toString}
                  updateSerializedDescriptor(res.get)
                }
                catch {
                  case _ => None
                }

    }
    res
  }



  //---------------------------------------------------------------------------


  /**Implicit conversion of a flow descriptor to a Basic DB Object
    *
   * @param flow The flow descriptor to convert
   * @return The encapsulating BasicDBObject
   */
  protected implicit def flowDescriptor2BasicDBObject ( flow:FlowDescriptor ) : BasicDBObject = {
    val bdbo = descriptor2BasicDBObject(flow)
    bdbo.put(K_TYPE,V_FLOW)
    bdbo
  }

  /** Implicit conversion of a component descriptor to a Basic DB Object
   *
   * @param comp The component descriptor to convert
   * @return The encapsulating BasicDBObject
   */
  protected implicit def componentDescriptor2BasicDBObject ( comp:ComponentDescriptor ) : BasicDBObject = {
    val bdbo = descriptor2BasicDBObject(comp)
    bdbo.put(K_TYPE,V_COMPONENT)
    bdbo.put(K_MODE,comp.mode match {
      case m:ComputeMode    => V_COMPUTE
      case m:FlowDescriptor => V_WEBUI
    })
    bdbo
  }

  /** Given descriptor returns a Basic DB Object with the _id field set
   *
   * @param uri The uri to wrap
   * @return The BasicDBObject with the _id field set
   */
  protected def wrapURI ( uri:String ) : BasicDBObject = {
    val bdbo = new BasicDBObject
    bdbo.put(K_ID,uri)
    bdbo
  }

  /** Given descriptor returns a Basic DB Object with the _id field set
   *
   * @param desc The descriptor to wrap
   * @return The BasicDBObject with the _id field set
   */
  protected def wrapURI ( desc:Descriptor ) : BasicDBObject = wrapURI(desc.uri)

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
  protected val collection = db getCollection USER_COLLECTION_NAME

  /** Test connectivity. Will thrown an exception if cannot connect */
  //collection.find.count

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

  /**Returns true is the respository is empty.
   *
   * @return True if the respository is empty, false otherwise
   */
  def isEmpty = size==0

  /**Returns the number of flows in a repository
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

  /**Remove the provided URI component/flow
   *
   * @param uri The URI to removeLocations
   */
  def remove (uri:RDFURI) = collection remove wrapURI(uri)

  /**Remove the provided descriptor
   *
   * @param uri The URI to removeLocations
   */
  def remove (desc:Descriptor) = collection remove wrapURI(desc)

  /** Add the given descriptors to the repository. If the descriptor exists it gets
   *  replaced by the new provided instance
   *
   * @param desc The list of flows to add
   */
  def add ( descs:List[Descriptor] ) : List[Option[String]] = descs.map (
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


  /** Returns all the component metadata stored in the repository that match
   *  a given query and sorted by the given criteria. Important: Remember that
   *  for efficiency purposes, you need to specify the sorting order in the
   *  reverse order you want them.
   *
   * @param query The query to run
   * @param sort The sort condition
   * @param skip The number of elements to skip
   * @param limit The maximum number of elements to returns
   * @return A list of component descriptors contained in the repository
   */
   def queryMetadata(query:String,sort:String,skip:Int,limit:Int) = {
    var res:List[Map[String,Any]] = Nil
    val cur = collection.find(query).sort(sort).skip(skip).limit(limit)
    while (cur.hasNext) {
      val bdbo = cur.next.asInstanceOf[BasicDBObject]
      val bdbl = (bdbo get K_TAGS).asInstanceOf[BasicDBList]
      val tagList = List[String]() ++ (0 until bdbl.size).toList.map(bdbl.get(_).toString)
      if ( bdbo.containsField(K_LOCATION) )
        res ::= Map (
            "uri"     -> bdbo.getString(K_ID),
            "name"    -> bdbo.getString(K_NAME),
            "date"    -> bdbo.get(K_DATE),
            "creator" -> bdbo.get(K_CREATOR),
            "rights"  -> bdbo.get(K_RIGHTS),
            "tags"    -> tagList,
            "desc"    -> bdbo.getString(K_DESC),
            "type"    -> bdbo.getString(K_TYPE),
            "location"-> bdbo.get(K_LOCATION).asInstanceOf[BasicDBObject].getString(K_LOCATION)
          )
      else
        res ::= Map (
            "uri"     -> bdbo.getString(K_ID),
            "name"    -> bdbo.getString(K_NAME),
            "date"    -> bdbo.get(K_DATE),
            "creator" -> bdbo.get(K_CREATOR),
            "rights"  -> bdbo.get(K_RIGHTS),
            "tags"    -> tagList,
            "desc"    -> bdbo.getString(K_DESC),
            "type"    -> bdbo.getString(K_TYPE)
          )
    }
    res
  }

  /** Returns all the component descriptors stored in the repository that match
   *  a given query and sorted by the given criteria. Important: Remember that
   *  for efficiency purposes, you need to specify the sorting order in the
   *  reverse order you want them.
   *
   * @param query The query to run
   * @param sort The sort condition
   * @param skip The number of elements to skip
   * @param limit The maximum number of elements to returns
   * @return A list of component descriptors contained in the repository
   */
  protected def queryComponents(query:String,sort:String,skip:Int,limit:Int) = {
    var res:List[ComponentDescriptor] = Nil
    val cur = collection.find(query).sort(sort).skip(skip).limit(limit)
    while (cur.hasNext)
      regenerateComponentDescriptorFromDBObject(cur.next) match {
        case Some(cd) => res ::= cd
        case None =>
      }
    res
  }

  /** Returns all the flow descriptors stored in the repository that match
   *  a given query and sorted by the given criteria. Important: Remember that
   *  for efficiency purposes, you need to specify the sorting order in the
   *  reverse order you want them.
   *
   * @param query The query to run
   * @param sort The sort condition
   * @param skip The number of elements to skip
   * @param limit The maximum number of elements to returns
   * @return A list of flow descriptors contained in the repository
   */
  protected def queryFlows(query:String,sort:String,skip:Int,limit:Int) = {
    var res:List[FlowDescriptor] = Nil
    val cur = collection.find(query).sort(sort).skip(skip).limit(limit)
    while (cur.hasNext)
      regenerateFlowDescriptorFromDBObject(cur.next) match {
        case Some(cd) => res ::= cd
        case None =>
      }
    res
  }

  /** Returns all the component descriptors stored in the repository.
   *
   * @return A list of flow descriptors contained in the repository
   */
  def components = queryComponents("{\"" + K_TYPE + "\": \"" + V_COMPONENT + "\"}","{}",0,Math.MAX_INT)

  /** Returns all the flow descriptors stored in the repository.
   *
   * @return A list of flow descriptors contained in the repository
   */
  def flows = queryFlows("{\"" + K_TYPE + "\": \"" + V_FLOW + "\"}","{}",0,Math.MAX_INT)

  /**Return the component descriptor for a give uri.
   *
   * @param uir The uri of the component to retrieve
   * @return The component descriptor if found, None otherwise
   */
  def componentFor(uri:RDFURI) = {
    val cl = queryComponents("{\"" + K_ID + "\": \"" + uri + "\",\"" + K_TYPE + "\": \"" + V_COMPONENT + "\"}","{}",0,Math.MAX_INT)
    if ( cl.size==1 ) Some(cl(0))
    else None
  }


  /**Return the flow descriptor for a give uri.
   *
   * @param uir The uri of the flow to retrieve
   * @return The flow descriptor if found, None otherwise
   */
  def flowFor(uri:RDFURI) = {
    val fl = queryFlows("{\"" + K_ID + "\": \"" + uri + "\",\"" + K_TYPE + "\": \"" + V_FLOW + "\"}","{}",0,Math.MAX_INT)
    if ( fl.size==1 ) Some(fl(0))
    else None
  }


  /**Return the descriptor for a give uri.
   *
   * @param uir The uri of the descriptor to retrieve
   * @return The descriptor if found, None otherwise
   */
  def descriptorFor(uri:RDFURI):Option[Descriptor] = {
    componentFor(uri) match {
      case None => flowFor(uri) match {
        case None => None
        case f => f
      }
      case c => c
    }
  }

  /** Returns all the component metadata stored in the repository.
   *
   * @return A list of flow metadata contained in the repository
   */
  def componentsMedatada = queryMetadata("{\"" + K_TYPE + "\": \"" + V_COMPONENT + "\"}","{}",0,Math.MAX_INT)

  /** Returns all the flow metadata stored in the repository.
   *
   * @return A list of flow metadata contained in the repository
   */
  def flowsMetadata = queryMetadata("{\"" + K_TYPE + "\": \"" + V_FLOW + "\"}","{}",0,Math.MAX_INT)

  /**Returns the metadata for the given uri.
   *
   * @param uri The uri to get metadata from
   */
  def metadataFor ( uri:RDFURI ) = {
    val l = queryMetadata("{\"" + K_ID + "\": \"" + uri + "\"}","{}",0,1)
    if (l.size==0) None
    else Some(l(0))
  }

  /** Return the tags that match the query.
   *
   * @param query The query
   * @return The set containing the tags
   */
  protected def queryTags ( query:String ) = {
    var set = Set[String]()
    val cursor = collection.find(query)
    while ( cursor.hasNext ) {
      val tags = cursor.next.get(K_TAGS).asInstanceOf[BasicDBList]
      (0 until tags.size).toList.foreach( i => set+=tags.get(i).toString )
    }
    set
  }

  /** Returns all the tags in the repository.
   *
   * @return The set of tags
   */
  def tags = queryTags("{}")

  /** Returns all the tags for flows in the repository.
   *
   * @return The set of tags
   */
  def flowsTags = queryTags("{\"" + K_TYPE + "\": \"" + V_FLOW + "\"}")

  /** Returns all the tags for components in the repository.
   *
   * @return The set of tags
   */
  def componentsTags = queryTags("{\"" + K_TYPE + "\": \"" + V_COMPONENT + "\"}")

  /** Return the tag cloud that match the query.
   *
   * @param query The query
   * @return The map containing the counts
   */
  protected def queryTagCloud ( query:String ) = {
    var map = scala.collection.mutable.Map[String,Int]()
    val cursor = collection.find(query)
    while ( cursor.hasNext ) {
      val tags = cursor.next.get(K_TAGS).asInstanceOf[BasicDBList]
      (0 until tags.size).toList.foreach( i => {
        val key = tags.get(i).toString
        if (map contains key) map(key) += 1 
        else                  map(key)  = 1
      })
    }
    map.foldLeft(Map[String,Int]())( (m,kv) => m+kv )
  }

  /** Return the tag cloud for the user repository
   *
   * @return A map with the tags and counts
   */
  def tagCloud = queryTagCloud("{}")


  /** Return the tag cloud  for the user flows in the repository
   *
   * @return A map with the tags and counts
   */
  def flowsTagCloud = queryTagCloud("{\"" + K_TYPE + "\": \"" + V_FLOW + "\"}")


  /** Return the tag cloud for the user components in the repository
   *
   * @return A map with the tags and counts
   */
  def componentsTagCloud = queryTagCloud("{\"" + K_TYPE + "\": \"" + V_COMPONENT + "\"}")

  /**Updates the metadata of a component.
   *
   * @param uri The uri to update
   * @param metadata A placeholder for extra metadata storage
   * @return The URI if succeded
   */
  def updateMetadata ( uri:RDFURI, metadata:String ) = {
    val cnd = wrapURI(uri)
    val update = new BasicDBObject
    update.put(K_METADATA,metadata)
    collection.update(cnd,update,true,false)
  }


  /**Get the metadata of a component.
   *
   * @param uri The uri to update
   * @param metadata A placeholder for extra metadata storage
   * @return The URI if succeded
   */
  def getMetadata ( uri:RDFURI ) = {
    val cur = collection find wrapURI(uri)
    if ( cur.hasNext ) Some(cur.next.get(K_METADATA).toString)
    else               None
  }

  /** Check if a given URI exist in the store.
   *
   * @param uri The URI to check
   * @return True if exist, false otherwise
   */
  def exist ( uri:RDFURI ) = 0 < collection.getCount(wrapURI(uri))
  
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