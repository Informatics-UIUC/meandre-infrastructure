package org.meandre.webservices.controllers;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.core.security.Role;
import org.meandre.core.security.SecurityManager;
import org.meandre.core.security.SecurityStoreException;
import org.meandre.core.security.User;
import org.meandre.webservices.servlets.WSSecurity;
import org.meandre.webservices.utils.WSLoggerFactory;



/** 
 * Controller for privileges (role) management via web services.
 *
 * @author Peter Groves
 *
 */
public class WSSecurityLogic {

	/** The logger for the WebServices. */
	@SuppressWarnings("unused")
	private static Logger log = WSLoggerFactory.getWSLogger();

	/** The security store that will be manipulated by http calls. */
	private  SecurityManager _securityStore;
	
	/** The about logic for the store.
	 * 
	 * @param store The store
	 */
	public WSSecurityLogic(SecurityManager store) {
		_securityStore = store;
	}

	
	///////////////////////////////////
	//Handlers  for web services calls
	//////////////////////////////////
	
    /**
     * handles a servlet request for the all users in the
     * system. 
     *
     * @param sReplyFormatExtension the file format extension the reply should
     * have. 'txt', 'xml' or 'json'
     */
    public void handleUsers(HttpServletRequest request, 
            HttpServletResponse response, String sReplyFormatExtension){
        
        Set<User> allUsers = null;
        try{
            allUsers = _securityStore.getUsers();
        }catch(SecurityStoreException sse){
            replyWithSecurityError(request, response, sse, WSSecurity.CMD_USERS);
            return;
        }
        replyWithUsers(allUsers, request, response, 
                sReplyFormatExtension, WSSecurity.CMD_USERS);
        return;
    }

    /**
     * handles a servlet request for a user in the
     * system given it's nickname. verifies it is a valid user. 
     *
     * @param sReplyFormatExtension the file format extension the reply should
     * have. 'txt', 'xml' or 'json'
     */
    public void handleUser(HttpServletRequest request, 
            HttpServletResponse response, String sReplyFormatExtension){
        
        User usr = null;
        String sUserName = extractUserName(request);
        if(sUserName == null){
            replyWithMissingParamError(request, response, 
                    WSSecurity.PARAM_USER_NAME, 
                    WSSecurity.CMD_USER);
            return;
        }
        try{
            usr = _securityStore.getUser(sUserName);
        }catch(SecurityStoreException sse){
            replyWithSecurityError(request, response, sse, WSSecurity.CMD_USER);
            return;
        }
        replyWithUser(usr, request, response, 
                sReplyFormatExtension, WSSecurity.CMD_USER);
    }

    /**
     *  handles a servlet request for the roles of a user (specified in
     *  a param of the request). The output roles are listed by their full url
     *  identifier, one per line.
     */
    public void handleRolesOfUser(HttpServletRequest request, 
            HttpServletResponse response, String sReplyFormatExtension){

        String sUserName = extractUserName(request);
        if(sUserName == null){
            replyWithMissingParamError(request, response, 
                    WSSecurity.PARAM_USER_NAME, 
                    WSSecurity.CMD_ROLES_OF_USER);
            return;
        }
        
        Set<Role> roles = null;
        try{
            User usr = _securityStore.getUser(sUserName);
            roles = _securityStore.getRolesOfUser(usr);
        }catch(SecurityStoreException sse){
            replyWithSecurityError(request, response, sse, 
                    WSSecurity.CMD_ROLES_OF_USER);
            return;
        }
        
        replyWithRoles(roles, request, response, sReplyFormatExtension,
                WSSecurity.CMD_ROLES_OF_USER);
        return;
    }

    /**
     * handles a request for the roles supported by this system.
     */
    public void handleValidRoles(HttpServletRequest request, 
            HttpServletResponse response, String sReplyFormatExtension){

        Set<Role> roles = Role.getStandardRoles();
        replyWithRoles(roles, request, response, sReplyFormatExtension,
                WSSecurity.CMD_VALID_ROLES);
    }

    /** handles a request to grant a new role to a user. replies with
     * the user if the user has the role after the call.
     */
    public void handleAssignRole(HttpServletRequest request, 
            HttpServletResponse response, String sReplyFormatExtension){
        String sUserName = extractUserName(request);
        if(sUserName == null){
            replyWithMissingParamError(request, response, 
                    WSSecurity.PARAM_USER_NAME, 
                    WSSecurity.CMD_ASSIGN_ROLE);
            return;
        }
        Role role = extractRole(request);
        if(role == null){
            replyWithMissingParamError(request, response, 
                    WSSecurity.PARAM_ROLE, 
                    WSSecurity.CMD_ASSIGN_ROLE);
            return;
        }
        User usr = null;
        try{
            usr = _securityStore.getUser(sUserName);
            _securityStore.grantRole(usr, role);
        }catch(SecurityStoreException sse){
            replyWithSecurityError(request, response, sse, 
                    WSSecurity.CMD_ASSIGN_ROLE);
            return;
        }
        replyWithUser(usr, request, response, sReplyFormatExtension, 
                WSSecurity.CMD_ASSIGN_ROLE);
        return;
    }

    /** handles a request to remove a role from a user's privileges.
     */
    public void handleRevokeRole(HttpServletRequest request, 
            HttpServletResponse response, String sReplyFormatExtension){

        String sUserName = extractUserName(request);
        if(sUserName == null){
            replyWithMissingParamError(request, response, 
                    WSSecurity.PARAM_USER_NAME,
                    WSSecurity.CMD_REVOKE_ROLE);
            return;
        }
        Role role = extractRole(request);
        if(role == null){
            replyWithMissingParamError(request, response, 
                    WSSecurity.PARAM_ROLE,
                    WSSecurity.CMD_REVOKE_ROLE);
            return;
        }
        User usr = null;
        try{
            usr = _securityStore.getUser(sUserName);
            _securityStore.revokeRole(usr, role);
        }catch(SecurityStoreException sse){
            replyWithSecurityError(request, response, sse,
                WSSecurity.CMD_REVOKE_ROLE);
            return;
        }
        replyWithUser(usr, request, response, sReplyFormatExtension, 
                WSSecurity.CMD_REVOKE_ROLE);
        return;

    }
    public void handleCreateUser(HttpServletRequest request, 
            HttpServletResponse response, String sReplyFormatExtension){
        
        String sUserName = extractUserName(request);
        if(sUserName == null){
            replyWithMissingParamError(request, response, 
                    WSSecurity.PARAM_USER_NAME,
                    WSSecurity.CMD_CREATE_USER);
            return;
        }

        String sUserFullName = extractUserFullName(request);
        if(sUserFullName == null){
            replyWithMissingParamError(request, response, 
                    WSSecurity.PARAM_FULL_NAME,
                    WSSecurity.CMD_CREATE_USER);
            return;
        }

        String sPassword = extractPassword(request);
        if(sUserFullName == null){
            replyWithMissingParamError(request, response, 
                    WSSecurity.PARAM_PASSWORD,
                    WSSecurity.CMD_CREATE_USER);
            return;
        }

        User usr = null;
        try{
            usr = _securityStore.createUser(sUserName, sUserFullName, sPassword);
        }catch(SecurityStoreException sse){
            replyWithSecurityError(request, response, sse,
                    WSSecurity.CMD_CREATE_USER);
            return;
        }
        replyWithUser(usr, request, response, sReplyFormatExtension, 
                WSSecurity.CMD_CREATE_USER);
        return;
    }


    public void handleRemoveUser(HttpServletRequest request, 
            HttpServletResponse response, String sReplyFormatExtension){

        String sUserName = extractUserName(request);
        if(sUserName == null){
            replyWithMissingParamError(request, response, 
                    WSSecurity.PARAM_USER_NAME, WSSecurity.CMD_REMOVE_USER);
            return;
        }
        User usr = null;
        try{
            usr = _securityStore.getUser(sUserName);
            _securityStore.removeUser(usr);
        }catch(SecurityStoreException sse){
            replyWithSecurityError(request, response, sse,
                    WSSecurity.CMD_REMOVE_USER);
            return;
        }
        replyWithUser(usr, request, response, sReplyFormatExtension, 
                WSSecurity.CMD_REMOVE_USER);
        return;
    }

    public void handleRevokeAllRoles(HttpServletRequest request, 
            HttpServletResponse response, String sReplyFormatExtension){
        String sUserName = extractUserName(request);
        if(sUserName == null){
            replyWithMissingParamError(request, response, 
                    WSSecurity.PARAM_USER_NAME, WSSecurity.CMD_REVOKE_ALL_ROLES);
            return;
        }
        User usr = null;
        try{
            usr = _securityStore.getUser(sUserName);
            _securityStore.revokeAllRoles(usr);
        }catch(SecurityStoreException sse){
            replyWithSecurityError(request, response, sse, 
                WSSecurity.CMD_REVOKE_ALL_ROLES);
            return;
        }
        replyWithUser(usr, request, response, sReplyFormatExtension, 
                WSSecurity.CMD_REVOKE_ALL_ROLES);
        return;
    }

    ////////////////////////
    //http param extraction
    ///////////////////////
    
    /**
     * extracts a user name parameter value from an http request. will
     * return null if the request does not contain a user name 
     * parameter.
     */
    private String extractUserName(HttpServletRequest request){
        String name = request.getParameter(WSSecurity.PARAM_USER_NAME);
        return name;
    }

    private String extractUserFullName(HttpServletRequest request){
        String fname = request.getParameter(WSSecurity.PARAM_FULL_NAME);
        return fname;
    }

    private String extractPassword(HttpServletRequest request){
        String name = request.getParameter(WSSecurity.PARAM_PASSWORD);
        return name;
    }


    /**
     * extracts a role name (role identifier) parameter value from an http 
     * request. will return null if the request does not contain a role name
     * parameter.
     */
    private Role extractRole(HttpServletRequest request){
        String sRole = request.getParameter(WSSecurity.PARAM_ROLE);
        Role rl = Role.fromUrl(sRole);
        return rl;
    }


    ////////////////
    //Bean Responses
    ////////////////

    /**
     * responds to a http request with a set of Users. generates a string
     * representation of the users in a format determined by 
     * sReplyFormatExtension and writes it to the response.
     *
     * @param sReplyFormatExtension 
     * acceptable formats are 'txt', 'xml', and 'json'. If another format
     * is requested, an error message will be written to the response.
     *
     * @param sRestCommand is an identifier for the original command that
     * was issued that this response is for. only used to log errors.
     */
    private void replyWithUsers(Set<User> users, HttpServletRequest request, 
            HttpServletResponse response, String sReplyFormatExtension,
            String sRestCommand){
        
        if(sReplyFormatExtension.equals("txt")) {
            String str = User.setToText(users);
            replyWithText(request, response, str);
        }else if (sReplyFormatExtension.equals("xml")){
            try{
                String str = User.setToXml(users);
                replyWithXml(request, response, str);
            }catch(Exception jse){
                replyWithGeneralError(request, response, jse, sRestCommand);
            }
        }else if (sReplyFormatExtension.equals("json")){
            try{
                String str = User.setToJSON(users).toString();
                replyWithText(request, response, str);
            }catch(Exception jse){
                replyWithGeneralError(request, response, jse, sRestCommand);
            }          
        }else{
            replyWithBadExtensionError(request, response, sReplyFormatExtension,
                    sRestCommand);
        }

    }




    private void replyWithUser(User usr, HttpServletRequest request, 
            HttpServletResponse response, String sReplyFormatExtension,
            String sRestCommand){

        if(sReplyFormatExtension.equals("txt")) {
            String str = usr.toString();
            replyWithText(request, response, str);
        }else if (sReplyFormatExtension.equals("xml")){
            try{
                String str = usr.toXml();
                replyWithXml(request, response, str);
            }catch(Exception jse){
                replyWithGeneralError(request, response, jse, sRestCommand);
            }
        }else if (sReplyFormatExtension.equals("json")){
            try{
                String str = usr.toJSON().toString();
                replyWithText(request, response, str);
            }catch(Exception jse){
                replyWithGeneralError(request, response, jse, sRestCommand);
            }          
        }else{
            replyWithBadExtensionError(request, response, sReplyFormatExtension,
                    sRestCommand);
        }
        return;
    }

    /**
     * responds to a http request with a set of Roles. generates a string
     * representation of the roles in a format determined by 
     * sReplyFormatExtension and writes it to the response.
     *
     * @param sReplyFormatExtension 
     * acceptable formats are 'txt', 'xml', and 'json'. If another format
     * is requested, an error message will be written to the response.
     *
     * @param sRestCommand is an identifier for the original command that
     * was issued that this response is for. only used to log errors.
     */
    private void replyWithRoles(Set<Role> roles, HttpServletRequest request, 
            HttpServletResponse response, String sReplyFormatExtension,
            String sRestCommand){
        
        if(sReplyFormatExtension.equals("txt")) {
            String str = Role.setToText(roles);
            replyWithText(request, response, str);
        }else if (sReplyFormatExtension.equals("xml")){
            try{
                String str = Role.setToXML(roles);
                replyWithXml(request, response, str);
            }catch(Exception jse){
                replyWithGeneralError(request, response, jse, sRestCommand);
            }
        }else if (sReplyFormatExtension.equals("json")){
            try{
                String str = Role.setToJSON(roles).toString();
                replyWithText(request, response, str);
            }catch(Exception jse){
                replyWithGeneralError(request, response, jse, sRestCommand);
            }          
        }else{
            replyWithBadExtensionError(request, response, sReplyFormatExtension,
                    sRestCommand);
        }
        return;
    }
    
    ////////////////////
    //Formatted Responses
    ////////////////////
    private void replyWithXml(HttpServletRequest request,
            HttpServletResponse response, String xmlContent) {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/xml");
        try {
            response.getWriter().println(xmlContent);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void replyWithText(HttpServletRequest request,
            HttpServletResponse response, String textContent) {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/plain");
        try {
            response.getWriter().println(textContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return;
    }

    //////////////////
    //Error responses
    //////////////////
    
    /**
     * used to handle a request when the request uses an invalid extension
     * for the type of info it's requesting (eg user requested
     * http://blah/blah.nt but only valid extensions are "txt, json").
     */
    private void replyWithBadExtensionError(HttpServletRequest request, 
            HttpServletResponse response, String sRequestedExtension,
            String sRestCommand){

        //TODO: transmit exception, not just human readable err message
        String sResponse = "Bad Extension: " + sRequestedExtension;
        sResponse += "\nIn Calling Command: sRestCommand";
        try{
            response.getWriter().println(sResponse);
        }catch(IOException ioe){
            //FIXME
        }

    }

    /**
     * used to handle a request when the SecurityManager did not allow
     * an operation. responds to the request using info from the
     * securityStoreException, which is assumed to be what triggered the
     * invalid security condition.
     */
    private void replyWithSecurityError(HttpServletRequest request, 
            HttpServletResponse response, SecurityStoreException sse,
            String sRestCommand){

        //TODO: transmit exception, not just human readable err message
        String sResponse = "SecurityError: " + sse.toString();
        sResponse += "\nIn Calling Command: " + sRestCommand;
        try{
            response.getWriter().println(sResponse);
        }catch(IOException ioe){
            ioe.printStackTrace();
    
        }
    }


    /**
     * handles a response when the http call required a parameter that was
     * not present.
     */
    private void replyWithMissingParamError(HttpServletRequest request, 
            HttpServletResponse response, String sExpectedHttpParam,
            String sRestCommand){
        //TODO: transmit exception, not just human readable err message
        String sResponse = "Missing HTTP Param: " + sExpectedHttpParam;
        sResponse += "\nIn Calling Command: " + sRestCommand;
        try{
            response.getWriter().println(sResponse);
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
    
    /**
     * handles a response when any Exception has occured. prints the
     * exception into the response. This is the catch-all error
     * propagation through the webservices and should only be used as a 
     * last resort.
     * 
     * @param request the original http request
     * @param response  the http response to respond with
     * @param exc the exception to be sent via this servlet call
     * @param sRestCommand the webservices command being called when the
     * exception occurred
     */
    private void replyWithGeneralError(HttpServletRequest request,
            HttpServletResponse response, Exception exc,
            String sRestCommand) {
        
        String sResponse = "Meandre Error: " + exc.toString();
        sResponse += "\nIn Calling Command:" + sRestCommand;
        try{
            response.getWriter().println(sResponse);
        }catch(IOException ioe){
            ioe.printStackTrace();
            
        }       
    }

    
}
