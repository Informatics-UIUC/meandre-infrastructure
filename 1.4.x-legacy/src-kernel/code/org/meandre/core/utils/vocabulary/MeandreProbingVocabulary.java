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
public class MeandreProbingVocabulary {

	/** The RDF model that holds the vocabulary terms */
	private static Model m_model = ModelFactory.createDefaultModel();

	/** The namespace of the vocabulary as a string  */
	public static final String NS = "http://www.meandre.org/probing/";

	/** The namespace of the vocabulary as a string */
	public static String getURI() {
		return NS;
	}

	/** The namespace of the vocabulary as a resource */
	public static final Resource NAMESPACE = m_model.createResource(NS);

	/** The components instances identifier. */
	public static final Property components_instances = m_model.createProperty("http://www.meandre.org/ontology/components_instances");

	/** The flow started. */
	public static final Property flow_started = m_model.createProperty("http://www.meandre.org/ontology/flow_started"); 

	/** The flow finished. */
	public static final Property flow_finished = m_model.createProperty("http://www.meandre.org/ontology/flow_finished"); 

	/** The flow aborted. */
	public static final Property flow_aborted = m_model.createProperty("http://www.meandre.org/ontology/flow_aborted"); 

	/** The executable component initialized. */
	public static final Property executable_component_instance_initialized = m_model.createProperty("http://www.meandre.org/ontology/executable_component_instance_initialized"); 

	/** The executable component initialized. */
	public static final Property executable_component_instance_aborted = m_model.createProperty("http://www.meandre.org/ontology/executable_component_instance_aborted"); 

	/** The executable component initialized. */
	public static final Property executable_component_instance_disposed = m_model.createProperty("http://www.meandre.org/ontology/executable_component_instance_disposed"); 

	/** A XML serialized state. */
	public static final Property state = m_model.createProperty("http://www.meandre.org/ontology/state"); 

	/** The executable component instance pushed data. */
	public static final Property executable_component_instance_pushed_data = m_model.createProperty("http://www.meandre.org/ontology/executable_component_instance_pushed_data"); 

	/** A XML serialized data piece. */
	public static final Property data_piece = m_model.createProperty("http://www.meandre.org/ontology/data_piece"); 

	/** The executable component instance pulled data. */
	public static final Property executable_component_instance_pulled_data = m_model.createProperty("http://www.meandre.org/ontology/executable_component_instance_pulled_data"); 

	/** The executable component instance fired. */
	public static final Property executable_component_instance_fired = m_model.createProperty("http://www.meandre.org/ontology/executable_component_instance_fired"); 

	/** The executable component instance is cooling down. */
	public static final Property executable_component_instance_cooling_down = m_model.createProperty("http://www.meandre.org/ontology/executable_component_instance_cooling_down"); 

	/** The executable component instance is cooling down. */
	public static final Property executable_component_instance_get_property = m_model.createProperty("http://www.meandre.org/ontology/executable_component_instance_get_property"); 


}
