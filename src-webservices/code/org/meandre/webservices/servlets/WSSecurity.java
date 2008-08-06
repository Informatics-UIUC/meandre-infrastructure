package org.meandre.webservices.servlets;

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
import org.meandre.webservices.controllers.WSSecurityLogic;
import org.meandre.webservices.logger.WSLoggerFactory;

/** 
 * A Handler for role management calls that affect the local security store.
 *
 * @author Peter Groves
 *
 */
public class WSSecurity extends HttpServlet {

    /** A default serial ID */
	private static final long serialVersionUID = 1L;
    
    public static final String CMD_ROLES_OF_USER = "roles_of_user";
    public static final String CMD_USERS = "users";
    public static final String CMD_USER = "user";
    public static final String CMD_VALID_ROLES = "valid_roles";
    public static final String CMD_ASSIGN_ROLE = "assign_role";
    public static final String CMD_REVOKE_ROLE = "revoke_role";
    public static final String CMD_CREATE_USER = "create_user";
    public static final String CMD_REMOVE_USER = "remove_user";
    public static final String CMD_REVOKE_ALL_ROLES = "revoke_all_roles";
    
    public static final String PARAM_USER_NAME = "user_name";
    public static final String PARAM_FULL_NAME = "user_full_name";
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_ROLE = "role_name";
    

	/** The logger for the WebServices */
	private static Logger log = WSLoggerFactory.getWSLogger();

	/** The security controller.  Does the actual translation between
     * http requests and the security store. */
	private WSSecurityLogic _securityLogic;

    /** The local store used to verify remote caller has permission to
     * perform the operation they are attempting.
     */
    private Store _store;

	/** Creates the about servlet for the given store.
	 * 
	 * @param store The store to use
	 */
	public WSSecurity(Store store) {
		_store = store;
		_securityLogic = new WSSecurityLogic(store.getSecurityStore());
	}

	/**
	 * Dispatches web requests for Meandre web services.
	 *
	 * @param request
	 *            The request object
	 * @param response
	 *            The response object
	 * @throws IOException
	 *             An IO exception arised when processing the request
	 * @throws ServletException
	 *             The servlet could not complete the request
	 */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

    	
    	//all functions require role 'admin'
    	if(!requestorHasRole(request, Role.ADMIN)){
    		response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    		return;
     	}
        URL requestUrl =  new URL(request.getRequestURL().toString());
    	String [] saParts = requestUrl.getPath().split("\\.");
   		String sTarget = saParts[0];
		String sExtension = "";

    	if ( saParts.length==2 ) {
    		sExtension = saParts[1];
    	}
        
        if(sTarget.endsWith(CMD_ROLES_OF_USER)){
            _securityLogic.handleRolesOfUser(request, response, sExtension);

        }else if(sTarget.endsWith(CMD_USERS)){
            _securityLogic.handleUsers(request, response, sExtension);

        }else if(sTarget.endsWith(CMD_VALID_ROLES)){
            _securityLogic.handleValidRoles(request, response, sExtension);

        }else if(sTarget.endsWith(CMD_ASSIGN_ROLE)){
            _securityLogic.handleAssignRole(request, response, sExtension);

        }else if(sTarget.endsWith(CMD_REVOKE_ROLE)){
            _securityLogic.handleRevokeRole(request, response, sExtension);

        }else if(sTarget.endsWith(CMD_CREATE_USER)){
        	_securityLogic.handleCreateUser(request, response, sExtension);            

        }else if(sTarget.endsWith(CMD_REMOVE_USER)){
            _securityLogic.handleRemoveUser(request, response, sExtension);

        }else if(sTarget.endsWith(CMD_REVOKE_ALL_ROLES)){
            _securityLogic.handleRevokeAllRoles(request, response, sExtension);

        }else if(sTarget.endsWith(CMD_USER)){
            _securityLogic.handleUser(request, response, sExtension);

        }else{
			// requested url was an invalid request 
			//
			log.info("Unknown Security service requested " + sTarget);
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
            SecurityManager secMan = _store.getSecurityStore();
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
