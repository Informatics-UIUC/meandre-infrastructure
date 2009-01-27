package org.meandre.core.utils.vocabulary;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/** This class contains the basic repository vocabulary for Meandre.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class RepositoryVocabulary {

	/** The RDF model that holds the vocabulary terms */
	private static Model m_model = ModelFactory.createDefaultModel();

	/** The namespace of the vocabulary as a string  */
	public static final String NS = "http://www.meandre.org/ontology/";

	/** The namespace of the vocabulary as a string */
	public static String getURI() {
		return NS;
	}

	/** The namespace of the vocabulary as a resource */
	public static final Resource NAMESPACE = m_model.createResource(NS);

	/** The components instances identifier. */
	public static final Property components_instances = m_model.createProperty("http://www.meandre.org/ontology/components_instances");
	
	/** The connector instance data port source. */
	public static final Property connector_instance_data_port_source = m_model.createProperty("http://www.meandre.org/ontology/connector_instance_data_port_source");
	
	/** The connector instance data port target. */
	public static final Property connector_instance_data_port_target = m_model.createProperty("http://www.meandre.org/ontology/connector_instance_data_port_target");

	/** The connector instance source. */
	public static final Property connector_instance_source = m_model.createProperty("http://www.meandre.org/ontology/connector_instance_source");

	/** The connector instance target. */
	public static final Property connector_instance_target = m_model.createProperty("http://www.meandre.org/ontology/connector_instance_target");

	/** The connector set resource */
	public static final Resource connector_set = m_model.createResource("http://www.meandre.org/ontology/connector_set");

	/** The connector set. */
	public static final Property connectors = m_model.createProperty("http://www.meandre.org/ontology/connectors");

	/** The data connector type. */
	public static final Property data_connector = m_model.createProperty("http://www.meandre.org/ontology/data_connector");
	
	/** The data connector configuration resource */
	public static final Resource data_connector_configuration = m_model.createResource("http://www.meandre.org/ontology/data_connector_configuration");
	
	/** The data port type */
	public static final Resource data_port = m_model.createResource("http://www.meandre.org/ontology/data_port");
	
	/** The executable component type */
	public static final Resource executable_component = m_model.createResource("http://www.meandre.org/ontology/executable_component");

	/** The executable component instance */
	public static final Property executable_component_instance = m_model.createProperty("http://www.meandre.org/ontology/executable_component_instance");

	/** The execution context */
	public static final Property execution_context = m_model.createProperty("http://www.meandre.org/ontology/execution_context");

	/** The firing policy */
	public static final Property firing_policy = m_model.createProperty("http://www.meandre.org/ontology/firing_policy");

	/** The flow component type */
	public static final Resource flow_component = m_model.createResource("http://www.meandre.org/ontology/flow_component");
	
	/** The input data port  */
	public static final Property input_data_port = m_model.createProperty("http://www.meandre.org/ontology/input_data_port");

	/** The instance configuration type */
	public static final Resource instance_configuration = m_model.createResource("http://www.meandre.org/ontology/instance_configuration");
	
	/** The instance name  */
	public static final Property instance_name = m_model.createProperty("http://www.meandre.org/ontology/instance_name");

	/** The instance resource */
	public static final Property instance_resource = m_model.createProperty("http://www.meandre.org/ontology/instance_resource");

	/** The instance set type */
	public static final Resource instance_set = m_model.createResource("http://www.meandre.org/ontology/instance_set"); 
	
	/** The key of an element */
	public static final Property key = m_model.createProperty("http://www.meandre.org/ontology/key");

	/** Name of an element */
	public static final Property name = m_model.createProperty("http://www.meandre.org/ontology/name");

	/** The output data port type  */
	public static final Property output_data_port = m_model.createProperty("http://www.meandre.org/ontology/output_data_port");

	/** The property type */
	public static final Resource property = m_model.createResource("http://www.meandre.org/ontology/property");
	
	/** The property set */
	public static final Property property_set = m_model.createProperty("http://www.meandre.org/ontology/property_set");

	/** The resource location  */
	public static final Property resource_location = m_model.createProperty("http://www.meandre.org/ontology/resource_location");

	/** The runnable entry value */
	public static final Property runnable = m_model.createProperty("http://www.meandre.org/ontology/runnable");

	/** The property tag */
	public static final Property tag = m_model.createProperty("http://www.meandre.org/ontology/tag");

	/** The property value */
	public static final Property value = m_model.createProperty("http://www.meandre.org/ontology/value");

	/** The mode property */
	public static final Property mode = m_model.createProperty("http://www.meandre.org/ontology/mode");
}
