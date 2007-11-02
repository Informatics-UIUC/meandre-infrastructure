package org.meandre.webservices.locations;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.meandre.WSCoreBootstrapper;
import org.meandre.core.store.Store;
import org.meandre.core.store.security.Action;
	
/** The class handling the locations interactions.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class WSLocations extends HttpServlet {

    /** A default serial ID */
	private static final long serialVersionUID = 1L;
	
	/** The logger for the bootstrapper */
    protected static Logger log = null;

    // Initializing the logger and its handlers
    static {
        log = Logger.getLogger(WSCoreBootstrapper.class.getName());
        log.setLevel(Level.CONFIG);
        log.addHandler(WSCoreBootstrapper.handler);
    }


	/**
	 * Dispatches web requests for Meandre web services.
	 * 
	 * @param sTarget
	 *            The target path
	 * @param request
	 *            The request object
	 * @param response
	 *            The response object
	 * @param dispath
	 *            The dispatch flag
	 * @throws IOException
	 *             An IO exception arised when processing the request
	 * @throws ServletException
	 *             The servlet could not complete the request
	 */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
    throws ServletException, IOException {
    	
    	String [] saParts = request.getRequestURL().toString().split("\\.");
   		String sTarget = saParts[0];
		String sExtension = "";

    	if ( saParts.length==2 ) {
    		sExtension = saParts[1];
    	}
    	
    	if ( sTarget.endsWith("list") ) {
    		if ( Store.getSecurityStore().hasGrantedRoleToUser(Action.BASE_ACTION_URL+"/Repository", request.getRemoteUser()) ) {
    			if ( sExtension.endsWith("txt") ) {
					listLocationUsingTxt(request,response);
				}
				else if ( sExtension.endsWith("xml") ) {
					listLocationsUsingXML(request,response);
				}
				else if ( sExtension.endsWith("json") ) {
					listLocationsUsingJSON(request,response);
				}
				else  {
					// 
					// Invalid request found
					//
					log.info("Uknown format requested "+sTarget);
					response.sendError(HttpServletResponse.SC_NOT_FOUND);
				}	
    		}
    		else {
    			//
    			// Not allowed
    			//
    			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    		}
    	}
    	else if ( sTarget.endsWith("add") ) {
    		if ( Store.getSecurityStore().hasGrantedRoleToUser(Action.BASE_ACTION_URL+"/Repository", request.getRemoteUser()) ) {
        		if ( sExtension.endsWith("txt") ) {
					addLocationUsingTxt(request,response);
				}
				else if ( sExtension.endsWith("xml") ) {
					addLocationUsingXML(request,response);
				}
				else if ( sExtension.endsWith("json") ) {
					addLocationUsingJSON(request,response);
				}
				else  {
					// 
					// Invalid request found
					//
					log.info("Uknown format requested "+sTarget);
					response.sendError(HttpServletResponse.SC_NOT_FOUND);
				}	
    		}
    		else {
    			//
    			// Not allowed
    			//
    			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    		}	
    	}
    	else if ( sTarget.endsWith("remove") ) {
    		if ( Store.getSecurityStore().hasGrantedRoleToUser(Action.BASE_ACTION_URL+"/Repository", request.getRemoteUser()) ) {
            	if ( sExtension.endsWith("txt") ) {
					removeLocationUsingTxt(request,response);
				}
				else if ( sExtension.endsWith("xml") ) {
					removeLocationUsingXML(request,response);
				}
				else if ( sExtension.endsWith("json") ) {
					removeLocationUsingJSON(request,response);
				}
				else  {
					// 
					// Invalid request found
					//
					log.info("Uknown format requested "+sTarget);
					response.sendError(HttpServletResponse.SC_NOT_FOUND);
				}	
    		}
    		else {
    			//
    			// Not allowed
    			//
    			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    		}	
    	}
    	else {
    		// 
			// Invalid request found
			//
			log.info("Uknown locations service requested "+sTarget);
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
    	}
		
	}

	/** Dump the information in plain text
	 * 
	 * @param request The request object
	 * @param response The response object
	 * @throws IOException Response exception
	 */
	private void listLocationUsingTxt(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		String sUser = request.getRemoteUser();
		PrintWriter pw = response.getWriter();
		
		try {
			JSONObject jo = WSLocationsLogic.listLocationsAsJSONObject(sUser);
			
			JSONArray ja = jo.getJSONArray("location_information");
			
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text/plain");
			
			for ( int i=0, iMax = ja.length() ; i<iMax ; i++ ) {
				pw.println(ja.getJSONObject(i).getString("location"));
				pw.println(ja.getJSONObject(i).getString("description"));
			}
		} catch (JSONException e) {
			log.warning(e.toString());
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		
	}

	/** Dump the information in RDF
	 * 
	 * @param request The request object
	 * @param response The response object
	 * @throws IOException Response exception
	 */
	private void listLocationsUsingXML(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		String sUser = request.getRemoteUser();
		PrintWriter pw = response.getWriter();
		
		try {
			JSONObject jo = WSLocationsLogic.listLocationsAsJSONObject(sUser);
			
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/xml");
			
			pw.println(XML.toString(jo,"locations"));
		} catch (JSONException e) {
			log.warning(e.toString());
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}


	/** Dump the information in RDF
	 * 
	 * @param request The request object
	 * @param response The response object
	 * @throws IOException Response exception
	 */
	private void listLocationsUsingJSON(HttpServletRequest request, HttpServletResponse response) 
	throws IOException {
		
		String sUser = request.getRemoteUser();
		PrintWriter pw = response.getWriter();
		
		 try {
 			JSONObject jo = WSLocationsLogic.listLocationsAsJSONObject(sUser);
 			
 			response.setStatus(HttpServletResponse.SC_OK);
 			response.setContentType("text/plain");
 			
 	 	   	pw.println(jo);
 	   	} catch (JSONException e) {
			log.warning(e.toString());
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	/** Add a location and dumps the results in plain text.
	 * 
	 * @param request The request object
	 * @param response The response object
	 * @throws IOException The response object failed
	 */
	private void addLocationUsingTxt(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		
		String sUser = request.getRemoteUser();
		String sLocation = request.getParameter("location");
		String sDescription = request.getParameter("description");
		PrintWriter pw = response.getWriter();
		
		if ( sLocation==null || sDescription==null ) {
			log.warning("Missing required parameters to "+request.getRequestURL().toString());
			response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
		}
		else  {
			boolean bAddOK = WSLocationsLogic.addLocation(sUser,sLocation,sDescription);
			
			if ( bAddOK ) {
				// Location could be correctly added
				response.setStatus(HttpServletResponse.SC_OK);
				response.setContentType("text/plain");
	 			
				pw.println(sLocation);
				pw.println(sDescription);
			}
			else {
				// Location failed to be added
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			}
		}
	}

	/** Add a location and dumps the results in JSON.
	 * 
	 * @param request The request object
	 * @param response The response object
	 * @throws IOException The response object failed
	 */
	private void addLocationUsingJSON(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		
		String sUser = request.getRemoteUser();
		String sLocation = request.getParameter("location");
		String sDescription = request.getParameter("description");
		PrintWriter pw = response.getWriter();
		
		if ( sLocation==null || sDescription==null ) {
			log.warning("Missing required parameters to "+request.getRequestURL().toString());
			response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
		}
		else  {
			boolean bAddOK = WSLocationsLogic.addLocation(sUser,sLocation,sDescription);
			
			if ( bAddOK ) {
				// Location could be correctly added
				response.setStatus(HttpServletResponse.SC_OK);
				response.setContentType("text/plain");
	 			
				try {
					pw.println(WSLocationsLogic.locationDescriptionToJSON(sLocation, sDescription));
				} catch (JSONException e) {
					throw new IOException(e.toString());
				}
			}
			else {
				// Location failed to be added
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			}
		}	
		
	}
	
	/** Add a location and dumps the results in XML.
	 * 
	 * @param request The request object
	 * @param response The response object
	 * @throws IOException The response object failed
	 */
	private void addLocationUsingXML(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		
		String sUser = request.getRemoteUser();
		String sLocation = request.getParameter("location");
		String sDescription = request.getParameter("description");
		PrintWriter pw = response.getWriter();
		
		if ( sLocation==null || sDescription==null ) {
			log.warning("Missing required parameters to "+request.getRequestURL().toString());
			response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
		}
		else  {
			boolean bAddOK = WSLocationsLogic.addLocation(sUser,sLocation,sDescription);
			
			if ( bAddOK ) {
				// Location could be correctly added
				response.setStatus(HttpServletResponse.SC_OK);
				response.setContentType("application/XML");
	 			
				try {
					pw.println(XML.toString(WSLocationsLogic.locationDescriptionToJSON(sLocation, sDescription),"location_information"));
				} catch (JSONException e) {
					throw new IOException(e.toString());
				}
			}
			else {
				// Location failed to be added
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			}
		}
		
	}
	
	/** Add a location and dumps the results in plain text.
	 * 
	 * @param request The request object
	 * @param response The response object
	 * @throws IOException The response object failed
	 */
	private void removeLocationUsingTxt(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		
		String sUser = request.getRemoteUser();
		String sLocation = request.getParameter("location");
		PrintWriter pw = response.getWriter();
		
		if ( sLocation==null ) {
			log.warning("Missing required parameter location to "+request.getRequestURL().toString());
			response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
		}
		else  {
			boolean bRemoveOK = WSLocationsLogic.removeLocation(sUser,sLocation);
			
			if ( bRemoveOK ) {
				// Location could be correctly added
				response.setStatus(HttpServletResponse.SC_OK);
				response.setContentType("text/plain");
	 			
				pw.println(sLocation);
			}
			else {
				// Location failed to be added
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			}
		}
	}
	
	/** Add a location and dumps the results in JSON.
	 * 
	 * @param request The request object
	 * @param response The response object
	 * @throws IOException The response object failed
	 */
	private void removeLocationUsingJSON(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		
		String sUser = request.getRemoteUser();
		String sLocation = request.getParameter("location");
		PrintWriter pw = response.getWriter();
		
		if ( sLocation==null ) {
			log.warning("Missing required parameter location to "+request.getRequestURL().toString());
			response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
		}
		else  {
			boolean bRemoveOK = WSLocationsLogic.removeLocation(sUser,sLocation);
			
			if ( bRemoveOK ) {
				// Location could be correctly added
				response.setStatus(HttpServletResponse.SC_OK);
				response.setContentType("text/plain");
	 			
				try {
					JSONObject jo = new JSONObject();
					jo.put("location", sLocation);
					pw.println(jo);
				} catch (JSONException e) {
					throw new IOException(e.toString());
				}
				
			}
			else {
				// Location failed to be added
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			}
		}
	}

	
	/** Add a location and dumps the results in XML.
	 * 
	 * @param request The request object
	 * @param response The response object
	 * @throws IOException The response object failed
	 */
	private void removeLocationUsingXML(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		
		String sUser = request.getRemoteUser();
		String sLocation = request.getParameter("location");
		PrintWriter pw = response.getWriter();
		
		if ( sLocation==null ) {
			log.warning("Missing required parameter location to "+request.getRequestURL().toString());
			response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
		}
		else  {
			boolean bRemoveOK = WSLocationsLogic.removeLocation(sUser,sLocation);
			
			if ( bRemoveOK ) {
				// Location could be correctly added
				response.setStatus(HttpServletResponse.SC_OK);
				response.setContentType("application/xml");
	 			
				try {
					JSONObject jo = new JSONObject();
					jo.put("location", sLocation);
					pw.println(XML.toString(jo,"location_information"));
				} catch (JSONException e) {
					throw new IOException(e.toString());
				}
				
			}
			else {
				// Location failed to be added
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			}
		}
	}


}
