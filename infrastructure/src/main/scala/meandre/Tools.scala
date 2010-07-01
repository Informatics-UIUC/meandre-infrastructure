package meandre

import kernel.rdf._
import meandre.kernel.Implicits._
import java.util.Date
import java.net.URL
import com.mongodb.{BasicDBList, DBObject, BasicDBObject}

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
    l.sort((a, b) => a(K_MD_DATE).asInstanceOf[Date].before(b(K_MD_DATE).asInstanceOf[Date]))

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
      case (Nil,_,_)  => res.reverse
      case (_,Nil,_)  => res.reverse
      case (_,_,Nil)  => res.reverse
      case (as,bs,cs) => zip3$(as.tail,bs.tail,cs.tail,(as.head,bs.head,cs.head)::res)
    }
    zip3$(la,lb,lc,Nil)
  }


  /** Zips four lists into one.
   *
   * @param la The first list
   * @param lb The second list
   * @param lc The third list
   * @param ld The fourth list
   * @return The zipped list
   */
  def zip4 [A,B,C,D] ( la:List[A], lb:List[B], lc:List[C], ld:List[D] ) :List[(A,B,C,D)] = {
    def zip4$ ( la:List[A], lb:List[B], lc:List[C], ld:List[D], res:List[(A,B,C,D)] ) : List[(A,B,C,D)] = (la,lb,lc,ld) match {
      case (Nil,_,_,_)   => res.reverse
      case (_,Nil,_,_)   => res.reverse
      case (_,_,Nil,_)   => res.reverse
      case (_,_,_,Nil)   => res.reverse
      case (as,bs,cs,ds) => zip4$(as.tail,bs.tail,cs.tail,ds.tail,(as.head,bs.head,cs.head,ds.head)::res)
    }
    zip4$(la,lb,lc,ld,Nil)
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


  //----------------------------------------------------------------------

  /**Given a descriptor returns a BasicDBObject containing the data.
   *
   * @param desc The descriptor to transform
   * @return The JSON object containing the descriptor
   */
  def descriptor2BasicDBObject ( desc:Descriptor ) =  {

    def processCommonDescription(cd:CommonDescription) = {
      val res:BasicDBObject = """
             {
              "name":"%s",
              "description":"%s",
              "creator":"%s",
              "creationDate":"%s",
              "rights":"%s",
              "tags":"%s"
             }
          """.format(
            cd.name,
            cd.description.getOrElse(""),
            cd.creator.getOrElse(""),
            cd.creationDate,
            cd.rights.getOrElse(""),
            cd.tags.foldLeft("")((a,b)=>a+b+", ").replaceAll(", $","")
          )
      res
    }

    def processProperties(cp:Map[String,PropertyDescription]) = {
      val res = new BasicDBList
      cp.toList.map(
        kv => {
          val p = new BasicDBObject
          p.put("name", kv._2.key)
          p.put("value", kv._2.value)
          p.put("description", kv._2.description.getOrElse(""))
          kv._2.other.foreach(okv=>p.put(okv._1,okv._2))
          p
        }
      ).sort((a,b)=>a.get("name").toString<b.get("name").toString).foreach(res add _)
      res
    }

    def processPortList(pl:List[Port]) = {

      def processPort(port:Port) = {
        val res = new BasicDBObject
        res.put("uri",port.uri)
        res.put("name",port.name)
        res.put("description",port.description.getOrElse(""))
        res
      }

      val rl = new BasicDBList
      pl.foreach(p => rl.add(processPort(p)))
      rl
    }

    def processComponentInstanceList(cl:List[ComponentInstanceDescription]) ={

      def processComponentInstance(cid:ComponentInstanceDescription)  = {
        val res = new BasicDBObject
        res.put("uri",cid.uri)
        res.put("name",cid.name)
        res.put("description",cid.description.getOrElse(""))
        res.put("component uri",cid.componentUri)
        val lp = new BasicDBList
        cid.properties.toList.map(
          kv => {
            val p = new BasicDBObject
            p.put("name", kv._2.key)
            p.put("value", kv._2.value)
            kv._2.other.foreach(okv=>p.put(okv._1,okv._2))
            p
          }
        ).sort((a,b)=>a.get("name").toString<b.get("name").toString).foreach(lp add _)
        res.put("properties",lp)
        res
      }

      val rl = new BasicDBList
      cl.foreach(cid => rl.add(processComponentInstance(cid)))
      rl
    }

    def processConnectorsList ( lcd:List[ConnectorDescription] ) = {

      def processConnector ( con:ConnectorDescription ) ={
        val res = new BasicDBObject

        res.put("uri",con.uri)
        res.put("source instance",con.sourceInstance)
        res.put("source instance data port",con.sourceInstanceDataPort)
        res.put("target instance",con.targetInstance)
        res.put("target instance data port",con.targetInstanceDataPort)

        res
      }

      val rl = new BasicDBList
      lcd.foreach(con => rl.add(processConnector(con)) )
      rl
    }

    //
    // Generate the JSON representation
    //

    val res = new BasicDBObject
    res.put("uri",desc.uri)
    res.putAll(processCommonDescription(desc.description).asInstanceOf[DBObject])
    res.put("properties",processProperties(desc.properties).asInstanceOf[DBObject])

    desc match {
      case d:ComponentDescriptor =>
              res.put("runnable",d.runnable)
              res.put("format",d.format)
              res.put("firing policy",d.firingPolicy match {
                case f:FiringAll => "all"
                case f:FiringAny => "any"
              })
              res.put("resource location",d.resourceLocation)
              res.put("mode",d.mode match {
                case m:ComputeMode => "compute"
                case m:WebUIMode   => "web mode"
              })
              val lc = new BasicDBList
              d.context.foreach ( _ match {
                case URIContext(uri)     => lc add uri
                case EmbeddedContext(ec) => lc add ec
              })
              res.put("contexts",lc)
              res.put("inputs",processPortList(d.inputs))
              res.put("outputs",processPortList(d.outputs))

      case d:FlowDescriptor =>
              res.put("instances",processComponentInstanceList(d.instances))
              res.put("connectors",processConnectorsList(d.connectors))

    }

    res
  }

}