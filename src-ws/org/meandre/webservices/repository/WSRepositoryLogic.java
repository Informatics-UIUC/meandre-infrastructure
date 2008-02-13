package org.meandre.webservices.repository;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
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

		return bRes;
	}

	/** Returns a JSON object containing the list of components.
	 *
	 * @param sUser The user making the request
	 * @return The JSON object with the list
	 * @throws IOException A problem arised
	 */
	public static JSONObject getListOfComponents ( String sUser )
	throws IOException {

		QueryableRepository qr = Store.getRepositoryStore(sUser);

		JSONObject joRes = new JSONObject();
		JSONArray ja = new JSONArray();

		try {
			for ( Resource res:qr.getAvailableExecutableComponents() ) {
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
	 * @return The string list
	 * @throws IOException A problem arised
	 */
	public static String getListOfComponentsAsTxt ( String sUser )
	throws IOException {

		StringBuffer sbRes = new StringBuffer();

		QueryableRepository qr = Store.getRepositoryStore(sUser);

		for ( Resource res:qr.getAvailableExecutableComponents() )
			sbRes.append(res.toString()+"\n");

		return sbRes.toString();

	}

	/** Returns a JSON object containing the list of flows.
	 *
	 * @param sUser The user making the request
	 * @return The JSON object with the list
	 * @throws IOException A problem arised
	 */
	public static JSONObject getListOfFlows ( String sUser )
	throws IOException {

		QueryableRepository qr = Store.getRepositoryStore(sUser);

		JSONObject joRes = new JSONObject();
		JSONArray ja = new JSONArray();

		try {
			for ( Resource res:qr.getAvailableFlows() ) {
				JSONObject jo = new JSONObject();
				jo.put("meandre_uri", res.toString());
				ja.put(jo);
			}
			joRes.put("meandre_flow_componet",ja);

		}
		catch ( Exception e ) {
			throw new IOException(e.toString());
		}

		return joRes;

	}

	/** Returns a string containing the list of flows.
	 *
	 * @param sUser The user making the request
	 * @return The string list
	 * @throws IOException A problem arised
	 */
	public static String getListOfFlowsAsTxt ( String sUser )
	throws IOException {

		StringBuffer sbRes = new StringBuffer();

		QueryableRepository qr = Store.getRepositoryStore(sUser);

		for ( Resource res:qr.getAvailableFlows() )
			sbRes.append(res.toString()+"\n");

		return sbRes.toString();

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
	public static Model getComponentDesciption(String sUser,
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

		return modelRes;
	}


	/** Returns the model description for the requested flow URI.
	 *
	 * @param sUser The user making the request
	 * @param sComponent The component URI requested
	 * @return The model
	 */
	public static Model getFlowDesciption(String sUser,
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

		return modelRes;
	}

	/** Returns a string with the list of searched components.
	 *
	 * @param sUser The requesting user
	 * @param sQuery The query
	 * @return The results
	 */
	public static String getSearchComponentsAsTxt(String sUser, String sQuery) {
		StringBuffer sbRes = new StringBuffer();

		QueryableRepository qr = Store.getRepositoryStore(sUser);
		Set<Resource> setRes = qr.getAvailableExecutableComponents(sQuery);

		for ( Resource res:setRes )
			sbRes.append(res.toString()+"\n");

		return sbRes.toString();
	}

	/** Returns a JSON object containing the list of components searched.
	 *
	 * @param sUser The user making the request
	 * @param sQuery The query
	 * @return The JSON object with the list
	 * @throws IOException A problem arised
	 */
	public static JSONObject getSearchComponentsAsJSON ( String sUser, String sQuery )
	throws IOException {

		QueryableRepository qr = Store.getRepositoryStore(sUser);

		JSONObject joRes = new JSONObject();
		JSONArray ja = new JSONArray();

		try {
			for ( Resource res:qr.getAvailableExecutableComponents(sQuery) ) {
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
	 * @return The results
	 */
	public static String getSearchFlowsAsTxt(String sUser, String sQuery) {
		StringBuffer sbRes = new StringBuffer();

		QueryableRepository qr = Store.getRepositoryStore(sUser);
		Set<Resource> setRes = qr.getAvailableFlows(sQuery);

		for ( Resource res:setRes )
			sbRes.append(res.toString()+"\n");

		return sbRes.toString();
	}

	/** Returns a JSON object containing the list of flows searched.
	 *
	 * @param sUser The user making the request
	 * @param sQuery The query
	 * @return The JSON object with the list
	 * @throws IOException A problem arised
	 */
	public static JSONObject getSearchFlowsAsJSON ( String sUser, String sQuery )
	throws IOException {

		QueryableRepository qr = Store.getRepositoryStore(sUser);

		JSONObject joRes = new JSONObject();
		JSONArray ja = new JSONArray();

		try {
			for ( Resource res:qr.getAvailableFlows(sQuery) ) {
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
	 * @return The model containing the flow
	 * @throws FileUploadException An exception araised while uploading the model
	 */
	@SuppressWarnings("unchecked")
	public static Model addToRepository(HttpServletRequest request, String sExtension)
	throws IOException, FileUploadException {

		boolean bEmbed = false;
		Model modelTmp = null;
		ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
		List lstItems = upload.parseRequest(request);
		Iterator<FileItem> itr = lstItems.iterator();
		HashSet<ExecutableComponentDescription> setComponentsToAdd = new HashSet<ExecutableComponentDescription>();
		Hashtable<String,byte[]> htContextBytes = new Hashtable<String,byte[]>();

		// The user repository
		QueryableRepository qr = Store.getRepositoryStore(request.getRemoteUser());
		Model modelQR = qr.getModel();
		modelQR.begin();

		while(itr.hasNext()) {
			FileItem item = itr.next();
		    // Get the name of the field
			String fieldName = item.getFieldName();

			// check if the current item is a form field or an uploaded file
			if(fieldName.equals("repository")) {

				ByteArrayInputStream bais = new ByteArrayInputStream(item.get());

				modelTmp = ModelFactory.createDefaultModel();

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
				// Check the uploaded description
				//
				QueryableRepository qrNew = new RepositoryImpl(modelTmp);

				//
				// Add to the user repository
				//
				// Adding components
				for ( Resource resComp:qrNew.getAvailableExecutableComponents() ) {
					if ( qr.getExecutableComponentDescription(resComp)==null ) {
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
				for ( Resource resFlow:qrNew.getAvailableFlows() ) {
					if ( qr.getFlowDescription(resFlow)==null ) {
						// Component does not exist
						modelQR.add(qrNew.getFlowDescription(resFlow).getModel());
					}
					else {
						// Component does exist
						log.warning("Flow "+resFlow+" already exist in "+request.getRemoteUser()+" repository. Discarding it.");
					}
				}
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
		}

		// TODO: Check that this uploaded contexts are really proper contexts :D

		// Adding the components after adding the contexts
		Model modUser = qr.getModel();
		URL urlRequest = new URL(request.getRequestURL().toString());
		Set<String> setFiles = htContextBytes.keySet();
		boolean bWriteOnce = true;
		modUser.begin();
		for ( ExecutableComponentDescription ecd:setComponentsToAdd) {
			if ( htContextBytes.keySet().isEmpty() ) {
				modUser.add(ecd.getModel());
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
					// Avoid dumping them multiple times
					bWriteOnce = false;
				}
				modUser.add(ecd.getModel());
			}

		}
		//Commiting changes
		modUser.commit();
		modelQR.commit();

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
