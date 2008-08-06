package org.meandre.webservices.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Set;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.store.Store;
import org.meandre.core.repository.ExecutableComponentDescription;
import org.meandre.core.repository.FlowDescription;
import org.meandre.core.repository.QueryableRepository;
import org.meandre.core.repository.RepositoryImpl;
import org.meandre.core.store.system.SystemStore;
import org.meandre.webservices.logger.WSLoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/** This class groups all the basic logic required to maintain the locations
 * for the Meandre webservices interface.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class WSLocationsLogic {

	/** The logger for the WebServices */
	private static Logger log = WSLoggerFactory.getWSLogger();
	
	/** The store to use */
	private Store store;

	/** The core configuration object */
	private CoreConfiguration cnf;
	
	/** Creates a location object for the given store.
	 * 
	 * @param cnf The core configuration
	 * @param store The store to use
	 */
    public WSLocationsLogic(CoreConfiguration cnf, Store store) {
		this.cnf = cnf;
    	this.store = store;
	}
	/** Returns a JSON object for the given location.
     * 
     * @param sLocation The location
     * @param sDescription The description
     * @return The JSON object
     * @throws JSONException The location could not be created
     */
	public JSONObject locationDescriptionToJSON ( String sLocation, String sDescription ) throws JSONException {
		JSONObject jo = new JSONObject();

		try {
			jo.put("location", sLocation);
			jo.put("description", sDescription);
		} catch (JSONException e) {
			log.warning("Could not create a JSON location description for "+sLocation+", "+sDescription);
			throw e;
		}
		
		return jo;		
	}
	/** Prepares the results objects
	 * 
	 * @param sUser The user requesting the listing
	 * @return The array of results
	 * @throws IOException
	 * @throws JSONException 
	 */
	public JSONObject listLocationsAsJSONObject ( String sUser )
			throws IOException, JSONException {
		
		SystemStore ss = store.getSystemStore(cnf,sUser);
		
 	    Set<Hashtable<String, String>> setProps = ss.getProperty(SystemStore.REPOSITORY_LOCATION);
		
		JSONObject joRes = new JSONObject();
		JSONArray ja = new JSONArray();
		
		for ( Hashtable<String, String> ht:setProps ) 
			ja.put(locationDescriptionToJSON(ht.get("value"), ht.get("description")));
					
		joRes.put("location_information",ja);
		
		return joRes;
	}

	/** This method adds a location to the user repository. Also checks that is
	 * a valid description of it.
	 * 
	 * @param The user adding the location
	 * @param sLocation
	 * @param sDescription
	 * @return True if the location could be successfully added
	 */
	public boolean addLocation(String sUser, String sLocation, String sDescription) {
		
		boolean bRes = true;
		
		// Retrieve system store
		SystemStore ss = store.getSystemStore(cnf,sUser);
	    
		if ( !isAlreadyAUsedLocation(ss,sLocation) ) {
			//
			// New location
			//
			try {
					
				URL url = new URL(sLocation);
				Model modelTmp = ModelFactory.createDefaultModel();
					
				modelTmp.setNsPrefix("", "http://www.meandre.org/ontology/");
				modelTmp.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
					modelTmp.setNsPrefix("rdfs","http://www.w3.org/2000/01/rdf-schema#");
				modelTmp.setNsPrefix("dc","http://purl.org/dc/elements/1.1/");
	    
				//
				// Read the location and check its consistency
				//
			
				if ( sLocation.endsWith(".ttl"))
					modelTmp.read(url.openStream(),null,"TTL");
				else if ( sLocation.endsWith(".nt"))
					modelTmp.read(url.openStream(),null,"N-TRIPLE");
				else
					modelTmp.read(url.openStream(),null);

				
				//
				// Test the location
				//
				QueryableRepository qrNew = new RepositoryImpl(modelTmp);
				
				//
				// If now exception was thrown, add the location to the list
				// and update the user repository 
				//
				ss.setProperty(SystemStore.REPOSITORY_LOCATION, sLocation, sDescription);
				QueryableRepository qr = store.getRepositoryStore(sUser);
				
				// Modifying the repository
				qr.getModel().begin();
				 
				// Adding components
				for ( ExecutableComponentDescription ecd:qrNew.getAvailableExecutableComponentDescriptions())
					if ( !qr.getAvailableExecutableComponents().contains(ecd.getExecutableComponent()))
						qr.getModel().add(ecd.getModel());
					else
						log.warning("Component "+ecd.getExecutableComponent()+" already exist in the current repository. Discarding it.");
				
				// Adding flows
				for ( FlowDescription fd:qrNew.getAvailableFlowDescriptions())
					if ( !qr.getAvailableFlows().contains(fd.getFlowComponent()))
						qr.getModel().add(fd.getModel());
					else
						log.warning("Flow "+fd.getFlowComponent()+" already exist in the current repository. Discarding it.");
				
				qr.getModel().commit();
				qr.refreshCache();
			}
			catch ( Exception e ) {
				log.warning("WSLocationsLogic.removeLocation: Failed to add location\n"+e.toString());
				bRes = false;
			}
		}
		else {
			//
			// Existing location (update the description)
			//
			ss.removeProperty(SystemStore.REPOSITORY_LOCATION, sLocation);
			ss.setProperty(SystemStore.REPOSITORY_LOCATION, sLocation, sDescription);
		}

		
		return bRes;
	}

	/** Checks if a location already exist on the users system repository.
	 * 
	 * @param ss The users system repository
	 * @param sLocation The location
	 * @return True if the location already exist
	 */
	private boolean isAlreadyAUsedLocation(SystemStore ss, String sLocation) {
		boolean bExist = false;
		Set<Hashtable<String, String>> setProps = ss.getProperty(SystemStore.REPOSITORY_LOCATION);
		
		for ( Hashtable<String, String> ht:setProps )
			if ( ht.get("value").equals(sLocation) ) {
				bExist = true;
				break;
			}

		return bExist;
	}
	
	/** Removes a location from the repository.
	 * 
	 * @param sUser The system store user
	 * @param sLocation The location to remove
	 * @return True if the location could be successfully removed
	 */
	public boolean removeLocation(String sUser, String sLocation) {
		boolean bRes = true;
		
		// Retrieve system store
		SystemStore ss = store.getSystemStore(cnf,sUser);
	    
		if ( !isAlreadyAUsedLocation(ss, sLocation)) {
			//
			// Location does not exist
			//
			bRes = false;
		}
		else {
			//
			// Location does exist
			//
			
			// 
			// Remove the location form the system properties
			//
			ss.removeProperty(SystemStore.REPOSITORY_LOCATION, sLocation);
			
			//
			// Regenerate the users repository
			//
			QueryableRepository qr = store.getRepositoryStore(sUser);
			
			// Cleaning the user repository entries
			Model mod = qr.getModel();
			mod.begin();
			mod.removeAll();
			mod.commit();
			
			// Regenerating the user repository entries
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
					try{
					if ( url.toString().endsWith(".ttl"))
						modelTmp.read(url.openStream(),null,"TTL");
					else if ( url.toString().endsWith(".nt"))
						modelTmp.read(url.openStream(),null,"N-TRIPLE");
					else
						modelTmp.read(url.openStream(),null);
	               }catch(Exception e){
	                    log.warning("WSLocationsLogic.removeLocation: Failed to " +
	                            "add location. Couldnt open url:" +
	                            url.toString() + "\n"+
	                            e.toString());
	                    bRes = false;
	                    return bRes;
	                    
	                }
					QueryableRepository qrNew = new RepositoryImpl(modelTmp);
					
					//
					// If now exception was thrown, add the location to the list
					// and update the user repository 
					//
					mod.begin();
					// Adding components
					for ( ExecutableComponentDescription ecd:qrNew.getAvailableExecutableComponentDescriptions())
						if ( !qr.getAvailableExecutableComponents().contains(ecd.getExecutableComponent()))
							qr.getModel().add(ecd.getModel());
						else
							log.warning("Component "+ecd.getExecutableComponent()+" already exist in the current repository. Discarding it.");
					
					// Adding flows
					for ( FlowDescription fd:qrNew.getAvailableFlowDescriptions())
						if ( !qr.getAvailableFlows().contains(fd.getFlowComponent()))
							qr.getModel().add(fd.getModel());
						else
							log.warning("Flow "+fd.getFlowComponent()+" already exist in the current repository. Discarding it.");
					mod.commit();
					qr.refreshCache();
				}
				catch ( Exception e ) {
					log.warning("WSLocationsLogic.removeLocation: Failed to load location\n"+e.toString());
					bRes = false;
				}
			}
		}
		
		return bRes;
	}



}
