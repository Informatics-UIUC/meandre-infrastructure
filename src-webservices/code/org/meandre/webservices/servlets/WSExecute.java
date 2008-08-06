package org.meandre.webservices.servlets;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.XML;
import org.meandre.configuration.CoreConfiguration;
import org.meandre.core.engine.ConductorException;
import org.meandre.core.repository.CorruptedDescriptionException;
import org.meandre.core.security.Role;
import org.meandre.core.security.SecurityManager;
import org.meandre.core.security.SecurityStoreException;
import org.meandre.core.security.User;
import org.meandre.core.store.Store;
import org.meandre.webservices.controllers.WSExecuteLogic;
import org.meandre.webservices.logger.WSLoggerFactory;
import org.meandre.webui.WebUIException;

/** A basic handler to execute flows.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class WSExecute extends HttpServlet {

    /** A default serial ID */
	private static final long serialVersionUID = 1L;
	
	/** The logger for the WebServices */
	private static Logger log = WSLoggerFactory.getWSLogger();
	
	/** The store to use */
	private Store store;
	
	/** THe execute logic object */
	private WSExecuteLogic wsExecuteLogic;
	
	/** Creates a basic execution servlet for the given store.
	 * 
	 * @param store The store to use
	 * @param cnf The core configuracion object
	 */
	public WSExecute(Store store, CoreConfiguration cnf ) {
		this.store = store;
		this.wsExecuteLogic = new WSExecuteLogic(store,cnf);
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
    	

    	String [] saParts = new URL(request.getRequestURL().toString()).getPath().split("\\.");
   		String sTarget = saParts[0];
		String sExtension = "";

    	if ( saParts.length==2 ) {
    		sExtension = saParts[1];
    	}
    	
    	if (requestorHasRole(request, Role.EXECUTION)) {
    		if ( sTarget.endsWith("/flow") ) {
    			if ( sExtension.equals("txt") )
    				executeTxt(request,response);
    			else if ( sExtension.equals("silent") )
    				executeTxtSilently(request,response);
    			else  {
    				// 
    				// Invalid format found
    				//
    				log.info("Uknown format requested "+sExtension);
    				response.sendError(HttpServletResponse.SC_NOT_FOUND);
    			}	
    		}
    		else if ( sTarget.endsWith("/list_running_flows") ) {
    			if ( sExtension.equals("txt") )
    				listRunningFlowsTxt(request, response);
    			else if ( sExtension.equals("json") )
    				listRunningFlowsJson(request, response);
    			else if ( sExtension.equals("xml") )
    				listRunningFlowsXML(request, response);
    			else  {
    				// 
    				// Invalid format found
    				//
    				log.info("Uknown format requested "+sExtension);
    				response.sendError(HttpServletResponse.SC_NOT_FOUND);
    			}	
    			
    		}
    		else if(sTarget.endsWith("/url")){
    			if ( sExtension.equals("txt") )
    				getFlowURL(request,response);
    			else{
    				// 
    				// Invalid format found
    				//
    				log.info(sTarget +" Uknown format requested "+sExtension);
    				response.sendError(HttpServletResponse.SC_NOT_FOUND);
    			}	
    				
    		
    		}
    		else if(sTarget.endsWith("/web_component_url")){
    			if ( sExtension.equals("txt") )
    				getWebComponentURLForFlow(request,response);
    			else{
    				// 
    				// Invalid format found
    				//
    				log.info(sTarget +" Uknown format requested "+sExtension);
    				response.sendError(HttpServletResponse.SC_NOT_FOUND);
    			}	
    				
    		}
    		else if(sTarget.endsWith("/uri_flow")){
    			if ( sExtension.equals("txt") )
    				getFlowURIFromToken(request,response);
    			else{
    				// 
    				// Invalid format found
    				//
    				log.info(sTarget +" Uknown format requested "+sExtension);
    				response.sendError(HttpServletResponse.SC_NOT_FOUND);
    			}
    		}
    		else  {
				// 
				// Invalid request found
				//
				log.info("Uknown execute service requested "+sTarget);
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


	/** Executes the requested flow.
     * 
     * @param request The request object
     * @param response The response object
     * @throws IOException A problem occurred while writing the response
     */
	private void executeTxt(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		
		try {
			wsExecuteLogic.executeVerboseFlowURI(request, response);
		} catch (ConductorException e) {
			log.info("Flow execution error for "+request.getParameter("uri"));
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		} catch (CorruptedDescriptionException e) {
			log.info("Corrupted description execption for "+request.getParameter("uri"));
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
		
	}

	/** Executes the requested flow.
	 * 
	 * @param request The request object
	 * @param response The response object
	 * @throws IOException A problem occurred while writing the response
	 */
	private void executeTxtSilently(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		
		try {
			wsExecuteLogic.executeSilentFlowURI(request, response);
		} catch (ConductorException e) {
			log.info("Flow execution error for "+request.getParameter("uri"));
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		} catch (CorruptedDescriptionException e) {
			log.info("Corrupted description execption for "+request.getParameter("uri"));
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
		
	}


	/** List the set of running flows and the links to their webui's as XML.
	 * @param request The request object
	 * @param response The response object to use
	 * @throws IOException Something went wrong
	 */
    private void listRunningFlowsXML(HttpServletRequest request, HttpServletResponse response) throws IOException {

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/xml");
		try {
			response.getWriter().println(XML.toString(wsExecuteLogic.listRunningFlowsAsJSON(request,response),"meandre_execution"));
		} catch (JSONException e) {
			throw new IOException(e.toString());
		}
		
	}
    
    /** Returns the url where a flow is running along with the hostname and port address
     * */
    private void getFlowURL(HttpServletRequest request, HttpServletResponse response) throws IOException{
    	try {
			wsExecuteLogic.getFlowURL(request,response);
		} catch (IOException e) {
			throw e;
		} catch (WebUIException e) {
			throw new IOException(e.getMessage());
		}
    }
    
    /**This web component url for the flow
     * 
     * @param request
     * @param response
     * @throws IOException
     */
    private void getWebComponentURLForFlow(HttpServletRequest request, HttpServletResponse response) throws IOException{
    	try {
			wsExecuteLogic.getWebComponentURLForFlow(request, response);
		} catch (IOException e) {
			throw e;
		} catch (WebUIException e) {
			throw new IOException(e.getMessage());
		}
    }
    
    /**This function returns flow uri for a token
     * 
     * @param request
     * @param response
     * @throws IOException
     */
    private void getFlowURIFromToken(HttpServletRequest request,
			HttpServletResponse response) throws IOException{
    	try {
			wsExecuteLogic.getFlowURIFromToken(request, response);
		} catch (IOException e) {
			throw e;
		} 
	}

    /** List the set of running flows and the links to their webui's as JSON.
     * @param request The request object
     * @param response The response object to use
     * @throws IOException Something went wrong
     */
	private void listRunningFlowsJson(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text/plain");
		response.getWriter().println(wsExecuteLogic.listRunningFlowsAsJSON(request,response));
		
		
	}

	/** List the set of running flows and the links to their webui's as XML.
	 * @param request The request object
	 * @param response The response object to use
	 * @throws IOException Something went wrong
	 */
	private void listRunningFlowsTxt(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text/plain");
		wsExecuteLogic.listRunningFlowsAsTxt(request,response);
	}
	
	/**
	 * checks to see if the user who issued the http request has a
	 * given meandre role.
	 * 
	 * @param request this request's remote user will be checked 
	 * @param roleToCheck 
	 * @return whether or not that user has the role
	 */
	private boolean requestorHasRole(HttpServletRequest request, 
	        Role roleToCheck){
	    boolean hasRole = false;
	    try{
	        SecurityManager secMan = store.getSecurityStore();
	        User usr = secMan.getUser(request.getRemoteUser());
	        hasRole = secMan.hasRoleGranted(usr, roleToCheck);
	    }catch(SecurityStoreException sse){
	        log.warning("Security Exception while verifying permissions for" + 
	                "role: " + roleToCheck.toString() + 
	                ". Permission being denied");
	        hasRole = false;
	    }
	    return hasRole;

	}


}
