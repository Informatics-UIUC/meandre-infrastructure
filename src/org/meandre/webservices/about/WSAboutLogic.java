package org.meandre.webservices.about;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.meandre.Bootstrapper;
import org.meandre.core.store.Store;
import org.meandre.core.store.security.SecurityStore;
import org.meandre.core.store.security.SecurityStoreException;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

/** The about services logic
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class WSAboutLogic {

    /** The logger for the bootstrapper */
    protected static Logger log = null;

    // Initializing the logger and its handlers
    static {
        log = Logger.getLogger(Bootstrapper.class.getName());
        log.setLevel(Level.CONFIG);
        log.addHandler(Bootstrapper.handler);
    }


	/** Prints the list of user roles.
	 * 
	 * @param request The request object
	 * @param response The response object
	 * @throws IOException A problem while printing
	 */
	public static void rolesInText(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		
		PrintWriter pw = response.getWriter();
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text/plain");
		
		try {
			SecurityStore ss = Store.getSecurityStore();
			for ( String sRole:(ss.getUser(request.getRemoteUser()).getGrantedActionRoles()) )
				pw.println(sRole);
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
	public static JSONObject rolesInJSON (HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		
		JSONObject joRes = new JSONObject();
		JSONArray ja = new JSONArray();
		
		try {
			SecurityStore ss = Store.getSecurityStore();
			for ( String sRole:(ss.getUser(request.getRemoteUser()).getGrantedActionRoles()) ) {
				JSONObject jo = new JSONObject();
				jo.put("meandre_role", sRole);
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
	public static void dumpUsingTxt(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter pw = response.getWriter();
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text/plain");
		
		pw.println("Meandre Execution Engine version "+Bootstrapper.VERSION);
		pw.println("All rigths reserved by DITA, NCSA, UofI (2007).");
		pw.println("2007. All rigths reserved by DITA, NCSA, UofI.");
		pw.println("THIS SOFTWARE IS PROVIDED UNDER University of Illinois/NCSA OPEN SOURCE LICENSE.");
		pw.println();
		
		Properties prop = Store.getAllProperties();

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
		pw.println("VERSION = "+Bootstrapper.VERSION);
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
	public static void dumpUsingRDF(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
	public static void dumpUsingTTL(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
	public static void dumpUsingNT(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
	public static Model getPropertyModel(HttpServletRequest request) {
		
		Properties prop = Store.getAllProperties();

		Model model;
		model = ModelFactory.createDefaultModel();
		
		// Setting the name spaces
		model.setNsPrefix("meandreWS", Bootstrapper.WS_BASE_URL);
		model.setNsPrefix("meandreSC", Store.BASE_REPSITORY_STORE_CONFIG_URL );
		model.setNsPrefix("xsd", XSD.getURI());
		model.setNsPrefix("rdf", RDF.getURI());
		model.setNsPrefix("rdfs",RDFS.getURI());
		model.setNsPrefix("dc",DC.getURI());

		Resource resRoot = model.createResource(Bootstrapper.WS_BASE_URL);
		
		// Meandre WS ones
		resRoot.addProperty(ResourceFactory.createProperty(DC.date.getURI()), model.createTypedLiteral(new Date(),XSDDatatype.XSDdate));
		resRoot.addProperty(ResourceFactory.createProperty(DC.creator.getURI()), model.createTypedLiteral(request.getRemoteUser()));
		resRoot.addProperty(ResourceFactory.createProperty(Bootstrapper.WS_BASE_URL+"sessionID"), model.createTypedLiteral(request.getSession().getId()));
			
		// Meandre ones
		resRoot.addProperty(ResourceFactory.createProperty(Bootstrapper.WS_BASE_URL+"version"), model.createTypedLiteral(Bootstrapper.VERSION));
		for ( Object oKey:prop.keySet() ) {
			String sKey = oKey.toString();
			if ( sKey.startsWith("MEANDRE")) {
				String sValue = prop.getProperty(sKey.toString());
				resRoot.addProperty(ResourceFactory.createProperty(Bootstrapper.WS_BASE_URL+sKey.toString().toLowerCase()), model.createTypedLiteral(sValue));
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


}
