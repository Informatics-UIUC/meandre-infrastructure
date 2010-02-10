package meandre

import com.mongodb.BasicDBObject
import com.mongodb.util.JSON
import com.hp.hpl.jena.rdf.model.{Resource, ModelFactory, Model}
import java.text.SimpleDateFormat
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype
import com.hp.hpl.jena.vocabulary._
import kernel.rdf.{Descriptor, MeandreRepositoryVocabulary => MRV, ConnectorDescription, FlowDescriptor}

/**
 * A collection of implicit conversion methods
 *
 * @author Xavier Llora
 * @date Jan 27, 2010 at 10:31:38 PM
 * 
 */
object Implicits {

  /** Attempts to parse a JSON String into s Basic DB object.
   *
   * @param s The JSON string to parse
   * @return The parsed Basic DB Object
   * @throws JSONParseException If failed to parse the string
   */
  implicit def String2BasicDBObject(s:String):BasicDBObject = JSON.parse(s).asInstanceOf[BasicDBObject]

  /**Converts a simple descriptor into a list of descriptors with the descriptor in it.
   *
   * @param desc The descriptor to wrap into a list
   * @return The list of one element containing the provided descriptor
   */
  implicit def DescriptorToList(desc:Descriptor):List[Descriptor] = List(desc)

  /** Given a flow descriptor, it returns the minimal equivalent Jena model
   *  which describes it.
   *
   * @param flow The flow descriptor to convert
   * @return The converted model
   */
  implicit def FlowDescriptor2Model(flow:FlowDescriptor):Model = {
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
      val prop = model.createResource(flow.uri+"/property/"+sKey.replaceAll("\\s+","-"))
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
          case _ =>
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
      val resCons = model.createResource(flow.uri + "connector/set")
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
      val resCons = model.createResource(flow.uri+"components/set")
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
                                model.createResource(sConID+"/property/"+kv._1).addProperty(MRV.key, model.createTypedLiteral(kv._1,XSDDatatype.XSDstring))
                                                                               .addProperty(MRV.value, model.createTypedLiteral(kv._2,XSDDatatype.XSDstring))
                                                                               .addProperty(RDF.`type`, MRV.property)
             )
          })
      })
    }

    model
  }

}