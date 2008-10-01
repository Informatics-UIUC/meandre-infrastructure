package org.meandre.webservices.deprecated.servlets;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.core.security.Role;
import org.meandre.core.security.SecurityManager;
import org.meandre.core.security.SecurityStoreException;
import org.meandre.core.security.User;
import org.meandre.core.store.Store;
import org.meandre.webservices.deprecated.controller.WSPublishLogic;
import org.meandre.webservices.logger.WSLoggerFactory;

/** A basic handler to handle the publishing facility.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class WSPublish extends HttpServlet {

    /** A default serial ID */
	private static final long serialVersionUID = 1L;
	
	/** The logger for the WebServices */
	private static Logger log = WSLoggerFactory.getWSLogger();
	
	/** The store to use */
	private Store store;

	/** The publish logic object */
	private WSPublishLogic wsPublishLogic;
	
	/** Creates the publishing servlet for the given store.
	 * 
	 * @param store The store to use
	 */
	public WSPublish(Store store) {
		this.store = store;
		this.wsPublishLogic = new WSPublishLogic(store);
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
    	
    	if ( sTarget.endsWith("/publish") ) {
    	    if(requestorHasRole(request, Role.PUBLISH)){
	    		if ( sExtension.equals("txt") ) {
	    			wsPublishLogic.publishURIAsTxt(request,response);
				}
				else if ( sExtension.equals("json") ) {
					wsPublishLogic.publishURIAsJSON(request,response);
				}
				else if ( sExtension.equals("xml") ) {
					wsPublishLogic.publishURIAsXML(request,response);
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
            if(requestorHasRole(request, Role.PUBLISH)){
                if ( sExtension.equals("txt") ) {
	    			wsPublishLogic.unpublishURIAsTxt(request,response);
				}
				else if ( sExtension.equals("json") ) {
					wsPublishLogic.unpublishURIAsJSON(request,response);
				}
				else if ( sExtension.equals("xml") ) {
					wsPublishLogic.unpublishURIAsXML(request,response);
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
