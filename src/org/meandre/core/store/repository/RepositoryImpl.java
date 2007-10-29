package org.meandre.core.store.repository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.meandre.Bootstrapper;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.larq.IndexBuilderString;
import com.hp.hpl.jena.query.larq.IndexLARQ;
import com.hp.hpl.jena.query.larq.LARQ;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/** This class implements a basic component repository.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class RepositoryImpl implements QueryableRepository {

	/** The available runnable types */
	public final static Set<String> setRunnable = new HashSet<String>();
	
	/**  The available format types */
	public final static Set<String> setFormat = new HashSet<String>();
	
	/** The available firing policies */
	public final static Set<String> setFiringPolicy = new HashSet<String>();
	
	/** The logger for the bootstrapper */
	protected static Logger log = null;
	
	static {
		//
		// Initializing the logger and its handlers
		//
		log = Logger.getLogger(Bootstrapper.class.getName());
		log.setLevel(Level.CONFIG);
		log.addHandler(Bootstrapper.handler);
		
		//
		// Initialize the sets
		//
		
		// Runnable
		setRunnable.add("java");
		
		// Format
		setFormat.add("java/class");
		
		// Firing policy
		setFiringPolicy.add("any");
		setFiringPolicy.add("all");
	}

	/** Retrieve all the executable components */
	private final static String QUERY_GET_ALL_EXECUTABLE_COMPONENTS = 
		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
		"PREFIX meandre: <http://www.meandre.org/ontology/>\n"+
		"SELECT ?component  " +
		"WHERE { ?component rdf:type meandre:executable_component }" ;
	
	/** Retrieve the main properties of an executable component */
	private final static String QUERY_GET_EXECUTABLE_COMPONENT = 
		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
		"PREFIX meandre: <http://www.meandre.org/ontology/>\n"+
		"PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"+
		"SELECT DISTINCT ?component ?name ?creator ?date ?desc ?rights ?runnable ?format ?location " +
		"WHERE { " +
		"	?component ?p ?o . " +
		"   ?component rdf:type meandre:executable_component . " +
		"	?component meandre:name ?name ." +
		"	?component dc:creator ?creator ." +
		"	?component dc:date ?date . " +
		" 	?component dc:description ?desc . " +
		"	?component dc:rights ?rights . " +
		"	?component meandre:runnable ?runnable . " +
		"	?component dc:format ?format . " +
		"   ?component meandre:resource_location ?location " +
		"}" ;
	
	/** Retieve all the tags linked to the executable component */
	private final static String QUERY_GET_EXECUTABLE_TAGS = 
		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
		"PREFIX meandre: <http://www.meandre.org/ontology/>\n"+
		"PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"+
		"SELECT DISTINCT ?component ?tag " +
		"WHERE { " +
		"	?component ?p ?o . " +
		"   ?component rdf:type meandre:executable_component . " +
		"	?component meandre:tag ?tag  " +
		"}" ;
	
	
	/** Returns all the executable component contexts */
	private final static String QUERY_GET_EXECUTABLE_COMPONENT_EXECUTION_CONTEXT = 
		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
		"PREFIX meandre: <http://www.meandre.org/ontology/>\n"+
		"PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"+
		"SELECT DISTINCT ?component ?context " +
		"WHERE { " +
		"	?component ?p ?o . " +
		"   ?component rdf:type meandre:executable_component . " +
		"	?component meandre:execution_context ?context " +
		"}" ;
	
	/** Retrieve the properties for a given component. */
	private final static String QUERY_GET_EXECUTABLE_PROPERTY_DEFINITIONS =
		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
		"PREFIX meandre: <http://www.meandre.org/ontology/>\n"+
		"PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"+
		"SELECT DISTINCT ?component ?pname ?key ?value ?desc " +
		"WHERE { " +
		"	?component ?p ?o . " +
		"   ?component rdf:type meandre:executable_component . " +
		"	?component meandre:property_set ?pname . " +
		"	?pname rdf:type meandre:property . " +
		"	?pname meandre:key ?key . " +
		"	?pname meandre:value ?value . " +
		"	?pname dc:description ?desc ." +
		"}" ;

	
	/** Retrieve the input data port descriptions of an executable component */
	private final static String QUERY_GET_EXECUTABLE_COMPONENT_INPUTS = 
		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
		"PREFIX meandre: <http://www.meandre.org/ontology/>\n"+
		"PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"+
		"SELECT DISTINCT ?component ?port ?identifier ?name ?desc " +
		"WHERE { " +
		"	?component ?p ?o . " +
		"   ?component rdf:type meandre:executable_component . " +
		"	?component meandre:input_data_port ?port . " +
		"	?port dc:identifier ?identifier . " +
		"	?port dc:description ?desc ." +
		"	?port meandre:name ?name . " +
		"}" ;
	
	/** Retrieve the firing policy for a given component */
	private final static String QUERY_GET_INPUT_FIRING =
		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
		"PREFIX meandre: <http://www.meandre.org/ontology/>\n"+
		"PREFIX dc: <http://purl.org/elements/1.1/>\n"+
		"SELECT DISTINCT ?component ?firing " +
		"WHERE { " +
		"	?component ?p ?o . " +
		"   ?component rdf:type meandre:executable_component . " +
		"	?component meandre:firing_policy ?firing ." +
		"}" ;
	
	/** Retrieve the input data port descriptions of an executable component */
	private final static String QUERY_GET_EXECUTABLE_COMPONENT_OUTPUTS = 
		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
		"PREFIX meandre: <http://www.meandre.org/ontology/>\n"+
		"PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"+
		"SELECT DISTINCT ?component ?port ?identifier ?name ?desc " +
		"WHERE { " +
		"	?component ?p ?o . " +
		"   ?component rdf:type meandre:executable_component . " +
		"	?component meandre:output_data_port ?port . " +
		"	?port dc:identifier ?identifier . " +
		"	?port dc:description ?desc ." +
		"	?port meandre:name ?name . " +
		"}" ;
	
	/** Retrieves all the available flows */
	private final static String QUERY_GET_ALL_FLOWS = 
		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
		"PREFIX meandre: <http://www.meandre.org/ontology/>\n"+
		"SELECT ?flow  " +
		"WHERE { " +
		"   ?flow rdf:type meandre:flow_component " +
		"}" ;

	/** Retrieve the main properties of a flow */
	private final static String QUERY_GET_FLOW_PROPERTIES = 
		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
		"PREFIX meandre: <http://www.meandre.org/ontology/>\n"+
		"PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"+
		"SELECT DISTINCT ?flow ?name ?creator ?date ?desc ?rights " +
		"WHERE { " +
		"	?flow ?p ?o . " +
		"   ?flow rdf:type meandre:flow_component . " +
		"	?flow meandre:name ?name ." +
		"	?flow dc:creator ?creator ." +
		"	?flow dc:date ?date . " +
		" 	?flow dc:description ?desc . " +
		"	?flow dc:rights ?rights " +
		"}" ;
	
	/** Retrieve the main instance properties of a flow */
	private final static String  QUERY_GET_FLOW_INSTANCE_PROPERTIES = 
		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
		"PREFIX meandre: <http://www.meandre.org/ontology/>\n"+
		"PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"+
		"SELECT DISTINCT ?eci ?key ?value " +
		"WHERE { " +
		"   ?eci rdf:type meandre:instance_configuration . " +
        "   ?eci meandre:property_set ?ps . " +
        "   ?ps rdf:type meandre:property . " +
        "   ?ps meandre:key ?key . " +
        "   ?ps meandre:value ?value  " +
		"}" ;
	
	/** The query retrieving all the components instaces required to execute the flow */
	private static final String  QUERY_GET_FLOW_INSTANCES = 
		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
		"PREFIX meandre: <http://www.meandre.org/ontology/>\n"+
		"PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"+
		"SELECT DISTINCT ?flow ?eci ?name ?resComp ?desc " +
		"WHERE { " +
		"	?flow ?p ?o . " +
        "   ?flow rdf:type meandre:flow_component . " +
        "   ?flow meandre:components_instances ?ci . " +
        "   ?ci meandre:executable_component_instance ?eci . " +
        "   ?eci rdf:type meandre:instance_configuration . " +
        "   ?eci meandre:instance_name ?name . " +
        "   ?eci meandre:instance_resource ?resComp . " +
        "   ?eci dc:description ?desc " +
		"}" ;
	
	/** This query retrieve all the connector informamation for a given flow */
	private final static String QUERY_GET_FLOW_CONNECTORS =
		  "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"+
		  "PREFIX meandre: <http://www.meandre.org/ontology/>\n"+
		  "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"+
		  "SELECT DISTINCT ?flow ?dataCon ?cis ?cidps ?cit ?cidpt "+
		  "WHERE {"+
		  "    ?flow ?p ?o . "+
		  "    ?flow rdf:type meandre:flow_component . "+
		  "    ?flow meandre:connectors ?cs ."+
		  "    ?cs rdf:type meandre:connector_set ."+
		  "    ?cs meandre:data_connector ?dataCon ."+
		  "    ?dataCon rdf:type meandre:data_connector_configuration ."+
		  "    ?dataCon meandre:connector_instance_source ?cis ."+
		  "    ?dataCon meandre:connector_instance_data_port_source ?cidps ."+
		  "    ?dataCon meandre:connector_instance_target ?cit ."+
		  "    ?dataCon meandre:connector_instance_data_port_target ?cidpt"+
		  "}";
	
	/** Get all the query tags. */
	private final static String QUERY_GET_FLOW_TAGS = 
		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
		"PREFIX meandre: <http://www.meandre.org/ontology/>\n"+
		"PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"+
		"SELECT DISTINCT ?flow ?tag " +
		"WHERE { " +
		"	?flow ?p ?o . " +
		"   ?flow rdf:type meandre:flow_component . " +
		"	?flow meandre:tag ?tag " +
		"}" ;
	
		
	/** The repository model */
	private Model model = null;
	
	/** The set of executable components available */
	protected Set<Resource> setComRes = null;
	
	/** The map from executable component resources to descriptions */
	protected Hashtable<Resource,ExecutableComponentDescription> htComDescMap = null;
	
	/** The set of flow components available */
	protected Set<Resource> setFlowRes = null;
	
	/** The map from flow component resources to descriptions */
	protected Hashtable<Resource,FlowDescription> htFlowDescMap = null;

	/** The text search indexer */
	private IndexBuilderString larqBuilder = null;

	/** the current text model */
	private IndexLARQ index = null;
	
	/** The tag information for components */
	protected Hashtable<String,Set<ExecutableComponentDescription>> htCompTags = null;
	
	/** The tag information for flows */
	protected Hashtable<String,Set<FlowDescription>> htFlowTags = null;
	
	/** Creates a new repository given the provided model.
	 * 
	 * @param mod The model containing the repository information
	 */
	public RepositoryImpl ( Model mod ) {
		// Initializing the repository
		log.info("Initializing the repository");
		
		this.model = mod;
		
		// Creating the basic caching mechanisms
		this.htComDescMap  = new Hashtable<Resource,ExecutableComponentDescription>();
		this.setComRes     = getAvailableExecutableComponentsFromModel();
		this.htFlowDescMap = new Hashtable<Resource,FlowDescription>();
		this.setFlowRes    = getAvailableFlowsFromModel();

		this.htCompTags    = new Hashtable<String,Set<ExecutableComponentDescription>>();
		this.htFlowTags    = new Hashtable<String,Set<FlowDescription>>();
		
		// Refresh the cached descriptions
		refreshCache();
	}
	
	/** Refreshes the cache of component descriptions available.
	 * 
	 */
	public void refreshCache () {
		// Flushing the cache
		log.info("Flushing the cached descriptions");
		htComDescMap.clear();
		setComRes.clear();
		htFlowDescMap.clear();
		setFlowRes.clear();
		htCompTags.clear();
		htFlowTags.clear();
		LARQ.removeDefaultIndex();

		log.info("Refreshing cached descriptions");
		// Query all the components
		setComRes = getAvailableExecutableComponentsFromModel();
		for ( Resource res:setComRes ) {
			try {
				ExecutableComponentDescription ecd = getExecutableComponentDescriptionFromModel(res);
				// Caching
				htComDescMap.put(res,ecd);
				// Adding the tags
				for ( String sTag:ecd.getTags().getTags() ) {
					Set<ExecutableComponentDescription> set = htCompTags.get(sTag);
					if ( set==null ) {
						set = new HashSet<ExecutableComponentDescription>();
						htCompTags.put(sTag,set);
					}
					set.add(ecd);
				}
			} catch (CorruptedDescriptionException e) {
				log.warning("Corrupted component found "+res.toString()+"\n"+e.toString());
			}
		}
		// Query all the flows
		setFlowRes = getAvailableFlowsFromModel();
		for ( Resource res:setFlowRes ) {
			try {
				FlowDescription fd =getFlowDescriptionFromModel(res);
				// Caching
				htFlowDescMap.put(res,fd);
				// Adding the tags
				for ( String sTag:fd.getTags().getTags() ) {
					Set<FlowDescription> set = htFlowTags.get(sTag);
					if ( set==null ) {
						set = new HashSet<FlowDescription>();
						htFlowTags.put(sTag,set);
					}
					set.add(fd);
				}
			} catch (CorruptedDescriptionException e) {
				log.warning("Corrupted flow found "+res.toString()+"\n"+e.toString());
			}
		}
		
		if ( model.size()==0 ) {
			model.createResource("http://www.meandre.org/ontology/")
			     .addProperty(ResourceFactory.createProperty("http://purl.org/dc/elements/1.1/description"), model.createTypedLiteral("This is the current repository aggregated using Meandre"))
			     .addProperty(ResourceFactory.createProperty("http://purl.org/dc/elements/1.1/date"), model.createTypedLiteral(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()),XSDDatatype.XSDdate));
		}
		
		// Read and index all literal strings.
		this.larqBuilder = new IndexBuilderString();
		
		// Create an index based on existing statements
		larqBuilder.indexStatements(model.listStatements());
		// Finish indexing
		larqBuilder.closeForWriting();
		// -- Create the access index  
		this.index = larqBuilder.getIndex() ;
		// -- Make globally available
		LARQ.setDefaultIndex(index);
		
	}
	
	/** Refreshes the cache of component descriptions available in the provided model.
	 * 
	 * @param modNew The new model to use as reprository
	 */
	public void refreshCache ( Model modNew ) {
		this.model = modNew;
		
		refreshCache();
	}

	/** Returns the current model used as a repository.
	 * 
	 * @return The model used as reprository
	 */
	public Model getModel () {
		return this.model;
	}

	/** Returns the set of availabble exececutable components from the model.
	 * 
	 * @return The set of resources describing the available components
	 */
	protected Set<Resource> getAvailableExecutableComponentsFromModel() {
		Set<Resource> hsRes = new HashSet<Resource>();
		
		Query query = QueryFactory.create(QUERY_GET_ALL_EXECUTABLE_COMPONENTS) ;
		QueryExecution exec = QueryExecutionFactory.create(query, model, null);
		ResultSet results = exec.execSelect();
		
		while ( results.hasNext() ) 
			hsRes.add(results.nextSolution().getResource("component"));
		
		return hsRes;
	}

	/** Returns the executable component description for the given resource from the model.
	 * 
	 * @param res The resource
	 * @return The description
	 * @throws CorruptedDescriptionException Corrupted description found
	 */
	protected ExecutableComponentDescription getExecutableComponentDescriptionFromModel ( Resource res ) throws CorruptedDescriptionException {
		// The basic data objects fill
		Resource resExecutableComponent = null;
		String sName = null;
		String sDescription = null;
		String sRights = null;
		String sCreator = null;
		Date dateCreation = null;
		String sRunnable = null;
		String sFiringPolicy = "all";
		String sFormat = null;
		Set<Resource> setContext  = null;
		Resource resLocation = null;
		Set<DataPortDescription> setInputs = null;
		Set<DataPortDescription> setOutputs = null;

		// Query the basic properties
		QuerySolutionMap qsmBindings = new QuerySolutionMap();
		qsmBindings.add("component", res);

		Query query = QueryFactory.create(QUERY_GET_EXECUTABLE_COMPONENT) ;
		QueryExecution exec = QueryExecutionFactory.create(query, model, qsmBindings);
		ResultSet results = exec.execSelect(); 
		
		QuerySolution resProps = results.nextSolution();
		if ( results.getRowNumber()>1 )
			throw new CorruptedDescriptionException("Executable component "+res+" appears to have multiple contradictory definitions");
		
		// ?component ?name ?creator ?date ?desc ?rights ?runnable ?format
		resExecutableComponent = resProps.getResource("component");
		sName = resProps.getLiteral("name").getLexicalForm();
		sCreator = resProps.getLiteral("creator").getLexicalForm();
		sDescription = resProps.getLiteral("desc").getLexicalForm();
		sRights = resProps.getLiteral("rights").getLexicalForm();
		sRunnable = resProps.getLiteral("runnable").getLexicalForm();
		sFormat = resProps.getLiteral("format").getLexicalForm();
		resLocation = resProps.getResource("location");
		try {
			dateCreation = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")).parse(resProps.getLiteral("date").getLexicalForm());
		} catch (ParseException e) {
			throw new CorruptedDescriptionException(e);
		}

		// Query tagging linked 
		Query queryTags = QueryFactory.create(QUERY_GET_EXECUTABLE_TAGS) ;
		QueryExecution execTags = QueryExecutionFactory.create(queryTags, model, qsmBindings);
		ResultSet resultsTags =  execTags.execSelect();
		
		Set<String> setTags = new HashSet<String>();
		while ( resultsTags.hasNext() ) {
			QuerySolution solTags = resultsTags.nextSolution();
			//?component ?tag
			setTags.add(solTags.getLiteral("tag").getLexicalForm());
		}
		TagsDescription tagDesc = new TagsDescription(setTags);
		
		// Query for properties
		Query queryProperties = QueryFactory.create(QUERY_GET_EXECUTABLE_PROPERTY_DEFINITIONS) ;
		QueryExecution execProperties = QueryExecutionFactory.create(queryProperties, model, qsmBindings);
		ResultSet resultsProperties =  execProperties.execSelect();
		
		Hashtable<String,String> htValues = new Hashtable<String,String>();
		Hashtable<String,String> htDescriptions = new Hashtable<String,String>();
		// ?component ?pname ?key ?value ?desc 
		while ( resultsProperties.hasNext() ) {
			QuerySolution sol = resultsProperties.nextSolution();
			String sKey = sol.getLiteral("key").getLexicalForm();
			htValues.put(sKey,sol.getLiteral("value").getLexicalForm());
			htDescriptions.put(sKey,sol.getLiteral("desc").getLexicalForm());
		}
		PropertiesDescriptionDefinition pddProperties = new PropertiesDescriptionDefinition(htValues,htDescriptions);
		
		// Query for context locations
		Query queryContext = QueryFactory.create(QUERY_GET_EXECUTABLE_COMPONENT_EXECUTION_CONTEXT) ;
		QueryExecution execContext = QueryExecutionFactory.create(queryContext, model, qsmBindings);
		ResultSet resultsContext =  execContext.execSelect();
		
		setContext = new HashSet<Resource>();
		while ( resultsContext.hasNext() ) {
			QuerySolution sol = resultsContext.nextSolution();
			// ?component ?context 
			setContext.add(sol.getResource("context"));
		}
		
		// Query for input data ports
		Query queryInputs = QueryFactory.create(QUERY_GET_EXECUTABLE_COMPONENT_INPUTS) ;
		QueryExecution execInputs = QueryExecutionFactory.create(queryInputs, model, qsmBindings);
		ResultSet resultsInputs = execInputs.execSelect();
		
		setInputs = new HashSet<DataPortDescription>();
		while ( resultsInputs.hasNext() )  {
			QuerySolution resIns = resultsInputs.nextSolution();
			// ?component ?port ?identifier ?name ?desc
			Resource resComp = resIns.getResource("port");
			String sIdentifier = resIns.getLiteral("identifier").getLexicalForm();
			String sInName = resIns.getLiteral("name").getLexicalForm();
			String sDesc = resIns.getLiteral("desc").getLexicalForm();
			setInputs.add(new DataPortDescription(resComp,sIdentifier,sInName,sDesc));
		}
		
		// If there is at least one input check for the firing criteria
		if ( setInputs.size()>0 ) {
			QuerySolutionMap qsmBindingsFiring = new QuerySolutionMap();
			qsmBindingsFiring.add("component", res);

			Query queryFiring = QueryFactory.create(QUERY_GET_INPUT_FIRING) ;
			QueryExecution execFiring = QueryExecutionFactory.create(queryFiring, model, qsmBindingsFiring);
			ResultSet resultsFiring = execFiring.execSelect();
		
			if ( !resultsFiring.hasNext() ) 
				log.info("Component "+res.toString()+" has not firing policy, assuming all" );
			else {
				QuerySolution qsFP = resultsFiring.nextSolution();
				if ( resultsFiring.getRowNumber()!=1 ) 
					throw new CorruptedDescriptionException("Multiple firing policies found for component "+res.toString()); 
				else	
					sFiringPolicy = qsFP.getLiteral("firing").getLexicalForm();
			}
		}
		
		// Query for output data ports
		Query queryOutputs = QueryFactory.create(QUERY_GET_EXECUTABLE_COMPONENT_OUTPUTS) ;
		QueryExecution execOutputs = QueryExecutionFactory.create(queryOutputs, model, qsmBindings);
		ResultSet resultsOutputs = execOutputs.execSelect();
		
		setOutputs = new HashSet<DataPortDescription>();
		while ( resultsOutputs.hasNext() )  {
			QuerySolution resIns = resultsOutputs.nextSolution();
			// ?component ?port ?identifier ?name ?desc
			Resource resComp = resIns.getResource("port");
			String sIdentifier = resIns.getLiteral("identifier").getLexicalForm();
			String sInName = resIns.getLiteral("name").getLexicalForm();
			String sDesc = resIns.getLiteral("desc").getLexicalForm();
			setOutputs.add(new DataPortDescription(resComp,sIdentifier,sInName,sDesc));
		}
		
		return new ExecutableComponentDescription(
				    resExecutableComponent,
					sName,
					sDescription,
					sRights,
					sCreator,
					dateCreation,
					sRunnable,
					sFiringPolicy,
					sFormat,
					setContext,
					resLocation,
					setInputs,
					setOutputs,
					pddProperties,
					tagDesc
				);
	}
	

	/** Returns the set of availabble flows in the repository from the model.
	 * 
	 * @return The set of resources describing the available flows
	 */
	protected Set<Resource> getAvailableFlowsFromModel() {
		Set<Resource> hsRes = new HashSet<Resource>();
		
		Query query = QueryFactory.create(QUERY_GET_ALL_FLOWS) ;
		QueryExecution exec = QueryExecutionFactory.create(query, model, null);
		ResultSet results = exec.execSelect();
		
		while ( results.hasNext() ) 
			hsRes.add(results.nextSolution().getResource("flow"));
		
		return hsRes;
	}
	
	/** Returns a description of the given resource from the model.
	 * 
	 * @param res The flow description to retrieve to retrieve
	 * @return The flow description
	 * @throws CorruptedDescriptionException The desctiption is corrupted
	 */
	protected FlowDescription getFlowDescriptionFromModel(Resource res) throws CorruptedDescriptionException {
		
		// Query the basic properties
		QuerySolutionMap qsmBindings = new QuerySolutionMap();
		qsmBindings.add("flow", res);

		Query query = QueryFactory.create(QUERY_GET_FLOW_PROPERTIES) ;
		QueryExecution exec = QueryExecutionFactory.create(query, model, qsmBindings);
		ResultSet results = exec.execSelect();
		
		QuerySolution resProps = results.nextSolution();
		if ( results.getRowNumber()>1 )
			throw new CorruptedDescriptionException("Executable component "+res+" appears to have multiple contradictory definitions");
		
		Resource resFlow = resProps.getResource("flow");
		String sName = resProps.getLiteral("name").getLexicalForm();
		String sCreator = resProps.getLiteral("creator").getLexicalForm();
		String sDescription = resProps.getLiteral("desc").getLexicalForm();
		String sRights = resProps.getLiteral("rights").getLexicalForm();
		Date dateCreation = null;
		try {
			dateCreation = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")).parse(resProps.getLiteral("date").getLexicalForm());
		} catch (ParseException e) {
			throw new CorruptedDescriptionException(e);
		}
		
		// Retrieve the tags linked to the flow
		Query queryTags = QueryFactory.create(QUERY_GET_FLOW_TAGS) ;
		QueryExecution execTags = QueryExecutionFactory.create(queryTags, model, qsmBindings);
		ResultSet resultsTags = execTags.execSelect();
		
		Set<String> setTags = new HashSet<String>();
		while ( resultsTags.hasNext() ) {
			QuerySolution solTags = resultsTags.nextSolution();
			// ?flow ?tag
			setTags.add(solTags.getLiteral("tag").getLexicalForm());
		}
		TagsDescription tagsDesc = new TagsDescription(setTags);
		
		// Retireve the instance list
		Query queryInstances = QueryFactory.create(QUERY_GET_FLOW_INSTANCES) ;
		QueryExecution execInstance = QueryExecutionFactory.create(queryInstances, model, qsmBindings);
		ResultSet resultsInstances = execInstance.execSelect();
		
		Set<ExecutableComponentInstanceDescription> setExecutableComponentInstances = new HashSet<ExecutableComponentInstanceDescription>();
		while ( resultsInstances.hasNext() ) {
			QuerySolution sol = resultsInstances.nextSolution();
			Resource resECI = sol.getResource("eci");
			String sInstanceName = sol.getLiteral("name").getLexicalForm();
			Resource resComponent = sol.getResource("resComp");
			String sInstanceDescription = sol.getLiteral("desc").getLexicalForm();
			
			// For each of them we need to get the properties
			// Crete the instance binding
			QuerySolutionMap qsmInstanceBindings = new QuerySolutionMap();
			qsmInstanceBindings.add("eci", resECI);

			// Retrieve the instance's properties
			Query queryInstanceProperties = QueryFactory.create(QUERY_GET_FLOW_INSTANCE_PROPERTIES) ;
			QueryExecution execInstanceProperties = QueryExecutionFactory.create(queryInstanceProperties, model, qsmInstanceBindings);
			ResultSet resultsInstanceProperties = execInstanceProperties.execSelect();
			
			Hashtable<String,String> htInsProp = new  Hashtable<String,String>();
			while ( resultsInstanceProperties.hasNext() ) {
				QuerySolution solProp = resultsInstanceProperties.nextSolution();
				// ?eci ?key ?value
				htInsProp.put(solProp.getLiteral("key").getLexicalForm(), solProp.getLiteral("value").getLexicalForm());
			}
			
			// Creating the instance of the executable component
			setExecutableComponentInstances.add(new ExecutableComponentInstanceDescription(resECI,resComponent,sInstanceName,sInstanceDescription,new PropertiesDescription(htInsProp)));	
		}
		
		// Retrieve the data port desciption
		Query queryConnectors = QueryFactory.create(QUERY_GET_FLOW_CONNECTORS) ;
		QueryExecution execConnectors = QueryExecutionFactory.create(queryConnectors, model, qsmBindings);
		ResultSet resultsConnectors = execConnectors.execSelect();

		Set<ConnectorDescription> setConnectorDescription = new HashSet<ConnectorDescription>();
		while ( resultsConnectors.hasNext() ) {
			QuerySolution sol = resultsConnectors.nextSolution();
			Resource resDataCon = sol.getResource("dataCon");
			Resource resComponentInstanceSource = sol.getResource("cis");
			Resource resComponentInstaceDataPortSource = sol.getResource("cidps");
			Resource resComponentInstanceTarget = sol.getResource("cit");
			Resource resComponentInstaceDataPortTarget = sol.getResource("cidpt");
			setConnectorDescription.add(new ConnectorDescription(resDataCon,resComponentInstanceSource,resComponentInstaceDataPortSource,resComponentInstanceTarget,resComponentInstaceDataPortTarget));
		}
		
		return new FlowDescription(resFlow,sName,sDescription,sRights,sCreator,dateCreation,setExecutableComponentInstances,setConnectorDescription,tagsDesc);
	}


	/** Returns the set of available executable components stored
	 * int the repository.
	 * 
	 * @return The set of executable components
	 */
	public Set<Resource> getAvailableExecutableComponents () {
		return setComRes;
	}
	
	/** Returns the set of available executable components stored
	 * int the repository that match the search criteria.
	 * 
	 * @param sQuery The string to search
	 * @return The set of executable components
	 */
	public Set<Resource> getAvailableExecutableComponents ( String sQuery ) {
		HashSet<Resource> setRes = new HashSet<Resource>();
		
		String QUERY_GET_ALL_SEARCHED_EXECUTABLE_COMPONENTS = 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
			"PREFIX meandre: <http://www.meandre.org/ontology/>\n"+
			"PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"+
			"PREFIX pf: <http://jena.hpl.hp.com/ARQ/property#>\n"+
			"SELECT DISTINCT ?component  " +
			"WHERE { " +
			"	{" +
			"      ?component ?p ?o . " +
			"      ?component rdf:type meandre:executable_component . " +
			"	   ?component meandre:tag ?tag . " +
			"      ?tag pf:textMatch '"+sQuery+"' " +
			"   }" +
			"   UNION" +
			"	{" +
			"      ?component ?p ?o . " +
			"      ?component rdf:type meandre:executable_component . " +
			"	   ?component dc:description ?desc . " +
			"      ?desc pf:textMatch '"+sQuery+"' " +
			"   }" +
			"   UNION" +
			"	{" +
			"      ?component ?p ?o . " +
			"      ?component rdf:type meandre:executable_component . " +
			"	   ?component meandre:name ?name . " +
			"      ?name pf:textMatch '"+sQuery+"' " +
			"   }" +
			"   UNION" +
			"	{" +
			"      ?component ?p ?o . " +
			"      ?component rdf:type meandre:executable_component . " +
			"	   ?component dc:creator ?creator . " +
			"      ?creator pf:textMatch '"+sQuery+"' " +
			"   }" +
			"}" ;
		
		// Retrieve the data port desciption
		Query query = QueryFactory.create(QUERY_GET_ALL_SEARCHED_EXECUTABLE_COMPONENTS) ;
		QueryExecution exec = QueryExecutionFactory.create(query, model, null);
		ResultSet results = exec.execSelect();

		while ( results.hasNext() ) {
			QuerySolution sol = results.nextSolution();
			setRes.add(sol.getResource("component"));
		}
		
		return setRes;
	}
	

	/** Returns the executable component description for the given resource.
	 * 
	 * @param res The resource
	 * @return The description
	 * @throws CorruptedDescriptionException Corrupted description found
	 */
	public ExecutableComponentDescription getExecutableComponentDescription ( Resource res ) {
		return htComDescMap.get(res);
	}
		
	/** Returns the set of availabble flows in the repository.
	 * 
	 * @return The set of resources describing the available flows
	 */
	public Set<Resource> getAvailableFlows() {
		return setFlowRes;
	}
	
	/** Returns the set of availabble flows in the repository that match the search criteria. The queries
	 * are based on Lucene syntax.
	 * 
	 * @param sQuery The query string
	 * @return The set of resources describing the available flows
	 */
	public Set<Resource> getAvailableFlows ( String sQuery ) {
		HashSet<Resource> setRes = new HashSet<Resource>();
		
		String QUERY_GET_ALL_SEARCHED_FLOWS = 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
			"PREFIX meandre: <http://www.meandre.org/ontology/>\n"+
			"PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"+
			"PREFIX pf: <http://jena.hpl.hp.com/ARQ/property#>\n"+
			"SELECT DISTINCT ?flow " +
			"WHERE { " +
			"	{" +
			"      ?flow ?p ?o . " +
			"      ?flow rdf:type meandre:flow_component . " +
			"	   ?flow meandre:tag ?tag . " +
			"      ?tag pf:textMatch '"+sQuery+"' " +
			"   }" +
			"   UNION" +
			"	{" +
			"      ?flow ?p ?o . " +
			"      ?flow rdf:type meandre:flow_component . " +
			"	   ?flow dc:description ?desc . " +
			"      ?desc pf:textMatch '"+sQuery+"' " +
			"   }" +
			"   UNION" +
			"	{" +
			"      ?flow ?p ?o . " +
			"      ?flow rdf:type meandre:flow_component . " +
			"	   ?flow meandre:name ?name . " +
			"      ?name pf:textMatch '"+sQuery+"' " +
			"   }" +
			"   UNION" +
			"	{" +
			"      ?flow ?p ?o . " +
			"      ?flow rdf:type meandre:flow_component . " +
			"	   ?flow dc:creator ?creator . " +
			"      ?creator pf:textMatch '"+sQuery+"' " +
			"   }" +
			"}" ;
		
		// Retrieve the data port desciption
		Query query = QueryFactory.create(QUERY_GET_ALL_SEARCHED_FLOWS) ;
		QueryExecution exec = QueryExecutionFactory.create(query, model, null);
		ResultSet results = exec.execSelect();

		while ( results.hasNext() ) {
			QuerySolution sol = results.nextSolution();
			setRes.add(sol.getResource("flow"));
		}
		
		return setRes;
	}
	
	/** Returns a description of the given resource.
	 * 
	 * @param res The flow description to retrieve to retrieve
	 * @return The flow description
	 * @throws CorruptedDescriptionException The desctiption is corrupted
	 */
	public FlowDescription getFlowDescription(Resource res) {
		return htFlowDescMap.get(res);
	}
	
	/** Returns the list of available tags in the repository.
	 * 
	 * @return The set of available tags.
	 */
	public Set<String> getTags () {
		HashSet<String> set = new HashSet<String>();

		set.addAll(htCompTags.keySet());
		set.addAll(htFlowTags.keySet());
		
		return set;
	}
	
	/** Returns the list of available component tags in the repository.
	 * 
	 * @return The set of available component tags.
	 */
	public Set<String> getComponentTags () {
		return htCompTags.keySet();
	}
	
	/** Returns the list of available flow tags in the repository.
	 * 
	 * @return The set of available flow tags.
	 */
	public Set<String> getFlowTags (){
		return htFlowTags.keySet();
	}
	
	/** Returns the set of executable components associated with this tag.
	 * 
	 * @param sTag The tag
	 * @return The set of components
	 */
	public Set<ExecutableComponentDescription> getComponentsByTag ( String sTag ) {
		return htCompTags.get(sTag);
	}

	/** Returns the set of flows associated with this tag.
	 * 
	 * @param sTag The tag
	 * @return The set of flows
	 */
	public Set<FlowDescription> getFlowsByTag ( String sTag ) {
		return htFlowTags.get(sTag);
	}

}
