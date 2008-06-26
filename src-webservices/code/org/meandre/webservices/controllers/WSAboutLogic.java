package org.meandre.webservices.controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.meandre.core.security.Role;
import org.meandre.core.security.SecurityManager;
import org.meandre.core.security.SecurityStoreException;
import org.meandre.core.security.User;
import org.meandre.core.store.Store;
import org.meandre.core.utils.Constants;
import org.meandre.plugins.MeandrePlugin;
import org.meandre.plugins.PluginFactory;
import org.meandre.plugins.bean.Plugin;
import org.meandre.webservices.WSCoreBootstrapper;
import org.meandre.webservices.utils.WSLoggerFactory;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

/** The about services logic
 *
 * @author Xavier Llor&agrave;
 * @modified by Amit Kumar: -added globalPluginsJSON function and getServerVersion function
 *  -May 31st 2008
 */
public class WSAboutLogic {

	/** The logger for the WebServices */
	private static Logger log = WSLoggerFactory.getWSLogger();

	/** The store the use */
	private  Store store;
	
	/*XStream used for plugin serialization**/
	static XStream xstream = new  XStream(new JettisonMappedXmlDriver());
	
	/** The about logic for the store.
	 * 
	 * @param store The store
	 */
	public WSAboutLogic(Store store) {
		this.store = store;
	}

	/** Prints the list of user roles.
	 *
	 * @param request The request object
	 * @param response The response object
	 * @throws IOException A problem while printing
	 */
	public void rolesInText(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		PrintWriter pw = response.getWriter();

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text/plain");

		try {
			SecurityManager ss = store.getSecurityStore();
			User usr = ss.getUser(request.getRemoteUser());
			Set<Role> usersRoles = ss.getRolesOfUser(usr);
			for (Role role: usersRoles)
				pw.println(role.getUrl());
		} catch (SecurityStoreException e) {
			log.warning("Security exception "+e.toString());
			throw new IOException(e.toString());
		}

	}

	/** Returns the list of roles as a JSON object.
	 *
	 * @param request The request object
	 * @param response The response object
	 * @return
	 * @throws IOException Something when wrong
	 */
	public JSONObject rolesInJSON (HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		JSONObject joRes = new JSONObject();
		JSONArray ja = new JSONArray();

		try {
	        SecurityManager ss = store.getSecurityStore();
	        User usr = ss.getUser(request.getRemoteUser());
	        Set<Role> usersRoles = ss.getRolesOfUser(usr);
	        for (Role role: usersRoles){
	            JSONObject jo = new JSONObject();
				jo.put("meandre_role", role.getUrl());
				ja.put(jo);
			}
			joRes.put("meandre_user_role",ja);

		}
		catch ( Exception e ) {
			throw new IOException(e.toString());
		}

		return joRes;

	}

	/** Prints the list of all valid role the server supports.
    *
    * @param request The request object
    * @param response The response object
    * @throws IOException A problem while printing
    */
   public void allRolesInText(HttpServletRequest request,
           HttpServletResponse response) throws IOException {

       PrintWriter pw = response.getWriter();

       response.setStatus(HttpServletResponse.SC_OK);
       response.setContentType("text/plain");

       try {
           SecurityManager ss = store.getSecurityStore();
           User usr = ss.getUser(request.getRemoteUser());
           Set<Role> usersRoles = ss.getRolesOfUser(usr);
           Set<Role> validRoles = Role.getStandardRoles();
           for (Role role: validRoles)
               pw.println(role.getUrl());
       } catch (SecurityStoreException e) {
           log.warning("Security exception "+e.toString());
           throw new IOException(e.toString());
       }

   }

   /** Returns the list of roles the server supports as a JSON object.
    *
    * @param request The request object
    * @param response The response object
    * @return
    * @throws IOException Something when wrong
    */
   public JSONObject allRolesInJSON (HttpServletRequest request,
           HttpServletResponse response) throws IOException {

       JSONObject joRes = new JSONObject();
       JSONArray ja = new JSONArray();

       try {
           Set<Role> validRoles = Role.getStandardRoles();
           for (Role role: validRoles){
               JSONObject jo = new JSONObject();
               jo.put("meandre_role", role.getUrl());
               ja.put(jo);
           }
           joRes.put("meandre_user_role",ja);

       }
       catch ( Exception e ) {
           throw new IOException(e.toString());
       }

       return joRes;

   }

	/** Dump the information in plain text
	 *
	 * @param request The request object
	 * @param response The response object
	 * @throws IOException Response exception
	 */
	public void dumpUsingTxt(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter pw = response.getWriter();

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text/plain");

		pw.println("Meandre Execution Engine version "+Constants.MEANDRE_VERSION);
		pw.println("All rigths reserved by DITA, NCSA, UofI (2007).");
		pw.println("2007. All rigths reserved by DITA, NCSA, UofI.");
		pw.println("THIS SOFTWARE IS PROVIDED UNDER University of Illinois/NCSA OPEN SOURCE LICENSE.");
		pw.println();

		Properties prop = store.getAllProperties();

		//
		// Current session information
		//
		pw.println("Current session information:");
		pw.println();
		pw.println("CURRENT_USER_LOGGED_IN = "+request.getRemoteUser());
		pw.println("CURRENT_SESSION_ID = "+request.getSession().getId());
		pw.println("CURRENT_TIME = "+new Date());
		pw.println();

		//
		// Print Meandre properties
		//
		pw.println("Meandre properties:");
		pw.println();
		pw.println("VERSION = "+Constants.MEANDRE_VERSION);
		for ( Object oKey:prop.keySet() ) {
			String sKey = oKey.toString();
			if ( sKey.startsWith("MEANDRE")) {
				String sValue = prop.getProperty(sKey.toString());
				pw.println(sKey.toString()+" = "+sValue);
			}
		}

		//
		// Print DB storage properties
		//
		pw.println();
		pw.println("Database backend storage properties:");
		pw.println();
		for ( Object oKey:prop.keySet() ) {
			String sKey = oKey.toString();
			if ( sKey.startsWith("DB")) {
				String sValue = prop.getProperty(sKey.toString());
				pw.println(sKey.toString()+" = "+sValue);
			}
		}
	}

	/** Dump the information in RDF
	 *
	 * @param request The request object
	 * @param response The response object
	 * @throws IOException Response exception
	 */
	public void dumpUsingRDF(HttpServletRequest request, HttpServletResponse response) throws IOException {
		ServletOutputStream resOutputStream = response.getOutputStream();

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/xml");

		Model model = getPropertyModel(request);

		model.write(resOutputStream,"RDF/XML-ABBREV");

	}


	/** Dump the information in RDF
	 *
	 * @param request The request object
	 * @param response The response object
	 * @throws IOException Response exception
	 */
	public void dumpUsingTTL(HttpServletRequest request, HttpServletResponse response) throws IOException {
		ServletOutputStream resOutputStream = response.getOutputStream();

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text/plain");

		Model model = getPropertyModel(request);

		model.write(resOutputStream,"TTL");

	}


	/** Dump the information in RDF
	 *
	 * @param request The request object
	 * @param response The response object
	 * @throws IOException Response exception
	 */
	public void dumpUsingNT(HttpServletRequest request, HttpServletResponse response) throws IOException {
		ServletOutputStream resOutputStream = response.getOutputStream();

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text/plain");

		Model model = getPropertyModel(request);

		model.write(resOutputStream,"N-TRIPLE");

	}

	/**  Creates a property model
	 *
	 * @param request The request object
	 * @return The model containing the properties
	 */
	public Model getPropertyModel(HttpServletRequest request) {

		Properties prop = store.getAllProperties();

		Model model;
		model = ModelFactory.createDefaultModel();

		// Setting the name spaces
		model.setNsPrefix("meandreWS", WSCoreBootstrapper.WS_BASE_URL);
		model.setNsPrefix("meandreSC", Store.BASE_REPSITORY_STORE_CONFIG_URL );
		model.setNsPrefix("xsd", XSD.getURI());
		model.setNsPrefix("rdf", RDF.getURI());
		model.setNsPrefix("rdfs",RDFS.getURI());
		model.setNsPrefix("dc",DC.getURI());

		Resource resRoot = model.createResource(WSCoreBootstrapper.WS_BASE_URL);

		// Meandre WS ones
		resRoot.addProperty(ResourceFactory.createProperty(DC.date.getURI()), model.createTypedLiteral(new Date(),XSDDatatype.XSDdateTime));
		resRoot.addProperty(ResourceFactory.createProperty(DC.creator.getURI()), model.createTypedLiteral(request.getRemoteUser()));
		resRoot.addProperty(ResourceFactory.createProperty(WSCoreBootstrapper.WS_BASE_URL+"sessionID"), model.createTypedLiteral(request.getSession().getId()));

		// Meandre ones
		resRoot.addProperty(ResourceFactory.createProperty(WSCoreBootstrapper.WS_BASE_URL+"version"), model.createTypedLiteral(Constants.MEANDRE_VERSION));
		for ( Object oKey:prop.keySet() ) {
			String sKey = oKey.toString();
			if ( sKey.startsWith("MEANDRE")) {
				String sValue = prop.getProperty(sKey.toString());
				resRoot.addProperty(ResourceFactory.createProperty(WSCoreBootstrapper.WS_BASE_URL+sKey.toString().toLowerCase()), model.createTypedLiteral(sValue));
			}
		}

		// Database back end ones
		//
		// Print DB storage properties
		//
		for ( Object oKey:prop.keySet() ) {
			String sKey = oKey.toString();
			if ( sKey.startsWith("DB")) {
				String sValue = prop.getProperty(sKey.toString());
				resRoot.addProperty(ResourceFactory.createProperty(Store.BASE_REPSITORY_STORE_CONFIG_URL+sKey.toString().toLowerCase()), model.createTypedLiteral(sValue));
			}
		}

		return model;
	}

	/** Get Plugin properties*/
	public static String globalPluginsJSON(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		//JSONObject joRes = new JSONObject();
		
		ArrayList<Plugin> alist = new ArrayList<Plugin>(5);
		Properties properties=PluginFactory.getPropPluginFactoryConfig();
		for ( Object oKey:properties.keySet()) {
			try {
				String sClassName = properties.getProperty(oKey.toString());
				MeandrePlugin mpPlugin = (MeandrePlugin) Class.forName(sClassName).newInstance();
				Plugin plugin = new Plugin();
				plugin.setAlias(mpPlugin.getAlias());
				plugin.setClassName(mpPlugin.getPluginClassName());
				plugin.setKey(oKey.toString());
				plugin.setServlet(mpPlugin.isServlet());
				alist.add(plugin);
				
			}catch(Exception ex){
				throw new IOException(ex.toString());
			}
			
		}
		String jsonString=xstream.toXML(alist);
		return jsonString;
	}

	/** returns server version */
	public static String getServerVersion() {
		return "version="+Constants.MEANDRE_VERSION;
	}




}
