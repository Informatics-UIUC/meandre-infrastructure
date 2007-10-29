package org.meandre.core.store.repository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Set;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/** This class wraps the basic description of an executable component.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class ExecutableComponentDescription {

	/** The resource for the executable component */
	private Resource resExecutableComponent = null;
	
	/** The name of the executable component */
	private String sName = null;
	
	/** The description of the executable component */
	private String sDescription = null;
	
	/** The rights of the executable component */
	private String sRights = null;
	
	/** The creator of the executable component */
	private String sCreator = null;
	
	/** The date of the executable component */
	private Date dateCreation = null;
	
	/** What kind of runnable component is being described */
	private String sRunnable = null;
	
	/** What is the firing policy */
	private String sFiringPolicy = null;
	
	/** What executable format does the runnable component take */
	private String sFormat = null;

	/** The set of contexts required for execution */
	private Set<Resource> setContext = null;
	
	/** The resource pointing to the executable component implementation */
	private Resource resLocation = null;
	
	/** The set of input data ports */
	private Set<DataPortDescription> setInputs = null;
	
	/** The hash table for input resource mapping */
	private Hashtable<Resource,DataPortDescription> htInputsMap = null;

	/** The hash table for output resource mapping */
	private Hashtable<Resource,DataPortDescription> htOutputsMap = null;

	/** The set of output data ports */
	private Set<DataPortDescription> setOutputs = null;

	/** The property descriptions */
	private PropertiesDescriptionDefinition pddProperties = null;

	/** The tag description */
	private TagsDescription tagDesc = null;
	
	/** Describes an executable component.
	 * 
	 * @param resExecutableComponent The resource for the wrapped executable component
	 * @param sName The name of the component
	 * @param sDescription The description of the component
	 * @param sRights The rights of the component
	 * @param sCreator The creator
	 * @param dateCreation The data of creation
	 * @param sRunnable What kind of runnable component it is
	 * @param sFiringPolicy The firing policy
	 * @param sFormat The format for the implementation of the component
	 * @param setContext The set of contexts of the component
	 * @param resLocation The location of the implementation of the component
	 * @param setInputs The set of input data ports
	 * @param setOutputs The set of output data ports
	 * @param pddProperties 
	 * @param tagDesc 
	 * @throws CorruptedDescriptionException Either the wrong runnable, format, or firing policy are incorrect
	 */
	public ExecutableComponentDescription (
			Resource resExecutableComponent,
			String sName,
			String sDescription,
			String sRights,
			String sCreator,
			Date dateCreation,
			String sRunnable,
			String sFiringPolicy,
			String sFormat,
			Set<Resource> setContext,
			Resource resLocation,
			Set<DataPortDescription> setInputs,
			Set<DataPortDescription> setOutputs, 
			PropertiesDescriptionDefinition pddProperties, 
			TagsDescription tagDesc
		) throws CorruptedDescriptionException{
		// Sanity checks
		if ( !RepositoryImpl.setRunnable.contains(sRunnable) )
			throw new CorruptedDescriptionException(sRunnable+" is not a valid type: "+RepositoryImpl.setRunnable);
		
		if ( !RepositoryImpl.setFormat.contains(sFormat) )
			throw new CorruptedDescriptionException(sFormat+" is not a valid type: "+RepositoryImpl.setFormat);
		
		if ( !RepositoryImpl.setFiringPolicy.contains(sFiringPolicy) )
			throw new CorruptedDescriptionException(sFiringPolicy+" is not a valid type: "+RepositoryImpl.setFiringPolicy);
		
		// Initialization
		this.resExecutableComponent = resExecutableComponent;
		this.sName = sName;
		this.sDescription = sDescription;
		this.sRights = sRights;
		this.sCreator = sCreator;
		this.dateCreation = dateCreation;
		this.sRunnable = sRunnable;
		this.sFiringPolicy = sFiringPolicy;
		this.sFormat = sFormat;
		this.setContext = setContext;
		this.resLocation = resLocation;
		this.setInputs = setInputs;
		this.setOutputs = setOutputs;	
		this.pddProperties = pddProperties;
		this.tagDesc  = tagDesc;
		// Update the mappings
		this.htInputsMap = new Hashtable<Resource,DataPortDescription>();
		this.htOutputsMap = new Hashtable<Resource,DataPortDescription>();
		for ( DataPortDescription dpd:setInputs )
			htInputsMap.put(dpd.getResource(),dpd);
		for ( DataPortDescription dpd:setOutputs )
			htOutputsMap.put(dpd.getResource(),dpd);
	}
	
	/** Returns the executable component resource.
	 * 
	 * @return The resource
	 */
	public Resource getExecutableComponent() {
		return resExecutableComponent;
	}
	
	/** Returns the components name.
	 * 
	 * @return The name
	 */
	public String getName() {
		return sName;
	}
	
	/** Returns the executable component description.
	 * 
	 * @return The description
	 */
	public String getDescription () {
		return sDescription;
	}
	
	/** Returns the rights of the component.
	 * 
	 * @return The rights
	 */
	public String getRights () {
		return sRights;
	}
	
	/** Returns the creator of the component.
	 * 
	 * @return The creator
	 */
	public String getCreator () {
		return sCreator;
	}
	
	/** Returns the creation date of the component.
	 * 
	 * @return The date
	 */
	public Date getCreationDate () {
		return dateCreation;
	}
	
	/** Returns the runnable type.
	 * 
	 * @return The runnable type
	 */
	public String getRunnable () {
		return sRunnable;
	}
	
	/** Returns the firing policy.
	 * 
	 * @return The firing policy
	 */
	public String getFiringPolicy () {
		return sFiringPolicy;
	}
	
	/** Returns the format of the executable component implementations.
	 * 
	 * @return The format of the executable component
	 */
	public String getFormat () {
		return sFormat;
	}
	
	/** The set of contextes associated to the context.
	 * 
	 * @return The context set
	 */
	public Set<Resource> getContext () {
		return setContext;
	}
	
	/** The location of the executable component.
	 * 
	 * @return The location of the executable component
	 */
	public Resource getLocation () {
		return resLocation;
	}
	
	/** The set of data ports that define the inputs of the executable component.
	 * 
	 * @return The set of data ports
	 */
	public Set<DataPortDescription> getInputs () {
		return setInputs;
	}
	
	/** Returns the input data port description linked to the provided resource.
	 * 
	 * @param res The resource
	 * @return The data port description
	 */
	public DataPortDescription getInput ( Resource res ) {
		return htInputsMap.get(res);
	}
	
	
	/** The set of data ports that define the outputs of the executable component.
	 * 
	 * @return The set of data ports
	 */
	public Set<DataPortDescription> getOutputs () {
		return setOutputs;
	}

	/** Returns the output data port description linked to the provided resource.
	 * 
	 * @param res The resource
	 * @return The data port description
	 */
	public DataPortDescription getOutput ( Resource res ) {
		return htOutputsMap.get(res);
	}
	/** Returns the property descriptions for the descripbed executable component.
	 * 
	 * @return The property definitions
	 */
	public PropertiesDescriptionDefinition getProperties () {
		return pddProperties;
	}
	
	/** Return the tags linked to the executable component.
	 * 
	 * @return The tag set
	 */
	public TagsDescription getTags () {
		return tagDesc;
	}

	/** Returns the model describing this executable component.
	* 
	 * @return The model
	 */
	public Model getModel() {
		Model model = ModelFactory.createDefaultModel();
		
		// Setting the name spaces
		model.setNsPrefix("", "http://www.meandre.org/ontology/");
		model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
		model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		model.setNsPrefix("rdfs","http://www.w3.org/2000/01/rdf-schema#");
		model.setNsPrefix("dc","http://purl.org/dc/elements/1.1/");
		
		Resource res = model.createResource(resExecutableComponent.toString());
		
		// Plain properties
		res.addProperty(ResourceFactory.createProperty("http://www.meandre.org/ontology/name"),model.createTypedLiteral(sName))
		   .addProperty(ResourceFactory.createProperty("http://purl.org/dc/elements/1.1/description"),model.createTypedLiteral(sDescription))
		   .addProperty(ResourceFactory.createProperty("http://purl.org/dc/elements/1.1/rights"),model.createTypedLiteral(sRights))
		   .addProperty(ResourceFactory.createProperty("http://purl.org/dc/elements/1.1/creator"),model.createTypedLiteral(sCreator))
		   .addProperty(ResourceFactory.createProperty("http://purl.org/dc/elements/1.1/date"),model.createTypedLiteral(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(dateCreation),XSDDatatype.XSDdate))
		   .addProperty(ResourceFactory.createProperty("http://purl.org/dc/elements/1.1/format"),model.createTypedLiteral(sFormat))
		   .addProperty(ResourceFactory.createProperty("http://www.meandre.org/ontology/runnable"),model.createTypedLiteral(sRunnable))
		   .addProperty(ResourceFactory.createProperty("http://www.meandre.org/ontology/firing_policy"),model.createTypedLiteral(sFiringPolicy))
		   .addProperty(ResourceFactory.createProperty("http://www.meandre.org/ontology/resource_location"),model.createResource(resLocation.toString()))
		   .addProperty(ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),model.createResource("http://www.meandre.org/ontology/executable_component"))
		   ;
		
		// Adding tags
		for ( String sTag:tagDesc.getTags() )
			res.addProperty(ResourceFactory.createProperty("http://www.meandre.org/ontology/tag"),model.createTypedLiteral(sTag));
			   
		// Adding execution contexts
		for ( Resource resContext:setContext )
			res.addProperty(ResourceFactory.createProperty("http://www.meandre.org/ontology/execution_context"),model.createResource(resContext.toString()));
		
		// Adding properties
		for ( String sKey:pddProperties.getKeys()) {
			String sValue = pddProperties.getValue(sKey);
			String  sDesc = pddProperties.getDescription(sKey);
			res.addProperty(ResourceFactory.createProperty("http://www.meandre.org/ontology/property_set"),
					model.createResource(resExecutableComponent.toString()+"/property/"+sKey)
						 .addProperty(ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),model.createResource("http://www.meandre.org/ontology/property"))
					     .addProperty(ResourceFactory.createProperty("http://www.meandre.org/ontology/key"),model.createTypedLiteral(sKey))
					     .addProperty(ResourceFactory.createProperty("http://www.meandre.org/ontology/value"),model.createTypedLiteral(sValue))
					     .addProperty(ResourceFactory.createProperty("http://purl.org/dc/elements/1.1/description"),model.createTypedLiteral(sDesc))
				);
		}
		
		// Adding inputs
		for ( DataPortDescription dpd:setInputs) {
			String sID = dpd.getIdentifier();
			String sName = dpd.getName();
			Resource resdpd = dpd.getResource();
			String sDesc = dpd.getDescription();
			res.addProperty(ResourceFactory.createProperty("http://www.meandre.org/ontology/input_data_port"),
					model.createResource(resdpd.toString())
						 .addProperty(ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),model.createResource("http://www.meandre.org/ontology/data_port"))
					     .addProperty(ResourceFactory.createProperty("http://purl.org/dc/elements/1.1/identifier"),model.createTypedLiteral(sID))
					     .addProperty(ResourceFactory.createProperty("http://www.meandre.org/ontology/name"),model.createTypedLiteral(sName))
					     .addProperty(ResourceFactory.createProperty("http://purl.org/dc/elements/1.1/description"),model.createTypedLiteral(sDesc))
				);
		}
		
		// Adding outputs
		for ( DataPortDescription dpd:setOutputs) {
			String sID = dpd.getIdentifier();
			String sName = dpd.getName();
			Resource resdpd = dpd.getResource();
			String sDesc = dpd.getDescription();
			res.addProperty(ResourceFactory.createProperty("http://www.meandre.org/ontology/output_data_port"),
					model.createResource(resdpd.toString())
					     .addProperty(ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),model.createResource("http://www.meandre.org/ontology/data_port"))
					     .addProperty(ResourceFactory.createProperty("http://purl.org/dc/elements/1.1/identifier"),model.createTypedLiteral(sID))
					     .addProperty(ResourceFactory.createProperty("http://www.meandre.org/ontology/name"),model.createTypedLiteral(sName))
					     .addProperty(ResourceFactory.createProperty("http://purl.org/dc/elements/1.1/description"),model.createTypedLiteral(sDesc))
				);
		}
		
		return model;
	}
	
	/** Returns the name of the component as description of the executable component description.
	 * 
	 * @return The name of the executable component
	 */
	public String toString() {
		return resExecutableComponent.toString();
	}

}
