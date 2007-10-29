package org.meandre.core.store.repository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
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
public class FlowDescription {

	/** The resource for the flow component */
	private Resource resFlowComponent = null;

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

	/** Instanciated executable components */
	private Set<ExecutableComponentInstanceDescription> setExecutableComponentInstances = null;

	/** Hash table to access the instantiated components */
	private Hashtable<Resource,ExecutableComponentInstanceDescription> htExecutableComponentInstances = null;

	/** Instanctiated connections between instantiated components */
	private Set<ConnectorDescription> setConnectorDescription = null;

	/** The tags linked to the flow */
	private TagsDescription tagDesc = null;

	/** Create an empty flow description instance
	 *
	 */
	public FlowDescription () {
		this.resFlowComponent = null;
		this.sName = "";
		this.sDescription = "";
		this.sRights = "";
		this.sCreator = "";
		this.dateCreation = new Date();
		this.setExecutableComponentInstances = new HashSet<ExecutableComponentInstanceDescription>();
		this.setConnectorDescription = new HashSet<ConnectorDescription>();
		this.tagDesc  = new TagsDescription();
		// Initialize the instance map
		this.htExecutableComponentInstances = new Hashtable<Resource,ExecutableComponentInstanceDescription>();
		for ( ExecutableComponentInstanceDescription ecid:setExecutableComponentInstances )	 {
			htExecutableComponentInstances.put(ecid.getExecutableComponentInstance(),ecid);
		}
	}

	/** Create a flow description instance
	 *
	 * @param resFlowComponent The resource identifying the flow
	 * @param sName The name of the flow
	 * @param sDescription The description of the flow
	 * @param sRights The rights of the flow
	 * @param sCreator The creator
	 * @param dateCreation The date of creation
	 * @param setExecutableComponentInstances The set of executable components instances used by the flow
	 * @param setConnectorDescription The set of connector descriptions
	 * @param tagsDesc
	 */
	public FlowDescription (
				Resource resFlowComponent,
				String sName,
				String sDescription,
				String sRights,
				String sCreator,
				Date dateCreation,
				Set<ExecutableComponentInstanceDescription> setExecutableComponentInstances,
				Set<ConnectorDescription> setConnectorDescription,
				TagsDescription tagsDesc
			) {
		this.resFlowComponent = resFlowComponent;
		this.sName = sName;
		this.sDescription = sDescription;
		this.sRights = sRights;
		this.sCreator = sCreator;
		this.dateCreation = dateCreation;
		this.setExecutableComponentInstances = setExecutableComponentInstances;
		this.setConnectorDescription = setConnectorDescription;
		this.tagDesc  = tagsDesc;
		// Initialize the instance map
		this.htExecutableComponentInstances = new Hashtable<Resource,ExecutableComponentInstanceDescription>();
		for ( ExecutableComponentInstanceDescription ecid:setExecutableComponentInstances )	 {
			htExecutableComponentInstances.put(ecid.getExecutableComponentInstance(),ecid);
		}
	}

	/** Sets the executable component resource.
	 *
	 * @param res The resource
	 */
	public void setFlowComponent ( Resource res ) {
		resFlowComponent = res;
	}

	/** Returns the executable component resource.
	 *
	 * @return The resource
	 */
	public Resource getFlowComponent() {
		return resFlowComponent;
	}

	/** Sets the components name.
	 *
	 * @param sName The name
	 */
	public void  setName( String sName) {
		this.sName=sName;
	}

	/** Returns the components name.
	 *
	 * @return The name
	 */
	public String getName() {
		return sName;
	}

	/** Sets the executable component description.
	 *
	 * @param sDesc The description
	 */
	public void setDescription ( String sDesc ) {
		this.sDescription=sDesc;
	}

	/** Returns the executable component description.
	 *
	 * @return The description
	 */
	public String getDescription () {
		return sDescription;
	}

	/** Set the rights of the component.
	 *
	 * @param sRightsText The rights
	 */
	public void  setRights ( String sRightsText ) {
		sRights =  sRightsText;
	}

	/** Returns the rights of the component.
	 *
	 * @return The rights
	 */
	public String getRights () {
		return sRights;
	}

	/** Sets the creator of the component.
	 *
	 * @param sCreator The creator
	 */
	public void  setCreator ( String sCreator) {
		this.sCreator=sCreator;
	}

	/** Returns the creator of the component.
	 *
	 * @return The creator
	 */
	public String getCreator () {
		return sCreator;
	}

	/** Sets the creation date of the component.
	 *
	 * @param date The date
	 */
	public void setCreationDate  ( Date d ) {
		dateCreation = d;
	}

	/** Returns the creation date of the component.
	 *
	 * @return The date
	 */
	public Date getCreationDate () {
		return dateCreation;
	}

	/** Returns a given executable component instance description based
	 * on the provide resource. Returns null if the instance is unknown
	 *
	 * @param res The resource to locate
	 * @return The executable component instance description
	 */
	public Resource getExecutableComponentResourceForInstance ( Resource res ) {
		
		ExecutableComponentInstanceDescription ecd = htExecutableComponentInstances.get(res);
		
		return (ecd==null)?null:ecd.getExecutableComponent();
		
	}

	/** Adds an executable component instance.
	 *
	 * @param ecid The executable component instances to add
	 */
	public void addExecutableComponentInstance ( ExecutableComponentInstanceDescription ecid ) {
		setExecutableComponentInstances.add(ecid);
		htExecutableComponentInstances.put(ecid.getExecutableComponent(), ecid);
	}

	/** Removes an executable component instance.
	 *
	 * @param res The executable coponent instances resource to remove
	 */
	public void removeExecutableComponentInstance ( Resource res ) {
		ExecutableComponentInstanceDescription ecid = htExecutableComponentInstances.get(res);
		if ( ecid!=null ) {
			setExecutableComponentInstances.remove(ecid);
			htExecutableComponentInstances.remove(ecid.getExecutableComponent());
		}
	}

	/** Removes an executable component instance.
	 *
	 * @param ecd The executable coponent instances to remove
	 */
	public void removeExecutableComponentInstance ( ExecutableComponentInstanceDescription ecd ) {
		setExecutableComponentInstances.remove(ecd);
		htExecutableComponentInstances.remove(ecd.getExecutableComponent());
	}

	/** Returns the set of executable component instances.
	 *
	 * @return The set of executable coponent instances descriptions
	 */
	public Set<ExecutableComponentInstanceDescription> getExecutableComponentInstances () {
		return setExecutableComponentInstances;
	}

	/** Returns the set of connector descriptions.
	 *
	 * @return The connector description set
	 */
	public Set<ConnectorDescription> getConnectorDescriptions () {
		return setConnectorDescription;
	}

	/** The tags linked to the flow.
	 *
	 * @return The tag set.
	 */
	public TagsDescription getTags () {
		return tagDesc;
	}

	/** Return the model that describes this flow component.
	 *
	 * @return The model
	 */
	public Model getModel () {
		Model model = ModelFactory.createDefaultModel();

		// Setting the name spaces
		model.setNsPrefix("", "http://www.meandre.org/ontology/");
		model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
		model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		model.setNsPrefix("rdfs","http://www.w3.org/2000/01/rdf-schema#");
		model.setNsPrefix("dc","http://purl.org/dc/elements/1.1/");

		if ( resFlowComponent!=null ) {
			Resource res = model.createResource(resFlowComponent.toString());
	
			// Plain properties
			res.addProperty(ResourceFactory.createProperty("http://www.meandre.org/ontology/name"),model.createTypedLiteral(sName))
			   .addProperty(ResourceFactory.createProperty("http://purl.org/dc/elements/1.1/description"),model.createTypedLiteral(sDescription))
			   .addProperty(ResourceFactory.createProperty("http://purl.org/dc/elements/1.1/rights"),model.createTypedLiteral(sRights))
			   .addProperty(ResourceFactory.createProperty("http://purl.org/dc/elements/1.1/creator"),model.createTypedLiteral(sCreator))
			   .addProperty(ResourceFactory.createProperty("http://purl.org/dc/elements/1.1/date"),model.createTypedLiteral(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(dateCreation),XSDDatatype.XSDdate))
			   .addProperty(ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),model.createResource("http://www.meandre.org/ontology/flow_component"));
			   ;
	
			// Adding tags
			for ( String sTag:tagDesc.getTags() )
				res.addProperty(ResourceFactory.createProperty("http://www.meandre.org/ontology/tag"),model.createTypedLiteral(sTag));
	
			// Adding connectors
			Resource resCons = null;
			if ( setConnectorDescription.size()>0 ) {
				res.addProperty(ResourceFactory.createProperty("http://www.meandre.org/ontology/connectors"),
					resCons=model.createResource(resFlowComponent.toString()+"/connector/set"));
				for ( ConnectorDescription cd:setConnectorDescription ) {
					String sConID = cd.getConnector().toString();
					String sSource = cd.getSourceInstance().toString();
					String sSourceDP = cd.getSourceIntaceDataPort().toString();
					String sTarget = cd.getTargetInstance().toString();
					String sTargetDP = cd.getTargetIntaceDataPort().toString();
					resCons.addProperty(ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
							model.createResource("http://www.meandre.org/ontology/connector_set"))
							     .addProperty(ResourceFactory.createProperty("http://www.meandre.org/ontology/data_connector"),
								  model.createResource(sConID).addProperty(ResourceFactory.createProperty("http://www.meandre.org/ontology/connector_instance_source"), model.createResource(sSource))
	                                                          .addProperty(ResourceFactory.createProperty("http://www.meandre.org/ontology/connector_instance_data_port_source"), model.createResource(sSourceDP))
	                                                          .addProperty(ResourceFactory.createProperty("http://www.meandre.org/ontology/connector_instance_target"), model.createResource(sTarget))
	                                                          .addProperty(ResourceFactory.createProperty("http://www.meandre.org/ontology/connector_instance_data_port_target"), model.createResource(sTargetDP))
	                                                          .addProperty(ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), model.createResource("http://www.meandre.org/ontology/data_connector_configuration"))
	                      );
				}
			}
	
			// Adding instances
			if ( setExecutableComponentInstances.size()>0 ) {
				res.addProperty(ResourceFactory.createProperty("http://www.meandre.org/ontology/components_instances"),
						resCons=model.createResource(resFlowComponent.toString()+"/components/set"));
				for ( ExecutableComponentInstanceDescription ecid:setExecutableComponentInstances ) {
					String sConID = ecid.getExecutableComponentInstance().toString();
					String sComp = ecid.getExecutableComponent().toString();
					String sName = ecid.getName();
					String sDesc = ecid.getDescription();
					Resource resIns = null;
					resCons.addProperty(ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
							model.createResource("http://www.meandre.org/ontology/instance_set"))
							     .addProperty(ResourceFactory.createProperty("http://www.meandre.org/ontology/executable_component_instance"),
								  (resIns=model.createResource(sConID)).addProperty(ResourceFactory.createProperty("http://www.meandre.org/ontology/instance_resource"), model.createResource(sComp))
		                                                         .addProperty(ResourceFactory.createProperty("http://www.meandre.org/ontology/instance_name"), model.createTypedLiteral(sName))
		                                                         .addProperty(ResourceFactory.createProperty("http://purl.org/dc/elements/1.1/description"), model.createTypedLiteral(sDesc))
		                                                         .addProperty(ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), model.createResource("http://www.meandre.org/ontology/instance_configuration"))
		                     );
					// Adding properties if any
					PropertiesDescription propIns = ecid.getProperties();
					if ( propIns!=null )
						for ( String sKey:propIns.getKeys() )
							resIns.addProperty(ResourceFactory.createProperty("http://www.meandre.org/ontology/property_set"),
									model.createResource(sConID+"/property/"+sKey).addProperty(ResourceFactory.createProperty("http://www.meandre.org/ontology/key"), model.createTypedLiteral(sKey))
	                                                                             .addProperty(ResourceFactory.createProperty("http://www.meandre.org/ontology/value"), model.createTypedLiteral(propIns.getValue(sKey)))
	                                                                             .addProperty(ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), model.createResource("http://www.meandre.org/ontology/property"))
							     );
				}
			}
		}

		return model;
	}
}
