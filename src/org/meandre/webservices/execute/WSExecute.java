package org.meandre.webservices.execute;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.WSCoreBootstrapper;
import org.meandre.core.engine.ConductorException;
import org.meandre.core.store.Store;
import org.meandre.core.store.repository.CorruptedDescriptionException;
import org.meandre.core.store.security.Action;

/** A basic handler to execute flows.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class WSExecute extends HttpServlet {

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
    	
    	String sTarget = request.getRequestURL().toString();
    	
    	if ( sTarget.endsWith("/flow.txt") ) {
    		if ( Store.getSecurityStore().hasGrantedRoleToUser(Action.BASE_ACTION_URL+"/Execution", request.getRemoteUser()) ) {
				executeTxt(request,response);
    		}
    		else {
    			//
    			// Not allowed
    			//
    			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    		}
		}
    	else if ( sTarget.endsWith("/flow.silent") ) {
    		if ( Store.getSecurityStore().hasGrantedRoleToUser(Action.BASE_ACTION_URL+"/Execution", request.getRemoteUser()) ) {
    			executeTxtSilently(request,response);
    		}
    		else {
    			//
    			// Not allowed
    			//
    			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
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

}
