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
import org.meandre.core.security.Role;
import org.meandre.core.security.SecurityManager;
import org.meandre.core.security.SecurityStoreException;
import org.meandre.core.security.User;
import org.meandre.core.store.Store;
import org.meandre.webservices.controllers.WSAboutLogic;
import org.meandre.webservices.utils.WSLoggerFactory;

/** A basic handler to display basic information.
 *
 * @author Xavier Llor&agrave;
 * @modified Amit Kumar -Added support for /version and /plugins -May 31st 2008
 */
public class WSAbout extends HttpServlet {

    /** A default serial ID */
	private static final long serialVersionUID = 1L;

	/** The logger for the WebServices */
	private static Logger log = WSLoggerFactory.getWSLogger();

	/** The store to use. */
	private Store store;
	
	/** The about logic object */
	private WSAboutLogic wsAboutLogic;

	/** Creates the about servlet for the given store.
	 * 
	 * @param store The store to use
	 */
	public WSAbout(Store store) {
		this.store = store;
		this.wsAboutLogic = new WSAboutLogic(store);
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
    	
 

    	if ( sTarget.endsWith("/installation") ) {
    	    boolean isAdmin = false;
    	    try{
    	        SecurityManager secStore = store.getSecurityStore();
    	        User usr = secStore.getUser(request.getRemoteUser());
    	        isAdmin = secStore.hasRoleGranted(usr, Role.ADMIN);
    	    }catch(SecurityStoreException sse){
    	        log.warning("Security Manager problem when replying to an" + 
    	                "\'installation\' servlet request, denying permission");
    	        isAdmin = false;
    	    }
    		if (isAdmin){
				if ( sExtension.equals("txt") ) {
					wsAboutLogic.dumpUsingTxt(request,response);
				}
				else if ( sExtension.equals("rdf") ) {
					wsAboutLogic.dumpUsingRDF(request,response);
				}
				else if ( sExtension.equals("ttl") ) {
					wsAboutLogic.dumpUsingTTL(request,response);
				}
				else if ( sExtension.equals("nt") ) {
					wsAboutLogic.dumpUsingNT(request,response);
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
    	else if ( sTarget.endsWith("/user_roles") ) {
    		if ( sExtension.equals("txt") ) {
    			wsAboutLogic.rolesInText(request,response);
			}
    		else if ( sExtension.equals("json") ) {
    			response.setStatus(HttpServletResponse.SC_OK);
				response.getWriter().println(wsAboutLogic.rolesInJSON(request,response));
			}
    		else if ( sExtension.equals("xml") ) {
    			try {
    				response.setContentType("text/xml");
					response.getWriter().println(XML.toString(wsAboutLogic.rolesInJSON(request,response),"meandre_security"));
				} catch (JSONException e) {
					log.warning("XML serialization failure for request "+request.getRequestURL());
					throw new IOException(e.toString());
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
    	else if ( sTarget.endsWith("/valid_roles") ) {
            if ( sExtension.equals("txt") ) {
                wsAboutLogic.allRolesInText(request,response);
            }
            else if ( sExtension.equals("json") ) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println(wsAboutLogic.allRolesInJSON(request,response));
            }
            else if ( sExtension.equals("xml") ) {
                try {
                    response.setContentType("text/xml");
                    response.getWriter().println(XML.toString(wsAboutLogic.allRolesInJSON(request,response),"meandre_security"));
                } catch (JSONException e) {
                    log.warning("XML serialization failure for request "+request.getRequestURL());
                    throw new IOException(e.toString());
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
    	else if ( sTarget.endsWith("/version") ) {
    		if ( sExtension.equals("txt") ) {
    			response.setStatus(HttpServletResponse.SC_OK);
    			response.setContentType("text/plain");
				response.getWriter().println(WSAboutLogic.getServerVersion());
			}else  {
				//
				// Invalid format
				//
				log.info("Invalid format "+sExtension+" for requested "+sTarget);
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}		
    	}
    	else if(sTarget.endsWith("/plugin")){
			if ( sExtension.equals("json") ) {
				response.getWriter().println(WSAboutLogic.globalPluginsJSON(request, response));
			}else  {
				//
				// Invalid format
				//
				log.info("Invalid format "+sExtension+" for requested "+sTarget);
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}	
		}else  {
			//
			// Invalid request found
			//
			log.info("Uknown about service requested "+sTarget);
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}


	}

}
