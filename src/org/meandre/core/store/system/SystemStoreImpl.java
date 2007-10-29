package org.meandre.core.store.system;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.meandre.Bootstrapper;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;

/** This class provides a wrapper for a system store property storage.
 * It is backed up by a file and when modified the content flushes the
 * information to the disk.
 *
 * @author Xavier Llor&agrave;
 *
 */
public class SystemStoreImpl implements SystemStore {

	/** The base system url */
    private static final String BASE_SYSTEM_URL =
            "http://www.meandre.org/system/";

    /** The base system url */
    private static final String BASE_SYSTEM_URL_PROPERTY =
            "http://www.meandre.org/system/property/";

    /** Query for a property */
    private final static String QUERY_GET_ALL_SYSTEM_PROPERTIES =
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "PREFIX meandre-props: <" + BASE_SYSTEM_URL_PROPERTY + ">\n" +
            "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" +
            "SELECT DISTINCT ?prop ?value ?key ?desc " +
            "WHERE {  " +
            "   <" + BASE_SYSTEM_URL +
            "> meandre-props:property-instance ?prop . " +
            "   ?prop meandre-props:key ?key ." +
            "   ?prop meandre-props:value ?value ." +
            "   ?prop dc:description ?desc ." +
            "}";


    /** Query for a property keys */
    private final static String QUERY_GET_ALL_SYSTEM_PROPERTY_KEYS =
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "PREFIX meandre-props: <" + BASE_SYSTEM_URL_PROPERTY + ">\n" +
            "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" +
            "SELECT DISTINCT ?key " +
            "WHERE {  " +
            "   <" + BASE_SYSTEM_URL +
            "> meandre-props:property-instance ?prop . " +
            "   ?prop meandre-props:key ?key " +
            "}";

    /** The model repository */
    protected Model model = null;

    /** The logger for the bootstrapper */
    protected static Logger log = null;

    // Initializing the logger and its handlers
    static {
        log = Logger.getLogger(Bootstrapper.class.getName());
        log.setLevel(Level.CONFIG);
        log.addHandler(Bootstrapper.handler);
    }


    /** Create a system store for properties and configuration related stuff.
     *
     * @param nmodel the model containing the system store
     */
    public SystemStoreImpl( Model model ) {
        this.model = model;

        this.model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
        this.model.setNsPrefix("meandreSystem",
                               "http://www.meandre.org/system/property/");
        this.model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        this.model.setNsPrefix("dc", "http://purl.org/dc/elements/1.1/");

        if ( model.size()==0 ) {
        	log.info("Empty model. Initializing a clean one.");
            setProperty(REPOSITORY_LOCATION,"http://localhost:1714/public/services/repository.ttl","The locally published components");
        }

        flush();
    }

    /** Flushes the current system model to disk.
	 *
	 *
	 */
	 public void flush() {
        
    }


	 /** Add a property to the system store.
	 *
	 * @param sKey The key
	 * @param sValue The value
	 * @param sDesc The description of the property
	 */
	public void setProperty(String sKey, String sValue, String sDesc) {
        Resource res = model.createResource(BASE_SYSTEM_URL);
        Resource resProp = model.createResource();
        resProp.addProperty(ResourceFactory.createProperty(
                BASE_SYSTEM_URL_PROPERTY + "key"), sKey);
        resProp.addProperty(ResourceFactory.createProperty(
                BASE_SYSTEM_URL_PROPERTY + "value"), sValue);
        resProp.addProperty(ResourceFactory.createProperty(
                "http://purl.org/dc/elements/1.1/description"), sDesc);
        res.addProperty(ResourceFactory.createProperty(BASE_SYSTEM_URL_PROPERTY +
                "property-instance"), resProp);
    }

	/** Returns the values for the requested property.
	 *
	 * @param sKey The property name
	 */
	public Set<Hashtable<String, String>> getProperty(String sKey) {
        // Run the query
        Query query = QueryFactory.create(QUERY_GET_ALL_SYSTEM_PROPERTIES);
        QueryExecution exec = QueryExecutionFactory.create(query, model, null);
        ResultSet results = exec.execSelect();

        // Pack the results
        Set<Hashtable<String,
                String>> setRes = new HashSet<Hashtable<String, String>>();
        while (results.hasNext()) {
            QuerySolution sol = results.nextSolution();
            // ?value ?key ?desc
            Hashtable<String, String> ht = new Hashtable<String, String>();
            ht.put("key", sol.getLiteral("key").getLexicalForm());
            ht.put("value", sol.getLiteral("value").getLexicalForm());
            ht.put("description", sol.getLiteral("desc").getLexicalForm());
            setRes.add(ht);
        }

        return setRes;
    }


	/** Returns the set of stored properties.
	 *
	 * @return The set of property keys
	 */
	public Set<String> getPropertyKeys() {
	    // Run the query
        Query query = QueryFactory.create(QUERY_GET_ALL_SYSTEM_PROPERTY_KEYS);
        QueryExecution exec = QueryExecutionFactory.create(query, model, null);
        ResultSet results = exec.execSelect();

        // Pack the results
        Set<String> setRes = new HashSet<String>();
        while (results.hasNext()) {
            QuerySolution sol = results.nextSolution();
            // ?key ?desc
            setRes.add(sol.getLiteral("key").getLexicalForm());
        }

        return setRes;
	}


	/** Removes a property from the system model.
	 *
	 * @param sKey The key
	 * @param sValue The value to remove
	 */
	public void removeProperty(String sKey, String sValue) {
        // Bindings
        QuerySolutionMap qsmBindings = new QuerySolutionMap();
        qsmBindings.add("key", model.createLiteral(sKey));
        qsmBindings.add("value", model.createLiteral(sValue));

        // Run the query
        Query query = QueryFactory.create(QUERY_GET_ALL_SYSTEM_PROPERTIES);
        QueryExecution exec = QueryExecutionFactory.create(query, model,
                qsmBindings);
        ResultSet results = exec.execSelect();
        List<Statement> lst = new LinkedList<Statement>();
        while (results.hasNext()) {
            QuerySolution sol = results.nextSolution();
            lst.add(model.createStatement(
                    model.createResource(BASE_SYSTEM_URL),
                    ResourceFactory.createProperty(BASE_SYSTEM_URL_PROPERTY +
                    "property-instance"),
                    sol.getResource("prop")
                    )
                    );
            lst.add(model.createStatement(
                    sol.getResource("prop"),
                    ResourceFactory.createProperty(BASE_SYSTEM_URL_PROPERTY +
                    "key"),
                    sol.getLiteral("key")
                    )
                    );
            lst.add(model.createStatement(
                    sol.getResource("prop"),
                    ResourceFactory.createProperty(BASE_SYSTEM_URL_PROPERTY +
                    "value"),
                    sol.getLiteral("value")
                    )
                    );
            lst.add(model.createStatement(
                    sol.getResource("prop"),
                    ResourceFactory.createProperty(
                    "http://purl.org/dc/elements/1.1/description"),
                    sol.getLiteral("desc")
                    )
                    );
        }
        model.remove(lst);
    }

    /** Returns the current model used as a system repository.
     *
     * @return The model used as reprository
     */
    public Model getModel() {
        return this.model;
    }
}
