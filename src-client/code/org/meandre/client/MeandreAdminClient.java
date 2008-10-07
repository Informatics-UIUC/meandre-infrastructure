package org.meandre.client;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.httpclient.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.meandre.core.security.Role;
import org.meandre.core.security.SecurityManager;
import org.meandre.core.security.SecurityStoreException;
import org.meandre.core.security.User;

public class MeandreAdminClient extends MeandreBaseClient 
implements SecurityManager{

    /** base of the urls used as remote calls for the admin functions.
     * urls end up being something like:
     * "http://{host}:{port}/{CMD_BASE}{CMD_XXXXX}.{ext}
     *
     * CMD_XXXX can be CMD_ROLES_OF_USER, CMD_USERS ....
	 * ext is json, xml, ...
     */
    public static final String CMD_BASE = "services/security/";

    ////
    //TODO: these constants are cut and paste from WSSecurity, need to
    //find them a common home. (It's like this b/c WSSecurity is not
    //packaged in the jar file(s) used by client apps)
    /////
    
    public static final String CMD_ROLES_OF_USER = "roles_of_user";
    public static final String CMD_USERS = "users";
    public static final String CMD_USER = "user";
    public static final String CMD_VALID_ROLES = "valid_roles";
    public static final String CMD_ASSIGN_ROLE = "assign_roles";
    public static final String CMD_REVOKE_ROLE = "revoke_roles";
    public static final String CMD_CREATE_USER = "create_users";
    public static final String CMD_REMOVE_USER = "remove_users";
    public static final String CMD_REVOKE_ALL_ROLES = "revoke_all_roles";

    public static final String PARAM_USER_NAME = "user_name";
    public static final String PARAM_FULL_NAME = "user_full_name";
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_ROLE = "role_name";
    /**
     * initialize to talk to a particular server. 
     *
     * @param serverHost just the hostname, e.g. "localhost", not "http://localhost"
     * @param serversPort the port on the serverhost that the server is listening
     */
    public MeandreAdminClient(String serverHost, int serversPort){
        super(serverHost, serversPort);
    }
    
    
    public User createUser(String nickName, String name, String password)
            throws SecurityStoreException {
        String sRestCommand = CMD_BASE + CMD_CREATE_USER + ".json";

        Set<NameValuePair> nvps = new HashSet<NameValuePair>();
        nvps.add(new NameValuePair(PARAM_USER_NAME, nickName));
        nvps.add(new NameValuePair(PARAM_FULL_NAME, name));        
        nvps.add(new NameValuePair(PARAM_PASSWORD, password));        
		User usr = null;
		try{
			log("calling remote create user");
	        JSONTokener jtUser = executeGetRequestJSON(sRestCommand, nvps);
	        log("remote create user done.");
	        JSONArray jaUsers = new JSONArray(jtUser);
    	    usr = User.fromJSON(jaUsers.getJSONObject(0));
		}catch(TransmissionException te){
			throw new SecurityStoreException(te);
		}catch(JSONException je){
			throw new SecurityStoreException(je);
		}
        return usr;
    }
    
    /**
     * retrieves what access privileges the input user has assigned.
     */
    public Set<Role> getRolesOfUser(User usr) throws SecurityStoreException {
        String sRestCommand = CMD_BASE + CMD_ROLES_OF_USER + ".json";
		Set<NameValuePair> nvps = new HashSet<NameValuePair>();
       	nvps.add(new NameValuePair(PARAM_USER_NAME, usr.getNickName()));       

		Set<Role> roles = null;
		try{
	        JSONTokener jtRoles = executeGetRequestJSON(sRestCommand, nvps);
    	    roles = Role.setFromJSON(new JSONArray(jtRoles));
		}catch(TransmissionException te){
			throw new SecurityStoreException(te);
		}catch(JSONException je){
			throw new SecurityStoreException(je);
		}
        return roles;
    }

    public User getUser(String nickName) throws SecurityStoreException {
        String sRestCommand = CMD_BASE + CMD_USER + ".json";
		Set<NameValuePair> nvps = new HashSet<NameValuePair>();
       	nvps.add(new NameValuePair(PARAM_USER_NAME, nickName));
		User usr = null;
		try{
	        JSONTokener jtUser = executeGetRequestJSON(sRestCommand, nvps);
	        JSONArray ja = new JSONArray(jtUser);
    	    usr = User.fromJSON(ja.getJSONObject(0));
		}catch(TransmissionException te){
			throw new SecurityStoreException(te);
		}catch(JSONException je){
			throw new SecurityStoreException(je);
		}
        return usr;
    }
    
    /** retrieve a list of all users from the remote Meandre server. 
     * @throws Exception */
    public Set<User> getUsers() throws SecurityStoreException {
        String sRestCommand = CMD_BASE + CMD_USERS + ".json";
		Set<User> users = null;
		try{
	        JSONTokener jtUsers = executeGetRequestJSON(sRestCommand, null);
    	    users = User.setFromJSON(new JSONArray(jtUsers));
		}catch(TransmissionException te){
			throw new SecurityStoreException(te);
		}catch(JSONException je){
			throw new SecurityStoreException(je);
		}
        return users;
    }

 /*   public Set<String> getUsersNickNames() {
        // TODO Auto-generated method stub
        return null;
    }
    */


    public void grantRole(User usr, Role roleToGrant)
            throws SecurityStoreException {
        String sRestCommand = CMD_BASE + CMD_ASSIGN_ROLE + ".json";

        Set<NameValuePair> nvps = new HashSet<NameValuePair>();
        nvps.add(new NameValuePair(PARAM_USER_NAME, usr.getNickName()));
        nvps.add(new NameValuePair(PARAM_ROLE, roleToGrant.getUrl()));

		//User retrievedUsr = null;
		try{
	        JSONTokener jtUser = executeGetRequestJSON(sRestCommand, nvps);
    	    //retrievedUsr = User.fromJSON(new JSONObject(jtUser));
    	    User.fromJSON(new JSONObject(jtUser));
		}catch(TransmissionException te){
			throw new SecurityStoreException(te);
		}catch(JSONException je){
			throw new SecurityStoreException(je);
		}
        return;
    }

	/**
	 * grants a set of roles to a given user. Actually this implementation
	 * makes multiple calls to grantRole, and will therefore make a call over the 
	 * network for each role in the input set.
	 */
    public void grantRoles(User usr, Set<Role> rolesToGrant)
            throws SecurityStoreException {
        for(Role rl: rolesToGrant){
			grantRole(usr, rl);
		}
		return;
    }

	/**
	 * calls getRolesOfUsers to retrieve the roles of the user from
	 * the server and then checks it for the input role
	 */
    public boolean hasRoleGranted(User usr, Role roleToCheck)
            throws SecurityStoreException {

		Set<Role> usersRoles = getRolesOfUser(usr);
		boolean hasRole = usersRoles.contains(roleToCheck);
		return hasRole;
    }

    public void revokeAllRoles(User usr) throws SecurityStoreException {
		String sRestCommand = CMD_BASE + CMD_REVOKE_ALL_ROLES + ".json";
		Set<NameValuePair> nvps = new HashSet<NameValuePair>();
       	nvps.add(new NameValuePair(PARAM_USER_NAME, usr.getNickName()));
		//User retrievedUsr = null;
		try{
	        JSONTokener jtUser = executeGetRequestJSON(sRestCommand, nvps);
    	    //usr = User.fromJSON(new JSONObject(jtUser));
    	    User.fromJSON(new JSONObject(jtUser));
		}catch(TransmissionException te){
			throw new SecurityStoreException(te);
		}catch(JSONException je){
			throw new SecurityStoreException(je);
		}
        return;
        
    }

    public void revokeRole(User usr, Role roleToRevoke)
            throws SecurityStoreException {

      	String sRestCommand = CMD_BASE + CMD_REVOKE_ROLE + ".json";
		Set<NameValuePair> nvps = new HashSet<NameValuePair>();
       	nvps.add(new NameValuePair(PARAM_USER_NAME, usr.getNickName()));
        nvps.add(new NameValuePair(PARAM_ROLE, roleToRevoke.getUrl()));
		//User retrievedUsr = null;
		try{
	        JSONTokener jtUser = executeGetRequestJSON(sRestCommand, nvps);
	        //retrievedUsr = User.fromJSON(new JSONObject(jtUser));
	        User.fromJSON(new JSONObject(jtUser));
		}catch(TransmissionException te){
			throw new SecurityStoreException(te);
		}catch(JSONException je){
			throw new SecurityStoreException(je);
		}
        return;
    }

	/** this implementation just makes multiple calls to revokeRole over
	 * the input set of rolesToRevoke.
	 */
    public void revokeRoles(User usr, Set<Role> rolesToRevoke)
            throws SecurityStoreException {
       for(Role rl : rolesToRevoke){
		   revokeRole(usr, rl);
	   }
	   return;
    }
    
    /**
     * deletes a user account and all roles assigned to it from the server. 
     */
    public void removeUser(User usr) throws SecurityStoreException {
   		String sRestCommand = CMD_BASE + CMD_REMOVE_USER + ".json";
		Set<NameValuePair> nvps = new HashSet<NameValuePair>();
       	nvps.add(new NameValuePair(PARAM_USER_NAME, usr.getNickName()));
		//User retrievedUsr = null;
		try{
	        JSONTokener jtUser = executeGetRequestJSON(sRestCommand, nvps);
	        //retrievedUsr = User.fromJSON(new JSONObject(jtUser));
	        JSONArray jaUsers = new JSONArray(jtUser);
	        User.fromJSON(jaUsers.getJSONObject(0));
		}catch(TransmissionException te){
			throw new SecurityStoreException(te);
		}catch(JSONException je){
			throw new SecurityStoreException(je);
		}
        return;     
    }

    /**
     * a list of all roles the server supports.
     */
    public Set<Role> getValidRoles() throws SecurityStoreException {
        String sRestCommand = CMD_BASE + CMD_VALID_ROLES + ".json";

		Set<Role> roles = null;
		try{
	        JSONTokener jtRoles = executeGetRequestJSON(sRestCommand, null);
	        JSONArray jaRoles = new JSONArray(jtRoles);
    	    roles = Role.setFromJSON(jaRoles);
		}catch(TransmissionException te){
			throw new SecurityStoreException(te);
		}catch(JSONException je){
			throw new SecurityStoreException(je);
		}
        return roles;
		
    }

    /** 
     * converts a given json tokener (which presumably contains an http
     * response in json form) and creates a user object out of the json data.
     *
    private User unpackJSONToUser(JSONTokener jt){

    }*/

    private void log(String msg){
    	System.out.println("MeandreAdminClient: " + msg);
    }

}
