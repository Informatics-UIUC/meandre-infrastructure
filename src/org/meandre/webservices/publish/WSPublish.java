package org.meandre.webservices.publish;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.WSCoreBootstrapper;
import org.meandre.core.store.Store;
import org.meandre.core.store.security.Action;

/** A basic handler to handle the publishing facility.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class WSPublish extends HttpServlet {

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
    	
    	if ( sTarget.endsWith("/publish") ) {
    		if ( Store.getSecurityStore().hasGrantedRoleToUser(Action.BASE_ACTION_URL+"/Publish", request.getRemoteUser()) ) {
	    		if ( sExtension.equals("txt") ) {
	    			WSPublishLogic.publishURIAsTxt(request,response);
				}
				else if ( sExtension.equals("json") ) {
					WSPublishLogic.publishURIAsJSON(request,response);
				}
				else if ( sExtension.equals("xml") ) {
					WSPublishLogic.publishURIAsXML(request,response);
				}
				else  {
					// 
					// Invalid format
					//
					log.info("Invalid format "+sExtension+" for requested "+sTarget);
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
     	else if ( sTarget.endsWith("/unpublish") ) {
     		if ( Store.getSecurityStore().hasGrantedRoleToUser(Action.BASE_ACTION_URL+"/Publish", request.getRemoteUser()) ) {
	    		if ( sExtension.equals("txt") ) {
					WSPublishLogic.unpublishURIAsTxt(request,response);
				}
				else if ( sExtension.equals("json") ) {
					WSPublishLogic.unpublishURIAsJSON(request,response);
				}
				else if ( sExtension.equals("xml") ) {
					WSPublishLogic.unpublishURIAsXML(request,response);
				}
				else  {
					// 
					// Invalid format
					//
					log.info("Invalid format "+sExtension+" for requested "+sTarget);
					response.sendError(HttpServletResponse.SC_NOT_FOUND);
				}	
     		}
			else  {
				// 
				// Invalid format
				//
				log.info("Invalid format "+sExtension+" for requested "+sTarget);
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}	
    	} 
     	else  {
			// 
			// Invalid request found
			//
			log.info("Uknown about service requested "+sTarget);
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}	
		
		
	}

}
