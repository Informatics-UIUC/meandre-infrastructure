package meandre.kernel

import com.mongodb.util.JSON
import com.hp.hpl.jena.rdf.model.{Resource, ModelFactory, Model}
import java.text.SimpleDateFormat
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype
import com.hp.hpl.jena.vocabulary._
import com.mongodb.{BasicDBList, BasicDBObject}
import collection.jcl.MutableIterator.Wrapper
import java.io.ByteArrayOutputStream
import rdf._
import meandre.kernel.rdf.{MeandreRepositoryVocabulary=>MRV}

/**
 * A collection of implicit conversion methods
 *
 * @author Xavier Llora
 * @date Jan 27, 2010 at 10:31:38 PM
 * 
 */
object Implicits {


  //----------------------------------------------------------------------

  
  val K_ID       = "_id"
  val K_TOKENS   = "_tokens"
  val K_TYPE     = "_type"
  val K_MODE     = "_mode"
  val K_BIN      = "_bin"
  val K_TTL      = "_ttl"
  val K_RDF      = "_rdf"
  val K_NT       = "_nt"
  val K_NAME     = "name"
  val K_DESC     = "desc"
  val K_CREATOR  = "creator"
  val K_DATE     = "ts"
  val K_RIGHTS   = "rights"
  val K_TAGS     = "tags"
  val K_METADATA = "metadata"
  val K_LOCATION = "location"

  //----------------------------------------------------------------------

  /**Given a Java iterator it wraps it into a Scala one.
   *
   * @param it The Java iterator
   * @return The Scala one
   */
  implicit def javaIteratorToScalaIterator[A](it : java.util.Iterator[A]) = new Wrapper(it)


  /** Attempts to parse a JSON String into s Basic DB object.
   *
   * @param s The JSON string to parse
   * @return The parsed Basic DB Object
   * @throws JSONParseException If failed to parse the string
   */
  implicit def string2BasicDBObject(s:String):BasicDBObject = JSON.parse(s).asInstanceOf[BasicDBObject]

  /**Converts a list of objects into a Basic DB List.
   *
   * @param l The list to convert
   * @return The converted Basic DB List
   */
  implicit def list2BasicDBList[A](l:List[A]) = {
    val res = new BasicDBList
    l.foreach(o => res.add(o.asInstanceOf[Object]))
    res
  }

  /**Converts an element to a list of one element.
   *
   * @param a The element to turn in to a
   * @return The list containing the provided element
   */
  implicit def anyToList [A] ( a:A ) : List[A] = List(a)

  /** Given a flow descriptor, it returns the minimal equivalent Jena model
   *  which describes it.
   *
   * @param flow The flow descriptor to convert
   * @return The converted model
   */
  implicit def flowDescriptor2Model(flow:FlowDescriptor):Model = {
    val model = ModelFactory.createDefaultModel

    // Setting the name spaces
    model.setNsPrefix("", MRV.NS)
    model.setNsPrefix("xsd",  XSD.getURI)
    model.setNsPrefix("rdf",  RDF.getURI)
    model.setNsPrefix("rdfs", RDFS.getURI)
    model.setNsPrefix("dc",   DC_11.getURI)

    val res = model.createResource(flow.uri)

    // Plain properties
    res.addProperty(MRV.name, model.createTypedLiteral(flow.description.name.asInstanceOf[Any]))
            .addProperty(DC_11.description, model.createTypedLiteral(flow.description.description.getOrElse("Unknown!"),XSDDatatype.XSDstring))
            .addProperty(DC_11.rights, model.createTypedLiteral(flow.description.rights.getOrElse("Unknown!"),XSDDatatype.XSDstring))
            .addProperty(DC_11.creator, model.createTypedLiteral(flow.description.creator.getOrElse("Unknown!"),XSDDatatype.XSDstring))
            .addProperty(DC_11.date, model.createTypedLiteral(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(flow.description.creationDate), XSDDatatype.XSDdateTime))
            .addProperty(RDF.`type`, MRV.flow_component)

    // Adding properties
    flow.properties.foreach ( kv => {
      val sKey = kv._1
      val propDesc = kv._2
      val sValue = propDesc.value
      val sDesc = propDesc.description.getOrElse("Unknown!")
      val prop = model.createResource(flow.uri+(if (flow.uri.endsWith("/")) "" else "/")+"property/"+sKey.replaceAll("\\s+","-"))
                        .addProperty(RDF.`type`,MRV.property)
                        .addProperty(MRV.key,model.createTypedLiteral(sKey,XSDDatatype.XSDstring))
                        .addProperty(MRV.value,model.createTypedLiteral(sValue,XSDDatatype.XSDstring))
                        .addProperty(DC_11.description,model.createTypedLiteral(sDesc,XSDDatatype.XSDstring))
      propDesc.other.foreach ( (kvop) => {
        val othersKey = kvop._1
        val othersVal = kvop._2
        var resOV:Option[Resource] = None
        try {
          resOV = Some(model.createResource(othersVal))
        }
        catch {
          case _ => // Not a resource will be treated as literal
        }
        resOV match {
          case Some(res) => prop.addProperty(model.createProperty(othersKey),res)
          case None      => prop.addProperty(model.createProperty(othersKey),model.createTypedLiteral(othersVal,XSDDatatype.XSDstring))
        }
        res.addProperty(MRV.property_set,prop)
      })
    })

    // Adding tags
    flow.description.tags.foreach(
      tag => res.addProperty(MRV.tag, model.createTypedLiteral(tag,XSDDatatype.XSDstring))
    )

    // Adding connectors
    if (flow.connectors.size > 0) {
      val resCons = model.createResource(flow.uri+(if (flow.uri.endsWith("/")) "" else "/")+"connector/set")
      res.addProperty(MRV.connectors, resCons)
      flow.connectors.foreach ( cd =>  {
          val sConID = cd.uri
          val sSource = cd.sourceInstance
          val sSourceDP = cd.sourceInstanceDataPort
          val sTarget = cd.targetInstance;
          val sTargetDP = cd.targetInstanceDataPort
          resCons.addProperty(RDF.`type`,MRV.connector_set)
                 .addProperty(MRV.data_connector,
                      model.createResource(sConID).addProperty(MRV.connector_instance_source, model.createResource(sSource))
                                                  .addProperty(MRV.connector_instance_data_port_source, model.createResource(sSourceDP))
                                                  .addProperty(MRV.connector_instance_target, model.createResource(sTarget))
                                                  .addProperty(MRV.connector_instance_data_port_target, model.createResource(sTargetDP))
                                                  .addProperty(RDF.`type`, MRV.data_connector_configuration)
                 )
          }
      )
    }

    // Adding instances
    if ( flow.instances.size>0 ) {
      val resCons = model.createResource(flow.uri+(if (flow.uri.endsWith("/")) "" else "/")+"components/set")
      res.addProperty(MRV.components_instances,resCons)
      flow.instances.foreach ( ecid => {
          val sConID = ecid.uri
          val sComp = ecid.componentUri
          val sName = ecid.name
          val sDesc = ecid.description.getOrElse("Unknown!")
          val resIns = model.createResource(sConID).addProperty(MRV.instance_resource, model.createResource(sComp))
                                                   .addProperty(MRV.instance_name, model.createTypedLiteral(sName,XSDDatatype.XSDstring))
                                                   .addProperty(DC_11.description, model.createTypedLiteral(sDesc,XSDDatatype.XSDstring))
                                                   .addProperty(RDF.`type`, MRV.instance_configuration)
          resCons.addProperty(RDF.`type`, MRV.instance_set)
                 .addProperty(MRV.executable_component_instance,resIns)

          // Adding properties if any
          ecid.properties.foreach ( (kv) => {
             resIns.addProperty(MRV.property_set,
                                model.createResource(sConID+(if (sConID.endsWith("/")) "" else "/")+"property/"+kv._1).addProperty(MRV.key, model.createTypedLiteral(kv._1,XSDDatatype.XSDstring))
                                                                               .addProperty(MRV.value, model.createTypedLiteral(kv._2,XSDDatatype.XSDstring))
                                                                               .addProperty(RDF.`type`, MRV.property)
             )
          })
      })
    }

    model
  }


  /** Given a component descriptor, it returns the minimal equivalent Jena model
   *  which describes it.
   *
   * @param comp The component descriptor to convert
   * @return The converted model
   */
  implicit def componentDescriptor2Model(comp:ComponentDescriptor):Model = {
    val model = ModelFactory.createDefaultModel

    // Setting the name spaces
    model.setNsPrefix("", MRV.NS)
    model.setNsPrefix("xsd",  XSD.getURI)
    model.setNsPrefix("rdf",  RDF.getURI)
    model.setNsPrefix("rdfs", RDFS.getURI)
    model.setNsPrefix("dc",   DC_11.getURI)

    val res = model.createResource(comp.uri)

    // Plain properties
    res.addProperty(MRV.name, model.createTypedLiteral(comp.description.name.asInstanceOf[Any]))
            .addProperty(DC_11.description, model.createTypedLiteral(comp.description.description.getOrElse("Unknown!"),XSDDatatype.XSDstring))
            .addProperty(DC_11.rights, model.createTypedLiteral(comp.description.rights.getOrElse("Unknown!"),XSDDatatype.XSDstring))
            .addProperty(DC_11.creator, model.createTypedLiteral(comp.description.creator.getOrElse("Unknown!"),XSDDatatype.XSDstring))
            .addProperty(DC_11.date, model.createTypedLiteral(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(comp.description.creationDate), XSDDatatype.XSDdateTime))
            .addProperty(RDF.`type`, MRV.executable_component)
            
            .addProperty(DC_11.format,model.createTypedLiteral(comp.format,XSDDatatype.XSDstring))
            .addProperty(MRV.runnable,model.createTypedLiteral(comp.runnable,XSDDatatype.XSDstring))
            .addProperty(MRV.firing_policy,model.createTypedLiteral(comp.firingPolicy match {
                    case s:FiringAll => "all"
                    case s:FiringAny => "any"
                    case _ => "all"
                  },XSDDatatype.XSDstring))
            .addProperty(MRV.resource_location,model.createResource(comp.resourceLocation))


    // Adding properties
    comp.properties.foreach ( kv => {
      val sKey = kv._1
      val propDesc = kv._2
      val sValue = propDesc.value
      val sDesc = propDesc.description.getOrElse("Unknown!")
      val prop = model.createResource(comp.uri+(if (comp.uri.endsWith("/")) "" else "/")+"property/"+sKey.replaceAll("\\s+","-"))
                        .addProperty(RDF.`type`,MRV.property)
                        .addProperty(MRV.key,model.createTypedLiteral(sKey,XSDDatatype.XSDstring))
                        .addProperty(MRV.value,model.createTypedLiteral(sValue,XSDDatatype.XSDstring))
                        .addProperty(DC_11.description,model.createTypedLiteral(sDesc,XSDDatatype.XSDstring))


      propDesc.other.foreach ( (kvop) => {
        val othersKey = kvop._1
        val othersVal = kvop._2
        var resOV:Option[Resource] = None
        try {
          resOV = Some(model.createResource(othersVal))
        }
        catch {
          case _ => // Not a resource will be treated as literal
        }
        resOV match {
          case Some(res) => prop.addProperty(model.createProperty(othersKey),res)
          case None      => prop.addProperty(model.createProperty(othersKey),model.createTypedLiteral(othersVal,XSDDatatype.XSDstring))
        }
        res.addProperty(MRV.property_set,prop)
      })
    })

    // Adding tags
    comp.description.tags.foreach(
      tag => res.addProperty(MRV.tag, model.createTypedLiteral(tag,XSDDatatype.XSDstring))
    )

    // Adding execution contexts
    comp.context.foreach(context => context match {
      case URIContext(uri) => res.addProperty(MRV.execution_context, model.createResource(uri))
      case EmbeddedContext(payload) => res.addProperty(MRV.execution_context, model.createTypedLiteral(payload, XSDDatatype.XSDstring))
    })

    // Adding inputs
    comp.inputs.foreach ( port => {
      val sID = port.uri
      val sName = port.name
      val resPort = model.createResource(sID)
      val sDesc = port.description.getOrElse("Unknown!")
      res.addProperty(MRV.input_data_port,
          model.createResource(sID)
               .addProperty(RDF.`type`,MRV.data_port)
               .addProperty(DC_11.identifier,model.createTypedLiteral(sID,XSDDatatype.XSDstring))
               .addProperty(MRV.name,model.createTypedLiteral(sName,XSDDatatype.XSDstring))
               .addProperty(DC_11.description,model.createTypedLiteral(sDesc,XSDDatatype.XSDstring)))
    })


    // Adding outputs
    comp.outputs.foreach ( port => {
      val sID = port.uri
      val sName = port.name
      val resPort = model.createResource(sID)
      val sDesc = port.description.getOrElse("Unknown!")
      res.addProperty(MRV.output_data_port,
          model.createResource(sID)
               .addProperty(RDF.`type`,MRV.data_port)
               .addProperty(DC_11.identifier,model.createTypedLiteral(sID,XSDDatatype.XSDstring))
               .addProperty(MRV.name,model.createTypedLiteral(sName,XSDDatatype.XSDstring))
               .addProperty(DC_11.description,model.createTypedLiteral(sDesc,XSDDatatype.XSDstring)))
    })

    // Adding the component mode
    res.addProperty(MRV.mode, comp.mode match {
      case m:ComputeMode => MRV.mode_compute
      case m:WebUIMode => MRV.mode_webui
      case _ => MRV.mode_compute
    });

    model

  }

  /**Given a descriptor it returns its RDF/XML serialized form.
   *
   * @param desc The descriptor
   * @return The RDF/XML
   */
  implicit def descriptor2String ( desc:Descriptor ) : String = {
    val model:Model = desc match {
      case f:FlowDescriptor => flowDescriptor2Model(f)
      case c:ComponentDescriptor => componentDescriptor2Model(c)
    }
    val baos = new ByteArrayOutputStream
    model.write(baos,null,"RDF/XML")
    baos.toString
  }

}