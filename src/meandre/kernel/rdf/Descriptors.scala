package meandre.kernel.rdf

import com.hp.hpl.jena.rdf.model.{Property=>JProperty}
import com.hp.hpl.jena.rdf.model._
import com.hp.hpl.jena.vocabulary._
import com.hp.hpl.jena.vocabulary.RDF.`type`
import java.text.SimpleDateFormat
import java.util.Date
import java.io.StringReader

/** The base class of the context hierarchy 
 * 
 * @author Xavier Llora
 */
abstract class Context ()                                  
/** A representation of a context indicating a resource
 * 
 * @author Xavier Llora
 */
case class URIContext ( uri: String ) extends Context
/** A container for an embeded context 
 * 
 * @author Xavier Llora
 */
case class EmbeddedContext ( payload: String ) extends Context

/** Contains the basic property information 
 * 
 * @author Xavier Llora
 */
case class Property ( val key: String, val value: String, val other: Map[String,String] )
/** A property that also allows to include a the property description 
 * 
 * @author Xavier Llora
 */
case class PropertyDescription ( 
		override val key: String, 
		override val value: String, 
		val description:Option[String],
		override val other: Map[String,String]
) extends Property(key,value,other)

/** A component port definition. Instances of this class are used for describing 
 *  both input and output ports for components.
 * 
 * @author Xavier Llora
 */
case class Port (
		uri: String,
		name: String,
		description: Option[String]
)

/** A container for all the shared description for meandre components (executable and  
 *  floww components). 
 * 
 * @author Xavier Llora
 */
case class CommonDescription (
		name: String,
		description: Option[String],
		creator: Option[String],
		creationDate: Date,
		rights: Option[String],
		tags: List[String]
)

/** The base class for the possible firing polices 
 * 
 * @author Xavier Llora
 */
abstract class FiringPolicy()
/** The firing policy that indicates that data on all ports should be present 
 * 
 * @author Xavier Llora
 */
case class FiringAll() extends FiringPolicy
/** The firing policy that indicates that data on at least one port should be present 
 * 
 * @author Xavier Llora
 */
case class FiringAny() extends FiringPolicy

/** A simple representation of the connection between an input and an output port 
 * 
 * @author Xavier Llora
 */
case class ConnectorDescription (
    uri:String,
		sourceInstanceDataPort: String,
		targetInstanceDataPort: String,
		sourceInstance: String,
		targetInstance: String
) 

/** Instances of this class describe the instances that define a flow. 
 * 
 * @author Xavier Llora
 */
case class ComponentInstanceDescription (
		uri: String,
		name: String,
		description: Option[String],
		componentUri: String,
		properties: Map[String,Property]
)
/**The basic mode of execution of a component
 *
 * @author Xavier Llora
 */
case class ModeDescription()
/**The mode of execution of a component is a compute one
 *
 * @author Xavier Llora
 */
case class ComputeMode() extends ModeDescription()
/**The basic mode of execution of a component is a WebUI one
 *
 * @author Xavier Llora
 */
case class WebUIMode() extends ModeDescription()
/** The base class for all the possible descriptors (including components and flows).
 *  This class contains the basic information that defines a descriptor, the uri, 
 *  the common description, and the properties.
 * 
 * @author Xavier Llora
 */
abstract sealed case class Descriptor(
		uri: String,
		description: CommonDescription,
		properties: Map[String,Property]
)
/** The component descriptor contains all the related information for a component.
 * 
 * @author Xavier Llora
 */
case class ComponentDescriptor (
		override val uri: String,
		override val description: CommonDescription,
		override val properties: Map[String,PropertyDescription],
		runnable: String,
		format: String,
		firingPolicy: FiringPolicy,
		resourceLocation: String,
    mode: ModeDescription,
		context: List[Context],
		inputs: List[Port],
		outputs: List[Port]
)
extends Descriptor(uri,description,properties)
/** The component descriptor contains all the related information for a component.
 * 
 * @author Xavier Llora
 */
case class FlowDescriptor (
		override val uri: String,
		override val description: CommonDescription,
		override val properties: Map[String,PropertyDescription],
		instances: List[ComponentInstanceDescription],
		connectors: List[ConnectorDescription]
)
extends Descriptor(uri,description,properties)

/** This singlecton object acts as a simple factory to turn
 *  serialized RDF models into usable Scala objects.
 * 
 * @author Xavier Llora
 */
object DescriptorsFactory  {
 
	/** Attempts to read a RDF model from a URL
     *
     * @param url The url to parse
     * @return The read model as a Jena object
     */
	def readModelFromURL ( url:String ) = {
		val model = ModelFactory.createDefaultModel
		try {
			model.read(url.toString)
		} catch  {
			case _ => try { model.read(url.toString,"TTL") } 
                  catch {
                  		case _ => try { model.read(url.toString,"N-TRIPLE") } 
                  				  finally { model }
                  }
                  finally { model }
		}
		finally { model }
	}


  /**Attempts to read a RDF model from text
   *
   * @param text The text to parse
   * @return The read model as a Jena object
   */
  def readModelFromText(text: String) = {
    val model = ModelFactory.createDefaultModel
    try {
      model.read(new StringReader(text),"RDF/XML")
    } catch {
      case _ => try {model.read(new StringReader(text), "TTL")}
      catch {
        case _ => try {model.read(new StringReader(text), "N-TRIPLE")}
        finally {model}
      }
      finally {model}
    }
    finally {model}
  }


  /** Safely gets the given property for the passed resource in the given model.
	 * 
	 * @param uri The resource to poke
     * @param prop The property to get
     * @param model The model to use
     * @return An option containing the value or None
	 */
	protected def safeGet ( uri: Resource, prop: JProperty, model: Model ) : Option[String] = {
		val objs = model.listObjectsOfProperty(uri,prop)
		if ( !objs.hasNext ) None else objs.nextNode match {
		  case s:Resource => Some(s.toString)
		  case s:Literal => Some(s.getString)
		}
	}
 
	/** Returns all the objects linked to a resource given a certain property in a model.
	 *
	 * @param uri The resource to poke
     * @param prop The property to get
     * @param model The model to use
     * @return A list containing all the RDFNode object for the given property and subject
     */
	protected def getMultivalueProperty ( uri: Resource, prop: JProperty, model: Model ) : List[RDFNode] = {
		var res: List[RDFNode] = Nil
		val objs = model.listObjectsOfProperty(uri,prop)
		while ( objs.hasNext ) res ::= objs.nextNode
		res
	}
 
	/** Returns all the staments that have the given subject.
     * 
     * @param uri The uri of the targeted subject
     * @return The list of statement for the given subject
     */
	protected def getStatementWithSubject ( uri: Resource ) : List[Statement] = {
		var res: List[Statement] = Nil
		val objs = uri.listProperties()
		while ( objs.hasNext ) res ::= objs.nextStatement
		res
	}
 
	/** Converts a list of RDFNodes to strings, regardless if they are resources or literals
	 * 
	 * @param lst The list of RDFNodes to convert
	 * @return A list containing the stringfied version of the RDFNodes 
	 */
	protected def rdfNodesToString ( lst: List[RDFNode] ) : List[String] = {
		lst.map((s) =>  s match {
		  case s:Resource => s.toString
		  case s:Literal => s.getString
		})
	}

	/** Get a property value throwing an execption if not available.
	 * 	
	 * @param uri The resource to poke
     * @param prop The property to get
     * @param model The model to use
     * @return The stringfied value of the property object
     */
	protected def mustGet ( uri: Resource, prop: JProperty, model: Model ) : String = {
		safeGet(uri,prop,model) match {
		  case None => throw new Exception("The property "+prop+" is required")
		  case Some(s) => s 
		}
	}
 
	/** The main data formater */
	protected val dateFormater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
 
	/** Converts a String into a Data object */
	protected def stringToDate ( string:String ) : Date =  dateFormater.parse(string)	

	/** Gets the common description for a given URI in a model.
	 *
	 * @param uri The resource to poke
     * @param model The model to use
     * @return The common description object
	 */
	protected def getCommonDescription ( uri: Resource, model: Model ) : CommonDescription = {
		new CommonDescription(
				mustGet(uri,MeandreRepositoryVocabulary.name,model),
				safeGet(uri,DC_11.description,model),
				safeGet(uri,DC_11.creator,model),
				stringToDate(mustGet(uri,DC_11.date,model)),
				safeGet(uri,DC_11.rights,model),
				rdfNodesToString(getMultivalueProperty(uri,MeandreRepositoryVocabulary.tag,model))
		)
	}

	
	/** Gets the common description for a given URI in a model.
	 *
	 * @param uri The resource to poke
     * @param model The model to use
     * @return The properties for the given URI in the given model
	 */
	protected def getProperties ( uri:Resource, model:Model )  = {
	  
		def getPairs ( node:Resource ) = 
		  for (stm<-getStatementWithSubject(node)  ) 
			  yield if (stm.getObject.isLiteral) stm.getPredicate.toString ->  stm.getLiteral.getString
                    else stm.getPredicate.toString -> stm.getObject.toString
	  
        val lm = for { node <- getMultivalueProperty ( uri, MeandreRepositoryVocabulary.property_set, model ) if node.isResource }
	  	  		  yield node match {
	  	  		    	case node:Resource => Map(getPairs(node):_*)
	  	  		  }
	  	lm.map( (m:Map[String,String]) => PropertyDescription (
    		  		 m.get(MeandreRepositoryVocabulary.key.toString).getOrElse("unknow"),
    		  		 m.get(MeandreRepositoryVocabulary.value.toString).getOrElse("unknow"),
    		  		 m.get(DC_11.description.toString),
    		  		 m - MeandreRepositoryVocabulary.key.toString
    		  		   - MeandreRepositoryVocabulary.value.toString 
    		  		   - DC_11.description.toString 
    		  		   - RDF.`type`.toString))      
    }

	/** Gets the data port information description for the requested type in a given model.
	 *
	 * @param uri The resource to poke
	 * @param portType The port type; either MeandreRepositoryVocabulary.input_data_port
	 *                 or MeandreRepositoryVocabulary.output_data_port
     * @param model The model to use
     * @return The data port information for the given URI in the given model
	 */
	protected def getDataPortForType ( uri:Resource, portType: JProperty, model: Model ) = 
		for ( propURI <- getMultivalueProperty(uri,portType,model ) if propURI.isResource ) 
			yield propURI match {
			  case portURI:Resource => Port(
					  portURI.toString,
					  safeGet(portURI,MeandreRepositoryVocabulary.name,model).getOrElse("unknow"),
					  safeGet(portURI,DC_11.description,model)
			  	)
		}
 
	/** Given model retrieves all the relevant information for each of
	 *  the contained components.
	 *
	 * @param model The model to use
     * @return The list of component descriptors stored in the model
	 */
	protected def buildComponentDescriptorsModel ( model:Model )   = {
		var res: List[ComponentDescriptor] = Nil
		val uris =  model.listSubjectsWithProperty(`type`, MeandreRepositoryVocabulary.executable_component)
		while ( uris.hasNext ) {
			val uri = uris.nextResource
			res ::= ComponentDescriptor (
					uri.toString,
					getCommonDescription(uri,model),
					Map( getProperties(uri,model) map { s:PropertyDescription=>(s.key,s) } :_* ),
					mustGet(uri,MeandreRepositoryVocabulary.runnable,model).toLowerCase,
					mustGet(uri,DC_11.format,model).toLowerCase,
					if (mustGet(uri,MeandreRepositoryVocabulary.firing_policy,model).toLowerCase=="any") FiringAny() else FiringAll(),
					mustGet(uri,MeandreRepositoryVocabulary.resource_location,model),
					safeGet(uri,MeandreRepositoryVocabulary.mode,model) match {
            case None => ComputeMode()
            case Some(s:Resource) if s==MeandreRepositoryVocabulary.mode_compute => ComputeMode()
            case Some(s:Resource) if s==MeandreRepositoryVocabulary.mode_webui => WebUIMode()
            case _ => ComputeMode()
          },
          getMultivalueProperty(uri,MeandreRepositoryVocabulary.execution_context,model).map(
					  (s) => s match {
					  		case s:Resource => URIContext(s.toString)
					  		case s:Literal => EmbeddedContext(s.getLexicalForm)
					  }
					),
					getDataPortForType(uri,MeandreRepositoryVocabulary.input_data_port,model),
					getDataPortForType(uri,MeandreRepositoryVocabulary.output_data_port,model)
			)
		}
		res
	}

  /**Given an URL retrieves all the relevant information for components in the
   *  serialized RDF pointed to.
   *
   * @param url The url contained a serialized RDF model
   * @return The list of component descriptors stored in the model
   */
  def buildComponentDescriptors(url: String) = buildComponentDescriptorsModel(readModelFromURL(url))

  /** Given a Jena model retrieves all the relevant information for components in the
     *  serialized RDF pointed to.
	 *
	 * @param url The url contained a serialized RDF model
     * @return The list of component descriptors stored in the model
	 */
	def buildComponentDescriptors ( model: Model ) = buildComponentDescriptorsModel(model)

	/** Given a flow URI and a model it retrieves all the described connectors.
	 * 
	 * @param uri The flow uri
	 * @param model The model to use
	 * @return The list of connector descriptions retrieved from the repository
	 */
	protected def getConnectors ( uri:Resource, model:Model ) = {
	  for ( conURI <- getMultivalueProperty(
			  				uri.getProperty(MeandreRepositoryVocabulary.connectors).getResource(),
			  				MeandreRepositoryVocabulary.data_connector, model ) 
	  		if conURI.isResource )
		  yield conURI match {
			  case conURI:Resource => ConnectorDescription(
            conURI.toString,
					  safeGet(conURI,MeandreRepositoryVocabulary.connector_instance_data_port_source,model).get,
					  safeGet(conURI,MeandreRepositoryVocabulary.connector_instance_data_port_target,model).get,
					  safeGet(conURI,MeandreRepositoryVocabulary.connector_instance_source,model).get,
					  safeGet(conURI,MeandreRepositoryVocabulary.connector_instance_target,model).get
			  	)
		}
	}
   
	/** Given an instance URI and a model it retrieves all the related Meandre properties
	 *  assigned to it. 
	 *
	 * @param uri The uri of the instance 
	 * @param model The model to explore
	 * @return A list containing the properties found for the instance 
	 */
	protected def getInstanceProperties ( uri:Resource, model:Model )  = {
	  
	  def getPairs ( node:Resource ) = 
		  for (stm<-getStatementWithSubject(node)  ) 
			  yield if (stm.getObject.isLiteral) stm.getPredicate.toString ->  stm.getLiteral.getString
                    else stm.getPredicate.toString -> stm.getObject.toString
	  
      val lm = for { node <- getMultivalueProperty ( uri, MeandreRepositoryVocabulary.property_set, model ) if node.isResource }
	  	  		  yield node match {
	  	  		    	case node:Resource => Map(getPairs(node):_*)
	  	  		  }
          
      lm.map( (m:Map[String,String]) => Property (
    		  		 m.get(MeandreRepositoryVocabulary.key.toString).getOrElse("unknow"),
    		  		 m.get(MeandreRepositoryVocabulary.value.toString).getOrElse("unknow"),
    		  		 m - MeandreRepositoryVocabulary.key.toString
    		  		   - MeandreRepositoryVocabulary.value.toString 
    		  		   - RDF.`type`.toString
      ))
      
    }
 
	/** Given a flow URI and a model returns all the instance associated to the flow.
	 * 
	 * @param uri The flow URI
	 * @param model The model to use
	 * @return A list of component instance descroptions 
	 */
 	protected def getInstances ( uri:Resource, model:Model ) = {
	  for ( insURI <- getMultivalueProperty(
			  				uri.getProperty(MeandreRepositoryVocabulary.components_instances).getResource(),
			  				MeandreRepositoryVocabulary.executable_component_instance, model ) 
	  		if insURI.isResource )
		  yield insURI match {
			  case insURI:Resource => ComponentInstanceDescription(
					  insURI.toString,
					  safeGet(insURI,MeandreRepositoryVocabulary.instance_name,model).get,
					  safeGet(insURI,DC_11.description,model),
					  safeGet(insURI,MeandreRepositoryVocabulary.instance_resource,model).get,
					  Map( getInstanceProperties(insURI,model) map { s:Property=>(s.key,s) } :_* )
			  	)
		}
	}
   
	
	/** Given model retrieves all the relevant information for each of
	 *  the contained flows.
	 *
	 * @param model The model to use
     * @return The list of flow descriptors stored in the model
	 */
	protected def buildFlowDescriptorsModel ( model: Model )   = {
	  var res: List[FlowDescriptor] = Nil
		val uris =  model.listSubjectsWithProperty(`type`, MeandreRepositoryVocabulary.flow_component)
		while ( uris.hasNext ) {
			val uri = uris.nextResource
			res ::= FlowDescriptor (
					uri.toString,
					getCommonDescription(uri,model),
					Map( getProperties(uri,model) map { s:PropertyDescription=>(s.key,s) } :_* ),
					getInstances(uri,model),
					getConnectors(uri,model)
			)
		}
		res
	}

  /** Given an URL retrieves all the relevant information for flows in the
     *  serialized RDF pointed to.
   *
   * @param url The url contained a serialized RDF model
     * @return The list of component descriptors stored in the model
   */
  def buildFlowDescriptors ( url: String ) = buildFlowDescriptorsModel(readModelFromURL(url))
	
	/** Given a Jena model retrieves all the relevant information for flows in the
   *  serialized RDF pointed to.
   *
   * @param model The Jena model to process
   * @return The list of component descriptors stored in the model
   */
  def buildFlowDescriptors(model: Model) = buildFlowDescriptorsModel(model)

  /** Given a URL returns a list of all the descriptors found, regardless
	 *  if they describe components or flows
	 * 
	 * @param url The url containing the serialized RDF model
	 * @return The list of descriptors found 
	 */
	def buildDescriptors ( url:String ) : List[Descriptor] = {
		val model = readModelFromURL(url)
		buildComponentDescriptorsModel(model)++buildFlowDescriptorsModel(model)
	}

  /**Given a Jena model returns a list of all the descriptors found, regardless
   *  if they describe components or flows
   *
   * @param model The Jena model to process
   * @return The list of descriptors found
   */
  def buildDescriptors(model: Model): List[Descriptor] = {
    buildComponentDescriptorsModel(model) ++ buildFlowDescriptorsModel(model)
  }

  /** Given a URL returns a list of all the descriptors found, regardless
	 *  if they describe components or flows
	 * 
	 * @param url The url containing the serialized RDF model
	 * @return The list of descriptors found 
	 */
	def apply ( url:String ) : List[Descriptor] = buildDescriptors(url)
 
	/** Given a Jena RDF model returns a list of all the descriptors found, regardless
	 *  if they describe components or flows
	 * 
	 * @param model The Jena RDF model
	 * @return The list of descriptors found 
	 */
	def apply ( model:Model ) : List[Descriptor] = buildDescriptors(model)

}

