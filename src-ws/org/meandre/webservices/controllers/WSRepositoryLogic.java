package org.meandre.webservices.controllers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONArray;
import org.json.JSONObject;
import org.meandre.core.store.Store;
import org.meandre.core.store.repository.ExecutableComponentDescription;
import org.meandre.core.store.repository.FlowDescription;
import org.meandre.core.store.repository.QueryableRepository;
import org.meandre.core.store.repository.RepositoryImpl;
import org.meandre.core.store.system.SystemStore;
import org.meandre.webservices.utils.WSLoggerFactory;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/** This class groups all the basic logic required to maintain the locations
 * for the Meandre webservices interface.
 *
 * @author Xavier Llor&agrave;
 *
 */
public class WSRepositoryLogic {

	/** The logger for the WebServices */
	private static Logger log = WSLoggerFactory.getWSLogger();

    /** Regenerates a user repository using the current locations for the user.
	 *
	 * @param sUser The system store user
	 * @param sLocation The location to remove
	 * @return True if the location could be successfully removed
	 */
	public static boolean regenerateRepository(String sUser) {
		boolean bRes = true;

		//
		// Regenerate the users repository
		//
		QueryableRepository qr = Store.getRepositoryStore(sUser);

		// Cleaning the user repository entries
		Model mod = qr.getModel();
		mod.begin();
		mod.removeAll();
		mod.commit();

		// Regenerating the user repository entries
		SystemStore ss = Store.getSystemStore(sUser);
		Set<Hashtable<String, String>> setProps = ss.getProperty(SystemStore.REPOSITORY_LOCATION);
		for ( Hashtable<String, String> ht:setProps ) {
			String sLoc = ht.get("value");
			try {
				URL url = new URL(sLoc);
				Model modelTmp = ModelFactory.createDefaultModel();

				modelTmp.setNsPrefix("", "http://www.meandre.org/ontology/");
				modelTmp.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
				modelTmp.setNsPrefix("rdfs","http://www.w3.org/2000/01/rdf-schema#");
				modelTmp.setNsPrefix("dc","http://purl.org/dc/elements/1.1/");

				//
				// Read the location and check its consistency
				//
				if ( sLoc.endsWith(".ttl"))
					modelTmp.read(url.openStream(),null,"TTL");
				else if ( sLoc.endsWith(".nt"))
					modelTmp.read(url.openStream(),null,"N-TRIPLE");
				else
					modelTmp.read(url.openStream(),null);

				//
				// Test the location
				//
				new RepositoryImpl(modelTmp);

				//
				// If now exception was thrown, add the location to the list
				// and update the user repository
				//
				mod.begin();
				mod.add(modelTmp);
				mod.commit();
			}
			catch ( Exception e ) {
				log.warning("Failed to load location\n"+e.toString());
				bRes = false;
			}
		}

		qr.refreshCache();

		return bRes;
	}

	/** Returns a JSON object containing the list of components.
	 *
	 * @param sUser The user making the request
     * @param sOrder The sorting order ("date" or "name" for now) - or null if no sorting required
     * @param sLimit The maximum number of values to be returned (or -1 if no limit)
	 * @return The JSON object with the list
	 * @throws IOException A problem arised
	 */
	public static JSONObject getListOfComponents ( String sUser, String sOrder, int limit )
	throws IOException {

		QueryableRepository qr = Store.getRepositoryStore(sUser);
        Collection<Resource> lstComponents;

        if ( sOrder != null || limit > 0 )
            lstComponents = getComponentsOrderedBy(qr.getModel(), sOrder, limit);
        else
            lstComponents = qr.getAvailableExecutableComponents();

		JSONObject joRes = new JSONObject();
		JSONArray ja = new JSONArray();

		try {
			for ( Resource res:lstComponents ) {
				JSONObject jo = new JSONObject();
				jo.put("meandre_uri", res.toString());
				ja.put(jo);
			}
			joRes.put("meandre_executable_component",ja);

		}
		catch ( Exception e ) {
			throw new IOException(e.toString());
		}

		return joRes;
	}


	/** Returns a string containing the list of components.
	 *
	 * @param sUser The user making the request
     * @param sOrder The sorting order ("date" or "name" for now) - or null if no sorting required
     * @param sLimit The maximum number of values to be returned (or -1 if no limit)
	 * @return The string list
	 * @throws IOException A problem arised
	 */
	public static String getListOfComponentsAsTxt ( String sUser, String sOrder, int limit )
	throws IOException {

		StringBuffer sbRes = new StringBuffer();

		QueryableRepository qr = Store.getRepositoryStore(sUser);
        Collection<Resource> lstComponents;

        if ( sOrder != null || limit > 0 )
            lstComponents = getComponentsOrderedBy(qr.getModel(), sOrder, limit);
        else
            lstComponents = qr.getAvailableExecutableComponents();

		for ( Resource res:lstComponents )
			sbRes.append(res.toString()+"\n");

		return sbRes.toString();

	}

    /**
     * Creates a list of components sorted by either date or name and returns the first N (='limit') values
     * @param model  The model containing the components
     * @param sOrder The sorting order ("date" or "name" for now) - or null if no sorting required
     * @param limit  The maximum number of values to be returned (or -1 if no limit)
     * @return The list of components
     */
    private static Collection<Resource> getComponentsOrderedBy(Model model, String sOrder, int limit) {
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
            (limit > 0 ? "LIMIT " + limit : "");

        Query query = QueryFactory.create(QUERY) ;
        QueryExecution exec = QueryExecutionFactory.create(query, model, null);
        ResultSet results = exec.execSelect();

        Collection<Resource> lstComponents = new ArrayList<Resource>();
        while ( results.hasNext() )
            lstComponents.add(results.nextSolution().getResource("component"));

        return lstComponents;
    }

	/** Returns a JSON object containing the list of flows.
	 *
	 * @param sUser The user making the request
     * @param sOrder The sorting order ("date" or "name" for now) - or null if no sorting required
     * @param sLimit The maximum number of values to be returned (or -1 if no limit)
	 * @return The JSON object with the list
	 * @throws IOException A problem arised
	 */
	public static JSONObject getListOfFlows ( String sUser, String sOrder, int limit )
	throws IOException {

		QueryableRepository qr = Store.getRepositoryStore(sUser);
        Collection<Resource> lstFlows;

        if ( sOrder != null || limit > 0 )
            lstFlows = getFlowsOrderedBy(qr.getModel(), sOrder, limit);
        else
            lstFlows = qr.getAvailableFlows();

		JSONObject joRes = new JSONObject();
		JSONArray ja = new JSONArray();

		try {
			for ( Resource res:lstFlows ) {
				JSONObject jo = new JSONObject();
				jo.put("meandre_uri", res.toString());
				ja.put(jo);
			}
			joRes.put("meandre_flow_component",ja);

		}
		catch ( Exception e ) {
			throw new IOException(e.toString());
		}

		return joRes;

	}

	/** Returns a string containing the list of flows.
	 *
	 * @param sUser The user making the request
	 * @param sOrder The sorting order ("date" or "name" for now) - or null if no sorting required
	 * @param sLimit The maximum number of values to be returned (or -1 if no limit)
	 * @return The string list
	 * @throws IOException A problem arised
	 */
	public static String getListOfFlowsAsTxt ( String sUser, String sOrder, int limit )
	throws IOException {

		StringBuffer sbRes = new StringBuffer();
		QueryableRepository qr = Store.getRepositoryStore(sUser);
        Collection<Resource> lstFlows;

        if ( sOrder != null || limit > 0 )
            lstFlows = getFlowsOrderedBy(qr.getModel(), sOrder, limit);
        else
            lstFlows = qr.getAvailableFlows();

    	for ( Resource res:lstFlows )
    	    sbRes.append(res.toString()+"\n");

		return sbRes.toString();
	}

    /**
     * Creates a list of flows sorted by either date or name and returns the first N (='limit') values
     * @param model  The model containing the flows
     * @param sOrder The sorting order ("date" or "name" for now) - or null if no sorting required
     * @param limit  The maximum number of values to be returned (or -1 if no limit)
     * @return The list of flows
     */
    private static Collection<Resource> getFlowsOrderedBy(Model model, String sOrder, int limit) {
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
            (limit > 0 ? "LIMIT " + limit : "");

        Query query = QueryFactory.create(QUERY) ;
        QueryExecution exec = QueryExecutionFactory.create(query, model, null);
        ResultSet results = exec.execSelect();

        Collection<Resource> lstFlows = new ArrayList<Resource>();
        while ( results.hasNext() )
            lstFlows.add(results.nextSolution().getResource("flow"));

        return lstFlows;
    }


	/** Returns a JSON object containing the list of tags in the user repository.
	 *
	 * @param sUser The user making the request
	 * @return The JSON object with the list
	 * @throws IOException A problem arised
	 */
	public static JSONObject getListOfTags ( String sUser )
	throws IOException {

		QueryableRepository qr = Store.getRepositoryStore(sUser);

		JSONObject joRes = new JSONObject();
		JSONArray ja = new JSONArray();

		try {
			for ( String sTag:qr.getTags() ) {
				JSONObject jo = new JSONObject();
				jo.put("tag", sTag);
				ja.put(jo);
			}
			joRes.put("meandre_tags",ja);

		}
		catch ( Exception e ) {
			throw new IOException(e.toString());
		}

		return joRes;

	}


	/** Returns a string containing the list of tags.
	 *
	 * @param sUser The user making the request
	 * @return The string list
	 * @throws IOException A problem arised
	 */
	public static String getListOfTagsAsTxt ( String sUser )
	throws IOException {

		StringBuffer sbRes = new StringBuffer();

		QueryableRepository qr = Store.getRepositoryStore(sUser);

		for ( String sTag:qr.getTags() )
			sbRes.append(sTag+"\n");

		return sbRes.toString();

	}


	/** Returns a JSON object containing the list of component tags in the user repository.
	 *
	 * @param sUser The user making the request
	 * @return The JSON object with the list
	 * @throws IOException A problem arised
	 */
	public static JSONObject getListOfComponentTags ( String sUser )
	throws IOException {

		QueryableRepository qr = Store.getRepositoryStore(sUser);

		JSONObject joRes = new JSONObject();
		JSONArray ja = new JSONArray();

		try {
			for ( String sTag:qr.getComponentTags() ) {
				JSONObject jo = new JSONObject();
				jo.put("tag", sTag);
				ja.put(jo);
			}
			joRes.put("meandre_tags",ja);

		}
		catch ( Exception e ) {
			throw new IOException(e.toString());
		}

		return joRes;

	}


	/** Returns a string containing the list of component tags.
	 *
	 * @param sUser The user making the request
	 * @return The string list
	 * @throws IOException A problem arised
	 */
	public static String getListOfComponentTagsAsTxt ( String sUser )
	throws IOException {

		StringBuffer sbRes = new StringBuffer();

		QueryableRepository qr = Store.getRepositoryStore(sUser);

		for ( String sTag:qr.getComponentTags() )
			sbRes.append(sTag+"\n");

		return sbRes.toString();

	}


	/** Returns a JSON object containing the list of flow tags in the user repository.
	 *
	 * @param sUser The user making the request
	 * @return The JSON object with the list
	 * @throws IOException A problem arised
	 */
	public static JSONObject getListOfFlowTags ( String sUser )
	throws IOException {

		QueryableRepository qr = Store.getRepositoryStore(sUser);

		JSONObject joRes = new JSONObject();
		JSONArray ja = new JSONArray();

		try {
			for ( String sTag:qr.getFlowTags() ) {
				JSONObject jo = new JSONObject();
				jo.put("tag", sTag);
				ja.put(jo);
			}
			joRes.put("meandre_tags",ja);

		}
		catch ( Exception e ) {
			throw new IOException(e.toString());
		}

		return joRes;

	}


	/** Returns a string containing the list of flow tags.
	 *
	 * @param sUser The user making the request
	 * @return The string list
	 * @throws IOException A problem arised
	 */
	public static String getListOfFlowTagsAsTxt ( String sUser )
	throws IOException {

		StringBuffer sbRes = new StringBuffer();

		QueryableRepository qr = Store.getRepositoryStore(sUser);

		for ( String sTag:qr.getFlowTags() )
			sbRes.append(sTag+"\n");

		return sbRes.toString();

	}

	/** Returns the model description for the requested component URI.
	 *
	 * @param sUser The user making the request
	 * @param sComponent The component URI requested
	 * @return The model
	 */
	public static Model getComponentDescription(String sUser,
			String sComponent) {

		Model modelRes = ModelFactory.createDefaultModel();

		modelRes.setNsPrefix("", "http://www.meandre.org/ontology/");
		modelRes.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
		modelRes.setNsPrefix("rdfs","http://www.w3.org/2000/01/rdf-schema#");
		modelRes.setNsPrefix("dc","http://purl.org/dc/elements/1.1/");

		Resource resURI = modelRes.createResource(sComponent);
		QueryableRepository qr = Store.getRepositoryStore(sUser);

		ExecutableComponentDescription ecd = qr.getExecutableComponentDescription(resURI);

		if ( ecd!=null ) {
			modelRes.add(ecd.getModel());
		}
		else
			modelRes = null;

		return modelRes;
	}


	/** Returns the model description for the requested flow URI.
	 *
	 * @param sUser The user making the request
	 * @param sComponent The component URI requested
	 * @return The model
	 */
	public static Model getFlowDescription(String sUser,
			String sComponent) {

		Model modelRes = ModelFactory.createDefaultModel();

		modelRes.setNsPrefix("", "http://www.meandre.org/ontology/");
		modelRes.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
		modelRes.setNsPrefix("rdfs","http://www.w3.org/2000/01/rdf-schema#");
		modelRes.setNsPrefix("dc","http://purl.org/dc/elements/1.1/");

		Resource resURI = modelRes.createResource(sComponent);
		QueryableRepository qr = Store.getRepositoryStore(sUser);

		FlowDescription fd = qr.getFlowDescription(resURI);

		if ( fd!=null ) {
			modelRes.add(fd.getModel());
		}
		else
			modelRes = null;

		return modelRes;
	}

	/** Returns a string with the list of searched components.
	 *
	 * @param sUser The requesting user
	 * @param sQuery The query
     * @param sOrder The sort critera (date | name)
     * @param limit The max number or results to return
	 * @return The results
	 */
	public static String getSearchComponentsAsTxt(String sUser, String sQuery, String sOrder, int limit) {
		StringBuffer sbRes = new StringBuffer();

		QueryableRepository qr = Store.getRepositoryStore(sUser);
		Set<Resource> setRes = qr.getAvailableExecutableComponents(sQuery);
        Collection<Resource> lstComponents;

        if ( sOrder != null || limit > 0 ) {
            Model model = ModelFactory.createDefaultModel();

            for ( Resource res:setRes )
                model.add(qr.getExecutableComponentDescription(res).getModel());

            lstComponents = getComponentsOrderedBy(model, sOrder, limit);
        }
        else
            lstComponents = setRes;

        for ( Resource res:lstComponents )
            sbRes.append(res.toString()+"\n");

		return sbRes.toString();
	}

	/** Returns a JSON object containing the list of components searched.
	 *
	 * @param sUser The user making the request
	 * @param sQuery The query
     * @param sOrder The sort critera (date | name)
     * @param limit The max number or results to return
	 * @return The JSON object with the list
	 * @throws IOException A problem arised
	 */
	public static JSONObject getSearchComponentsAsJSON ( String sUser, String sQuery, String sOrder, int limit )
	throws IOException {

		QueryableRepository qr = Store.getRepositoryStore(sUser);
		Set<Resource> setRes = qr.getAvailableFlows(sQuery);
        Collection<Resource> lstComponents;

        if ( sOrder != null || limit > 0 ) {
            Model model = ModelFactory.createDefaultModel();

            for ( Resource res:setRes )
                model.add(qr.getExecutableComponentDescription(res).getModel());

            lstComponents = getComponentsOrderedBy(model, sOrder, limit);
        }
        else
            lstComponents = setRes;

		JSONObject joRes = new JSONObject();
		JSONArray ja = new JSONArray();

		try {
			for ( Resource res:lstComponents ) {
				JSONObject jo = new JSONObject();
				jo.put("meandre_uri", res.toString());
				ja.put(jo);
			}
			joRes.put("meandre_executable_component",ja);

		}
		catch ( Exception e ) {
			throw new IOException(e.toString());
		}

		return joRes;
	}


	/** Returns a string with the list of searched flows.
	 *
	 * @param sUser The requesting user
	 * @param sQuery The query
	 * @param sOrder The sort critera (date | name)
     * @param limit The max number or results to return
     * @return The results
	 */
	public static String getSearchFlowsAsTxt(String sUser, String sQuery, String sOrder, int limit) {
		StringBuffer sbRes = new StringBuffer();

		QueryableRepository qr = Store.getRepositoryStore(sUser);
		Set<Resource> setRes = qr.getAvailableFlows(sQuery);
        Collection<Resource> lstFlows;

        if ( sOrder != null || limit > 0 ) {
            Model model = ModelFactory.createDefaultModel();

            for ( Resource res:setRes )
                model.add(qr.getFlowDescription(res).getModel());

            lstFlows = getFlowsOrderedBy(model, sOrder, limit);
        }
        else
            lstFlows = setRes;

        for ( Resource res:lstFlows )
            sbRes.append(res.toString()+"\n");

		return sbRes.toString();
	}

    /** Returns a JSON object containing the list of flows searched.
	 *
	 * @param sUser The user making the request
	 * @param sQuery The query
	 * @param sOrder The sort critera (date | name)
     * @param limit The max number or results to return
     * @return The JSON object with the list
	 * @throws IOException A problem arised
	 */
	public static JSONObject getSearchFlowsAsJSON(String sUser, String sQuery, String sOrder, int limit)
	throws IOException {

		QueryableRepository qr = Store.getRepositoryStore(sUser);
        Set<Resource> setRes = qr.getAvailableFlows(sQuery);
        Collection<Resource> lstFlows;

        if ( sOrder != null || limit > 0 ) {
            Model model = ModelFactory.createDefaultModel();

            for ( Resource res:setRes )
                model.add(qr.getFlowDescription(res).getModel());

            lstFlows = getFlowsOrderedBy(model, sOrder, limit);
        }
        else
            lstFlows = setRes;

		JSONObject joRes = new JSONObject();
		JSONArray ja = new JSONArray();

		try {
			for ( Resource res:lstFlows ) {
				JSONObject jo = new JSONObject();
				jo.put("meandre_uri", res.toString());
				ja.put(jo);
			}
			joRes.put("meandre_flow_component",ja);

		}
		catch ( Exception e ) {
			throw new IOException(e.toString());
		}

		return joRes;

	}


	/** Returns a string with the list of components by the given tag.
	 *
	 * @param sUser The requesting user
	 * @param sQuery The query
	 * @return The results
	 */
	public static String getComponentsByTagAsTxt(String sUser, String sQuery) {
		StringBuffer sbRes = new StringBuffer();

		QueryableRepository qr = Store.getRepositoryStore(sUser);
		Set<ExecutableComponentDescription> setRes = qr.getComponentsByTag(sQuery);

		if  ( setRes!=null )
			for ( ExecutableComponentDescription res:setRes )
				sbRes.append(res.getExecutableComponent().toString()+"\n");

		return sbRes.toString();
	}

	/** Returns a JSON with the list of components by the given tag.
	 *
	 * @param sUser The user making the request
	 * @param sQuery The query
	 * @return The JSON object with the list
	 * @throws IOException A problem arised
	 */
	public static JSONObject getComponentsByTagAsJSON ( String sUser, String sQuery )
	throws IOException {

		QueryableRepository qr = Store.getRepositoryStore(sUser);

		JSONObject joRes = new JSONObject();
		JSONArray ja = new JSONArray();

		try {
			Set<ExecutableComponentDescription> setRes = qr.getComponentsByTag(sQuery);

			if  ( setRes!=null )
				for ( ExecutableComponentDescription res:setRes ) {
					JSONObject jo = new JSONObject();
					jo.put("meandre_uri", res.getExecutableComponent().toString());
					ja.put(jo);
				}
			joRes.put("meandre_executable_component",ja);

		}
		catch ( Exception e ) {
			throw new IOException(e.toString());
		}

		return joRes;

	}



	/** Returns a string with the list of flows by the given tag.
	 *
	 * @param sUser The requesting user
	 * @param sQuery The query
	 * @return The results
	 */
	public static String getFlowsByTagAsTxt(String sUser, String sQuery) {
		StringBuffer sbRes = new StringBuffer();

		QueryableRepository qr = Store.getRepositoryStore(sUser);
		Set<FlowDescription> setRes = qr.getFlowsByTag(sQuery);

		if  ( setRes!=null )
			for ( FlowDescription res:setRes )
				sbRes.append(res.getFlowComponent().toString()+"\n");

		return sbRes.toString();
	}

	/** Returns a JSON with the list of flows by the given tag.
	 *
	 * @param sUser The user making the request
	 * @param sQuery The query
	 * @return The JSON object with the list
	 * @throws IOException A problem arised
	 */
	public static JSONObject getFlowsByTagAsJSON ( String sUser, String sQuery )
	throws IOException {

		QueryableRepository qr = Store.getRepositoryStore(sUser);

		JSONObject joRes = new JSONObject();
		JSONArray ja = new JSONArray();

		try {
			Set<FlowDescription> setRes = qr.getFlowsByTag(sQuery);

			if  ( setRes!=null )
				for ( FlowDescription res:setRes ) {
					JSONObject jo = new JSONObject();
					jo.put("meandre_uri", res.getFlowComponent().toString());
					ja.put(jo);
				}
			joRes.put("meandre_flow_component",ja);

		}
		catch ( Exception e ) {
			throw new IOException(e.toString());
		}

		return joRes;

	}

	/** Adds components and flows to the user repository.
	 *
	 * @param request The request object
	 * @param sExtension The extension format
	 * @param baDump The array to set the dump flag
	 * @return The model containing the flow
	 * @throws FileUploadException An exception araised while uploading the model
	 */
	@SuppressWarnings("unchecked")
	public static Model addToRepository(HttpServletRequest request, String sExtension, boolean [] baDump)
	throws IOException, FileUploadException {

		boolean bEmbed = false;
		boolean bOverwrite = false;
		Model modelAcc = ModelFactory.createDefaultModel();
		ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
		List lstItems = upload.parseRequest(request);
		Iterator<FileItem> itr = lstItems.iterator();
		HashSet<ExecutableComponentDescription> setComponentsToAdd = new HashSet<ExecutableComponentDescription>();
		Hashtable<String,byte[]> htContextBytes = new Hashtable<String,byte[]>();

		// The user repository
		QueryableRepository qr = Store.getRepositoryStore(request.getRemoteUser());

		while(itr.hasNext()) {
			FileItem item = itr.next();
		    // Get the name of the field
			String fieldName = item.getFieldName();

			// check if the current item is a form field or an uploaded file
			if(fieldName.equals("repository")) {

				ByteArrayInputStream bais = new ByteArrayInputStream(item.get());

				Model modelTmp = ModelFactory.createDefaultModel();

				modelTmp.setNsPrefix("", "http://www.meandre.org/ontology/");
				modelTmp.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
				modelTmp.setNsPrefix("rdfs","http://www.w3.org/2000/01/rdf-schema#");
				modelTmp.setNsPrefix("dc","http://purl.org/dc/elements/1.1/");

				//
				// Read the location and check its consistency
				//
				if ( sExtension.equals("ttl"))
					modelTmp.read(bais,null,"TTL");
				else if ( sExtension.equals("nt"))
					modelTmp.read(bais,null,"N-TRIPLE");
				else
					modelTmp.read(bais,null);

				//
				// Accumulate the models
				//
				modelAcc.add(modelTmp);
			}
			// Check if we need to upload jar files
			else if(fieldName.equals("context")) {
				String sFile = item.getName();
				htContextBytes.put(sFile, item.get());
			}
			else if ( fieldName.equals("embed") ) {
				String sValue = item.getString();
				bEmbed = sValue.equals("true");
			}
			else if ( fieldName.equals("dump") ) {
				String sValue = item.getString();
				baDump[0] = sValue.equals("true");
			}
			else if ( fieldName.equals("overwrite") ) {
				String sValue = item.getString();
				bOverwrite = sValue.equals("true");
			}
		}

		// TODO: Check that this uploaded contexts are really proper contexts :D
		//
		// Check the uploaded description
		//
		QueryableRepository qrNew = new RepositoryImpl(modelAcc);

		//
		// Add to the user repository
		//
		// Adding components
		for ( Resource resComp:qrNew.getAvailableExecutableComponents() ) {
			if ( qr.getExecutableComponentDescription(resComp)==null || bOverwrite) {
				// Component does not exist
				// Add to the set to modify before adding to the repository repository
				setComponentsToAdd.add(qrNew.getExecutableComponentDescription(resComp));
			}
			else {
				// Component does exist
				log.warning("Component "+resComp+" already exist in "+request.getRemoteUser()+" repository. Discarding it.");
			}
		}
		// Adding flows
		Model modUser = qr.getModel();
		modUser.begin();
		for ( Resource resFlow:qrNew.getAvailableFlows() ) {
			if ( qr.getFlowDescription(resFlow)==null ) {
				// Component does not exist
				modUser.add(qrNew.getFlowDescription(resFlow).getModel());
			}
			else {
				if ( bOverwrite ) {
					log.info("Overwriting flow "+resFlow);
					modUser.remove(qr.getFlowDescription(resFlow).getModel());
					modUser.add(qrNew.getFlowDescription(resFlow).getModel());
				}
				else
					// Component does exist
					log.warning("Flow "+resFlow+" already exist in "+request.getRemoteUser()+" repository. Discarding it. No overwrite flag provided.");
			}
		}

		// Adding the components after adding the contexts
		URL urlRequest = new URL(request.getRequestURL().toString());
		Set<String> setFiles = htContextBytes.keySet();
		boolean bWriteOnce = true;
		for ( ExecutableComponentDescription ecd:setComponentsToAdd) {
			if ( htContextBytes.keySet().isEmpty() ) {
				if ( qr.getAvailableExecutableComponents().contains(ecd.getExecutableComponent()) ) {
					if ( bOverwrite ) {
						modUser.remove(qr.getExecutableComponentDescription(ecd.getExecutableComponent()).getModel());
						modUser.add(ecd.getExecutableComponent().getModel());
					}
					else
						log.warning("Discarding upload of the existem component "+ecd.getExecutableComponent()+". No overwrite flag provided.");
				}
				else {
					modUser.add(ecd.getModel());
				}
			}
			else {
				if ( (bEmbed && ecd.getRunnable().equals("java") && ecd.getFormat().equals("java/class") ) ||
					 (ecd.getRunnable().equals("python") && ecd.getFormat().equals("jython")) ) {
					//
					// Embed all the context per descriptor
					//
					for ( String sFile:setFiles) {
						Literal lit = modUser.createTypedLiteral(htContextBytes.get(sFile),XSDDatatype.XSDbase64Binary);
						ecd.getContext().add(lit);
					}
				}
				else if ( bWriteOnce ) {
					//
					// Dump the context file to the disc only once
					//
					for ( String sFile:setFiles) {
						// Dump the files to disk
						new File(Store.getPublicResourcesDirectory()+File.separator+"contexts"+File.separator+"java"+File.separator).mkdirs();
			    		File savedFile = new File(Store.getPublicResourcesDirectory()+File.separator+"contexts"+File.separator+"java"+File.separator+sFile);
						try {
							FileOutputStream fos = new FileOutputStream(savedFile);
							fos.write(htContextBytes.get(sFile));
							fos.close();
						} catch (Exception e) {
							log.warning("Problems writing context "+sFile+" to "+savedFile.getAbsolutePath());
							throw new IOException(e.toString());
						}
						// Add the proper context resources
						Resource res = modUser.createResource(urlRequest.getProtocol()+"://"+urlRequest.getHost()+":"+urlRequest.getPort()+"/public/resources/contexts/java/"+sFile);
						ecd.getContext().add(res);
					}
					ecd.getModel().write(System.out,"TTL");
					// Avoid dumping them multiple times
					bWriteOnce = false;
				}

				if ( qr.getAvailableExecutableComponents().contains(ecd.getExecutableComponent()) ) {
					if ( bOverwrite ) {
						modUser.remove(qr.getExecutableComponentDescription(ecd.getExecutableComponent()).getModel());
						modUser.add(ecd.getModel());
					}
					else
						log.warning("Discarding upload of the existem component "+ecd.getExecutableComponent()+". No overwrite flag provided.");
				}
				else {
					modUser.add(ecd.getModel());
				}
			}

		}
		//Commiting changes
		modUser.commit();

		// Regenerate the cache after adding
		qr.refreshCache();

		return modUser;
	}


	/** Adds a flow to the user repository.
	 *
	 * @param request The request object
	 * @param sExtension The extension format
	 * @return The model containing the flow
	 * @throws FileUploadException An exception araised while uploading the model
	 */
	@SuppressWarnings("unchecked")
	public static Model addFlowsToRepository(HttpServletRequest request, String sExtension)
	throws IOException, FileUploadException {

		// Read the uploaded descriptor into a model
		String sFlowsDesc = request.getParameter("repository");
		boolean bOverwrite = false;
		String sReq = request.getParameter("overwrite");
		if ( sReq!=null )
				bOverwrite = sReq.equals("true");

		Model modNew = ModelFactory.createDefaultModel();

		modNew.setNsPrefix("", "http://www.meandre.org/ontology/");
		modNew.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
		modNew.setNsPrefix("rdfs","http://www.w3.org/2000/01/rdf-schema#");
		modNew.setNsPrefix("dc","http://purl.org/dc/elements/1.1/");

		StringReader srModel = new StringReader(sFlowsDesc);

		if ( sExtension.equals("ttl"))
			modNew.read(srModel,null,"TTL");
		else if ( sExtension.equals("nt"))
			modNew.read(srModel,null,"N-TRIPLE");
		else
			modNew.read(srModel,null);

		// Create the returned model
		Model modResult = ModelFactory.createDefaultModel();

		modResult.setNsPrefix("", "http://www.meandre.org/ontology/");
		modResult.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
		modResult.setNsPrefix("rdfs","http://www.w3.org/2000/01/rdf-schema#");
		modResult.setNsPrefix("dc","http://purl.org/dc/elements/1.1/");

		// Generate a repository for the uploaded model
		QueryableRepository qrNew = new RepositoryImpl(modNew);

		// The user repository
		QueryableRepository qr = Store.getRepositoryStore(request.getRemoteUser());
		Model modUser = qr.getModel();

		for ( FlowDescription fd:qrNew.getAvailableFlowDecriptions()) {
			if ( !qr.getAvailableFlows().contains(fd.getFlowComponent())) {
				// The model for the flow to add
				Model fdModel = fd.getModel();
				// Add to the user repository
				modUser.begin();
				modUser.add(fdModel);
				modUser.commit();
				// Add to the result model
				modResult.add(fdModel);
			}
			else if ( bOverwrite ) {
				// Flow is there byt needs to be overwritten
				Model modOld = qr.getFlowDescription(fd.getFlowComponent()).getModel();
				Model modRep = qr.getModel();
				modRep.begin();
				modRep.remove(modOld);
				modRep.add(fd.getModel());
				modRep.commit();
			}
			else {
				log.info("Flow already exist and the overwrite flag has not been provided. Discading flow update "+fd.getFlowComponent());
			}
		}

		// Regenerate the cache after adding
		qr.refreshCache();

		return modResult;
	}


	/** Returns a string with the deleted URI if successful, blank otherwise.
	 *
	 * @param sUser The requesting user
	 * @param sURI The URI
	 * @return The results
	 */
	public static String removeURIAsTxt(String sUser, String sURI) {
		String sRes = "";
		QueryableRepository qr = Store.getRepositoryStore(sUser);
		Model modQR = qr.getModel();
		Resource resURI = modQR.createResource(sURI);

		ExecutableComponentDescription ecd = qr.getExecutableComponentDescription(resURI);
	 	FlowDescription fd = qr.getFlowDescription(resURI);

	 	if ( ecd!=null ) {
	 		// Removing flow
	 		modQR.begin();
	 		modQR.remove(ecd.getModel());
	 		modQR.commit();
	 		sRes = sURI;
	 	}
	 	else if ( fd!=null ) {
	 		// Removing component
	 		modQR.begin();
	 		modQR.remove(fd.getModel());
	 		modQR.commit();
	 		sRes = sURI;
	 	}

	 	// Regenerate the cache after adding
		qr.refreshCache();

		return sRes;
	}


	/** Returns a string with the deleted URI if successful, blank otherwise.
	 *
	 * @param sUser The requesting user
	 * @param sURI The URI
	 * @return The results
	 * @throws IOException Some problem arise
	 */
	public static JSONObject removeURIAsJSON(String sUser, String sURI) throws IOException {
		JSONObject joRes = new JSONObject();

		try {
			QueryableRepository qr = Store.getRepositoryStore(sUser);
			Model modQR = qr.getModel();
			Resource resURI = modQR.createResource(sURI);

			ExecutableComponentDescription ecd = qr.getExecutableComponentDescription(resURI);
		 	FlowDescription fd = qr.getFlowDescription(resURI);


		 	if ( ecd!=null ) {
		 		// Removing flow
		 		modQR.begin();
		 		modQR.remove(ecd.getModel());
		 		modQR.commit();
		 		joRes.put("meandre_uri", sURI);
		 	}
		 	else if ( fd!=null ) {
		 		// Removing component
		 		modQR.begin();
		 		modQR.remove(fd.getModel());
		 		modQR.commit();
		 		joRes.put("meandre_uri", sURI);
		 	}

		 	// Regenerate the cache after adding
			qr.refreshCache();

		}
		catch ( Exception e ) {
			throw new IOException(e.toString());
		}

		return joRes;
	}


}
