package org.meandre.core.repository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.meandre.core.logger.KernelLoggerFactory;
import org.meandre.core.utils.vocabulary.RepositoryVocabulary;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.larq.IndexBuilderString;
import com.hp.hpl.jena.query.larq.IndexLARQ;
import com.hp.hpl.jena.query.larq.LARQ;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;

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

	/** The core root logger */
	protected static Logger log = KernelLoggerFactory.getCoreLogger();

	static {

		//
		// Initialize the sets
		//

		// Runnable
		setRunnable.add("java");
		setRunnable.add("python");
		setRunnable.add("lisp");

		// Format
		setFormat.add("java/class");
		setFormat.add("jython");
		setFormat.add("clojure");

		// Firing policy
		setFiringPolicy.add("any");
		setFiringPolicy.add("all");
	}

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

	/** The current text model */
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
		log.finer("Initializing the repository");

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
		log.fine("Refreshing cached descriptions");
		htComDescMap.clear();
		setComRes.clear();
		htFlowDescMap.clear();
		setFlowRes.clear();
		htCompTags.clear();
		htFlowTags.clear();

		LARQ.removeDefaultIndex();

		log.finer("Refreshing cached descriptions");
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
				FlowDescription fd = getFlowDescriptionFromModel(res);
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
			     .addProperty(DC.description, model.createTypedLiteral("This is the current repository aggregated using Meandre"))
			     .addProperty(DC.date, model.createTypedLiteral(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()),XSDDatatype.XSDdateTime));
		}

		// Read and index all literal strings.
		// The non-working demo below shows how to divert the LARQ index to a file instead of memory based
		// this.larqBuilder = new IndexBuilderString(new File(Store.getRunResourcesDirectory()+File.separator+Store.getRepositoryIndexFile()));
		//
		this.larqBuilder = new IndexBuilderString();

		// Create an index based on existing statements
		larqBuilder.indexStatements(model.listStatements(null,DC.description,(RDFNode)null));
		larqBuilder.indexStatements(model.listStatements(null,DC.rights,(RDFNode)null));
		larqBuilder.indexStatements(model.listStatements(null,DC.creator,(RDFNode)null));
		larqBuilder.indexStatements(model.listStatements(null,RepositoryVocabulary.name,(RDFNode)null));
		larqBuilder.indexStatements(model.listStatements(null,RepositoryVocabulary.tag,(RDFNode)null));

		//larqBuilder.indexStatements(model.listStatements());

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
		ResIterator subjs = model.listSubjectsWithProperty(RDF.type,RepositoryVocabulary.executable_component);

		while ( subjs.hasNext() )
			hsRes.add(subjs.nextResource());

		return hsRes;

		/* The original SPARQL version

		private final static String QUERY_GET_ALL_EXECUTABLE_COMPONENTS =
		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
		"PREFIX meandre: <http://www.meandre.org/ontology/>\n"+
		"SELECT ?component  " +
		"WHERE { ?component rdf:type meandre:executable_component }" ;

		Set<Resource> hsRes = new HashSet<Resource>();
		Query query = QueryFactory.create(QUERY_GET_ALL_EXECUTABLE_COMPONENTS) ;
		QueryExecution exec = QueryExecutionFactory.create(query, model, null);
		ResultSet results = exec.execSelect();

		while ( results.hasNext() )
			hsRes.add(results.nextSolution().getResource("component"));

		return hsRes;
		*/
	}

	/** Returns the literals matching that particular subject/predicate combination.
	 *
	 * @param resSubj The subject resource
	 * @param prop The predicate property
	 * @param bCheckUnique Force a check for a unique value
	 * @return Return the list of matching literals
	 * @throws CorruptedDescriptionException A unique check was placed and more than one value was found
	 */
	@SuppressWarnings("unchecked")
	private List<Literal> getLiteralsFromModel ( Resource resSubj, Property prop, boolean bCheckUnique )
	throws CorruptedDescriptionException {
		List list = model.listObjectsOfProperty(resSubj, prop).toList();
		if ( bCheckUnique && list.size()!=1 )
			throw new CorruptedDescriptionException("Not an unique value "+list);
		return list;
	}


	/** Returns the resources matching that particular subject/predicate combination.
	 *
	 * @param resSubj The subject resource
	 * @param prop The predicate property
	 * @param bCheckUnique Force a check for a unique value
	 * @return Return the list of matching resources
	 * @throws CorruptedDescriptionException A unique check was placed and more than one value was found
	 */
	@SuppressWarnings("unchecked")
	private List<Resource> getObjectResourcesFromModel ( Resource resSubj, Property prop, boolean bCheckUnique )
	throws CorruptedDescriptionException {
		List list = model.listObjectsOfProperty(resSubj, prop).toList();
		if ( bCheckUnique && list.size()!=1 )
			throw new CorruptedDescriptionException("Not an unique value "+list);
		return list;
	}


	/** Returns the resources matching that particular subject/predicate combination as RDFNodes.
	 *
	 * @param resSubj The subject resource
	 * @param prop The predicate property
	 * @param bCheckUnique Force a check for a unique value
	 * @return Return the list of matching RDFNodes
	 * @throws CorruptedDescriptionException A unique check was placed and more than one value was found
	 */
	@SuppressWarnings("unchecked")
	private List<RDFNode> getObjectResourcesFromModelAsRDFNodes ( Resource resSubj, Property prop, boolean bCheckUnique )
	throws CorruptedDescriptionException {
		List list = model.listObjectsOfProperty(resSubj, prop).toList();
		if ( bCheckUnique && list.size()!=1 )
			throw new CorruptedDescriptionException("Not an unique value "+list);
		return list;
	}

	/** Returns the resources matching that particular predicate/object resource combination.
	 *
	 * @param prop The predicate property
	 * @param resObj The object resource
	 * @param bCheckUnique Force a check for a unique value
	 * @return Return the list of matching resources
	 * @throws CorruptedDescriptionException A unique check was placed and more than one value was found
	 */
	@SuppressWarnings("unchecked")
	private List<Resource> getSubjectResourcesFromModel ( Property prop, Resource resObj, boolean bCheckUnique )
	throws CorruptedDescriptionException {
		List list = model.listSubjectsWithProperty(prop,resObj).toList();
		if ( bCheckUnique && list.size()!=1 )
			throw new CorruptedDescriptionException("Not an unique value "+list);
		return list;
	}


	/** Returns the resources matching that particular predicate/object literal combination.
	 *
	 * @param prop The predicate property
	 * @param litObj The object literal
	 * @param bCheckUnique Force a check for a unique value
	 * @return Return the list of matching resources
	 * @throws CorruptedDescriptionException A unique check was placed and more than one value was found
	 */
	@SuppressWarnings("unchecked")
	private List<Resource> getSubjectResourcesFromModel ( Property prop, Literal litObj, boolean bCheckUnique )
	throws CorruptedDescriptionException {
		List list = model.listSubjectsWithProperty(prop,litObj).toList();
		if ( bCheckUnique && list.size()!=1 )
			throw new CorruptedDescriptionException("Not an unique value "+list);
		return list;
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
		Set<RDFNode> setContext  = null;
		Resource resLocation = null;
		Set<DataPortDescription> setInputs = null;
		Set<DataPortDescription> setOutputs = null;
		Resource resMode = ExecutableComponentDescription.COMPUTE_COMPONENT;

		// Check the resource is a component
		if ( model.listStatements(res,RDF.type,RepositoryVocabulary.executable_component).toList().size()!=1 )
			throw new CorruptedDescriptionException("Not a executable component: "+res);

		/* Original query set bindings for the SPARQL queries
		QuerySolutionMap qsmBindings = new QuerySolutionMap();
		qsmBindings.add("component", res);
		*/

		/* The original SPARQL version
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
		*/

		//
		// Query the basic properties
		//
		resExecutableComponent = res;
		sName = getLiteralsFromModel(res,RepositoryVocabulary.name,true).iterator().next().getLexicalForm();
		sCreator = getLiteralsFromModel(res,DC.creator,true).iterator().next().getLexicalForm();
		sDescription = getLiteralsFromModel(res,DC.description,true).iterator().next().getLexicalForm();
		sRights = getLiteralsFromModel(res,DC.rights,true).iterator().next().getLexicalForm();
		sRunnable = getLiteralsFromModel(res,RepositoryVocabulary.runnable,true).iterator().next().getLexicalForm();
		sFormat = getLiteralsFromModel(res,DC.format,true).iterator().next().getLexicalForm();
		resLocation = getObjectResourcesFromModel(res,RepositoryVocabulary.resource_location,true).iterator().next();
		try {
			dateCreation = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")).parse(getLiteralsFromModel(res,DC.date,true).iterator().next().getLexicalForm());
		} catch (ParseException e) {
			throw new CorruptedDescriptionException(e);
		}

		// Check the mode
		List<Resource> ls = getObjectResourcesFromModel(res,RepositoryVocabulary.mode,false);
		if ( ls.size()==1 )
			resMode = ls.get(0);
		else if ( ls.size()>1 )
			throw new CorruptedDescriptionException("Multiple modes for component "+res+": "+ls);

		/* Original SPARQL version
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
		*/

		//
		// Get the component tags
		//
		Set<String> setTags = new HashSet<String>();
		for ( Literal lit:getLiteralsFromModel(res, RepositoryVocabulary.tag, false) )
			setTags.add(lit.getLexicalForm());
		TagsDescription tagDesc = new TagsDescription(setTags);



		/* Retrieve the properties for a given component.
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
		*/

		//
		// Query for properties
		//
		Hashtable<String,String> htValues = new Hashtable<String,String>();
		Hashtable<String,String> htDescriptions = new Hashtable<String,String>();
		for ( Resource resProp:getObjectResourcesFromModel(res, RepositoryVocabulary.property_set, false) ) {
			String sKey = getLiteralsFromModel(resProp, RepositoryVocabulary.key, true).iterator().next().getLexicalForm();
			htValues.put(sKey,getLiteralsFromModel(resProp, RepositoryVocabulary.value, true).iterator().next().getLexicalForm());
			htDescriptions.put(sKey,getLiteralsFromModel(resProp, DC.description, true).iterator().next().getLexicalForm());
		}
		PropertiesDescriptionDefinition pddProperties = new PropertiesDescriptionDefinition(htValues,htDescriptions);


		/* Original SPARQL version
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

		Query queryContext = QueryFactory.create(QUERY_GET_EXECUTABLE_COMPONENT_EXECUTION_CONTEXT) ;
		QueryExecution execContext = QueryExecutionFactory.create(queryContext, model, qsmBindings);
		ResultSet resultsContext =  execContext.execSelect();

		setContext = new HashSet<Resource>();
		while ( resultsContext.hasNext() ) {
			QuerySolution sol = resultsContext.nextSolution();
			// ?component ?context
			setContext.add(sol.getResource("context"));
		}
		*/

		//
		// Query for context locations
		//
		// TODO: This should be smarter to distinguish resources and literals
		//
		setContext = new HashSet<RDFNode>();
		for ( RDFNode rdfnodeLoc:getObjectResourcesFromModelAsRDFNodes(res,RepositoryVocabulary.execution_context,false))
			setContext.add(rdfnodeLoc);


		/* The original SPARQL version
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
		*/

		// Query for input data ports
		setInputs = new HashSet<DataPortDescription>();
		for ( Resource resPort:getObjectResourcesFromModel(res, RepositoryVocabulary.input_data_port, false) ) {
			String sIdentifier = getLiteralsFromModel(resPort, DC.identifier, true).iterator().next().getLexicalForm();
			String sInName = getLiteralsFromModel(resPort, RepositoryVocabulary.name, true).iterator().next().getLexicalForm();
			String sDesc = getLiteralsFromModel(resPort, DC.description, true).iterator().next().getLexicalForm();
			setInputs.add(new DataPortDescription(resPort,sIdentifier,sInName,sDesc));
		}

		// If there is at least one input check for the firing criteria
		if ( setInputs.size()>0 ) {

			/* Original SPARQL version
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
			*/

			List<Literal> lstLit = getLiteralsFromModel(res, RepositoryVocabulary.firing_policy, true);
			if ( lstLit.size()==0 )
				log.info("Component "+res.toString()+" has not firing policy, assuming all" );
			else
				sFiringPolicy = lstLit.iterator().next().getLexicalForm();

		}


		/* Original SPARQL version

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
		*/

		// Query for output data ports
		setOutputs = new HashSet<DataPortDescription>();
		for ( Resource resPort:getObjectResourcesFromModel(res, RepositoryVocabulary.output_data_port, false) ) {
			String sIdentifier = getLiteralsFromModel(resPort, DC.identifier, true).iterator().next().getLexicalForm();
			String sOutName = getLiteralsFromModel(resPort, RepositoryVocabulary.name, true).iterator().next().getLexicalForm();
			String sDesc = getLiteralsFromModel(resPort, DC.description, true).iterator().next().getLexicalForm();
			setOutputs.add(new DataPortDescription(resPort,sIdentifier,sOutName,sDesc));
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
					tagDesc,
					resMode
				);
	}


	/** Returns the set of availabble flows in the repository from the model.
	 *
	 * @return The set of resources describing the available flows
	 */
	protected Set<Resource> getAvailableFlowsFromModel() {
		Set<Resource> hsRes = new HashSet<Resource>();
		ResIterator subjs = model.listSubjectsWithProperty(RDF.type,RepositoryVocabulary.flow_component);

		while ( subjs.hasNext() )
			hsRes.add(subjs.nextResource());

		return hsRes;

		/* The original SPARQL version

		private final static String QUERY_GET_ALL_FLOWS =
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
			"PREFIX meandre: <http://www.meandre.org/ontology/>\n"+
			"SELECT ?flow  " +
			"WHERE { " +
			"   ?flow rdf:type meandre:flow_component " +
			"}" ;

		Set<Resource> hsRes = new HashSet<Resource>();

		Query query = QueryFactory.create(QUERY_GET_ALL_FLOWS) ;
		QueryExecution exec = QueryExecutionFactory.create(query, model, null);
		ResultSet results = exec.execSelect();

		while ( results.hasNext() )
			hsRes.add(results.nextSolution().getResource("flow"));

		return hsRes;

		*/
	}

	/** Returns a description of the given resource from the model.
	 *
	 * @param res The flow description to retrieve
	 * @return The flow description
	 * @throws CorruptedDescriptionException The desctiption is corrupted
	 */
	protected FlowDescription getFlowDescriptionFromModel(Resource res) throws CorruptedDescriptionException {

		// Check the resource is a component
		if ( model.listStatements(res,RDF.type,RepositoryVocabulary.flow_component).toList().size()!=1 )
			throw new CorruptedDescriptionException("Not a flow: "+res);

		/* The original SPARQL version

		// Query the basic properties
		QuerySolutionMap qsmBindings = new QuerySolutionMap();
		qsmBindings.add("flow", res);
		*/

		/* The original SPARQL version

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
		*/

		//
		// Query the basic properties
		//
		Resource resFlow = res;
		String sName = getLiteralsFromModel(res,RepositoryVocabulary.name,true).iterator().next().getLexicalForm();
		String sCreator = getLiteralsFromModel(res,DC.creator,true).iterator().next().getLexicalForm();
		String sDescription = getLiteralsFromModel(res,DC.description,true).iterator().next().getLexicalForm();
		String sRights = getLiteralsFromModel(res,DC.rights,true).iterator().next().getLexicalForm();
		Date dateCreation = null;
		try {
			dateCreation = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")).parse(getLiteralsFromModel(res,DC.date,true).iterator().next().getLexicalForm());
		} catch (ParseException e) {
			throw new CorruptedDescriptionException(e);
		}


		/* The original SPARQL version

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
		*/

		//
		// Get the flow tags
		//
		Set<String> setTags = new HashSet<String>();
		for ( Literal lit:getLiteralsFromModel(res, RepositoryVocabulary.tag, false) )
			setTags.add(lit.getLexicalForm());
		TagsDescription tagsDesc = new TagsDescription(setTags);


		/* The original SPARQL version

		// The query retrieving all the components instaces required to execute the flow
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

		// Retrieve the main instance properties of a flow
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
		*/

		//
		// Retireve the instance list
		//
		Set<ExecutableComponentInstanceDescription> setExecutableComponentInstances = new HashSet<ExecutableComponentInstanceDescription>();
		List<Resource> lstInstances = getObjectResourcesFromModel(res, RepositoryVocabulary.components_instances, false);
		if ( lstInstances.size()>0 ) {
			Resource resCI = lstInstances.iterator().next();
			for ( Resource resECI:getObjectResourcesFromModel(resCI, RepositoryVocabulary.executable_component_instance, false) ) {
				String sInstanceName = getLiteralsFromModel(resECI, RepositoryVocabulary.instance_name, true).iterator().next().getLexicalForm();
				Resource resComponent = getObjectResourcesFromModel(resECI, RepositoryVocabulary.instance_resource, true).iterator().next();
				String sInstanceDescription = getLiteralsFromModel(resECI, DC.description, true).iterator().next().getLexicalForm();

				// For each of them we need to get the properties
				// Crete the instance binding
				QuerySolutionMap qsmInstanceBindings = new QuerySolutionMap();
				qsmInstanceBindings.add("eci", resECI);

				// Retrieve the instance's properties
				Hashtable<String,String> htInsProp = new  Hashtable<String,String>();
		        for ( Resource resProp:getObjectResourcesFromModel(resECI, RepositoryVocabulary.property_set, false) ) {
		        	String sKey = getLiteralsFromModel(resProp, RepositoryVocabulary.key, true).iterator().next().getLexicalForm();
		        	String sValue = getLiteralsFromModel(resProp, RepositoryVocabulary.value, true).iterator().next().getLexicalForm();
		        	htInsProp.put(sKey,sValue);
		        }

				// Creating the instance of the executable component
				setExecutableComponentInstances.add(new ExecutableComponentInstanceDescription(resECI,resComponent,sInstanceName,sInstanceDescription,new PropertiesDescription(htInsProp)));
			}
		}

		/* The original SPARQL version

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


		// Retrieve the data port connector desciption
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
		*/

		//
		// Retrieve the data port connector desciption
		//
		Set<ConnectorDescription> setConnectorDescription = new HashSet<ConnectorDescription>();
		List<Resource> lstRes = getSubjectResourcesFromModel(RDF.type, RepositoryVocabulary.data_connector_configuration, false);
		List<Resource> lstCS = getObjectResourcesFromModel(res, RepositoryVocabulary.connectors, false);
		Resource resCS = null;
		if ( lstCS.size()>0 ) {
			resCS = lstCS.iterator().next();
			for ( Resource resDataCon:lstRes ) {
				if  ( model.contains(resCS,RepositoryVocabulary.data_connector,resDataCon) ) {
					Resource resComponentInstanceSource = getObjectResourcesFromModel(resDataCon, RepositoryVocabulary.connector_instance_source, true).iterator().next();
					Resource resComponentInstaceDataPortSource = getObjectResourcesFromModel(resDataCon, RepositoryVocabulary.connector_instance_data_port_source, true).iterator().next();
					Resource resComponentInstanceTarget = getObjectResourcesFromModel(resDataCon, RepositoryVocabulary.connector_instance_target, true).iterator().next();
					Resource resComponentInstaceDataPortTarget = getObjectResourcesFromModel(resDataCon, RepositoryVocabulary.connector_instance_data_port_target, true).iterator().next();
					setConnectorDescription.add(new ConnectorDescription(resDataCon,resComponentInstanceSource,resComponentInstaceDataPortSource,resComponentInstanceTarget,resComponentInstaceDataPortTarget));
				}
			}
		}

		return new FlowDescription(resFlow,sName,sDescription,sRights,sCreator,dateCreation,setExecutableComponentInstances,setConnectorDescription,tagsDesc);
	}


	/** Returns the set of available executable components stored
	 * in the repository.
	 *
	 * @return The set of executable components
	 */
	public Set<Resource> getAvailableExecutableComponents () {
		return setComRes;
	}
	

    /**
     * Creates a list of components sorted by either date or name and returns the first N (='limit') values
     * @param sOrder The sorting order ("date" or "name" for now) - or null if no sorting required
     * @param limit  The maximum number of values to be returned (or -1 if no limit)
     * @return The list of components
     */
    public Collection<Resource> getAvailableExecutableComponentsOrderedBy(String sOrder, int limit) {
        String sOrderBy = "";
        if ( sOrder != null ) {
            if ( sOrder.equals("date") )
                sOrderBy = "DESC(?date) ?name";
            else if ( sOrder.equals("name") )
                sOrderBy = "?name DESC(?date)";
        }

        final String QUERY =
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
            "PREFIX meandre: <http://www.meandre.org/ontology/>\n"+
            "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
            "SELECT DISTINCT ?component " +
            "WHERE {" +
            "   ?component rdf:type meandre:executable_component ." +
            "   ?component meandre:name ?name ." +
            "   ?component dc:date ?date " +
            "} " +
            (sOrder != null ? "ORDER BY " + sOrderBy + " " : "") +
            (limit >= 0 ? "LIMIT " + limit : "");

        Query query = QueryFactory.create(QUERY) ;
        QueryExecution exec = QueryExecutionFactory.create(query, model, null);
        ResultSet results = exec.execSelect();

        Collection<Resource> lstComponents = new LinkedList<Resource>();
        while ( results.hasNext() )
            lstComponents.add(results.nextSolution().getResource("component"));

        return lstComponents;
    }


	/** Returns the set of available executable components stored
	 * in the repository.
	 *
	 * @return The set of executable components
	 */
	public Set<ExecutableComponentDescription> getAvailableExecutableComponentDescriptions () {
		Set<ExecutableComponentDescription> setFD = new HashSet<ExecutableComponentDescription>();

		for ( Resource res:getAvailableExecutableComponents() )
			setFD.add(getExecutableComponentDescription(res));

		return setFD;
	}

	/** Returns the map of available executable components descriptions stored
	 * in the repository.
	 *
	 * @return The set of executable component descriptions
	 */
	public Map<String,ExecutableComponentDescription> getAvailableExecutableComponentDescriptionsMap () {
		Map<String,ExecutableComponentDescription> mapECD = new HashMap<String,ExecutableComponentDescription>();

		for ( Resource res:getAvailableExecutableComponents() )
			mapECD.put(res.toString(),getExecutableComponentDescription(res));

		return mapECD;
	}


	/** Returns the set of available executable components stored
	 * in the repository that match the search criteria.
	 *
	 * @param sQuery The string to search
	 * @return The set of executable components
	 */
	public Set<Resource> getAvailableExecutableComponents ( String sQuery ) {

		/* Old SPARQL version
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
	    */

		HashSet<Resource> setRes = new HashSet<Resource>();

		NodeIterator ni = index.searchModelByIndex(sQuery);

		while ( ni.hasNext() ) {
			Literal lit = (Literal) ni.nextNode();
			try {
				for ( Resource res:getSubjectResourcesFromModel(null, lit, false) ) {
					if ( model.contains(res,RDF.type,RepositoryVocabulary.executable_component))
						setRes.add(res);
				}
			} catch (CorruptedDescriptionException e) {
				// No execption will be thrown because of the false flag
			}
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
	
    /**
     * Creates a list of flows sorted by either date or name and returns the first N (='limit') values
     * @param sOrder The sorting order ("date" or "name" for now) - or null if no sorting required
     * @param limit  The maximum number of values to be returned (or -1 if no limit)
     * @return The list of flows
     */
    public Collection<Resource> getAvailableFlowsOrderedBy(String sOrder, int limit) {
        String sOrderBy = "";
        if ( sOrder != null ) {
            if ( sOrder.equals("date") )
                sOrderBy = "DESC(?date) ?name";
            else if ( sOrder.equals("name") )
                sOrderBy = "?name DESC(?date)";
        }

        final String QUERY =
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
            "PREFIX meandre: <http://www.meandre.org/ontology/>\n"+
            "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
            "SELECT DISTINCT ?flow " +
            "WHERE {" +
            "   ?flow rdf:type meandre:flow_component ." +
            "   ?flow meandre:name ?name ." +
            "   ?flow dc:date ?date " +
            "} " +
            (sOrder != null ? "ORDER BY " + sOrderBy + " " : "") +
            (limit >= 0 ? "LIMIT " + limit : "");

        Query query = QueryFactory.create(QUERY) ;
        QueryExecution exec = QueryExecutionFactory.create(query, model, null);
        ResultSet results = exec.execSelect();

        Collection<Resource> lstFlows = new ArrayList<Resource>();
        while ( results.hasNext() )
            lstFlows.add(results.nextSolution().getResource("flow"));

        return lstFlows;
    }



	/** Returns the set of available flows descriptions in the repository.
	 *
	 * @return The set of resources describing the available flows
	 */
	public Set<FlowDescription> getAvailableFlowDescriptions() {
		Set<FlowDescription> setFD = new HashSet<FlowDescription>();

		for ( Resource res:getAvailableFlows() )
			setFD.add(getFlowDescription(res));

		return setFD;
	}


	/** Returns the map of available flows descriptions in the repository.
	 *
	 * @return The set of resources describing the available flows
	 */
	public Map<String,FlowDescription> getAvailableFlowDescriptionsMap() {
		Map<String,FlowDescription> mapFD = new HashMap<String,FlowDescription>();

		for ( Resource res:getAvailableFlows() )
			mapFD.put(res.toString(),getFlowDescription(res));

		return mapFD;
	}

	/** Returns the set of availabble flows in the repository that match the search criteria. The queries
	 * are based on Lucene syntax.
	 *
	 * @param sQuery The query string
	 * @return The set of resources describing the available flows
	 */
	public Set<Resource> getAvailableFlows ( String sQuery ) {
		/* Original SPARQL version

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
		*/

		HashSet<Resource> setRes = new HashSet<Resource>();

		NodeIterator ni = index.searchModelByIndex(sQuery);

		while ( ni.hasNext() ) {
			Literal lit = (Literal) ni.nextNode();
			try {
				for ( Resource res:getSubjectResourcesFromModel(null, lit, false) ) {
					if ( model.contains(res,RDF.type,RepositoryVocabulary.flow_component))
						setRes.add(res);
				}
			} catch (CorruptedDescriptionException e) {
				// No execption will be thrown because of the false flag
			}
		}

		return setRes;
	}

	/** Returns a description of the given resource.
	 *
	 * @param res The flow description to retrieve
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

	/** Returns the map of available component tags in the repository.
	 *
	 * @return The set of available component tags.
	 */
	public Map<String,Set<ExecutableComponentDescription>> getComponentTagsMap () {
		Map<String,Set<ExecutableComponentDescription>> mapRes = new HashMap<String,Set<ExecutableComponentDescription>> ();

		for ( String sTag:htCompTags.keySet() ) {
			HashSet<ExecutableComponentDescription> hs = new HashSet<ExecutableComponentDescription>();
			for ( ExecutableComponentDescription ecd:getComponentsByTag(sTag) )
				hs.add(ecd);
			mapRes.put(sTag, hs);
		}

		return mapRes;
	}


	/** Returns the list of available flow tags in the repository.
	 *
	 * @return The set of available flow tags.
	 */
	public Set<String> getFlowTags (){
		return htFlowTags.keySet();
	}

	/** Returns the map of available flow tags in the repository.
	 *
	 * @return The map of available flow tags.
	 */
	public Map<String,Set<FlowDescription>> getFlowTagsMap (){
		Map<String,Set<FlowDescription>> mapRes = new HashMap<String,Set<FlowDescription>> ();

		for ( String sTag:htFlowTags.keySet() ) {
			HashSet<FlowDescription> hs = new HashSet<FlowDescription>();
			for ( FlowDescription fd:getFlowsByTag(sTag) )
				hs.add(fd);
			mapRes.put(sTag, hs);
		}

		return mapRes;
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

	public Collection<Resource> getgetAvailableExecutableComponentsOrderedBy(
			String order, int limit) {
		// TODO Auto-generated method stub
		return null;
	}

	

}
