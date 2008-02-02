package org.meandre.webservices.execute;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.XML;
import org.meandre.core.engine.ConductorException;
import org.meandre.core.store.Store;
import org.meandre.core.store.repository.CorruptedDescriptionException;
import org.meandre.core.store.security.Action;
import org.meandre.webservices.utils.WSLoggerFactory;

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
    	
    	if ( Store.getSecurityStore().hasGrantedRoleToUser(Action.BASE_ACTION_URL+"/Execution", request.getRemoteUser()) ) {
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
			WSExecuteLogic.executeVerboseFlowURI(request, response);
		} catch (CorruptedDescriptionException e) {
			log.info("Corrupted repository description found for "+request.getParameter("uri"));
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (ConductorException e) {
			log.info("Flow execution error for "+request.getParameter("uri"));
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
			WSExecuteLogic.executeSilentFlowURI(request, response);
		} catch (CorruptedDescriptionException e) {
			log.info("Corrupted repository description found for "+request.getParameter("uri"));
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (ConductorException e) {
			log.info("Flow execution error for "+request.getParameter("uri"));
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
			response.getWriter().println(XML.toString(WSExecuteLogic.listRunningFlowsAsJSON(request,response),"meandre_execution"));
		} catch (JSONException e) {
			throw new IOException(e.toString());
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
		response.getWriter().println(WSExecuteLogic.listRunningFlowsAsJSON(request,response));
		
		
	}

	/** List the set of running flows and the links to their webui's as XML.
	 * @param request The request object
	 * @param response The response object to use
	 * @throws IOException Something went wrong
	 */
	private void listRunningFlowsTxt(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text/plain");
		WSExecuteLogic.listRunningFlowsAsTxt(request,response);
	}
	


}
